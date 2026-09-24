package net.alek.succorstadiums.item.equipment.pouch;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;

import net.alek.succorstadiums.attachment.ModAttachments;

// PouchSettingsHelper class
public final class PouchSettingsHelper {

    private PouchSettingsHelper() {}

    // Check if auto deposit is enabled by fetching the players pouch settings mod attachment
    public static boolean isAutoDepositEnabled(Player player, Identifier pouchTypeId) {
        return player.getAttachedOrCreate(ModAttachments.POUCH_SETTINGS, PouchSettingsData::new)
                .isAutoDepositEnabled(pouchTypeId);
    }

    // Set the players auto deposit setting
    public static void setAutoDepositEnabled(Player player, Identifier pouchTypeId, boolean enabled) {
        PouchSettingsData data = player.getAttachedOrCreate(ModAttachments.POUCH_SETTINGS, PouchSettingsData::new);
        player.setAttached(ModAttachments.POUCH_SETTINGS, data.withAutoDeposit(pouchTypeId, enabled));
    }
}