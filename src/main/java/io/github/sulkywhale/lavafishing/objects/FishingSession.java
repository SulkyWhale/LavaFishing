package io.github.sulkywhale.lavafishing.objects;

import org.bukkit.entity.FishHook;

public final class FishingSession {

    private int startTime;
    private int waitTime;
    private final int lureTime;
    private final int lootPeriodLength;
    private final FishHook fishHook;
    private final boolean inNether;
    private boolean finishedWaitTime = false;

    public FishingSession(int startTime, int waitTime, int lureTime, int lootPeriodLength, FishHook fishHook, boolean inNether) {
        this.startTime = startTime;
        this.waitTime = waitTime;
        this.lureTime = lureTime;
        this.lootPeriodLength = lootPeriodLength;
        this.fishHook = fishHook;
        this.inNether = inNether;
    }

    public int getLootTime() {
        return startTime + waitTime + lureTime;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getLootPeriodLength() {
        return lootPeriodLength;
    }

    public FishHook getFishHook() {
        return fishHook;
    }

    public boolean hasFinishedWaitTime() {
        return finishedWaitTime;
    }

    public void setFinishedWaitTime(boolean finishedWaitTime) {
        this.finishedWaitTime = finishedWaitTime;
    }

    public boolean isInNether() {
        return inNether;
    }

}
