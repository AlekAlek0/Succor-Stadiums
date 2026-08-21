package net.alek.succorstadiums.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.particle.ModParticles;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModParticleProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private final Map<ParticleType<?>, JsonArray> descriptions = new LinkedHashMap<>();

    public ModParticleProvider(FabricPackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "particles");
    }

    private void addDescriptions() {
        sprite(ModParticles.FOREST_ANGRY,
                Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "forest_angry"));
    }

    private void sprite(ParticleType<?> type, Identifier... textures) {
        JsonArray array = new JsonArray();
        for (Identifier texture : textures) {
            array.add(texture.toString());
        }
        descriptions.put(type, array);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput writer) {
        addDescriptions();

        CompletableFuture<?>[] futures = descriptions.entrySet().stream()
                .map(entry -> {
                    Identifier id = net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(entry.getKey());
                    JsonObject json = new JsonObject();
                    json.add("textures", entry.getValue());
                    assert id != null;
                    return DataProvider.saveStable(writer, json, pathProvider.json(id));
                })
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    @Override
    public @NonNull String getName() {
        return "Particle Definitions";
    }
}