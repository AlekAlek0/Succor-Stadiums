package net.alek.succorstadiums.item.trinkets;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.alek.succorstadiums.sound.ModSounds;

// RoseboundTomeItem class
public class RoseboundTomeItem extends Item {
    public RoseboundTomeItem(Properties properties) {
        super(properties);
        registerTick();
    }

    // Constants for rosebound tome
    private static final int COOLDOWN_TICKS = 20 * 90; // 90 seconds
    private static final int RING_DURATION_TICKS = 20 * 20; // 20 seconds
    private static final int PARTICLE_COUNT = 72;
    private static final double ROSEBOUND_TOME_RING_RADIUS = 3.75;
    private static final int REGEN_DURATION = 20 * 30; // 30 seconds
    private static final int REGEN_AMPLIFIER = 0; // Regeneration I
    private static final int REGEN_CHECK_INTERVAL = 10; // ticks between ring occupancy checks

    // Create the rosebound tome dust particle
    private static final DustParticleOptions ROSEBOUND_TOME_DUST =
            new DustParticleOptions(0xFF1313, 1.0f);

    // Create an array list of active rings and if ticks are registered
    private static final List<FollowingRing> activeRings = new ArrayList<>();
    private static boolean tickRegistered = false;

    // If ticks are already registered return else register the ticks and set it to true
    private static void registerTick() {
        if (tickRegistered) return;
        tickRegistered = true;

        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            Iterator<FollowingRing> it = activeRings.iterator();
            while (it.hasNext()) {
                FollowingRing ring = it.next();
                ring.tick();
                if (ring.isDone()) it.remove();
            }
        });
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand hand) {

        // Check to see if level is client sided if true return a pass value for the interaction result
        if (level.isClientSide()) return InteractionResult.PASS;

        // Check to see if level is a server level if false return a pass value for the interaction result
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        // Check to see if player is a server player if false return a pass value for the interaction result
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        // Replace any ring this player already has so they never stack
        activeRings.removeIf(ring -> ring.player == serverPlayer);
        activeRings.add(new FollowingRing(serverLevel, serverPlayer));

        // Set cooldown, play a sound effect and return a success value for the interaction result
        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ROSEBOUND_TOME_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    // Class for a particle ring that follows the players feet
    private static class FollowingRing {

        // Constants for level, player, ticks alive and done
        private final ServerLevel level;
        private final ServerPlayer player;
        private int ticksAlive = 0;
        private boolean done = false;

        FollowingRing(ServerLevel level, ServerPlayer player) {
            this.level = level;
            this.player = player;
        }

        void tick() {

            // If done is true return
            if (done) return;

            // Stop if the player died, disconnected or changed dimension
            if (!player.isAlive() || player.isRemoved() || player.level() != level) {
                done = true;
                return;
            }

            // Increment ticks alive get the center as a 3d vector and spawn ring at the players pos
            ticksAlive++;
            Vec3 center = player.position();
            spawnRing(center);

            // Only check ring occupancy every regen check interval ticks instead of every tick
            if (ticksAlive % REGEN_CHECK_INTERVAL == 0) {
                applyRegen(center);
            }

            // If ticks alive is greater than the duration set done to true
            if (ticksAlive >= RING_DURATION_TICKS) done = true;
        }

        // Gives regeneration 1 for 30 seconds to every player standing inside the ring
        private void applyRegen(Vec3 center) {
            AABB area = new AABB(
                    center.x - ROSEBOUND_TOME_RING_RADIUS, center.y - 1, center.z - ROSEBOUND_TOME_RING_RADIUS,
                    center.x + ROSEBOUND_TOME_RING_RADIUS, center.y + 3, center.z + ROSEBOUND_TOME_RING_RADIUS
            );

            double radiusSqr = ROSEBOUND_TOME_RING_RADIUS * ROSEBOUND_TOME_RING_RADIUS;

            for (Player target : level.getEntitiesOfClass(Player.class, area, LivingEntity::isAlive)) {
                double dx = target.getX() - center.x;
                double dz = target.getZ() - center.z;
                if (dx * dx + dz * dz > radiusSqr) continue; // AABB is square, ring is round

                // Only apply when they don't already have it, so the timer isn't reset every check
                if (!target.hasEffect(MobEffects.REGENERATION)) {
                    target.addEffect(new MobEffectInstance(
                            MobEffects.REGENERATION,
                            REGEN_DURATION,
                            REGEN_AMPLIFIER
                    ));
                }
            }
        }

        private void spawnRing(Vec3 center) {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                double angle = (2 * Math.PI / PARTICLE_COUNT) * i;
                double x = center.x + ROSEBOUND_TOME_RING_RADIUS * Math.cos(angle);
                double z = center.z + ROSEBOUND_TOME_RING_RADIUS * Math.sin(angle);

                level.sendParticles(ROSEBOUND_TOME_DUST,
                        x, center.y + 0.1, z,
                        1, 0, 0, 0, 0
                );
            }
        }

        // Accessor method to return if done is true or false
        boolean isDone() { return done; }
    }
}