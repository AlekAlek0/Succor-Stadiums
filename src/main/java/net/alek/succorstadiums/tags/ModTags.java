package net.alek.succorstadiums.tags;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {

    public static class Items {

        public static final TagKey<Item> BEAN_POLE_REPAIR = createTag("bean_pole_repair");
        public static final TagKey<Item> BONE_DAGGER_REPAIR = createTag("bone_dagger_repair");
        public static final TagKey<Item> BANANNER_BLADE_REPAIR = createTag("bananner_blade_repair");
        public static final TagKey<Item> FUMBLEBRINGER_FORK_REPAIR = createTag("fumblebringer_fork_repair");

        private static TagKey<Item> createTag(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, name));
        }
    }
}