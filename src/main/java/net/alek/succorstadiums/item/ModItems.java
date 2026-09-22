package net.alek.succorstadiums.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;

import java.util.function.Function;

import net.alek.succorstadiums.item.equipment.PlainsCoinPouchItem;
import static net.alek.succorstadiums.item.ModArmorMaterials.*;
import static net.alek.succorstadiums.item.ModToolMaterials.*;
import net.alek.succorstadiums.item.weapons.ranged.*;
import net.alek.succorstadiums.item.weapons.melee.*;
import net.alek.succorstadiums.item.weapons.magic.*;
import net.alek.succorstadiums.item.trinkets.*;
import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.food.ModFoods;
import net.alek.succorstadiums.item.foods.*;
import net.alek.succorstadiums.item.armor.*;

public class ModItems {

    public static final Item BRENNON_ORE = registerItem("brennon_ore", Item::new);
    public static final Item SILVER_INGOT = registerItem("silver_ingot", Item::new);
    public static final Item EMERALD_COIN = registerItem("emerald_coin", properties -> new Item(properties
            .stacksTo(99)));
    public static final Item SILK_SPOOL = registerItem("silk_spool", Item::new);
    public static final Item SILK_WEAVE = registerItem("silk_weave", Item::new);
    public static final Item BONE_BROTH = registerItem("bone_broth", Item::new);
    public static final Item SPIDER_CARAPACE = registerItem("spider_carapace", Item::new);
    public static final Item SPIDER_LEG = registerItem("spider_leg", Item::new);
    public static final Item BANANA_SLIME_BALL = registerItem("banana_slime_ball", Item::new);
    public static final Item BANANA_BRANCH = registerItem("banana_branch", Item::new);

    public static final Item BALE_HELMET = registerItem("bale_helmet",
            (properties) -> new BaleArmorItem(BALE_ARMOR_MATERIAL, ArmorType.HELMET, properties));

    public static final Item BALE_CHESTPLATE = registerItem("bale_chestplate",
            (properties) -> new BaleArmorItem(BALE_ARMOR_MATERIAL, ArmorType.CHESTPLATE, properties));

    public static final Item BALE_LEGGINGS = registerItem("bale_leggings",
            (properties) -> new BaleArmorItem(BALE_ARMOR_MATERIAL, ArmorType.LEGGINGS, properties));

    public static final Item BALE_BOOTS = registerItem("bale_boots",
            (properties) -> new BaleArmorItem(BALE_ARMOR_MATERIAL, ArmorType.BOOTS, properties));

    public static final Item ARACHNO_CARAPACE_HELMET = registerItem("arachno_carapace_helmet", properties -> new ArachnoCarapaceArmorItem(
                    properties.humanoidArmor(ARACHNO_CARAPACE_ARMOR_MATERIAL, ArmorType.HELMET)
                            .durability(256),
            ArmorType.HELMET
    ));

    public static final Item ARACHNO_CARAPACE_CHESTPLATE = registerItem("arachno_carapace_chestplate", properties -> new ArachnoCarapaceArmorItem(
                    properties.humanoidArmor(ARACHNO_CARAPACE_ARMOR_MATERIAL, ArmorType.CHESTPLATE)
                            .durability(364),
            ArmorType.CHESTPLATE
    ));

    public static final Item ARACHNO_CARAPACE_LEGGINGS = registerItem("arachno_carapace_leggings", properties -> new ArachnoCarapaceArmorItem(
                    properties.humanoidArmor(ARACHNO_CARAPACE_ARMOR_MATERIAL, ArmorType.LEGGINGS)
                            .durability(300),
            ArmorType.LEGGINGS
    ));

    public static final Item ARACHNO_CARAPACE_BOOTS = registerItem("arachno_carapace_boots", properties -> new ArachnoCarapaceArmorItem(
                    properties.humanoidArmor(ARACHNO_CARAPACE_ARMOR_MATERIAL, ArmorType.BOOTS)
                            .durability(256),
            ArmorType.BOOTS
    ));

    public static final Item NANNER_WATER_WADERS = registerItem("nanner_water_waders", properties -> new NannerWaterWadersItem(
                    properties.humanoidArmor(NANNER_WADERS_MATERIAL , ArmorType.BOOTS)
                            .durability(100))
    );

    public static final Item HELM_OF_THE_FOREST = registerItem("helm_of_the_forest", properties -> new ArmorOfTheForestItem(
                    properties.humanoidArmor(ARMOR_OF_THE_FOREST_ARMOR_MATERIAL, ArmorType.HELMET)
                            .durability(365),
            ArmorType.HELMET
    ));

    public static final Item CHESTPLATE_OF_THE_FOREST = registerItem("chestplate_of_the_forest", properties -> new ArmorOfTheForestItem(
                    properties.humanoidArmor(ARMOR_OF_THE_FOREST_ARMOR_MATERIAL, ArmorType.CHESTPLATE)
                            .durability(456),
            ArmorType.CHESTPLATE
    ));

    public static final Item LEGGINGS_OF_THE_FOREST = registerItem("leggings_of_the_forest", properties -> new ArmorOfTheForestItem(
                    properties.humanoidArmor(ARMOR_OF_THE_FOREST_ARMOR_MATERIAL, ArmorType.LEGGINGS)
                            .durability(438),
            ArmorType.LEGGINGS
    ));

    public static final Item BOOTS_OF_THE_FOREST = registerItem("boots_of_the_forest", properties -> new ArmorOfTheForestItem(
                    properties.humanoidArmor(ARMOR_OF_THE_FOREST_ARMOR_MATERIAL, ArmorType.BOOTS)
                            .durability(328),
            ArmorType.BOOTS
    ));

    public static final Item BEAN_POLE = registerItem("bean_pole", properties -> new BeanPoleItem(
            properties.sword(BEAN_POLE_TOOL_MATERIAL,
                    0F,
                    0F)
    ));

    public static final Item BONE_DAGGER = registerItem("bone_dagger", properties -> new BoneDaggerItem(
            properties.sword(BONE_DAGGER_TOOL_MATERIAL,
                    0F,
                    0F)
    ));

    public static final Item BANANNER_BLADE = registerItem("bananner_blade", properties -> new BannanerBladeItem(
            properties.sword(BANANNER_BLADE_TOOL_MATERIAL,
                    0F,
                    0F)
    ));

    public static final Item FUMBLEBRINGER_FORK = registerItem("fumblebringer_fork", properties -> new FumblebringerForkItem(
            properties.spear(FUMBLEBRINGER_FORK_TOOL_MATERIAL,
            0.65F,
            0.50F,
            0.6F,
            5.0F,
            14.0F,
            8.0F,
            5.1F,
            8.0F,
            4.6F)
    ));

    public static final Item GREAT_SWORD_OF_THE_FOREST = registerItem("great_sword_of_the_forest", properties -> new GreatSwordOfTheForestItem(
            properties.sword(GREAT_SWORD_OF_THE_FOREST_TOOL_MATERIAL,
                    0,
                    0)
    ));

    public static final Item SWORD_OF_THE_FOREST = registerItem("sword_of_the_forest", properties -> new SwordOfTheForestItem(
            properties.sword(SWORD_OF_THE_FOREST_TOOL_MATERIAL,
                    0F,
                    0F)
    ));

    public static final Item SPROUT_SICKLE = registerItem("sprout_sickle", properties -> new SproutSickleItem(
            properties.sword(SPROUT_SICKLE_TOOL_MATERIAL,
                    0F,
                    0F)
    ));

    public static final Item OAK_SWORD = registerItem("oak_sword", properties -> new Item(
            properties.sword(OAK_SWORD_TOOL_MATERIAL,
                    3,
                    -2.4F)
    ));

    public static final Item FIRECHARGED_CANE = registerItem("firecharged_cane", properties -> new FirechargedCaneItem(properties
            .durability(300)));
    public static final Item AQUAONDUIT = registerItem("aquaonduit", properties -> new AquaonduitItem(properties
            .durability(300)));

    public static final Item BOWNANA = registerItem("bownana", properties -> new BownanaItem(properties
            .durability(384)));
    public static final Item ARACHNO_CROSSBOW = registerItem("arachno_crossbow", properties -> new ArachnoCrossbowItem(properties
            .durability(450)));
    public static final Item CREEPBOW = registerItem("creepbow", properties -> new CreepbowItem(properties
            .durability(450)));
    public static final Item RAZOR_THORN = registerItem("razor_thorn", properties -> new RazorThornItem(properties));
    public static final Item BALE_ARROW = registerItem("bale_arrow", properties -> new BaleArrowItem(properties
            .stacksTo(64)));

    public static final Item GHRAMBLE_BAPPLE = registerItem("ghramble_bapple", properties -> new Item(properties.food(ModFoods.GHRAMBLE_BAPPLE, ModFoods.GHRAMBLE_BAPPLE_CONSUMABLE)
            .useCooldown(5)));
    public static final Item CREEPER_SALVE = registerItem("creeper_salve", properties -> new CreeperSalveItem(properties.food(ModFoods.CREEPER_SALVE, ModFoods.CREEPER_SALVE_CONSUMABLE)
            .useCooldown(12)
            .stacksTo(8)));
    public static final Item MANA_PASTE = registerItem("mana_paste", properties -> new ManaPasteItem(properties.food(ModFoods.MANA_PASTE, ModFoods.MANA_PASTE_CONSUMABLE)
            .useCooldown(15)));
    public static final Item MAGIC_FLESH = registerItem("magic_flesh", properties -> new Item(properties.food(ModFoods.MAGIC_FLESH, ModFoods.MAGIC_FLESH_CONSUMABLE)));
    public static final Item PLANT_POWDER = registerItem("plant_powder", properties -> new Item(properties.food(ModFoods.PLANT_POWDER, ModFoods.PLANT_POWDER_CONSUMABLE)));
    public static final Item BEEF_STEW = registerItem("beef_stew", properties -> new Item(properties.food(ModFoods.BEEF_STEW, ModFoods.BEEF_STEW_CONSUMABLE)
            .stacksTo(16)));
    public static final Item CHICKEN_STEW = registerItem("chicken_stew", properties -> new Item(properties.food(ModFoods.CHICKEN_STEW, ModFoods.CHICKEN_STEW_CONSUMABLE)
            .stacksTo(16)));
    public static final Item PORK_STEW = registerItem("pork_stew", properties -> new Item(properties.food(ModFoods.PORK_STEW, ModFoods.PORK_STEW_CONSUMABLE)
            .stacksTo(16)));
    public static final Item MUTTON_STEW = registerItem("mutton_stew", properties -> new Item(properties.food(ModFoods.MUTTON_STEW, ModFoods.MUTTON_STEW_CONSUMABLE)
            .stacksTo(16)));
    public static final Item RABBIT_STEW = registerItem("rabbit_stew", properties -> new Item(properties.food(ModFoods.RABBIT_STEW, ModFoods.RABBIT_STEW_CONSUMABLE)
            .stacksTo(16)));
    public static final Item ROTTEN_STEW = registerItem("rotten_stew", properties -> new Item(properties.food(ModFoods.ROTTEN_STEW, ModFoods.ROTTEN_STEW_CONSUMABLE)
            .stacksTo(16)));

    public static final Item FLINT_CHARM = registerItem("flint_charm", properties -> new FlintCharmItem(properties
            .stacksTo(1)));
    public static final Item RESURRECTION_AMULET = registerItem("resurrection_amulet", properties -> new ResurrectionAmuletItem(properties
            .stacksTo(1)));
    public static final Item DOG_WHISTLE = registerItem("dog_whistle", properties -> new DogWhistleItem(properties
            .durability(48)
            .stacksTo(1)));

    public static final Item PLAINS_COIN_POUCH = registerItem("plains_coin_pouch", properties -> new PlainsCoinPouchItem(properties
            .stacksTo(1)));

    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name)))));
    }

    public static void registerModItems() {

        SuccorStadiums.LOGGER.info("Registering Mod Items for " + SuccorStadiums.MOD_ID);

    }
}