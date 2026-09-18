package net.alek.succorstadiums.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Registry;

import net.alek.succorstadiums.entity.projectile.BaleArrowEntity;
import net.alek.succorstadiums.entity.items.RazorThornEntity;
import net.alek.succorstadiums.entity.monsters.*;
import net.alek.succorstadiums.SuccorStadiums;

// ModEntityTypes class
public class ModEntityTypes {

    // Item Entities

    // Register razor thorn
    public static final EntityType<RazorThornEntity> RAZOR_THORN = register(
            "razor_thorn", EntityType.Builder.<RazorThornEntity>of(RazorThornEntity::new,
                    MobCategory.MISC).sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
    );

    // Register bale arrow
    public static final EntityType<BaleArrowEntity> BALE_ARROW = register(
            "bale_arrow", EntityType.Builder.<BaleArrowEntity>of(BaleArrowEntity::new,
                            MobCategory.MISC).sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
    );

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Living Entities

    // Register banana slime
    public static final EntityType<BananaSlime> BANANA_SLIME = registerMob(
                "banana_slime", EntityType.Builder.of(BananaSlime::new,
                    MobCategory.MONSTER).sized(0.75f, 0.75f));

    // Register farmbie
    public static final EntityType<Farmbie> FARMBIE = registerMob(
                "farmbie", EntityType.Builder.of(Farmbie::new,
                    MobCategory.MONSTER).sized(0.6f, 1.95f));

    // Register farmbie blue
    public static final EntityType<FarmbieBlue> FARMBIE_BLUE = registerMob(
            "farmbie_blue", EntityType.Builder.of(FarmbieBlue::new,
                    MobCategory.MONSTER).sized(0.6f, 1.95f));

    // Register grass creeper
    public static final EntityType<GrassCreeper> GRASS_CREEPER = registerMob(
                "grass_creeper", EntityType.Builder.of(GrassCreeper::new,
                    MobCategory.MONSTER).sized(0.6f, 1.7f));

    // Register skelcrow
    public static final EntityType<Skelcrow> SKELCROW = registerMob(
            "skelcrow", EntityType.Builder.of(Skelcrow::new,
                    MobCategory.MONSTER).sized(0.6f, 1.99f));

    // Register method for item entities
    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    // Register method for living entities
    private static <T extends Entity> EntityType<T> registerMob(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    // Register ModEntityTypes class
    public static void registerModEntityTypes() {
        SuccorStadiums.LOGGER.info("Registering Mod Entity Types for " + SuccorStadiums.MOD_ID);
    }
}