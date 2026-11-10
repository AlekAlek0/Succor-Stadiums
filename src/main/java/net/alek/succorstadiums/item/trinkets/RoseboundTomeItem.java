package net.alek.succorstadiums.item.trinkets;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
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

            // Increment ticks alive and spawn ring at the players pos
            ticksAlive++;
            spawnRing(player.position());

            // If ticks alive is greater than the duration set done to true
            if (ticksAlive >= RING_DURATION_TICKS) done = true;
        }

        // Private helper method to spawn ring in the center of the players feet
        // with the rosebound tome dust particle
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