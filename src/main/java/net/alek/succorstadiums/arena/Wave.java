package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    private int waveNumber;
    private final List<WaveMob> mobs;

    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.mobs = new ArrayList<>();
    }

    public int getWaveNumber() { return waveNumber; }
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }
    public List<WaveMob> getMobs() { return mobs; }

    public void addMob(String mobType, int count) {
        mobs.add(new WaveMob(mobType, count));
    }

    public int getTotalMobCount() {
        return mobs.stream().mapToInt(WaveMob::getCount).sum();
    }
}