package net.alek.succorstadiums.arena;

// RewardItem class
public class RewardItem {

    private String itemId; // null when xp or advancement is true
    private int count; // reward item count
    private boolean xp; // amount of xp
    private boolean xpIsLevels; // only important if xp is true
    private boolean advancement; // boolean for if reward item is an advancement
    private String advancementId; // only important if advancement is true, e.g. "succorstadiums:vincible"

    // Public constructor for creating a reward item that isn't an advancement
    public RewardItem(String itemId, int count, boolean xp, boolean xpIsLevels) {
        this.itemId = itemId;
        this.count = count;
        this.xp = xp;
        this.xpIsLevels = xpIsLevels;
        this.advancement = false;
        this.advancementId = null;
    }

    // Public helper method for if the reward item is an advancement
    public static RewardItem ofAdvancement(String advancementId) {
        RewardItem reward = new RewardItem(null, 1, false, false);
        reward.advancement = true;
        reward.advancementId = advancementId;
        return reward;
    }

    // Accessor methods for getting reward items
    public String getItemId() { return itemId; }
    public int getCount() { return count; }
    public boolean isXp() { return xp; }
    public boolean isXpLevels() { return xpIsLevels; }
    public boolean isAdvancement() { return advancement; }
    public String getAdvancementId() { return advancementId; }

    // Mutator methods for setting reward items
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setCount(int count) { this.count = count; }
    public void setXp(boolean xp) { this.xp = xp; }
    public void setXpIsLevels(boolean xpIsLevels) { this.xpIsLevels = xpIsLevels; }
    public void setAdvancement(boolean advancement) { this.advancement = advancement; }
    public void setAdvancementId(String advancementId) { this.advancementId = advancementId; }
}