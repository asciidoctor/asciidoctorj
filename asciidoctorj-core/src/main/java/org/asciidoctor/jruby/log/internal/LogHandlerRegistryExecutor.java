package org.asciidoctor.jruby.log.internal;

import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.log.LogHandler;

import java.util.ServiceLoader;

public class LogHandlerRegistryExecutor {
    private static ServiceLoader<LogHandler> logHandlerServiceLoader = ServiceLoader
        .load(LogHandler.class);

    private AsciidoctorJRuby asciidoctor;

    public LogHandlerRegistryExecutor(AsciidoctorJRuby asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllLogHandlers() {
        for (LogHandler logHandler: logHandlerServiceLoader) {
            asciidoctor.registerLogHandler(logHandler);
        }
    }

}