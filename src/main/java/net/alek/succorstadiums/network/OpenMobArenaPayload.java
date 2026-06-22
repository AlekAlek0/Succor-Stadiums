package net.alek.succorstadiums.network;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenMobArenaPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenMobArenaPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "open_mob_arena"));

    public static final StreamCodec<FriendlyByteBuf, OpenMobArenaPayload> CODEC = StreamCodec.unit(new OpenMobArenaPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
