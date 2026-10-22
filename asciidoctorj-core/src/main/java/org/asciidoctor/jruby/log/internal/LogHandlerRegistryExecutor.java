package org.asciidoctor.jruby.log.internal;

import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.log.LogHandler;

import java.util.ServiceLoader;

public class LogHandlerRegistryExecutor {

    private AsciidoctorJRuby asciidoctor;

    public LogHandlerRegistryExecutor(AsciidoctorJRuby asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllLogHandlers() {
        ServiceLoader<LogHandler> logHandlerServiceLoader = ServiceLoader
                .load(LogHandler.class);

        for (LogHandler logHandler: logHandlerServiceLoader) {
            asciidoctor.registerLogHandler(logHandler);
        }
    }

}