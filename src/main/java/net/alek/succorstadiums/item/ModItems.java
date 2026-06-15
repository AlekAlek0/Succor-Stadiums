package net.alek.succorstadiums.item;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.food.ModFoods;
import net.alek.succorstadiums.item.armor.NannerWaterWadersItem;
import net.alek.succorstadiums.item.trinkets.FlintCharmItem;
import net.alek.succorstadiums.item.weapons.*;
import net.alek.succorstadiums.item.trinkets.ResurrectionAmuletItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import java.util.function.Function;

import static net.alek.succorstadiums.item.ModToolMaterials.BEAN_POLE_TOOL_MATERIAL;
import static net.alek.succorstadiums.item.ModToolMaterials.BONE_DAGGER_TOOL_MATERIAL;

import static net.alek.succorstadiums.item.ModArmorMaterials.NANNER_WADERS_MATERIAL;

public class ModItems {

    public static final Item BRENNON_ORE = registerItem("brennon_ore", Item::new);
    public static final Item SILVER_INGOT = registerItem("silver_ingot", Item::new);
    public static final Item SILK_SPOOL = registerItem("silk_spool", Item::new);
    public static final Item SILK_WEAVE = registerItem("silk_weave", Item::new);
    public static final Item BONE_BROTH = registerItem("bone_broth", Item::new);

    public static final Item BALE_HELMET = registerItem("bale_helmet", properties -> new Item(
                    properties.humanoidArmor(ModArmorMaterials.BALE_ARMOR_MATERIAL, ArmorType.HELMET)
                            .durability(128)
            )
    );

    public static final Item BALE_CHESTPLATE = registerItem("bale_chestplate", properties -> new Item(
                    properties.humanoidArmor(ModArmorMaterials.BALE_ARMOR_MATERIAL, ArmorType.CHESTPLATE)
                            .durability(160)
            )
    );

    public static final Item BALE_LEGGINGS = registerItem("bale_leggings", properties -> new Item(
            properties.humanoidArmor(ModArmorMaterials.BALE_ARMOR_MATERIAL, ArmorType.LEGGINGS)
                            .durability(144)
            )
    );

    public static final Item BALE_BOOTS = registerItem("bale_boots", properties -> new Item(
            properties.humanoidArmor(ModArmorMaterials.BALE_ARMOR_MATERIAL, ArmorType.BOOTS)
                            .durability(112)
            )
    );

    public static final Item NANNER_WATER_WADERS = registerItem("nanner_water_waders", properties -> new NannerWaterWadersItem(
                    properties.humanoidArmor(NANNER_WADERS_MATERIAL , ArmorType.BOOTS)
                            .durability(100)
            )
    );

    public static final Item BEAN_POLE = registerItem("bean_pole", properties -> new BeanPoleItem(
            properties.sword(BEAN_POLE_TOOL_MATERIAL,
                    0F,
                    0F)
                    .durability(80)));

    public static final Item BONE_DAGGER = registerItem("bone_dagger", properties -> new BoneDaggerItem(
            properties.sword(BONE_DAGGER_TOOL_MATERIAL,
                    0F,
                    0F)
                    .durability(64)
    ));

    public static final Item FIRE_STAFF = registerItem("fire_staff", properties -> new FireStaffItem(properties.durability(50)));
    public static final Item AQUA_STAFF = registerItem("aqua_staff", properties -> new AquaRodItem(properties.durability(50)));

    public static final Item BOWNANA = registerItem("bownana", properties -> new BownanaItem(properties.durability(384)));


    public static final Item GRAMBLE_BAPPLE = registerItem("ghramble_bapple", properties -> new Item(properties.food(ModFoods.GHRAMBLE_BAPPLE, ModFoods.GHRAMBLE_BAPPLE_CONSUMABLE)));
    public static final Item ROTTEN_STEW = registerItem("rotten_stew", properties -> new Item(properties.food(ModFoods.ROTTEN_STEW, ModFoods.ROTTEN_STEW_CONSUMABLE).stacksTo(16)));

    public static final Item FLINT_CHARM = registerItem("flint_charm", properties -> new FlintCharmItem(properties.stacksTo(1)));
    public static final Item RESURRECTION_AMULET = registerItem("resurrection_amulet", properties -> new ResurrectionAmuletItem(properties.stacksTo(1)));

    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name)))));
    }

    public static void registerModItems() {

        SuccorStadiums.LOGGER.info("Registering Mod Items for " + SuccorStadiums.MOD_ID);

    }

}
