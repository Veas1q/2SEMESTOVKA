package ru.itis.dis403.network;

import static ru.itis.dis403.network.Protocol.*;
import ru.itis.dis403.logic.GameLoop;
import ru.itis.dis403.logic.GameState;
import ru.itis.dis403.logic.Leaderboard;
import ru.itis.dis403.logic.PhysicsEngine;
import ru.itis.dis403.model.*;
import ru.itis.dis403.ui.SpriteLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class HostServer {

    private final long gameDurationMs;
    private long startTime;

    private boolean gameStarted = false;
    private boolean gameOver = false;
    private String winnerName = "";

    private final GameState state = new GameState();
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Random random = new Random();
    private final PhysicsEngine physics;

    private int nextId = 1;
    private BufferedImage playerSprite;

    public HostServer(int port, long gameDurationMs) {
        this.gameDurationMs = gameDurationMs;
        this.physics = new PhysicsEngine(state);

        BufferedImage ps = SpriteLoader.load("/sprites/player/player_sheet.png");
        if (ps != null) {
            playerSprite = SpriteLoader.crop(ps, 0, 0, 43, 70);
        }

        new GameLoop(state, this::update).start();

        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                while (true) {
                    Socket socket = server.accept();
                    ClientHandler handler = new ClientHandler(socket, nextId++);
                    clients.add(handler);
                    handler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void update() {

        if (!gameStarted || gameOver) {
            broadcastState();
            return;
        }

        physics.updateBullets();
        checkDeaths();

        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

        if (elapsed >= gameDurationMs) {
            finishGame();
        }

        broadcastState();
    }

    private void finishGame() {
        gameOver = true;

        for (Player p : state.players.values()) {
            Leaderboard.addKills(p.name, p.kills);
        }

        Player winner = null;
        for (Player p : state.players.values()) {
            if (winner == null || p.kills > winner.kills) {
                winner = p;
            }
        }

        if (winner != null) {
            winnerName = winner.name;
        }
    }

    private void checkDeaths() {
        for (Player p : state.players.values()) {
            if (p.isDead()) {
                Player killer = state.players.get(p.lastDamageFrom);
                if (killer != null && killer.id != p.id) {
                    killer.kills++;
                }
                respawnPlayer(p);
            }
        }
    }


    private void broadcastState() {

        long remainingMs = Math.max(
                0,
                gameDurationMs - (System.currentTimeMillis() - startTime)
        );

        StringBuilder sb = new StringBuilder();

        sb.append(PLAYERS);
        state.players.values().forEach(p ->
                sb.append(p.id).append(",")
                        .append(p.x).append(",")
                        .append(p.y).append(",")
                        .append(p.hp).append(",")
                        .append(p.weapon.ammoInMagazine).append(",")
                        .append(p.weapon.ammoReserve).append(",")
                        .append(p.facingLeft ? 1 : 0).append(",")
                        .append(p.kills).append(",")
                        .append(p.name).append(";")
        );

        sb.append(BULLETS);
        state.bullets.forEach(b ->
                sb.append(b.x).append(",")
                        .append(b.y).append(";")
        );

        sb.append(STARTED).append(gameStarted ? 1 : 0);
        sb.append(TIME).append(remainingMs / 1000);
        sb.append(GAME_OVER).append(gameOver ? 1 : 0);

        sb.append(LEADERBOARD);
        Leaderboard.top5().forEach(e ->
                sb.append(e.getKey()).append(",")
                        .append(e.getValue()).append(";")
        );

        sb.append(WINNER).append(winnerName);

        Packet packet = new Packet(PacketType.STATE, sb.toString());
        clients.forEach(c -> c.send(packet));
    }



    class ClientHandler extends Thread {

        BufferedReader in;
        PrintWriter out;
        int id;

        ClientHandler(Socket socket, int id) throws IOException {
            this.id = id;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Player p = new Player(id, 0, 0, playerSprite, "");
            respawnPlayer(p);
            state.players.put(id, p);

            send(new Packet(PacketType.YOU, String.valueOf(id)));
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {

                    Packet packet = Packet.deserialize(line);
                    Player pl = state.players.get(id);

                    if (packet.type == PacketType.START_GAME) {
                        gameStarted = true;
                        gameOver = false;
                        startTime = System.currentTimeMillis();
                        continue;
                    }

                    if (!gameStarted &&
                            (packet.type == PacketType.MOVE || packet.type == PacketType.SHOOT)) {
                        continue;
                    }

                    if (packet.type == PacketType.NAME) {
                        if (pl != null) pl.name = packet.data;
                        continue;
                    }

                    if (packet.type == PacketType.MOVE) {
                        handleMove(pl, packet.data);
                    }

                    if (packet.type == PacketType.SHOOT) {
                        handleShoot(pl, packet.data);
                    }

                    if (packet.type == PacketType.RELOAD) {
                        if (pl != null) pl.weapon.reload();
                    }

                    if (packet.type == PacketType.RESTART_GAME) {
                        restartGame();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void send(Packet p) {
            out.println(p.serialize());
        }
    }


    private void handleMove(Player pl, String data) {
        if (pl == null) return;

        String[] d = data.split(",");
        float newX = Float.parseFloat(d[0]);
        float newY = Float.parseFloat(d[1]);

        if (d.length > 2) {
            pl.facingLeft = d[2].equals("1");
        }

        float oldX = pl.x;
        float oldY = pl.y;

        pl.x = newX;
        pl.y = newY;

        Rectangle hitbox = pl.getHitbox();

        for (Rectangle wall : state.map.walls) {
            if (wall.intersects(hitbox)) {
                pl.x = oldX;
                pl.y = oldY;
                return;
            }
        }
    }


    private void handleShoot(Player pl, String data) {
        if (pl == null || !pl.weapon.canShoot()) return;

        String[] d = data.split(",");
        float targetX = Float.parseFloat(d[2]);
        float targetY = Float.parseFloat(d[3]) - 4;

        if (pl.facingLeft && targetX > pl.x + 10) return;
        if (!pl.facingLeft && targetX < pl.x) return;

        float gunX = pl.x;
        float gunY = pl.y + 4;

        float dx = targetX - gunX;
        float dy = targetY - gunY;

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;

        dx /= len;
        dy /= len;

        Bullet bullet = new Bullet(
                gunX,
                gunY,
                dx * 6f,
                dy * 6f,
                pl.id,
                pl.weapon.damage
        );

        pl.weapon.shoot();
        state.bullets.add(bullet);
    }

    private void restartGame() {
        gameStarted = false;
        gameOver = false;
        winnerName = "";
        startTime = System.currentTimeMillis();

        for (Player p : state.players.values()) {
            p.hp = 100;
            p.kills = 0;
            p.lastDamageFrom = -1;
            p.weapon = Weapon.pistol();
            respawnPlayer(p);
        }

        state.bullets.clear();
    }

    private void respawnPlayer(Player p) {
        if (state.map.spawnPoints.isEmpty()) return;

        Point spawn = state.map.spawnPoints.get(
                random.nextInt(state.map.spawnPoints.size())
        );

        p.x = spawn.x;
        p.y = spawn.y;
        p.hp = 100;
        p.weapon = Weapon.pistol();
        p.lastDamageFrom = -1;
    }
}
