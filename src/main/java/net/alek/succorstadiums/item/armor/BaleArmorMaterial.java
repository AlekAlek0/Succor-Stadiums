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

public class BaleArmorMaterial {

    // Armor material key
    public static final ResourceKey<EquipmentAsset> BALE_ARMOR_MATERIAL_KEY =
            ResourceKey.create(EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "bale_armor"));

    // Armor material definition
    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            0,
            Map.of(
                    ArmorType.HELMET,     1,     // Helmet protection value
                    ArmorType.CHESTPLATE, 2,     // Chestplate protection value
                    ArmorType.LEGGINGS,   1,     // Leggings protection value
                    ArmorType.BOOTS,      0        // Boots protection value
            ),
            10,                      // Enchantability (higher = better enchants)
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0F,                            // Toughness
            0F,                                     // Knockback resistance
            null,
            BALE_ARMOR_MATERIAL_KEY
    );


}
