package net.alek.succorstadiums.sound;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent AQUA_ROD_USE = registerSound("aqua_staff_use");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void registerModSounds() {

        SuccorStadiums.LOGGER.info("Registering Mod Items for " + SuccorStadiums.MOD_ID);

    }
}