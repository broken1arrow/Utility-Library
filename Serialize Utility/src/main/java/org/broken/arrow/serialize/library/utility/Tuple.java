package org.broken.arrow.serialize.library.utility;

public class Tuple<K,U, V> {

	private final K firstValue;
	private final V secondValue;
	private final U thirdValue;

	public Tuple(K first, V second, final U third) {
		this.firstValue = first;
		this.secondValue = second;
		this.thirdValue = third;
	}

	public K first() {
		return firstValue;
	}

	public V second() {
		return secondValue;
	}

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
}
