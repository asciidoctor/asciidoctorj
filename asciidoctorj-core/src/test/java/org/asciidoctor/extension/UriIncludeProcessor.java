package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.util.TestHttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Map;

public class UriIncludeProcessor extends IncludeProcessor {

    public UriIncludeProcessor() {
    }

    public UriIncludeProcessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public boolean handles(String target) {
        return target.startsWith("http://") || target.startsWith("https://");
    }

    @Override
    public void process(Document document, PreprocessorReader reader, String target,
                        Map<String, Object> attributes) {

        StringBuilder content = readContent(target);
        reader.push_include(content.toString(), target, target, 1, attributes);
    }

    private StringBuilder readContent(String target) {
        StringBuilder content = new StringBuilder();

        try {

            URL url = new URL(target);
            URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", TestHttpServer.getInstance().getLocalPort())));
            InputStream openStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(openStream));

            String line;
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
