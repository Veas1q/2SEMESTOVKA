package ru.itis.dis403.model;

public class Weapon {

    public final int damage;
    public final int magazineSize;
    public int ammoInMagazine;
    public int ammoReserve;

    public Weapon(int damage, int magazineSize, int ammoReserve) {
        this.damage = damage;
        this.magazineSize = magazineSize;
        this.ammoReserve = ammoReserve;
        this.ammoInMagazine = magazineSize;
    }

    public boolean canShoot() {
        return ammoInMagazine > 0;
    }

    public void shoot() {
        ammoInMagazine--;
    }

    public void reload() {
        int needed = magazineSize - ammoInMagazine;
        int taken = Math.min(needed, ammoReserve);
        ammoInMagazine += taken;
        ammoReserve -= taken;
    }


    public static Weapon pistol() {
        return new Weapon(15, 12, 48);
    }
}
