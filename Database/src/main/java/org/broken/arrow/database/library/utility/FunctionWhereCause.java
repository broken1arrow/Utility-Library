package org.broken.arrow.database.library.utility;

public interface FunctionWhereCause<T,V> {


    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the class instance you set as type.
     */
    V apply(T t);

}
