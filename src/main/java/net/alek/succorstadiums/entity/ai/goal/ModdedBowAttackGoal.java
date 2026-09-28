package net.alek.succorstadiums.entity.ai.goal;

import java.util.EnumSet;

import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

import net.alek.succorstadiums.datagen.ModItemTagProvider;

public class ModdedBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public ModdedBowAttackGoal(T mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public void setMinAttackInterval(int interval) {
        this.attackIntervalMin = interval;
    }

    private boolean isHoldingBow() {
        var held = this.mob.getMainHandItem();
        return held.is(ModItemTagProvider.MODDED_BOWS) || held.is(Items.BOW);
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.isHoldingBow();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSee = this.mob.getSensing().hasLineOfSight(target);
        if (canSee != (this.seeTime > 0)) this.seeTime = 0;
        this.seeTime += canSee ? 1 : -1;

        if (distSqr <= this.attackRadiusSqr && this.seeTime >= 20) {
            this.mob.getNavigation().stop();
            this.strafingTime++;
        } else {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.mob.getRandom().nextFloat() < 0.3F) this.strafingClockwise = !this.strafingClockwise;
            if (this.mob.getRandom().nextFloat() < 0.3F) this.strafingBackwards = !this.strafingBackwards;
            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distSqr > this.attackRadiusSqr * 0.75F) this.strafingBackwards = false;
            else if (distSqr < this.attackRadiusSqr * 0.25F) this.strafingBackwards = true;

            this.mob.getMoveControl().strafe(
                    this.strafingBackwards ? -0.5F : 0.5F,
                    this.strafingClockwise ? 0.5F : -0.5F);
            this.mob.lookAt(target, 30.0F, 30.0F);
        } else {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.mob.isUsingItem()) {
            if (!canSee && this.seeTime < -60) {
                this.mob.stopUsingItem();
            } else if (canSee) {
                int ticks = this.mob.getTicksUsingItem();
                if (ticks >= 20) {
                    this.mob.stopUsingItem();
                    this.mob.performRangedAttack(target, BowItem.getPowerForTime(ticks));
                    this.attackTime = this.attackIntervalMin;
                }
            }
        } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
        }
    }
}