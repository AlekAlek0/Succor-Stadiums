package net.alek.succorstadiums.entity;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.monster.Slime;

public class ModEntityAttributes {

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                ModEntityTypes.MASHED_BANANA_SLIME,
                Slime.createMobAttributes().add(Attributes.ATTACK_DAMAGE, 0.0D)
        );
    }
}