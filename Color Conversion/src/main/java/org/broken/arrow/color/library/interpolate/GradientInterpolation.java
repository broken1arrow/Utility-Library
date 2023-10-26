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

}
