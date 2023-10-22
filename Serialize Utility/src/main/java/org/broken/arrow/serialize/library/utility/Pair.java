package org.broken.arrow.serialize.library.utility;

import java.util.Objects;

public class Pair<First, Second> {
	private final First firstValue;
	private final Second secondValue;

	public Pair(final First first, final Second second) {
		this.firstValue = first;
		this.secondValue = second;
	}

	public static <First, Second> Pair<First, Second> of(final First first, final Second second) {
		return new Pair<>(first, second);
	}

	public First getFirst() {
		return this.firstValue;
	}

	public Second getSecond() {
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