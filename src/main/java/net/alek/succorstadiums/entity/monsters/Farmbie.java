package net.alek.succorstadiums.entity.monsters;

import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Farmbie extends Zombie {
    public Farmbie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

}
