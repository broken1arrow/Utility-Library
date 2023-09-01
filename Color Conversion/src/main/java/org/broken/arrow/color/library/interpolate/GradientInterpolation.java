package org.broken.arrow.color.library.interpolate;

/**
 * A utility class that provides interpolation methods for color calculations when applying gradients.
 * These methods assist in generating color values between two given values.
 */
public class GradientInterpolation implements Interpolator {


	@Override
	public double[] interpolate(double from, double to, int max) {
		final double[] res = new double[max];
		for (int i = 0; i < max; i++) {
			res[i] = from + i * ((to - from) / (max - 1));
		}
		return res;
	}

	/**
	 * Generates interpolated values using a quadratic function.
	 *
	 * @param from The starting value.
	 * @param to The ending value.
	 * @param max The number of steps to interpolate.
	 * @param mode The mode indicating whether to generate values increasing or decreasing.
	 * @return An array of interpolated values.
	 */
	public double[] quadratic(double from, double to, int max, boolean mode) {
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
