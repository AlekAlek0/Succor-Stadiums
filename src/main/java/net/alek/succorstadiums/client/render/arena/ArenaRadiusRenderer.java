package net.alek.succorstadiums.client.render.arena;

// Minecraft and Mojang classes: rendering, world, block positions and text
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.core.registries.Registries;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.LevelReader;
import com.mojang.blaze3d.PrimitiveTopology;
import net.minecraft.resources.ResourceKey;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;

// Fabric API: render, tick, connection and networking events
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Java utilities: collections, math and text formatting
import java.util.concurrent.ConcurrentHashMap;
import java.util.OptionalDouble;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Arrays;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Logging, plus JOML math types (matrices and vectors)
import org.slf4j.LoggerFactory;
import org.joml.Matrix4fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

// Succor Stadiums classes: config, arena packets, mod id and the spawn rule
import net.alek.succorstadiums.config.SuccorStadiumsConfigScreen;
import net.alek.succorstadiums.network.arena.ArenaActionPayload;
import net.alek.succorstadiums.network.arena.ArenaDataPayload;
import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;
import net.alek.succorstadiums.config.SuccorStadiumsConfig;
import net.alek.succorstadiums.arena.SpawnSpot;

// Draws the radius of any number of mob arenas in the world:
// a fading wall and a ground ring per arena, a floating label above each arena center,
// and a tile on every block where mobs can't spawn.
//
// How it works each frame:
//   1. Extraction (extract): reads the config, the arena data and the scan results into an immutable Frame
//      and submits the label text.
//   2. Drawing (renderAndDraw): turns the Frame into vertices and draws them in one batch.
// Between frames, every tick: pollArenaData keeps the arena data fresh, scanInvalidTiles finds the invalid
// blocks and updateRingHeights samples the terrain under the ring.
//
// Call ArenaRadiusRenderer.register() once from the client initializer.
public final class ArenaRadiusRenderer {

    // Initialize a logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Fixed constants for min and maximum segments to render

    private static final int MIN_SEGMENTS = 32;
    private static final int MAX_SEGMENTS = 128;

    // The minimum amount of columns per tick per arena
    private static final int MIN_COLUMNS_PER_ARENA = 50;

    // Invalid tiles are stored as x, y, z, reason (SpawnSpot.Spot ordinal) quads
    private static final int TILE_STRIDE = 4;

    // Initialize a ring lift to sit 0.02 blocks above the ground
    // and initialize ring resample ticks for the ring terrain heights to be reread every 10 ticks
    private static final double RING_LIFT = 0.02;
    private static final int RING_RESAMPLE_TICKS = 10;

    // Bytes reserved for the vertex buffer (1 << 22 = 4 MB), sized for the largest values the config allows:
    // 32 arenas x 2 shapes x 128 segments x 8 vertices x 16 bytes = 1 MB, plus 20000 tiles x 8 vertices x 16 bytes = 2.5 MB
    private static final int BUFFER_SIZE = 1 << 22;

    // Vanilla filled-box pipeline: translucent, depth tested (hidden behind blocks)
    private static final RenderPipeline PIPELINE = RenderPipelines.DEBUG_FILLED_BOX;

    // Initialize fixed values for a color multiplier of white, no model offset, and an unused texture matrix
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    // ── State ─────────────────────────────────────────────────────────────────

    // A client copy of the arena data
    private record ArenaInfo(String name, ResourceKey<Level> dimension, double x, double y, double z, int radius) {}

    // Every arena the client knows about, by name. Replaced as a whole on each update (never edited in place),
    // and volatile, so readers never see a half-built map
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

    // Latest sampled ring heights per arena, and the countdown until the next sampling
    private static final Map<String, RingHeights> ringHeights = new ConcurrentHashMap<>();
    private static int ringTicks = 0;

    // Result of one finished invalid tile scan: the invalid tiles, how many loaded columns were checked,
    // and how many invalid tiles there are per reason (indexed by SpawnSpot.Spot ordinal)
    private record ScanResult(int[] tiles, int scanned, int[] reasonCounts) {}

    // Shared empty array for arenas with no invalid tiles (avoids allocating a new one every frame)
    private static final int[] NO_TILES = new int[0];

    // Immutable snapshot of one arena, taken during extraction and used while drawing.
    // invalidTiles holds x, y, z, reason quads (the ground cell and the reason for each invalid column).
    private record RadiusState(double x, double y, double z, double radius, int wallRgb, int ringRgb,
                               double[] ringY, int[] invalidTiles) {}

    // Immutable snapshot of the config values the drawing code needs
    private record RenderSettings(
            boolean showWall, boolean showRing,
            double wallBelow, double wallAbove, double ringHalfWidth, float wallAlpha, float ringAlpha,
            double maxDrawDistance, int maxArenasDrawn,
            float[][] tileColors, double tileDrawDistance, int maxTilesDrawn, double tileInset, double tileLift) {}

    // Everything the drawing step needs for one frame: all arena snapshots plus the settings
    private record Frame(List<RadiusState> states, RenderSettings settings) {}

    // The newest frame. Written during extraction and read during drawing, volatile so the swap is seen right away
    private static volatile Frame frame = null;

    // Ticks counted since the last arena data request
    private static int pollTicks = 0;
    // The GPU buffer the vertices are written into (created the first time something is drawn)
    private static StagedVertexBuffer stagedBuffer = null;
    // Set once the pipeline error has been logged, so it isn't logged every frame
    private static boolean warnedTopology = false;
    // Set if floating text throws an error, which turns the labels off (the wall and ring keep working)
    private static boolean labelsBroken = false;

    // Private constructor: this is a static utility class and should never be instantiated
    private ArenaRadiusRenderer() {}

    // ── Config ────────────────────────────────────────────────────────────────

    // The live config, clamped into its allowed ranges
    private static SuccorStadiumsConfig config() {
        SuccorStadiumsConfig cfg = SuccorStadiumsConfigScreen.getConfig();
        cfg.clampArenaRendering();
        return cfg;
    }

    // Converts the config into the frozen RenderSettings for one frame
    private static RenderSettings settingsFrom(SuccorStadiumsConfig cfg) {
        // Tile opacity as 0 to 1
        float tileAlpha = cfg.arenaTileOpacity / 100f;
        // Whether liquid and blocked tiles get their own colors
        boolean byReason = cfg.arenaColorTilesByReason;
        // One color per spawn reason, indexed by the Spot enum's position (ordinal)
        float[][] tileColors = new float[SpawnSpot.Spot.values().length][];
        // VALID is never drawn, but gets a color so the array has no empty slot
        tileColors[SpawnSpot.Spot.VALID.ordinal()] = rgba(cfg.arenaTileColor, tileAlpha);
        // Tiles with no solid ground always use the ground tile color
        tileColors[SpawnSpot.Spot.NO_GROUND.ordinal()] = rgba(cfg.arenaTileColor, tileAlpha);
        // Liquid and blocked tiles use their own color only when coloring by reason, otherwise the ground color
        tileColors[SpawnSpot.Spot.LIQUID.ordinal()] =
                rgba(byReason ? cfg.arenaTileLiquidColor : cfg.arenaTileColor, tileAlpha);
        tileColors[SpawnSpot.Spot.BLOCKED.ordinal()] =
                rgba(byReason ? cfg.arenaTileBlockedColor : cfg.arenaTileColor, tileAlpha);

        // Opacity is divided by 100 (0 to 1). Ring width is divided by 200: hundredths of a block become blocks, halved
        // because the ring extends out on both sides of the circle. Inset and lift are divided by 100
        return new RenderSettings(
                cfg.arenaShowWall, cfg.arenaShowRing,
                cfg.arenaWallBelow, cfg.arenaWallAbove, cfg.arenaRingWidth / 200.0,
                cfg.arenaWallOpacity / 100f, cfg.arenaRingOpacity / 100f,
                cfg.arenaMaxDrawDistance, cfg.arenaMaxArenasDrawn,
                tileColors, cfg.arenaTileDrawDistance, cfg.arenaMaxTilesDrawn,
                cfg.arenaTileInset / 100.0, cfg.arenaTileLift / 100.0);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    // Hooks the renderer into the game. Call this once from the client initializer
    public static void register() {
        // Every frame, after the world is read: build the immutable frame and submit the labels
        LevelExtractionEvents.END_EXTRACTION.register(ArenaRadiusRenderer::extract);
        // Every frame, after translucent terrain: draw the frame
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(ArenaRadiusRenderer::renderAndDraw);
        // Every tick: ask the server for fresh arena data while an overlay is on
        ClientTickEvents.END_CLIENT_TICK.register(ArenaRadiusRenderer::pollArenaData);
        // Every tick: run a slice of the invalid tile scan
        ClientTickEvents.END_CLIENT_TICK.register(ArenaRadiusRenderer::scanInvalidTiles);
        // Every tick: resample the terrain height under the ring
        ClientTickEvents.END_CLIENT_TICK.register(ArenaRadiusRenderer::updateRingHeights);
        // Switch every overlay off when you leave the world
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
        // Free the GPU buffer when the game closes
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> close());
    }

    // Whether an arena's overlay is switched on (used for the Show / Hide Radius button text)
    public static boolean isEnabled(String arenaName) {
        return enabled.contains(arenaName);
    }

    // Switches one arena's overlay (circle, label and invalid spawn tiles) on or off.
    // Other arenas are unaffected.
    public static void toggle(ArenaDataPayload.ArenaEntry arena) {
        // Already on: switch it off and drop its scan and ring data
        if (enabled.remove(arena.name())) {
            scans.remove(arena.name());
            ringHeights.remove(arena.name());
            return;
        }

        // Make sure the arena is known right away, even before the next data update arrives
        Map<String, ArenaInfo> next = new LinkedHashMap<>(known);
        next.put(arena.name(), toInfo(arena));
        known = Collections.unmodifiableMap(next);

        // Switch the overlay on
        enabled.add(arena.name());
    }

    // Feed this every ArenaDataPayload that arrives, whether or not a screen is open.
    // Refreshes the cached arena data and drops overlays of arenas that no longer exist (deleted or renamed).
    public static void onArenaData(ArenaDataPayload payload) {
        // Build a fresh map from the arenas in the payload
        Map<String, ArenaInfo> next = new LinkedHashMap<>();
        for (ArenaDataPayload.ArenaEntry entry : payload.arenas()) {
            next.put(entry.name(), toInfo(entry));
        }
        // Swap in the new data in one step
        known = Collections.unmodifiableMap(next);
        // Switch off overlays for arenas that no longer exist
        enabled.retainAll(next.keySet());
    }

    // ── Data helpers ──────────────────────────────────────────────────────────

    // Converts a network entry into the client's ArenaInfo, parsing the dimension text
    private static ArenaInfo toInfo(ArenaDataPayload.ArenaEntry e) {
        return new ArenaInfo(e.name(), parseDimension(e.dimension()), e.x(), e.y(), e.z(), e.radius());
    }

    // Turns dimension text like minecraft:the_nether into a ResourceKey. Falls back to the Overworld if it can't be parsed
    private static ResourceKey<Level> parseDimension(String id) {
        try {
            // Blank or missing text means the default dimension (the Overworld)
            String value = (id == null || id.isBlank()) ? ArenaDataPayload.ArenaEntry.DEFAULT_DIMENSION : id;
            return ResourceKey.create(Registries.DIMENSION, Identifier.parse(value));
        } catch (Exception e) {
            LOGGER.warn("Invalid arena dimension '{}', assuming the Overworld", id);
            return Level.OVERWORLD;
        }
    }

    // ── Live data polling ─────────────────────────────────────────────────────

    // Every tick: while any overlay is on, asks the server for fresh arena data every arenaDataRefreshTicks ticks
    private static void pollArenaData(Minecraft client) {
        // Nothing is on (or no player): reset the counter and do nothing
        if (enabled.isEmpty() || client.player == null) {
            pollTicks = 0;
            return;
        }
        // Wait until enough ticks have passed
        if (++pollTicks < config().arenaDataRefreshTicks) return;
        pollTicks = 0;
        // Ask the server to send the arena data again
        ClientPlayNetworking.send(ArenaActionPayload.requestData());
    }

    // Wipes all renderer state (called when you disconnect)
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
    // the spawner's standing-spot check fails at the surface. A finished pass is published as a ScanResult.
    private static final class ArenaScan {
        // What this scan was built for, so it can be rebuilt if the arena changes
        final ResourceKey<Level> dimension;
        final double centerX;
        final double centerZ;
        final int radius;
        // Every block column the scan will visit (x and z share the same index)
        final int[] colX;
        final int[] colZ;

        // The next column to check
        int cursor = 0;
        // Ticks left to wait before the next pass starts
        int cooldown = 0;
        int[] found = new int[192];   // x, y, z, reason quads
        // Counters for the pass in progress
        int scannedColumns = 0;
        int[] reasonCounts = new int[SpawnSpot.Spot.values().length];
        int foundCount = 0;

        // Null until the first full pass has finished
        volatile ScanResult published = null;

        // Builds the list of block columns that touch the circle
        ArenaScan(ArenaInfo arena) {
            this.dimension = arena.dimension();
            this.centerX = arena.x();
            this.centerZ = arena.z();
            this.radius = arena.radius();

            // The square that contains the circle
            int minX = Mth.floor(centerX - radius), maxX = Mth.floor(centerX + radius);
            int minZ = Mth.floor(centerZ - radius), maxZ = Mth.floor(centerZ + radius);

            // First pass: count the columns, so the arrays are exactly the right size
            int count = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (touchesCircle(x, z)) count++;
                }
            }

            // Second pass: fill in the column coordinates
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
            // Distance from the circle center to the nearest edge of this block, per axis (0 if the center is within
            // the block's span)
            double dx = Math.max(Math.max(blockX - centerX, centerX - (blockX + 1)), 0.0);
            double dz = Math.max(Math.max(blockZ - centerZ, centerZ - (blockZ + 1)), 0.0);
            // Inside the circle if that nearest point is within the radius
            return dx * dx + dz * dz <= (double) radius * radius;
        }

        // Whether this scan was built for the same arena (same dimension, center and radius)
        boolean matches(ArenaInfo arena) {
            return dimension.equals(arena.dimension())
                    && centerX == arena.x() && centerZ == arena.z() && radius == arena.radius();
        }

        // Checks up to 'budget' columns, then publishes the result once the whole list is done
        void step(LevelReader level, int budget, int rescanDelayTicks) {
            // Waiting between passes
            if (cooldown > 0) {
                cooldown--;
                return;
            }

            // One reusable position object, instead of creating one per column
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            // Stop at the budget or the end of the list, whichever comes first
            int end = Math.min(cursor + budget, colX.length);
            while (cursor < end) {
                // The next column
                int x = colX[cursor];
                int z = colZ[cursor];
                cursor++;

                // Not loaded on the client: unknown, so neither marked nor counted
                if (!level.hasChunk(x >> 4, z >> 4)) continue;

                scannedColumns++;
                // Height of the first cell a mob would stand in for this column
                int y = SpawnSpot.surfaceY(level, x, z);
                pos.set(x, y, z);
                // Ask the spawn rule if a mob can stand there, and why not if it can't
                SpawnSpot.Spot spot = SpawnSpot.check(level, pos);
                // Record invalid columns along with their reason
                if (spot != SpawnSpot.Spot.VALID) add(x, y, z, spot.ordinal());
            }

            // Finished the whole list: publish the result and start again after the delay
            if (cursor >= colX.length) {
                // Copy the found tiles (trimmed to size) so the published result never changes
                published = new ScanResult(Arrays.copyOf(found, foundCount * TILE_STRIDE), scannedColumns, reasonCounts);
                // New counts array, the published result keeps the old one
                reasonCounts = new int[SpawnSpot.Spot.values().length];
                foundCount = 0;
                scannedColumns = 0;
                cursor = 0;
                cooldown = rescanDelayTicks;
            }
        }

        // Stores one invalid tile as x, y, z, reason and counts it
        private void add(int x, int y, int z, int reason) {
            // Double the array when it is full
            if ((foundCount + 1) * TILE_STRIDE > found.length) found = Arrays.copyOf(found, found.length * 2);
            found[foundCount * TILE_STRIDE] = x;
            found[foundCount * TILE_STRIDE + 1] = y;
            found[foundCount * TILE_STRIDE + 2] = z;
            found[foundCount * TILE_STRIDE + 3] = reason;
            reasonCounts[reason]++;
            foundCount++;
        }
    }

    // Every tick: runs a slice of the invalid tile scan for each shown arena
    private static void scanInvalidTiles(Minecraft client) {
        SuccorStadiumsConfig cfg = config();
        // Feature off or no world: drop all scans
        if (!cfg.arenaShowInvalidTiles || enabled.isEmpty() || client.level == null || client.player == null) {
            scans.clear();
            return;
        }

        // Forget scans for arenas whose overlay is off
        scans.keySet().removeIf(name -> !enabled.contains(name));

        // Pick the arenas that can be scanned right now
        ResourceKey<Level> currentDimension = client.level.dimension();
        List<ArenaInfo> active = new ArrayList<>();
        for (String name : enabled) {
            ArenaInfo arena = known.get(name);
            // Missing, empty or too big to scan: no scan for it
            if (arena == null || arena.radius() <= 0 || arena.radius() > cfg.arenaMaxScanRadius) {
                scans.remove(name);
                continue;
            }
            // In another dimension: keep it but don't scan it
            if (!arena.dimension().equals(currentDimension)) continue;
            active.add(arena);
        }
        if (active.isEmpty()) return;

        // Share the per-tick column budget between the arenas (with a minimum each)
        int perArena = Math.max(MIN_COLUMNS_PER_ARENA, cfg.arenaScanColumnsPerTick / active.size());
        for (ArenaInfo arena : active) {
            // Create a new scan if there isn't one or the arena changed
            ArenaScan scan = scans.get(arena.name());
            if (scan == null || !scan.matches(arena)) {
                scan = new ArenaScan(arena);
                scans.put(arena.name(), scan);
            }
            // Check the next slice of columns
            scan.step(client.level, perArena, cfg.arenaRescanDelayTicks);
        }
    }

    // ── Terrain-following ring ────────────────────────────────────────────────

    // Samples the terrain height under every ring vertex a few times a second
    private static void updateRingHeights(Minecraft client) {
        SuccorStadiumsConfig cfg = config();
        // Option off or nothing to show: clear the ring data
        if (!cfg.arenaRingFollowsTerrain || !cfg.arenaShowRing || enabled.isEmpty()
                || client.level == null || client.player == null) {
            ringHeights.clear();
            ringTicks = 0;
            return;
        }
        // Only resample every RING_RESAMPLE_TICKS ticks
        if (ringTicks > 0) {
            ringTicks--;
            return;
        }
        ringTicks = RING_RESAMPLE_TICKS;

        // Forget arenas that are no longer shown
        ringHeights.keySet().removeIf(name -> !enabled.contains(name));

        LevelReader level = client.level;
        ResourceKey<Level> currentDimension = client.level.dimension();
        for (String name : enabled) {
            ArenaInfo arena = known.get(name);
            // Skip arenas that are missing, empty or in another dimension
            if (arena == null || arena.radius() <= 0 || !arena.dimension().equals(currentDimension)) {
                ringHeights.remove(name);
                continue;
            }

            // Same segment count the drawing uses, so there is one height per ring vertex
            int segments = segmentsFor(arena.radius());
            double[] y = new double[segments];
            for (int i = 0; i < segments; i++) {
                // Angle of this vertex around the circle
                double angle = (Math.PI * 2.0) * i / segments;
                // The block column under this vertex
                int x = Mth.floor(arena.x() + arena.radius() * Math.cos(angle));
                int z = Mth.floor(arena.z() + arena.radius() * Math.sin(angle));
                // Not loaded on the client: fall back to the arena's own height
                double ground = level.hasChunk(x >> 4, z >> 4) ? SpawnSpot.surfaceY(level, x, z) : arena.y();
                // Lift the ring slightly above the ground
                y[i] = ground + RING_LIFT;
            }
            // Store the heights together with the arena they belong to
            ringHeights.put(name, new RingHeights(arena.dimension(), arena.x(), arena.z(), arena.radius(), y));
        }
    }

    // ── Extraction (read data here, build an immutable state) ─────────────────

    // Every frame: reads the current state into an immutable Frame and submits the floating labels
    private static void extract(LevelExtractionContext context) {
        Minecraft client = Minecraft.getInstance();
        // Nothing to draw
        if (enabled.isEmpty() || client.level == null || client.player == null) {
            frame = null;
            return;
        }

        SuccorStadiumsConfig cfg = config();
        ResourceKey<Level> currentDimension = client.level.dimension();
        // Squared distances are compared, so no square root is needed
        Vec3 playerPos = client.player.position();
        double labelRangeSqr = (double) cfg.arenaLabelRange * cfg.arenaLabelRange;
        // Wall and ring colors without any alpha bits
        int wallRgb = cfg.arenaWallColor & 0xFFFFFF;
        int ringRgb = cfg.arenaRingColor & 0xFFFFFF;

        // The arena snapshots for this frame
        List<RadiusState> next = new ArrayList<>();
        for (String name : enabled) {
            ArenaInfo arena = known.get(name);
            // Skip arenas that are missing, empty or in another dimension
            if (arena == null || arena.radius() <= 0) continue;
            if (!arena.dimension().equals(currentDimension)) continue;

            // This arena's scan (only when invalid tiles are on)
            ArenaScan scan = cfg.arenaShowInvalidTiles ? scans.get(name) : null;
            ScanResult result = scan == null ? null : scan.published; // null until the first pass has finished

            // Ring heights, used only if they still match this arena (otherwise the ring is drawn flat)
            RingHeights ring = cfg.arenaRingFollowsTerrain ? ringHeights.get(name) : null;
            double[] ringY = ring != null && ring.matches(arena) ? ring.y() : null;

            // Snapshot of this arena for drawing
            next.add(new RadiusState(arena.x(), arena.y(), arena.z(), arena.radius(), wallRgb, ringRgb,
                    ringY, result == null ? NO_TILES : result.tiles()));

            // Submit the floating label if it's on and the player is close enough
            if (cfg.arenaShowLabel && playerPos.distanceToSqr(arena.x(), arena.y(), arena.z()) <= labelRangeSqr) {
                submitLabel(arena, result, cfg);
            }
        }
        // Publish the frame in one step
        frame = new Frame(List.copyOf(next), settingsFrom(cfg));
    }

    // ── Floating text (Mojang's Gizmos API) ───────────────────────────────────

    // One piece of text with its own color, and a label line made of one or more pieces side by side
    private record Seg(String text, int argb) {}

    private record Line(List<Seg> segs) {}

    // Shortcut for a line made of a single piece of text
    private static Line line(String text, int argb) {
        return new Line(List.of(new Seg(text, argb)));
    }

    // Size of one font pixel in the world at text scale 1.0. Measured from the in-game gizmo text: 0.025 made the
    // pieces overlap, 0.05 still overlapped slightly. If pieces still overlap raise it, if gaps are too wide lower it.
    private static final double TEXT_WORLD_PER_PIXEL = 0.056;
    private static final String SEGMENT_GAP = "    ";

    // Builds the floating label (name on top, info lines below) and submits it as text
    private static void submitLabel(ArenaInfo arena, ScanResult scan, SuccorStadiumsConfig cfg) {
        // Labels were turned off after an error
        if (labelsBroken) return;

        try {
            // Line spacing and text sizes from the config
            double spacing = cfg.arenaLabelLineSpacing / 100.0;
            float nameScale = cfg.arenaNameScale / 100f;
            float infoScale = cfg.arenaInfoScale / 100f;

            // Lines below the name: radius, coordinates and, with invalid tiles on, the invalid count and valid share
            List<Line> lines = new ArrayList<>();
            lines.add(line("Radius: " + arena.radius(), argb(cfg.arenaRadiusLineColor)));
            lines.add(line("X: " + Mth.floor(arena.x()) + "  Y: " + Mth.floor(arena.y())
                    + "  Z: " + Mth.floor(arena.z()), argb(cfg.arenaCoordsLineColor)));

            // The invalid tile lines
            if (cfg.arenaShowInvalidTiles) {
                // Line colors
                int invalidColor = argb(cfg.arenaInvalidLineColor);
                int blockedColor = argb(cfg.arenaBlockedLineColor);
                int liquidColor = argb(cfg.arenaLiquidLineColor);
                int validColor = argb(cfg.arenaValidLineColor);

                // Too big to scan
                if (arena.radius() > cfg.arenaMaxScanRadius) {
                    lines.add(line("Invalid Tiles: radius too large to scan", invalidColor));
                    // The scan hasn't finished yet
                } else if (scan == null) {
                    lines.add(line("Invalid Tiles: scanning...", invalidColor));
                } else {
                    // The counts share one line, each in its own color. Reasons with no tiles are left out.
                    // The pieces of the tile count line
                    List<Seg> counts = new ArrayList<>();
                    // Tile counts per reason from the finished scan
                    int noGround = scan.reasonCounts()[SpawnSpot.Spot.NO_GROUND.ordinal()];
                    int blocked = scan.reasonCounts()[SpawnSpot.Spot.BLOCKED.ordinal()];
                    int liquid = scan.reasonCounts()[SpawnSpot.Spot.LIQUID.ordinal()];
                    // Add a piece for each reason that has tiles
                    if (noGround > 0) counts.add(new Seg("Invalid Tiles: " + noGround, invalidColor));
                    if (blocked > 0) counts.add(new Seg("Blocked Tiles: " + blocked, blockedColor));
                    if (liquid > 0) counts.add(new Seg("Liquid Tiles: " + liquid, liquidColor));

                    // Nothing is invalid at all
                    if (counts.isEmpty()) lines.add(line("No invalid tiles", validColor));
                    else lines.add(new Line(counts));
                }

                // The valid spawn area line
                if (cfg.arenaShowValidPercent && arena.radius() <= cfg.arenaMaxScanRadius) {
                    String percent;
                    if (scan == null) percent = "scanning...";
                        // Nothing was checked (no loaded chunks)
                    else if (scan.scanned() == 0) percent = "no loaded chunks";
                    else {
                        // Invalid tiles = array length / 4, since each tile is 4 ints
                        int invalid = scan.tiles().length / TILE_STRIDE;
                        // Share of the checked columns that are valid, with one decimal ('%%' prints a literal percent sign)
                        percent = String.format(Locale.ROOT, "%.1f%%", (scan.scanned() - invalid) * 100.0 / scan.scanned());
                    }
                    lines.add(line("Valid spawn area: " + percent, validColor));
                }
            }

            // The name is the top line, the other lines hang below it
            // Height of the name line: label height plus one spacing for every line below it
            double topY = arena.y() + cfg.arenaLabelHeight + lines.size() * spacing;
            // The name goes on top
            text(arena.name(), arena.x(), topY, arena.z(), argb(cfg.arenaNameColor), nameScale);
            // The info lines go below it, each one spacing lower than the last
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
        // A single piece: draw it directly
        if (segs.size() == 1) {
            text(segs.get(0).text(), x, y, z, segs.get(0).argb(), scale);
            return;
        }

        // Several pieces: measure each one so they can sit side by side
        Minecraft client = Minecraft.getInstance();
        // World size of one font pixel at this text scale
        double unit = TEXT_WORLD_PER_PIXEL * scale;
        // Width of the gap between pieces
        double gap = client.font.width(SEGMENT_GAP) * unit;

        // Width of each piece
        double[] widths = new double[segs.size()];
        // Total width = all pieces plus the gaps between them
        double total = gap * (segs.size() - 1);
        for (int i = 0; i < widths.length; i++) {
            widths[i] = client.font.width(segs.get(i).text()) * unit;
            total += widths[i];
        }

        // Horizontal direction that points to the right of the screen
        // The player's facing angle in radians
        double yaw = Math.toRadians(client.player == null ? 0.0 : client.player.getYRot());
        // The right direction as x and z, worked out from that angle
        double rightX = -Math.cos(yaw);
        double rightZ = -Math.sin(yaw);

        // Start at the left end so the whole line ends up centered
        double cursor = -total / 2.0;
        for (int i = 0; i < widths.length; i++) {
            // Center of this piece
            double center = cursor + widths[i] / 2.0;
            // Draw the piece shifted along the right direction
            text(segs.get(i).text(), x + rightX * center, y, z + rightZ * center, segs.get(i).argb(), scale);
            // Move the cursor past this piece and the gap
            cursor += widths[i] + gap;
        }
    }

    // Adds a fully opaque alpha byte to an RGB color
    private static int argb(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    // Submits one piece of billboard text (always faces the player)
    private static void text(String text, double x, double y, double z, int argb, float scale) {
        // Empty adjustLeft = centered on the position. Always on top so it stays readable through terrain.
        Gizmos.billboardText(text, new Vec3(x, y, z), new TextGizmo.Style(argb, scale, OptionalDouble.empty()))
                .setAlwaysOnTop();
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    // Every frame after translucent terrain: turns the frame into vertices and draws them in one batch
    private static void renderAndDraw(LevelRenderContext context) {
        // The latest frame from extraction
        Frame current = frame;
        if (current == null || current.states().isEmpty()) return;

        // Settings and camera position for this frame
        final RenderSettings set = current.settings();
        final Vec3 camera = context.levelState().cameraRenderState.pos;

        // Skip arenas that are too far away, and keep only the nearest ones if there are many
        // The arenas to draw this frame
        List<RadiusState> drawList = new ArrayList<>(current.states().size());
        for (RadiusState a : current.states()) {
            // Distance from the camera to the arena's edge
            double distance = Math.sqrt(camera.distanceToSqr(a.x(), a.y(), a.z())) - a.radius();
            if (distance <= set.maxDrawDistance()) drawList.add(a);
        }
        if (drawList.isEmpty()) return;
        if (drawList.size() > set.maxArenasDrawn()) {
            // Sort nearest first so the closest arenas are kept
            drawList.sort(Comparator.comparingDouble(a -> camera.distanceToSqr(a.x(), a.y(), a.z())));
            drawList = new ArrayList<>(drawList.subList(0, set.maxArenasDrawn()));
        }

        // The vertex format the pipeline expects
        var format = PIPELINE.getVertexFormatBinding(0);
        if (format == null) return;

        // This code writes quads, so the pipeline has to draw quads
        PrimitiveTopology primitive = PIPELINE.getPrimitiveTopology();
        if (primitive != PrimitiveTopology.QUADS) {
            if (!warnedTopology) {
                warnedTopology = true;
                LOGGER.error("Arena radius renderer expects a QUADS pipeline but got {}", primitive);
            }
            return;
        }

        // Create the vertex buffer the first time it is needed
        if (stagedBuffer == null) {
            stagedBuffer = new StagedVertexBuffer(() -> "Arena Radius Buffer", BUFFER_SIZE);
        }

        // Start a new draw inside the buffer
        var draw = stagedBuffer.appendDraw(format, primitive, RenderSystem.getProjectionType().vertexSorting());

        // The matrix stack for this frame
        PoseStack matrices = context.poseStack();
        matrices.pushPose();
        // Shift the world so the camera is at the origin (keeps float precision high far from spawn)
        matrices.translate(-camera.x, -camera.y, -camera.z);
        Matrix4fc matrix = matrices.last().pose();

        // Where the vertices are written
        VertexConsumer builder = stagedBuffer.getVertexBuilder(draw);
        // Tile budget shared by all arenas
        int tilesLeft = set.maxTilesDrawn();
        for (RadiusState a : drawList) {
            // Wall and ring first, then the invalid tiles with whatever tile budget is left
            buildGeometry(builder, matrix, a, set);
            tilesLeft -= buildInvalidTiles(builder, matrix, a.invalidTiles(), camera, tilesLeft, set);
        }

        // Restore the matrix stack
        matrices.popPose();

        // Send the vertices to the GPU
        stagedBuffer.upload();
        // Info needed to run the draw
        var info = stagedBuffer.getExecuteInfo(draw);
        if (info != null) {
            Minecraft client = Minecraft.getInstance();

            // Uniform data the shader needs (matrices, color multiplier and offsets)
            var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

            // The screen's color and depth targets
            var mainTarget = client.gameRenderer.mainRenderTarget();
            var colorView = mainTarget.getColorTextureView();
            var depthView = mainTarget.getDepthTextureView();

            // Draw only if there is a color target
            if (colorView != null) {
                // A render pass groups the draw commands. try-with-resources closes it automatically
                try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> MOD_ID + " arena radius rendering",
                        colorView, Optional.empty(), depthView, OptionalDouble.empty())) {
                    // Use the filled-box pipeline
                    renderPass.setPipeline(PIPELINE);
                    // Standard uniforms (projection and so on)
                    RenderSystem.bindDefaultUniforms(renderPass);
                    // Our own transform data
                    renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                    // Bind the vertices
                    renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
                    // Bind the indices that turn each quad into triangles
                    renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
                    // Draw everything that was written
                    renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
                }
            }
        }

        // Tell the buffer this frame's draws are finished so it can be reused
        stagedBuffer.endFrame();
    }

    // Writes the wall and ring of one arena into the buffer
    private static void buildGeometry(VertexConsumer b, Matrix4fc m, RadiusState a, RenderSettings set) {
        // Bottom and top height of the wall
        double yBottom = a.y() - set.wallBelow();
        double yTop = a.y() + set.wallAbove();

        // Wall and ring colors, each with its own opacity
        float[] wallColor = rgba(a.wallRgb(), set.wallAlpha());
        float[] ringColor = rgba(a.ringRgb(), set.ringAlpha());

        // Segment count shared by the wall and ring
        int segments = segmentsFor(a.radius());
        // Wall
        if (set.showWall()) {
            wall(b, m, a.x(), a.z(), a.radius(), yBottom, yTop, segments, wallColor);
        }
        // Ring (flat at the arena height, or following the ring heights if there are any)
        if (set.showRing()) {
            flatRing(b, m, a.x(), a.y() + RING_LIFT, a.z(), a.radius(), set.ringHalfWidth(), segments, ringColor,
                    a.ringY());
        }
    }

    // Splits an RGB number into red, green and blue (0 to 1) and adds the alpha
    private static float[] rgba(int rgb, float alpha) {
        return new float[]{((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha};
    }

    // Tile on the ground of every invalid column near the camera. Returns how many tiles were drawn.
    private static int buildInvalidTiles(VertexConsumer b, Matrix4fc m, int[] tiles, Vec3 camera, int limit,
                                         RenderSettings set) {
        // Squared draw distance, to avoid square roots
        double rangeSqr = set.tileDrawDistance() * set.tileDrawDistance();
        // Tiles drawn so far
        int drawn = 0;

        // Step through the tiles, 4 ints each, until all are done or the limit is reached
        for (int i = 0; i + TILE_STRIDE - 1 < tiles.length && drawn < limit; i += TILE_STRIDE) {
            // Block position of this tile
            int bx = tiles[i];
            int by = tiles[i + 1];
            int bz = tiles[i + 2];
            // Color for this tile's reason (index clamped to stay valid)
            float[][] colors = set.tileColors();
            float[] color = colors[Math.clamp(tiles[i + 3], 0, colors.length - 1)];

            // Distance from the camera to the tile
            double dx = bx + 0.5 - camera.x;
            double dy = by - camera.y;
            double dz = bz + 0.5 - camera.z;
            // Too far away: skip it
            if (dx * dx + dy * dy + dz * dz > rangeSqr) continue;

            // Shrink the quad by the inset so neighbouring tiles look like separate blocks
            double x0 = bx + set.tileInset(), x1 = bx + 1 - set.tileInset();
            double z0 = bz + set.tileInset(), z1 = bz + 1 - set.tileInset();
            // Lift above the ground to avoid flicker
            double y = by + set.tileLift();
            // A flat square quad
            doubleSidedQuad(b, m, color,
                    x0, y, z0,
                    x1, y, z0,
                    x1, y, z1,
                    x0, y, z1);
            // Count it
            drawn++;
        }
        return drawn;
    }

    // How many segments a circle of this radius gets: about 1.5 per block, between MIN_SEGMENTS and MAX_SEGMENTS
    private static int segmentsFor(double radius) {
        return Math.clamp((long) Math.ceil(radius * 1.5), MIN_SEGMENTS, MAX_SEGMENTS);
    }

    // Vertical cylinder wall made of one quad per segment
    private static void wall(VertexConsumer b, Matrix4fc m, double cx, double cz, double radius,
                             double yBottom, double yTop, int segments, float[] color) {
        // One quad per segment
        for (int i = 0; i < segments; i++) {
            // Start and end angle of this segment
            double a0 = (Math.PI * 2.0) * i / segments;
            double a1 = (Math.PI * 2.0) * (i + 1) / segments;
            // Start and end points on the circle
            double x0 = cx + radius * Math.cos(a0), z0 = cz + radius * Math.sin(a0);
            double x1 = cx + radius * Math.cos(a1), z1 = cz + radius * Math.sin(a1);

            // Bottom edge opaque, top edge fading out
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
        // Wrong number of heights: draw flat instead
        if (ys != null && ys.length != segments) ys = null;
        // Inner and outer edge of the ring (the inner edge can't go below the center)
        double inner = Math.max(0.0, radius - halfWidth);
        double outer = radius + halfWidth;
        // One quad per segment
        for (int i = 0; i < segments; i++) {
            double a0 = (Math.PI * 2.0) * i / segments;
            double a1 = (Math.PI * 2.0) * (i + 1) / segments;
            // Cosine and sine of the start and end angles
            double c0 = Math.cos(a0), s0 = Math.sin(a0);
            double c1 = Math.cos(a1), s1 = Math.sin(a1);
            // Heights at the start and end of the segment (the last segment wraps back to the first)
            double y0 = ys == null ? y : ys[i];
            double y1 = ys == null ? y : ys[(i + 1) % segments];

            // Quad between the inner and outer edge
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
        // Front face: alpha at the bottom, 0 at the top
        vertex(b, m, c, c[3], x0, y0, z0);
        vertex(b, m, c, c[3], x1, y1, z1);
        vertex(b, m, c, 0f, x2, y2, z2);
        vertex(b, m, c, 0f, x3, y3, z3);

        // Back face: the same corners in reverse order
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
        // Front face
        vertex(b, m, c, x0, y0, z0);
        vertex(b, m, c, x1, y1, z1);
        vertex(b, m, c, x2, y2, z2);
        vertex(b, m, c, x3, y3, z3);

        // Back face (reverse order)
        vertex(b, m, c, x3, y3, z3);
        vertex(b, m, c, x2, y2, z2);
        vertex(b, m, c, x1, y1, z1);
        vertex(b, m, c, x0, y0, z0);
    }

    // Adds one vertex using the color's own alpha
    private static void vertex(VertexConsumer b, Matrix4fc m, float[] c, double x, double y, double z) {
        vertex(b, m, c, c[3], x, y, z);
    }

    // Adds one vertex: moves the position by the matrix and sets the color and alpha
    private static void vertex(VertexConsumer b, Matrix4fc m, float[] c, float alpha, double x, double y, double z) {
        b.addVertex(m, (float) x, (float) y, (float) z).setColor(c[0], c[1], c[2], alpha);
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    // Releases the GPU buffer (called when the game closes)
    private static void close() {
        if (stagedBuffer != null) {
            stagedBuffer.close();
            stagedBuffer = null;
        }
    }
}