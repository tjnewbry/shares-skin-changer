package net.tjnewbry.skinhead.core;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class SkinFile {

    private final Path path;
    private final ArmType detected;

    private SkinFile(Path path, ArmType detected) {
        this.path = path;
        this.detected = detected;
    }

    /**
     * Validates a skin file and detects its arm type.
     *
     * @throws IllegalArgumentException with a user-readable message if the file is unusable
     */
    public static SkinFile load(Path path) {
        if (!path.toString().endsWith(".png")) {
            throw new IllegalArgumentException("File must be a .png");
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File does not exist");
        }

        BufferedImage img;
        try {
            img = ImageIO.read(path.toFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read image: " + e.getMessage());
        }
        if (img == null) {
            throw new IllegalArgumentException("File is not a valid image");
        }
        if (img.getWidth() != 64 || img.getHeight() != 64) {
            throw new IllegalArgumentException("Skin must be 64x64 pixels");
        }

        return new SkinFile(path, detectArmType(img));
    }

    // Each pair of pixels is only used by 4px arms, so a slim skin leaves both transparent.
    // Two or more transparent pairs means slim, so one stray pixel can't flip the result.
    private static ArmType detectArmType(BufferedImage img) {
        int slimHints = 0;
        if (transparent(img, 54, 20) && transparent(img, 54, 31)) slimHints++; // right arm
        if (transparent(img, 50, 16) && transparent(img, 50, 19)) slimHints++; // right hand
        if (transparent(img, 46, 52) && transparent(img, 46, 63)) slimHints++; // left arm
        if (transparent(img, 42, 48) && transparent(img, 42, 51)) slimHints++; // left hand
        return slimHints >= 2 ? ArmType.SLIM : ArmType.CLASSIC;
    }

    private static boolean transparent(BufferedImage img, int x, int y) {
        return ((img.getRGB(x, y) >> 24) & 0xFF) == 0;
    }

    public Path path() {
        return path;
    }

    public ArmType detectedArmType() {
        return detected;
    }
}
