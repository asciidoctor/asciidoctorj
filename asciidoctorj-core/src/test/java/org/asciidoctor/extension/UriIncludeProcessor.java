package org.asciidoctor.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.util.TestHttpServer;

public class UriIncludeProcessor extends IncludeProcessor {

    public UriIncludeProcessor() {}

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
