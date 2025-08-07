package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.meta.ColorMeta;
import org.broken.arrow.library.logging.Logging;

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

public class ImageOverlay extends MapPixel {
  private static final Logging logger = new Logging(ImageOverlay.class);
  private final Image imageId;


  public ImageOverlay(final int x, final int y, @Nullable final Image imageId) {
    super(x, y);
    this.imageId = imageId;
  }

  public Image getImage() {
    return imageId;
  }

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
        System.out.println("Fail to serialize the image ");
        e.printStackTrace();
      }
    }
    return map;
  }

  public static ImageOverlay deserialize(Map<String, Object> map) {
    int x = (int) map.get("x");
    int y = (int) map.get("y");
    Object imageByte = map.get("image");

    Image image = null;
    try {
      image = ImageOverlay.getImageId((byte[]) imageByte);
    } catch (IOException e) {
      logger.logError(e, ()-> "Fail to retrieve the image ");
    }

    return new ImageOverlay(x, y, image);
  }

  public byte[] imageToBytes() throws IOException {
    if (this.imageId == null)
      return null;

    final BufferedImage image = toBufferedImage(this.imageId);
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", output);
      return output.toByteArray();
    }
  }

  public static Image getImageId(byte[] data) throws IOException {
    try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
      return ImageIO.read(input);
    }
  }

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