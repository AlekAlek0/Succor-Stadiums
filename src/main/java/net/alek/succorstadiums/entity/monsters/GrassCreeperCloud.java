package net.alek.succorstadiums.entity.monsters;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GrassCreeperCloud extends AreaEffectCloud {
    private static final DustParticleOptions DIRT_DUST = new DustParticleOptions(0x8B5A2B, 1.3F);
    private int damageCooldown = 0;

    public GrassCreeperCloud(Level level, double x, double y, double z) {
        super(level, x, y, z);
        this.setRadius((float) GrassCreeper.ATTACK_RADIUS);
        this.setRadiusPerTick(0.0F);
        this.setDuration(GrassCreeper.RING_DURATION_TICKS);
        this.setWaitTime(0);
        this.setCustomParticle(DIRT_DUST);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        this.damageCooldown--;
        if (this.damageCooldown <= 0) {
            this.damageCooldown = 20;
            ServerLevel serverLevel = (ServerLevel) this.level();
            DamageSource magic = this.damageSources().magic();
            for (Player player : serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(this.getRadius()))) {
                if (this.distanceTo(player) <= this.getRadius()) {
                    player.hurtServer(serverLevel, magic, 0.75F);
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
                }
            }
        }
    }
}