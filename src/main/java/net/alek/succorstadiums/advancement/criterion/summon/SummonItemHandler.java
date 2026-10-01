package net.alek.succorstadiums.advancement.criterion.summon;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.alek.succorstadiums.advancement.ModCriteria;
import net.alek.succorstadiums.SuccorStadiums;

// SummonItemHandler class
// Fires the Endless Companions criterion whenever a player uses any item in the summon_items tag
public class SummonItemHandler {

    public static final TagKey<Item> SUMMON_ITEMS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "summon_items"));

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.getItemInHand(hand).is(SUMMON_ITEMS)) {
                ModCriteria.SUMMON_ITEM_USE.trigger(serverPlayer);
            }
            // Pass so the item's own use behavior still runs normally
            return InteractionResult.PASS;
        });
    }
}