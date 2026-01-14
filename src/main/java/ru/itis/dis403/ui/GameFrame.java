package ru.itis.dis403.ui;

import ru.itis.dis403.logic.GameState;
import ru.itis.dis403.model.Bullet;
import ru.itis.dis403.model.Player;
import ru.itis.dis403.network.*;

import ru.itis.dis403.config.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static ru.itis.dis403.config.GameConstants.*;
import static ru.itis.dis403.network.Protocol.*;
public class GameFrame extends JFrame {

    private final GameState state = new GameState();
    private ClientConnection connection;

    private boolean gameStarted = false;
    private JButton restartButton;
    private JButton exitButton;
    private JButton startButton;
    private boolean isHost;
    private final LocalPlayer localPlayer = new LocalPlayer();

    private int myId = -1;
    private String playerName;

    private BufferedImage playerSprite;

    public GameFrame(boolean isHost, String ip) {
        this.isHost = isHost;

        playerName = JOptionPane.showInputDialog("Введите ваш ник:");
        if (playerName == null || playerName.isBlank()) {
            playerName = "Player";
        }

        if (isHost) {
            int minutes = Integer.parseInt(
                    JOptionPane.showInputDialog("Сколько минут длится игра?")
            );
            new HostServer(5000, minutes * 60_000L);
        }

        playerSprite = SpriteLoader.load("/sprites/player/player_sheet.png");
        if (playerSprite != null) {
            playerSprite = SpriteLoader.crop(playerSprite, 0, 0, 43, 70);
        }

        GamePanel panel = initButtons(isHost);


        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setFocusable(true);
        requestFocusInWindow();
        setFocusTraversalKeysEnabled(false);

        // ===== СЕТЬ =====
        // В GameFrame при создании ClientConnection мы передаём IP и порт,
        // чтобы указать, к какому серверу нужно подключиться.
        //
        // Третьим параметром передаётся лямбда-выражение — это реализация
        // интерфейса PacketListener.
        //
        // PacketListener выступает в роли callback-функции:
        // это код, который ClientConnection вызовет сам,
        // когда от сервера придёт сетевой пакет.
        //
        // ClientConnection сохраняет этот callback у себя.
        // В отдельном сетевом потоке он принимает сообщения от сервера,
        // десериализует строку в объект Packet
        // и вызывает callback (listener.onPacket).
        //
        // Таким образом управление временно передаётся в GameFrame,
        // где пакет обрабатывается, после чего управление
        // возвращается обратно в сетевой поток ClientConnection.

        connection = new ClientConnection(ip, 5000, packet -> {

            if (packet.type == PacketType.YOU) {
                myId = Integer.parseInt(packet.data);
                panel.setMyId(myId);

                connection.send(new Packet(PacketType.NAME, playerName));
                return;
            }

            if (packet.type == PacketType.STATE) {
                parseState(packet.data, panel);
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> localPlayer.up = true;
                    case KeyEvent.VK_S -> localPlayer.down = true;
                    case KeyEvent.VK_A -> { localPlayer.left = true; localPlayer.facingLeft = true; }
                    case KeyEvent.VK_D -> { localPlayer.right = true;localPlayer.facingLeft = false; }
                    case KeyEvent.VK_R -> connection.send(new Packet(PacketType.RELOAD, ""));
                    case KeyEvent.VK_TAB -> panel.setShowTab(true);

                }
            }

            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> localPlayer.up = false;
                    case KeyEvent.VK_S -> localPlayer.down = false;
                    case KeyEvent.VK_A -> localPlayer.left = false;
                    case KeyEvent.VK_D -> localPlayer.right = false;
                    case KeyEvent.VK_TAB -> panel.setShowTab(false);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { localPlayer.shooting = true; }
            public void mouseReleased(MouseEvent e) { localPlayer.shooting = false; }
        });

//        Таймер постоянно обрабатывает ввод, но пакеты отправляются только при изменении состояния или при допустимой стрельбе, чтобы не перегружать сервер.
        new Timer(16, e -> {

            if (!gameStarted) {
                return;
            }

            float nx = localPlayer.x;
            float ny = localPlayer.y;

            if (localPlayer.up) ny -= PLAYER_SPEED;
            if (localPlayer.down) ny += PLAYER_SPEED;
            if (localPlayer.left) nx -= PLAYER_SPEED;
            if (localPlayer.right) nx += PLAYER_SPEED;

            if (nx != localPlayer.x || ny != localPlayer.y) {
                localPlayer.x = nx;
                localPlayer.y = ny;
                connection.send(new Packet(
                        PacketType.MOVE,
                        localPlayer.x + "," + localPlayer.y + "," + (localPlayer.facingLeft ? 1 : 0)
                ));
            }

            if (localPlayer.shooting) {
                long now = System.currentTimeMillis();
                if (now - localPlayer.lastShotTime >= SHOOT_DELAY_MS) {

                    Point mouse = MouseInfo.getPointerInfo().getLocation();
                    Point window = getLocationOnScreen();
                    Point screen = new Point(mouse.x - window.x, mouse.y - window.y);
                    Point world = panel.screenToWorld(screen);

                    connection.send(new Packet(
                            PacketType.SHOOT,
                            localPlayer.x + "," + localPlayer.y + "," + world.x + "," + world.y
                    ));

                    localPlayer.lastShotTime = now;
                }
            }
        }).start();
    }

    private GamePanel initButtons(boolean isHost) {
        GamePanel panel = new GamePanel(state);
        restartButton = new JButton("Restart");
        exitButton = new JButton("Exit");

        panel.setLayout(null);

        int bw = 160;
        int bh = 35;

        restartButton.setBounds(
                (panel.getWidth() - bw) / 2,
                panel.getHeight() / 2 + 80,
                bw,
                bh
        );

        exitButton.setBounds(
                (panel.getWidth() - bw) / 2,
                panel.getHeight() / 2 + 120,
                bw,
                bh
        );

        restartButton.setVisible(false);
        exitButton.setVisible(false);

        panel.add(restartButton);
        panel.add(exitButton);

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int x = (panel.getWidth() - bw) / 2;
                restartButton.setBounds(x, panel.getHeight() / 2 + 80, bw, bh);
                exitButton.setBounds(x, panel.getHeight() / 2 + 120, bw, bh);
            }
        });

        restartButton.addActionListener(e -> {
            if (isHost) {
                connection.send(new Packet(PacketType.RESTART_GAME, ""));
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        add(panel);
        if (isHost) {
             startButton = new JButton("Начать игру");
            panel.setLayout(null);

            int bx = (panel.getWidth() - bw) / 2;
            int by = (panel.getHeight() - bh) / 2;

            startButton.setBounds(bx, by, bw, bh);
            if (startButton != null) {
                startButton.setVisible(isHost && !gameStarted && !panel.isGameOver());
            }
            panel.add(startButton);

            panel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int x = (panel.getWidth() - bw) / 2;
                    int y = (panel.getHeight() - bh) / 2;
                    startButton.setBounds(x, y, bw, bh);
                }
            });

            startButton.addActionListener(e -> {
                connection.send(new Packet(PacketType.START_GAME, ""));
            });
        }
        return panel;
    }

    private void parseTime(String raw, GamePanel panel) {
        int tIndex = raw.indexOf(TIME);
        int gIndex = raw.indexOf(GAME_OVER);

        if (tIndex != -1 && gIndex != -1 && tIndex < gIndex) {
            long seconds = Long.parseLong(raw.substring(tIndex + 3, gIndex));
            panel.setRemainingTime(seconds);
        }
    }
    private void parseGameFlags(String raw, GamePanel panel) {
        int sIndex = raw.indexOf(STARTED);
        int gIndex = raw.indexOf(GAME_OVER);

        if (gIndex != -1) {
            boolean over = raw.substring(gIndex + 3).startsWith("1");
            panel.setGameOver(over);
        }

        if (sIndex != -1) {
            boolean started = raw.substring(sIndex + 3).startsWith("1");
            this.gameStarted = started;
            panel.setGameStarted(started);
        }
    }
    private void parseWinner(String raw, GamePanel panel) {
        int wIndex = raw.indexOf(WINNER);
        if (wIndex != -1) {
            String winner = raw.substring(wIndex + 3);
            panel.setWinnerName(winner);
        }
    }
    private void parseLeaderboard(String raw, GamePanel panel) {
        int lIndex = raw.indexOf(LEADERBOARD);
        if (lIndex != -1) {
            String list = raw.substring(lIndex + 3);
            List<String> top = new ArrayList<>();
            for (String s : list.split(";")) {
                if (!s.isEmpty()) {
                    top.add(s);
                }
            }
            panel.setGlobalTop(top);
        }
    }
    private String cutServiceBlocks(String raw) {
        int cutIndex = raw.length();

        int t = raw.indexOf(TIME);
        int g = raw.indexOf(GAME_OVER);
        int s = raw.indexOf(STARTED);

        if (t != -1) {
            cutIndex = Math.min(cutIndex, t);
        }

        if (g != -1) {
            cutIndex = Math.min(cutIndex, g);
        }

        if (s != -1) {
            cutIndex = Math.min(cutIndex, s);
        }

        return raw.substring(0, cutIndex);
    }

    private void parsePlayers(String data) {
        int pStart = data.indexOf(PLAYERS);
        int bStart = data.indexOf(BULLETS);
        if (pStart == -1 || bStart == -1) return;

        String playersPart = data.substring(pStart + 2, bStart);

        for (String s : playersPart.split(";")) {
            if (s.isEmpty()) continue;

            String[] d = s.split(",");
            if (d.length < 8) continue;

            int id = Integer.parseInt(d[0]);
            float px = Float.parseFloat(d[1]);
            float py = Float.parseFloat(d[2]);
            int hp = Integer.parseInt(d[3]);
            int ammoMag = Integer.parseInt(d[4]);
            int ammoRes = Integer.parseInt(d[5]);
            boolean facingLeftServer = d[6].equals("1");

            int kills = 0;
            String name;

            if (d.length == 9) {
                kills = Integer.parseInt(d[7]);
                name = d[8];
            } else {
                name = d[7];
            }

            Player p = state.players.get(id);
            if (p == null) {
                p = new Player(id, px, py, playerSprite, name);
                state.players.put(id, p);
            }

            p.x = px;
            p.y = py;
            p.hp = hp;
            p.weapon.ammoInMagazine = ammoMag;
            p.weapon.ammoReserve = ammoRes;
            p.name = name;
            p.kills = kills;
            p.facingLeft = (id == myId) ? localPlayer.facingLeft : facingLeftServer;

            if (id == myId) {
                localPlayer.x = px;
                localPlayer.y = py;
            }
        }
    }

    private void parseBullets(String data) {
        int bStart = data.indexOf(BULLETS);
        if (bStart == -1) return;

        state.bullets.clear();
        String bulletsPart = data.substring(bStart + 3);

        for (String s : bulletsPart.split(";")) {
            if (s.isEmpty()) continue;
            String[] d = s.split(",");
            if (d.length < 2) continue;

            state.bullets.add(new Bullet(
                    Float.parseFloat(d[0]),
                    Float.parseFloat(d[1]),
                    0, 0, -1, 0
            ));
        }
    }

    private void updateButtons(GamePanel panel) {
        if (panel.isGameOver()) {
            exitButton.setVisible(true);
            restartButton.setVisible(isHost);
        } else {
            exitButton.setVisible(false);
            restartButton.setVisible(false);
        }

        if (startButton != null) {
            startButton.setVisible(isHost && !gameStarted && !panel.isGameOver());
        }
    }

    private void parseState(String raw, GamePanel panel) {

        parseTime(raw, panel);
        parseGameFlags(raw, panel);
        parseWinner(raw, panel);
        parseLeaderboard(raw, panel);

        String data = cutServiceBlocks(raw);
        parsePlayers(data);
        parseBullets(data);

        updateButtons(panel);
        panel.repaint();
    }



}
