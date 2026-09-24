package net.alek.succorstadiums.network.item.equipment.pouch;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

// SetPouchAutoDepositPayload class
public record SetPouchAutoDepositPayload(Identifier pouchTypeId, boolean enabled) implements CustomPacketPayload {

    // Create a type for the payload
    public static final Type<SetPouchAutoDepositPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("succorstadiums", "set_pouch_auto_deposit"));

    // Create a stream codec for the SetPouchAutoDepositPayload
    public static final StreamCodec<RegistryFriendlyByteBuf, SetPouchAutoDepositPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SetPouchAutoDepositPayload::pouchTypeId,
            ByteBufCodecs.BOOL, SetPouchAutoDepositPayload::enabled,
            SetPouchAutoDepositPayload::new
    );

    // Override the type of our custom packet with our type
    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}