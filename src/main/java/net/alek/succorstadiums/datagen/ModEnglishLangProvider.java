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
    public void generateTranslations(HolderLookup.@NonNull Provider holderLookup, FabricLanguageProvider.@NonNull TranslationBuilder translationBuilder) {

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
        translationBuilder.add(ModItems.GRAY_SPIDER_EYE, "Gray Spider Eye");
        translationBuilder.add(ModItems.GRAY_SPIDER_LEG, "Gray Spider Leg");
        translationBuilder.add(ModItems.PLANT_FIBER, "Plant Fiber");

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

        // Ranged Weapon translations
        translationBuilder.add(ModItems.OAK_BOW, "Oak Bow");
        translationBuilder.add(ModItems.BOWNANA, "§eBownana");
        translationBuilder.add(ModItems.ARACHNO_CROSSBOW, "§4Arachno Crossbow");
        translationBuilder.add(ModItems.CREEPBOW, "Creepbow");
        translationBuilder.add(ModItems.RAZOR_THORN, "Razor Thorn");
        translationBuilder.add(ModItems.BALE_ARROW, "Bale Arrow");

        // Magic Weapon translations
        translationBuilder.add(ModItems.FIRECHARGED_CANE, "§4Firecharged Cane");
        translationBuilder.add(ModItems.AQUAONDUIT, "§3Aquaonduit");

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
        translationBuilder.add(ModItems.ROASTED_GRAY_SPIDER_LEG, "Roasted Gray Spider Leg");

        // Trinket translations
        translationBuilder.add(ModItems.FLINT_CHARM, "Flint Charm");
        translationBuilder.add(ModItems.RESURRECTION_AMULET, "Resurrection Amulet");
        translationBuilder.add(ModItems.ROSEBOUND_TOME, "Rosebound Tome");

        // Summons translations
        translationBuilder.add(ModItems.SUMMONERS_CANE, "Summoners Cane");
        translationBuilder.add(ModItems.DOG_WHISTLE, "Dog Whistle");

        // Equipment translations
        translationBuilder.add(ModItems.PLAINS_COIN_POUCH, "Plains Coin Pouch");
        translationBuilder.add(ModItems.CARDBOARD_SHIELD, "Cardboard Shield");

        // Item lore and tooltip translations
        ItemTextTranslations.generate(translationBuilder);

        // Enchantment translations
        translationBuilder.add("enchantment.succorstadiums.vipers_bite", "Vipers Bite");
        translationBuilder.add("enchantment.succorstadiums.rose_thorn", "Rose Thorn");
        translationBuilder.add("enchantment.succorstadiums.swiftness", "Swiftness");

        // Mob Effect translations
        translationBuilder.add("effect.succorstadiums.paralysis", "Paralysis");
        translationBuilder.add("effect.succorstadiums.mana_sickness", "Mana Sickness");

        // Gameplay message translations
        translationBuilder.add("death.attack.plant_powder_1", "%1$s tried to consume plant powder");
        translationBuilder.add("death.attack.plant_powder_2", "%1$s just wanted to know what it would do");
        translationBuilder.add("message.succorstadiums.mana_full", "§cMana is already full");
        translationBuilder.add("message.succorstadiums.not_enough_mana", "§cInsufficient Mana");
        translationBuilder.add("message.succorstadiums.no_coins_to_deposit", "§cNo coins left to deposit in pouch");
        translationBuilder.add("message.succorstadiums.no_coins_to_withdraw", "§cNo coins left to withdraw from pouch");
        translationBuilder.add("message.succorstadiums.no_target_in_view", "§cNo entity in view to target");
        translationBuilder.add("message.succorstadiums.no_summons", "§cYou have no summons spawned");
        translationBuilder.add("message.succorstadiums.offhand_only", "§cMust be held in the offhand");

        // Item entity translations
        translationBuilder.add("entity.succorstadiums.razor_thorn", "Razor Thorn");
        translationBuilder.add("entity.succorstadiums.bale_arrow", "Bale Arrow");

        // Entity translations
        translationBuilder.add("entity.succorstadiums.banana_slime", "Banana Slime");
        translationBuilder.add("entity.succorstadiums.farmbie", "Farmbie");
        translationBuilder.add("entity.succorstadiums.farmbie_blue", "Farmbie Blue");
        translationBuilder.add("entity.succorstadiums.grass_creeper", "Grass Creeper");
        translationBuilder.add("entity.succorstadiums.skelcrow", "Skelcrow");
        translationBuilder.add("entity.succorstadiums.gray_spider", "Gray Spider");

        // Mod Keybindings translations
        translationBuilder.add("key.category.succorstadiums.general", "Succor Stadiums");
        translationBuilder.add("key.succorstadiums.open_config", "Open Config Screen");
        translationBuilder.add("key.succorstadiums.open_mob_arena", "Open Mob Arena GUI");
        translationBuilder.add("key.succorstadiums.open_backpack", "Open Backpack");
        translationBuilder.add("key.succorstadiums.coin.pouch_transfer_modifier", "Coin Pouch Transfer Modifier");

        // Mod container translations
        translationBuilder.add("container.succorstadiums.backpack", "Backpack");
        translationBuilder.add("container.succorstadiums.plains_coin_pouch", "Plains Coin Pouch");

        // Mod button translations
        translationBuilder.add("button.succorstadiums.withdraw_coins", "Withdraw Coins");

    }
}