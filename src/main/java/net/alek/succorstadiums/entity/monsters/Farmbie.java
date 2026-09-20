package net.alek.succorstadiums.entity.monsters;

import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.Nullable;

import net.alek.succorstadiums.item.ModItems;


public class Farmbie extends Zombie {
    public Farmbie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    @Override
    protected void populateDefaultEquipmentSlots(final RandomSource random, final DifficultyInstance difficulty) {
        if (random.nextFloat() < 0.15F * difficulty.getSpecialMultiplier()) {
            int armorType = random.nextInt(3);

            for (int i = 1; i <= 3; ++i) {
                if (random.nextFloat() < 0.1087F) {
                    ++armorType;
                }
            }

            float partialChance =
                    this.level().getDifficulty() == Difficulty.HARD
                            ? 0.1F
                            : 0.25F;

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
                        this.setItemSlot(
                                slot,
                                new ItemStack(equip)
                        );
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
