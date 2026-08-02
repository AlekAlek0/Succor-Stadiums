package net.alek.succorstadiums.datagen;

import net.minecraft.core.HolderLookup;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;

import net.alek.succorstadiums.item.ModItems;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;

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

        // Melee Weapon translations
        translationBuilder.add(ModItems.BEAN_POLE, "10ft Beanpole");
        translationBuilder.add(ModItems.BONE_DAGGER, "Bone Dagger");
        translationBuilder.add(ModItems.BANANNER_BLADE, "Bananner Blade");
        translationBuilder.add(ModItems.FUMBLEBRINGER_FORK, "Fumblebringer Fork");
        translationBuilder.add(ModItems.SWORD_OF_THE_FOREST, "Sword of the Forest");


        // Magic Weapon translations
        translationBuilder.add(ModItems.FIRE_STAFF, "§4Firecharged Cane");
        translationBuilder.add(ModItems.AQUA_STAFF, "§3Aquaonduit");

        // Ranged Weapon translations
        translationBuilder.add(ModItems.BOWNANA, "§eBownana");
        translationBuilder.add(ModItems.ARACHNO_CROSSBOW, "§4Arachno Crossbow");

        // Food translations
        translationBuilder.add(ModItems.GRAMBLE_BAPPLE, "Ghramble Bapple");
        translationBuilder.add(ModItems.ROTTEN_STEW, "Rotten Stew");

        // Trinket translations
        translationBuilder.add(ModItems.FLINT_CHARM, "Flint Charm");
        translationBuilder.add(ModItems.RESURRECTION_AMULET, "Resurrection Amulet");
        translationBuilder.add(ModItems.DOG_WHISTLE, "Dog Whistle");

        // Enchantment translations
        translationBuilder.add("enchantment.succorstadiums.vipers_bite", "Vipers Bite");


        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


        // Item Lore translations
        translationBuilder.add("item.succorstadiums.brennon_ore.lore", "Finally!");
        translationBuilder.add("item.succorstadiums.silver_ingot.lore", "Shiny!");
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

        // Armor Tooltip translations
        translationBuilder.add("item.succorstadiums.arachno_carapace_armor.tooltip_0", "Full Set Bonus:");
        translationBuilder.add("item.succorstadiums.arachno_carapace_armor.tooltip_1", "+1 Mid-air Jump");
        translationBuilder.add("item.succorstadiums.nanner_water_waders.tooltip", "Increased Speed on Soul Sand / Soil, Mud and Underwater.");

        // Melee Weapon Lore translations
        translationBuilder.add("item.succorstadiums.bean_pole.lore", "I wouldn't even touch you with a...");
        translationBuilder.add("item.succorstadiums.bone_dagger.lore", "3 inches is actually pretty big..");
        translationBuilder.add("item.succorstadiums.bananner_blade.lore", "All though it resembles the banana fruit this sturdy blade is actually made of from Baldnana wood.");
        translationBuilder.add("item.succorstadiums.fumblebringer_fork.lore", "This weapon was once used to bring upon \"The Great Fumbling\".");
        translationBuilder.add("item.succorstadiums.sword_of_the_forest.lore", "Recommended for big hoards.");

        // Melee Weapon Tooltip translations
        //--------------------------

        // Magic Weapon Lore translations
        translationBuilder.add("item.succorstadiums.fire_staff.lore", "Fireball.");
        translationBuilder.add("item.succorstadiums.aqua_staff.lore", "Slows both movement and fall speed.");

        // Magic Weapon Tooltip translations
        translationBuilder.add("item.succorstadiums.fire_staff.tooltip_0", "When Used:");
        translationBuilder.add("item.succorstadiums.fire_staff.tooltip_1", "⏳ Cooldown: 00:05");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_0", "When Used:");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_1", "⏳ Cooldown: 00:12");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip_2", "⌚ Ring Duration: 00:03");
        //--------------------------

        // Ranged Weapon Lore translations
        translationBuilder.add("item.succorstadiums.bownana.lore", "Nana Nana.");
        translationBuilder.add("item.succorstadiums.arachno_crossbow.lore", "Slow but powerful, a promising ranged option.");

        // Ranged Weapon Tooltip translations
        //--------------------------

        // Food Lore translations
        translationBuilder.add("item.succorstadiums.ghramble_bapple.lore", "Ghramble is my favorite bapple.");
        translationBuilder.add("item.succorstadiums.rotten_stew.lore", "Cafeteria slop.");

        // Food Tooltip translations
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_0", "\uD83D\uDEE1 Resistance II | 0:45 | 30%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_1", "❤ Regeneration I | 0:15 | 85%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_2", "+ Health Boost I | 0:15 | 5%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_3", "- Weakness I | 0:08 | 75%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_4", "⚓ Slowness IV | 0:10 | 35%");
        translationBuilder.add("item.succorstadiums.rotten_stew.tooltip", "\uD83C\uDF56 Hunger II | 0:20 | 30%");

        // Trinket Lore translations
        translationBuilder.add("item.succorstadiums.flint_charm.lore", "Flint Charm? I just don't see it.");
        translationBuilder.add("item.succorstadiums.resurrection_amulet.lore", "I mean the name tells you all you need to know.");
        translationBuilder.add("item.succorstadiums.dog_whistle.lore", "Summons 5 doggies to help you fight!");

        // Trinket Tooltip translations
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_0", "When Used:");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_1", "⏳ Cooldown: 0:35");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_2", "⌚ Despawn: 0:30");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_3", "❤ Health: 2.5 ");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip_4", "⚔ Damage: 1 ");

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Entity translations
        translationBuilder.add("entity.succorstadiums.banana_slime", "Banana Slime");
        translationBuilder.add("entity.succorstadiums.zombie_farmer", "Zombie Farmer");
        translationBuilder.add("entity.succorstadiums.grass_creeper", "Grass Creeper");

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Mod Keybindings translations

        translationBuilder.add("key.category.succorstadiums.general", "Succor Stadiums");
        translationBuilder.add("key.succorstadiums.open_backpack", "Open Backpack");
        translationBuilder.add("key.succorstadiums.open_mob_arena", "Open Mob Arena GUI");

        // Mod Sounds Subtitle translations
        translationBuilder.add("sound.succorstadiums.aqua_staff_use", "§3Aquaonduit used");
        translationBuilder.add("sound.succorstadiums.fire_staff_use", "§4Firecharged Cane used");
        translationBuilder.add("sound.succorstadiums.arachno_carapace_armor_equip", "§4Arachno Carpace Armor equips");

        // Mod Backpack container translations

        translationBuilder.add("container.succorstadiums.backpack", "Backpack");

    }
}
