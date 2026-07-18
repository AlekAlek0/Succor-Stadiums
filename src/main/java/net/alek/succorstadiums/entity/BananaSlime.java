package net.alek.succorstadiums.entity;

import net.alek.succorstadiums.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BananaSlime extends Slime {

    public BananaSlime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    @Override
    public ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.BANANA_SLIME_BALL).getItem());
    }
}