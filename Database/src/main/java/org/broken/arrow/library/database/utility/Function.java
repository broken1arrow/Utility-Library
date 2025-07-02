package org.broken.arrow.library.database.utility;

public interface Function<T> {


    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the class instance you set as type.
     */
    T apply(T t);

}
