package net.alek.succorstadiums.arena;

public class WaveMob {

    private String mobType; // e.g. "minecraft:zombie"
    private int count;

    public WaveMob(String mobType, int count) {
        this.mobType = mobType;
        this.count = count;
    }

    public String getMobType() { return mobType; }
    public int getCount() { return count; }

    public void setMobType(String mobType) { this.mobType = mobType; }
    public void setCount(int count) { this.count = count; }

}