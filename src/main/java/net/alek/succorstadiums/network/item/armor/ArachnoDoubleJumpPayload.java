package net.alek.succorstadiums.network.item.armor;

import io.netty.buffer.ByteBuf;
import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ArachnoDoubleJumpPayload() implements CustomPacketPayload {
    public static final Type<ArachnoDoubleJumpPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arachno_double_jump"));

    public static final StreamCodec<ByteBuf, ArachnoDoubleJumpPayload> CODEC =
            StreamCodec.of((buf, payload) -> {}, buf -> new ArachnoDoubleJumpPayload());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
