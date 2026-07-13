package net.alek.succorstadiums.command;

import com.mojang.brigadier.context.CommandContext;
import net.alek.succorstadiums.arena.*;
import net.alek.succorstadiums.network.OpenMobArenaPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

// Mod commands class
public class ModCommands {

    // Register method for mod commands
    public static void registerModCommands() {

        // Register the mob arena gui command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        Commands.literal("succorstadiums")
                                .then(Commands.literal("mobarenaGUI").executes(ModCommands::OpenMobArenaGUI))
                )
        );
    }

    // Helper method for the open mob arena gui command
    private static int OpenMobArenaGUI(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, new OpenMobArenaPayload());
        }
        return 1;
    }
}