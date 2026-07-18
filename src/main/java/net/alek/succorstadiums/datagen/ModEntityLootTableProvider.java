package net.alek.succorstadiums.datagen;

import net.alek.succorstadiums.item.ModItems;
import net.alek.succorstadiums.loottable.ModLootTables;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModEntityLootTableProvider extends SimpleFabricLootTableSubProvider {
    public ModEntityLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, LootContextParamSets.ENTITY);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter) {

        exporter.accept(ModLootTables.BANANA_SLIME_LOOT,

                LootTable.lootTable()
                        // 10% chance to drop 1 banana branch
                        .withPool(
                                LootPool.lootPool()
                                        .when(LootItemRandomChanceCondition.randomChance(0.10f))
                                        .add(
                                                LootItem.lootTableItem(ModItems.BANANA_BRANCH)
                                                        .apply(SetItemCountFunction.setCount(
                                                                ConstantValue.exactly(1)))
                                        )
                        )
                        // 5% chance to drop 1-3 banana slime ball
                        .withPool(
                                LootPool.lootPool()
                                        .when(LootItemRandomChanceCondition.randomChance(0.05f))
                                        .add(
                                                LootItem.lootTableItem(ModItems.BANANA_SLIME_BALL)
                                                        .apply(SetItemCountFunction.setCount(
                                                                UniformGenerator.between(1, 3)))
                                        )
                        )
        );
    }
}