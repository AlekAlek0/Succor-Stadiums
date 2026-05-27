package net.alek.succorstadiums.client;

import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SuccorStadiumsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {

            // Item Tooltips
            if (stack.is(ModItems.BRENNON_ORE)) {
                tooltip.add(
                        Component.translatable("item.succorstadiums.brennon_ore.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SILVER_INGOT)) {
                tooltip.add(
                        Component.translatable("item.succorstadiums.silver_ingot.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)

                );
            }

            // Weapon Tooltips
            if (stack.is(ModItems.BEAN_POLE)) {
                tooltip.add(
                        Component.translatable("item.succorstadiums.bean_pole.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BONE_DAGGER)) {
                tooltip.add(
                        Component.translatable("item.succorstadiums.bone_dagger.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
            }

            // Food Tooltips
            if (stack.is(ModItems.GRAMBLE_BAPPLE)) {
                tooltip.add(
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_0")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
                tooltip.add(
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_1")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_2")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_3")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_4")
                                .withStyle(ChatFormatting.RED)
                );
                tooltip.add(
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_5")
                                .withStyle(ChatFormatting.RED)
                );
            }
        });
    }
}