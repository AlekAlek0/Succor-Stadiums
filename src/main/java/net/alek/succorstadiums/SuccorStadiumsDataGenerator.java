package net.alek.succorstadiums;

import net.alek.succorstadiums.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class SuccorStadiumsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {

		var pack = fabricDataGenerator.createPack();

		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModEquipmentAssetProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModEntityLootTableProvider::new);
		pack.addProvider(ModEnglishLangProvider::new);
		pack.addProvider(ModItemTagProvider::new);
		pack.addProvider(ModEnchantmentGenerator::new);
		pack.addProvider(ModDamageTypeProvider::new);
		pack.addProvider(ModAdvancements::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.DAMAGE_TYPE, ModDamageTypeProvider::bootstrap);
		registryBuilder.add(Registries.ENCHANTMENT, ModEnchantmentGenerator::bootstrap);
	}
}