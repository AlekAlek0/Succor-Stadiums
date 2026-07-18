package net.alek.succorstadiums.loottable;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class ModLootTables {
    public static ResourceKey<LootTable> BANANA_SLIME_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "entities/banana_slime"));
}