package org.broken.arrow.color.library.interpolate;


/**
 * An interface that allows you to implement your own interpolation calculation when applying gradients.
 * This library also includes a built-in class {@link GradientInterpolation} that performs these calculations.
 */
@FunctionalInterface
public interface Interpolator {

	/**
	 * Interpolates between two values with linear progression.
	 *
	 * @param from The starting value.
	 * @param to The ending value.
	 * @param max The number of steps to interpolate.
	 * @return An array of interpolated values.
	 */
	double[] interpolate(double from, double to, int max);

}