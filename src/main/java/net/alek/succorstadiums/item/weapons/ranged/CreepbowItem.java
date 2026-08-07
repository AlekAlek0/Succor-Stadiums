package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class CreepbowItem extends BowItem {

    // Vanilla full draw is reached at 20 ticks of use
    private static final int FULL_CHARGE_TICKS = 20;

    // How long it must stay fully charged before detonating: 3 seconds = 60 ticks.
    private static final int HOLD_AFTER_FULL_CHARGE_TICKS = 60;
    private static final int TRIGGER_TICK = FULL_CHARGE_TICKS + HOLD_AFTER_FULL_CHARGE_TICKS;

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

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    livingEntity.getX(), livingEntity.getY() + 1.0, livingEntity.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0
            );
        }

        level.playSound(
                null,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        livingEntity.hurt(level.damageSources().explosion(null, livingEntity), 0.3F);

        livingEntity.stopUsingItem();
    }
}