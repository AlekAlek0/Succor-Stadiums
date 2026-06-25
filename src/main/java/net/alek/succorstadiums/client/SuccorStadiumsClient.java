package net.alek.succorstadiums.client;

import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.network.OpenMobArenaPayload;
import net.alek.succorstadiums.network.ResurrectionAmuletPayload;
import net.alek.succorstadiums.screen.MobArenaScreen;
import net.alek.succorstadiums.screen.MobArenaScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SuccorStadiumsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        MobArenaScreenHandler.register();

        ClientPlayNetworking.registerGlobalReceiver(ResurrectionAmuletPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            ItemStack stack = new ItemStack(ModItems.RESURRECTION_AMULET);
            context.client().gameRenderer.displayItemActivation(stack);

            var level = context.client().level;
            var player = context.client().player;
            if (level == null || player == null) return;

            for (int i = 0; i < 100; i++) {
                level.addParticle(
                        new DustParticleOptions(0xC1B9AE, 2.0f),
                        player.getX() + (Math.random() - 0.5) * 2,
                        player.getY() + Math.random() * 2,
                        player.getZ() + (Math.random() - 0.5) * 2,
                        (Math.random() - 0.5) * 0.5,
                        Math.random() * 0.5,
                        (Math.random() - 0.5) * 0.5
                );
            }
        }));

        ClientPlayNetworking.registerGlobalReceiver(OpenMobArenaPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            Minecraft.getInstance().setScreen(new MobArenaScreen(Component.literal("Mob Arena Manager")));
        }));

        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {

            // Item Tooltips
            if (stack.is(ModItems.BRENNON_ORE)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.brennon_ore.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SILVER_INGOT)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.silver_ingot.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.SILK_SPOOL)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.silk_spool.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.SILK_WEAVE)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.silk_weave.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.BONE_BROTH)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bone_broth.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SPIDER_SHELL_FRAGMENT)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.spider_shell_fragment.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            // Armor Tooltips
            if (stack.is(ModItems.BALE_HELMET)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bale_helmet.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_CHESTPLATE)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bale_chestplate.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_LEGGINGS)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bale_leggings.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_BOOTS)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bale_boots.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.NANNER_WATER_WADERS)) {
                tooltip.add(Component.translatable("item.succorstadiums.nanner_water_waders.tooltip_0")
                        .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
                tooltip.add(Component.translatable("item.succorstadiums.nanner_water_waders.tooltip_1")
                        .withStyle(ChatFormatting.BLUE)
                );
            }

            // Weapon Tooltips
            if (stack.is(ModItems.BEAN_POLE)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bean_pole.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BONE_DAGGER)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bone_dagger.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BANANNER_BLADE)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bananner_blade.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }

            if (stack.is(ModItems.FUMBLEBRINGER_FORK)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.fumblebringer_fork.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }

            if (stack.is(ModItems.AQUA_STAFF)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.aqua_staff.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.FIRE_STAFF)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.fire_staff.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BOWNANA)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.bownana.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }

            // Food Tooltips
            if (stack.is(ModItems.GRAMBLE_BAPPLE)) {
                tooltip.add(
                        1,
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

            if (stack.is(ModItems.ROTTEN_STEW)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.rotten_stew.tooltip_0")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
                tooltip.add(
                        2,
                        Component.translatable("item.succorstadiums.rotten_stew.tooltip_1")
                                .withStyle(ChatFormatting.RED)
                );
            }


            // Trinket Tooltips
            if (stack.is(ModItems.FLINT_CHARM)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.flint_charm.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.RESURRECTION_AMULET)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.resurrection_amulet.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.DOG_WHISTLE)) {
                tooltip.add(
                        1,
                        Component.translatable("item.succorstadiums.dog_whistle.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY,
                                        ChatFormatting.ITALIC)
                );
            }

        });
    }
}