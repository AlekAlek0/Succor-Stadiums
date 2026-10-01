package net.alek.succorstadiums.network.arena;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import java.util.List;

import net.alek.succorstadiums.SuccorStadiums;

public record ArenaSetRewardsPayload(String arenaName, int waveNumber, boolean participation,
                                     List<ArenaDataPayload.RewardEntry> rewards) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ArenaSetRewardsPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arena_set_rewards"));

    public static final StreamCodec<FriendlyByteBuf, ArenaSetRewardsPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.arenaName());
                buf.writeInt(p.waveNumber());
                buf.writeBoolean(p.participation());
                ArenaDataPayload.writeRewards(buf, p.rewards());
            },
            buf -> {
                String arenaName = buf.readUtf();
                int waveNumber = buf.readInt();
                boolean participation = buf.readBoolean();
                List<ArenaDataPayload.RewardEntry> rewards = ArenaDataPayload.readRewards(buf);
                return new ArenaSetRewardsPayload(arenaName, waveNumber, participation, rewards);
            }
    );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}