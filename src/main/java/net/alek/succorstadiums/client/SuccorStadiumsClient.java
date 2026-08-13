package net.alek.succorstadiums.client;

import net.alek.succorstadiums.client.render.entity.monsters.BananaSlimeRenderer;
import net.alek.succorstadiums.client.render.entity.monsters.GrassCreeperRenderer;
import net.alek.succorstadiums.client.render.entity.monsters.SkelcrowRenderer;
import net.alek.succorstadiums.client.render.entity.monsters.FarmbieRenderer;
import net.alek.succorstadiums.entity.ModEntityTypes;
import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.network.arena.OpenMobArenaPayload;
import net.alek.succorstadiums.network.item.armor.ArachnoDoubleJumpPayload;
import net.alek.succorstadiums.network.item.armor.ArachnoDoubleJumpResultPayload;
import net.alek.succorstadiums.network.item.trinkets.ResurrectionAmuletPayload;
import net.alek.succorstadiums.screen.mobarenagui.MobArenaScreen;
import net.alek.succorstadiums.screen.mobarenagui.MobArenaScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SuccorStadiumsClient implements ClientModInitializer {
    private static boolean canDoubleJump;
    private static boolean hasReleasedJumpKey;

    @Override
    public void onInitializeClient() {

        ModKeyBindings.registerKeyBindings();
        ScreenCloseHandler.register();
        KeyInputHandler.register();

        EntityRenderers.register(ModEntityTypes.BANANA_SLIME, BananaSlimeRenderer::new);
        EntityRenderers.register(ModEntityTypes.FARMBIE, FarmbieRenderer::new);
        EntityRenderers.register(ModEntityTypes.GRASS_CREEPER, GrassCreeperRenderer::new);
        EntityRenderers.register(ModEntityTypes.SKELCROW, SkelcrowRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(SuccorStadiumsClient::handleArachnoDoubleJumpInput);

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

        ClientPlayNetworking.registerGlobalReceiver(OpenMobArenaPayload.TYPE, (payload, context) -> context.client().execute(() ->
                Minecraft.getInstance().gui.setScreen(new MobArenaScreen(Component.literal("Mob Arena Manager")))));

        ClientPlayNetworking.registerGlobalReceiver(ArachnoDoubleJumpResultPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            if (!payload.success()) return;

            LocalPlayer player = context.client().player;
            if (player == null) return;

            Vec3 velocity = player.getDeltaMovement();
            double jumpBoost = 0.55D - velocity.y;
            player.push(0.0D, jumpBoost, 0.0D);
            player.fallDistance = 0.0F;
            spawnArachnoDoubleJumpClouds(player);
        }));

        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {

            // Item Tooltips
            if (stack.is(ModItems.BRENNON_ORE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.brennon_ore.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SILVER_INGOT)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.silver_ingot.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.SILK_SPOOL)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.silk_spool.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.SILK_WEAVE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.silk_weave.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.BONE_BROTH)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bone_broth.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SPIDER_CARAPACE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.spider_carapace.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SPIDER_LEG)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.spider_leg.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BANANA_SLIME_BALL)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.banana_slime_ball.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BANANA_BRANCH)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.banana_branch.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            // Armor Tooltips
            if (stack.is(ModItems.BALE_HELMET)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_helmet.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_CHESTPLATE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_chestplate.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_LEGGINGS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_leggings.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_BOOTS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_boots.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_HELMET)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_helmet.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.DARK_PURPLE)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_2")
                                .withStyle(ChatFormatting.GRAY)
                );
                tooltip.add(6, Component.translatable("item.succorstadiums.spacer"));
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_CHESTPLATE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_chestplate.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.DARK_PURPLE)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_2")
                                .withStyle(ChatFormatting.GRAY)
                );
                tooltip.add(6, Component.translatable("item.succorstadiums.spacer"));
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_LEGGINGS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_leggings.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.DARK_PURPLE)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_2")
                                .withStyle(ChatFormatting.GRAY)
                );
                tooltip.add(6, Component.translatable("item.succorstadiums.spacer"));
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_BOOTS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_boots.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.DARK_PURPLE)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_2")
                                .withStyle(ChatFormatting.GRAY)
                );
                tooltip.add(6, Component.translatable("item.succorstadiums.spacer"));
            }
            if (stack.is(ModItems.NANNER_WATER_WADERS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.nanner_water_waders.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(4,
                        Component.translatable("item.succorstadiums.nanner_water_waders.tooltip")
                                .withStyle(ChatFormatting.BLUE)
                );
            }

            // Weapon Tooltips
            if (stack.is(ModItems.BEAN_POLE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bean_pole.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BONE_DAGGER)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bone_dagger.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BANANNER_BLADE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bananner_blade.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }

            if (stack.is(ModItems.FUMBLEBRINGER_FORK)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.fumblebringer_fork.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SWORD_OF_THE_FOREST)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.sword_of_the_forest.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.FIRE_STAFF)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.fire_staff.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(2, Component.translatable("item.succorstadiums.spacer"));
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.fire_staff.tooltip_0")
                                .withStyle(ChatFormatting.GRAY)
                );
                tooltip.add(4,
                        Component.translatable("item.succorstadiums.fire_staff.tooltip_1")
                                .withStyle(ChatFormatting.AQUA)
                );
            }
            if (stack.is(ModItems.AQUA_STAFF)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.aqua_staff.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(2, Component.translatable("item.succorstadiums.spacer"));
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.aqua_staff.tooltip_0")
                                .withStyle(ChatFormatting.GRAY)
                );
                tooltip.add(4,
                        Component.translatable("item.succorstadiums.aqua_staff.tooltip_1")
                                .withStyle(ChatFormatting.AQUA)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.aqua_staff.tooltip_2")
                                .withStyle(ChatFormatting.AQUA)
                );
            }
            if (stack.is(ModItems.BOWNANA)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bownana.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.ARACHNO_CROSSBOW)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_crossbow.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }

            // Food Tooltips
            if (stack.is(ModItems.GRAMBLE_BAPPLE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.ghramble_bapple.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(2,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_0")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_1")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(4,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_2")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_3")
                                .withStyle(ChatFormatting.RED)
                );
                tooltip.add(6,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_4")
                                .withStyle(ChatFormatting.RED)
                );
            }
            if (stack.is(ModItems.PLANT_POWDER)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.plant_powder.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.CHICKEN_STEW)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.chicken_stew.tooltip").withStyle(ChatFormatting.RED)
                );
            }
            if (stack.is(ModItems.ROTTEN_STEW)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.rotten_stew.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(2,
                        Component.translatable("item.succorstadiums.rotten_stew.tooltip").withStyle(ChatFormatting.RED)
                );
            }


            // Trinket Tooltips
            if (stack.is(ModItems.FLINT_CHARM)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.flint_charm.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.RESURRECTION_AMULET)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.resurrection_amulet.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.DOG_WHISTLE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.dog_whistle.lore")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(2, Component.translatable("item.succorstadiums.spacer"));
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.dog_whistle.tooltip_0")
                                .withStyle(ChatFormatting.GRAY)
                );
                tooltip.add(4,
                        Component.translatable("item.succorstadiums.dog_whistle.tooltip_1")
                                .withStyle(ChatFormatting.AQUA)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.dog_whistle.tooltip_2")
                                .withStyle(ChatFormatting.AQUA)
                );
                tooltip.add(6,
                        Component.translatable("item.succorstadiums.dog_whistle.tooltip_3")
                                .withStyle(ChatFormatting.RED)
                );
                tooltip.add(7,
                        Component.translatable("item.succorstadiums.dog_whistle.tooltip_4")
                                .withStyle(ChatFormatting.RED)
                );
            }
        });
    }

    private static void handleArachnoDoubleJumpInput(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (isDoubleJumpBlocked(player)) {
            canDoubleJump = false;
            hasReleasedJumpKey = false;
            return;
        }

        if (player.onGround()) {
            hasReleasedJumpKey = false;
            canDoubleJump = true;
            return;
        }

        boolean jumpPressed = client.options.keyJump.isDown();
        if (!jumpPressed) {
            hasReleasedJumpKey = true;
            return;
        }

        if (player.getAbilities().flying || !canDoubleJump || !hasReleasedJumpKey) return;
        if (!isWearingFullArachnoSet(player)) return;

        canDoubleJump = false;
        ClientPlayNetworking.send(new ArachnoDoubleJumpPayload());
    }

    private static boolean isWearingFullArachnoSet(LocalPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ARACHNO_CARAPACE_HELMET)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ARACHNO_CARAPACE_CHESTPLATE)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ARACHNO_CARAPACE_LEGGINGS)
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ARACHNO_CARAPACE_BOOTS);
    }

    private static boolean isDoubleJumpBlocked(LocalPlayer player) {
        return player.isSwimming()
                || player.isInWater()
                || player.isInLava()
                || player.onClimbable()
                || player.getAbilities().flying;
    }

    private static void spawnArachnoDoubleJumpClouds(LocalPlayer player) {
        for (int i = 0; i < 18; i++) {
            double angle = Math.random() * Math.PI * 2.0D;
            double radius = 0.15D + Math.random() * 0.45D;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            player.level().addParticle(
                    ParticleTypes.CLOUD,
                    player.getX() + xOffset,
                    player.getY() + 0.1D + Math.random() * 0.25D,
                    player.getZ() + zOffset,
                    xOffset * 0.08D,
                    -0.02D + Math.random() * 0.04D,
                    zOffset * 0.08D
            );
        }
    }
}