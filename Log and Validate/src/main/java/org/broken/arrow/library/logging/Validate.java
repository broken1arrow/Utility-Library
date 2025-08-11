package org.broken.arrow.library.logging;

/**
 * Utility class providing static methods for common validation checks.
 * <p>
 * This class is not instantiable and provides methods to validate conditions such as
 * null checks, empty checks, and boolean assertions. If a validation fails,
 * a {@link ValidateExceptions} runtime exception is thrown with an appropriate message.
 * </p>
 */
public class Validate {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Validate() {
	}

	/**
	 * Checks that the specified object reference is not null.
	 *
	 * @param checkNull the object reference to check for nullity
	 * @throws ValidateExceptions if {@code checkNull} is {@code null}
	 */
	public static void checkNotNull(final Object checkNull) {
		if (checkNull == null)
			throw new ValidateExceptions("Object is null ");
	}

	/**
	 * Checks that the specified object reference is not null.
	 *
	 * @param checkNull the object reference to check for nullity
	 * @param message the exception message to use if the check fails
	 * @throws ValidateExceptions if {@code checkNull} is {@code null}
	 */
	public static void checkNotNull(final Object checkNull, final String message) {
		if (checkNull == null)
			throw new ValidateExceptions(message);
	}

	/**
	 * Checks that the specified object is not null or an empty string.
	 *
	 * @param checkNull the object reference to check
	 * @param message the exception message to use if the check fails
	 * @throws ValidateExceptions if {@code checkNull} is {@code null} or equals to the empty string
	 */
	public static void checkNotEmpty(final Object checkNull, final String message) {
		if (checkNull != null && checkNull.equals(""))
			throw new ValidateExceptions(message);
		if (checkNull == null)
			throw new ValidateExceptions(message);
	}

	/**
	 * Checks that the specified boolean condition is false.
	 *
	 * @param condition the boolean condition to check
	 * @param message the exception message to use if the check fails (i.e., condition is true)
	 * @throws ValidateExceptions if {@code condition} is {@code true}
	 */
	public static void checkBoolean(final boolean condition, final String message ) {
		if (condition)
			throw new ValidateExceptions(message );
	}

	/**
	 * Runtime exception thrown by the {@link Validate} class when validation fails.
	 */
	public static class ValidateExceptions extends RuntimeException {

		/**
		 * Constructs a new validation exception with the specified detail message and cause.
		 *
		 * @param throwable the cause of the exception
		 * @param message the detail message
		 */
		public ValidateExceptions(final Throwable throwable, final String message) {
			super(message, throwable);
		}

		/**
		 * Constructs a new validation exception with the specified detail message.
		 *
		 * @param message the detail message
		 */
		public ValidateExceptions(final String message) {
			super(message);
		}
	}
}
