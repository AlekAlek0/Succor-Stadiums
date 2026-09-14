package net.alek.succorstadiums.food.consumeeffects;

import net.minecraft.world.item.consume_effects.ConsumeEffect;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.Level;
import com.mojang.serialization.Codec;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.food.ModConsumeEffects;
import net.alek.succorstadiums.mana.ManaHelper;

public class RestoreManaConsumeEffect implements ConsumeEffect {

    public static final MapCodec<RestoreManaConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("mana_amount").forGetter(e -> e.manaAmount)
    ).apply(instance, RestoreManaConsumeEffect::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RestoreManaConsumeEffect> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, e -> e.manaAmount,
                    RestoreManaConsumeEffect::new
            );

    private final int manaAmount;

    public RestoreManaConsumeEffect(int manaAmount) {
        this.manaAmount = manaAmount;
    }

    @Override
    public ConsumeEffect.@NonNull Type<? extends ConsumeEffect> getType() {
        return ModConsumeEffects.RESTORE_MANA_TYPE;
    }

    @Override
    public boolean apply(Level level, @NonNull ItemStack stack, @NonNull LivingEntity entity) {
        if (level.isClientSide()) {
            return true;
        }

        if (entity instanceof Player player) {
            ManaHelper.addMana(player, manaAmount);
        }

        return true;
    }
}