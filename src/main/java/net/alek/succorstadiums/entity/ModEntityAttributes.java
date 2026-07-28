package net.alek.succorstadiums.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class ModEntityAttributes {

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.BANANA_SLIME,
                Slime.createMobAttributes()
        );

        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.ZOMBIE_FARMER,
                Zombie.createAttributes()
                        .add(Attributes.MOVEMENT_SPEED, 0.24D)
                        .add(Attributes.MAX_HEALTH, 12.0D)
                        .add(Attributes.ARMOR, 1.0D)

        );



    }
}