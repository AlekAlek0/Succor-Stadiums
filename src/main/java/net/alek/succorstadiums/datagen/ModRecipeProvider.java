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

                // Item recipe data gens

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

                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

                // Armor data gens

                // Arachno Helmet Recipe
                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CARAPACE_HELMET, 1)
                        .pattern("aaa")
                        .pattern("a a")
                        .pattern("b b")
                        .define('a', ModItems.SPIDER_CARAPACE)
                        .define('b', Items.SPIDER_EYE)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(Items.SPIDER_EYE), has(Items.SPIDER_EYE))
                        .save(output);

                // Arachno Chestplate Recipe
                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CARAPACE_CHESTPLATE, 1)
                        .pattern("aca")
                        .pattern("bab")
                        .pattern("aaa")
                        .define('a', ModItems.SPIDER_CARAPACE)
                        .define('b', ModItems.SPIDER_LEG)
                        .define('c', Items.SPIDER_EYE)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.SPIDER_LEG), has(ModItems.SPIDER_LEG))
                        .unlockedBy(getHasName(Items.SPIDER_EYE), has(Items.SPIDER_EYE))
                        .save(output);

                // Arachno Leggings Recipe
                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CARAPACE_LEGGINGS, 1)
                        .pattern("aaa")
                        .pattern("bcb")
                        .pattern("b b")
                        .define('a', ModItems.SPIDER_CARAPACE)
                        .define('b', ModItems.SPIDER_LEG)
                        .define('c', Items.SPIDER_EYE)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.SPIDER_LEG), has(ModItems.SPIDER_LEG))
                        .unlockedBy(getHasName(Items.SPIDER_EYE), has(Items.SPIDER_EYE))
                        .save(output);

                // Arachno Boots Recipe
                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CARAPACE_BOOTS, 1)
                        .pattern("c c")
                        .pattern("a a")
                        .pattern("b b")
                        .define('a', ModItems.SPIDER_CARAPACE)
                        .define('b', ModItems.SPIDER_LEG)
                        .define('c', Items.SPIDER_EYE)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.SPIDER_LEG), has(ModItems.SPIDER_LEG))
                        .unlockedBy(getHasName(Items.SPIDER_EYE), has(Items.SPIDER_EYE))
                        .save(output);

                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

                // Ranged weapon data gens

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
                        .pattern("aba")
                        .pattern("cdc")
                        .pattern(" e ")
                        .define('a', ModItems.SPIDER_LEG)
                        .define('b', Items.SPIDER_EYE)
                        .define('c', ModItems.SILK_SPOOL)
                        .define('d', Items.CROSSBOW)
                        .define('e', ModItems.SPIDER_CARAPACE)
                        .unlockedBy(getHasName(ModItems.SPIDER_LEG), has(ModItems.SPIDER_LEG))
                        .unlockedBy(getHasName(Items.SPIDER_EYE), has(Items.SPIDER_EYE))
                        .unlockedBy(getHasName(ModItems.SILK_SPOOL), has(ModItems.SILK_SPOOL))
                        .unlockedBy(getHasName(Items.CROSSBOW), has(Items.CROSSBOW))
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .save(output);

                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

                // Food data gens

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