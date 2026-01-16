package ru.itis.dis403.ui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpriteLoader {

    public static BufferedImage load(String path) {
        try {
            return ImageIO.read(SpriteLoader.class.getResource(path));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Не удалось загрузить спрайт: " + path);
            return null;
        }
    }
}
