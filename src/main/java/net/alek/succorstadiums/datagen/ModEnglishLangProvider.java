package net.alek.succorstadiums.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import net.alek.succorstadiums.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModEnglishLangProvider extends FabricLanguageProvider {
    public ModEnglishLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        // Specifying en_us is optional, as it's the default language code
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider holderLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {

        // Spacer translation
        translationBuilder.add("item.succorstadiums.spacer", "");


        // Creative Mode Tab translations
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_items", "Succor Stadium Items");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_armor", "Succor Stadium Armor");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_melee", "Succor Stadium Melee");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_ranged", "Succor Stadium Ranged");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_magic", "Succor Stadium Magic");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_foods", "Succor Stadium Food");
        translationBuilder.add("creativemodetab.succorstadiums.succor_stadium_trinkets", "Succor Stadium Trinkets");

        // Item translations
        translationBuilder.add(ModItems.BRENNON_ORE, "Brennon Ore");
        translationBuilder.add(ModItems.SILVER_INGOT, "Silver Ingot");
        translationBuilder.add(ModItems.SILK_SPOOL, "Silk Spool");
        translationBuilder.add(ModItems.SILK_WEAVE, "Silk Weave");
        translationBuilder.add(ModItems.BONE_BROTH, "Bone Broth");
        translationBuilder.add(ModItems.SPIDER_CARAPACE, "Spider Carapace");
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
        translationBuilder.add("enchantment.succorstadiums.viper_bite", "Viper Bite");


        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


        // Item Tooltip translations
        translationBuilder.add("item.succorstadiums.brennon_ore.tooltip", "Finally!");
        translationBuilder.add("item.succorstadiums.silver_ingot.tooltip", "Shiny!");
        translationBuilder.add("item.succorstadiums.silk_spool.tooltip", "Used to make the Spider Silk Bow and Silkweave.");
        translationBuilder.add("item.succorstadiums.silk_weave.tooltip", "Used to make the Silkweave armor set.");
        translationBuilder.add("item.succorstadiums.bone_broth.tooltip", "Looks inedible, but may have a use...");
        translationBuilder.add("item.succorstadiums.spider_carapace.tooltip", "Part of a Spiders tough exterior, the possible applications for such an item are endless.");

        // Armor Tooltip translations
        translationBuilder.add("item.succorstadiums.bale_helmet.tooltip", "Why a bale bucket you may ask... well it sounded nice.");
        translationBuilder.add("item.succorstadiums.bale_chestplate.tooltip", "Aim for the target.");
        translationBuilder.add("item.succorstadiums.bale_leggings.tooltip", "All sneaky beaky like.");
        translationBuilder.add("item.succorstadiums.bale_boots.tooltip", "Good for a short fall.");

        translationBuilder.add("item.succorstadiums.arachno_carapace_armor.tooltip_0", "Full Set Bonus:");
        translationBuilder.add("item.succorstadiums.arachno_carapace_armor.tooltip_1", "+1 Mid-air Jump");
        translationBuilder.add("item.succorstadiums.arachno_carapace_helmet.tooltip", "These Eyes have seen a lot.");
        translationBuilder.add("item.succorstadiums.arachno_carapace_chestplate.tooltip", "Made of 100% unethically sourced Spider Carapace.");
        translationBuilder.add("item.succorstadiums.arachno_carapace_leggings.tooltip", "Spiderlegs would have been a cooler name... just sayin.");
        translationBuilder.add("item.succorstadiums.arachno_carapace_boots.tooltip", "Spider Boots? But spiders don't wear boots!");

        translationBuilder.add("item.succorstadiums.nanner_water_waders.tooltip_0", "Helps you traverse mucky terrain with the greatest of ease.");
        translationBuilder.add("item.succorstadiums.nanner_water_waders.tooltip_1", "Increased Speed on Soul Sand / Soil, Mud and Underwater.");

        // Melee Weapon Tooltip translations
        translationBuilder.add("item.succorstadiums.bean_pole.tooltip", "I wouldn't even touch you with a...");
        translationBuilder.add("item.succorstadiums.bone_dagger.tooltip", "3 inches is actually pretty big..");
        translationBuilder.add("item.succorstadiums.bananner_blade.tooltip", "All though it resembles the banana fruit this sturdy blade is actually made of from Baldnana wood.");
        translationBuilder.add("item.succorstadiums.fumblebringer_fork.tooltip", "This weapon was once used to bring apon \"The Great Fumbling\".");

        // Magic Weapon Tooltip translations
        translationBuilder.add("item.succorstadiums.fire_staff.tooltip", "Absolute Flames.");
        translationBuilder.add("item.succorstadiums.aqua_staff.tooltip", "A staff that shoots out a circle inflicting slowness and slow falling.");

        // Ranged Weapon Tooltip translations
        translationBuilder.add("item.succorstadiums.bownana.tooltip", "Nana Nana.");
        translationBuilder.add("item.succorstadiums.arachno_crossbow.tooltip", "Slow but powerful, a promising ranged option.");

        // Food Tooltip translations
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_0", "Ghramble is my favorite bapple.");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_1", "+ Resistance II | 0:45 | 30%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_2", "+ Regeneration I | 0:15 | 85%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_3", "+ Health Boost I | 0:15 | 5%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_4", "- Weakness I | 0:08 | 75%");
        translationBuilder.add("item.succorstadiums.ghramble_bapple.tooltip_5", "- Slowness IV | 0:10 | 35%");
        translationBuilder.add("item.succorstadiums.rotten_stew.tooltip_0", "Cafeteria slop.");
        translationBuilder.add("item.succorstadiums.rotten_stew.tooltip_1", "- Hunger II | 0:20 | 30%");

        // Trinket Tooltip translations
        translationBuilder.add("item.succorstadiums.flint_charm.tooltip", "Flint Charm? I just don't see it.");
        translationBuilder.add("item.succorstadiums.resurrection_amulet.tooltip", "I mean the name tells you all you need to know.");
        translationBuilder.add("item.succorstadiums.dog_whistle.tooltip", "Summons 4 doggies to help you fight!");


        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        // Entity translations
        translationBuilder.add("entity.succorstadiums.banana_slime", "Banana Slime");

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
