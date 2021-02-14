package org.asciidoctor.it.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringbootAppApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_convert_document() {

        final SourceContent request = new SourceContent();
        String asciidocDocument = "= Title\n" +
                "\n" +
                "== First chapter\n" +
                "first chapter\n" +
                "\n" +
                "== Second chapter\n" +
                "second chapter";
        request.setData(base64Encode(asciidocDocument));

        ResponseEntity<ConvertedResource> responseEntity = restTemplate.postForEntity("/asciidoc", request, ConvertedResource.class);

        assertThat(responseEntity.getStatusCodeValue())
                .isEqualTo(HttpStatus.OK.value());
        final ConvertedResource response = responseEntity.getBody();
        assertThat(response.getContentType())
                .isEqualTo(MediaType.TEXT_HTML_VALUE);

        assertThat(base64Decode(response))
                .contains("<h1>Title</h1>")
                .contains("<p>first chapter</p>")
                .contains("<h2 id=\"_second_chapter\">Second chapter</h2>");
    }

    private String base64Encode(String asciidocDocument) {
        return new String(Base64.getEncoder().encode(asciidocDocument.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Decode(ConvertedResource response) {
        return new String(Base64.getDecoder().decode(response.getContent()));
    }
}
