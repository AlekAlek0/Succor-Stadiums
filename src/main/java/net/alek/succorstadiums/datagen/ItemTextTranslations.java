package net.alek.succorstadiums.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import java.util.List;

// ItemTextTranslations class
public final class ItemTextTranslations {

    // Create the prefix and spacer strings
    private static final String PREFIX = "item.succorstadiums.";
    private static final String SP = "";

    // Create a list of strings for each armor set
    private static final List<String> BALE_ARMOR = List.of("bale_helmet", "bale_chestplate", "bale_leggings", "bale_boots");
    private static final List<String> ARACHNO_ARMOR = List.of(
            "arachno_carapace_helmet", "arachno_carapace_chestplate", "arachno_carapace_leggings", "arachno_carapace_boots");
    private static final List<String> FOREST_ARMOR = List.of(
            "helm_of_the_forest", "chestplate_of_the_forest", "leggings_of_the_forest", "boots_of_the_forest");

    private ItemTextTranslations() {}

    private static void lore(FabricLanguageProvider.TranslationBuilder b, String id, String text) {
        b.add(PREFIX + id + ".lore", text);
    }

    // Lines directly under the lore
    private static void tooltip(FabricLanguageProvider.TranslationBuilder b, String id, String... lines) {
        for (int i = 0; i < lines.length; i++) b.add(PREFIX + id + ".tooltip_" + i, lines[i]);
    }

    // Same lines for every item in a set (shared set bonus text)
    private static void tooltip(FabricLanguageProvider.TranslationBuilder b, List<String> ids, String... lines) {
        for (String id : ids) tooltip(b, id, lines);
    }

    // Lines appended after vanilla attribute lines
    private static void bottom(FabricLanguageProvider.TranslationBuilder b, String id, String... lines) {
        for (int i = 0; i < lines.length; i++) b.add(PREFIX + id + ".bottom_" + i, lines[i]);
    }

    public static void generate(FabricLanguageProvider.TranslationBuilder b) {
        // ---------- Items ----------
        lore(b, "brennon_ore", "Finally!");
        lore(b, "silver_ingot", "Shiny!");
        lore(b, "emerald_coin", "Common tender in the plains.");
        lore(b, "silk_spool", "Used to make the Spider Silk Bow and Silkweave.");
        lore(b, "silk_weave", "Used to make the Silkweave armor set.");
        lore(b, "bone_broth", "Looks inedible, but may have a use...");
        lore(b, "spider_carapace", "Part of a Spiders tough exterior, the possible applications for such an item are endless.");
        lore(b, "spider_leg", "Still wriggling..");
        lore(b, "banana_slime_ball", "Ballnana.");
        lore(b, "banana_branch", "The Banana must remain unharmed.");

        // ---------- Armor ----------
        lore(b, "bale_helmet", "Why a bale bucket you may ask... well it sounded nice.");
        lore(b, "bale_chestplate", "Aim for the target.");
        lore(b, "bale_leggings", "All sneaky beaky like.");
        lore(b, "bale_boots", "Good for a short fall.");
        tooltip(b, BALE_ARMOR, SP, "§5Full Set Bonus:", "§6 3% Bale Arrows not consumed on use", SP);

        lore(b, "arachno_carapace_helmet", "These Eyes have seen a lot.");
        lore(b, "arachno_carapace_chestplate", "Made of 100% unethically sourced Spider Carapace.");
        lore(b, "arachno_carapace_leggings", "Spiderlegs would have been a cooler name... just sayin.");
        lore(b, "arachno_carapace_boots", "Spider Boots? But spiders don't wear boots!");
        tooltip(b, ARACHNO_ARMOR, SP, "§5Full Set Bonus:", "§9-0.13 Air-Drag", "§9+1 Mid-air Jump", "§c Consumes 4 Mana");

        lore(b, "nanner_water_waders", "Helps you traverse mucky terrain with the greatest of ease.");
        bottom(b, "nanner_water_waders", "§9 Increased Speed on Soul Sand / Soil, Mud and Underwater.");

        lore(b, "helm_of_the_forest", "Helmet of a Forest Guardian.");
        lore(b, "chestplate_of_the_forest", "Chestplate of a Forest Guardian.");
        lore(b, "leggings_of_the_forest", "Leggings of a Forest Guardian.");
        lore(b, "boots_of_the_forest", "Boots of a Forest Guardian.");
        tooltip(b, FOREST_ARMOR, SP, "§5Full Set Bonus:", "§9 20% Knockback Resist");

        // ---------- Melee ----------
        lore(b, "bean_pole", "I wouldn't even touch you with a...");
        lore(b, "bone_dagger", "3 inches is actually pretty big..");
        bottom(b, "bone_dagger", "§2 2x Crit Damage");
        lore(b, "bananner_blade", "All though it resembles the banana fruit this sturdy blade is actually made of from Baldnana wood.");
        lore(b, "fumblebringer_fork", "This weapon was once used to bring upon \"The Great Fumbling\".");
        lore(b, "sword_of_the_forest", "Recommended for big hoards.");
        tooltip(b, "sword_of_the_forest", SP, "§3Armor of the Forest Bonus:", "§2+1 Attack Damage", "§c  Poison I | 0:04 | 25%");
        lore(b, "sprout_sickle", "Inflicts Paralysis…");
        bottom(b, "sprout_sickle", "§c Paralysis | 0:03 | 8%");
        lore(b, "oak_sword", "Your first proper weapon.");

        // ---------- Ranged ----------
        lore(b, "oak_bow", "Not as powerful as you might expect.");
        lore(b, "bownana", "Nana Nana.");
        lore(b, "arachno_crossbow", "Slow but powerful, a promising ranged option.");
        lore(b, "creepbow", "Hold charge for an extra powerful shot.");
        lore(b, "razor_thorn", "Just a sharp branch.");
        tooltip(b, "razor_thorn", SP, "§51% Chance for free a thorn.", SP, "§7When Thrown:", "§2 4 Attack Damage 3x");
        lore(b, "bale_arrow", "Knockback.");

        // ---------- Magic ----------
        lore(b, "firecharged_cane", "Fireball.");
        tooltip(b, "firecharged_cane", SP, "§7When Used:", "§2 3 Magic Damage", "§b 1.5s Cooldown", "§c-4 Mana");
        lore(b, "aquaonduit", "Slows both movement and fall speed.");
        tooltip(b, "aquaonduit", SP, "§7When Used:", "§2 0 Magic Damage", "§b 12s Cooldown", "§b 12s Ring Duration",
                "§2 Slow Falling I | 00:15 | 100%", "§c Slowness II | 00:15 | 100%", "§c-7 Mana");

        // ---------- Food ----------
        lore(b, "ghramble_bapple", "Ghramble is my favorite bapple.");
        tooltip(b, "ghramble_bapple",
                "§aResistance II | 0:45 | 30%", "§aHealth Boost I | 0:30 | 10%", "§aRegeneration I | 0:15 | 75%",
                "§cSlowness IV | 0:14 | 30%", "§cWeakness I | 0:12 | 65%", "§cParalysis I | 0:08 | 8%");
        lore(b, "mana_paste", "Restores mana in seconds!");
        tooltip(b, "mana_paste", SP, "%3$s%3$s%4$s");
        lore(b, "creeper_salve", "Patches you up in seconds!");
        tooltip(b, "creeper_salve", SP, "%1$s%1$s%1$s");
        lore(b, "plant_powder", "DO NOT CONSUME");
        lore(b, "rotten_stew", "Cafeteria slop.");
        tooltip(b, "rotten_stew", "§c\uD83C\uDF56 Hunger II | 0:20 | 30%");
        tooltip(b, "chicken_stew", "§c\uD83C\uDF56 Hunger I | 0:20 | 10%");

        // ---------- Trinkets ----------
        lore(b, "flint_charm", "Flint Charm? I just don't see it.");
        tooltip(b, "flint_charm", SP, "§7When in Off Hand:", "§2 +1 Melee Attack Damage");
        lore(b, "resurrection_amulet", "I mean the name tells you all you need to know.");
        lore(b, "rosebound_tome", "Spawns a healing aura.");
        tooltip(b, "rosebound_tome", SP, "§7When Used:", "§2 0 Magic Damage", "§b 90s Cooldown", "§b 20s Ring Duration",
                "§2 Regeneration I | 00:30 | 100%", "§c-5 Mana");

        // ---------- Summons ----------
        lore(b, "summoners_cane", "A trusty tool for any summoner.");
        bottom(b, "summoners_cane", "§c-6 Mana");
        lore(b, "dog_whistle", "Summons 3 doggies to help you fight!");
        tooltip(b, "dog_whistle", SP, "§7When Used:", "§2 1.5 Summon Damage", "§2 30s Summon Duration", "§2 10s Cooldown", "§c-8 Mana");

        // ---------- Equipment ----------
        lore(b, "cardboard_shield", "Only Blocks Projectiles.");
        lore(b, "plains_coin_pouch", "A pouch for your coins.");
    }
}