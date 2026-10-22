// Usage: java -cp asciidoctorj-native/build/classes/java/main:asciidoctorj-native/build/resources/main:asciidoctorj-api/build/classes/java/main tools/compare/ConvertJava.java <list-file> <root> <out-dir>
import org.asciidoctor.core.*;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ConvertJava {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[1]), out = Path.of(args[2]);
        for (String rel : Files.readAllLines(Path.of(args[0]))) {
            if (rel.isEmpty()) continue;
            Path dst = out.resolve(rel);
            Files.createDirectories(dst.getParent());
            MemoryLogger logger = new MemoryLogger();
            String html;
            try {
                Map<String, Object> attrs = new LinkedHashMap<>();
                attrs.put("reproducible", "");
                attrs.put("linkcss", "");
                attrs.put("docdate", "2020-01-01");
                attrs.put("doctime", "00:00:00 UTC");
                attrs.put("localdate", "2020-01-01");
                attrs.put("localtime", "00:00:00 UTC");
                attrs.put("docyear", "2020");
                attrs.put("localyear", "2020");
                // extra attributes, e.g. ATTRS='toc sectnums icons=font'
                for (String a : System.getenv().getOrDefault("ATTRS", "").split("\\s+")) {
                    if (a.isEmpty()) continue;
                    String[] kv = a.split("=", 2);
                    attrs.put(kv[0], kv.length > 1 ? kv[1] : "");
                }
                Map<String, Object> opts = new LinkedHashMap<>();
                opts.put("safe", "safe");
                opts.put("standalone", true);
                opts.put("to_file", false);
                opts.put("attributes", attrs);
                opts.put("logger", logger);
                html = String.valueOf(Asciidoctor.convertFile(root.resolve(rel).toString(), opts));
            } catch (Throwable e) {
                html = "EXCEPTION: " + e.getClass().getName() + ": " + e.getMessage() + "\n";
            }
            Files.writeString(Path.of(dst + ".html"), html);
            Files.writeString(Path.of(dst + ".log"), logger.getMessages().stream().map(m -> {
                String loc = m.message() instanceof LogMessage lm && lm.sourceLocation() != null
                        ? "line " + lm.sourceLocation().getLineNumber() + ": " : "";
                return m.severity() + ": " + loc + m.text();
            }).collect(Collectors.joining("\n")));
        }
    }
}
