package net.alek.succorstadiums.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.alek.succorstadiums.network.arena.OpenMobArenaPayload;
import net.alek.succorstadiums.util.CustomVillagerSpawner;
import net.alek.succorstadiums.mana.ManaHelper;

// Mod commands class
public class ModCommands {

    // Register method for the mod commands
    public static void registerModCommands() {

        // Register the succor stadium commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        Commands.literal("succorstadiums")
                                // Mob arena gui subcommand
                                .then(Commands.literal("mobarenaGUI").executes(ModCommands::OpenMobArenaGUI))
                                // Villagers subcommand group
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("ye")
                                                .executes(ModCommands::executeSummonYe)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("ol")
                                                .executes(ModCommands::executeSummonOl)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("marvin")
                                                .executes(ModCommands::executeSummonMarvin)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("bimbleton")
                                                .executes(ModCommands::executeSummonBimbleton)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("bartholomew")
                                                .executes(ModCommands::executeSummonBartholomew)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("propung")
                                                .executes(ModCommands::executeSummonPropung)
                                        )
                                )
                                // Dev subcommand group
                                .then(Commands.literal("dev")
                                        .then(Commands.literal("disenchant")
                                                .executes(ModCommands::executeDisenchant)
                                        )
                                )
                )
        );

        // Register the mana commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("mana")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                    .executes(ModCommands::executeAddMana)
                                            )
                                    )
                            )
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                    .executes(ModCommands::executeConsumeMana)
                                            )
                                    )
                            )
            );
        });

        // Register the hypermana commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("hypermana")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                    .executes(ModCommands::executeAddHypermana)
                                            )
                                    )
                            )
                            .then(Commands.literal("consume")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                    .executes(ModCommands::executeConsumeHypermana)
                                            )
                                    )
                            )
            );
        });
    }

    // Helper method for the open mob arena gui command
    private static int OpenMobArenaGUI(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, new OpenMobArenaPayload());
        }
        return 1;
    }


    // Helper method for the villagers ye command
    private static int executeSummonYe(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnYeBuy(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Ye"), true);
        return 1;
    }

    // Helper method for the villagers ol command
    private static int executeSummonOl(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnOlSell(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Ol"), true);
        return 1;
    }

    // Helper method for the villagers marvin command
    private static int executeSummonMarvin(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnMarvin(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Marvin Malarkey"), true);
        return 1;
    }

    // Helper method for the villagers bimbleton command
    private static int executeSummonBimbleton(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnBimbleton(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Ghimple Bimbleton"), true);
        return 1;
    }

    // Helper method for the villagers bartholomew command
    private static int executeSummonBartholomew(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnBartholomew(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Bartholomew Bale"), true);
        return 1;
    }

    // Helper method for the villagers propung command
    private static int executeSummonPropung(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnPropung(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Propung Giewish"), true);
        return 1;
    }

    // Helper method for the dev disenchant command
    private static int executeDisenchant(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("You're not holding an item."));
            return 0;
        }

        stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        source.sendSuccess(() -> Component.literal("Disenchanted your item."), true);
        return 1;
    }

    // Helper method for the mana add command
    private static int executeAddMana(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getEntity();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        ManaHelper.addMana(player, amount);

        return 0;
    }

    // Helper method for the mana consume command
    private static int executeConsumeMana(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getEntity();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        ManaHelper.consumeManaOnly(player, amount);

        return 0;
    }

    // Helper method for the hypermana add command
    private static int executeAddHypermana(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getEntity();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        ManaHelper.addHypermana(player, amount);

        return 0;
    }

    // Helper method for the hypermana consume command
    private static int executeConsumeHypermana(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getEntity();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        ManaHelper.consumeHypermana(player, amount);

        return 0;
    }

}