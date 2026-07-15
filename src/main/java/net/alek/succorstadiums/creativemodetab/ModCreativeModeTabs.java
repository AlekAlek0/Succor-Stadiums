package net.alek.succorstadiums.creativemodetab;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

// Mod creative mode tabs class
public class ModCreativeModeTabs {

    // Create a new creative mode tab for succor stadium items with the given namespace identifier
    public static final CreativeModeTab SUCCOR_STADIUM_ITEM_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_items"),

            // Set the icon, title, and items for the creative tab menu
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BRENNON_ORE))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BRENNON_ORE);
                        output.accept(ModItems.SILVER_INGOT);
                        output.accept(ModItems.SILK_WEAVE);
                        output.accept(ModItems.SILK_SPOOL);
                        output.accept(ModItems.BONE_BROTH);
                        output.accept(ModItems.SPIDER_CARAPACE);
                        output.accept(ModItems.BANANA_SLIME_BALL);



                    }).build());

    // Create a new creative mode tab for succor stadium armor with the given namespace identifier
    public static final CreativeModeTab SUCCOR_STADIUM_ARMOR_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_armor"),

            // Set the icon, title, and items for the creative tab menu
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BALE_CHESTPLATE))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_armor"))
                    .displayItems((parameters, output) -> {

                        var enchantments = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

                        // Helmet - Projectile Protection 2
                        ItemStack helmet = new ItemStack(ModItems.BALE_HELMET);
                        helmet.enchant(enchantments.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 2);
                        output.accept(helmet);

                        // Chestplate - Projectile Protection 2
                        ItemStack chestplate = new ItemStack(ModItems.BALE_CHESTPLATE);
                        chestplate.enchant(enchantments.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 2);
                        output.accept(chestplate);

                        // Leggings - Swift Sneak 1, Projectile Protection 1
                        ItemStack leggings = new ItemStack(ModItems.BALE_LEGGINGS);
                        leggings.enchant(enchantments.getOrThrow(Enchantments.SWIFT_SNEAK), 1);
                        leggings.enchant(enchantments.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 1);
                        output.accept(leggings);

                        // Boots - Feather Falling 2, Projectile Protection 1
                        ItemStack boots = new ItemStack(ModItems.BALE_BOOTS);
                        boots.enchant(enchantments.getOrThrow(Enchantments.FEATHER_FALLING), 2);
                        boots.enchant(enchantments.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 1);
                        output.accept(boots);

                        output.accept(ModItems.ARACHNO_CARAPACE_HELMET);
                        output.accept(ModItems.ARACHNO_CARAPACE_CHESTPLATE);
                        output.accept(ModItems.ARACHNO_CARAPACE_LEGGINGS);
                        output.accept(ModItems.ARACHNO_CARAPACE_BOOTS);
                        output.accept(ModItems.NANNER_WATER_WADERS);



                    }).build());

    // Create a new creative mode tab for succor stadium melee weapons with the given namespace identifier
    public static final CreativeModeTab SUCCOR_STADIUM_MELEE_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_melee"),

            // Set the icon, title, and items for the creative tab menu
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BEAN_POLE))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_melee"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BEAN_POLE);
                        output.accept(ModItems.BONE_DAGGER);
                        output.accept(ModItems.BANANNER_BLADE);
                        output.accept(ModItems.FUMBLEBRINGER_FORK);



                    }).build());

    // Create a new creative mode tab for succor stadium ranged weapons with the given namespace identifier
    public static final CreativeModeTab SUCCOR_STADIUM_RANGED_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_ranged"),

            // Set the icon, title, and items for the creative tab menu
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BOWNANA))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_ranged"))
                    .displayItems((parameters, output) -> {

                        var enchantments = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

                        output.accept(ModItems.BOWNANA);

                        ItemStack arachno_crossbow = new ItemStack(ModItems.ARACHNO_CROSSBOW);
                        arachno_crossbow.enchant(enchantments.getOrThrow(Enchantments.PIERCING), 2);
                        output.accept(arachno_crossbow);



                    }).build());

    // Create a new creative mode tab for succor stadium magic weapons with the given namespace identifier
    public static final CreativeModeTab SUCCOR_STADIUM_MAGIC_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_magic"),

            // Set the icon, title, and items for the creative tab menu
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.AQUA_STAFF))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_magic"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FIRE_STAFF);
                        output.accept(ModItems.AQUA_STAFF);



                    }).build());
    // Create a new creative mode tab for succor stadium trinkets with the given namespace identifier
    public static final CreativeModeTab SUCCOR_STADIUM_TRINKET_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_trinkets"),

            // Set the icon, title, and items for the creative tab menu
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.FLINT_CHARM))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_trinkets"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FLINT_CHARM);
                        output.accept(ModItems.RESURRECTION_AMULET);
                        output.accept(ModItems.DOG_WHISTLE);



                    }).build());

    // Create a new creative mode tab for succor stadium foods with the given namespace identifier
    public static final CreativeModeTab SUCCOR_STADIUM_FOOD_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_foods"),

            // Set the icon, title, and items for the creative tab menu
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GRAMBLE_BAPPLE))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_foods"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GRAMBLE_BAPPLE);
                        output.accept(ModItems.ROTTEN_STEW);



                    }).build());

    // Register method for the mod creative mode tabs
    public static void registerModCreativeModeTabs()
    {
        SuccorStadiums.LOGGER.info("Registering Creative Mode Tabs for " + SuccorStadiums.MOD_ID);
    }
}