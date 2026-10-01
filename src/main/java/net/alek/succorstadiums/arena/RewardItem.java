package net.alek.succorstadiums.arena;

// RewardItem class
// A single reward entry: an item (itemId + count), XP points, XP levels, or an advancement.
public class RewardItem {

    private String itemId; // null/unused when xp == true or advancement == true
    private int count;
    private boolean xp;
    private boolean xpIsLevels; // only meaningful when xp == true; false = points, true = levels
    private boolean advancement;
    private String advancementId; // only meaningful when advancement == true, e.g. "succorstadiums:vincible"

    public RewardItem(String itemId, int count) {
        this(itemId, count, false, false);
    }

    public RewardItem(String itemId, int count, boolean xp) {
        this(itemId, count, xp, false);
    }

    public RewardItem(String itemId, int count, boolean xp, boolean xpIsLevels) {
        this.itemId = itemId;
        this.count = count;
        this.xp = xp;
        this.xpIsLevels = xpIsLevels;
        this.advancement = false;
        this.advancementId = null;
    }

    public static RewardItem ofXpPoints(int amount) {
        return new RewardItem(null, amount, true, false);
    }

    public static RewardItem ofXpLevels(int amount) {
        return new RewardItem(null, amount, true, true);
    }

    public static RewardItem ofAdvancement(String advancementId) {
        RewardItem reward = new RewardItem(null, 1, false, false);
        reward.advancement = true;
        reward.advancementId = advancementId;
        return reward;
    }

    public String getItemId() { return itemId; }
    public int getCount() { return count; }
    public boolean isXp() { return xp; }
    public boolean isXpLevels() { return xpIsLevels; }
    public boolean isAdvancement() { return advancement; }
    public String getAdvancementId() { return advancementId; }

    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setCount(int count) { this.count = count; }
    public void setXp(boolean xp) { this.xp = xp; }
    public void setXpIsLevels(boolean xpIsLevels) { this.xpIsLevels = xpIsLevels; }
    public void setAdvancement(boolean advancement) { this.advancement = advancement; }
    public void setAdvancementId(String advancementId) { this.advancementId = advancementId; }
}