package net.alek.succorstadiums.network;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.ArrayList;

public record ArenaActionPayload(Action action, String arenaName, String newName, int waveNumber, String mobType, int count,
                                 String ridingMob, String mainHandItem, String offHandItem, List<String> armorItems,
                                 String potionEffects, String enchantments, // Added these two fields
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
                buf.writeUtf(p.ridingMob() == null ? "" : p.ridingMob()); // Write ridingMob
                buf.writeUtf(p.mainHandItem() == null ? "" : p.mainHandItem()); // Write mainHandItem
                buf.writeUtf(p.offHandItem() == null ? "" : p.offHandItem()); // Write offHandItem
                buf.writeCollection(p.armorItems(), FriendlyByteBuf::writeUtf); // Write armorItems
                buf.writeUtf(p.potionEffects() == null ? "" : p.potionEffects()); // Write potionEffects
                buf.writeUtf(p.enchantments() == null ? "" : p.enchantments()); // Write enchantments
                buf.writeDouble(p.x());
                buf.writeDouble(p.y());
                buf.writeDouble(p.z());
                buf.writeInt(p.radius());
                buf.writeInt(p.delay());
                buf.writeCollection(p.playerNames(), FriendlyByteBuf::writeUtf);
            },
            buf -> new ArenaActionPayload(
                    buf.readEnum(Action.class),
                    buf.readUtf(), buf.readUtf(), buf.readInt(),
                    buf.readUtf(), buf.readInt(),
                    buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf),
                    buf.readUtf(), buf.readUtf(), // Read new fields
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readInt(), buf.readInt(),
                    buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf)
            )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Convenience constructors
    public static ArenaActionPayload requestData() {
        return new ArenaActionPayload(Action.REQUEST_DATA, "", "", 0, "", 0, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload createArena(String name, double x, double y, double z, int radius, int delay) {
        return new ArenaActionPayload(Action.CREATE_ARENA, name, "", 0, "", 0, "", "", "", List.of(), "", "", x, y, z, radius, delay, List.of());
    }
    public static ArenaActionPayload removeArena(String name) {
        return new ArenaActionPayload(Action.REMOVE_ARENA, name, "", 0, "", 0, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload editArena(String name, String newName, double x, double y, double z, int radius, int delay) {
        return new ArenaActionPayload(Action.EDIT_ARENA, name, newName, 0, "", 0, "", "", "", List.of(), "", "", x, y, z, radius, delay, List.of());
    }
    public static ArenaActionPayload addWave(String arena) {
        return new ArenaActionPayload(Action.ADD_WAVE, arena, "", 0, "", 0, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload removeWave(String arena, int wave) {
        return new ArenaActionPayload(Action.REMOVE_WAVE, arena, "", wave, "", 0, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload addMob(String arena, int wave, String mob, int count, String ridingMob, String mainHandItem, String offHandItem, List<String> armorItems, String potionEffects, String enchantments) {
        return new ArenaActionPayload(Action.ADD_MOB, arena, "", wave, mob, count, ridingMob, mainHandItem, offHandItem, armorItems, potionEffects, enchantments, 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload removeMob(String arena, int wave, String mob, int count) {
        return new ArenaActionPayload(Action.REMOVE_MOB, arena, "", wave, mob, count, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
    public static ArenaActionPayload startArena(String arena, List<String> playerNames) {
        return new ArenaActionPayload(Action.START_ARENA, arena, "", 0, "", 0, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, playerNames);
    }
    public static ArenaActionPayload stopArena(String arena) {
        return new ArenaActionPayload(Action.STOP_ARENA, arena, "", 0, "", 0, "", "", "", List.of(), "", "", 0, 0, 0, 0, 0, List.of());
    }
}