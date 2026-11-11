package net.alek.succorstadiums.dev;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Dev only commands and tools class
public class DevCommands {

    // Players that currently have the dev no cooldown mode enabled
    private static final Set<UUID> NO_COOLDOWN_PLAYERS = ConcurrentHashMap.newKeySet();

    // Builds the "dev" subcommand group so it can be attached to the main succorstadiums command
    public static LiteralArgumentBuilder<CommandSourceStack> buildDevCommands() {
        return Commands.literal("developer")
                .then(Commands.literal("disenchant")
                        .executes(DevCommands::executeDisenchant)
                )
                .then(Commands.literal("no_cooldown")
                        .executes(DevCommands::executeToggleNoCooldown)
                );
    }

    // Registers the tick handler used by the dev tools
    public static void registerDevTick() {

        // Every tick clear the cooldowns of the held items for players with no cooldown mode on
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (NO_COOLDOWN_PLAYERS.isEmpty()) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!NO_COOLDOWN_PLAYERS.contains(player.getUUID())) continue;

                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack held = player.getItemInHand(hand);
                    if (held.isEmpty()) continue;

                    var cooldowns = player.getCooldowns();
                    if (cooldowns.isOnCooldown(held)) {
                        cooldowns.removeCooldown(cooldowns.getCooldownGroup(held));
                    }
                }
            }
        });
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

    // Helper method for the dev no_cooldown command, toggles no item cooldowns for the player
    private static int executeToggleNoCooldown(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }

        UUID id = player.getUUID();
        boolean enabled = !NO_COOLDOWN_PLAYERS.remove(id);

        if (enabled) {
            NO_COOLDOWN_PLAYERS.add(id);
        }

        // "No Item Cooldowns:" in pink, then ON in green or OFF in red
        Component message = Component.literal("Item Cooldowns: ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(enabled ? "ON" : "OFF")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));

        source.sendSuccess(() -> message, true);
        return 1;
    }
}