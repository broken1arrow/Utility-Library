package org.broken.arrow.yaml.library.utillity;

public final class Valid extends RuntimeException {
	public static void checkBoolean(final boolean b, final String s) {
		if (!b)
			throw new CatchExceptions(s);
	}

	public static void checkNotNull(final Object b, final String s) {
		if (b == null)
			throw new CatchExceptions(s);
	}

	private static class CatchExceptions extends RuntimeException {
		public CatchExceptions(final String message) {
			super(message);
		}

		public CatchExceptions(final Throwable cause, final String message) {
			super(message, cause);
		}
	}
}