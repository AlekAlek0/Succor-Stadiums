package net.alek.succorstadiums.arena;


public class WaveMob {

    private String mobType;
    private int count;

    // Constructor to create a waveMob with given mobType and count.
    public WaveMob(String mobType, int count) {
        this.mobType = mobType;
        this.count = count;
    }

    // Get the mobType of the Wavemob
    public String getMobType() {
        return mobType;
    }

    // Get mob count of the waveMob
    public int getCount() {
        return count;
    }

    // Set the wave mobType
    public void setMobType(String mobType) {
        this.mobType = mobType;
    }

    // Set the mob count of the waveMob
    public void setCount(int count) {
        this.count = count;
    }

}