package net.alek.succorstadiums.entity.monsters;

import net.alek.succorstadiums.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ZombieFarmer extends Zombie {
    public ZombieFarmer(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        if (random.nextFloat() < 0.01F) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.FUMBLEBRINGER_FORK));
        }
    }

}
