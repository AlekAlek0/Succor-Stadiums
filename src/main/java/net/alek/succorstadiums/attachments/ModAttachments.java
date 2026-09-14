package net.alek.succorstadiums.attachments;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.alek.succorstadiums.mana.ManaData;

public class ModAttachments {
    public static final AttachmentType<ManaData> MANA =
            AttachmentRegistry.create(
                    Identifier.fromNamespaceAndPath("succorstadiums", "mana"),
                    builder -> builder
                            .persistent(ManaData.CODEC)
                            .syncWith(ManaData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            );

    private ModAttachments() {}
}