package org.broken.arrow.library.yaml.utillity;
/**
 * Utility class providing static validation methods that throw runtime exceptions
 * when conditions fail.
 * <p>
 * This class offers simple checks for boolean expressions and null references,
 * throwing a custom unchecked exception {@link CatchExceptions} with a provided
 * message if validation fails.
 * </p>
 * <p>
 * Note that {@code Valid} itself extends {@link RuntimeException}, but
 * the thrown exceptions are instances of the inner class {@link CatchExceptions}.
 * </p>
 */
public final class Valid extends RuntimeException {

	/**
	 * Checks a boolean condition and throws a {@link CatchExceptions} if the condition is false.
	 *
	 * @param b the boolean condition to check
	 * @param s the exception message if the condition is false
	 * @throws CatchExceptions if {@code b} is false
	 */
	public static void checkBoolean(final boolean b, final String s) {
		if (!b)
			throw new CatchExceptions(s);
	}

	/**
	 * Checks that the given object reference is not {@code null}, throwing
	 * a {@link CatchExceptions} if it is.
	 *
	 * @param b the object to check for nullity
	 * @param s the exception message if the object is {@code null}
	 * @throws CatchExceptions if {@code b} is {@code null}
	 */
	public static void checkNotNull(final Object b, final String s) {
		if (b == null)
			throw new CatchExceptions(s);
	}

	/**
	 * Private unchecked exception used internally by {@link Valid} to signal
	 * validation failures.
	 */
	private static class CatchExceptions extends RuntimeException {

		/**
		 * Constructs a new {@code CatchExceptions} with the specified detail message.
		 *
		 * @param message the detail message
		 */
		public CatchExceptions(final String message) {
			super(message);
		}

		/**
		 * Constructs a new {@code CatchExceptions} with the specified cause and detail message.
		 *
		 * @param cause the cause of the exception
		 * @param message the detail message
		 */
		public CatchExceptions(final Throwable cause, final String message) {
			super(message, cause);
		}
	}
}