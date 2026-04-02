package org.broken.arrow.library.menu.event.update;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simple event dispatcher for update triggers within a menu lifecycle.
 *
 * <p>Listeners are intended to be registered during menu setup and will be
 * invoked every time {@link #markUpdated()} is called.</p>
 *
 */
public class UpdateEvent {
    private final List<Runnable> listeners = new ArrayList<>();

    /**
     * Registers a listener that will be invoked on update.
     *
     * @param listener the listener to invoke when an update occurs
     */
    public void addListener(@Nonnull final Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Triggers all registered listeners.
     */
    public void markUpdated() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

}
