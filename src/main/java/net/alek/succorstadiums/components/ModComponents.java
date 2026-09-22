package net.alek.succorstadiums.components;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;

import net.alek.succorstadiums.item.equipment.pouch.PouchContents;
import net.alek.succorstadiums.SuccorStadiums;

// ModComponents class
public class ModComponents {
    public static final DataComponentType<PouchContents> POUCH_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("succorstadiums", "pouch_contents"),
            DataComponentType.<PouchContents>builder()
                    .persistent(PouchContents.CODEC)
                    .networkSynchronized(PouchContents.STREAM_CODEC)
                    .build()
    );

    public static void registerModComponents() {

        SuccorStadiums.LOGGER.info("Registering Mod Components for " + SuccorStadiums.MOD_ID);

    }
}