package net.alek.succorstadiums.client.render.arena;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.alek.succorstadiums.network.arena.ArenaActionPayload;
import net.alek.succorstadiums.network.arena.ArenaDataPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

public final class ArenaRadiusRenderer {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Wall extends this far below / above the arena center Y (it fades out toward the top)
    private static final double WALL_BELOW = 4.0;
    private static final double WALL_ABOVE = 16.0;

    // Half the width of the flat ring drawn on the ground at the arena edge (0.06 = a 0.12 block wide line)
    private static final double RING_HALF_WIDTH = 0.06;

    private static final int MIN_SEGMENTS = 32;
    private static final int MAX_SEGMENTS = 128;

    // How often (in ticks) fresh arena data is requested from the server while any overlay is on
    private static final int POLL_INTERVAL_TICKS = 20;

    // Arenas whose edge is farther than this from the camera are not drawn; at most this many are drawn (nearest first)
    private static final double MAX_DRAW_DISTANCE = 256.0;
    private static final int MAX_ARENAS_DRAWN = 16;

    // Floating text: shown when the player is within LABEL_RANGE of the arena center
    private static final double LABEL_RANGE = 128.0;
    private static final double LABEL_HEIGHT = 3.0;        // blocks above the arena center Y (top of the 3 lines is higher)
    private static final double LABEL_LINE_SPACING = 0.75; // blocks between text lines
    private static final float NAME_SCALE = 1.0f;
    private static final float INFO_SCALE = 0.7f;

    // Bytes reserved for the vertex buffer (16 arenas at 128 segments is well under this)
    private static final int BUFFER_SIZE = 1 << 21;

    // Color (RGB) used for every arena's circle and name label
    private static final int ARENA_COLOR = 0xFF69B4;

    // Wall alpha is the value at the bottom edge; it fades to fully transparent at the top
    private static final float SPAWN_WALL_ALPHA = 0.35f;
    private static final float SPAWN_RING_ALPHA = 0.90f;

    // Vanilla filled-box pipeline: translucent, depth tested (hidden behind blocks).
    // For see-through-walls, register a copy with an empty depth state like the Fabric docs example.
    private static final RenderPipeline PIPELINE = RenderPipelines.DEBUG_FILLED_BOX;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    // ── State ─────────────────────────────────────────────────────────────────

    // Client copy of the arena data (replaced wholesale on every update, so readers never see a half-built map)
    private record ArenaInfo(String name, ResourceKey<Level> dimension, double x, double y, double z, int radius) {}

    private static volatile Map<String, ArenaInfo> known = Map.of();

    // Names of the arenas whose overlay is switched on
    private static final Set<String> enabled = ConcurrentHashMap.newKeySet();

    // Immutable snapshot taken during extraction and used while drawing
    private record RadiusState(double x, double y, double z, double radius, int rgb) {}

    private static volatile List<RadiusState> frameStates = List.of();

    private static int pollTicks = 0;
    private static StagedVertexBuffer stagedBuffer = null;
    private static boolean warnedTopology = false;
    private static boolean labelsBroken = false;

    private ArenaRadiusRenderer() {}

    // ── Public API ────────────────────────────────────────────────────────────

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(ArenaRadiusRenderer::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(ArenaRadiusRenderer::renderAndDraw);
        ClientTickEvents.END_CLIENT_TICK.register(ArenaRadiusRenderer::pollArenaData);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> close());
    }

    public static boolean isEnabled(String arenaName) {
        return enabled.contains(arenaName);
    }

    // Switches one arena's overlay on or off and returns whether it is now on. Other arenas are unaffected.
    public static void toggle(ArenaDataPayload.ArenaEntry arena) {
        if (enabled.remove(arena.name())) return;

        // Make sure the arena is known right away, even before the next data update arrives
        Map<String, ArenaInfo> next = new LinkedHashMap<>(known);
        next.put(arena.name(), toInfo(arena));
        known = Collections.unmodifiableMap(next);

        enabled.add(arena.name());
    }

    // Feed this every ArenaDataPayload that arrives, whether or not a screen is open.
    // Refreshes the cached arena data and drops overlays of arenas that no longer exist (deleted or renamed).
    public static void onArenaData(ArenaDataPayload payload) {
        Map<String, ArenaInfo> next = new LinkedHashMap<>();
        for (ArenaDataPayload.ArenaEntry entry : payload.arenas()) {
            next.put(entry.name(), toInfo(entry));
        }
        known = Collections.unmodifiableMap(next);
        enabled.retainAll(next.keySet());
    }

    // ── Data helpers ──────────────────────────────────────────────────────────

    private static ArenaInfo toInfo(ArenaDataPayload.ArenaEntry e) {
        return new ArenaInfo(e.name(), parseDimension(e.dimension()), e.x(), e.y(), e.z(), e.radius());
    }

    private static ResourceKey<Level> parseDimension(String id) {
        try {
            String value = (id == null || id.isBlank()) ? ArenaDataPayload.ArenaEntry.DEFAULT_DIMENSION : id;
            return ResourceKey.create(Registries.DIMENSION, Identifier.parse(value));
        } catch (Exception e) {
            LOGGER.warn("Invalid arena dimension '{}', assuming the Overworld", id);
            return Level.OVERWORLD;
        }
    }

    // ── Live data polling ─────────────────────────────────────────────────────

    private static void pollArenaData(Minecraft client) {
        if (enabled.isEmpty() || client.player == null) {
            pollTicks = 0;
            return;
        }
        if (++pollTicks < POLL_INTERVAL_TICKS) return;
        pollTicks = 0;
        ClientPlayNetworking.send(ArenaActionPayload.requestData());
    }

    private static void clear() {
        enabled.clear();
        known = Map.of();
        frameStates = List.of();
        pollTicks = 0;
    }

    // ── Extraction (read data here, build an immutable state) ─────────────────

    private static void extract(LevelExtractionContext context) {
        Minecraft client = Minecraft.getInstance();
        if (enabled.isEmpty() || client.level == null || client.player == null) {
            frameStates = List.of();
            return;
        }

        ResourceKey<Level> currentDimension = client.level.dimension();
        Vec3 playerPos = client.player.position();
        double labelRangeSqr = LABEL_RANGE * LABEL_RANGE;

        List<RadiusState> next = new ArrayList<>();
        for (String name : enabled) {
            ArenaInfo arena = known.get(name);
            if (arena == null || arena.radius() <= 0) continue;
            if (!arena.dimension().equals(currentDimension)) continue;

            next.add(new RadiusState(arena.x(), arena.y(), arena.z(), arena.radius(), ARENA_COLOR));

            if (playerPos.distanceToSqr(arena.x(), arena.y(), arena.z()) <= labelRangeSqr) {
                submitLabel(arena);
            }
        }
        frameStates = List.copyOf(next);
    }

    // ── Floating text (Mojang's Gizmos API) ───────────────────────────────────

    private static void submitLabel(ArenaInfo arena) {
        if (labelsBroken) return;

        try {
            double topY = arena.y() + LABEL_HEIGHT + 2 * LABEL_LINE_SPACING;

            text(arena.name(), arena.x(), topY, arena.z(), 0xFF000000 | ARENA_COLOR, NAME_SCALE);
            text("Radius: " + arena.radius(),
                    arena.x(), topY - LABEL_LINE_SPACING, arena.z(), 0xFFFFFFFF, INFO_SCALE);
            text("X: " + Mth.floor(arena.x()) + "  Y: " + Mth.floor(arena.y()) + "  Z: " + Mth.floor(arena.z()),
                    arena.x(), topY - 2 * LABEL_LINE_SPACING, arena.z(), 0xFFBBBBBB, INFO_SCALE);
        } catch (RuntimeException e) {
            // Most likely no gizmo collector is active at this point of the frame. Keep the walls working.
            labelsBroken = true;
            LOGGER.error("Arena radius labels disabled: could not submit gizmo text", e);
        }
    }

    private static void text(String text, double x, double y, double z, int argb, float scale) {
        // Empty adjustLeft = centered on the position. Always on top so it stays readable through terrain.
        Gizmos.billboardText(text, new Vec3(x, y, z), new TextGizmo.Style(argb, scale, OptionalDouble.empty()))
                .setAlwaysOnTop();
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private static void renderAndDraw(LevelRenderContext context) {
        List<RadiusState> states = frameStates;
        if (states.isEmpty()) return;

        final Vec3 camera = context.levelState().cameraRenderState.pos;

        // Skip arenas that are too far away, and keep only the nearest ones if there are many
        List<RadiusState> drawList = new ArrayList<>(states.size());
        for (RadiusState s : states) {
            double distance = Math.sqrt(camera.distanceToSqr(s.x(), s.y(), s.z())) - s.radius();
            if (distance <= MAX_DRAW_DISTANCE) drawList.add(s);
        }
        if (drawList.isEmpty()) return;
        if (drawList.size() > MAX_ARENAS_DRAWN) {
            drawList.sort(Comparator.comparingDouble(s -> camera.distanceToSqr(s.x(), s.y(), s.z())));
            drawList = new ArrayList<>(drawList.subList(0, MAX_ARENAS_DRAWN));
        }

        var format = PIPELINE.getVertexFormatBinding(0);
        if (format == null) return;

        PrimitiveTopology primitive = PIPELINE.getPrimitiveTopology();
        if (primitive != PrimitiveTopology.QUADS) {
            if (!warnedTopology) {
                warnedTopology = true;
                LOGGER.error("Arena radius renderer expects a QUADS pipeline but got {}", primitive);
            }
            return;
        }

        if (stagedBuffer == null) {
            stagedBuffer = new StagedVertexBuffer(() -> "Arena Radius Buffer", BUFFER_SIZE);
        }

        var draw = stagedBuffer.appendDraw(format, primitive, RenderSystem.getProjectionType().vertexSorting());

        PoseStack matrices = context.poseStack();
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        Matrix4fc matrix = matrices.last().pose();

        VertexConsumer builder = stagedBuffer.getVertexBuilder(draw);
        for (RadiusState s : drawList) {
            buildGeometry(builder, matrix, s);
        }

        matrices.popPose();

        stagedBuffer.upload();
        var info = stagedBuffer.getExecuteInfo(draw);
        if (info != null) {
            Minecraft client = Minecraft.getInstance();

            var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

            var mainTarget = client.gameRenderer.mainRenderTarget();
            var colorView = mainTarget.getColorTextureView();
            var depthView = mainTarget.getDepthTextureView();

            if (colorView != null) {
                try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> MOD_ID + " arena radius rendering",
                        colorView, Optional.empty(), depthView, OptionalDouble.empty())) {
                    renderPass.setPipeline(PIPELINE);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                    renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
                    renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
                    renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
                }
            }
        }

        stagedBuffer.endFrame();
    }

    private static void buildGeometry(VertexConsumer b, Matrix4fc m, RadiusState s) {
        double yBottom = s.y() - WALL_BELOW;
        double yTop = s.y() + WALL_ABOVE;

        float r = ((s.rgb() >> 16) & 0xFF) / 255f;
        float g = ((s.rgb() >> 8) & 0xFF) / 255f;
        float bl = (s.rgb() & 0xFF) / 255f;
        float[] wallColor = {r, g, bl, SPAWN_WALL_ALPHA};
        float[] ringColor = {r, g, bl, SPAWN_RING_ALPHA};

        int segments = segmentsFor(s.radius());
        wall(b, m, s.x(), s.z(), s.radius(), yBottom, yTop, segments, wallColor);
        flatRing(b, m, s.x(), s.y() + 0.02, s.z(), s.radius(), segments, ringColor);
    }

    private static int segmentsFor(double radius) {
        return Math.clamp((long) Math.ceil(radius * 1.5), MIN_SEGMENTS, MAX_SEGMENTS);
    }

    // Vertical cylinder wall made of one quad per segment
    private static void wall(VertexConsumer b, Matrix4fc m, double cx, double cz, double radius,
                             double yBottom, double yTop, int segments, float[] color) {
        for (int i = 0; i < segments; i++) {
            double a0 = (Math.PI * 2.0) * i / segments;
            double a1 = (Math.PI * 2.0) * (i + 1) / segments;
            double x0 = cx + radius * Math.cos(a0), z0 = cz + radius * Math.sin(a0);
            double x1 = cx + radius * Math.cos(a1), z1 = cz + radius * Math.sin(a1);

            fadingQuad(b, m, color,
                    x0, yBottom, z0,
                    x1, yBottom, z1,
                    x1, yTop, z1,
                    x0, yTop, z0);
        }
    }

    // Flat horizontal ring strip at height y
    private static void flatRing(VertexConsumer b, Matrix4fc m, double cx, double y, double cz,
                                 double radius, int segments, float[] color) {
        double inner = Math.max(0.0, radius - ArenaRadiusRenderer.RING_HALF_WIDTH);
        double outer = radius + ArenaRadiusRenderer.RING_HALF_WIDTH;
        for (int i = 0; i < segments; i++) {
            double a0 = (Math.PI * 2.0) * i / segments;
            double a1 = (Math.PI * 2.0) * (i + 1) / segments;
            double c0 = Math.cos(a0), s0 = Math.sin(a0);
            double c1 = Math.cos(a1), s1 = Math.sin(a1);

            doubleSidedQuad(b, m, color,
                    cx + inner * c0, y, cz + inner * s0,
                    cx + outer * c0, y, cz + outer * s0,
                    cx + outer * c1, y, cz + outer * s1,
                    cx + inner * c1, y, cz + inner * s1);
        }
    }

    // Wall quad given as bottom-left, bottom-right, top-right, top-left. Alpha is the color's alpha at the bottom
    // and 0 at the top. Emitted in both windings like doubleSidedQuad.
    private static void fadingQuad(VertexConsumer b, Matrix4fc m, float[] c,
                                   double x0, double y0, double z0,
                                   double x1, double y1, double z1,
                                   double x2, double y2, double z2,
                                   double x3, double y3, double z3) {
        vertex(b, m, c, c[3], x0, y0, z0);
        vertex(b, m, c, c[3], x1, y1, z1);
        vertex(b, m, c, 0f, x2, y2, z2);
        vertex(b, m, c, 0f, x3, y3, z3);

        vertex(b, m, c, 0f, x3, y3, z3);
        vertex(b, m, c, 0f, x2, y2, z2);
        vertex(b, m, c, c[3], x1, y1, z1);
        vertex(b, m, c, c[3], x0, y0, z0);
    }

    // Emitted in both windings so it shows from either side regardless of the pipeline's face culling
    private static void doubleSidedQuad(VertexConsumer b, Matrix4fc m, float[] c,
                                        double x0, double y0, double z0,
                                        double x1, double y1, double z1,
                                        double x2, double y2, double z2,
                                        double x3, double y3, double z3) {
        vertex(b, m, c, x0, y0, z0);
        vertex(b, m, c, x1, y1, z1);
        vertex(b, m, c, x2, y2, z2);
        vertex(b, m, c, x3, y3, z3);

        vertex(b, m, c, x3, y3, z3);
        vertex(b, m, c, x2, y2, z2);
        vertex(b, m, c, x1, y1, z1);
        vertex(b, m, c, x0, y0, z0);
    }

    private static void vertex(VertexConsumer b, Matrix4fc m, float[] c, double x, double y, double z) {
        vertex(b, m, c, c[3], x, y, z);
    }

    private static void vertex(VertexConsumer b, Matrix4fc m, float[] c, float alpha, double x, double y, double z) {
        b.addVertex(m, (float) x, (float) y, (float) z).setColor(c[0], c[1], c[2], alpha);
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private static void close() {
        if (stagedBuffer != null) {
            stagedBuffer.close();
            stagedBuffer = null;
        }
    }
}