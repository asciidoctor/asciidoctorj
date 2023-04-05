package org.asciidoctor;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@WebServlet("/asciidoctor")
public class AsciidoctorServlet extends HttpServlet {

    private Asciidoctor asciidoctor;

    @Override
    public void init() {
        asciidoctor = Asciidoctor.Factory.create();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(200);
        try (InputStreamReader reader = new InputStreamReader(req.getInputStream());
             OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream())) {
            asciidoctor.convert(reader, writer, OptionsBuilder.options().get());
        }
    }
}
