package org.broken.arrow.title.update.library.utility;

public class Validate {

	public static void checkNotNull(Object checkNull) {
		if (checkNull == null)
			throw new CatchExceptions("Object is null ");
	}

	public static void checkNotNull(Object checkNull, String s) {
		if (checkNull == null)
			throw new CatchExceptions(s);
	}

	public static void checkNotNull(Object checkNull, String s, Throwable throwable) {
		if (checkNull == null)
			throw new CatchExceptions(throwable, s);
	}

	public static void checkNotEmpty(Object checkNull, String s) {
		if (checkNull != null && checkNull.equals(""))
			throw new CatchExceptions(s);
		if (checkNull == null)
			throw new CatchExceptions("Value should not be null.");
	}

	public static void checkBoolean(boolean bolen, String s) {
		if (bolen)
			throw new CatchExceptions(s);
	}

	public static void checkBoolean(boolean bolen, String s, Throwable throwable) {
		if (bolen)
			throw new CatchExceptions(throwable, s);
	}

	public static class CatchExceptions extends RuntimeException {

		public CatchExceptions(Throwable throwable, String message) {
			super(message, throwable);
		}

		public CatchExceptions(String message) {
			super(message);
		}
	}
}
