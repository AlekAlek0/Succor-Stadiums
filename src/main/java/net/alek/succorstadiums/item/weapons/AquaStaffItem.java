package net.alek.succorstadiums.item.weapons;

import net.alek.succorstadiums.sound.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

public class AquaStaffItem extends Item {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final double RING_RADIUS = 2.5;
    private static final int PARTICLE_COUNT = 48;
    private static final int SLOWNESS_DURATION = 100;
    private static final int SLOW_FALLING_DURATION = 100;
    private static final int SLOWNESS_AMPLIFIER = 0;
    private static final int SLOW_FALLING_AMPLIFIER = 0;
    private static final int COOLDOWN_TICKS = 240;
    private static final int RING_DURATION_TICKS = 60;

    private static final DustParticleOptions AQUA_DUST =
            new DustParticleOptions(0x00FFFF, 1.0f);

    private static final List<ParticleBolt> activeBolts = new ArrayList<>();
    private static final List<StaticRing> activeRings = new ArrayList<>();
    private static boolean tickRegistered = false;

    public AquaStaffItem(Properties properties) {
        super(properties);
        registerTick();
    }

    private static void registerTick() {
        if (tickRegistered) return;
        tickRegistered = true;

        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            Iterator<ParticleBolt> boltIt = activeBolts.iterator();
            while (boltIt.hasNext()) {
                ParticleBolt bolt = boltIt.next();
                bolt.tick();
                if (bolt.isDone()) boltIt.remove();
            }

            Iterator<StaticRing> ringIt = activeRings.iterator();
            while (ringIt.hasNext()) {
                StaticRing ring = ringIt.next();
                ring.tick();
                if (ring.isDone()) ringIt.remove();
            }
        });
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        Vec3 start = user.getEyePosition().add(user.getLookAngle().scale(1.5));
        Vec3 direction = user.getLookAngle().normalize();

        activeBolts.add(new ParticleBolt(serverLevel, start, direction));


        user.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }

    // Class for particle bolt
    private static class ParticleBolt {

        private static final double SPEED = 0.8;
        private static final int MAX_TICKS = 60;

        private final ServerLevel level;
        private final Vec3 direction;
        private Vec3 position;
        private int ticks = 0;
        private boolean done = false;

        ParticleBolt(ServerLevel level, Vec3 start, Vec3 direction) {
            this.level = level;
            this.position = start;
            this.direction = direction;
        }

        void tick() {
            if (done) return;
            ticks++;

            position = position.add(direction.scale(SPEED));

            level.sendParticles(AQUA_DUST,
                    position.x, position.y, position.z,
                    3, 0.05, 0.05, 0.05, 0
            );

            BlockPos blockPos = BlockPos.containing(position);
            BlockState blockState = level.getBlockState(blockPos);

            if (blockState.isSolid() || ticks >= MAX_TICKS) {
                Vec3 landPos = new Vec3(position.x, blockPos.getY() + 1.1, position.z);

                level.sendParticles(AQUA_DUST,
                        landPos.x, landPos.y, landPos.z,
                        30, 0.3, 0.1, 0.3, 0
                );

                level.playSound(null, landPos.x, landPos.y,  landPos.z, ModSounds.AQUA_ROD_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

                AABB area = new AABB(
                        landPos.x - RING_RADIUS, landPos.y - 2, landPos.z - RING_RADIUS,
                        landPos.x + RING_RADIUS, landPos.y + 2, landPos.z + RING_RADIUS
                );

                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> true);

                targets.forEach(entity -> {
                    boolean applied = entity.addEffect(
                            new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER)
                    );
                    boolean applied_2 = entity.addEffect(
                            new MobEffectInstance(MobEffects.SLOW_FALLING, SLOW_FALLING_DURATION, SLOW_FALLING_AMPLIFIER)
                    );
                });

                activeRings.add(new StaticRing(level, landPos));
                done = true;
            }
        }

        boolean isDone() { return done; }
    }

    // Class for particle ring
    private static class StaticRing {

        private final ServerLevel level;
        private final Vec3 center;
        private int ticksAlive = 0;

        StaticRing(ServerLevel level, Vec3 center) {
            this.level = level;
            this.center = center;
        }

        void tick() {
            ticksAlive++;
            spawnRing();

            // Re-apply slowness every 10 ticks to any mob inside the ring
            if (ticksAlive % 10 == 0) {
                AABB area = new AABB(
                        center.x - RING_RADIUS, center.y - 3, center.z - RING_RADIUS,
                        center.x + RING_RADIUS, center.y + 4, center.z + RING_RADIUS
                );
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> true);
                targets.forEach(entity -> {
                    boolean applied = entity.addEffect(
                            new MobEffectInstance(MobEffects.SLOW_FALLING, 40)
                    );
                    boolean applied_2 = entity.addEffect(
                            new MobEffectInstance(MobEffects.SLOWNESS, 40, SLOW_FALLING_AMPLIFIER)
                    );
                });
            }
        }

        private void spawnRing() {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                double angle = (2 * Math.PI / PARTICLE_COUNT) * i;
                double x = center.x + RING_RADIUS * Math.cos(angle);
                double z = center.z + RING_RADIUS * Math.sin(angle);

                level.sendParticles(AQUA_DUST,
                        x, center.y, z,
                        1, 0, 0, 0, 0
                );
            }
        }

        boolean isDone() { return ticksAlive >= RING_DURATION_TICKS; }
    }
}