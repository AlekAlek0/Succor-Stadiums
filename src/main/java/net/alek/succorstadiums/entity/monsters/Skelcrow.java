package net.alek.succorstadiums.entity.monsters;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.alek.succorstadiums.entity.ai.goal.ModdedBowAttackGoal;
import net.alek.succorstadiums.item.weapons.ranged.ModdedBow;
import net.alek.succorstadiums.datagen.ModItemTagProvider;
import net.alek.succorstadiums.item.ModItems;

public class Skelcrow extends Stray {
    public Skelcrow(EntityType<? extends Stray> type, Level level) {
        super(type, level);
    }

    private ModdedBowAttackGoal<Skelcrow> bowGoal;
    private MeleeAttackGoal meleeGoal;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    @Override
    protected @NonNull AbstractArrow getArrow(@NonNull ItemStack projectile, float power, @Nullable ItemStack weapon) {
        return ProjectileUtil.getMobArrow(this, projectile, power, weapon);
    }

    @Override
    public void reassessWeaponGoal() {
        if (this.level().isClientSide()) return;

        if (this.bowGoal == null) {
            this.bowGoal = new ModdedBowAttackGoal<>(this, 1.0D, 20, 15.0F);
        }
        if (this.meleeGoal == null) {
            this.meleeGoal = new MeleeAttackGoal(this, 1.2D, false) {
                @Override
                public void stop() {
                    super.stop();
                    Skelcrow.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    Skelcrow.this.setAggressive(true);
                }
            };
        }

        this.goalSelector.removeGoal(this.meleeGoal);
        this.goalSelector.removeGoal(this.bowGoal);

        ItemStack held = this.getMainHandItem();
        if (held.is(ModItemTagProvider.MODDED_BOWS) || held.is(Items.BOW)) {
            int interval = this.level().getDifficulty() != Difficulty.HARD ? 40 : 20;
            this.bowGoal.setMinAttackInterval(interval);
            this.goalSelector.addGoal(4, this.bowGoal);
        } else {
            this.goalSelector.addGoal(4, this.meleeGoal);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ItemStack bowItem = this.getMainHandItem();
        ItemStack projectile = this.getProjectile(bowItem);
        AbstractArrow arrow = this.getArrow(projectile, power, bowItem);

        float mult = bowItem.getItem() instanceof ModdedBow b ? b.getVelocityMultiplier() : 1.6F;

        double xd = target.getX() - this.getX();
        double yd = target.getY(0.3333333333333333D) - arrow.getY();
        double zd = target.getZ() - this.getZ();
        double distanceToTarget = Math.sqrt(xd * xd + zd * zd);
        double arc = distanceToTarget * 0.2F / (mult * mult);

        arrow.shoot(xd, yd + arc, zd, mult, (float) (14 - this.level().getDifficulty().getId() * 4));

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    @Override
    protected void populateDefaultEquipmentSlots(final RandomSource random, final DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.OAK_BOW));

        if (random.nextFloat() < 0.15F * difficulty.getSpecialMultiplier()) {
            int armorType = random.nextInt(3);

            for (int i = 1; i <= 3; ++i) {
                if (random.nextFloat() < 0.1087F) {
                    armorType++;
                }
            }

            float partialChance = this.level().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;

            boolean first = true;

            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack itemStack = this.getItemBySlot(slot);

                if (!first && random.nextFloat() < partialChance) {
                    break;
                }

                first = false;

                if (itemStack.isEmpty()) {
                    Item equip = getEquipmentForSlot(slot, armorType);

                    if (equip != null) {
                        this.setItemSlot(slot, new ItemStack(equip));
                    }
                }
            }
        }
    }

    public static @Nullable Item getEquipmentForSlot(final EquipmentSlot slot, final int type) {
        switch (slot) {
            case HEAD:
                if (type <= 2) {
                    return ModItems.BALE_HELMET;
                } else {
                    return ModItems.ARACHNO_CARAPACE_HELMET;
                }
            case CHEST:
                if (type <= 2) {
                    return ModItems.BALE_CHESTPLATE;
                } else {
                    return ModItems.ARACHNO_CARAPACE_CHESTPLATE;
                }
            case LEGS:
                if (type <= 2) {
                    return ModItems.BALE_LEGGINGS;
                } else {
                    return ModItems.ARACHNO_CARAPACE_LEGGINGS;
                }
            case FEET:
                if (type <= 2) {
                    return ModItems.BALE_BOOTS;
                } else {
                    return ModItems.ARACHNO_CARAPACE_BOOTS;
                }
            default:
                return null;
        }
    }
}