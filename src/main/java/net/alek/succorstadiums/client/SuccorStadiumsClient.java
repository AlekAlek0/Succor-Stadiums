package net.alek.succorstadiums.client;

import net.alek.succorstadiums.client.render.entity.MashedBananaSlimeRenderer;
import net.alek.succorstadiums.entity.ModEntityTypes;
import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.network.ArachnoDoubleJumpPayload;
import net.alek.succorstadiums.network.OpenMobArenaPayload;
import net.alek.succorstadiums.network.ResurrectionAmuletPayload;
import net.alek.succorstadiums.screen.MobArenaScreen;
import net.alek.succorstadiums.screen.MobArenaScreenHandler;
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

        EntityRenderers.register(
                ModEntityTypes.MASHED_BANANA_SLIME,
                MashedBananaSlimeRenderer::new
        );

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

        ClientPlayNetworking.registerGlobalReceiver(OpenMobArenaPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            Minecraft.getInstance().setScreen(new MobArenaScreen(Component.literal("Mob Arena Manager")));
        }));

        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {

            // Item Tooltips
            if (stack.is(ModItems.BRENNON_ORE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.brennon_ore.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SILVER_INGOT)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.silver_ingot.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.SILK_SPOOL)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.silk_spool.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.SILK_WEAVE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.silk_weave.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)

                );
            }
            if (stack.is(ModItems.BONE_BROTH)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bone_broth.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.SPIDER_CARAPACE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.spider_carapace.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            // Armor Tooltips
            if (stack.is(ModItems.BALE_HELMET)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_helmet.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_CHESTPLATE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_chestplate.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_LEGGINGS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_leggings.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BALE_BOOTS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bale_boots.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_HELMET)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_helmet.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.GRAY)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.spacer")
                                .withStyle(ChatFormatting.BLUE)
                );
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_CHESTPLATE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_chestplate.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.GRAY)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.spacer")
                                .withStyle(ChatFormatting.BLUE)
                );
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_LEGGINGS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_leggings.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.GRAY)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.spacer")
                                .withStyle(ChatFormatting.BLUE)
                );
            }
            if (stack.is(ModItems.ARACHNO_CARAPACE_BOOTS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_carapace_boots.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_0")
                                .withStyle(ChatFormatting.GRAY)
                );

                tooltip.add(4,
                        Component.translatable("item.succorstadiums.arachno_carapace_armor.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.spacer")
                                .withStyle(ChatFormatting.BLUE)
                );
            }
            if (stack.is(ModItems.NANNER_WATER_WADERS)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.nanner_water_waders.tooltip_0")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(4,
                        Component.translatable("item.succorstadiums.nanner_water_waders.tooltip_1")
                                .withStyle(ChatFormatting.BLUE)
                );
            }

            // Weapon Tooltips
            if (stack.is(ModItems.BEAN_POLE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bean_pole.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BONE_DAGGER)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bone_dagger.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BANANNER_BLADE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bananner_blade.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }

            if (stack.is(ModItems.FUMBLEBRINGER_FORK)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.fumblebringer_fork.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }

            if (stack.is(ModItems.AQUA_STAFF)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.aqua_staff.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.FIRE_STAFF)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.fire_staff.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.BOWNANA)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.bownana.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.ARACHNO_CROSSBOW)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.arachno_crossbow.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }

            // Food Tooltips
            if (stack.is(ModItems.GRAMBLE_BAPPLE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_0")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(2,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_1")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(3,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_2")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(4,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_3")
                                .withStyle(ChatFormatting.GREEN)
                );
                tooltip.add(5,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_4")
                                .withStyle(ChatFormatting.RED)
                );
                tooltip.add(6,
                        Component.translatable("item.succorstadiums.ghramble_bapple.tooltip_5")
                                .withStyle(ChatFormatting.RED)
                );
            }

            if (stack.is(ModItems.ROTTEN_STEW)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.rotten_stew.tooltip_0")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
                tooltip.add(2,
                        Component.translatable("item.succorstadiums.rotten_stew.tooltip_1").withStyle(ChatFormatting.RED)
                );
            }


            // Trinket Tooltips
            if (stack.is(ModItems.FLINT_CHARM)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.flint_charm.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.RESURRECTION_AMULET)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.resurrection_amulet.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
            if (stack.is(ModItems.DOG_WHISTLE)) {
                tooltip.add(1,
                        Component.translatable("item.succorstadiums.dog_whistle.tooltip")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            }
        });
    }

    private static void handleArachnoDoubleJumpInput(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.options == null) return;

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

        Vec3 velocity = player.getDeltaMovement();
        double jumpBoost = 0.55D - velocity.y;
        player.push(0.0D, jumpBoost, 0.0D);
        player.fallDistance = 0.0F;
        spawnArachnoDoubleJumpClouds(player);
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
