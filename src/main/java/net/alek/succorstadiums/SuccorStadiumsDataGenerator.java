package net.alek.succorstadiums;

import net.alek.succorstadiums.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class SuccorStadiumsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {

		var pack = fabricDataGenerator.createPack();

		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModEquipmentAssetProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModEntityLootTableProvider::new);
		pack.addProvider(ModEnglishLangProvider::new);

	}
}