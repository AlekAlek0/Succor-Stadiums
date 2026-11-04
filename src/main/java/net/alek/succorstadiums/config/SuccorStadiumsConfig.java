package net.alek.succorstadiums.config;

// SuccorStadiumsConfig class
public class SuccorStadiumsConfig {

    // ── General Category ──────────────────────────────────────────────────

    public Theme mobArenaTheme = Theme.DARK;   // color theme of the Mob Arena GUI

    public MagicIndicatorMode magicIndicatorMode = MagicIndicatorMode.CROSSHAIR;   // where the magic indicator is shown (crosshair by default)

    // ── Pouch Auto Deposit Category ──────────────────────────────────────────────────

    public PouchScrollDirection pouchScrollDirection = PouchScrollDirection.DEFAULT;   // DEFAULT: scrolling up deposits, INVERTED: scrolling up withdraws
    public boolean plainsPouchAutoDepositEnabled = true;   // coins picked up off the ground go straight into the plains coin pouch

    // ── Mob Arena Rendering Category ──────────────────────────────────────────────────

    // Inclusive int range, shared by the config screen (slider / field limits) and clampArenaRendering()
    // Values are clamped to their Range by clampArenaRendering(), so a hand-edited config file can't break rendering.
    public record Range(int min, int max) {
        public int clamp(int value) {
            return Math.clamp(value, min, max);
        }
    }

    public static final Range ARENA_OPACITY = new Range(0, 100);                // percent
    public static final Range ARENA_WALL_BELOW = new Range(0, 32);              // blocks
    public static final Range ARENA_WALL_ABOVE = new Range(1, 64);              // blocks
    public static final Range ARENA_LABEL_RANGE = new Range(8, 512);            // blocks
    public static final Range ARENA_LABEL_HEIGHT = new Range(0, 32);            // blocks
    public static final Range ARENA_LABEL_LINE_SPACING = new Range(25, 200);    // hundredths of a block
    public static final Range ARENA_TEXT_SCALE = new Range(30, 300);            // percent
    public static final Range ARENA_TILE_DRAW_DISTANCE = new Range(8, 128);     // blocks
    public static final Range ARENA_MAX_TILES_DRAWN = new Range(100, 20000);
    public static final Range ARENA_TILE_INSET = new Range(0, 30);              // hundredths of a block
    public static final Range ARENA_TILE_LIFT = new Range(1, 20);               // hundredths of a block
    public static final Range ARENA_MAX_DRAW_DISTANCE = new Range(32, 1024);    // blocks
    public static final Range ARENA_MAX_ARENAS_DRAWN = new Range(1, 32);
    public static final Range ARENA_DATA_REFRESH_TICKS = new Range(5, 100);
    public static final Range ARENA_MAX_SCAN_RADIUS = new Range(8, 160);        // blocks
    public static final Range ARENA_SCAN_COLUMNS_PER_TICK = new Range(50, 5000);
    public static final Range ARENA_RESCAN_DELAY_TICKS = new Range(0, 200);

    // What to show
    public boolean arenaShowWall = true;             // the translucent wall around the edge of the arena
    public boolean arenaShowLabel = true;            // the floating label above the arena center (name, radius, coordinates, tile counts)
    public boolean arenaShowInvalidTiles = true;     // mark the blocks where mobs can't spawn, and add their counts to the label
    public boolean arenaShowValidPercent = true;     // "Valid spawn area: 87%" line in the label (needs invalid tiles on)

    // Circle
    public int arenaWallColor = 0xFF69B4;      // RGB, pink: color of the wall
    public int arenaWallOpacity = 35;          // percent, strongest at the bottom edge and fades to 0 at the top
    public int arenaWallBelow = 4;             // blocks the wall reaches below the arena center Y
    public int arenaWallAbove = 16;            // blocks the wall reaches above the arena center Y

    // Floating label
    public int arenaNameColor = 0xFF69B4;         // RGB, pink: the arena name line
    public int arenaRadiusLineColor = 0xFFFFFF;   // RGB, white: the "Radius: N" line
    public int arenaCoordsLineColor = 0xBBBBBB;   // RGB, light gray: the "X: Y: Z:" line
    // The three invalid tile counts share one line. Their defaults match the tile colors below, but they are separate settings.
    public int arenaInvalidLineColor = 0xFF2626;  // RGB, red: "Invalid Tiles" (no solid ground)
    public int arenaBlockedLineColor = 0xFFB020;  // RGB, orange: "Blocked Tiles" (ceiling, leaves, a block in the way)
    public int arenaLiquidLineColor = 0x3A8DFF;   // RGB, blue: "Liquid Tiles" (water or lava)
    public int arenaLowLineColor = 0xB040FF;      // RGB, purple: "Low Tiles" (a mob could stand here, but it is below the arena's height)
    public int arenaValidLineColor = 0x55FF55;    // RGB, light green: the "Valid spawn area" line (and "No invalid tiles")
    public int arenaLabelRange = 128;          // blocks, the label is shown when the player is this close to the arena center
    public int arenaLabelHeight = 3;           // blocks above the arena center Y where the label floats
    public int arenaLabelLineSpacing = 75;     // hundredths of a block between the label lines
    public int arenaNameScale = 100;           // percent, text size of the name line
    public int arenaInfoScale = 70;            // percent, text size of the other lines

    // Invalid spawn tiles
    public boolean arenaColorTilesByReason = true;   // false = every invalid tile uses the ground color
    public int arenaTileColor = 0xFF2626;      // RGB, red: block below isn't solid ground (air, slab, fence, ...)
    public int arenaTileLiquidColor = 0x3A8DFF;   // RGB, blue: water or lava
    public int arenaTileBlockedColor = 0xFFB020;  // RGB, orange: ceiling, leaves or a block in the way
    public int arenaTileLowColor = 0xB040FF;      // RGB, purple: below the arena's height (mobs only spawn at or above it)
    public int arenaTileOpacity = 45;          // percent, how visible the tiles are
    public int arenaTileDrawDistance = 48;     // blocks from the camera, tiles farther away aren't drawn
    public int arenaMaxTilesDrawn = 8000;      // most tiles drawn in one frame, lower it if the game stutters near big arenas
    public int arenaTileInset = 4;             // hundredths of a block of gap between a tile and its block edges
    public int arenaTileLift = 3;              // hundredths of a block the tiles float above the ground (raise if they flicker)

    // Performance
    public int arenaMaxDrawDistance = 256;     // blocks, arenas whose edge is farther than this from the camera are skipped
    public int arenaMaxArenasDrawn = 16;       // when more arenas are shown than this, only the nearest ones are drawn
    public int arenaDataRefreshTicks = 10;     // ticks between requests for fresh arena data while an overlay is on (20 ticks = 1 second)
    public int arenaMaxScanRadius = 96;        // blocks, arenas with a larger radius are not scanned for invalid tiles
    public int arenaScanColumnsPerTick = 500;  // block columns checked per tick by the invalid tile scan, shared by all scanned arenas
    public int arenaRescanDelayTicks = 5;      // ticks to wait between full scans, so block changes show up after about this long

    // Clamps every Mob Arena Rendering value into its allowed range (call before reading the values)
    public void clampArenaRendering() {
        arenaWallColor &= 0xFFFFFF;
        arenaNameColor &= 0xFFFFFF;
        arenaRadiusLineColor &= 0xFFFFFF;
        arenaCoordsLineColor &= 0xFFFFFF;
        arenaInvalidLineColor &= 0xFFFFFF;
        arenaBlockedLineColor &= 0xFFFFFF;
        arenaLowLineColor &= 0xFFFFFF;
        arenaLiquidLineColor &= 0xFFFFFF;
        arenaValidLineColor &= 0xFFFFFF;
        arenaTileLiquidColor &= 0xFFFFFF;
        arenaTileBlockedColor &= 0xFFFFFF;
        arenaTileLowColor &= 0xFFFFFF;
        arenaTileColor &= 0xFFFFFF;

        arenaWallOpacity = ARENA_OPACITY.clamp(arenaWallOpacity);
        arenaWallBelow = ARENA_WALL_BELOW.clamp(arenaWallBelow);
        arenaWallAbove = ARENA_WALL_ABOVE.clamp(arenaWallAbove);

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