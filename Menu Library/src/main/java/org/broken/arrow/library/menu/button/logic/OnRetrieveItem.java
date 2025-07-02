package org.broken.arrow.library.menu.button.logic;

import javax.annotation.Nullable;

@FunctionalInterface
public interface OnRetrieveItem<I, S, T> {

    I apply(S slot,@Nullable T fillObject);

}
