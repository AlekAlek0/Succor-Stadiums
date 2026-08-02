package net.alek.succorstadiums.food;

import com.mojang.serialization.MapCodec;
import net.alek.succorstadiums.datagen.ModDamageTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public record ExplodeConsumeEffect() implements ConsumeEffect {

    public static final MapCodec<ExplodeConsumeEffect> CODEC =
            MapCodec.unit(ExplodeConsumeEffect::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplodeConsumeEffect> STREAM_CODEC =
            StreamCodec.unit(new ExplodeConsumeEffect());

    @Override
    public ConsumeEffect.@NonNull Type<ExplodeConsumeEffect> getType() {
        return ModConsumeEffects.PLANT_POWDER_TYPE;
    }

    @Override
    public boolean apply(Level level, @NonNull ItemStack stack, @NonNull LivingEntity entity) {
        if (level.isClientSide()) {
            return true;
        }

        RegistryAccess registryAccess = level.registryAccess();
        ResourceKey<DamageType> key = level.getRandom().nextBoolean()
                ? ModDamageTypes.PLANT_POWDER_1
                : ModDamageTypes.PLANT_POWDER_2;

        Holder<DamageType> holder = registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
        DamageSource source = new DamageSource(holder, entity, entity);

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F, 1.0F);

        entity.hurt(source, Float.MAX_VALUE);
        return true;
    }
}