package org.broken.arrow.menu.library.utility;

/**
 *
 * @param <T> the type of data to return.
 * @deprecated is already better alternative in the Java already.
 */
@Deprecated
@FunctionalInterface
public interface Function<T> {

	T apply();
	
}
