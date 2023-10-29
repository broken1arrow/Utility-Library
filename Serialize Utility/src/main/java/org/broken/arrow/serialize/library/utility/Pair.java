package org.broken.arrow.serialize.library.utility;

import java.util.Objects;

public class Pair<K, V> {
	private final K firstValue;
	private final V secondValue;

	public Pair(final K first, final V second) {
		this.firstValue = first;
		this.secondValue = second;
	}

	public static <K, V> Pair<K, V> of(final K k, final V v) {
		return new Pair<>(k, v);
	}

	public K getFirst() {
		return this.firstValue;
	}

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