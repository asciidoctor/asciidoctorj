package org.asciidoctor.nativeimage;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds the entries of {@link RegisterClass}, {@link RegisterDirectory} and {@link RegisterFile} to a reachability
 * metadata file.
 * <p>
 * The file is set with the option {@code -AnativeMetadata=<path>}. The processor only adds entries: it does not
 * remove or change the entries that are already in the file.
 */
public class NativeMetadataProcessor extends AbstractProcessor {

    static final String OPTION = "nativeMetadata";

    // the type, and the reflection flags of the type
    private final Map<String, Map<String, Object>> reflectionTypes = new LinkedHashMap<>();
    private final Set<String> resourceGlobs = new LinkedHashSet<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(RegisterClass.class.getName(), RegisterClasses.class.getName(), RegisterDirectory.class.getName(),
                RegisterFile.class.getName());
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(OPTION);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
                    collect(mirror);
                }
            }
        }
        if (roundEnv.processingOver()) {
            write();
        }
        return false;
    }

    private void collect(AnnotationMirror mirror) {
        final String annotationName = ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString();
        if (annotationName.equals(RegisterClasses.class.getName())) {
            for (AnnotationValue repeated : values(mirror).get("value")) {
                collect((AnnotationMirror) repeated.getValue());
            }
        } else if (annotationName.equals(RegisterClass.class.getName())) {
            collectClasses(mirror);
        } else if (annotationName.equals(RegisterDirectory.class.getName())) {
            for (AnnotationValue item : listOf(values(mirror).get("value"))) {
                final String path = (String) item.getValue();
                final String name = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                resourceGlobs.add(name);
                resourceGlobs.add(name + "/**");
            }
        } else if (annotationName.equals(RegisterFile.class.getName())) {
            for (AnnotationValue item : listOf(values(mirror).get("value"))) {
                resourceGlobs.add((String) item.getValue());
            }
        }
    }

    private void collectClasses(AnnotationMirror mirror) {
        final Map<String, List<AnnotationValue>> values = values(mirror);
        final Map<String, Object> flags = new LinkedHashMap<>();
        if (flag(values, "constructors")) {
            flags.put("allDeclaredConstructors", true);
            flags.put("allPublicConstructors", true);
        }
        if (flag(values, "methods")) {
            flags.put("allPublicMethods", true);
        }
        if (flag(values, "fields")) {
            flags.put("allDeclaredFields", true);
        }
        for (AnnotationValue item : listOf(values.get("value"))) {
            reflectionTypes.computeIfAbsent(binaryName((TypeMirror) item.getValue()), type -> new LinkedHashMap<>())
                    .putAll(flags);
        }
    }

    // the values of the annotation (with the default values), by name. A single value is a list with one item.
    private Map<String, List<AnnotationValue>> values(AnnotationMirror mirror) {
        final Map<String, List<AnnotationValue>> values = new LinkedHashMap<>();
        processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).forEach((method, value) ->
                values.put(method.getSimpleName().toString(), listOf(value)));
        return values;
    }

    @SuppressWarnings("unchecked")
    private static List<AnnotationValue> listOf(Object value) {
        if (value instanceof List<?> list) {
            return (List<AnnotationValue>) list;
        }
        final AnnotationValue annotationValue = (AnnotationValue) value;
        return annotationValue.getValue() instanceof List<?> list ? (List<AnnotationValue>) list : List.of(annotationValue);
    }

    private static boolean flag(Map<String, List<AnnotationValue>> values, String name) {
        return (Boolean) values.get(name).get(0).getValue();
    }

    private String binaryName(TypeMirror type) {
        final TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        return processingEnv.getElementUtils().getBinaryName(element).toString();
    }

    private void write() {
        final String path = processingEnv.getOptions().get(OPTION);
        if (path == null || (reflectionTypes.isEmpty() && resourceGlobs.isEmpty())) {
            return;
        }
        try {
            final Path file = Path.of(path);
            final Map<String, Object> metadata = readExisting(file);
            reflectionTypes.forEach((type, flags) -> {
                final Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type", type);
                entry.putAll(flags);
                addEntry(metadata, "reflection", "type", entry);
            });
            for (String glob : resourceGlobs) {
                final Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("glob", glob);
                addEntry(metadata, "resources", "glob", entry);
            }
            final String content = Json.write(metadata);
            if (!Files.exists(file) || !content.equals(Files.readString(file, StandardCharsets.UTF_8))) {
                if (file.getParent() != null) {
                    Files.createDirectories(file.getParent());
                }
                Files.writeString(file, content, StandardCharsets.UTF_8);
            }
        } catch (IOException | RuntimeException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot update the native metadata file " + path + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readExisting(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new LinkedHashMap<>();
        }
        return (Map<String, Object>) Json.parse(Files.readString(file, StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private void addEntry(Map<String, Object> metadata, String section, String key, Map<String, Object> entry) {
        final List<Object> entries = (List<Object>) metadata.computeIfAbsent(section, name -> new ArrayList<>());
        for (Object existing : entries) {
            final Map<String, Object> existingEntry = (Map<String, Object>) existing;
            if (entry.get(key).equals(existingEntry.get(key))) {
                entry.forEach(existingEntry::putIfAbsent);
                return;
            }
        }
        entries.add(entry);
    }
}
