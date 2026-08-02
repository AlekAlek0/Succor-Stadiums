package net.alek.succorstadiums.datagen;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> PLANT_POWDER_1 = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "plant_powder_1"));

    public static final ResourceKey<DamageType> PLANT_POWDER_2 = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "plant_powder_2"));

}