package org.broken.arrow.library.logging;

public class Validate {

	private Validate() {
	}

	public static void checkNotNull(final Object checkNull) {
		if (checkNull == null)
			throw new ValidateExceptions("Object is null ");
	}

	public static void checkNotNull(final Object checkNull, final String s) {
		if (checkNull == null)
			throw new ValidateExceptions(s);
	}

	public static void checkNotEmpty(final Object checkNull, final String s) {
		if (checkNull != null && checkNull.equals(""))
			throw new ValidateExceptions(s);
		if (checkNull == null)
			throw new ValidateExceptions(s);
	}

	public static void checkBoolean(final boolean b, final String s) {
		if (b)
			throw new ValidateExceptions(s);
	}

	public static class ValidateExceptions extends RuntimeException {

		public ValidateExceptions(final Throwable throwable, final String message) {
			super(message, throwable);
		}
		public ValidateExceptions(final String message) {
			super(message);
		}
	}
}
