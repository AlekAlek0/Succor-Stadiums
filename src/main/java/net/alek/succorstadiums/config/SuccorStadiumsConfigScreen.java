package net.alek.succorstadiums.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.alek.succorstadiums.network.item.equipment.pouch.SetPouchAutoDepositPayload;
import net.alek.succorstadiums.item.ModItems;

// SuccorStadiumsConfigScreen class
public class SuccorStadiumsConfigScreen {

    private static final SuccorStadiumsConfig CONFIG = SuccorStadiumsConfigManager.load();

    public static Screen create(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Succor Stadiums"));

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(
                Component.literal("General")
        );

        general.addEntry(
                entries.startTextDescription(
                        Component.literal("TIP: Press \\ by default anytime in-game to open this menu quickly!")
                                .withStyle(ChatFormatting.YELLOW)
                ).build()
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

        ConfigCategory pouchAutoDeposit = builder.getOrCreateCategory(
                Component.literal("Pouch Auto Deposit")
        );

        pouchAutoDeposit.addEntry(
                entries.startBooleanToggle(
                                Component.literal("Plains Coin Pouch: Auto Deposit"),
                                CONFIG.plainsPouchAutoDepositEnabled
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Automatically deposit coins picked up off the ground into your pouch."))
                        .setSaveConsumer(value -> {
                            CONFIG.plainsPouchAutoDepositEnabled = value;
                            ClientPlayNetworking.send(
                                    new SetPouchAutoDepositPayload(
                                            BuiltInRegistries.ITEM.getKey(ModItems.PLAINS_COIN_POUCH),
                                            value
                                    )
                            );
                        })
                        .build()
        );

        builder.setSavingRunnable(() -> SuccorStadiumsConfigManager.save(CONFIG));

        return builder.build();
    }

    public static SuccorStadiumsConfig getConfig() {
        return CONFIG;
    }
}