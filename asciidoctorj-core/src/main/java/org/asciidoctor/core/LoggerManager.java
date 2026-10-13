package org.asciidoctor.core;

import java.util.function.Supplier;

/**
 * Manages the logger instance used by the processor.
 *
 * <p>The logger is global, unless a logger is bound to the current scope using {@link #callWithLogger(Logger,
 * Supplier)} (which is what the {@code logger} API option does). A scoped logger takes precedence over the global
 * logger and is not visible from other threads, so concurrent conversions don't share their logger.
 */
public final class LoggerManager {
    private static final ThreadLocal<Logger> SCOPED_LOGGER = new ThreadLocal<>();
    private static Supplier<Logger> loggerFactory = Logger::new;
    private static Logger logger;

    private LoggerManager() {
    }

    /**
     * @return the logger bound to the current scope, if any, otherwise the global logger (creating it on first access)
     */
    public static Logger getLogger() {
        Logger scopedLogger = SCOPED_LOGGER.get();
        return scopedLogger != null ? scopedLogger : getGlobalLogger();
    }

    private static synchronized Logger getGlobalLogger() {
        if (logger == null) {
            logger = loggerFactory.get();
        }
        return logger;
    }

    /**
     * Sets the global logger used by the processor. If the specified logger is null, a new default logger is created.
     *
     * <p>This method does not change the logger bound to the current scope, if any.
     */
    public static synchronized void setLogger(Logger newLogger) {
        logger = newLogger == null ? loggerFactory.get() : newLogger;
    }

    public static synchronized Supplier<Logger> getLoggerFactory() {
        return loggerFactory;
    }

    public static synchronized void setLoggerFactory(Supplier<Logger> factory) {
        loggerFactory = factory;
    }

    /**
     * Calls the specified action with the specified logger bound to the current scope.
     *
     * @return the result of the action
     */
    public static <T> T callWithLogger(Logger scopedLogger, Supplier<T> action) {
        Logger previousLogger = SCOPED_LOGGER.get();
        if (previousLogger == scopedLogger) {
            return action.get();
        }
        SCOPED_LOGGER.set(scopedLogger);
        try {
            return action.get();
        } finally {
            if (previousLogger == null) {
                SCOPED_LOGGER.remove();
            } else {
                SCOPED_LOGGER.set(previousLogger);
            }
        }
    }

    /**
     * Runs the specified action with the specified logger bound to the current scope.
     */
    public static void runWithLogger(Logger scopedLogger, Runnable action) {
        callWithLogger(scopedLogger, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Resolves the value of the {@code logger} API option: a falsy value (null or false) disables logging.
     */
    static Logger resolveLoggerOption(Object value) {
        return value instanceof Logger optionLogger ? optionLogger : new NullLogger();
    }
}
