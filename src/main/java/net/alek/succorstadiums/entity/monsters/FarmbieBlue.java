package net.alek.succorstadiums.entity.monsters;

import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.item.ModItems;

public class FarmbieBlue extends Farmbie {
    public FarmbieBlue(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.FUMBLEBRINGER_FORK));

    }
}
