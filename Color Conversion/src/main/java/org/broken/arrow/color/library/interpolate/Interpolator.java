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

	/**
	 * Generates interpolated values using a quadratic function.
	 *
	 * @param from The starting value.
	 * @param to The ending value.
	 * @param max The number of steps to interpolate.
	 * @param mode The mode indicating whether to generate values increasing or decreasing.
	 * @return An array of interpolated values.
	 */
	default double[] quadratic(double from, double to, int max, boolean mode){
		final double[] results = new double[max];
		if (mode) {
			double a = (to - from) / (max * max);
			for (int i = 0; i < results.length; i++) {
				results[i] = a * i * i + from;
			}
		} else {
			double a = (from - to) / (max * max);
			double b = -2 * a * max;
			for (int i = 0; i < results.length; i++) {
				results[i] = a * i * i + b * i + from;
			}
		}
		return results;
	}

}