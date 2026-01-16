package ru.itis.dis403.ui;

import ru.itis.dis403.logic.GameState;
import ru.itis.dis403.model.Bullet;
import ru.itis.dis403.model.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.itis.dis403.config.GameConstants.PLAYER_SPRITE_DRAW_SIZE;
import static ru.itis.dis403.config.GameConstants.WORLD_SIZE;

public class GamePanel extends JPanel {

    private final GameState state;
    private int myId = -1;
    private boolean gameOver = false;
    private long remainingTime = 0;
    private boolean gameStarted = false;
    private String winnerName = "";
    private boolean showTab = false;
    private List<String> globalTop = new ArrayList<>();

    private final List<BloodEffect> blood = new ArrayList<>();
    private final Map<Integer, Integer> lastHp = new HashMap<>();

    public GamePanel(GameState state) {
        this.state = state;
        setBackground(Color.BLACK);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean value) {
        this.gameOver = value;
    }

    public void setGlobalTop(List<String> top) {
        this.globalTop = top;
        repaint();
    }


    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        repaint();
    }

    public void setShowTab(boolean show) {
        this.showTab = show;
        repaint();
    }


    public void setWinnerName(String name) {
        this.winnerName = name;
        repaint();
    }

    public void setRemainingTime(long seconds) {
        this.remainingTime = seconds;
    }

    public void setMyId(int id) {
        this.myId = id;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        applyWorldTransform(g2);

        drawMap(g2);
        drawPlayers(g2);
        drawBullets(g2);
        drawBlood(g2);

        drawHud(g);
        drawGlobalTop(g);

        if (gameOver) drawGameOverOverlay(g);
        if (!gameStarted) drawLobbyOverlay(g);
        if (showTab) drawTab(g);

        g2.dispose();
    }


    public Point screenToWorld(Point screen) {

        float scale = Math.min(
                getWidth() / (float) WORLD_SIZE,
                getHeight() / (float) WORLD_SIZE
        );

        int offsetX = (int) ((getWidth() - WORLD_SIZE * scale) / 2);
        int offsetY = (int) ((getHeight() - WORLD_SIZE * scale) / 2);

        int wx = (int) ((screen.x - offsetX) / scale);
        int wy = (int) ((screen.y - offsetY) / scale);

        return new Point(wx, wy);
    }
    private void applyWorldTransform(Graphics2D g2) {
        float scale = Math.min(
                getWidth() / (float) WORLD_SIZE,
                getHeight() / (float) WORLD_SIZE
        );

        int offsetX = (int) ((getWidth() - WORLD_SIZE * scale) / 2);
        int offsetY = (int) ((getHeight() - WORLD_SIZE * scale) / 2);

        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);
    }
    private void drawMap(Graphics2D g2) {
        if (state.map.background != null) {
            g2.drawImage(state.map.background, 0, 0, null);
        }
    }
    private void drawPlayers(Graphics2D g2) {
        for (Player p : state.players.values()) {

            int prevHp = lastHp.getOrDefault(p.id, p.hp);
            if (p.hp < prevHp) {
                for (int i = 0; i < 8; i++) {
                    blood.add(new BloodEffect(
                            (int) p.x + 2 + (int)(Math.random() * 6),
                            (int) p.y + (int)(Math.random() * 10)
                    ));
                }
            }
            lastHp.put(p.id, p.hp);

            int drawSize = PLAYER_SPRITE_DRAW_SIZE;
            if (p.sprite != null) {
                if (p.facingLeft) {
                    g2.drawImage(
                            p.sprite,
                            (int) p.x + drawSize,
                            (int) p.y,
                            -drawSize,
                            drawSize,
                            null
                    );
                } else {
                    g2.drawImage(
                            p.sprite,
                            (int) p.x,
                            (int) p.y,
                            drawSize,
                            drawSize,
                            null
                    );
                }
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 4));
            g2.setColor(Color.WHITE);
            g2.drawString(p.name, (int) p.x - 1, (int) p.y - 5);

            int hx = (int) p.x + 2;
            int hy = (int) p.y;

            int barWidth = 10;
            int barHeight = 1;

            int hpX = hx - 3;
            int hpY = hy - 3;

            g2.setColor(Color.RED);
            g2.fillRect(hpX, hpY, barWidth, barHeight);

            g2.setColor(Color.GREEN);
            g2.fillRect(
                    hpX,
                    hpY,
                    (int) (barWidth * (p.hp / 100f)),
                    barHeight
            );
        }
    }
    private void drawBullets(Graphics2D g2) {
        g2.setColor(Color.YELLOW);
        for (Bullet b : state.bullets) {
            int size = 4;
            g2.fillOval(
                    (int) b.x - size / 2,
                    (int) b.y - size / 2,
                    size,
                    size
            );
        }
    }
    private void drawBlood(Graphics2D g2) {
        long now = System.currentTimeMillis();
        blood.removeIf(b -> !b.isAlive());

        for (BloodEffect b : blood) {
            float life = (now - b.spawnTime) / 500f;
            int alpha = (int) (200 * (1f - life));

            g2.setColor(new Color(180, 0, 0, alpha));
            g2.fillRect(b.x, b.y, 2, 2);
        }
    }
    private void drawHud(Graphics g) {
        Player me = state.players.get(myId);
        if (me == null) return;

        g.setColor(Color.WHITE);
        g.drawString("HP: " + me.hp, 10, 20);
        g.drawString("Патроны: " + me.weapon.ammoInMagazine + " / " + me.weapon.ammoReserve, 10, 40);

        g.drawString("W - ВВЕРХ", 10, 60);
        g.drawString("S - ВНИЗ", 10, 80);
        g.drawString("D - ВПРАВО", 10, 100);
        g.drawString("A - ВЛЕВО", 10, 120);
        g.drawString("R - ПЕРЕЗАРЯДКА", 10, 140);
        g.drawString("TAB - ТАБЛИЦА ЛИДЕРОВ", 10, 160);


        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Осталось времени: " + remainingTime + " с", getWidth() - 140, 20);
    }
    private void drawGlobalTop(Graphics g) {
        int panelRightX = getWidth() - 100;
        int panelTopY = 60;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(panelRightX - 10, panelTopY - 30, 100, 160, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("ЛУЧИЕ ИГРОКИ", panelRightX - 10, panelTopY - 20);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int y = panelTopY;
        int place = 1;

        for (String s : globalTop) {
            if (place > 5) break;
            String[] p = s.split(",");
            if (p.length != 2) continue;

            g.drawString(place + ". " + p[0] + " — " + p[1], panelRightX, y);
            y += 22;
            place++;
        }

    }

    private void drawGameOverOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Игра завершена", getWidth() / 2 - 110, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Победил: " + winnerName, getWidth() / 2 - 80, 250);
    }

    private void drawLobbyOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Лобби", getWidth() / 2 - 40, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Ждем пока хост начнет игру", getWidth() / 2 - 100, 240);
    }
    private void drawTab(Graphics g) {
        int w = 400;
        int h = 300;
        int x = (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;

        g.setColor(new Color(255, 255, 255, 180));
        g.fillRoundRect(x, y, w, h, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("ТАБЛИЦА ЛИДЕРОВ", x + 120, y + 30);

        List<Player> players = new ArrayList<>(state.players.values());
        players.sort((a, b) -> Integer.compare(b.kills, a.kills));

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        int yy = y + 80;

        for (Player p : players) {
            g.drawString(p.name + " — " + p.kills, x + 40, yy);
            yy += 26;
        }
    }
}
