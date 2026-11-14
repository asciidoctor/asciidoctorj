package org.asciidoctor.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resets the JUL loggers used by the tests without a read of a configuration file.
 * A native image has no java.home, so LogManager.readConfiguration() fails there.
 */
public final class TestLogManager {

    private TestLogManager() {
    }

    public static void reset() {
        resetLogger(Logger.getLogger(""), Level.INFO);
        resetLogger(Logger.getLogger("asciidoctor"), null);
    }

    private static void resetLogger(Logger logger, Level level) {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.setLevel(level);
    }
}
