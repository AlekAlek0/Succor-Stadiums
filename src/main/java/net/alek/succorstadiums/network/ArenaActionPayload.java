package net.alek.succorstadiums.network;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.ArrayList;

public record ArenaActionPayload(Action action, String arenaName, String newName, int waveNumber, String mobType, int count,
                                 Integer size, // Added size field
                                 String ridingMob, String mainHandItem, String offHandItem, List<String> armorItems,
                                 String potionEffects, String enchantments,
                                 double x, double y, double z, int radius, int delay, List<String> playerNames) implements CustomPacketPayload {

    public enum Action {
        REQUEST_DATA, REQUEST_GUI,
        CREATE_ARENA, REMOVE_ARENA, EDIT_ARENA,
        ADD_WAVE, REMOVE_WAVE,
        ADD_MOB, REMOVE_MOB,
        START_ARENA, STOP_ARENA
    }

    public static final CustomPacketPayload.Type<ArenaActionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arena_action"));

    public static final StreamCodec<FriendlyByteBuf, ArenaActionPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeEnum(p.action());
                buf.writeUtf(p.arenaName());
                buf.writeUtf(p.newName());
                buf.writeInt(p.waveNumber());
                buf.writeUtf(p.mobType());
                buf.writeInt(p.count());
                // Write size
                buf.writeBoolean(p.size() != null);
                if (p.size() != null) {
                    buf.writeInt(p.size());
                }
                buf.writeUtf(p.ridingMob() == null ? "" : p.ridingMob());
                buf.writeUtf(p.mainHandItem() == null ? "" : p.mainHandItem());
                buf.writeUtf(p.offHandItem() == null ? "" : p.offHandItem());
                buf.writeCollection(p.armorItems(), FriendlyByteBuf::writeUtf);
                buf.writeUtf(p.potionEffects() == null ? "" : p.potionEffects());
                buf.writeUtf(p.enchantments() == null ? "" : p.enchantments());
                buf.writeDouble(p.x());
                buf.writeDouble(p.y());
                buf.writeDouble(p.z());
                buf.writeInt(p.radius());
                buf.writeInt(p.delay());
                buf.writeCollection(p.playerNames(), FriendlyByteBuf::writeUtf);
            },
            buf -> {
                Action action = buf.readEnum(Action.class);
                String arenaName = buf.readUtf();
                String newName = buf.readUtf();
                int waveNumber = buf.readInt();
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
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                int radius = buf.readInt();
                int delay = buf.readInt();
                List<String> playerNames = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);

                return new ArenaActionPayload(action, arenaName, newName, waveNumber, mobType, count, size,
                        ridingMob.isEmpty() ? null : ridingMob,
                        mainHandItem.isEmpty() ? null : mainHandItem,
                        offHandItem.isEmpty() ? null : offHandItem,
                        armorItems,
                        potionEffects.isEmpty() ? null : potionEffects,
                        enchantments.isEmpty() ? null : enchantments,
                        x, y, z, radius, delay, playerNames);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Convenience constructors
    public static ArenaActionPayload requestData() {
        return new ArenaActionPayload(Action.REQUEST_DATA, "", "", 0, "", 0, null, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload createArena(String name, double x, double y, double z, int radius, int delay) {
        return new ArenaActionPayload(Action.CREATE_ARENA, name, "", 0, "", 0, null, "", "", "", List.of(), "", "", x, y, z, radius, delay, List.of());
    }
    public static ArenaActionPayload removeArena(String name) {
        return new ArenaActionPayload(Action.REMOVE_ARENA, name, "", 0, "", 0, null, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload editArena(String name, String newName, double x, double y, double z, int radius, int delay) {
        return new ArenaActionPayload(Action.EDIT_ARENA, name, newName, 0, "", 0, null, "", "", "", List.of(), "", "", x, y, z, radius, delay, List.of());
    }
    public static ArenaActionPayload addWave(String arena) {
        return new ArenaActionPayload(Action.ADD_WAVE, arena, "", 0, "", 0, null, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload removeWave(String arena, int wave) {
        return new ArenaActionPayload(Action.REMOVE_WAVE, arena, "", wave, "", 0, null, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload addMob(String arena, int wave, String mob, int count, Integer size, String ridingMob, String mainHandItem, String offHandItem, List<String> armorItems, String potionEffects, String enchantments) {
        return new ArenaActionPayload(Action.ADD_MOB, arena, "", wave, mob, count, size, ridingMob, mainHandItem, offHandItem, armorItems, potionEffects, enchantments, 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload removeMob(String arena, int wave, String mob, int count) {
        return new ArenaActionPayload(Action.REMOVE_MOB, arena, "", wave, mob, count, null, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload startArena(String arena, List<String> playerNames) {
        return new ArenaActionPayload(Action.START_ARENA, arena, "", 0, "", 0, null, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, playerNames);
    }
    public static ArenaActionPayload stopArena(String arena) {
        return new ArenaActionPayload(Action.STOP_ARENA, arena, "", 0, "", 0, null, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
}