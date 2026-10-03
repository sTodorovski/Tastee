package com.example.tasteebackend.config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ProfilePictureGenerator {

    private static final int SIZE = 512;

    public static String generateProfilePicture(
            String firstName,
            String lastName,
            String uniqueIdentifier
    ) throws IOException {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        Color background = generateColor(firstName, lastName);
        graphics.setColor(background);
        graphics.fillRect(0, 0, SIZE, SIZE);

        String initials = getInitials(firstName, lastName);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 180));

        FontMetrics metrics = graphics.getFontMetrics();
        int x = (SIZE - metrics.stringWidth(initials)) / 2;
        int y = (SIZE - metrics.getHeight()) / 2 + metrics.getAscent();

        graphics.drawString(initials, x, y);
        graphics.dispose();

        String filename = sanitizeFileName(
                firstName + "_" + lastName + "_" + uniqueIdentifier
        ) + ".png";

        File folder = new File("src/main/resources/static/images/profiles");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, filename);
        ImageIO.write(image, "png", file);

        return "/images/profiles/" + filename;
    }

    private static String getInitials(String firstName, String lastName) {
        String firstInitial = firstName.substring(0, 1).toUpperCase();
        String lastInitial = lastName.substring(0, 1).toUpperCase();
        return firstInitial + lastInitial;
    }

    private static Color generateColor(String firstName, String lastName) {
        String value = firstName.toLowerCase() + lastName.toLowerCase();
        int hash = value.hashCode();
        int red = 100 + Math.abs(hash % 156);
        int green = 100 + Math.abs((hash / 10) % 156);
        int blue = 100 + Math.abs((hash / 100) % 156);
        return new Color(red, green, blue);
    }

    private static String sanitizeFileName(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }
}