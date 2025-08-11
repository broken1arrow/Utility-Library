package org.broken.arrow.library.logging;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging messages with flexible levels and
 * support for lazy message construction using {@link Supplier} or
 * customizable message building via {@link Consumer} of {@link MessageWrapper}.
 * <p>
 * Allows logging exceptions with messages, and supports placeholders replacement
 * within messages.
 */
public final class Logging {

    private final Logger log;

    /**
     * Constructs a Logging instance for the given class.
     *
     * @param clazz the class whose name will be used as the logger name
     */
    public Logging(@Nonnull final Class<?> clazz) {
        log = Logger.getLogger(clazz.getName());
    }

    /**
     * Logs a message at the INFO level.
     * The message is lazily constructed via the supplied {@link Supplier}.
     *
     * @param msg the supplier that provides the log message
     */
    public void log(final Supplier<String> msg) {
        this.log(Level.INFO, null, msg);
    }

    /**
     * Logs an exception and a message at the WARNING level.
     *
     * @param exception the exception to log
     * @param msg the supplier that provides the log message
     */
    public void log(final Exception exception, final Supplier<String> msg) {
        this.log(Level.WARNING, exception, msg);
    }

    /**
     * Logs a message at the specified level.
     *
     * @param level the logging level (if null, defaults to INFO)
     * @param msg the supplier that provides the log message
     */
    public void log(final Level level, final Supplier<String> msg) {
        this.log(level, null, msg);
    }

    /**
     * Logs a message and optional throwable at the specified level.
     *
     * @param level the logging level (if null, defaults to INFO)
     * @param exception the throwable to log, may be null
     * @param msg the supplier that provides the log message
     */
    public void log(final Level level, final Throwable exception, final Supplier<String> msg) {
        String message = msg.get();
        final Level logLevel = level == null ? Level.INFO : level;


        if (exception != null) log.log(logLevel, message, exception);
        else log.log(logLevel, message);
    }

    /**
     * Logs an error-level message with a throwable.
     *
     * @param exception the throwable to log
     * @param msg the supplier that provides the log message
     */
    public void logError(@Nonnull final Throwable exception, @Nonnull final Supplier<String> msg) {
        this.log(Level.WARNING, exception, msg);
    }

    /**
     * Logs a warning message.
     *
     * @param msg the supplier that provides the log message
     */
    public void warn(final Supplier<String> msg) {
        this.log(Level.WARNING, null, msg);
    }

    /**
     * Logs a warning message using a {@link Consumer} of {@link MessageWrapper}
     * for building the message with placeholders.
     *
     * @param wrapper the consumer to build the message
     */
    public void warn(final Consumer<MessageWrapper> wrapper) {
        this.log(Level.WARNING, null,  wrapper);
    }

    /**
     * Logs an error message with exception using a {@link Consumer} of {@link MessageWrapper}
     * for building the message with placeholders.
     *
     * @param exception the exception to log
     * @param wrapper the consumer to build the message
     */
    public void logError(final Exception exception, final Consumer<MessageWrapper> wrapper) {
        this.log(Level.WARNING, exception,  wrapper);
    }

    /**
     * Logs a message with a given level and exception,
     * allowing message construction via {@link Consumer} of {@link MessageWrapper}.
     *
     * @param level the logging level (defaults to INFO if null)
     * @param exception the exception to log, may be null
     * @param wrapper the consumer to build the message
     */
    public void log(final Level level, final Exception exception, final Consumer<MessageWrapper> wrapper) {
        final MessageWrapper messageWrapper = new MessageWrapper();
        wrapper.accept(messageWrapper);

        final String message = messageWrapper.getMessage();
        final Level logLevel = level == null ? Level.INFO : level;

        if (exception != null) log.log(logLevel, message, exception);
        else log.log(logLevel, message);

    }

    /**
     * Inner helper class for building log messages with support for
     * placeholders replacement.
     */
    public static final class MessageWrapper {
        private String message;
        private String msgCopy;
        private final Map<String, String> placeholders = new HashMap<>();

        private MessageWrapper() {
        }

        /**
         * Adds a placeholder key-value pair to replace in the message.
         *
         * @param key the placeholder key (e.g. "{username}")
         * @param value the value to replace the placeholder with
         * @return this MessageWrapper instance for chaining
         */
        public MessageWrapper putPlaceholder(final String key, final String value) {
            placeholders.put(key, value);
            return this;
        }

        /**
         * Adds multiple placeholder pairs to the message.
         *
         * @param mapPairs array of {@link MapPair} key-value pairs
         * @return this MessageWrapper instance for chaining
         */
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

        /**
         * Returns the constructed message with placeholders replaced.
         *
         * @return the message with placeholders replaced
         */
        public String getMessage() {
            return setPlaceholders();
        }

        /**
         * Sets the base message before placeholder replacement.
         *
         * @param msg the base message string
         * @return this MessageWrapper instance for chaining
         */
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
