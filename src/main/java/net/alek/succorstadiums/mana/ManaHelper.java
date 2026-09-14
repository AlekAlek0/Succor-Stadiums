package net.alek.succorstadiums.mana;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import net.alek.succorstadiums.attachments.ModAttachments;

public final class ManaHelper {

    public static ManaData get(Player player) {
        return player.getAttachedOrCreate(ModAttachments.MANA, ManaData::new);
    }

    public static boolean consumeMana(Player player, int amount) {
        ManaData data = get(player);
        if (!data.hasEnoughMana(amount)) {
            return false;
        }
        player.setAttached(ModAttachments.MANA, data.withManaConsumed(amount));
        return true;
    }

    public static void tick(ServerPlayer player) {
        ManaData data = get(player);
        ManaData updated = data.ticked();
        if (updated != data) {
            player.setAttached(ModAttachments.MANA, updated);
        }
    }

    private ManaHelper() {}
}