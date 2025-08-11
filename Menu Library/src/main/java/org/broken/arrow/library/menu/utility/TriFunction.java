package org.broken.arrow.library.menu.utility;

/**
 * Represents a function that takes three arguments and produces a result.
 *
 * @param <T> the type of the result of the function
 * @param <V> the type of the first argument to the function
 * @param <Z> the type of the second argument to the function
 * @param <X> the type of the third argument to the function
 */
public interface TriFunction<T, V, Z, X> {

    /**
     * Applies this function to the given arguments.
     *
     * @param v the first function argument
     * @param z the second function argument
     * @param x the third function argument
     * @return the function result
     */
    T apply(final V v, final Z z, final X x);

}
