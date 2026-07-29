package net.alek.succorstadiums.entity.monsters;

import net.alek.succorstadiums.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class BananaSlime extends Slime {
    public BananaSlime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(size <= 1 ? 1.0 : size);
        }
    }

    @Override
    public @NonNull ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.BANANA_SLIME_BALL).getItem());
    }

    @Override
    protected boolean isDealsDamage() {
        return this.isEffectiveAi();
    }
}