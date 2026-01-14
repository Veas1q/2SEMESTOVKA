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

    public static BufferedImage crop(
            BufferedImage sheet,
            int col, int row,
            int frameW, int frameH
    ) {
        return sheet.getSubimage(
                col * frameW,
                row * frameH,
                frameW,
                frameH
        );
    }
}
