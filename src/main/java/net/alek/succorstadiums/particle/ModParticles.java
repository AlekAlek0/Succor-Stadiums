package net.alek.succorstadiums.particle;

import net.alek.succorstadiums.SuccorStadiums;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModParticles {

    public static final SimpleParticleType FOREST_ANGRY = FabricParticleTypes.simple();

    private static void registerParticle(String path, SimpleParticleType particle) {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, path),
                particle);
    }

    public static void registerModParticles() {
        registerParticle("forest_angry", FOREST_ANGRY);
    }
}