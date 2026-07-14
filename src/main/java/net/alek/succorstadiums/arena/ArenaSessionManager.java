package net.alek.succorstadiums.arena;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArenaSessionManager {

    private static final Map<String, ArenaSession> activeSessions = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(ArenaSessionManager::tick);
        // Register for AFTER_DEATH event instead of AFTER_RESPAWN
        ServerLivingEntityEvents.AFTER_DEATH.register(ArenaSessionManager::onLivingEntityDeath);
    }

    public static ArenaSession getSession(String arenaName) {
        return activeSessions.get(arenaName);
    }

    public static void startSession(ArenaSession session) {
        String arenaName = session.getArena().getName();
        if (activeSessions.containsKey(arenaName)) return;
        activeSessions.put(arenaName, session);
        session.start();
    }

    public static void stopSession(String arenaName) {
        activeSessions.remove(arenaName);
    }

    public static boolean isRunning(String arenaName) {
        return activeSessions.containsKey(arenaName);
    }

    private static void tick(MinecraftServer server) {
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, ArenaSession> entry : activeSessions.entrySet()) {
            entry.getValue().tick();
            if (entry.getValue().isFinished()) {
                toRemove.add(entry.getKey());
            }
        }
        toRemove.forEach(activeSessions::remove);
    }

    // Handle living entity death event
    private static void onLivingEntityDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayer player) {
            UUID playerUUID = player.getUUID();
            for (ArenaSession session : activeSessions.values()) {
                if (session.hasPlayer(playerUUID)) {
                    session.onPlayerDeath(player);
                    break;
                }
            }
        }
    }
}