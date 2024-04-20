package org.broken.arrow.menu.library.button.logic;

import javax.annotation.Nullable;

@FunctionalInterface
public interface OnRetrieveItem<I, S, T> {

    I apply(S slot,@Nullable T fillObject);

}
