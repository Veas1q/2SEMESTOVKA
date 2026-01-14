package ru.itis.dis403.model;

import ru.itis.dis403.config.GameConstants;

import java.awt.*;

public class Bullet {

    public float x, y;
    public float vx, vy;
    public int ownerId;
    public int damage;

    public Bullet(float x, float y, float vx, float vy, int ownerId, int damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.ownerId = ownerId;
        this.damage = damage;
    }

    public Rectangle getHitbox() {
        return new Rectangle(
                (int) x,
                (int) y,
                GameConstants.BULLET_SIZE,
                GameConstants.BULLET_SIZE
        );
    }
}
