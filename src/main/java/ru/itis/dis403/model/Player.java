package ru.itis.dis403.model;

import ru.itis.dis403.config.GameConstants;
import ru.itis.dis403.ui.SpriteLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Player {

    public int id;
    public float x, y;
    public int hp = 100;
    public boolean facingLeft = false;
    public int kills = 0;
    public String name;
    public int lastDamageFrom = -1;

    public BufferedImage sprite = SpriteLoader.load("/sprites/player/player_sheet.png");
    public Weapon weapon;

    public Player(int id, float x, float y, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = name;
        this.weapon = Weapon.pistol();
    }

    public Rectangle getHitbox() {
        return new Rectangle(
                (int) x + GameConstants.PLAYER_HITBOX_OFFSET_X,
                (int) y,
                GameConstants.PLAYER_HITBOX_WIDTH,
                GameConstants.PLAYER_HITBOX_HEIGHT
        );
    }

    public boolean isDead() {
        if (weapon.ammoReserve + weapon.ammoInMagazine == 0 || hp <= 0) {
            return true;
        }
        return false;
    }

    public void respawn(float x, float y) {
        this.x = x;
        this.y = y;
        this.hp = 100;
        this.weapon = Weapon.pistol();
        this.lastDamageFrom = -1;
    }
}
