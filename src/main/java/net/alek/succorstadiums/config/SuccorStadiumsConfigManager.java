package net.alek.succorstadiums.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import org.slf4j.Logger;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

// SuccorStadiumsConfigManager class
public class SuccorStadiumsConfigManager {

    // Initialize succorstadiums logger, gson, and succorstadiums config file path
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("succorstadiums.json");

    // Load succorstadiums config file
    public static SuccorStadiumsConfig load() {

        // If config file path exists try and load it else throw an exception
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                SuccorStadiumsConfig loaded = GSON.fromJson(reader, SuccorStadiumsConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                LOGGER.error("Error loading succorstadiums config file:", e);
            }
        }

        // If no config file path exists create a new one, save, and return it
        SuccorStadiumsConfig defaultConfig = new SuccorStadiumsConfig();
        save(defaultConfig);
        return defaultConfig;
    }

    // Save succorstadiums config file
    public static void save(SuccorStadiumsConfig config) {

        // Try to create a directory and save config file else throw an exception
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Error saving succorstadiums config file:", e);
        }
    }
}