package org.asciidoctor.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * A minimal web server that serves the files in the test directory (mirrors using_test_webserver in the Ruby test
 * helper).
 */
public final class TestWebServer implements AutoCloseable {
    private final HttpServer server;
    private final List<String> requests = Collections.synchronizedList(new ArrayList<>());
    private final String baseUrl;

    TestWebServer(String host, int port, String baseDir) {
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.createContext("/", exchange -> handle(exchange, baseDir));
        server.start();
        baseUrl = "http://" + host + ":" + port;
    }

    private void handle(HttpExchange exchange, String baseDir) throws IOException {
        String method = exchange.getRequestMethod();
        String resource = exchange.getRequestURI().getRawPath();
        requests.add(method + " " + exchange.getRequestURI() + " " + exchange.getProtocol() + "\r\n");
        byte[] body;
        int status;
        String contentType;
        if (!method.equals("GET")) {
            status = 405;
            contentType = "text/plain";
            body = "405 - Method not allowed\n".getBytes(StandardCharsets.UTF_8);
        } else if (resource.equals("/name/asciidoctor")) {
            status = 200;
            contentType = "application/json";
            body = "{\"name\": \"asciidoctor\"}\n".getBytes(StandardCharsets.UTF_8);
        } else {
            Path file = Path.of(baseDir, resource.isEmpty() ? "." : resource);
            if (Files.isRegularFile(file)) {
                status = 200;
                String ext = Helpers.extname(file.toString());
                contentType = ext.isEmpty() || ext.equals(".adoc") ? "text/plain" : "image/" + ext.substring(1);
                body = Files.readAllBytes(file);
            } else {
                status = 404;
                contentType = "text/plain";
                body = "404 - Resource not found.\n".getBytes(StandardCharsets.UTF_8);
            }
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @return the request lines received by this server
     */
    public List<String> getRequests() {
        return requests;
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
