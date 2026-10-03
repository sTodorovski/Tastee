package com.example.tasteebackend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

@Component
@Order(1)
public class CoverGenerator implements CommandLineRunner {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 600;
    private static final int NUMBER_OF_COVERS = 20;

    @Override
    public void run(String... args) throws Exception {
        String folder = "src/main/resources/static/images/covers";
        File directory = new File(folder);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File[] existing = directory.listFiles();
        if (existing != null && existing.length >= NUMBER_OF_COVERS) {
            return;
        }

        Random random = new Random();

        for (int i = 1; i <= NUMBER_OF_COVERS; i++) {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            Color color1 = randomColor(random);
            Color color2 = randomColor(random);

            GradientPaint gradient = new GradientPaint(0, 0, color1, WIDTH, HEIGHT, color2);
            graphics.setPaint(gradient);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            for (int j = 0; j < 10; j++) {
                int size = random.nextInt(250) + 100;
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(HEIGHT);

                graphics.setColor(new Color(255, 255, 255, 40));
                graphics.fillOval(x, y, size, size);
            }

            graphics.dispose();

            File output = new File(folder + "/cover" + i + ".jpg");
            ImageIO.write(image, "jpg", output);
        }

    }

    private Color randomColor(Random random) {
        return new Color(
                random.nextInt(150) + 50,
                random.nextInt(150) + 50,
                random.nextInt(150) + 50
        );
    }
}