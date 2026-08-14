package net.alek.succorstadiums.entity.monsters;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import net.alek.succorstadiums.item.weapons.ranged.CreepbowItem;
import net.alek.succorstadiums.entity.ai.CreepbowAttackGoal;
import net.alek.succorstadiums.item.ModItems;

import org.jspecify.annotations.NonNull;

public class Skelcrow extends Stray {

    private final CreepbowAttackGoal<Skelcrow> creepbowGoal =
            new CreepbowAttackGoal<>(this, 1.0D, 20, 15.0F);

    public Skelcrow(EntityType<? extends Stray> type, Level level) {
        super(type, level);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CREEPBOW));
    }

    @Override
    public void reassessWeaponGoal() {
        if (!this.level().isClientSide()) {
            ItemStack mainHand = this.getMainHandItem();

            if (mainHand.getItem() instanceof CreepbowItem) {
                this.goalSelector.removeAllGoals(goal ->
                        goal instanceof MeleeAttackGoal
                                || goal instanceof RangedBowAttackGoal
                                || goal instanceof CreepbowAttackGoal);
                this.goalSelector.addGoal(4, this.creepbowGoal);
            } else {
                this.goalSelector.removeGoal(this.creepbowGoal);
                super.reassessWeaponGoal();
            }
        }
    }

    @Override
    public void performRangedAttack(@NonNull LivingEntity target, float power) {
        ItemStack bowStack = this.getMainHandItem();
        if (!(bowStack.getItem() instanceof CreepbowItem)) {
            super.performRangedAttack(target, power);
            return;
        }

        ItemStack projectileStack = this.getProjectile(bowStack);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectileStack, power, bowStack);

        if (arrow instanceof Arrow tippedArrow) {
            tippedArrow.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        }

        double xd = target.getX() - this.getX();
        double yd = target.getY(0.3333333333333333) - arrow.getY();
        double zd = target.getZ() - this.getZ();
        double distanceToTarget = Math.sqrt(xd * xd + zd * zd);

        if (this.level() instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileUsingShoot(
                    arrow, serverLevel, projectileStack,
                    xd, yd + distanceToTarget * 0.2F, zd,
                    power * CreepbowItem.VELOCITY_MULTIPLIER, (float) (14 - serverLevel.getDifficulty().getId() * 4)
            );
        }

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}