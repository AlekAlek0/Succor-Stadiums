package net.alek.succorstadiums.mana;

import net.alek.succorstadiums.attachments.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

public final class ManaHelper {

    public static ManaData get(Player player) {
        return player.getAttachedOrCreate(ModAttachments.MANA, ManaData::new);
    }

    public static HypermanaData getHypermana(Player player) {
        return player.getAttachedOrCreate(ModAttachments.HYPERMANA, HypermanaData::new);
    }

    public static boolean consumeMana(Player player, int amount) {
        HypermanaData hyperData = getHypermana(player);
        ManaData manaData = get(player);

        int fromHyper = Math.min(hyperData.getHypermana(), amount);
        int remainder = amount - fromHyper;

        if (!manaData.hasEnoughMana(remainder)) {
            return false;
        }

        if (fromHyper > 0) {
            player.setAttached(ModAttachments.HYPERMANA, hyperData.withHypermanaConsumed(fromHyper));
        }
        if (remainder > 0) {
            player.setAttached(ModAttachments.MANA, manaData.withManaConsumed(remainder));
        }
        return true;
    }

    public static void addMana(Player player, int amount) {
        ManaData data = get(player);
        ManaData updated = data.withManaAdded(amount);
        if (updated != data) {
            player.setAttached(ModAttachments.MANA, updated);
        }
    }

    public static void addHypermana(Player player, int amount) {
        HypermanaData data = getHypermana(player);
        HypermanaData updated = data.withHypermanaGranted(amount);
        player.setAttached(ModAttachments.HYPERMANA, updated);
    }

    public static void tick(ServerPlayer player) {
        ManaData data = get(player);
        ManaData updated = data.ticked();
        if (updated != data) {
            player.setAttached(ModAttachments.MANA, updated);
        }

        HypermanaData hyperData = getHypermana(player);
        HypermanaData hyperUpdated = hyperData.ticked();
        if (hyperUpdated != hyperData) {
            player.setAttached(ModAttachments.HYPERMANA, hyperUpdated);
        }
    }

    private ManaHelper() {}
}