package org.asciidoctor.asciidoctorj.log.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.log.LogHandler;

import java.util.ServiceLoader;

public class LogHandlerRegistryExecutor {
    private static ServiceLoader<LogHandler> logHandlerServiceLoader = ServiceLoader
        .load(LogHandler.class);

    private Asciidoctor asciidoctor;

    public LogHandlerRegistryExecutor(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllLogHandlers() {
        for (LogHandler logHandler: logHandlerServiceLoader) {
            asciidoctor.registerLogHandler(logHandler);
        }
    }

}