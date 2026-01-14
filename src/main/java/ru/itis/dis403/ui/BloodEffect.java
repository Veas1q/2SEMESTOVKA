package ru.itis.dis403.ui;

public class BloodEffect {

    public int x, y;
    public long spawnTime;

    public static final long LIFE_TIME = 500;

    public BloodEffect(int x, int y) {
        this.x = x;
        this.y = y;
        this.spawnTime = System.currentTimeMillis();
    }

    public boolean isAlive(){
        return System.currentTimeMillis() - spawnTime < LIFE_TIME;
    }
}
