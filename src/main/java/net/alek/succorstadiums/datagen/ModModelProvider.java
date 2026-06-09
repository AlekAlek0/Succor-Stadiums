package net.alek.succorstadiums.datagen;

import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

        itemModelGenerators.generateFlatItem(ModItems.BRENNON_ORE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SILVER_INGOT, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SILK_SPOOL, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SILK_WEAVE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BONE_BROTH, ModelTemplates.FLAT_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.BALE_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BALE_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BALE_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BALE_BOOTS, ModelTemplates.FLAT_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.BEAN_POLE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.BONE_DAGGER, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.FIRE_STAFF, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.AQUA_STAFF, ModelTemplates.FLAT_HANDHELD_ITEM);

        ItemModel.Unbaked Bownana_Normal = ItemModelUtils.plainModel(
                itemModelGenerators.createFlatItemModel(ModItems.BOWNANA, ModelTemplates.FLAT_HANDHELD_ITEM)
        );
        ItemModel.Unbaked Bownana_Pulling = ItemModelUtils.plainModel(
                itemModelGenerators.createFlatItemModel(ModItems.BOWNANA, "_pulling_0", ModelTemplates.FLAT_HANDHELD_ITEM)
        );

        itemModelGenerators.itemModelOutput.accept(
                ModItems.BOWNANA,
                ItemModelUtils.conditional(
                        ItemModelUtils.isUsingItem(),
                        Bownana_Pulling,
                        Bownana_Normal
                )
        );

        itemModelGenerators.generateFlatItem(ModItems.RESURRECTION_AMULET, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.FLINT_CHARM, ModelTemplates.FLAT_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.GRAMBLE_BAPPLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ROTTEN_STEW, ModelTemplates.FLAT_ITEM);

    }
}
