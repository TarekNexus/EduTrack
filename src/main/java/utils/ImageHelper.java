package utils;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class ImageHelper {

    /**
     * Stored under the user's home directory (not a path relative to the
     * working directory) so a student's photo keeps resolving no matter how
     * the app is launched next time (mvnw exec:java, the packaged jar, or an
     * IDE run configuration each use a different working directory).
     */
    private static final Path ASSETS_DIR =
        Paths.get(System.getProperty("user.home"), ".edutrack", "images");

    private ImageHelper() {
    }

    /** Copies the chosen photo into the app's image store and returns its absolute path, or null on failure. */
    public static String copyToAssets(File source, String studentCode) {
        try {
            Files.createDirectories(ASSETS_DIR);
            String extension = extensionOf(source.getName());
            String safeCode = studentCode.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safeCode + "_" + System.currentTimeMillis() + extension;
            Path target = ASSETS_DIR.resolve(fileName);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            return null;
        }
    }

    /** Loads and scales an image from disk, or returns null if the path is missing/unreadable. */
    public static ImageIcon loadScaledIcon(String path, int width, int height) {
        if (path == null || path.isBlank()) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null;
            }
            java.awt.Image scaled = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            return null;
        }
    }

    /** Like {@link #loadScaledIcon}, but clipped to a circle for a nicer avatar look. */
    public static ImageIcon loadCircularIcon(String path, int size) {
        ImageIcon square = loadScaledIcon(path, size, size);
        return square == null ? null : toCircular(square, size);
    }

    private static ImageIcon toCircular(ImageIcon source, int size) {
        BufferedImage circular = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circular.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(source.getImage(), 0, 0, size, size, null);
        g2.dispose();
        return new ImageIcon(circular);
    }

    /** Draws a simple circular avatar with initials, used when a student has no photo. */
    public static ImageIcon placeholderIcon(String displayName, int size, Color background) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(background);
        g2.fillOval(0, 0, size, size);

        String initials = initialsOf(displayName);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
        var metrics = g2.getFontMetrics();
        int x = (size - metrics.stringWidth(initials)) / 2;
        int y = (size - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(initials, x, y);
        g2.dispose();
        return new ImageIcon(image);
    }

    private static String initialsOf(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : ".png";
    }
}
