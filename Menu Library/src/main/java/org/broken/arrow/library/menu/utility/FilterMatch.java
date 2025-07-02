package org.broken.arrow.library.menu.utility;

public enum FilterMatch {
    /**
     * Only the itemStack type need to match.
     */
    TYPE,

    /**
     * All set data on the itemStack need to match.
     */
    META,

    /**
     * Only the set name,lore and type need too match the itemStack.
     */
    TYPE_NAME_LORE

}
