package net.alek.succorstadiums.item;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {

    public static final Item BRENNON_ORE = registerItem("brennon_ore", Item::new);
    public static final Item SUCCOR_STADIUM_ICON = registerItem("succor_stadium_icon", Item::new);


    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name)))));
    }



    public static void registerModItems() {

        SuccorStadiums.LOGGER.info("Registering Mod Items for " + SuccorStadiums.MOD_ID);

    }

}
