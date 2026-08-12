package net.alek.succorstadiums.arena;

// A single reward entry: an item (itemId + count), XP points, or XP levels.
public class RewardItem {

    private String itemId; // null/unused when xp == true
    private int count;
    private boolean xp;
    private boolean xpIsLevels; // only meaningful when xp == true; false = points, true = levels

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
    }

    public static RewardItem ofXpPoints(int amount) {
        return new RewardItem(null, amount, true, false);
    }

    public static RewardItem ofXpLevels(int amount) {
        return new RewardItem(null, amount, true, true);
    }

    public String getItemId() { return itemId; }
    public int getCount() { return count; }
    public boolean isXp() { return xp; }
    public boolean isXpLevels() { return xpIsLevels; }

    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setCount(int count) { this.count = count; }
    public void setXp(boolean xp) { this.xp = xp; }
    public void setXpIsLevels(boolean xpIsLevels) { this.xpIsLevels = xpIsLevels; }
}