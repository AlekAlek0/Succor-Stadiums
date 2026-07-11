package net.alek.succorstadiums.entity;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {

    public static final EntityType<MashedBananaSlime> MASHED_BANANA_SLIME = registerMob(
            "mashed_banana_slime", EntityType.Builder.<MashedBananaSlime>of(MashedBananaSlime::new,
                    MobCategory.MONSTER).sized(0.75f, 0.75f));



    private static <T extends Entity> EntityType<T> registerMob(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerModEntityTypes() {
        SuccorStadiums.LOGGER.info("Registering EntityTypes for " + SuccorStadiums.MOD_ID);
    }
}