package net.alek.succorstadiums.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

import static net.alek.succorstadiums.tags.ModTags.Items.*;

public class ModToolMaterials {

    public static final ToolMaterial BEAN_POLE_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            160,
            0F,
            0F,
            22,
            BEAN_POLE_REPAIR
    );

    public static final ToolMaterial BONE_DAGGER_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            128,
            0F,
            0F,
            22,
            BONE_DAGGER_REPAIR
    );

    public static final ToolMaterial FUMBLEBRINGER_FORK_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            256,
            2F,
            1.5F,
            22,
            FUMBLEBRINGER_FORK_REPAIR
    );


}
