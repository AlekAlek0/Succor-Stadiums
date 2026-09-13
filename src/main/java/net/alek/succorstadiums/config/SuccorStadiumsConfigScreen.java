package net.alek.succorstadiums.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;

public class SuccorStadiumsConfigScreen {

    private static final SuccorStadiumsConfig CONFIG = new SuccorStadiumsConfig();

    public static Screen create(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Succor Stadiums"));

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(
                Component.literal("General")
        );

        general.addEntry(
                entries.startEnumSelector(
                                Component.literal("GUI Theme"),
                                Theme.class,
                                CONFIG.mobArenaTheme
                        )
                        .setDefaultValue(Theme.DARK)
                        .setSaveConsumer(value ->
                                CONFIG.mobArenaTheme = value
                        )
                        .setEnumNameProvider(value ->
                                Component.literal(
                                        ((Theme) value).getDisplayName()
                                )
                        )
                        .build()
        );

        general.addEntry(
                entries.startEnumSelector(
                                Component.literal("Magic Indicator"),
                                MagicIndicatorMode.class,
                                CONFIG.magicIndicatorMode
                        )
                        .setDefaultValue(MagicIndicatorMode.CROSSHAIR)
                        .setSaveConsumer(value ->
                                CONFIG.magicIndicatorMode = value
                        )
                        .setEnumNameProvider(value ->
                                Component.literal(
                                        ((MagicIndicatorMode) value).getDisplayName()
                                )
                        )
                        .build()
        );

        builder.setSavingRunnable(() -> {
            // Save config to disk here later
        });

        return builder.build();
    }

    public static SuccorStadiumsConfig getConfig() {
        return CONFIG;
    }
}