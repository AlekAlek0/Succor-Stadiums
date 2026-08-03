package net.alek.succorstadiums.enchantment;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {

    public static final ResourceKey<Enchantment> VIPERS_BITE = key("vipers_bite");
    public static final ResourceKey<Enchantment> ROSE_THORN = key("rose_thorn");

    private static ResourceKey<Enchantment> key(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, path);
        return ResourceKey.create(Registries.ENCHANTMENT, id);
    }
}
