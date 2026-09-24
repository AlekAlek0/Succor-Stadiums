package net.alek.succorstadiums.network.item.equipment.pouch;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

// PouchScrollTransferPayload class
public record PouchScrollTransferPayload(boolean deposit) implements CustomPacketPayload {

    // Create a type for the payload
    public static final Type<PouchScrollTransferPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("succorstadiums", "pouch_scroll_transfer"));

    // Create a stream codec for the PouchScrollTransferPayload
    public static final StreamCodec<RegistryFriendlyByteBuf, PouchScrollTransferPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PouchScrollTransferPayload::deposit,
            PouchScrollTransferPayload::new
    );

    // Override the type of our custom packet with our type
    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}