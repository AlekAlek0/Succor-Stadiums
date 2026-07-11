package net.alek.succorstadiums.network;

import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArachnoDoubleJumpHandler {
    private static final Set<UUID> USED_DOUBLE_JUMP = new HashSet<>();

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArachnoDoubleJumpPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> tryDoubleJump(context.player()));
        });

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
        if (!USED_DOUBLE_JUMP.add(player.getUUID())) return;

        Vec3 velocity = player.getDeltaMovement();
        double jumpBoost = 0.55D - velocity.y;
        player.push(0.0D, jumpBoost, 0.0D);
        player.fallDistance = 0.0F;
        spawnArachnoDoubleJumpClouds(player);
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
