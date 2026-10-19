package net.alek.succorstadiums.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.function.IntFunction;
import java.util.function.Consumer;
import java.util.Optional;
import java.util.Locale;

import net.alek.succorstadiums.network.item.equipment.pouch.SetPouchAutoDepositPayload;
import net.alek.succorstadiums.item.ModItems;

// SuccorStadiumsConfigScreen class
public class SuccorStadiumsConfigScreen {

    // Load the config file
    private static final SuccorStadiumsConfig CONFIG = SuccorStadiumsConfigManager.load();

    // A fresh config, used as the single source of truth for the "reset to default" values
    private static final SuccorStadiumsConfig DEFAULTS = new SuccorStadiumsConfig();

    // Crate screen method
    public static Screen create(Screen parent) {

        // Create the config builder with parent screen and succor stadiums title
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Succor Stadiums"));

        // Create entries
        ConfigEntryBuilder entries = builder.entryBuilder();

        // Create a new category called general
        ConfigCategory general = builder.getOrCreateCategory(
                Component.literal("General")
        );

        // Add entries to the general category
        general.addEntry(
                entries.startTextDescription(
                        Component.literal("TIP: Press \\ by default anytime in-game to open this menu quickly!")
                                .withStyle(ChatFormatting.YELLOW)
                ).build()
        );

        general.addEntry(
                entries.startEnumSelector(Component.literal("GUI Theme"), Theme.class,
                                CONFIG.mobArenaTheme)
                        .setDefaultValue(Theme.DARK)
                        .setSaveConsumer(value -> CONFIG.mobArenaTheme = value)
                        .setEnumNameProvider(value -> Component.literal(((Theme) value).getDisplayName())).build()
        );

        general.addEntry(
                entries.startEnumSelector(Component.literal("Magic Indicator"), MagicIndicatorMode.class,
                                CONFIG.magicIndicatorMode)
                        .setDefaultValue(MagicIndicatorMode.CROSSHAIR)
                        .setSaveConsumer(value -> CONFIG.magicIndicatorMode = value)
                        .setEnumNameProvider(value -> Component.literal(((MagicIndicatorMode) value).getDisplayName())).build()
        );

        // Create a new category called pouch auto deposit
        ConfigCategory pouchAutoDeposit = builder.getOrCreateCategory(
                Component.literal("Pouch Auto Deposit")
        );

        // Add entries to the pouch auto deposit category
        pouchAutoDeposit.addEntry(
                entries.startEnumSelector(Component.literal("Pouch Scroll Direction - DEFAULT (Scroll Up Deposits) - INVERTED (Scroll Up Withdraws)"), PouchScrollDirection.class,
                                CONFIG.pouchScrollDirection)
                        .setDefaultValue(PouchScrollDirection.DEFAULT)
                        .setSaveConsumer(value -> CONFIG.pouchScrollDirection = value)
                        .setEnumNameProvider(value -> Component.literal(((PouchScrollDirection) value).getDisplayName())).build()
        );

        pouchAutoDeposit.addEntry(
                entries.startBooleanToggle(Component.literal("Plains Coin Pouch: Auto Deposit"), CONFIG.plainsPouchAutoDepositEnabled)
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Automatically deposit coins picked up off the ground into your pouch."))
                        .setSaveConsumer(value -> {CONFIG.plainsPouchAutoDepositEnabled = value;
                            ClientPlayNetworking.send(new SetPouchAutoDepositPayload(BuiltInRegistries.ITEM.getKey(ModItems.PLAINS_COIN_POUCH), value));
                        }).build()
        );

        // Create a new category called mob arena rendering
        ConfigCategory arenaRendering = builder.getOrCreateCategory(
                Component.literal("Mob Arena Rendering")
        );

        // Make sure old or hand-edited values show up inside their slider / field limits
        CONFIG.clampArenaRendering();


        // Create header for what to show config options
        addHeader(arenaRendering, entries, "What To Show");

        // Add entries to what to show area of the mob arena rendering category
        addToggle(arenaRendering, entries, "Show Wall",
                "The translucent wall around the edge of the arena.",
                CONFIG.arenaShowWall, DEFAULTS.arenaShowWall,
                value -> CONFIG.arenaShowWall = value);

        addToggle(arenaRendering, entries, "Show Ground Ring",
                "The thin ring on the ground at the arena edge.",
                CONFIG.arenaShowRing, DEFAULTS.arenaShowRing,
                value -> CONFIG.arenaShowRing = value);

        addToggle(arenaRendering, entries, "Show Floating Label",
                "Name, radius and coordinates floating above the arena center.",
                CONFIG.arenaShowLabel, DEFAULTS.arenaShowLabel,
                value -> CONFIG.arenaShowLabel = value);

        addToggle(arenaRendering, entries, "Show Invalid Spawn Tiles",
                "Marks the blocks inside the arena where mobs can't spawn. Also adds the invalid tile count to the label.",
                CONFIG.arenaShowInvalidTiles, DEFAULTS.arenaShowInvalidTiles,
                value -> CONFIG.arenaShowInvalidTiles = value);

        addToggle(arenaRendering, entries, "Show Valid Spawn Percentage",
                "Adds a line to the label with the share of the arena where mobs can spawn. Needs Show Invalid Spawn Tiles.",
                CONFIG.arenaShowValidPercent, DEFAULTS.arenaShowValidPercent,
                value -> CONFIG.arenaShowValidPercent = value);

        // Create header for circle config options
        addHeader(arenaRendering, entries, "Circle");

        // Add entries to circle area of the mob arena rendering category
        addColor(arenaRendering, entries, "Wall Color",
                "Color of the arena wall.",
                CONFIG.arenaWallColor, DEFAULTS.arenaWallColor,
                value -> CONFIG.arenaWallColor = value);

        addColor(arenaRendering, entries, "Ring Color",
                "Color of the ground ring.",
                CONFIG.arenaRingColor, DEFAULTS.arenaRingColor,
                value -> CONFIG.arenaRingColor = value);

        addSlider(arenaRendering, entries, "Wall Opacity",
                "How visible the wall is at the bottom. It fades out toward the top.",
                CONFIG.arenaWallOpacity, DEFAULTS.arenaWallOpacity, SuccorStadiumsConfig.ARENA_OPACITY,
                value -> value + "%",
                value -> CONFIG.arenaWallOpacity = value);

        addSlider(arenaRendering, entries, "Ring Opacity",
                "How visible the ground ring is.",
                CONFIG.arenaRingOpacity, DEFAULTS.arenaRingOpacity, SuccorStadiumsConfig.ARENA_OPACITY,
                value -> value + "%",
                value -> CONFIG.arenaRingOpacity = value);

        addSlider(arenaRendering, entries, "Wall Depth Below Center",
                "How far the wall reaches below the arena center.",
                CONFIG.arenaWallBelow, DEFAULTS.arenaWallBelow, SuccorStadiumsConfig.ARENA_WALL_BELOW,
                value -> value + " blocks",
                value -> CONFIG.arenaWallBelow = value);

        addSlider(arenaRendering, entries, "Wall Height Above Center",
                "How far the wall reaches above the arena center.",
                CONFIG.arenaWallAbove, DEFAULTS.arenaWallAbove, SuccorStadiumsConfig.ARENA_WALL_ABOVE,
                value -> value + " blocks",
                value -> CONFIG.arenaWallAbove = value);

        addSlider(arenaRendering, entries, "Ring Width",
                "Width of the ground ring.",
                CONFIG.arenaRingWidth, DEFAULTS.arenaRingWidth, SuccorStadiumsConfig.ARENA_RING_WIDTH,
                SuccorStadiumsConfigScreen::hundredthsOfBlocks,
                value -> CONFIG.arenaRingWidth = value);

        addToggle(arenaRendering, entries, "Ring Follows Terrain",
                "The ground ring rises and falls with the terrain instead of staying at the arena center height.",
                CONFIG.arenaRingFollowsTerrain, DEFAULTS.arenaRingFollowsTerrain,
                value -> CONFIG.arenaRingFollowsTerrain = value);

        // Create header for floating label config options
        addHeader(arenaRendering, entries, "Floating Label");

        // Add entries to floating label area of the mob arena rendering category
        addColor(arenaRendering, entries, "Name Color",
                "Color of the arena name line.",
                CONFIG.arenaNameColor, DEFAULTS.arenaNameColor,
                value -> CONFIG.arenaNameColor = value);

        addColor(arenaRendering, entries, "Radius Line Color",
                "Color of the \"Radius: N\" line.",
                CONFIG.arenaRadiusLineColor, DEFAULTS.arenaRadiusLineColor,
                value -> CONFIG.arenaRadiusLineColor = value);

        addColor(arenaRendering, entries, "Coordinates Line Color",
                "Color of the X / Y / Z line.",
                CONFIG.arenaCoordsLineColor, DEFAULTS.arenaCoordsLineColor,
                value -> CONFIG.arenaCoordsLineColor = value);

        addColor(arenaRendering, entries, "Invalid Tiles Line Color",
                "Color of the \"Invalid Tiles\" line (blocks with no solid ground). Defaults to the Ground Tile Color.",
                CONFIG.arenaInvalidLineColor, DEFAULTS.arenaInvalidLineColor,
                value -> CONFIG.arenaInvalidLineColor = value);

        addColor(arenaRendering, entries, "Blocked Tiles Line Color",
                "Color of the \"Blocked Tiles\" line. Defaults to the Blocked Tile Color.",
                CONFIG.arenaBlockedLineColor, DEFAULTS.arenaBlockedLineColor,
                value -> CONFIG.arenaBlockedLineColor = value);

        addColor(arenaRendering, entries, "Liquid Tiles Line Color",
                "Color of the \"Liquid Tiles\" line. Defaults to the Liquid Tile Color.",
                CONFIG.arenaLiquidLineColor, DEFAULTS.arenaLiquidLineColor,
                value -> CONFIG.arenaLiquidLineColor = value);

        addColor(arenaRendering, entries, "Valid Area Line Color",
                "Color of the \"Valid spawn area\" line.",
                CONFIG.arenaValidLineColor, DEFAULTS.arenaValidLineColor,
                value -> CONFIG.arenaValidLineColor = value);

        addNumber(arenaRendering, entries, "Label Range",
                "The label is only shown when you are within this many blocks of the arena center.",
                CONFIG.arenaLabelRange, DEFAULTS.arenaLabelRange, SuccorStadiumsConfig.ARENA_LABEL_RANGE,
                value -> CONFIG.arenaLabelRange = value);

        addSlider(arenaRendering, entries, "Label Height",
                "How far above the arena center the label floats.",
                CONFIG.arenaLabelHeight, DEFAULTS.arenaLabelHeight, SuccorStadiumsConfig.ARENA_LABEL_HEIGHT,
                value -> value + " blocks",
                value -> CONFIG.arenaLabelHeight = value);

        addSlider(arenaRendering, entries, "Label Line Spacing",
                "Distance between the lines of the label.",
                CONFIG.arenaLabelLineSpacing, DEFAULTS.arenaLabelLineSpacing, SuccorStadiumsConfig.ARENA_LABEL_LINE_SPACING,
                SuccorStadiumsConfigScreen::hundredthsOfBlocks,
                value -> CONFIG.arenaLabelLineSpacing = value);

        addSlider(arenaRendering, entries, "Name Text Size",
                "Size of the arena name line.",
                CONFIG.arenaNameScale, DEFAULTS.arenaNameScale, SuccorStadiumsConfig.ARENA_TEXT_SCALE,
                value -> value + "%",
                value -> CONFIG.arenaNameScale = value);

        addSlider(arenaRendering, entries, "Info Text Size",
                "Size of the radius, coordinates and invalid tile lines.",
                CONFIG.arenaInfoScale, DEFAULTS.arenaInfoScale, SuccorStadiumsConfig.ARENA_TEXT_SCALE,
                value -> value + "%",
                value -> CONFIG.arenaInfoScale = value);

        // Create header for invalid spawn tiles config options
        addHeader(arenaRendering, entries, "Invalid Spawn Tiles");

        // Add entries to invalid spawn tiles area of the mob arena rendering category
        addToggle(arenaRendering, entries, "Color Tiles By Reason",
                "Use a different color for each reason a block is invalid. When off, every invalid tile uses the Ground Tile Color.",
                CONFIG.arenaColorTilesByReason, DEFAULTS.arenaColorTilesByReason,
                value -> CONFIG.arenaColorTilesByReason = value);

        addColor(arenaRendering, entries, "Ground Tile Color",
                "Mobs can't stand here because the block below isn't a full solid block (air, slab, fence, ...).",
                CONFIG.arenaTileColor, DEFAULTS.arenaTileColor,
                value -> CONFIG.arenaTileColor = value);

        addColor(arenaRendering, entries, "Liquid Tile Color",
                "Mobs can't stand here because of water or lava.",
                CONFIG.arenaTileLiquidColor, DEFAULTS.arenaTileLiquidColor,
                value -> CONFIG.arenaTileLiquidColor = value);

        addColor(arenaRendering, entries, "Blocked Tile Color",
                "Mobs can't stand here because a block is in the way (a ceiling, leaves, ...).",
                CONFIG.arenaTileBlockedColor, DEFAULTS.arenaTileBlockedColor,
                value -> CONFIG.arenaTileBlockedColor = value);

        addSlider(arenaRendering, entries, "Tile Opacity",
                "How visible the invalid spawn tiles are.",
                CONFIG.arenaTileOpacity, DEFAULTS.arenaTileOpacity, SuccorStadiumsConfig.ARENA_OPACITY,
                value -> value + "%",
                value -> CONFIG.arenaTileOpacity = value);

        addSlider(arenaRendering, entries, "Tile Draw Distance",
                "Tiles farther than this from you are not drawn.",
                CONFIG.arenaTileDrawDistance, DEFAULTS.arenaTileDrawDistance, SuccorStadiumsConfig.ARENA_TILE_DRAW_DISTANCE,
                value -> value + " blocks",
                value -> CONFIG.arenaTileDrawDistance = value);

        addNumber(arenaRendering, entries, "Max Tiles Drawn",
                "The most tiles drawn in one frame. Lower this if the game stutters near large arenas.",
                CONFIG.arenaMaxTilesDrawn, DEFAULTS.arenaMaxTilesDrawn, SuccorStadiumsConfig.ARENA_MAX_TILES_DRAWN,
                value -> CONFIG.arenaMaxTilesDrawn = value);

        addSlider(arenaRendering, entries, "Tile Inset",
                "Gap between a tile and the edges of its block, so neighbouring tiles read as separate blocks.",
                CONFIG.arenaTileInset, DEFAULTS.arenaTileInset, SuccorStadiumsConfig.ARENA_TILE_INSET,
                SuccorStadiumsConfigScreen::hundredthsOfBlocks,
                value -> CONFIG.arenaTileInset = value);

        addSlider(arenaRendering, entries, "Tile Lift",
                "How far tiles float above the ground. Raise this if tiles flicker.",
                CONFIG.arenaTileLift, DEFAULTS.arenaTileLift, SuccorStadiumsConfig.ARENA_TILE_LIFT,
                SuccorStadiumsConfigScreen::hundredthsOfBlocks,
                value -> CONFIG.arenaTileLift = value);

        // Create header for performance config options
        addHeader(arenaRendering, entries, "Performance");

        // Add entries to performance area of the mob arena rendering category
        addNumber(arenaRendering, entries, "Max Arena Draw Distance",
                "Arenas whose edge is farther than this many blocks from you are not drawn.",
                CONFIG.arenaMaxDrawDistance, DEFAULTS.arenaMaxDrawDistance, SuccorStadiumsConfig.ARENA_MAX_DRAW_DISTANCE,
                value -> CONFIG.arenaMaxDrawDistance = value);

        addSlider(arenaRendering, entries, "Max Arenas Drawn",
                "When more arenas are switched on than this, only the nearest ones are drawn.",
                CONFIG.arenaMaxArenasDrawn, DEFAULTS.arenaMaxArenasDrawn, SuccorStadiumsConfig.ARENA_MAX_ARENAS_DRAWN,
                String::valueOf,
                value -> CONFIG.arenaMaxArenasDrawn = value);

        addSlider(arenaRendering, entries, "Arena Data Refresh",
                "How often the overlay asks the server for fresh arena data while it is on (20 ticks = 1 second).",
                CONFIG.arenaDataRefreshTicks, DEFAULTS.arenaDataRefreshTicks, SuccorStadiumsConfig.ARENA_DATA_REFRESH_TICKS,
                value -> value + " ticks",
                value -> CONFIG.arenaDataRefreshTicks = value);

        addSlider(arenaRendering, entries, "Max Scan Radius",
                "Arenas with a larger radius are not scanned for invalid spawn tiles.",
                CONFIG.arenaMaxScanRadius, DEFAULTS.arenaMaxScanRadius, SuccorStadiumsConfig.ARENA_MAX_SCAN_RADIUS,
                value -> value + " blocks",
                value -> CONFIG.arenaMaxScanRadius = value);

        addNumber(arenaRendering, entries, "Scan Speed",
                "Block columns checked per tick for invalid spawn tiles, shared by all arenas. Higher is faster but costs more.",
                CONFIG.arenaScanColumnsPerTick, DEFAULTS.arenaScanColumnsPerTick, SuccorStadiumsConfig.ARENA_SCAN_COLUMNS_PER_TICK,
                value -> CONFIG.arenaScanColumnsPerTick = value);

        addSlider(arenaRendering, entries, "Rescan Delay",
                "Pause between full scans, so block changes show up after about this long.",
                CONFIG.arenaRescanDelayTicks, DEFAULTS.arenaRescanDelayTicks, SuccorStadiumsConfig.ARENA_RESCAN_DELAY_TICKS,
                value -> value + " ticks",
                value -> CONFIG.arenaRescanDelayTicks = value);

        // Save the config file
        builder.setSavingRunnable(() -> SuccorStadiumsConfigManager.save(CONFIG));

        return builder.build();
    }

    // Accessor method to get the config file
    public static SuccorStadiumsConfig getConfig() {
        return CONFIG;
    }

    // ── Entry helpers ─────────────────────────────────────────────────────────

    // Helper method to add a header entry
    private static void addHeader(ConfigCategory category, ConfigEntryBuilder entries, String text) {
        category.addEntry(
                entries.startTextDescription(
                        Component.literal(text).withStyle(ChatFormatting.YELLOW)
                ).build()
        );
    }

    // Helper method to add a toggle field entry
    private static void addToggle(ConfigCategory category, ConfigEntryBuilder entries, String name, String tooltip,
                                  boolean value, boolean defaultValue, Consumer<Boolean> save) {
        category.addEntry(
                entries.startBooleanToggle(Component.literal(name), value)
                        .setDefaultValue(defaultValue)
                        .setTooltip(Component.literal(tooltip))
                        .setSaveConsumer(save)
                        .build()
        );
    }

    // Helper method to add a slider field entry
    private static void addSlider(ConfigCategory category, ConfigEntryBuilder entries, String name, String tooltip,
                                  int value, int defaultValue, SuccorStadiumsConfig.Range range,
                                  IntFunction<String> format, Consumer<Integer> save) {
        category.addEntry(
                entries.startIntSlider(Component.literal(name), range.clamp(value), range.min(), range.max())
                        .setDefaultValue(defaultValue)
                        .setTextGetter(current -> Component.literal(format.apply(current)))
                        .setTooltip(Component.literal(tooltip))
                        .setSaveConsumer(save)
                        .build()
        );
    }

    // Helper method to add a int field entry
    private static void addNumber(ConfigCategory category, ConfigEntryBuilder entries, String name, String tooltip,
                                  int value, int defaultValue, SuccorStadiumsConfig.Range range, Consumer<Integer> save) {
        category.addEntry(
                entries.startIntField(Component.literal(name), range.clamp(value))
                        .setDefaultValue(defaultValue)
                        .setMin(range.min())
                        .setMax(range.max())
                        .setTooltip(Component.literal(tooltip))
                        .setSaveConsumer(save)
                        .build()
        );
    }

    // Helper method for our custom color field entry
    private static void addColor(ConfigCategory category, ConfigEntryBuilder entries, String name, String tooltip,
                                 int value, int defaultValue, Consumer<Integer> save) {
        category.addEntry(
                entries.startStrField(Component.literal(name), toHex(value))
                        .setDefaultValue(toHex(defaultValue))
                        .setTooltip(Component.literal(tooltip + " Hex color, like #FF69B4."))
                        .setErrorSupplier(text -> isHexColor(text)
                                ? Optional.empty()
                                : Optional.of(Component.literal("Enter a 6-digit hex color, like #FF69B4")))
                        .setSaveConsumer(text -> {
                            if (isHexColor(text)) save.accept(parseHex(text));
                        })
                        .build()
        );
    }

    // Helper method to return an integer to string hex format
    private static String toHex(int rgb) {
        return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    // Helper method for if a string of text is a hex color
    private static boolean isHexColor(String text) {
        return text != null && text.trim().matches("#?[0-9A-Fa-f]{6}");
    }

    // Helper method to trim the string and remove the # and converts the 6 digits from base 16 to a number
    private static int parseHex(String text) {
        String digits = text.trim();
        if (digits.startsWith("#")) digits = digits.substring(1);
        return Integer.parseInt(digits, 16) & 0xFFFFFF;
    }

    // Helper method that divides a passed value by 100 and prints 2 decimals
    private static String hundredthsOfBlocks(int value) {
        return String.format(Locale.ROOT, "%.2f blocks", value / 100.0);
    }
}