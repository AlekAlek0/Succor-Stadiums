package net.alek.succorstadiums.datagen;

import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import net.alek.succorstadiums.SuccorStadiums;

// ModDamageTypes class
public class ModDamageTypes {

    // Create resource key for plant powder damage death message
    public static final ResourceKey<DamageType> PLANT_POWDER_1 = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "plant_powder_1"));

    // Create resource key for plant powder damage death message
    public static final ResourceKey<DamageType> PLANT_POWDER_2 = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "plant_powder_2"));

}