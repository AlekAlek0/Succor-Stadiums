package net.alek.succorstadiums.entity.monsters;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GrassCreeperGreenRingCloud extends AreaEffectCloud {

    private static final DustParticleOptions GREEN_DUST = new DustParticleOptions(0x2E8B57, 1.3F);

    private static final float INNER_RADIUS = (float) GrassCreeper.ATTACK_RADIUS; // brown boundary — exclusive
    private static final float OUTER_RADIUS = GrassCreeper.GREEN_RING_OUTER_RADIUS;
    private static final float RING_RADIUS = (INNER_RADIUS + OUTER_RADIUS) / 2.0F;
    private static final float RING_JITTER = (OUTER_RADIUS - INNER_RADIUS) / 2.0F; // keeps jitter inside the band

    private static final int PARTICLE_COUNT = 40; // more points per pass = denser ring
    private static final int PARTICLES_PER_POINT = 2; // extra spread at each angle
    private static final int RING_PARTICLE_INTERVAL_TICKS = 3;

    private int lifeTicks = 0;
    private int damageCooldown = 0;
    private int ringParticleCooldown = 0;

    public GrassCreeperGreenRingCloud(Level level, double x, double y, double z) {
        super(level, x, y, z);
        this.setRadius(0.01F);
        this.setRadiusPerTick(0.0F);
        this.setDuration(GrassCreeper.RING_DURATION_TICKS);
        this.setWaitTime(0);
        this.setCustomParticle(GREEN_DUST);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) return;

        this.lifeTicks++;
        if (this.lifeTicks >= GrassCreeper.RING_DURATION_TICKS) {
            this.discard();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        this.ringParticleCooldown--;
        if (this.ringParticleCooldown <= 0) {
            this.ringParticleCooldown = RING_PARTICLE_INTERVAL_TICKS;
            spawnRing(serverLevel);
        }

        this.damageCooldown--;
        if (this.damageCooldown <= 0) {
            this.damageCooldown = 20;
            DamageSource magic = this.damageSources().magic();
            for (Player player : serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(OUTER_RADIUS))) {
                double dist = this.distanceTo(player);
                if (dist > INNER_RADIUS && dist <= OUTER_RADIUS) {
                    player.hurtServer(serverLevel, magic, GrassCreeper.GREEN_CLOUD_DAMAGE);
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,
                            GrassCreeper.RING_DURATION_TICKS, 0, false, true));
                }
            }
        }
    }

    private void spawnRing(ServerLevel level) {
        Vec3 center = this.position();

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = (2 * Math.PI / PARTICLE_COUNT) * i;
            double baseX = Math.cos(angle);
            double baseZ = Math.sin(angle);

            for (int j = 0; j < PARTICLES_PER_POINT; j++) {
                // jitter the radius within the band so the ring reads as a
                // filled annulus rather than one thin line of dots
                double jitteredRadius = RING_RADIUS + (level.getRandom().nextDouble() - 0.5) * RING_JITTER;
                double x = center.x + jitteredRadius * baseX;
                double z = center.z + jitteredRadius * baseZ;
                double y = center.y + 0.05 + level.getRandom().nextDouble() * 0.3;

                level.sendParticles(GREEN_DUST, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}