package net.alek.succorstadiums.arena;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

public class MobArenaManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, MobArena> arenas = new HashMap<>();
    private static Path saveFile;

    // Called once on server start from your ModInitializer
    public static void init(MinecraftServer server) {
        Path dir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("succorstadiums");
        saveFile = dir.resolve("arenas.json");

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOGGER.error("", e);
        }

        load();
    }

    // Create a MobArena with the given name, center position, radius, and wave delay
    public static boolean createArena(String name, double x, double y, double z, int radius, int delayBetweenWaves) {
        if (arenas.containsKey(name)) return false;
        arenas.put(name, new MobArena(name, x, y, z, radius, delayBetweenWaves));
        save();
        return true;
    }

    // Remove a given MobArena
    public static boolean removeArena(String name) {
        boolean removed = arenas.remove(name) != null;
        if (removed) save();
        return removed;
    }

    // Get a given MobArena
    public static MobArena getArena(String name) {

        return arenas.get(name);
    }

    // Get all MobArenas
    public static Collection<MobArena> getAllArenas() {

        return arenas.values();
    }

    // Check if arena already exists with name given
    public static boolean arenaExists(String name) {

        return arenas.containsKey(name);
    }

    // Save the MobArena info to a JSON file
    public static void save() {
        try (Writer writer = new FileWriter(saveFile.toFile())) {
            List<MobArena> list = new ArrayList<>(arenas.values());
            GSON.toJson(list, writer);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    // Load the data in the arena JSON file
    private static void load() {
        if (!Files.exists(saveFile)) return;

        try (Reader reader = new FileReader(saveFile.toFile())) {
            Type listType = new TypeToken<List<MobArena>>() {}.getType();
            List<MobArena> list = GSON.fromJson(reader, listType);
            if (list != null) {
                arenas.clear();
                list.forEach(arena -> arenas.put(arena.getName(), arena));
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}