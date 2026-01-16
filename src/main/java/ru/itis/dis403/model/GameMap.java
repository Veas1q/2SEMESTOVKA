package ru.itis.dis403.model;

import ru.itis.dis403.ui.SpriteLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static ru.itis.dis403.config.GameConstants.WORLD_SIZE;

public class GameMap {

    public static final int TILE_SIZE = 8;
    private static final int BORDER_THICKNESS = 4;

    public BufferedImage background;
    public final List<Rectangle> walls = new ArrayList<>();
    public final List<Point> spawnPoints = new ArrayList<>();
    private final int[][] collisionMap = new int[32][32];

    public GameMap() {
        loadBackground();
        buildCollisionMap();
        initSpawnPoints();
        buildWorldBounds();
        buildWallsFromMap();
    }

    private void loadBackground() {
        try {
            background = SpriteLoader.load("/sprites/map/map1.png");
        } catch (Exception e) {
            System.out.println("Не удалось загрузить карту");
        }
    }

    private void buildCollisionMap() {

        collisionMap[14][14] = 1 ;
        collisionMap[14][13] = 1 ;
        collisionMap[15][13] = 1 ;
        collisionMap[16][13] = 1 ;

        collisionMap[14][17] = 1 ;
        collisionMap[14][18] = 1 ;
        collisionMap[15][18] = 1 ;
        collisionMap[16][18] = 1 ;
        collisionMap[17][18] = 1 ;

        collisionMap[19][13] = 1 ;
        collisionMap[20][13] = 1 ;
        collisionMap[21][13] = 1 ;
        collisionMap[22][13] = 1 ;
        collisionMap[22][14] = 1 ;


        collisionMap[20][18] = 1 ;
        collisionMap[21][18] = 1 ;
        collisionMap[22][18] = 1 ;
        collisionMap[22][17] = 1 ;


        collisionMap[2][5] = 1 ;
        collisionMap[3][5] = 1 ;
        collisionMap[4][5] = 1 ;

        collisionMap[5][2] = 1 ;
        collisionMap[5][3] = 1 ;
        collisionMap[5][4] = 1 ;
        collisionMap[5][5] = 1 ;


        collisionMap[27][5] = 1 ;
        collisionMap[28][5] = 1 ;
        collisionMap[29][5] = 1 ;

        collisionMap[26][2] = 1 ;
        collisionMap[26][3] = 1 ;
        collisionMap[26][4] = 1 ;
        collisionMap[26][5] = 1 ;


        collisionMap[5][29] = 1 ;
        collisionMap[5][28] = 1 ;
        collisionMap[5][27] = 1 ;
        collisionMap[5][26] = 1 ;


        collisionMap[2][25] = 1 ;
        collisionMap[3][25] = 1 ;
        collisionMap[4][25] = 1 ;
        collisionMap[5][25] = 1 ;

        collisionMap[26][29] = 1 ;
        collisionMap[26][28] = 1 ;
        collisionMap[26][27] = 1 ;
        collisionMap[26][26] = 1 ;


        collisionMap[29][26] = 1 ;
        collisionMap[28][26] = 1 ;
        collisionMap[27][26] = 1 ;
    }

    private void buildWorldBounds() {

        walls.add(new Rectangle(0, 0, WORLD_SIZE, BORDER_THICKNESS));
        walls.add(new Rectangle(0, WORLD_SIZE - BORDER_THICKNESS, WORLD_SIZE, BORDER_THICKNESS));
        walls.add(new Rectangle(0, 0, BORDER_THICKNESS, WORLD_SIZE));
        walls.add(new Rectangle(WORLD_SIZE - BORDER_THICKNESS, 0, BORDER_THICKNESS, WORLD_SIZE));
    }

    private void buildWallsFromMap() {
        for (int y = 0; y < collisionMap.length; y++) {
            for (int x = 0; x < collisionMap[y].length; x++) {
                if (collisionMap[y][x] == 1) {
                    walls.add(new Rectangle(
                            x * TILE_SIZE,
                            y * TILE_SIZE,
                            TILE_SIZE,
                            TILE_SIZE
                    ));
                }
            }
        }
    }


    private void initSpawnPoints() {

        int marginTile = 3;

        spawnPoints.add(tileCenter(marginTile, marginTile));

        spawnPoints.add(tileCenter(32 - marginTile - 1, marginTile));

        spawnPoints.add(tileCenter(marginTile, 32 - marginTile - 1));

        spawnPoints.add(tileCenter(32 - marginTile - 1, 32 - marginTile - 1));

        spawnPoints.add(tileCenter(16, 16));
    }

    private Point tileCenter(int tx, int ty) {
        return new Point(
                tx * TILE_SIZE + TILE_SIZE / 2,
                ty * TILE_SIZE + TILE_SIZE / 2
        );
    }

}
