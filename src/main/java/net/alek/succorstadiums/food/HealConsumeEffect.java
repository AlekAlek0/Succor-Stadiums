package net.alek.succorstadiums.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public record HealConsumeEffect(float amount) implements ConsumeEffect {

    public static final MapCodec<HealConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("amount").forGetter(HealConsumeEffect::amount)
            ).apply(instance, HealConsumeEffect::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, HealConsumeEffect> STREAM_CODEC =
            StreamCodec.of(
                    (buf, effect) -> buf.writeFloat(effect.amount()),
                    buf -> new HealConsumeEffect(buf.readFloat())
            );

    @Override
    public ConsumeEffect.@NonNull Type<HealConsumeEffect> getType() {
        return ModConsumeEffects.HEAL_TYPE;
    }

    @Override
    public boolean apply(Level level, @NonNull ItemStack stack, @NonNull LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.heal(amount);
        }
        return true;
    }
}