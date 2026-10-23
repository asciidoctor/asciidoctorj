package org.asciidoctor.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;

/**
 * A HTTP server (used as a proxy) that serves the specified resources by URL.
 *
 * <p>NOTE implemented with the HTTP server of the JDK (instead of Netty in AsciidoctorJ).
 */
public class TestHttpServer {

    private static TestHttpServer instance;

    private final HttpServer server;

    private final Map<String, File> resources;

    private TestHttpServer(Map<String, File> resources) {
        this.resources = resources;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.createContext("/", exchange -> {
            // NOTE the request URI is the absolute URL since the server is used as a proxy
            File resourceFile = this.resources.get(exchange.getRequestURI().toString());
            byte[] content = resourceFile == null
                    ? "<html><head/><body><h1>Sorry, no content found</h1></body>".getBytes()
                    : Files.readAllBytes(resourceFile.toPath());
            exchange.sendResponseHeaders(resourceFile == null ? 404 : 200, content.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(content);
            }
        });
        server.start();
    }

    public static void start(Map<String, File> resources) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = new TestHttpServer(resources);
    }

    public static TestHttpServer getInstance() {
        return instance;
    }

    public int getLocalPort() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
        instance = null;
    }
}
