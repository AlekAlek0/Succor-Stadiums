package net.alek.succorstadiums.network.item.equipment.pouch;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record WithdrawExactCoinsPayload(int tradeIndex) implements CustomPacketPayload {

    public static final Type<WithdrawExactCoinsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("succorstadiums", "withdraw_exact_coins"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WithdrawExactCoinsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WithdrawExactCoinsPayload::tradeIndex,
            WithdrawExactCoinsPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}