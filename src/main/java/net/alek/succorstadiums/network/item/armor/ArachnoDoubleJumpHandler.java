package net.alek.succorstadiums.network.item.armor;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.alek.succorstadiums.mana.ManaHelper;
import net.alek.succorstadiums.item.ModItems;

public class ArachnoDoubleJumpHandler {
    private static final Set<UUID> USED_DOUBLE_JUMP = new HashSet<>();
    private static final int MANA_COST = 4; // 2 Stars

    private static final Component NOT_ENOUGH_MANA_MESSAGE =
            Component.translatable("message.succorstadiums.not_enough_mana");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArachnoDoubleJumpPayload.TYPE, (payload, context) -> context.server().execute(() -> tryDoubleJump(context.player())));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.onGround() || isDoubleJumpBlocked(player)) {
                    USED_DOUBLE_JUMP.remove(player.getUUID());
                }
            }
        });
    }

    private static void tryDoubleJump(ServerPlayer player) {
        if (!isWearingFullArachnoSet(player)) return;
        if (player.onGround() || isDoubleJumpBlocked(player)) return;
        if (USED_DOUBLE_JUMP.contains(player.getUUID())) return;

        // Check if player has enough mana to double jump if not send success false to jump result payload and return
        if (!ManaHelper.consumeMana(player, MANA_COST)) {
            player.sendOverlayMessage(NOT_ENOUGH_MANA_MESSAGE);
            ServerPlayNetworking.send(player, new ArachnoDoubleJumpResultPayload(false));
            return;
        }

        USED_DOUBLE_JUMP.add(player.getUUID());

        Vec3 velocity = player.getDeltaMovement();
        double jumpBoost = 0.55D - velocity.y;
        player.push(0.0D, jumpBoost, 0.0D);
        player.fallDistance = 0.0F;
        spawnArachnoDoubleJumpClouds(player);

        ServerPlayNetworking.send(player, new ArachnoDoubleJumpResultPayload(true));
    }

    private static boolean isWearingFullArachnoSet(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ARACHNO_CARAPACE_HELMET)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ARACHNO_CARAPACE_CHESTPLATE)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ARACHNO_CARAPACE_LEGGINGS)
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ARACHNO_CARAPACE_BOOTS);
    }

    private static boolean isDoubleJumpBlocked(ServerPlayer player) {
        return player.isSwimming()
                || player.isInWater()
                || player.isInLava()
                || player.onClimbable()
                || player.getAbilities().flying;
    }

    private static void spawnArachnoDoubleJumpClouds(ServerPlayer player) {
        ServerLevel level = player.level();
        level.sendParticles(
                ParticleTypes.CLOUD,
                player.getX(),
                player.getY() + 0.2D,
                player.getZ(),
                18,
                0.45D,
                0.15D,
                0.45D,
                0.03D
        );
    }
}