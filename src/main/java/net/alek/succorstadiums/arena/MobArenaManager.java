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

// Mob arena manager class
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

    // Constructor to create a mob arena with the given name, center position, radius, and wave delay
    public static void createArena(String name, double x, double y, double z, int radius, int delayBetweenWaves) {
        if (arenas.containsKey(name)) return;
        arenas.put(name, new MobArena(name, x, y, z, radius, delayBetweenWaves));
        save();
    }

    // Mutator method to remove an existing mob arena
    public static void removeArena(String name) {
        boolean removed = arenas.remove(name) != null;
        if (removed) save();
    }

    // Accessor method to get a given mob arena
    public static MobArena getArena(String name) {
        return arenas.get(name);
    }

    // Accessor method to get all created mob arenas
    public static Collection<MobArena> getAllArenas() {
        return arenas.values();
    }

    // Mutator method to rename an existing mob arena
    public static void renameArena(String oldName, String newName) {
        if (!arenas.containsKey(oldName)) return;
        if (arenas.containsKey(newName)) return;
        MobArena arena = arenas.remove(oldName);
        arena.setName(newName);
        arenas.put(newName, arena);
        save();
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