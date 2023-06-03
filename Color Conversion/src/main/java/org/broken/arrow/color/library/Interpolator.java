package org.broken.arrow.color.library;

@FunctionalInterface
public interface Interpolator {

	double[] interpolate(double from, double to, int max);

}