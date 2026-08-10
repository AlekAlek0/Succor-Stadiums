package net.alek.succorstadiums.network.arena;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ArenaSetRewardsPayload(String arenaName, int waveNumber,
                                     List<ArenaDataPayload.RewardEntry> rewards) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ArenaSetRewardsPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arena_set_rewards"));

    public static final StreamCodec<FriendlyByteBuf, ArenaSetRewardsPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.arenaName());
                buf.writeInt(p.waveNumber());
                buf.writeInt(p.rewards().size());
                for (ArenaDataPayload.RewardEntry r : p.rewards()) {
                    buf.writeUtf(r.itemId());
                    buf.writeInt(r.count());
                }
            },
            buf -> {
                String arenaName = buf.readUtf();
                int waveNumber = buf.readInt();
                int count = buf.readInt();
                List<ArenaDataPayload.RewardEntry> rewards = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    String itemId = buf.readUtf();
                    int c = buf.readInt();
                    rewards.add(new ArenaDataPayload.RewardEntry(itemId, c));
                }
                return new ArenaSetRewardsPayload(arenaName, waveNumber, rewards);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}