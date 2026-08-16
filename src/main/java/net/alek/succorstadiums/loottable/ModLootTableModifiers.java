package net.alek.succorstadiums.loottable;

import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ModLootTableModifiers {

    private static final ResourceKey<LootTable> ZOMBIE_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/zombie"));

    private static final ResourceKey<LootTable> SPIDER_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/spider"));

    private static final ResourceKey<LootTable> CREEPER_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/creeper"));


    public static void registerModLootTableModifiers() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && key.equals(ZOMBIE_LOOT)) {
                // 66% chance to drop 3-5 rotten flesh
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.66f))
                                .add(
                                        LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(3, 5)))
                                )
                );
            }
            if (source.isBuiltin() && key.equals(SPIDER_LOOT)) {
                // 20% chance to drop 0-2 spider legs
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.20f))
                                .add(
                                        LootItem.lootTableItem(ModItems.SPIDER_LEG)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(0, 2)))
                                )
                );
                // 5% chance to drop 2-4 spider carapace
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.05f))
                                .add(
                                        LootItem.lootTableItem(ModItems.SPIDER_CARAPACE)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(2, 4)))
                                )
                );
            }
            if (source.isBuiltin() && key.equals(CREEPER_LOOT)) {
                // 33% chance to drop 2-4 lapis lazuli
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .when(LootItemRandomChanceCondition.randomChance(0.33f))
                                .add(
                                        LootItem.lootTableItem(Items.LAPIS_LAZULI)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(2, 4)))
                                )
                );
            }
        });
    }
}
