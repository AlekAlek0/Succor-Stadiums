package net.alek.succorstadiums.datagen;

import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

        // Item datagen
        itemModelGenerators.generateFlatItem(ModItems.BRENNON_ORE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SILVER_INGOT, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SILK_SPOOL, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SILK_WEAVE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BONE_BROTH, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SPIDER_CARAPACE, ModelTemplates.FLAT_ITEM);

        // Armor datagen
        itemModelGenerators.generateFlatItem(ModItems.BALE_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BALE_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BALE_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BALE_BOOTS, ModelTemplates.FLAT_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.ARACHNO_CARAPACE_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ARACHNO_CARAPACE_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ARACHNO_CARAPACE_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ARACHNO_CARAPACE_BOOTS, ModelTemplates.FLAT_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.NANNER_WATER_WADERS, ModelTemplates.FLAT_ITEM);

        // Melee weapon datagen
        itemModelGenerators.generateFlatItem(ModItems.BEAN_POLE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BONE_DAGGER, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BANANNER_BLADE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateSpear(ModItems.FUMBLEBRINGER_FORK);

        // Magic weapon datagen
        itemModelGenerators.generateFlatItem(ModItems.FIRE_STAFF, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.AQUA_STAFF, ModelTemplates.FLAT_HANDHELD_ITEM);

        // Ranged weapon datagen
        itemModelGenerators.createFlatItemModel(ModItems.BOWNANA, ModelTemplates.BOW);
        itemModelGenerators.generateBow(ModItems.BOWNANA);

        itemModelGenerators.createFlatItemModel(ModItems.ARACHNO_CROSSBOW, ModelTemplates.CROSSBOW);
        itemModelGenerators.generateCrossbow(ModItems.ARACHNO_CROSSBOW);

        // Food datagen
        itemModelGenerators.generateFlatItem(ModItems.GRAMBLE_BAPPLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ROTTEN_STEW, ModelTemplates.FLAT_ITEM);

        // Trinket datagen
        itemModelGenerators.generateFlatItem(ModItems.RESURRECTION_AMULET, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.FLINT_CHARM, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.DOG_WHISTLE, ModelTemplates.FLAT_ITEM);

    }
}