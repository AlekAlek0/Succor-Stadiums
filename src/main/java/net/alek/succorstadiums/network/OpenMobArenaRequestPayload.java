package net.alek.succorstadiums.network;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenMobArenaRequestPayload() implements CustomPacketPayload {

    public static final Type<OpenMobArenaRequestPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            SuccorStadiums.MOD_ID,
                            "open_mob_arena_request"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMobArenaRequestPayload> CODEC =
            StreamCodec.unit(new OpenMobArenaRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}