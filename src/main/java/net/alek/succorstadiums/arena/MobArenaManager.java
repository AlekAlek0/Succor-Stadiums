package net.alek.succorstadiums.arena;

import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import org.slf4j.Logger;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

// Mob arena manager class
public class MobArenaManager {

    // Initialize a logger and gson instance
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Create a map of mob arenas and initialize a path to the save file
    private static final Map<String, MobArena> arenas = new HashMap<>();
    private static Path saveFile;

    // Called on server start from Mod initialize
    public static void init(MinecraftServer server) {

        // Get the world's root folder then build folder and arena.json file
        Path dir = server.getWorldPath(LevelResource.ROOT)
                .resolve("succorstadiums");
        saveFile = dir.resolve("arenas.json");

        // Try and create succor stadiums folder if it doesn't already exist
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOGGER.error("Failed to create arena directory: ", e);
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

        // If save file doesnt exist return
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