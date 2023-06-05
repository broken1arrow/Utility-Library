package org.broken.arrow.menu.library.utility;

import javax.annotation.Nonnull;

public interface PairFunction<T> {
	@Nonnull
	Pair<T, Boolean> apply();

}
