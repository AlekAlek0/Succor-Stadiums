package net.alek.succorstadiums.network.arena;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Pastes a full copied wave (name, delay, and all its mobs) into a target arena as a new wave. */
public record ArenaPasteWavePayload(String targetArenaName, String waveName, int delaySeconds,
                                    List<ArenaDataPayload.MobEntry> mobs) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ArenaPasteWavePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arena_paste_wave"));

    public static final StreamCodec<FriendlyByteBuf, ArenaPasteWavePayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.targetArenaName());
                buf.writeUtf(p.waveName() == null ? "" : p.waveName());
                buf.writeInt(p.delaySeconds());
                buf.writeInt(p.mobs().size());
                for (ArenaDataPayload.MobEntry mob : p.mobs()) {
                    buf.writeUtf(mob.mobType());
                    buf.writeInt(mob.count());
                    buf.writeBoolean(mob.size() != null);
                    if (mob.size() != null) buf.writeInt(mob.size());
                    buf.writeUtf(mob.ridingMob() == null ? "" : mob.ridingMob());
                    buf.writeUtf(mob.mainHandItem() == null ? "" : mob.mainHandItem());
                    buf.writeUtf(mob.offHandItem() == null ? "" : mob.offHandItem());
                    buf.writeCollection(mob.armorItems(), FriendlyByteBuf::writeUtf);
                    buf.writeUtf(mob.potionEffects() == null ? "" : mob.potionEffects());
                    buf.writeUtf(mob.enchantments() == null ? "" : mob.enchantments());
                }
            },
            buf -> {
                String targetArena = buf.readUtf();
                String waveName = buf.readUtf();
                int delaySeconds = buf.readInt();
                int mobCount = buf.readInt();
                List<ArenaDataPayload.MobEntry> mobs = new ArrayList<>();
                for (int i = 0; i < mobCount; i++) {
                    String mobType = buf.readUtf();
                    int count = buf.readInt();
                    Integer size = null;
                    if (buf.readBoolean()) size = buf.readInt();
                    String ridingMob = buf.readUtf();
                    String mainHandItem = buf.readUtf();
                    String offHandItem = buf.readUtf();
                    List<String> armorItems = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
                    String potionEffects = buf.readUtf();
                    String enchantments = buf.readUtf();
                    mobs.add(new ArenaDataPayload.MobEntry(
                            mobType, count, size,
                            ridingMob.isEmpty() ? null : ridingMob,
                            mainHandItem.isEmpty() ? null : mainHandItem,
                            offHandItem.isEmpty() ? null : offHandItem,
                            armorItems,
                            potionEffects.isEmpty() ? null : potionEffects,
                            enchantments.isEmpty() ? null : enchantments
                    ));
                }
                return new ArenaPasteWavePayload(targetArena, waveName.isEmpty() ? null : waveName, delaySeconds, mobs);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}