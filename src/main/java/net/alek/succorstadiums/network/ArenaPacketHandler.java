package net.alek.succorstadiums.network;

import net.alek.succorstadiums.arena.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class ArenaPacketHandler {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("SuccorStadiums/Packets");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArenaActionPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                handle(payload, player, context.server());
            });
        });
    }

    private static void handle(ArenaActionPayload p, ServerPlayer player, MinecraftServer server) {

        switch (p.action()) {

            case REQUEST_DATA -> sendData(player);

            case CREATE_ARENA -> {
                MobArenaManager.createArena(p.arenaName(), p.x(), p.y(), p.z(), p.radius(), p.delay());
                sendData(player);
            }
            case REMOVE_ARENA -> {
                // Stop the arena if it's running before removing it
                if (ArenaSessionManager.isRunning(p.arenaName())) {
                    ArenaSession session = ArenaSessionManager.getSession(p.arenaName());
                    if (session != null) session.KillCurrentWave(); // Ensure current wave is killed
                    ArenaSessionManager.stopSession(p.arenaName());
                    LOGGER.info("Stopped running arena {} before deletion.", p.arenaName());
                }
                MobArenaManager.removeArena(p.arenaName());
                sendData(player);
            }
            case EDIT_ARENA -> {
                MobArena arena = MobArenaManager.getArena(p.arenaName());
                if (arena != null) {
                    if (!p.newName().isEmpty() && !p.newName().equals(p.arenaName())) {
                        MobArenaManager.renameArena(p.arenaName(), p.newName());
                        arena = MobArenaManager.getArena(p.newName()); // re-fetch under new name
                    }
                    if (arena != null) {
                        arena.setCenter(p.x(), p.y(), p.z());
                        arena.setRadius(p.radius());
                        arena.setDelayBetweenWaves(p.delay());
                        MobArenaManager.save();
                    }
                }
                sendData(player);
            }
            case ADD_WAVE -> {
                MobArena arena = MobArenaManager.getArena(p.arenaName());
                if (arena != null) { arena.addWave(); MobArenaManager.save(); }
                sendData(player);
            }
            case REMOVE_WAVE -> {
                MobArena arena = MobArenaManager.getArena(p.arenaName());
                if (arena != null) { arena.removeWave(p.waveNumber()); MobArenaManager.save(); }
                sendData(player);
            }
            case ADD_MOB -> {
                MobArena arena = MobArenaManager.getArena(p.arenaName());
                if (arena != null) {
                    Wave wave = arena.getWave(p.waveNumber());
                    if (wave != null) {
                        wave.addMob(
                                p.mobType(), p.count(),
                                p.ridingMob(), p.mainHandItem(), p.offHandItem(), p.armorItems(),
                                p.potionEffects().isEmpty() ? null : p.potionEffects(),
                                p.enchantments().isEmpty()  ? null : p.enchantments()
                        );
                        MobArenaManager.save();
                    }
                }
                sendData(player);
            }
            case REMOVE_MOB -> {
                MobArena arena = MobArenaManager.getArena(p.arenaName());
                if (arena != null) {
                    Wave wave = arena.getWave(p.waveNumber());
                    if (wave != null) { wave.removeMob(p.mobType(), p.count()); MobArenaManager.save(); }
                }
                sendData(player);
            }
            case START_ARENA -> {
                MobArena arena = MobArenaManager.getArena(p.arenaName());
                if (arena != null && !ArenaSessionManager.isRunning(p.arenaName())) {
                    List<ServerPlayer> targetPlayers = new ArrayList<>();
                    for (String playerName : p.playerNames()) {
                        ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(playerName);
                        if (targetPlayer != null) {
                            targetPlayers.add(targetPlayer);
                        } else {
                            LOGGER.warn("Player {} not found for starting arena {}.", playerName, p.arenaName());
                        }
                    }

                    if (!targetPlayers.isEmpty()) {
                        ArenaSession session = new ArenaSession(arena, player.level(), targetPlayers);
                        ArenaSessionManager.startSession(session);
                    } else {
                        LOGGER.warn("No valid players selected to start arena {}.", p.arenaName());
                    }
                }
                sendData(player);
            }
            case STOP_ARENA -> {
                ArenaSession session = ArenaSessionManager.getSession(p.arenaName());
                if (session != null) session.KillCurrentWave();
                ArenaSessionManager.stopSession(p.arenaName());
                sendData(player);
            }
        }
    }

    private static void sendData(ServerPlayer player) {
        ServerPlayNetworking.send(player, ArenaDataPayload.fromServer());
    }
}