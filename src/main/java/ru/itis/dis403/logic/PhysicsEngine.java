package ru.itis.dis403.logic;

import ru.itis.dis403.model.Player;

import java.awt.*;

public class PhysicsEngine {

    private final GameState state;

    public PhysicsEngine(GameState state) {
        this.state = state;
    }

    public void updateBullets() {

        state.bullets.removeIf(b -> {

            b.x += b.vx;
            b.y += b.vy;


            for (Rectangle wall : state.map.walls) {
                if (wall.intersects(b.getHitbox())) {
                    return true;
                }
            }

            for (Player p : state.players.values()) {
                if (p.id == b.ownerId) continue;

                if (p.getHitbox().contains(b.x, b.y)) {
                    p.hp -= b.damage;
                    p.lastDamageFrom = b.ownerId;
                    return true;
                }
            }

            return false;
        });
    }
}
