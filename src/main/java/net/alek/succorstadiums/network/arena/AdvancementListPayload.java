package net.alek.succorstadiums.network.arena;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Server -> client: every advancement ID on the server, used to autofill the advancement reward field
public record AdvancementListPayload(List<String> ids) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AdvancementListPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "advancement_list"));

    public static final StreamCodec<FriendlyByteBuf, AdvancementListPayload> CODEC = StreamCodec.of(
            (buf, p) -> buf.writeCollection(p.ids(), FriendlyByteBuf::writeUtf),
            buf -> new AdvancementListPayload(buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf))
    );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Only advancements with a display are listed, which skips purely technical ones. Those can still be typed in manually
    public static AdvancementListPayload fromServer(MinecraftServer server) {
        List<String> ids = server.getAdvancements().getAllAdvancements().stream()
                .filter(holder -> holder.value().display().isPresent())
                .map(holder -> holder.id().toString())
                .sorted()
                .collect(Collectors.toList());
        return new AdvancementListPayload(ids);
    }
}