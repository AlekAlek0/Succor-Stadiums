package net.alek.succorstadiums.entity;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.entity.monsters.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {

    public static final EntityType<BananaSlime> BANANA_SLIME = registerMob(
                "banana_slime", EntityType.Builder.of(BananaSlime::new,
                    MobCategory.MONSTER).sized(0.75f, 0.75f));

    public static final EntityType<Farmbie> FARMBIE = registerMob(
                "farmbie", EntityType.Builder.of(Farmbie::new,
                    MobCategory.MONSTER).sized(0.6f, 1.95f));

    public static final EntityType<GrassCreeper> GRASS_CREEPER = registerMob(
                "grass_creeper", EntityType.Builder.of(GrassCreeper::new,
                    MobCategory.MONSTER).sized(0.6f, 1.7f));

    public static final EntityType<Skelcrow> SKELCROW = registerMob(
            "skelcrow", EntityType.Builder.of(Skelcrow::new,
                    MobCategory.MONSTER).sized(0.6f, 1.99f));



    private static <T extends Entity> EntityType<T> registerMob(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerModEntityTypes() {
        SuccorStadiums.LOGGER.info("Registering EntityTypes for " + SuccorStadiums.MOD_ID);
    }
}