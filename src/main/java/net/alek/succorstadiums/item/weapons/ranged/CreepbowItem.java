package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.NonNull;
import java.util.List;

public class CreepbowItem extends BowItem {

    // Vanilla velocity is 3.0F
    public static final float VELOCITY_MULTIPLIER = 0.80F;

    private static final int FULL_CHARGE_TICKS = 20;
    private static final int HOLD_AFTER_FULL_CHARGE_TICKS = 60;
    private static final int TRIGGER_TICK = FULL_CHARGE_TICKS + HOLD_AFTER_FULL_CHARGE_TICKS;

    private static final double BLAST_RADIUS = 3.0;
    private static final float MOB_DAMAGE = 3.0F;
    private static final double KNOCKBACK_MULTIPLIER = 1.5;

    public CreepbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(final @NonNull ItemStack itemStack, final @NonNull Level level, final @NonNull LivingEntity entity, final int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        ItemStack projectile = player.getProjectile(itemStack);
        if (projectile.isEmpty()) {
            return false;
        }

        int timeHeld = this.getUseDuration(itemStack, entity) - remainingTime;
        float pow = getPowerForTime(timeHeld);
        if (pow < 0.1F) {
            return false;
        }

        List<ItemStack> firedProjectiles = draw(itemStack, projectile, player);
        if (level instanceof ServerLevel serverLevel && !firedProjectiles.isEmpty()) {
            this.shoot(serverLevel, player, player.getUsedItemHand(), itemStack, firedProjectiles, pow * VELOCITY_MULTIPLIER, 1.0F, pow == 1.0F, null);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + pow * 0.5F);
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        return true;
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity livingEntity, @NonNull ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);

        if (!(livingEntity instanceof Player)) {
            return;
        }

        int useDuration = getUseDuration(stack, livingEntity);
        int ticksInUse = useDuration - remainingUseDuration;

        if (ticksInUse == TRIGGER_TICK) {
            if (!level.isClientSide()) {
                this.releaseUsing(stack, level, livingEntity, 0);
            }
            detonate(level, livingEntity);
        }
    }

    private void detonate(Level level, LivingEntity livingEntity) {
        if (level.isClientSide()) {
            return;
        }

        Vec3 center = livingEntity.position().add(0, 1.0, 0);

        if (level instanceof ServerLevel serverLevel) {
            for (int explosion_count = 0; explosion_count < 3; explosion_count++) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0
                );
            }
            applyMobOnlyExplosion(serverLevel, livingEntity, center);
        }

        level.playSound(
                null,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        livingEntity.stopUsingItem();
    }

    private void applyMobOnlyExplosion(ServerLevel level, LivingEntity source, Vec3 center) {
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

            mob.hurtServer(level, level.damageSources().explosion(null, source), MOB_DAMAGE);
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