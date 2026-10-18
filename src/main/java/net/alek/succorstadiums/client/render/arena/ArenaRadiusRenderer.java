package net.alek.succorstadiums.client.render.arena;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.alek.succorstadiums.arena.SpawnSpot;
import net.alek.succorstadiums.config.SuccorStadiumsConfig;
import net.alek.succorstadiums.config.SuccorStadiumsConfigScreen;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

// Draws the radius of any number of mob arenas in the world: translucent cylinder walls plus a ground ring
// per arena, and floating info text (name, radius, coordinates) above each arena center.
//
// It also marks the invalid spawn tiles: every block column inside the circle where the spawner's standing-spot
// check (SpawnSpot, the same code ArenaSession uses) fails gets a tile on the ground, colored by the reason.
// The label shows how many tiles are invalid and what share of the arena is valid.
// The ground ring can follow the terrain. All overlays switch off when you disconnect.
//
// - Toggled per arena from MobArenaScreen (toggle / isEnabled). The invalid spawn tiles show with the circle.
// - How it looks and what it costs is configurable: SuccorStadiumsConfig, "Mob Arena Rendering" category.
// - Only arenas in the dimension the player is currently in are drawn.
// - Keeps its own copy of the arena data, refreshed from every ArenaDataPayload (onArenaData) and by polling the
//   server while any overlay is on, so it never depends on the screen being open.
//
// Call ArenaRadiusRenderer.register() once from the client initializer.
public final class ArenaRadiusRenderer {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── Fixed constants (everything else comes from the config) ───────────────

    private static final int MIN_SEGMENTS = 32;
    private static final int MAX_SEGMENTS = 128;

    // The invalid tile scan always gets at least this many columns per tick per arena
    private static final int MIN_COLUMNS_PER_ARENA = 50;

    // Invalid tiles are stored as x, y, z, reason (SpawnSpot.Spot ordinal) quads
    private static final int TILE_STRIDE = 4;

    // The ring floats this far above the ground, and its terrain heights are re-sampled this often
    private static final double RING_LIFT = 0.02;
    private static final int RING_RESAMPLE_TICKS = 10;

    // Bytes reserved for the vertex buffer, sized for the largest values the config allows:
    // 32 arenas x 2 shapes x 128 segments x 8 vertices x 16 bytes = 1 MB, plus 20000 tiles x 8 vertices x 16 bytes = 2.5 MB
    private static final int BUFFER_SIZE = 1 << 22;

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

    // One incremental column scan per enabled arena (feeds the invalid spawn tiles)
    private static final Map<String, ArenaScan> scans = new ConcurrentHashMap<>();

    // Terrain height under each ring segment vertex of one arena (already includes RING_LIFT)
    private record RingHeights(ResourceKey<Level> dimension, double centerX, double centerZ, int radius, double[] y) {
        boolean matches(ArenaInfo arena) {
            return dimension.equals(arena.dimension())
                    && centerX == arena.x() && centerZ == arena.z() && radius == arena.radius();
        }
    }

    private static final Map<String, RingHeights> ringHeights = new ConcurrentHashMap<>();
    private static int ringTicks = 0;

    // Result of one finished invalid tile scan: the invalid tiles, how many loaded columns were checked,
    // and how many invalid tiles there are per reason (indexed by SpawnSpot.Spot ordinal)
    private record ScanResult(int[] tiles, int scanned, int[] reasonCounts) {}

    private static final int[] NO_TILES = new int[0];

    // Immutable snapshot of one arena, taken during extraction and used while drawing.
    // invalidTiles holds x, y, z triples (the ground cell of each invalid column).
    private record RadiusState(double x, double y, double z, double radius, int wallRgb, int ringRgb,
                               double[] ringY, int[] invalidTiles) {}

    // Immutable snapshot of the config values the drawing code needs
    private record RenderSettings(
            boolean showWall, boolean showRing,
            double wallBelow, double wallAbove, double ringHalfWidth, float wallAlpha, float ringAlpha,
            double maxDrawDistance, int maxArenasDrawn,
            float[][] tileColors, double tileDrawDistance, int maxTilesDrawn, double tileInset, double tileLift) {}

    private record Frame(List<RadiusState> states, RenderSettings settings) {}

    private static volatile Frame frame = null;

    private static int pollTicks = 0;
    private static StagedVertexBuffer stagedBuffer = null;
    private static boolean warnedTopology = false;
    private static boolean labelsBroken = false;

    private ArenaRadiusRenderer() {}

    // ── Config ────────────────────────────────────────────────────────────────

    // The live config, clamped into its allowed ranges
    private static SuccorStadiumsConfig config() {
        SuccorStadiumsConfig cfg = SuccorStadiumsConfigScreen.getConfig();
        cfg.clampArenaRendering();
        return cfg;
    }

    private static RenderSettings settingsFrom(SuccorStadiumsConfig cfg) {
        float tileAlpha = cfg.arenaTileOpacity / 100f;
        boolean byReason = cfg.arenaColorTilesByReason;
        float[][] tileColors = new float[SpawnSpot.Spot.values().length][];
        tileColors[SpawnSpot.Spot.VALID.ordinal()] = rgba(cfg.arenaTileColor, tileAlpha);
        tileColors[SpawnSpot.Spot.NO_GROUND.ordinal()] = rgba(cfg.arenaTileColor, tileAlpha);
        tileColors[SpawnSpot.Spot.LIQUID.ordinal()] =
                rgba(byReason ? cfg.arenaTileLiquidColor : cfg.arenaTileColor, tileAlpha);
        tileColors[SpawnSpot.Spot.BLOCKED.ordinal()] =
                rgba(byReason ? cfg.arenaTileBlockedColor : cfg.arenaTileColor, tileAlpha);

        return new RenderSettings(
                cfg.arenaShowWall, cfg.arenaShowRing,
                cfg.arenaWallBelow, cfg.arenaWallAbove, cfg.arenaRingWidth / 200.0,
                cfg.arenaWallOpacity / 100f, cfg.arenaRingOpacity / 100f,
                cfg.arenaMaxDrawDistance, cfg.arenaMaxArenasDrawn,
                tileColors, cfg.arenaTileDrawDistance, cfg.arenaMaxTilesDrawn,
                cfg.arenaTileInset / 100.0, cfg.arenaTileLift / 100.0);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(ArenaRadiusRenderer::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(ArenaRadiusRenderer::renderAndDraw);
        ClientTickEvents.END_CLIENT_TICK.register(ArenaRadiusRenderer::pollArenaData);
        ClientTickEvents.END_CLIENT_TICK.register(ArenaRadiusRenderer::scanInvalidTiles);
        ClientTickEvents.END_CLIENT_TICK.register(ArenaRadiusRenderer::updateRingHeights);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> close());
    }

    public static boolean isEnabled(String arenaName) {
        return enabled.contains(arenaName);
    }

    // Switches one arena's overlay (circle, label and invalid spawn tiles) on or off and returns whether it is now on.
    // Other arenas are unaffected.
    public static void toggle(ArenaDataPayload.ArenaEntry arena) {
        if (enabled.remove(arena.name())) {
            scans.remove(arena.name());
            ringHeights.remove(arena.name());
            return;
        }

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
        if (++pollTicks < config().arenaDataRefreshTicks) return;
        pollTicks = 0;
        ClientPlayNetworking.send(ArenaActionPayload.requestData());
    }

    private static void clear() {
        enabled.clear();
        scans.clear();
        ringHeights.clear();
        known = Map.of();
        frame = null;
        pollTicks = 0;
    }

    // ── Invalid spawn tile scan ───────────────────────────────────────────────

    // Walks the block columns whose cell touches the arena circle, a slice per tick, and records the ones where
    // the spawner's standing-spot check fails at the surface. A finished pass is published as x, y, z triples.
    private static final class ArenaScan {
        final ResourceKey<Level> dimension;
        final double centerX;
        final double centerZ;
        final int radius;
        final int[] colX;
        final int[] colZ;

        int cursor = 0;
        int cooldown = 0;
        int[] found = new int[192];   // x, y, z, reason quads
        int scannedColumns = 0;
        int[] reasonCounts = new int[SpawnSpot.Spot.values().length];
        int foundCount = 0;

        // Null until the first full pass has finished
        volatile ScanResult published = null;

        ArenaScan(ArenaInfo arena) {
            this.dimension = arena.dimension();
            this.centerX = arena.x();
            this.centerZ = arena.z();
            this.radius = arena.radius();

            int minX = Mth.floor(centerX - radius), maxX = Mth.floor(centerX + radius);
            int minZ = Mth.floor(centerZ - radius), maxZ = Mth.floor(centerZ + radius);

            int count = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (touchesCircle(x, z)) count++;
                }
            }

            this.colX = new int[count];
            this.colZ = new int[count];
            int i = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (touchesCircle(x, z)) {
                        colX[i] = x;
                        colZ[i] = z;
                        i++;
                    }
                }
            }
        }

        // Spawn positions are continuous, so a block column counts when any part of its cell is inside the circle
        private boolean touchesCircle(int blockX, int blockZ) {
            double dx = Math.max(Math.max(blockX - centerX, centerX - (blockX + 1)), 0.0);
            double dz = Math.max(Math.max(blockZ - centerZ, centerZ - (blockZ + 1)), 0.0);
            return dx * dx + dz * dz <= (double) radius * radius;
        }

        boolean matches(ArenaInfo arena) {
            return dimension.equals(arena.dimension())
                    && centerX == arena.x() && centerZ == arena.z() && radius == arena.radius();
        }

        void step(LevelReader level, int budget, int rescanDelayTicks) {
            if (cooldown > 0) {
                cooldown--;
                return;
            }

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int end = Math.min(cursor + budget, colX.length);
            while (cursor < end) {
                int x = colX[cursor];
                int z = colZ[cursor];
                cursor++;

                // Not loaded on the client: unknown, so neither marked nor counted
                if (!level.hasChunk(x >> 4, z >> 4)) continue;

                scannedColumns++;
                int y = SpawnSpot.surfaceY(level, x, z);
                pos.set(x, y, z);
                SpawnSpot.Spot spot = SpawnSpot.check(level, pos);
                if (spot != SpawnSpot.Spot.VALID) add(x, y, z, spot.ordinal());
            }

            if (cursor >= colX.length) {
                published = new ScanResult(Arrays.copyOf(found, foundCount * TILE_STRIDE), scannedColumns, reasonCounts);
                reasonCounts = new int[SpawnSpot.Spot.values().length];
                foundCount = 0;
                scannedColumns = 0;
                cursor = 0;
                cooldown = rescanDelayTicks;
            }
        }

        private void add(int x, int y, int z, int reason) {
            if ((foundCount + 1) * TILE_STRIDE > found.length) found = Arrays.copyOf(found, found.length * 2);
            found[foundCount * TILE_STRIDE] = x;
            found[foundCount * TILE_STRIDE + 1] = y;
            found[foundCount * TILE_STRIDE + 2] = z;
            found[foundCount * TILE_STRIDE + 3] = reason;
            reasonCounts[reason]++;
            foundCount++;
        }
    }

    private static void scanInvalidTiles(Minecraft client) {
        SuccorStadiumsConfig cfg = config();
        if (!cfg.arenaShowInvalidTiles || enabled.isEmpty() || client.level == null || client.player == null) {
            scans.clear();
            return;
        }

        // Forget scans for arenas whose overlay is off
        scans.keySet().removeIf(name -> !enabled.contains(name));

        ResourceKey<Level> currentDimension = client.level.dimension();
        List<ArenaInfo> active = new ArrayList<>();
        for (String name : enabled) {
            ArenaInfo arena = known.get(name);
            if (arena == null || arena.radius() <= 0 || arena.radius() > cfg.arenaMaxScanRadius) {
                scans.remove(name);
                continue;
            }
            if (!arena.dimension().equals(currentDimension)) continue;
            active.add(arena);
        }
        if (active.isEmpty()) return;

        int perArena = Math.max(MIN_COLUMNS_PER_ARENA, cfg.arenaScanColumnsPerTick / active.size());
        for (ArenaInfo arena : active) {
            ArenaScan scan = scans.get(arena.name());
            if (scan == null || !scan.matches(arena)) {
                scan = new ArenaScan(arena);
                scans.put(arena.name(), scan);
            }
            scan.step(client.level, perArena, cfg.arenaRescanDelayTicks);
        }
    }

    // ── Terrain-following ring ────────────────────────────────────────────────

    // Samples the terrain height under every ring vertex a few times a second
    private static void updateRingHeights(Minecraft client) {
        SuccorStadiumsConfig cfg = config();
        if (!cfg.arenaRingFollowsTerrain || !cfg.arenaShowRing || enabled.isEmpty()
                || client.level == null || client.player == null) {
            ringHeights.clear();
            ringTicks = 0;
            return;
        }
        if (ringTicks > 0) {
            ringTicks--;
            return;
        }
        ringTicks = RING_RESAMPLE_TICKS;

        ringHeights.keySet().removeIf(name -> !enabled.contains(name));

        LevelReader level = client.level;
        ResourceKey<Level> currentDimension = client.level.dimension();
        for (String name : enabled) {
            ArenaInfo arena = known.get(name);
            if (arena == null || arena.radius() <= 0 || !arena.dimension().equals(currentDimension)) {
                ringHeights.remove(name);
                continue;
            }

            int segments = segmentsFor(arena.radius());
            double[] y = new double[segments];
            for (int i = 0; i < segments; i++) {
                double angle = (Math.PI * 2.0) * i / segments;
                int x = Mth.floor(arena.x() + arena.radius() * Math.cos(angle));
                int z = Mth.floor(arena.z() + arena.radius() * Math.sin(angle));
                // Not loaded on the client: fall back to the arena's own height
                double ground = level.hasChunk(x >> 4, z >> 4) ? SpawnSpot.surfaceY(level, x, z) : arena.y();
                y[i] = ground + RING_LIFT;
            }
            ringHeights.put(name, new RingHeights(arena.dimension(), arena.x(), arena.z(), arena.radius(), y));
        }
    }

    // ── Extraction (read data here, build an immutable state) ─────────────────

    private static void extract(LevelExtractionContext context) {
        Minecraft client = Minecraft.getInstance();
        if (enabled.isEmpty() || client.level == null || client.player == null) {
            frame = null;
            return;
        }

        SuccorStadiumsConfig cfg = config();
        ResourceKey<Level> currentDimension = client.level.dimension();
        Vec3 playerPos = client.player.position();
        double labelRangeSqr = (double) cfg.arenaLabelRange * cfg.arenaLabelRange;
        int wallRgb = cfg.arenaWallColor & 0xFFFFFF;
        int ringRgb = cfg.arenaRingColor & 0xFFFFFF;

        List<RadiusState> next = new ArrayList<>();
        for (String name : enabled) {
            ArenaInfo arena = known.get(name);
            if (arena == null || arena.radius() <= 0) continue;
            if (!arena.dimension().equals(currentDimension)) continue;

            ArenaScan scan = cfg.arenaShowInvalidTiles ? scans.get(name) : null;
            ScanResult result = scan == null ? null : scan.published; // null until the first pass has finished

            RingHeights ring = cfg.arenaRingFollowsTerrain ? ringHeights.get(name) : null;
            double[] ringY = ring != null && ring.matches(arena) ? ring.y() : null;

            next.add(new RadiusState(arena.x(), arena.y(), arena.z(), arena.radius(), wallRgb, ringRgb,
                    ringY, result == null ? NO_TILES : result.tiles()));

            if (cfg.arenaShowLabel && playerPos.distanceToSqr(arena.x(), arena.y(), arena.z()) <= labelRangeSqr) {
                submitLabel(arena, result, cfg);
            }
        }
        frame = new Frame(List.copyOf(next), settingsFrom(cfg));
    }

    // ── Floating text (Mojang's Gizmos API) ───────────────────────────────────

    // One piece of text with its own color, and a label line made of one or more pieces side by side
    private record Seg(String text, int argb) {}

    private record Line(List<Seg> segs) {}

    private static Line line(String text, int argb) {
        return new Line(List.of(new Seg(text, argb)));
    }

    // Size of one font pixel in the world at text scale 1.0. Measured from the in-game gizmo text: 0.025 made the
    // pieces overlap, 0.05 still overlapped slightly. If pieces still overlap raise it, if gaps are too wide lower it.
    private static final double TEXT_WORLD_PER_PIXEL = 0.056;
    private static final String SEGMENT_GAP = "    ";

    private static void submitLabel(ArenaInfo arena, ScanResult scan, SuccorStadiumsConfig cfg) {
        if (labelsBroken) return;

        try {
            double spacing = cfg.arenaLabelLineSpacing / 100.0;
            float nameScale = cfg.arenaNameScale / 100f;
            float infoScale = cfg.arenaInfoScale / 100f;

            // Lines below the name: radius, coordinates and, with invalid tiles on, the invalid count and valid share
            List<Line> lines = new ArrayList<>();
            lines.add(line("Radius: " + arena.radius(), argb(cfg.arenaRadiusLineColor)));
            lines.add(line("X: " + Mth.floor(arena.x()) + "  Y: " + Mth.floor(arena.y())
                    + "  Z: " + Mth.floor(arena.z()), argb(cfg.arenaCoordsLineColor)));

            if (cfg.arenaShowInvalidTiles) {
                int invalidColor = argb(cfg.arenaInvalidLineColor);
                int blockedColor = argb(cfg.arenaBlockedLineColor);
                int liquidColor = argb(cfg.arenaLiquidLineColor);
                int validColor = argb(cfg.arenaValidLineColor);

                if (arena.radius() > cfg.arenaMaxScanRadius) {
                    lines.add(line("Invalid Tiles: radius too large to scan", invalidColor));
                } else if (scan == null) {
                    lines.add(line("Invalid Tiles: scanning...", invalidColor));
                } else {
                    // The counts share one line, each in its own color. Reasons with no tiles are left out.
                    List<Seg> counts = new ArrayList<>();
                    int noGround = scan.reasonCounts()[SpawnSpot.Spot.NO_GROUND.ordinal()];
                    int blocked = scan.reasonCounts()[SpawnSpot.Spot.BLOCKED.ordinal()];
                    int liquid = scan.reasonCounts()[SpawnSpot.Spot.LIQUID.ordinal()];
                    if (noGround > 0) counts.add(new Seg("Invalid Tiles: " + noGround, invalidColor));
                    if (blocked > 0) counts.add(new Seg("Blocked Tiles: " + blocked, blockedColor));
                    if (liquid > 0) counts.add(new Seg("Liquid Tiles: " + liquid, liquidColor));

                    if (counts.isEmpty()) lines.add(line("No invalid tiles", validColor));
                    else lines.add(new Line(counts));
                }

                if (cfg.arenaShowValidPercent && arena.radius() <= cfg.arenaMaxScanRadius) {
                    String percent;
                    if (scan == null) percent = "scanning...";
                    else if (scan.scanned() == 0) percent = "no loaded chunks";
                    else {
                        int invalid = scan.tiles().length / TILE_STRIDE;
                        percent = String.format(Locale.ROOT, "%.1f%%", (scan.scanned() - invalid) * 100.0 / scan.scanned());
                    }
                    lines.add(line("Valid spawn area: " + percent, validColor));
                }
            }

            // The name is the top line, the other lines hang below it
            double topY = arena.y() + cfg.arenaLabelHeight + lines.size() * spacing;
            text(arena.name(), arena.x(), topY, arena.z(), argb(cfg.arenaNameColor), nameScale);
            for (int i = 0; i < lines.size(); i++) {
                textLine(lines.get(i), arena.x(), topY - (i + 1) * spacing, arena.z(), infoScale);
            }
        } catch (RuntimeException e) {
            // Most likely no gizmo collector is active at this point of the frame. Keep the walls working.
            labelsBroken = true;
            LOGGER.error("Arena radius labels disabled: could not submit gizmo text", e);
        }
    }

    // Draws a line centered on (x, z). A line of several pieces is laid out left to right across the screen
    // (perpendicular to where the player looks), with the whole line centered.
    private static void textLine(Line line, double x, double y, double z, float scale) {
        List<Seg> segs = line.segs();
        if (segs.size() == 1) {
            text(segs.get(0).text(), x, y, z, segs.get(0).argb(), scale);
            return;
        }

        Minecraft client = Minecraft.getInstance();
        double unit = TEXT_WORLD_PER_PIXEL * scale;
        double gap = client.font.width(SEGMENT_GAP) * unit;

        double[] widths = new double[segs.size()];
        double total = gap * (segs.size() - 1);
        for (int i = 0; i < widths.length; i++) {
            widths[i] = client.font.width(segs.get(i).text()) * unit;
            total += widths[i];
        }

        // Horizontal direction that points to the right of the screen
        double yaw = Math.toRadians(client.player == null ? 0.0 : client.player.getYRot());
        double rightX = -Math.cos(yaw);
        double rightZ = -Math.sin(yaw);

        double cursor = -total / 2.0;
        for (int i = 0; i < widths.length; i++) {
            double center = cursor + widths[i] / 2.0;
            text(segs.get(i).text(), x + rightX * center, y, z + rightZ * center, segs.get(i).argb(), scale);
            cursor += widths[i] + gap;
        }
    }

    private static int argb(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private static void text(String text, double x, double y, double z, int argb, float scale) {
        // Empty adjustLeft = centered on the position. Always on top so it stays readable through terrain.
        Gizmos.billboardText(text, new Vec3(x, y, z), new TextGizmo.Style(argb, scale, OptionalDouble.empty()))
                .setAlwaysOnTop();
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private static void renderAndDraw(LevelRenderContext context) {
        Frame current = frame;
        if (current == null || current.states().isEmpty()) return;

        final RenderSettings set = current.settings();
        final Vec3 camera = context.levelState().cameraRenderState.pos;

        // Skip arenas that are too far away, and keep only the nearest ones if there are many
        List<RadiusState> drawList = new ArrayList<>(current.states().size());
        for (RadiusState a : current.states()) {
            double distance = Math.sqrt(camera.distanceToSqr(a.x(), a.y(), a.z())) - a.radius();
            if (distance <= set.maxDrawDistance()) drawList.add(a);
        }
        if (drawList.isEmpty()) return;
        if (drawList.size() > set.maxArenasDrawn()) {
            drawList.sort(Comparator.comparingDouble(a -> camera.distanceToSqr(a.x(), a.y(), a.z())));
            drawList = new ArrayList<>(drawList.subList(0, set.maxArenasDrawn()));
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
        int tilesLeft = set.maxTilesDrawn();
        for (RadiusState a : drawList) {
            buildGeometry(builder, matrix, a, set);
            tilesLeft -= buildInvalidTiles(builder, matrix, a.invalidTiles(), camera, tilesLeft, set);
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

    private static void buildGeometry(VertexConsumer b, Matrix4fc m, RadiusState a, RenderSettings set) {
        double yBottom = a.y() - set.wallBelow();
        double yTop = a.y() + set.wallAbove();

        float[] wallColor = rgba(a.wallRgb(), set.wallAlpha());
        float[] ringColor = rgba(a.ringRgb(), set.ringAlpha());

        int segments = segmentsFor(a.radius());
        if (set.showWall()) {
            wall(b, m, a.x(), a.z(), a.radius(), yBottom, yTop, segments, wallColor);
        }
        if (set.showRing()) {
            flatRing(b, m, a.x(), a.y() + RING_LIFT, a.z(), a.radius(), set.ringHalfWidth(), segments, ringColor,
                    a.ringY());
        }
    }

    private static float[] rgba(int rgb, float alpha) {
        return new float[]{((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha};
    }

    // Tile on the ground of every invalid column near the camera. Returns how many tiles were drawn.
    private static int buildInvalidTiles(VertexConsumer b, Matrix4fc m, int[] tiles, Vec3 camera, int limit,
                                         RenderSettings set) {
        double rangeSqr = set.tileDrawDistance() * set.tileDrawDistance();
        int drawn = 0;

        for (int i = 0; i + TILE_STRIDE - 1 < tiles.length && drawn < limit; i += TILE_STRIDE) {
            int bx = tiles[i];
            int by = tiles[i + 1];
            int bz = tiles[i + 2];
            float[][] colors = set.tileColors();
            float[] color = colors[Math.clamp(tiles[i + 3], 0, colors.length - 1)];

            double dx = bx + 0.5 - camera.x;
            double dy = by - camera.y;
            double dz = bz + 0.5 - camera.z;
            if (dx * dx + dy * dy + dz * dz > rangeSqr) continue;

            double x0 = bx + set.tileInset(), x1 = bx + 1 - set.tileInset();
            double z0 = bz + set.tileInset(), z1 = bz + 1 - set.tileInset();
            double y = by + set.tileLift();
            doubleSidedQuad(b, m, color,
                    x0, y, z0,
                    x1, y, z0,
                    x1, y, z1,
                    x0, y, z1);
            drawn++;
        }
        return drawn;
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

    // Ring strip. Flat at height y, or following the terrain when ys (one height per segment vertex) is given.
    private static void flatRing(VertexConsumer b, Matrix4fc m, double cx, double y, double cz,
                                 double radius, double halfWidth, int segments, float[] color, double[] ys) {
        if (ys != null && ys.length != segments) ys = null;
        double inner = Math.max(0.0, radius - halfWidth);
        double outer = radius + halfWidth;
        for (int i = 0; i < segments; i++) {
            double a0 = (Math.PI * 2.0) * i / segments;
            double a1 = (Math.PI * 2.0) * (i + 1) / segments;
            double c0 = Math.cos(a0), s0 = Math.sin(a0);
            double c1 = Math.cos(a1), s1 = Math.sin(a1);
            double y0 = ys == null ? y : ys[i];
            double y1 = ys == null ? y : ys[(i + 1) % segments];

            doubleSidedQuad(b, m, color,
                    cx + inner * c0, y0, cz + inner * s0,
                    cx + outer * c0, y0, cz + outer * s0,
                    cx + outer * c1, y1, cz + outer * s1,
                    cx + inner * c1, y1, cz + inner * s1);
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