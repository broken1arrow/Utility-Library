package org.broken.arrow.library.serialize.utility;

import java.util.Objects;

/**
 * A simple generic container to hold a pair of objects.
 *
 * <p>This class stores two values, referred to as the first and second value,
 * which may be of different types. It provides standard methods for retrieval,
 * equality checking, hash code generation, and a string representation.</p>
 *
 * @param <K> the type of the first value
 * @param <V> the type of the second value
 */
public class Pair<K, V> {
	private final K firstValue;
	private final V secondValue;

	/**
	 * Constructs a new pair with the given values.
	 *
	 * @param first the first value in the pair
	 * @param second the second value in the pair
	 */
	public Pair(final K first, final V second) {
		this.firstValue = first;
		this.secondValue = second;
	}

	/**
	 * Static factory method to create a new pair.
	 *
	 * @param k the first value
	 * @param v the second value
	 * @param <K> the type of the first value
	 * @param <V> the type of the second value
	 * @return a new {@code Pair} containing the given values
	 */
	public static <K, V> Pair<K, V> of(final K k, final V v) {
		return new Pair<>(k, v);
	}

	/**
	 * Returns the first value of the pair.
	 *
	 * @return the first value
	 */
	public K getFirst() {
		return this.firstValue;
	}

	/**
	 * Returns the second value of the pair.
	 *
	 * @return the second value
	 */
	public V getSecond() {
		return this.secondValue;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(firstValue, pair.firstValue) && Objects.equals(secondValue, pair.secondValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstValue, secondValue);
	}

	@Override
	public String toString() {
		return String.format("{%s,%s}", this.firstValue, this.secondValue);
	}
}