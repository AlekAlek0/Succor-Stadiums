package net.alek.succorstadiums.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("succorstadiums", "general"));

    public static KeyMapping OPEN_CONFIG;
    public static KeyMapping OPEN_MOB_ARENA_GUI;
    public static KeyMapping OPEN_BACKPACK;

    public static void registerKeyBindings() {

        OPEN_CONFIG = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.succorstadiums.open_config",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_BACKSLASH,
                        CATEGORY
                )
        );

        OPEN_MOB_ARENA_GUI = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.succorstadiums.open_mob_arena",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_H,
                        CATEGORY
                )
        );

        OPEN_BACKPACK = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.succorstadiums.open_backpack",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        CATEGORY
                )
        );
    }
}