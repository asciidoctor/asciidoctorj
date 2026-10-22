package org.asciidoctor.it.springboot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kotlin.collections.ArrayDeque;
import okhttp3.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonMap;
import static org.asciidoctor.it.springboot.ProcessHelper.run;
import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootTest {

    final List<Process> testProcesses = new ArrayDeque<>();


    @Test
    public void should_start_SpringBoot_service_with_requiresUnpack() throws IOException, InterruptedException {

        int serverPort = 8000 + ThreadLocalRandom.current().nextInt(1000);
        startSpringBootApp(serverPort);

        String asciidocDocument = "= Title\n" +
                "\n" +
                "== First chapter\n" +
                "first chapter\n" +
                "\n" +
                "== Second chapter\n" +
                "second chapter";
        Map<String, String> jsonResponse = convertDocument(asciidocDocument, serverPort);

        assertThat(jsonResponse.get("contentType"))
                .isEqualTo("text/html");
        assertThat(base64Decode(jsonResponse.get("content")))
                .contains("<h1>Title</h1>")
                .contains("<p>first chapter</p>")
                .contains("<h2 id=\"_second_chapter\">Second chapter</h2>");
    }

    @AfterEach
    public void cleanup() {
        testProcesses.forEach(Process::destroy);
    }

    private Map<String, String> convertDocument(String asciidocDocument, int serverPort) throws IOException {
        String body = "{\"data\":\"" + base64Encode(asciidocDocument) + "\"}";

        Request request = new Request.Builder()
                .url("http://localhost:" + serverPort + "/asciidoc")
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();
        Response response = new OkHttpClient()
                .newCall(request)
                .execute();

        return new Gson()
                .fromJson(response.body().string(), new TypeToken<Map<String, String>>() {}.getType());
    }

    private String base64Encode(String asciidocDocument) {
        return new String(Base64.getEncoder().encode(asciidocDocument.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Decode(String value) {
        return new String(Base64.getDecoder().decode(value));
    }

    private void startSpringBootApp(int serverPort) throws IOException, InterruptedException {

        ProcessHelper.ProcessOutput run = run(singletonMap("SERVER_PORT", String.valueOf(serverPort)),
                "java", "-jar", "build/libs/springboot-app-0.0.1-SNAPSHOT.jar");

        testProcesses.add(run.process);

        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> {
                    Request request = new Request.Builder()
                            .url("http://localhost:" + serverPort + "/actuator/health/readiness")
                            .build();
                    Response response = new OkHttpClient()
                            .newCall(request)
                            .execute();
                    return response.isSuccessful() && response.body().string().contains("UP");
                });
    }
}
