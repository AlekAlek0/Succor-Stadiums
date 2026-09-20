package net.alek.succorstadiums.datagen;

import net.minecraft.core.HolderLookup;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;

import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.item.ModItems;

// ModEnglishLangProvider class
public class ModEnglishLangProvider extends FabricLanguageProvider {
    public ModEnglishLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {

        // Specify en_us
        super(dataOutput, "en_us", registryLookup);
    }

    // Generate translations
    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider holderLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {

        // Spacer translation
        translationBuilder.add("item.succorstadiums.spacer", "");

        // Creative Mode Tab translations
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_items", "Succor Stadium Items");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_armor", "Succor Stadium Armor");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_melee", "Succor Stadium Melee");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_ranged", "Succor Stadium Ranged");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_magic", "Succor Stadium Magic");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_trinkets", "Succor Stadium Trinkets");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_foods", "Succor Stadium Food");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_equipment", "Succor Stadium Equipment");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_summons", "Succor Stadium Summons");

        // Item translations
        translationBuilder.add(ModItems.BRENNON_ORE, "Brennon Ore");
        translationBuilder.add(ModItems.SILVER_INGOT, "Silver Ingot");
        translationBuilder.add(ModItems.EMERALD_COIN, "Emerald Coin");
        translationBuilder.add(ModItems.SILK_SPOOL, "Silk Spool");
        translationBuilder.add(ModItems.SILK_WEAVE, "Silk Weave");
        translationBuilder.add(ModItems.BONE_BROTH, "Bone Broth");
        translationBuilder.add(ModItems.SPIDER_CARAPACE, "Spider Carapace");
        translationBuilder.add(ModItems.SPIDER_LEG, "Spider Leg");
        translationBuilder.add(ModItems.BANANA_SLIME_BALL, "Banana Slime Ball");
        translationBuilder.add(ModItems.BANANA_BRANCH, "Banana Branch");

        // Armor translations
        translationBuilder.add(ModItems.BALE_HELMET, "§fBale Bucket");
        translationBuilder.add(ModItems.BALE_CHESTPLATE, "§fBale Target");
        translationBuilder.add(ModItems.BALE_LEGGINGS, "§fBale Leggings");
        translationBuilder.add(ModItems.BALE_BOOTS, "§fBale Boots");
        translationBuilder.add(ModItems.ARACHNO_CARAPACE_HELMET, "§4Arachno Carapace Helmet");
        translationBuilder.add(ModItems.ARACHNO_CARAPACE_CHESTPLATE, "§4Arachno Carapace Chestplate");
        translationBuilder.add(ModItems.ARACHNO_CARAPACE_LEGGINGS, "§4Arachno Carapace Leggings");
        translationBuilder.add(ModItems.ARACHNO_CARAPACE_BOOTS, "§4Arachno Carapace Boots");
        translationBuilder.add(ModItems.NANNER_WATER_WADERS, "Nanner Water Waders");
        translationBuilder.add(ModItems.HELM_OF_THE_FOREST, "§2Helm Of The Forest");
        translationBuilder.add(ModItems.CHESTPLATE_OF_THE_FOREST, "§2Chestplate Of The Forest");
        translationBuilder.add(ModItems.LEGGINGS_OF_THE_FOREST, "§2Leggings Of The Forest");
        translationBuilder.add(ModItems.BOOTS_OF_THE_FOREST, "§2Boots Of The Forest");

        // Melee Weapon translations
        translationBuilder.add(ModItems.BEAN_POLE, "10ft Beanpole");
        translationBuilder.add(ModItems.BONE_DAGGER, "Bone Dagger");
        translationBuilder.add(ModItems.BANANNER_BLADE, "Bananner Blade");
        translationBuilder.add(ModItems.FUMBLEBRINGER_FORK, "Fumblebringer Fork");
        translationBuilder.add(ModItems.GREAT_SWORD_OF_THE_FOREST, "Great Sword of The Forest");
        translationBuilder.add(ModItems.SWORD_OF_THE_FOREST, "§2Sword of the Forest");
        translationBuilder.add(ModItems.SPROUT_SICKLE, "Sprout Sickle");
        translationBuilder.add(ModItems.OAK_SWORD, "Oak Sword");

        // Magic Weapon translations
        translationBuilder.add(ModItems.FIRECHARGED_CANE, "§4Firecharged Cane");
        translationBuilder.add(ModItems.AQUAONDUIT, "§3Aquaonduit");

        // Ranged Weapon translations
        translationBuilder.add(ModItems.BOWNANA, "§eBownana");
        translationBuilder.add(ModItems.ARACHNO_CROSSBOW, "§4Arachno Crossbow");
        translationBuilder.add(ModItems.CREEPBOW, "Creepbow");
        translationBuilder.add(ModItems.RAZOR_THORN, "Razor Thorn");
        translationBuilder.add(ModItems.BALE_ARROW, "Bale Arrow");

        // Food translations
        translationBuilder.add(ModItems.GHRAMBLE_BAPPLE, "Ghramble Bapple");
        translationBuilder.add(ModItems.CREEPER_SALVE, "Creeper Salve");
        translationBuilder.add(ModItems.MANA_PASTE, "Mana Paste");
        translationBuilder.add(ModItems.MAGIC_FLESH, "Magic Flesh");
        translationBuilder.add(ModItems.PLANT_POWDER, "Plant Powder");
        translationBuilder.add(ModItems.BEEF_STEW, "Beef Stew");
        translationBuilder.add(ModItems.CHICKEN_STEW, "Chicken Stew");
        translationBuilder.add(ModItems.PORK_STEW, "Pork Stew");
        translationBuilder.add(ModItems.MUTTON_STEW, "Mutton Stew");
        translationBuilder.add(ModItems.RABBIT_STEW, "Rabbit Stew");
        translationBuilder.add(ModItems.ROTTEN_STEW, "Rotten Stew");

        // Trinket translations
        translationBuilder.add(ModItems.FLINT_CHARM, "Flint Charm");
        translationBuilder.add(ModItems.RESURRECTION_AMULET, "Resurrection Amulet");
        translationBuilder.add(ModItems.DOG_WHISTLE, "Dog Whistle");

        // Enchantment translations
        translationBuilder.add("enchantment.succorstadiums.vipers_bite", "Vipers Bite");
        translationBuilder.add("enchantment.succorstadiums.rose_thorn", "Rose Thorn");
        translationBuilder.add("enchantment.succorstadiums.swiftness", "Swiftness");

        // Mob Effect translations
        translationBuilder.add("effect.succorstadiums.paralysis", "Paralysis");

        // Gameplay message translations
        translationBuilder.add("death.attack.plant_powder_1", "%1$s tried to consume plant powder");
        translationBuilder.add("death.attack.plant_powder_2", "%1$s just wanted to know what it would do");
        translationBuilder.add("message.succorstadiums.mana_paste.mana_full", "§cMana is already full");
        translationBuilder.add("message.succorstadiums.not_enough_mana", "§cInsufficient Mana");
        translationBuilder.add("message.succorstadiums.arachno_double_jump.no_spider_leg", "§cNo spider leg ammo in inventory");

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Item Lore translations
        translationBuilder.add("item.succorstadiums.brennon_ore.lore", "Finally!");
        translationBuilder.add("item.succorstadiums.silver_ingot.lore", "Shiny!");
        translationBuilder.add("item.succorstadiums.emerald_coin.lore", "Common tender in the plains.");
        translationBuilder.add("item.succorstadiums.silk_spool.lore", "Used to make the Spider Silk Bow and Silkweave.");
        translationBuilder.add("item.succorstadiums.silk_weave.lore", "Used to make the Silkweave armor set.");
        translationBuilder.add("item.succorstadiums.bone_broth.lore", "Looks inedible, but may have a use...");
        translationBuilder.add("item.succorstadiums.spider_carapace.lore", "Part of a Spiders tough exterior, the possible applications for such an item are endless.");
        translationBuilder.add("item.succorstadiums.spider_leg.lore", "Still wriggling");
        translationBuilder.add("item.succorstadiums.banana_slime_ball.lore", "Ballnana");
        translationBuilder.add("item.succorstadiums.banana_branch.lore", "The Banana must remain unharmed");

        // Item Tooltip translations
        //--------------------------

        // Armor Lore translations
        translationBuilder.add("item.succorstadiums.bale_helmet.lore", "Why a bale bucket you may ask... well it sounded nice.");
        translationBuilder.add("item.succorstadiums.bale_chestplate.lore", "Aim for the target.");
        translationBuilder.add("item.succorstadiums.bale_leggings.lore", "All sneaky beaky like.");
        translationBuilder.add("item.succorstadiums.bale_boots.lore", "Good for a short fall.");
        translationBuilder.add("item.succorstadiums.arachno_carapace_helmet.lore", "These Eyes have seen a lot.");
        translationBuilder.add("item.succorstadiums.arachno_carapace_chestplate.lore", "Made of 100% unethically sourced Spider Carapace.");
        translationBuilder.add("item.succorstadiums.arachno_carapace_leggings.lore", "Spiderlegs would have been a cooler name... just sayin.");
        translationBuilder.add("item.succorstadiums.arachno_carapace_boots.lore", "Spider Boots? But spiders don't wear boots!");
        translationBuilder.add("item.succorstadiums.nanner_water_waders.lore", "Helps you traverse mucky terrain with the greatest of ease.");
        translationBuilder.add("item.succorstadiums.helm_of_the_forest.lore", "Helmet of a Forest Guardian.");
        translationBuilder.add("item.succorstadiums.chestplate_of_the_forest.lore", "Chestplate of a Forest Guardian.");
        translationBuilder.add("item.succorstadiums.leggings_of_the_forest.lore", "Leggings of a Forest Guardian.");
        translationBuilder.add("item.succorstadiums.boots_of_the_forest.lore", "Boots of a Forest Guardian.");

        // Armor Tooltip translations
        translationBuilder.add("item.succorstadiums.arachno_carapace_armor.tooltip_0", "Full Set Bonus:");
        translationBuilder.add("item.succorstadiums.arachno_carapace_armor.tooltip_1", "+1 Mid-air Jump");
        translationBuilder.add("item.succorstadiums.arachno_carapace_armor.tooltip_2", " Consumes 4 Mana");
        translationBuilder.add("item.succorstadiums.nanner_water_waders.tooltip", "Increased Speed on Soul Sand / Soil, Mud and Underwater.");
        translationBuilder.add("item.succorstadiums.armor_of_the_forest.tooltip_0", "Full Set Bonus:");
        translationBuilder.add("item.succorstadiums.armor_of_the_forest.tooltip_1", " 20% Knockback Resist");


        // Melee Weapon Lore translations
        translationBuilder.add("item.succorstadiums.bean_pole.lore", "I wouldn't even touch you with a...");
        translationBuilder.add("item.succorstadiums.bone_dagger.lore", "3 inches is actually pretty big..");
        translationBuilder.add("item.succorstadiums.bananner_blade.lore", "All though it resembles the banana fruit this sturdy blade is actually made of from Baldnana wood.");
        translationBuilder.add("item.succorstadiums.fumblebringer_fork.lore", "This weapon was once used to bring upon \"The Great Fumbling\".");
        translationBuilder.add("item.succorstadiums.sword_of_the_forest.lore", "Recommended for big hoards.");
        translationBuilder.add("item.succorstadiums.sprout_sickle.lore", "Inflicts Paralysis…");
        translationBuilder.add("item.succorstadiums.oak_sword.lore", "Your first proper weapon.");

        // Melee Weapon Tooltip translations
        translationBuilder.add("item.succorstadiums.bone_dagger.tooltip_0", " 2x Crit Damage");
        translationBuilder.add("item.succorstadiums.sword_of_the_forest.tooltip_0", "Armor of the Forest Bonus:");
        translationBuilder.add("item.succorstadiums.sword_of_the_forest.tooltip_1", "+1 Attack Damage");
        translationBuilder.add("item.succorstadiums.sword_of_the_forest.tooltip_2", "  Poison I | 0:04 | 25%");

        translationBuilder.add("item.succorstadiums.sprout_sickle.tooltip_0", "Paralysis | 0:03 | 8%");

        // Magic Weapon Lore translations
        translationBuilder.add("item.succorstadiums.fire_staff.lore", "Fireball.");
        translationBuilder.add("item.succorstadiums.aqua_staff.lore", "Slows both movement and fall speed.");

        // Magic Weapon Tooltip translations
        translationBuilder.add("item.succorstadiums.fire_staff.tooltip_0", "When Used:");
        translationBuilder.add("item.succorstadiums.fire_staff.tooltip_1", " 3 Magic Damage");
        translationBuilder.add("item.succorstadiums.fire_staff.tooltip_2", " 1.5s Cooldown");
        translationBuilder.add("item.succorstadiums.fire_staff.tooltip_3", "-4 Mana");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_0", "When Used:");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_1", " 0 Magic Damage");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_2", " 12s Cooldown");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_3", " 12s Ring Duration");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_4", " Slow Falling I | 00:15 | 100%");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_5", " Slowness II | 00:15 | 100%");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_6", "-7 Mana");

        //--------------------------

        // Ranged Weapon Lore translations
        translationBuilder.add("item.succorstadiums.bownana.lore", "Nana Nana.");
        translationBuilder.add("item.succorstadiums.arachno_crossbow.lore", "Slow but powerful, a promising ranged option.");
        translationBuilder.add("item.succorstadiums.razor_thorn.lore", "Just a sharp branch.");
        translationBuilder.add("item.succorstadiums.bale_arrow.lore", "Knockback.");

        // Ranged Weapon Tooltip translations
        translationBuilder.add("item.succorstadiums.razor_thorn.tooltip_0", "1% Chance for free a thorn.");
        translationBuilder.add("item.succorstadiums.razor_thorn.tooltip_1", "When Thrown:");
        translationBuilder.add("item.succorstadiums.razor_thorn.tooltip_2", " 4 Attack Damage 3x");

        // Food Lore translations
        translationBuilder.add("item.succorstadiums.ghramble_bapple.lore", "Ghramble is my favorite bapple.");
        translationBuilder.add("item.succorstadiums.mana_paste.lore", "Restores mana in seconds.");
        translationBuilder.add("item.succorstadiums.plant_powder.lore", "DO NOT CONSUME");
        translationBuilder.add("item.succorstadiums.rotten_stew.lore", "Cafeteria slop.");

        // Food Tooltip translations
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_0", "Resistance II | 0:45 | 30%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_1", "Health Boost I | 0:30 | 10%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_2", "Regeneration I | 0:15 | 75%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_3", "Slowness IV | 0:14 | 30%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_4", "Weakness I | 0:12 | 65%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_5", "Paralysis I | 0:08 | 8%");
        translationBuilder.add("item.succorstadiums.chicken_stew.tooltip", "\uD83C\uDF56 Hunger I | 0:20 | 10%");
        translationBuilder.add("item.succorstadiums.rotten_stew.tooltip", "\uD83C\uDF56 Hunger II | 0:20 | 30%");

        // Trinket Lore translations
        translationBuilder.add("item.succorstadiums.flint_charm.lore", "Flint Charm? I just don't see it.");
        translationBuilder.add("item.succorstadiums.resurrection_amulet.lore", "I mean the name tells you all you need to know.");
        translationBuilder.add("item.succorstadiums.dog_whistle.lore", "Summons 3 doggies to help you fight!");

        // Trinket Tooltip translations
        translationBuilder.add("item.succorstadiums.flint_charm.tooltip_0", "When in Off Hand:");
        translationBuilder.add("item.succorstadiums.flint_charm.tooltip_1", " +1 Melee Attack Damage");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_0", "When Used:");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_1", " 1.5 Summon Damage");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_2", " 30s Summon Duration");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_3", " 10s Cooldown");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_4", "-8 Mana");

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Item entity translations
        translationBuilder.add("entity.succorstadiums.razor_thorn", "Razor Thorn");
        translationBuilder.add("entity.succorstadiums.bale_arrow", "Bale Arrow");

        // Entity translations
        translationBuilder.add("entity.succorstadiums.banana_slime", "Banana Slime");
        translationBuilder.add("entity.succorstadiums.farmbie", "Farmbie");
        translationBuilder.add("entity.succorstadiums.farmbie_blue", "Farmbie Blue");
        translationBuilder.add("entity.succorstadiums.grass_creeper", "Grass Creeper");
        translationBuilder.add("entity.succorstadiums.skelcrow", "Skelcrow");

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Mod Keybindings translations
        translationBuilder.add("key.category.succorstadiums.general", "Succor Stadiums");
        translationBuilder.add("key.succorstadiums.open_config", "Open Config Screen");
        translationBuilder.add("key.succorstadiums.open_mob_arena", "Open Mob Arena GUI");
        translationBuilder.add("key.succorstadiums.open_backpack", "Open Backpack");

        // Mod Backpack container translations
        translationBuilder.add("container.succorstadiums.backpack", "Backpack");

    }
}