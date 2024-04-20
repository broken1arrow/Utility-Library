package org.broken.arrow.menu.library.button.logic;


/**
 * Tell what type of update you want on your button.
 */
public enum ButtonUpdateAction {

    /**
     * All buttons get updated.
     */
    ALL,

    /**
     * Update only this button.
     */
    THIS,

    /**
     * Not update the button or buttons at all.
     */
    NONE,

}
