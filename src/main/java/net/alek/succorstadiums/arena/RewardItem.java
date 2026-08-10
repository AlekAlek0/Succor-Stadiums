package net.alek.succorstadiums.arena;

// A single reward item entry: an item id and how many to give.
public class RewardItem {

    private String itemId;
    private int count;

    public RewardItem(String itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }

    public String getItemId() { return itemId; }
    public int getCount() { return count; }

    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setCount(int count) { this.count = count; }
}