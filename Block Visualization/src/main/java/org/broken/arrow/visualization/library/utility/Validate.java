package org.broken.arrow.visualization.library.utility;

public class Validate {

	public static void checkNotNull(final Object checkNull) {
		if (checkNull == null)
			throw new CatchExceptions("Object is null ");
	}

	public static void checkNotNull(final Object checkNull, final String s) {
		if (checkNull == null)
			throw new CatchExceptions(s);
	}

	public static void checkNotEmpty(final Object checkNull, final String s) {
		if (checkNull != null && checkNull.equals(""))
			throw new CatchExceptions(s);
		if (checkNull == null)
			throw new CatchExceptions("Value should not be null.");
	}

	public static void checkBoolean(final boolean bolen, final String s) {
		if (bolen)
			throw new CatchExceptions(s);
	}

	public static class CatchExceptions extends RuntimeException {
		public CatchExceptions(final Throwable throwable, final String message) {
			super(message, throwable);
		}

		public CatchExceptions(final String message) {
			super(message);
		}
	}
}
