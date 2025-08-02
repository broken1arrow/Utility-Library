package org.broken.arrow.library.itemcreator.meta.map.pixel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PixelData extends MapPixel {
  private final Color color;

  public PixelData(final int x, final int y) {
    this(x, y, null);
  }

  public PixelData(final int x, final int y, @Nullable final Color color) {
    super(x, y);
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  @Nonnull
  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    map.put("type", type());
    map.put("x", getX());
    map.put("y", getY());
    if (color != null) {
      map.put("color", color.getRGB());
    }
    return map;
  }

  public static PixelData deserialize(Map<String, Object> map) {
    int x = (int) map.get("x");
    int y = (int) map.get("y");
    Object colorObject = map.get("color");
    Color color = colorObject != null ? new Color((int) colorObject) : null;
    return new PixelData(x, y, color);
  }
}