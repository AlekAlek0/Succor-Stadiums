package net.alek.succorstadiums.arena;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaSessionManager {

    private static final Map<String, ArenaSession> activeSessions = new HashMap<>();

    // Register the tick event once on server start
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(ArenaSessionManager::tick);
    }

    public static ArenaSession getSession(String arenaName) {
        // Replace 'activeSessions' with whatever the name of your Map/List is
        return activeSessions.get(arenaName);
    }

    public static void startSession(ArenaSession session) {
        String arenaName = session.getArena().getName();
        if (activeSessions.containsKey(arenaName)) return; // already running
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
}