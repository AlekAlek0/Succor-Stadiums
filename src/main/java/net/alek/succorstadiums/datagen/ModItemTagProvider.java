package net.alek.succorstadiums.datagen;

import net.alek.succorstadiums.SuccorStadiums;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    public static final TagKey<Item> BONE_DAGGERS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "bone_daggers")
    );


    @Override
    protected void addTags(HolderLookup.@NonNull Provider wrapperLookup) {
        builder(ItemTags.SWORDS)
                .add(ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "sword_of_the_forest")))
                .add(ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "sprout_sickle")));

        builder(ItemTags.SPEARS)
                .add(ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "fumblebringer_fork")));

        builder(BONE_DAGGERS)
                .add(ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "bone_dagger")));

    }
}