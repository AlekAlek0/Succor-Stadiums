package net.alek.succorstadiums.item;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.food.ModFoods;
import net.alek.succorstadiums.item.custom.FireStaffItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import java.util.function.Function;

public class ModItems {

    public static final Item SUCCOR_STADIUM_ICON = registerItem("succor_stadium_icon", Item::new);

    public static final TagKey<Item> REPAIRS_BEAN_POLE = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "repairs_bean_pole"));
    public static final ToolMaterial BEAN_POLE_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            128,
            0F,
            -1F,
            22,
            REPAIRS_BEAN_POLE
    );

    public static final TagKey<Item> REPAIRS_BONE_DAGGER = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "repairs_bone_dagger"));
    public static final ToolMaterial BONE_DAGGER_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            64,
            0F,
            -1F,
            22,
            REPAIRS_BONE_DAGGER
    );

    public static final Item BRENNON_ORE = registerItem("brennon_ore", Item::new);
    public static final Item SILVER_INGOT = registerItem("silver_ingot", Item::new);

    public static final Item BEAN_POLE = registerItem("bean_pole", properties -> new Item(properties.sword(BEAN_POLE_TOOL_MATERIAL, 0.1F, -3.8F)));
    public static final Item BONE_DAGGER = registerItem("bone_dagger", properties -> new Item(properties.sword(BONE_DAGGER_TOOL_MATERIAL, 1.3F, -2.3F)));
    public static final Item FIRE_STAFF = registerItem("fire_staff", properties -> new FireStaffItem(properties.durability(50)));

    public static final Item GRAMBLE_BAPPLE = registerItem("ghramble_bapple", properties -> new Item(properties.food(ModFoods.GHRAMBLE_BAPPLE, ModFoods.GHRAMBLE_BAPPLE_CONSUMABLE)));

    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name)))));
    }

    public static void registerModItems() {

        SuccorStadiums.LOGGER.info("Registering Mod Items for " + SuccorStadiums.MOD_ID);

    }

}
