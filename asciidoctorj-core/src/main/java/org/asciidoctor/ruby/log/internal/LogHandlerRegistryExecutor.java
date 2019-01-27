package org.asciidoctor.ruby.log.internal;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.ruby.AsciidoctorJ;

import java.util.ServiceLoader;

public class LogHandlerRegistryExecutor {
    private static ServiceLoader<LogHandler> logHandlerServiceLoader = ServiceLoader
        .load(LogHandler.class);

    private AsciidoctorJ asciidoctor;

    public LogHandlerRegistryExecutor(AsciidoctorJ asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllLogHandlers() {
        for (LogHandler logHandler: logHandlerServiceLoader) {
            asciidoctor.registerLogHandler(logHandler);
        }
    }

}