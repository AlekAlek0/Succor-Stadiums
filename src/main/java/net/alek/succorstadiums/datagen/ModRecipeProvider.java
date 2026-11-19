package net.alek.succorstadiums.datagen;

import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;

import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.item.ModItems;

// ModRecipeProvider class
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
                        .define('b', ModItems.GRAY_SPIDER_EYE)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_EYE), has(ModItems.GRAY_SPIDER_EYE))
                        .save(output);

                // Arachno Chestplate Recipe
                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CARAPACE_CHESTPLATE, 1)
                        .pattern("aca")
                        .pattern("bab")
                        .pattern("aaa")
                        .define('a', ModItems.SPIDER_CARAPACE)
                        .define('b', ModItems.GRAY_SPIDER_LEG)
                        .define('c', ModItems.GRAY_SPIDER_EYE)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_LEG), has(ModItems.GRAY_SPIDER_LEG))
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_EYE), has(ModItems.GRAY_SPIDER_EYE))
                        .save(output);

                // Arachno Leggings Recipe
                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CARAPACE_LEGGINGS, 1)
                        .pattern("aaa")
                        .pattern("bcb")
                        .pattern("b b")
                        .define('a', ModItems.SPIDER_CARAPACE)
                        .define('b', ModItems.GRAY_SPIDER_LEG)
                        .define('c', ModItems.GRAY_SPIDER_EYE)
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_LEG), has(ModItems.GRAY_SPIDER_LEG))
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_EYE), has(ModItems.GRAY_SPIDER_EYE))
                        .save(output);

                // Arachno Boots Recipe
                shaped(RecipeCategory.COMBAT, ModItems.ARACHNO_CARAPACE_BOOTS, 1)
                        .pattern("a a")
                        .pattern("b b")
                        .pattern("c c")
                        .define('a', ModItems.GRAY_SPIDER_EYE)
                        .define('b', ModItems.SPIDER_CARAPACE)
                        .define('c', ModItems.GRAY_SPIDER_LEG)
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_EYE), has(ModItems.GRAY_SPIDER_EYE))
                        .unlockedBy(getHasName(ModItems.SPIDER_CARAPACE), has(ModItems.SPIDER_CARAPACE))
                        .unlockedBy(getHasName(ModItems.SPIDER_LEG), has(ModItems.SPIDER_LEG))
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
                        .pattern(" a ")
                        .define('a', ModItems.GRAY_SPIDER_LEG)
                        .define('b', ModItems.GRAY_SPIDER_EYE)
                        .define('c', ModItems.PLANT_FIBER)
                        .define('d', ModItems.SPIDER_CARAPACE)
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_LEG), has(ModItems.GRAY_SPIDER_LEG))
                        .unlockedBy(getHasName(ModItems.GRAY_SPIDER_EYE), has(ModItems.GRAY_SPIDER_EYE))
                        .unlockedBy(getHasName(ModItems.PLANT_FIBER), has(ModItems.PLANT_FIBER))
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

                // Cooked Gray Spider Leg Recipes (furnace, smoker, campfire)
                simpleCookingRecipe("smelting", SmeltingRecipe::new, 180, ModItems.GRAY_SPIDER_LEG, ModItems.ROASTED_GRAY_SPIDER_LEG, 0F);
                simpleCookingRecipe("smoking", SmokingRecipe::new, 90, ModItems.GRAY_SPIDER_LEG, ModItems.ROASTED_GRAY_SPIDER_LEG, 0F);
                simpleCookingRecipe("campfire_cooking", CampfireCookingRecipe::new, 540, ModItems.GRAY_SPIDER_LEG, ModItems.ROASTED_GRAY_SPIDER_LEG, 0F);
            }
        };
    }

    @Override
    public @NonNull String getName() {
        return "ModRecipeProvider";
    }
}