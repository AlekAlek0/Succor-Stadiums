package net.alek.succorstadiums.item;

import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.sound.ModSounds;

// ModArmorMaterials class
public class ModArmorMaterials {

    // Create registry key for armor
    public static final ResourceKey<? extends Registry<EquipmentAsset>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset"));

    // Create registries for armor
    public static final ResourceKey<EquipmentAsset> BALE_ARMOR_KEY = ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "bale_armor"));
    public static final ResourceKey<EquipmentAsset> ARACHNO_CARAPACE_ARMOR_KEY = ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arachno_carapace_armor"));
    public static final ResourceKey<EquipmentAsset> ARMOR_OF_THE_FOREST_KEY = ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "armor_of_the_forest"));
    public static final ResourceKey<EquipmentAsset> NANNER_WADERS_KEY = ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "nanner_water_waders"));

    // Create bale armor material
    public static final ArmorMaterial BALE_ARMOR_MATERIAL = new ArmorMaterial(0,
            ArmorMaterials.makeDefense(1,2,2,1,6),
            25, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, null, BALE_ARMOR_KEY);

    // Create arachno carapace armor material
    public static final ArmorMaterial ARACHNO_CARAPACE_ARMOR_MATERIAL = new ArmorMaterial(0,
            ArmorMaterials.makeDefense(0,0,0,0,0),
            25, Holder.direct(ModSounds.ARACHNO_CARAPACE_ARMOR_EQUIP), 0.0f, 0f, null, ARACHNO_CARAPACE_ARMOR_KEY);

    // Create nanner waders material
    public static final ArmorMaterial NANNER_WADERS_MATERIAL = new ArmorMaterial(0,
            ArmorMaterials.makeDefense(0,0,0,0,0),
            25, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, null, NANNER_WADERS_KEY);

    // Create armor of the forest armor material
    public static final ArmorMaterial ARMOR_OF_THE_FOREST_ARMOR_MATERIAL = new ArmorMaterial(0,
            ArmorMaterials.makeDefense(0,0,0,0, 0),
            25, SoundEvents.ARMOR_EQUIP_COPPER, 1.0f, 0.2f, null, ARMOR_OF_THE_FOREST_KEY);

}