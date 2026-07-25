package net.alek.succorstadiums.loottable;

import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ModLootTableModifers {
    private static final ResourceKey<LootTable> SPIDER_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/spider"));

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && key.equals(SPIDER_LOOT)) {
                // 40% chance to drop 0-2 spider legs
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.40f))
                                .add(
                                        LootItem.lootTableItem(ModItems.SPIDER_LEG)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(0, 2)))
                                )
                );

                // 20% chance to drop 2-4 spider carapace
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.20f))
                                .add(
                                        LootItem.lootTableItem(ModItems.SPIDER_CARAPACE)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(2, 4)))
                                )
                );
            }
        });
    }
}
