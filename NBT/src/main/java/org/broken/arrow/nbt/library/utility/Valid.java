package org.broken.arrow.nbt.library.utility;

public class Valid {

	public static void checkNotNull(Object checkNull) {
		if (checkNull == null) {
			throw new CatchExceptions("Object is null ");
		}
	}

	public static void checkNotNull(Object checkNull, String s) {
		if (checkNull == null) {
			throw new CatchExceptions(s);
		}
	}

	public static void checkBoolean(boolean bolen, String s) {
		if (!bolen) {
			throw new CatchExceptions(s);
		}
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