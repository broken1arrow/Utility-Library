package org.broken.arrow.library.itemcreator.utility;

/**
 * A simple generic tuple class that holds two related values.
 *
 * @param <K> the type of the first value
 * @param <V> the type of the second value
 */
public class Tuple<K, V> {

	private final K firstValue;
	private final V secondValue;

	/**
	 * Constructs a new {@code Tuple} with the specified values.
	 *
	 * @param firstValue  the first value of the tuple
	 * @param secondValue the second value of the tuple
	 */
	public Tuple(K firstValue, V secondValue) {

		this.firstValue = firstValue;
		this.secondValue = secondValue;
	}

	/**
	 * Returns the first value in the tuple.
	 *
	 * @return the first value
	 */
	public K getFirst() {
		return firstValue;
	}

	/**
	 * Returns the second value in the tuple.
	 *
	 * @return the second value
	 */
	public V getSecond() {
		return secondValue;
	}

	/**
	 * Returns a string representation of this tuple, consisting of
	 * the string representations of the first and second values
	 * separated by an underscore ('_').
	 *
	 * @return a string representation of this tuple
	 */
	@Override
	public String toString() {
		return firstValue + "_" + secondValue;
	}
}
