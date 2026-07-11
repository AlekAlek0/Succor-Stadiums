package net.alek.succorstadiums.item;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.sound.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAsset;

public class ModArmorMaterials {

    public static final ResourceKey<? extends Registry<EquipmentAsset>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset"));

    public static final ResourceKey<EquipmentAsset> BALE_ARMOR_KEY = ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "bale_armor"));
    public static final ResourceKey<EquipmentAsset> NANNER_WADERS_KEY = ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "nanner_water_waders"));
    public static final ResourceKey<EquipmentAsset> ARACHNO_CARAPACE_ARMOR_KEY = ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "arachno_carapace_armor"));


    public static final ArmorMaterial BALE_ARMOR_MATERIAL = new ArmorMaterial(0,
            ArmorMaterials.makeDefense(1,2,2,1,0),
            99, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, null, BALE_ARMOR_KEY);

    public static final ArmorMaterial ARACHNO_CARAPACE_ARMOR_MATERIAL = new ArmorMaterial(0,
            ArmorMaterials.makeDefense(1,2,2,1,0),
            99, Holder.direct(ModSounds.ARACHNO_CARAPACE_ARMOR_EQUIP), 0.0F, 0.1F, null, ARACHNO_CARAPACE_ARMOR_KEY);

    public static final ArmorMaterial NANNER_WADERS_MATERIAL = new ArmorMaterial(0,
            ArmorMaterials.makeDefense(0,0,0,0,0),
            99, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, null, NANNER_WADERS_KEY);

}