package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.map.MapCanvas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an image overlay pixel on a map, extending the base {@link MapPixel} class.
 * This class holds an image to be rendered at a specific (x, y) coordinate on the map.
 *
 * <p>The image can be serialized to and deserialized from a byte array in PNG format,
 * allowing it to be stored or transmitted as part of the map's data.</p>
 *
 * <p>Includes utility methods for converting between {@link Image} and
 * {@link BufferedImage}, and for handling the serialization of image data.</p>
 *
 * <p>Logging is used to capture and report any IOExceptions that occur during
 * serialization or deserialization.</p>
 *
 * @see MapPixel
 */
public class ImageOverlay extends MapPixel {
    private static final Logging logger = new Logging(ImageOverlay.class);
    private final Image imageId;

    /**
     * Constructs an ImageOverlay at the specified coordinates with the given image.
     *
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @param imageId The image to overlay at the specified location. Can be null.
     */
    public ImageOverlay(final int x, final int y, @Nullable final Image imageId) {
        super(x, y);
        this.imageId = imageId;
    }

    /**
     * Returns the image associated with this overlay.
     *
     * @return The {@link Image} instance, or null if no image is set.
     */
    @Nullable
    public Image getImage() {
        return imageId;
    }

    @Override
    public void render(final @Nonnull MapRendererData mapRendererData, @Nonnull final MapCanvas canvas) {
        final Image image = this.getImage();
        if (image != null) {
            canvas.drawImage(this.getX(), this.getY(), image);
        }
    }

    /**
     * Serializes this ImageOverlay into a map representation.
     * The image is converted into a PNG byte array for serialization.
     *
     * @return A map containing serialized fields of this object.
     */
    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type());
        map.put("x", getX());
        map.put("y", getY());
        if (imageId != null) {
            try {
                map.put("image", imageToBytes());
            } catch (IOException e) {
                logger.logError(e, () -> "Fail to serialize the image.");
            }
        }
        return map;
    }

    /**
     * Deserializes an ImageOverlay from a map representation.
     * Attempts to reconstruct the image from a byte array if available.
     *
     * @param map The map containing serialized ImageOverlay data.
     * @return A new instance of {@link ImageOverlay} with deserialized data.
     */
    public static ImageOverlay deserialize(Map<String, Object> map) {
        int x = (int) map.get("x");
        int y = (int) map.get("y");
        Object imageByte = map.get("image");

        Image image = null;
        try {
            image = ImageOverlay.getImageId((byte[]) imageByte);
        } catch (IOException e) {
            logger.logError(e, () -> "Fail to retrieve the image.");
        }

        return new ImageOverlay(x, y, image);
    }

    /**
     * Converts the stored {@link Image} into a byte array in PNG format.
     *
     * @return A byte array representing the image in PNG format, or an empty array if no image exists.
     * @throws IOException If an error occurs during writing the image data.
     */
    public byte[] imageToBytes() throws IOException {
        if (this.imageId == null)
            return new byte[0];

        final BufferedImage image = toBufferedImage(this.imageId);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        }
    }

    /**
     * Converts a byte array representing a PNG image back into an {@link Image} instance.
     *
     * @param data The byte array containing PNG image data.
     * @return The deserialized {@link Image}.
     * @throws IOException If an error occurs during reading the image data.
     */
    public static Image getImageId(byte[] data) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            return ImageIO.read(input);
        }
    }

    /**
     * Converts a generic {@link Image} to a {@link BufferedImage},
     * which allows direct pixel manipulation and image processing.
     *
     * <p>If the input image is already a {@link BufferedImage}, it is returned as is.
     * Otherwise, the image is fully loaded and copied into a new {@link BufferedImage}
     * with transparency support.</p>
     *
     * @param img The image to convert.
     * @return A {@link BufferedImage} instance of the input image.
     */
    public BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Ensure the image is fully loaded
        img = new ImageIcon(img).getImage();

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        // Draw the image onto the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

}