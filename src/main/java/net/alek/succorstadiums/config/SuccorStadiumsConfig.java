package net.alek.succorstadiums.config;

// SuccorStadiumsConfig class
public class SuccorStadiumsConfig {

    // ── General Category ──────────────────────────────────────────────────

    public Theme mobArenaTheme = Theme.DARK;

    public MagicIndicatorMode magicIndicatorMode = MagicIndicatorMode.CROSSHAIR;

    // ── Pouch Auto Deposit Category ──────────────────────────────────────────────────

    public PouchScrollDirection pouchScrollDirection = PouchScrollDirection.DEFAULT;
    public boolean plainsPouchAutoDepositEnabled = true;

    // ── Mob Arena Rendering Category ──────────────────────────────────────────────────

    // Inclusive int range, shared by the config screen (slider / field limits) and clampArenaRendering()
    // Values are clamped to their Range by clampArenaRendering(), so a hand-edited config file can't break rendering.
    public record Range(int min, int max) {
        public int clamp(int value) {
            return Math.clamp(value, min, max);
        }
    }

    public static final Range ARENA_OPACITY = new Range(0, 100);                  // percent
    public static final Range ARENA_WALL_BELOW = new Range(0, 32);                // blocks
    public static final Range ARENA_WALL_ABOVE = new Range(1, 64);                // blocks
    public static final Range ARENA_RING_WIDTH = new Range(1, 100);               // hundredths of a block
    public static final Range ARENA_LABEL_RANGE = new Range(8, 512);              // blocks
    public static final Range ARENA_LABEL_HEIGHT = new Range(0, 32);              // blocks
    public static final Range ARENA_LABEL_LINE_SPACING = new Range(25, 200);      // hundredths of a block
    public static final Range ARENA_TEXT_SCALE = new Range(30, 300);              // percent
    public static final Range ARENA_TILE_DRAW_DISTANCE = new Range(8, 128);       // blocks
    public static final Range ARENA_MAX_TILES_DRAWN = new Range(100, 20000);      // tiles
    public static final Range ARENA_TILE_INSET = new Range(0, 30);                // hundredths of a block
    public static final Range ARENA_TILE_LIFT = new Range(1, 20);                 // hundredths of a block
    public static final Range ARENA_MAX_DRAW_DISTANCE = new Range(32, 1024);      // blocks
    public static final Range ARENA_MAX_ARENAS_DRAWN = new Range(1, 32);          // arenas
    public static final Range ARENA_DATA_REFRESH_TICKS = new Range(5, 100);       // ticks
    public static final Range ARENA_MAX_SCAN_RADIUS = new Range(8, 160);          // blocks
    public static final Range ARENA_SCAN_COLUMNS_PER_TICK = new Range(50, 5000);
    public static final Range ARENA_RESCAN_DELAY_TICKS = new Range(0, 200);

    // What to show
    public boolean arenaShowWall = true;
    public boolean arenaShowRing = true;
    public boolean arenaShowLabel = true;
    public boolean arenaShowInvalidTiles = true;
    public boolean arenaShowValidPercent = true;                // "Valid spawn area: 87%" line (needs invalid tiles on)

    // Circle
    public int arenaWallColor = 0xFF69B4;            // RGB, pink
    public int arenaRingColor = 0xFF69B4;            // RGB, pink
    public int arenaWallOpacity = 35;                // percent, strongest at the bottom edge and fades to 0 at the top
    public int arenaRingOpacity = 90;                // percent
    public int arenaWallBelow = 4;                   // blocks below the arena center Y
    public int arenaWallAbove = 16;                  // blocks above the arena center Y
    public int arenaRingWidth = 12;                  // hundredths of a block
    public boolean arenaRingFollowsTerrain = true;   // ring hugs the ground instead of staying at the arena center height

    // Floating label
    public int arenaNameColor = 0xFF69B4;         // RGB, pink
    public int arenaRadiusLineColor = 0xFFFFFF;   // RGB, white
    public int arenaCoordsLineColor = 0xBBBBBB;   // RGB, light gray
    public int arenaInvalidLineColor = 0xFF2626;  // RGB, red
    public int arenaBlockedLineColor = 0xFFB020;  // RGB, orange
    public int arenaLiquidLineColor = 0x3A8DFF;   // RGB, blue
    public int arenaValidLineColor = 0x55FF55;    // RGB, light green
    public int arenaLabelRange = 128;             // shown when the player is within this many blocks of the arena center
    public int arenaLabelHeight = 3;              // blocks above the arena center Y
    public int arenaLabelLineSpacing = 75;        // hundredths of a block between lines
    public int arenaNameScale = 100;              // percent
    public int arenaInfoScale = 70;               // percent

    // Invalid spawn tiles
    public boolean arenaColorTilesByReason = true;   // false = every invalid tile uses the ground color
    public int arenaTileColor = 0xFF2626;            // RGB, red
    public int arenaTileLiquidColor = 0x3A8DFF;      // RGB, blue
    public int arenaTileBlockedColor = 0xFFB020;     // RGB, orange
    public int arenaTileOpacity = 45;                // percent
    public int arenaTileDrawDistance = 48;           // blocks from the camera
    public int arenaMaxTilesDrawn = 8000;            // per frame
    public int arenaTileInset = 4;                   // hundredths of a block of gap to the block edges
    public int arenaTileLift = 3;                    // hundredths of a block above the ground

    // Performance
    public int arenaMaxDrawDistance = 256;     // arenas whose edge is farther than this from the camera are skipped
    public int arenaMaxArenasDrawn = 16;       // nearest first
    public int arenaDataRefreshTicks = 10;     // how often fresh arena data is requested while an overlay is on
    public int arenaMaxScanRadius = 96;        // arenas with a larger radius are not scanned for invalid tiles
    public int arenaScanColumnsPerTick = 500;  // invalid tile scan speed, shared by all scanned arenas
    public int arenaRescanDelayTicks = 5;      // pause between full scans

    // Clamps every Mob Arena Rendering value into its allowed range (call before reading the values)
    public void clampArenaRendering() {
        arenaWallColor &= 0xFFFFFF;
        arenaRingColor &= 0xFFFFFF;
        arenaNameColor &= 0xFFFFFF;
        arenaRadiusLineColor &= 0xFFFFFF;
        arenaCoordsLineColor &= 0xFFFFFF;
        arenaInvalidLineColor &= 0xFFFFFF;
        arenaBlockedLineColor &= 0xFFFFFF;
        arenaLiquidLineColor &= 0xFFFFFF;
        arenaValidLineColor &= 0xFFFFFF;
        arenaTileLiquidColor &= 0xFFFFFF;
        arenaTileBlockedColor &= 0xFFFFFF;
        arenaTileColor &= 0xFFFFFF;

        arenaWallOpacity = ARENA_OPACITY.clamp(arenaWallOpacity);
        arenaRingOpacity = ARENA_OPACITY.clamp(arenaRingOpacity);
        arenaWallBelow = ARENA_WALL_BELOW.clamp(arenaWallBelow);
        arenaWallAbove = ARENA_WALL_ABOVE.clamp(arenaWallAbove);
        arenaRingWidth = ARENA_RING_WIDTH.clamp(arenaRingWidth);

        arenaLabelRange = ARENA_LABEL_RANGE.clamp(arenaLabelRange);
        arenaLabelHeight = ARENA_LABEL_HEIGHT.clamp(arenaLabelHeight);
        arenaLabelLineSpacing = ARENA_LABEL_LINE_SPACING.clamp(arenaLabelLineSpacing);
        arenaNameScale = ARENA_TEXT_SCALE.clamp(arenaNameScale);
        arenaInfoScale = ARENA_TEXT_SCALE.clamp(arenaInfoScale);

        arenaTileOpacity = ARENA_OPACITY.clamp(arenaTileOpacity);
        arenaTileDrawDistance = ARENA_TILE_DRAW_DISTANCE.clamp(arenaTileDrawDistance);
        arenaMaxTilesDrawn = ARENA_MAX_TILES_DRAWN.clamp(arenaMaxTilesDrawn);
        arenaTileInset = ARENA_TILE_INSET.clamp(arenaTileInset);
        arenaTileLift = ARENA_TILE_LIFT.clamp(arenaTileLift);

        arenaMaxDrawDistance = ARENA_MAX_DRAW_DISTANCE.clamp(arenaMaxDrawDistance);
        arenaMaxArenasDrawn = ARENA_MAX_ARENAS_DRAWN.clamp(arenaMaxArenasDrawn);
        arenaDataRefreshTicks = ARENA_DATA_REFRESH_TICKS.clamp(arenaDataRefreshTicks);
        arenaMaxScanRadius = ARENA_MAX_SCAN_RADIUS.clamp(arenaMaxScanRadius);
        arenaScanColumnsPerTick = ARENA_SCAN_COLUMNS_PER_TICK.clamp(arenaScanColumnsPerTick);
        arenaRescanDelayTicks = ARENA_RESCAN_DELAY_TICKS.clamp(arenaRescanDelayTicks);
    }
}