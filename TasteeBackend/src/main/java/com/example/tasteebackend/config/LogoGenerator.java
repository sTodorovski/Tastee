package com.example.tasteebackend.config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class LogoGenerator {

    public static String generateLogo(String restaurantName) throws IOException {
        int size = 512;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        Random random = new Random();
        Color background = new Color(
                random.nextInt(200),
                random.nextInt(200),
                random.nextInt(200)
        );

        graphics.setColor(background);
        graphics.fillRect(0, 0, size, size);

        String initials = getInitials(restaurantName);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 180));

        FontMetrics metrics = graphics.getFontMetrics();
        int x = (size - metrics.stringWidth(initials)) / 2;
        int y = (size - metrics.getHeight()) / 2 + metrics.getAscent();

        graphics.drawString(initials, x, y);
        graphics.dispose();

        String filename = restaurantName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase() + ".png";

        File folder = new File("src/main/resources/static/images/logos");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, filename);
        ImageIO.write(image, "png", file);

        return "/images/logos/" + filename;
    }

    private static String getInitials(String name) {
        String[] words = name.split(" ");

        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        }

        return (words[0].charAt(0) + "" + words[1].charAt(0)).toUpperCase();
    }
}