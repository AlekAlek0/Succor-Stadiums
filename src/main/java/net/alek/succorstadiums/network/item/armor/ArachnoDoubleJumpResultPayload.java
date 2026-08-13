package net.alek.succorstadiums.network.item.armor;

import io.netty.buffer.ByteBuf;
import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ArachnoDoubleJumpResultPayload(boolean success) implements CustomPacketPayload {
    public static final Type<ArachnoDoubleJumpResultPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arachno_double_jump_result"));

    public static final StreamCodec<ByteBuf, ArachnoDoubleJumpResultPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeBoolean(payload.success()),
                    buf -> new ArachnoDoubleJumpResultPayload(buf.readBoolean())
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}