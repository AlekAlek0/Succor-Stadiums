package net.alek.succorstadiums.entity.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import net.alek.succorstadiums.item.ModItems;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RazorThornEntity extends AbstractArrow implements ItemSupplier {

    private static final float DAMAGE = 1f;

    @Nullable
    private Direction stuckFace;

    public RazorThornEntity(EntityType<? extends RazorThornEntity> type, Level level) {
        super(type, level);
        this.setBaseDamage(DAMAGE);
        this.setPickupItemStack(this.getDefaultPickupItem());
    }

    public RazorThornEntity(EntityType<? extends RazorThornEntity> type, LivingEntity owner, Level level, ItemStack stack) {
        super(type, owner, level, stack, null);
        this.setBaseDamage(DAMAGE);
        this.setPickupItemStack(this.getDefaultPickupItem());
    }

    public boolean isPlantedInGround() {
        return this.isInGround() && this.stuckFace == Direction.UP;
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult result) {
        super.onHitBlock(result);
        this.stuckFace = result.getDirection();
    }

    @Override
    protected @NonNull ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.RAZOR_THORN);
    }

    @Override
    protected @NonNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.CROSSBOW_HIT;
    }

    @Override
    public @NonNull ItemStack getItem() {
        return getDefaultPickupItem();
    }
}