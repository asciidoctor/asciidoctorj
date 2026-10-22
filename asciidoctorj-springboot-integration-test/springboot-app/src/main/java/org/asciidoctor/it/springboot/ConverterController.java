package org.asciidoctor.it.springboot;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * Simple Spring MVC Rest Controller to convert AsciiDoc sources to HTML.
 */
@RestController
public class ConverterController {

    private final AsciidoctorService asciidoctorService;

    public ConverterController(AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }


    @PostMapping("/asciidoc")
    public ConvertedResource convert(@RequestBody SourceContent content) {
        byte[] decodedContent = Base64.getDecoder().decode(content.getData());
        String converted = asciidoctorService.convert(new String(decodedContent));

        String encodedConvertedContent = Base64.getEncoder().encodeToString(converted.getBytes(StandardCharsets.UTF_8));
        return new ConvertedResource(encodedConvertedContent, MediaType.TEXT_HTML_VALUE);
    }
}
