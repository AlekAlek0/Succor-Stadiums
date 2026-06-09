package net.alek.succorstadiums.item.armor;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import java.util.Map;

public class NannerWaterWadersMaterial {

    // Armor material key
    public static final ResourceKey<EquipmentAsset> NANNER_WATER_WADERS_ARMOR_MATERIAL_KEY =
            ResourceKey.create(EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "nanner_water_waders_armor"));

    // Armor material definition
    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            0,
            Map.of(
                    ArmorType.BOOTS,      0        // Boots protection value
            ),
            10,                      // Enchantability (higher = better enchants)
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0F,                            // Toughness
            0F,                                     // Knockback resistance
            null,
            NANNER_WATER_WADERS_ARMOR_MATERIAL_KEY
    );


}
