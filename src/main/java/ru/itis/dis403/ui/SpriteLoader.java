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
<<<<<<< HEAD

=======
>>>>>>> 98e3899cb3a3f5e01c9678bbce961a31e207572d
}
