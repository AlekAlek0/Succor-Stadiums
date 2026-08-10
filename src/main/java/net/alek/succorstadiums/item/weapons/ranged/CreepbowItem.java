package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class CreepbowItem extends BowItem {

    // Vanilla full draw is reached at 20 ticks of use
    private static final int FULL_CHARGE_TICKS = 20;

    // How long it must stay fully charged before detonating: 3 seconds = 60 ticks.
    private static final int HOLD_AFTER_FULL_CHARGE_TICKS = 60;
    private static final int TRIGGER_TICK = FULL_CHARGE_TICKS + HOLD_AFTER_FULL_CHARGE_TICKS;

    // Explosion tuning
    private static final double BLAST_RADIUS = 3.0;
    private static final float MOB_DAMAGE = 3.0F; // 1.5 hearts (1 heart = 2 damage)
    private static final double KNOCKBACK_MULTIPLIER = 1.5;

    public CreepbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity livingEntity, @NonNull ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);

        int useDuration = getUseDuration(stack, livingEntity);
        int ticksInUse = useDuration - remainingUseDuration;

        if (ticksInUse == TRIGGER_TICK) {
            detonate(level, livingEntity);
        }
    }

    private void detonate(Level level, LivingEntity livingEntity) {
        if (level.isClientSide()) {
            return;
        }

        Vec3 center = livingEntity.position().add(0, 1.0, 0);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    center.x, center.y, center.z,
                    1, 0.0, 0.0, 0.0, 0.0
            );
        }

        level.playSound(
                null,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        applyMobOnlyExplosion(level, livingEntity, center);

        livingEntity.stopUsingItem();
    }

    private void applyMobOnlyExplosion(Level level, LivingEntity source, Vec3 center) {
        AABB affectedArea = new AABB(
                center.x - BLAST_RADIUS, center.y - BLAST_RADIUS, center.z - BLAST_RADIUS,
                center.x + BLAST_RADIUS, center.y + BLAST_RADIUS, center.z + BLAST_RADIUS
        );

        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, affectedArea, Mob::isAlive);

        for (Mob mob : mobs) {
            Vec3 mobCenter = mob.position().add(0, mob.getBbHeight() / 2.0, 0);
            double distance = mobCenter.distanceTo(center);
            if (distance > BLAST_RADIUS) {
                continue;
            }

            mob.hurt(level.damageSources().explosion(null, source), MOB_DAMAGE);
            applyKnockback(mob, center, mobCenter, distance);
        }
    }

    private void applyKnockback(Mob mob, Vec3 center, Vec3 mobCenter, double distance) {
        Vec3 direction = mobCenter.subtract(center);
        direction = direction.lengthSqr() < 1.0E-4
                ? new Vec3(0, 1, 0)
                : direction.normalize();

        double falloff = 1.0 - (distance / BLAST_RADIUS);
        double strength = falloff * KNOCKBACK_MULTIPLIER;

        mob.setDeltaMovement(mob.getDeltaMovement().add(direction.scale(strength)));
        mob.hurtMarked = true;
    }
}