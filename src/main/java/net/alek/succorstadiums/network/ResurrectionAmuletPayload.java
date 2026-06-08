package net.alek.succorstadiums.network;

import io.netty.buffer.ByteBuf;
import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ResurrectionAmuletPayload(int entityId) implements CustomPacketPayload {

    public static final Type<ResurrectionAmuletPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "sands_of_time")
    );
    public static final StreamCodec<ByteBuf, ResurrectionAmuletPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, ResurrectionAmuletPayload::entityId, ResurrectionAmuletPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}