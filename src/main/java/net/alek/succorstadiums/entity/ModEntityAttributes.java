package net.alek.succorstadiums.entity;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.Creeper;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

// ModEntityAttributes class
public class ModEntityAttributes {

    // Register method
    public static void register() {

        // Register banana slime entity attributes
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.BANANA_SLIME,
                Slime.createMobAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 0D)
        );

        // Register farmbie entity attributes
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.FARMBIE,
                Zombie.createAttributes()
                        .add(Attributes.MOVEMENT_SPEED, 0.24D)
                        .add(Attributes.ATTACK_DAMAGE, 1.0D)
                        .add(Attributes.ATTACK_SPEED, 1.0D)
                        .add(Attributes.MAX_HEALTH, 12.0D)
                        .add(Attributes.ARMOR, 0D)

        );

        // Register farmbie blue entity attributes
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.FARMBIE_BLUE,
                Zombie.createAttributes()
                        .add(Attributes.MOVEMENT_SPEED, 0.25D)
                        .add(Attributes.ATTACK_DAMAGE, 2.5D)
                        .add(Attributes.ATTACK_SPEED, 0.5D)
                        .add(Attributes.MAX_HEALTH, 16.0D)
                        .add(Attributes.ARMOR, 2D)
        );

        // Register grass creeper entity attributes
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.GRASS_CREEPER,
                Creeper.createAttributes()
                        .add(Attributes.MAX_HEALTH, 10D)
        );

        // Register skelcrow entity attributes
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.SKELCROW,
                Stray.createAttributes()
                        .add(Attributes.MAX_HEALTH, 10D)
        );
    }
}