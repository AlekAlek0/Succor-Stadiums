package net.alek.succorstadiums.creativemodetab;

import net.alek.succorstadiums.SuccorStadiums;

import net.alek.succorstadiums.item.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;


public class ModCreativeModeTabs {

    public static final CreativeModeTab SUCCOR_STADIUM_ITEM_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_items"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SUCCOR_STADIUM_ICON))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_items"))
                    .displayItems((parameters, output) -> {
                      output.accept(ModItems.BRENNON_ORE);
                      output.accept(ModItems.SILVER_INGOT);





                    }).build());

    public static final CreativeModeTab SUCCOR_STADIUM_WEAPON_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "succor_stadium_weapons"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SUCCOR_STADIUM_ICON))
                    .title(Component.translatable("creativemodetab.succorstadiums.succor_stadium_weapons"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BEAN_POLE);
                        output.accept(ModItems.FIRE_STAFF);





                    }).build());


    public static void registerModCreativeModeTabs()
    {
        SuccorStadiums.LOGGER.info("Registering Creative Mode Tabs for " + SuccorStadiums.MOD_ID);
    }

}
