package org.broken.arrow.library.logging;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Logging {

    private final Logger log;

    public Logging(@Nonnull final Class<?> clazz) {
        log = Logger.getLogger(clazz.getName());
    }

    /**
     * will as default only send the message under info tag.
     *
     * @param msg the message builder.
     */
    public void log(final Supplier<String> msg) {
        this.log(Level.INFO, null, msg);
    }

    public void log(final Exception exception, final Supplier<String> msg) {
        this.log(Level.WARNING, exception, msg);
    }

    public void log(final Level level, final Supplier<String> msg) {
        this.log(level, null, msg);
    }

    public void log(final Level level, final Throwable exception, final Supplier<String> msg) {
        String message = msg.get();
        final Level logLevel = level == null ? Level.INFO : level;


        if (exception != null) log.log(logLevel, message, exception);
        else log.log(logLevel, message);
    }
    public void logError(@Nonnull final Throwable exception, @Nonnull final Supplier<String> msg) {
        this.log(Level.WARNING, exception, msg);
    }

    public void warn(final Supplier<String> msg) {
        this.log(Level.WARNING, null, msg);
    }
    public void warn(final Consumer<MessageWrapper> wrapper) {
        this.log(Level.WARNING, null,  wrapper);
    }
    public void logError(final Exception exception, final Consumer<MessageWrapper> wrapper) {
        this.log(Level.WARNING, exception,  wrapper);
    }
    public void log(final Level level, final Exception exception, final Consumer<MessageWrapper> wrapper) {
        final MessageWrapper messageWrapper = new MessageWrapper();
        wrapper.accept(messageWrapper);

        final String message = messageWrapper.getMessage();
        final Level logLevel = level == null ? Level.INFO : level;

        if (exception != null) log.log(logLevel, message, exception);
        else log.log(logLevel, message);

    }

    public static final class MessageWrapper {
        private String message;
        private String msgCopy;
        private final Map<String, String> placeholders = new HashMap<>();

        private MessageWrapper() {
        }

        public MessageWrapper putPlaceholder(final String key, final String value) {
            placeholders.put(key, value);
            return this;
        }

        public MessageWrapper putPlaceholders(final MapPair... mapPairs) {
            if(mapPairs == null)
                return this;

            for (MapPair pair : mapPairs) {
                if (pair == null || pair.getKey() == null) continue;

                String value = pair.getValue() != null ? pair.getValue().toString() : "";
                placeholders.put(pair.getKey(), value);
            }
            return this;
        }

        public String getMessage() {
            return setPlaceholders();
        }

        public MessageWrapper setMessage(final String msg) {
            this.message = msg;
            return this;
        }

        private String setPlaceholders() {
            if (placeholders.isEmpty()) {
                return message;
            }
            placeholders.forEach((placeholderKey, value) ->
                    msgCopy = message.replace(placeholderKey, value != null ? value : ""));
            return msgCopy;
        }
    }
}
