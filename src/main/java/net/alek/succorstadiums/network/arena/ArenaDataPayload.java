package net.alek.succorstadiums.network.arena;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import net.alek.succorstadiums.arena.ArenaSessionManager;
import net.alek.succorstadiums.arena.MobArenaManager;

import net.alek.succorstadiums.arena.RewardItem;
import net.alek.succorstadiums.arena.MobArena;
import net.alek.succorstadiums.SuccorStadiums;

import org.jspecify.annotations.NonNull;
import java.util.stream.Collectors;
import java.util.*;

// ArenaDataPayload
public record ArenaDataPayload(List<ArenaEntry> arenas) implements CustomPacketPayload {

    // Create the packet type for the arena action payload identified as succorstadiums:arena_data
    public static final CustomPacketPayload.Type<ArenaDataPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arena_data"));

    public record ArenaEntry(
            String name, double x, double y, double z,
            int width, int length, int delaySeconds, boolean running, String group,
            List<RewardEntry> rewards, List<RewardEntry> participationRewards, List<WaveEntry> waves,
            String dimension
    ) {

        public static final String DEFAULT_DIMENSION = "minecraft:overworld";

        // Never null/blank, so the client can always resolve a dimension
        public ArenaEntry {
            if (dimension == null || dimension.isBlank()) dimension = DEFAULT_DIMENSION;
        }
    }

    public record WaveEntry(int waveNumber, String name, Integer delaySeconds, List<RewardEntry> rewards, List<MobEntry> mobs) {}

    public record MobEntry(
            String mobType, int count, Integer size, String ridingMob,
            String mainHandItem, String offHandItem, List<String> armorItems,
            String potionEffects, String enchantments
    ) {}

    // advancementId == null means this is not an advancement reward (it's an item or XP reward)
    public record RewardEntry(String itemId, int count, boolean xp, boolean levels, String advancementId) {

        public boolean isAdvancement() {
            return advancementId != null;
        }
    }

    // Helper method to write a list of rewards to the packet buffer: first the count, then each reward's fields
    public static void writeRewards(FriendlyByteBuf buf, List<RewardEntry> rewards) {
        buf.writeInt(rewards.size());
        for (RewardEntry r : rewards) {
            buf.writeUtf(r.itemId() == null ? "" : r.itemId());
            buf.writeInt(r.count());
            buf.writeBoolean(r.xp());
            buf.writeBoolean(r.levels());
            buf.writeUtf(r.advancementId() == null ? "" : r.advancementId());
        }
    }

    // Helper method to read a list of rewards from the packet buffer in the same order they were written
    public static List<RewardEntry> readRewards(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<RewardEntry> rewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String itemId = buf.readUtf();
            int c = buf.readInt();
            boolean xp = buf.readBoolean();
            boolean levels = buf.readBoolean();
            String advancementId = buf.readUtf();
            rewards.add(new RewardEntry(
                    itemId.isEmpty() ? null : itemId, c, xp, levels,
                    advancementId.isEmpty() ? null : advancementId));
        }
        return rewards;
    }

    // Defines how to write the payload to bytes and read it back; both halves must use the same field order
    public static final StreamCodec<FriendlyByteBuf, ArenaDataPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {

                // Write the number of arenas first so the reader knows how many to read, then write each arena
                buf.writeInt(payload.arenas().size());
                for (ArenaEntry arena : payload.arenas()) {

                    // Write the arena's name, position, size, delay, running state, group (null sent as "") and dimension
                    buf.writeUtf(arena.name());
                    buf.writeDouble(arena.x());
                    buf.writeDouble(arena.y());
                    buf.writeDouble(arena.z());
                    buf.writeInt(arena.width());
                    buf.writeInt(arena.length());
                    buf.writeInt(arena.delaySeconds());
                    buf.writeBoolean(arena.running());
                    buf.writeUtf(arena.group() == null ? "" : arena.group());
                    buf.writeUtf(arena.dimension());

                    // Write the arena rewards and participation rewards
                    writeRewards(buf, arena.rewards());
                    writeRewards(buf, arena.participationRewards());

                    // Write how many waves are in the list and loop that many times
                    buf.writeInt(arena.waves().size());
                    for (WaveEntry wave : arena.waves()) {

                        // Write each wave's number, name (null sent as ""), optional delay and rewards
                        buf.writeInt(wave.waveNumber());
                        buf.writeUtf(wave.name() == null ? "" : wave.name());
                        buf.writeBoolean(wave.delaySeconds() != null);
                        if (wave.delaySeconds() != null) buf.writeInt(wave.delaySeconds());
                        writeRewards(buf, wave.rewards());

                        // Write how many mobs are in the list and loop that many times
                        buf.writeInt(wave.mobs().size());
                        for (MobEntry mob : wave.mobs()) {

                            // Write the mobs type, count, optional size, riding mob, held items, armor, effects and enchantments
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
                    }
                }
            },
            buf -> {

                // Read the arena count and loop that many times
                int arenaCount = buf.readInt();
                List<ArenaEntry> arenas = new ArrayList<>();
                for (int i = 0; i < arenaCount; i++) {

                    // Read the arena's name, position, size, delay, running state, group (null sent as "") and dimension
                    String name = buf.readUtf();
                    double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
                    int width = buf.readInt(), length = buf.readInt(), delay = buf.readInt();
                    boolean running = buf.readBoolean();
                    String group = buf.readUtf();
                    String dimension = buf.readUtf();

                    // Read the arena rewards and participation rewards
                    List<RewardEntry> arenaRewards = readRewards(buf);
                    List<RewardEntry> arenaParticipationRewards = readRewards(buf);

                    // Read how many waves are in the list and loop that many times
                    int waveCount = buf.readInt();
                    List<WaveEntry> waves = new ArrayList<>();
                    for (int w = 0; w < waveCount; w++) {

                        // Read each wave's number, name (null sent as ""), optional delay and rewards
                        int waveNum = buf.readInt();
                        String waveName = buf.readUtf();
                        Integer waveDelay = null;
                        if (buf.readBoolean()) waveDelay = buf.readInt();
                        List<RewardEntry> waveRewards = readRewards(buf);

                        // Read how many mobs are in the list and loop that many times
                        int mobCount = buf.readInt();
                        List<MobEntry> mobs = new ArrayList<>();
                        for (int m = 0; m < mobCount; m++) {

                            // Read the mobs type, count, optional size, riding mob, held items, armor, effects and enchantments
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

                            // Add the mob entry, turning empty strings back into null
                            mobs.add(new MobEntry(mobType, count, size,
                                    ridingMob.isEmpty() ? null : ridingMob,
                                    mainHandItem.isEmpty() ? null : mainHandItem,
                                    offHandItem.isEmpty() ? null : offHandItem,
                                    armorItems,
                                    potionEffects.isEmpty() ? null : potionEffects,
                                    enchantments.isEmpty() ? null : enchantments
                            ));
                        }
                        // Add the wave entry to the arena
                        waves.add(new WaveEntry(waveNum, waveName.isEmpty() ? null : waveName, waveDelay, waveRewards, mobs));
                    }
                    // Add the arena entry
                    arenas.add(new ArenaEntry(name, x, y, z, width, length, delay, running,
                            group.isEmpty() ? null : group, arenaRewards, arenaParticipationRewards, waves, dimension));
                }
                return new ArenaDataPayload(arenas);
            }
    );

    // Override the type which tells minecraft which identifier to attach when this payload is sent
    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Converts a server-side RewardItem into its network representation
    // The advancement id is only included when the reward is an advancement
    public static RewardEntry toRewardEntry(RewardItem r) {
        return new RewardEntry(
                r.getItemId(), r.getCount(), r.isXp(), r.isXpLevels(),
                r.isAdvancement() ? r.getAdvancementId() : null);
    }

    // Builds the payload from the server's live arena data (called by the packet handler's sendData)
    public static ArenaDataPayload fromServer() {
        List<ArenaEntry> entries = new ArrayList<>();

        // Copy every saved MobArena into a read-only ArenaEntry for the client
        for (MobArena arena : MobArenaManager.getAllArenas()) {
            List<WaveEntry> waves = new ArrayList<>();

            // Convert each wave into a WaveEntry, with its mobs and rewards
            arena.getWaves().forEach(wave -> {

                // Convert each mob (a null armor list becomes an empty list so the codec never sees null)
                List<MobEntry> mobs = new ArrayList<>();
                wave.getMobs().forEach(mob -> mobs.add(new MobEntry(
                        mob.getMobType(), mob.getCount(), mob.getSize(),
                        mob.getRidingMob(), mob.getMainHandItem(), mob.getOffHandItem(),
                        mob.getArmorItems() != null ? mob.getArmorItems() : Collections.emptyList(),
                        mob.getPotionEffects(), mob.getEnchantments()
                )));

                // Convert the wave's rewards
                List<RewardEntry> waveRewards = wave.getRewards().stream()
                        .map(ArenaDataPayload::toRewardEntry)
                        .collect(Collectors.toList());

                waves.add(new WaveEntry(wave.getWaveNumber(), wave.getName(), wave.getDelaySeconds(), waveRewards, mobs));
            });

            // Convert the arena rewards and participation rewards
            List<RewardEntry> arenaRewards = arena.getRewards().stream()
                    .map(ArenaDataPayload::toRewardEntry)
                    .collect(Collectors.toList());
            List<RewardEntry> arenaParticipationRewards = arena.getParticipationRewards().stream()
                    .map(ArenaDataPayload::toRewardEntry)
                    .collect(Collectors.toList());

            // Build the ArenaEntry; the running flag comes from ArenaSessionManager
            entries.add(new ArenaEntry(
                    arena.getName(), arena.getCenterX(), arena.getCenterY(), arena.getCenterZ(),
                    arena.getWidth(), arena.getLength(), arena.getDelayBetweenWaves(),
                    ArenaSessionManager.isRunning(arena.getName()), arena.getGroup(),
                    arenaRewards, arenaParticipationRewards, waves, arena.getDimension()
            ));
        }
        // Reverse the order the arenas are sent in
        Collections.reverse(entries);
        return new ArenaDataPayload(entries);
    }
}