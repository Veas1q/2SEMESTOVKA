package ru.itis.dis403;


import ru.itis.dis403.model.GameMap;
import ru.itis.dis403.ui.GameFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args) {
        String[] options = {"Host", "Client"};
        ImageIcon icon = new ImageIcon(Main.class.getResource("/sprites/icon/icon.png")); // JOptionPane требует тип ImageIcon

        int choice = JOptionPane.showOptionDialog(
                null,
                "Выберите режим",
                "MiniСS",
                JOptionPane.DEFAULT_OPTION, // свои кнопки из options, а не готовые
                JOptionPane.PLAIN_MESSAGE,
                icon,
                options,
                options[0] // выбран по дефолту хост
        );

        if (choice == 0) {
            new GameFrame(true, "localhost");
        } else {
            String ip = JOptionPane.showInputDialog("IP хоста:");
            if (ip == null || ip.isBlank()) {
                ip = "127.0.0.1";
            }
            new GameFrame(false, ip);
        }
    }
}
