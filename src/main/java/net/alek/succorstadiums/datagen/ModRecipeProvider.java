package net.alek.succorstadiums.datagen;

import java.util.concurrent.CompletableFuture;

import net.alek.succorstadiums.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registryLookup, @NonNull RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {

                // Silk Spool Recipe
                shapeless(RecipeCategory.MISC, ModItems.SILK_SPOOL)
                        .requires(Items.STRING, 8)
                        .unlockedBy(getHasName(Items.STRING), has(Items.STRING))
                        .save(output);

                // Silk Weave Recipe
                shapeless(RecipeCategory.MISC, ModItems.SILK_WEAVE, 2)
                        .requires(ModItems.SILK_SPOOL)
                        .unlockedBy(getHasName(ModItems.SILK_SPOOL), has(ModItems.SILK_SPOOL))
                        .save(output);

                // Bownana Recipe

                shaped(RecipeCategory.COMBAT, ModItems.BOWNANA, 1)
                        .pattern("cba")
                        .pattern("bca")
                        .pattern("cba")
                        .define('a', ModItems.SILK_SPOOL)
                        .define('b', ModItems.BANANA_BRANCH)
                        .define('c', ModItems.BANANA_SLIME_BALL)
                        .unlockedBy(getHasName(ModItems.SILK_SPOOL), has(ModItems.SILK_SPOOL))
                        .unlockedBy(getHasName(ModItems.BANANA_BRANCH), has(ModItems.BANANA_BRANCH))
                        .unlockedBy(getHasName(ModItems.BANANA_SLIME_BALL), has(ModItems.BANANA_SLIME_BALL))
                        .save(output);

                // Arachno Crossbow Recipe

                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CROSSBOW, 1)
                        .pattern("cec")
                        .pattern("sbs")
                        .pattern(" c ")
                        .define('c', ModItems.SPIDER_CARAPACE)
                        .define('s', ModItems.SILK_SPOOL)
                        .define('e', Items.SPIDER_EYE)
                        .define('b', Items.CROSSBOW)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.SILK_SPOOL), has(ModItems.SILK_SPOOL))
                        .unlockedBy(getHasName(Items.SPIDER_EYE), has(Items.SPIDER_EYE))
                        .unlockedBy(getHasName(Items.CROSSBOW), has(Items.CROSSBOW))
                        .save(output);

                // Bone Broth Recipe
                shaped(RecipeCategory.FOOD, ModItems.BONE_BROTH, 2)
                        .pattern("xxx")
                        .pattern("xxx")
                        .pattern(" p ")
                        .define('x', Items.BONE)
                        .define('p', Items.BOWL)
                        .unlockedBy(getHasName(Items.BONE), has(Items.BONE))
                        .unlockedBy(getHasName(Items.BOWL), has(Items.BOWL))
                        .save(output);

                // Rotten Stew Recipe
                shaped(RecipeCategory.FOOD, ModItems.ROTTEN_STEW)
                        .pattern("xxx")
                        .pattern(" p ")
                        .define('x', Items.ROTTEN_FLESH)
                        .define('p', ModItems.BONE_BROTH)
                        .unlockedBy(getHasName(Items.ROTTEN_FLESH), has(Items.ROTTEN_FLESH))
                        .unlockedBy(getHasName(ModItems.BONE_BROTH), has(ModItems.BONE_BROTH))
                        .save(output);
            }
        };
    }

    @Override
    public @NonNull String getName() {
        return "ModRecipeProvider";
    }
}