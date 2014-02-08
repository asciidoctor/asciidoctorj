package org.asciidoctor.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.asciidoctor.ast.DocumentRuby;

public class UriIncludeProcessor extends IncludeProcessor {

    public UriIncludeProcessor(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    @Override
    public boolean handles(String target) {
        return target.startsWith("http://") || target.startsWith("https://");
    }

    @Override
    public void process(PreprocessorReader reader, String target,
            Map<String, Object> attributes) {

        StringBuilder content = readContent(target);
        reader.push_include(content.toString(), target, target, 1, attributes);

    }

    private StringBuilder readContent(String target) {
        StringBuilder content = new StringBuilder();

        try {

            URL url = new URL(target);
            InputStream openStream = url.openStream();

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(openStream));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            bufferedReader.close();

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return content;
    }

}
