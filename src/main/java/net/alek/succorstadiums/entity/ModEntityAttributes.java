package net.alek.succorstadiums.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class ModEntityAttributes {

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.BANANA_SLIME,
                Slime.createMobAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 0D)
        );

        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.FARMBIE,
                Zombie.createAttributes()
                        .add(Attributes.MOVEMENT_SPEED, 0.24D)
                        .add(Attributes.ATTACK_DAMAGE, 1.0D)
                        .add(Attributes.MAX_HEALTH, 12.0D)
                        .add(Attributes.ARMOR, 0D)

        );

        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.GRASS_CREEPER,
                Creeper.createAttributes()
                        .add(Attributes.MAX_HEALTH, 10D)
        );

        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.SKELCROW,
                Stray.createAttributes()
                        .add(Attributes.MAX_HEALTH, 10D)
        );
    }
}