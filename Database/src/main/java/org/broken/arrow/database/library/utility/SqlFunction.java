package org.broken.arrow.database.library.utility;


@FunctionalInterface
public interface SqlFunction<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the class instance you set as type.
     */
    T apply(T t);
}
