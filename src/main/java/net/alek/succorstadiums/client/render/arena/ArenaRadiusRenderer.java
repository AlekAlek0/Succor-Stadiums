package net.alek.succorstadiums.client.render.arena;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalDouble;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

// Draws the radius of one mob arena in the world as translucent cylinder walls plus a ground ring.
// Toggled from MobArenaScreen; call ArenaRadiusRenderer.register() once from the client initializer.
public final class ArenaRadiusRenderer {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── Tunables ──────────────────────────────────────────────────────────────

    // Mobs spawn within radius / 2 of the center (see ArenaSession#randomPositionInRadius),
    // so the "spawn zone" is half of the configured radius value.
    private static final double SPAWN_RADIUS_DIVISOR = 2.0;

    // Also draw a fainter wall at the full configured radius value
    private static final boolean DRAW_CONFIGURED_RADIUS = true;

    // Wall extends this far below / above the arena center Y
    private static final double WALL_BELOW = 8.0;
    private static final double WALL_ABOVE = 24.0;

    // Half the width of the flat ring drawn on the ground at the spawn zone edge
    private static final double RING_HALF_WIDTH = 0.25;

    private static final int MIN_SEGMENTS = 32;
    private static final int MAX_SEGMENTS = 128;

    // Bytes reserved for the vertex buffer: 128 segments * 3 shapes * 8 vertices is far below this
    private static final int BUFFER_SIZE = 1 << 18;

    // r, g, b, a
    private static final float[] SPAWN_WALL_COLOR       = {0.26f, 0.70f, 0.28f, 0.22f};
    private static final float[] SPAWN_RING_COLOR       = {0.30f, 0.90f, 0.35f, 0.90f};
    private static final float[] CONFIGURED_WALL_COLOR  = {0.70f, 0.70f, 0.75f, 0.10f};

    // Vanilla filled-box pipeline: translucent, depth tested (hidden behind blocks).
    // For see-through-walls, register a copy with an empty depth state like the Fabric docs example.
    private static final RenderPipeline PIPELINE = RenderPipelines.DEBUG_FILLED_BOX;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    // ── Overlay state (client thread only) ────────────────────────────────────

    private static String shownArena = null;
    private static double centerX, centerY, centerZ, configuredRadius;

    // Immutable snapshot taken during extraction and used while drawing
    private record RadiusState(double x, double y, double z, double spawnRadius, double configuredRadius) {}

    private static RadiusState state = null;

    private static StagedVertexBuffer stagedBuffer = null;
    private static boolean warnedTopology = false;

    private ArenaRadiusRenderer() {}

    // ── Public API ────────────────────────────────────────────────────────────

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(ArenaRadiusRenderer::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(ArenaRadiusRenderer::renderAndDraw);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> close());
    }

    // Shows (or updates) the overlay for the given arena. Only one arena is shown at a time.
    public static void show(String arenaName, double x, double y, double z, double radius) {
        shownArena = arenaName;
        centerX = x;
        centerY = y;
        centerZ = z;
        configuredRadius = radius;
    }

    public static void hide() {
        shownArena = null;
    }

    public static boolean isShowing(String arenaName) {
        return shownArena != null && shownArena.equals(arenaName);
    }

    public static String shownArena() {
        return shownArena;
    }

    // ── Extraction (read data here, build an immutable state) ─────────────────

    private static void extract(LevelExtractionContext context) {
        if (shownArena == null || configuredRadius <= 0) {
            state = null;
            return;
        }
        state = new RadiusState(centerX, centerY, centerZ,
                configuredRadius / SPAWN_RADIUS_DIVISOR, configuredRadius);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private static void renderAndDraw(LevelRenderContext context) {
        RadiusState s = state;
        if (s == null) return;

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
        Vec3 camera = context.levelState().cameraRenderState.pos;
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        Matrix4fc matrix = matrices.last().pose();

        VertexConsumer builder = stagedBuffer.getVertexBuilder(draw);
        buildGeometry(builder, matrix, s);

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

        if (DRAW_CONFIGURED_RADIUS) {
            wall(b, m, s.x(), s.z(), s.configuredRadius(), yBottom, yTop,
                    segmentsFor(s.configuredRadius()), CONFIGURED_WALL_COLOR);
        }

        int spawnSegments = segmentsFor(s.spawnRadius());
        wall(b, m, s.x(), s.z(), s.spawnRadius(), yBottom, yTop, spawnSegments, SPAWN_WALL_COLOR);
        flatRing(b, m, s.x(), s.y() + 0.02, s.z(), s.spawnRadius(), RING_HALF_WIDTH, spawnSegments, SPAWN_RING_COLOR);
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

            doubleSidedQuad(b, m, color,
                    x0, yBottom, z0,
                    x1, yBottom, z1,
                    x1, yTop, z1,
                    x0, yTop, z0);
        }
    }

    // Flat horizontal ring strip at height y
    private static void flatRing(VertexConsumer b, Matrix4fc m, double cx, double y, double cz,
                                 double radius, double halfWidth, int segments, float[] color) {
        double inner = Math.max(0.0, radius - halfWidth);
        double outer = radius + halfWidth;
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
        b.addVertex(m, (float) x, (float) y, (float) z).setColor(c[0], c[1], c[2], c[3]);
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private static void close() {
        if (stagedBuffer != null) {
            stagedBuffer.close();
            stagedBuffer = null;
        }
    }
}