package org.broken.arrow.library.serialize.utility;

import java.util.Objects;

/**
 * A generic container to hold a tuple of three objects.
 *
 * <p>This class stores three values, referred to as first, second, and third,
 * which may be of different types. It provides simple accessors and a string
 * representation for easy debugging and display.</p>
 *
 * @param <K> the type of the first value
 * @param <V> the type of the second value
 * @param <U> the type of the third value
 */
public class Tuple<K,U, V> {

	private final K firstValue;
	private final V secondValue;
	private final U thirdValue;

	/**
	 * Constructs a new tuple with the given values.
	 *
	 * @param first the first value in the tuple
	 * @param second the second value in the tuple
	 * @param third the third value in the tuple
	 */
	public Tuple(K first, V second, final U third) {
		this.firstValue = first;
		this.secondValue = second;
		this.thirdValue = third;
	}

	/**
	 * Returns the first value of the tuple.
	 *
	 * @return the first value
	 */
	public K first() {
		return firstValue;
	}

	/**
	 * Returns the second value of the tuple.
	 *
	 * @return the second value
	 */
	public V second() {
		return secondValue;
	}

	/**
	 * Returns the third value of the tuple.
	 *
	 * @return the third value
	 */
	public U third() {
		return thirdValue;
	}

	@Override
	public String toString() {
		return "Tuple{" +
				"firstValue=" + firstValue +
				", secondValue=" + secondValue +
				", thirdValue=" + thirdValue +
				'}';
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null || getClass() != object.getClass()) return false;
		final Tuple<?, ?, ?> tuple = (Tuple<?, ?, ?>) object;
		return Objects.equals(firstValue, tuple.firstValue) && Objects.equals(secondValue, tuple.secondValue) && Objects.equals(thirdValue, tuple.thirdValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstValue, secondValue, thirdValue);
	}
}
