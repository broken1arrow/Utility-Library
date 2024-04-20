package org.broken.arrow.menu.library.button.logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface OnClick<T, P, M, C, I, F> {

    @Nonnull T apply(@Nonnull P player,@Nonnull M menu,@Nonnull C click,@Nonnull I clickedItem,@Nullable F fillObject);

}
