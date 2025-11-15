package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.map.MapCanvas;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private final Image image;

    /**
     * Creates an {@code ImageOverlay} at the specified pixel coordinates using the
     * provided image. The image will be automatically scaled down (max 128×128) to
     * ensure it fits on the map instead of being cropped.
     *
     * <p>This is the recommended constructor when you want automatic resizing
     * without manually preparing the image dimensions.</p>
     *
     * @param x     the x-coordinate on the map in pixels
     * @param y     the y-coordinate on the map in pixels
     * @param image the image to draw
     */
    public ImageOverlay(final int x, final int y, @Nonnull final Image image) {
        this(x, y,  image, true);
    }

    /**
     * Creates an {@code ImageOverlay} at the specified pixel coordinates using the
     * provided image, with optional automatic scaling.
     *
     * <p>If {@code scale} is {@code true}, the image will be resized to a maximum
     * of 128×128 pixels to avoid being clipped. If {@code false}, the image is used
     * as-is, and any portion exceeding the map size will be cut off. Use this
     * constructor if you want full control over pre-scaling your image.</p>
     *
     * @param x     the x-coordinate on the map in pixels
     * @param y     the y-coordinate on the map in pixels
     * @param image the image to draw
     * @param scale whether the image should be automatically resized to fit the map
     */
    public ImageOverlay(final int x, final int y, @Nonnull final Image image, boolean scale) {
        super(x, y);
        this.image = scale ? ItemCreator.scale((BufferedImage) image, 128, 128) : image;
    }

    /**
     * Returns the image associated with this overlay.
     *
     * @return The {@link Image} instance, or null if no image is set.
     */
    @Nonnull
    public Image getImage() {
        return image;
    }

    @Override
    public void render(final @Nonnull MapRendererData mapRendererData, @Nonnull final MapCanvas canvas) {
        final Image image = this.getImage();
        canvas.drawImage(this.getX(), this.getY(), image);
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
        if (image != null) {
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
        if (this.image == null)
            return new byte[0];

        final BufferedImage image = toBufferedImage(this.image);
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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final ImageOverlay that = (ImageOverlay) o;
        return Objects.equals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), image);
    }
}