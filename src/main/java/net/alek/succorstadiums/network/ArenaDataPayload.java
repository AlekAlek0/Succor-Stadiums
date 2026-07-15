package net.alek.succorstadiums.network;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.arena.MobArena;
import net.alek.succorstadiums.arena.MobArenaManager;
import net.alek.succorstadiums.arena.ArenaSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.*;

public record ArenaDataPayload(List<ArenaEntry> arenas) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ArenaDataPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arena_data"));

    public record ArenaEntry(
            String name, double x, double y, double z,
            int radius, int delaySeconds, boolean running,
            List<WaveEntry> waves
    ) {}

    public record WaveEntry(int waveNumber, List<MobEntry> mobs) {}

    public record MobEntry(
            String mobType,
            int count,
            Integer size, // Added size field
            String ridingMob,
            String mainHandItem,
            String offHandItem,
            List<String> armorItems,
            String potionEffects,
            String enchantments
    ) {}

    public static final StreamCodec<FriendlyByteBuf, ArenaDataPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.arenas().size());
                for (ArenaEntry arena : payload.arenas()) {
                    buf.writeUtf(arena.name());
                    buf.writeDouble(arena.x());
                    buf.writeDouble(arena.y());
                    buf.writeDouble(arena.z());
                    buf.writeInt(arena.radius());
                    buf.writeInt(arena.delaySeconds());
                    buf.writeBoolean(arena.running());
                    buf.writeInt(arena.waves().size());
                    for (WaveEntry wave : arena.waves()) {
                        buf.writeInt(wave.waveNumber());
                        buf.writeInt(wave.mobs().size());
                        for (MobEntry mob : wave.mobs()) {
                            buf.writeUtf(mob.mobType());
                            buf.writeInt(mob.count());
                            // Write size
                            buf.writeBoolean(mob.size() != null);
                            if (mob.size() != null) {
                                buf.writeInt(mob.size());
                            }
                            buf.writeUtf(mob.ridingMob() == null ? "" : mob.ridingMob());
                            buf.writeUtf(mob.mainHandItem() == null ? "" : mob.mainHandItem());
                            buf.writeUtf(mob.offHandItem() == null ? "" : mob.offHandItem());
                            buf.writeCollection(mob.armorItems(), FriendlyByteBuf::writeUtf);
                            buf.writeUtf(mob.potionEffects() == null ? "" : mob.potionEffects());
                            buf.writeUtf(mob.enchantments() == null ? "" : mob.enchantments());
                        }
                    }
                }
            },
            buf -> {
                int arenaCount = buf.readInt();
                List<ArenaEntry> arenas = new ArrayList<>();
                for (int i = 0; i < arenaCount; i++) {
                    String name = buf.readUtf();
                    double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
                    int radius = buf.readInt(), delay = buf.readInt();
                    boolean running = buf.readBoolean();
                    int waveCount = buf.readInt();
                    List<WaveEntry> waves = new ArrayList<>();
                    for (int w = 0; w < waveCount; w++) {
                        int waveNum = buf.readInt();
                        int mobCount = buf.readInt();
                        List<MobEntry> mobs = new ArrayList<>();
                        for (int m = 0; m < mobCount; m++) {
                            String mobType = buf.readUtf();
                            int count = buf.readInt();
                            // Read size
                            Integer size = null;
                            if (buf.readBoolean()) {
                                size = buf.readInt();
                            }
                            String ridingMob = buf.readUtf();
                            String mainHandItem = buf.readUtf();
                            String offHandItem = buf.readUtf();
                            List<String> armorItems = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
                            String potionEffects = buf.readUtf();
                            String enchantments = buf.readUtf();

                            mobs.add(new MobEntry(
                                    mobType,
                                    count,
                                    size, // Added size here
                                    ridingMob.isEmpty() ? null : ridingMob,
                                    mainHandItem.isEmpty() ? null : mainHandItem,
                                    offHandItem.isEmpty() ? null : offHandItem,
                                    armorItems,
                                    potionEffects.isEmpty() ? null : potionEffects,
                                    enchantments.isEmpty() ? null : enchantments
                            ));
                        }
                        waves.add(new WaveEntry(waveNum, mobs));
                    }
                    arenas.add(new ArenaEntry(name, x, y, z, radius, delay, running, waves));
                }
                return new ArenaDataPayload(arenas);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Build a payload from live server data */
    public static ArenaDataPayload fromServer() {
        List<ArenaEntry> entries = new ArrayList<>();
        for (MobArena arena : MobArenaManager.getAllArenas()) {
            List<WaveEntry> waves = new ArrayList<>();
            arena.getWaves().forEach(wave -> {
                List<MobEntry> mobs = new ArrayList<>();
                wave.getMobs().forEach(mob -> mobs.add(new MobEntry(
                        mob.getMobType(),
                        mob.getCount(),
                        mob.getSize(), // Added size here
                        mob.getRidingMob(),
                        mob.getMainHandItem(),
                        mob.getOffHandItem(),
                        mob.getArmorItems() != null ? mob.getArmorItems() : Collections.emptyList(),
                        mob.getPotionEffects(),
                        mob.getEnchantments()
                )));
                waves.add(new WaveEntry(wave.getWaveNumber(), mobs));
            });
            entries.add(new ArenaEntry(
                    arena.getName(), arena.getCenterX(), arena.getCenterY(), arena.getCenterZ(),
                    arena.getRadius(), arena.getDelayBetweenWaves(),
                    ArenaSessionManager.isRunning(arena.getName()), waves
            ));
        }
        Collections.reverse(entries);
        return new ArenaDataPayload(entries);
    }
}