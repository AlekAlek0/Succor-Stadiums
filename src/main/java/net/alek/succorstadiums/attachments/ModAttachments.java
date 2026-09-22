package net.alek.succorstadiums.attachments;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.alek.succorstadiums.item.equipment.pouch.PouchSettingsData;
import net.alek.succorstadiums.mana.HypermanaData;
import net.alek.succorstadiums.mana.ManaData;

// ModAttachments class
public class ModAttachments {
    public static final AttachmentType<ManaData> MANA =
            AttachmentRegistry.create(
                    Identifier.fromNamespaceAndPath("succorstadiums", "mana"),
                    builder -> builder
                            .persistent(ManaData.CODEC)
                            .syncWith(ManaData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            );

    public static final AttachmentType<HypermanaData> HYPERMANA =
            AttachmentRegistry.create(
                    Identifier.fromNamespaceAndPath("succorstadiums", "hypermana"),
                    builder -> builder
                            .persistent(HypermanaData.CODEC)
                            .syncWith(HypermanaData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            );

    public static final AttachmentType<PouchSettingsData> POUCH_SETTINGS =
            AttachmentRegistry.create(
                    Identifier.fromNamespaceAndPath("succorstadiums", "pouch_settings"),
                    builder -> builder
                            .persistent(PouchSettingsData.CODEC)
                            .syncWith(PouchSettingsData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            );

    private ModAttachments() {}
}