package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class InlineMacroRegistrationTest {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    private String document(String blockName, String text) {
        return "= Test\n" +
                "\n" +
                "== A section\n" +
                "\n" +
                "\nHello " + blockName + ":" + text + "[]";
    }

    public static class AbstractTestProcessor extends InlineMacroProcessor {
        @Override
        public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
            String transformed = target.chars()
                    .mapToObj(c -> Character.isUpperCase(c) ? " " + (char) c : Character.toString((char) c))
                    .collect(joining())
                    .toUpperCase().trim();
            return createPhraseNode(parent, "quoted", transformed);
        }
    }

    public static class TestProcessorWithName extends InlineMacroProcessor {

        public static final String NAME = "somename";

        public TestProcessorWithName() {
            super(NAME);
        }

        @Override
        public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
            String transformed = target.chars()
                    .mapToObj(c -> Character.isUpperCase(c) ? " " + (char) c : Character.toString((char) c))
                    .collect(joining())
                    .toUpperCase().trim();
            return createPhraseNode(parent, "quoted", transformed);
        }
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    @Name("meta-inline")
    public @interface MetaInlineName { }

    @MetaInlineName
    public static class MetaNamedInlineProcessor extends AbstractTestProcessor { }

    @Name(AnnotatedTestProcessor.NAME)
    public static class AnnotatedTestProcessor extends AbstractTestProcessor {
        public static final String NAME = "annotated";
    }

    @Test
    public void testExtensionGroupRegistersMetaNamedInlineProcessor() {
        asciidoctor.createGroup().inlineMacro(MetaNamedInlineProcessor.class).register();
        check("Hello WORLD", asciidoctor.convert(document("meta-inline", "World"), emptyOptions()));
    }

    @Test
    public void testRegisterNamedClassAsClass() {
        asciidoctor.getExtensionRegistry().inlineMacro(AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "World"), emptyOptions());
        check("Hello WORLD", result);
    }

    @Test
    public void testRegisterNamedClassAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.getExtensionRegistry().inlineMacro(explicitblockname, AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Explicit"), emptyOptions());
        check("Hello EXPLICIT", result);
    }

    @Test
    public void testRegisterNamedClassAsInstance() {
        asciidoctor.getExtensionRegistry().inlineMacro(new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "AnotherTest"), emptyOptions());
        check("Hello ANOTHER TEST", result);
    }

    @Test
    public void testRegisterNamedClassAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.getExtensionRegistry().inlineMacro(blockName, new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(blockName, "YetAnotherTest"), emptyOptions());
        check("Hello YET ANOTHER TEST", result);
    }

    @Test
    public void testRegisterClassWithoutExplicitName() {
        assertThrows(IllegalArgumentException.class, () -> asciidoctor.getExtensionRegistry().inlineMacro(AbstractTestProcessor.class));
    }


    @Test
    public void testRegisterClassAsClassWithExplicitName() {
        final String explicitblockname = "anotherexplicitname";
        asciidoctor.getExtensionRegistry().inlineMacro(explicitblockname, AbstractTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "ExplicitClass"), emptyOptions());
        check("Hello EXPLICIT CLASS", result);
    }

    @Test
    public void testRegisterClassAsInstance() {
        assertThrows(AsciidoctorCoreException.class, () -> {
            asciidoctor.getExtensionRegistry().inlineMacro(new AbstractTestProcessor());
            asciidoctor.convert(document("foo", "HelloExplicitInstance"), emptyOptions());
        });
    }

    @Test
    public void testRegisterClassAsInstanceWithExplicitName() {
        final String explicitblockname = "someexplicitname";
        asciidoctor.getExtensionRegistry().inlineMacro(explicitblockname, new AbstractTestProcessor());
        final String result = asciidoctor.convert(document(explicitblockname, "ExplicitInstance"), emptyOptions());
        check("Hello EXPLICIT INSTANCE", result);
    }


    @Test
    public void testRegisterClassWithNameAsClass() {
        assertThrows(IllegalArgumentException.class, () -> asciidoctor.getExtensionRegistry().inlineMacro(TestProcessorWithName.class));
    }

    @Test
    public void testRegisterClassWithNameAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.getExtensionRegistry().inlineMacro(explicitblockname, TestProcessorWithName.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Explicit"), emptyOptions());
        check("Hello EXPLICIT", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstance() {
        asciidoctor.getExtensionRegistry().inlineMacro(new TestProcessorWithName());
        final String result = asciidoctor.convert(document(TestProcessorWithName.NAME, "AnotherTest"), emptyOptions());
        check("Hello ANOTHER TEST", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.getExtensionRegistry().inlineMacro(blockName, new TestProcessorWithName());
        final String result = asciidoctor.convert(document(blockName, "YetAnotherTest"), emptyOptions());
        check("Hello YET ANOTHER TEST", result);
    }

    private void check(String expected, String html) {
        assertThat(Jsoup.parse(html).select("div.paragraph > p").first().text()).isEqualTo(expected);
    }
}
