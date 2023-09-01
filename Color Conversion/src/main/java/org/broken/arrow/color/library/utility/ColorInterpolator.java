package org.broken.arrow.color.library.utility;

import org.broken.arrow.color.library.interpolate.Interpolator;

import javax.annotation.Nonnull;

/**
 * This interface provides methods for creating the necessary colors
 * for gradient calculations.
 */
public interface ColorInterpolator {

 /**
  * Get the red colors or hue if you use HSB.
  *
  * @param interpolator the calculation for the specific color.
  * @param stringLength the length of the string that should have the gradients set.
  * @return the array of colors.
  */
 double[] getRedColors(@Nonnull final Interpolator interpolator,final int stringLength);

 /**
  * Get the green colors or saturation if you use HSB.
  *
  * @param interpolator the calculation for the specific color.
  * @param stringLength the length of the string that should have the gradients set.
  * @return the array of colors.
  */
 double[] getGreenColors(@Nonnull final Interpolator interpolator,final int stringLength);

 /**
  * Get the blue colors or brightness if you use HSB.
  *
  * @param interpolator the calculation for the specific color.
  * @param stringLength the length of the string that should have the gradients set.
  * @return the array of colors.
  */
 double[] getBlueColors(@Nonnull final Interpolator interpolator, final int stringLength);

 /**
  * This method apply the color set.
  *
  * @param red the red color or hue if you use HSB.
  * @param green the green colors or saturation if you use HSB.
  * @param blue the blue colors or brightness if you use HSB.
  * @return should return a hex like this &lt;#D16BA5&gt;
  */
 String apply(double red,double green,double blue);

}
