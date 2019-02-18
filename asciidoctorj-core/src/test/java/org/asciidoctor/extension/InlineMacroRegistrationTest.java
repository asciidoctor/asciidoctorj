package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class InlineMacroRegistrationTest {

    @ArquillianResource
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
        public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
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
        public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
            String transformed = target.chars()
                .mapToObj(c -> Character.isUpperCase(c) ? " " + (char) c : Character.toString((char) c))
                .collect(joining())
                .toUpperCase().trim();
            return createPhraseNode(parent, "quoted", transformed);
        }
    }

    @Name(AnnotatedTestProcessor.NAME)
    public static class AnnotatedTestProcessor extends AbstractTestProcessor {
        public static final String NAME = "annotated";
    }

    @Test
    public void testRegisterNamedClassAsClass() {
        asciidoctor.javaExtensionRegistry().inlineMacro(AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "World"), OptionsBuilder.options());
        check("Hello WORLD", result);
    }

    @Test
    public void testRegisterNamedClassAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.javaExtensionRegistry().inlineMacro(explicitblockname, AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Explicit"), OptionsBuilder.options());
        check("Hello EXPLICIT", result);
    }

    @Test
    public void testRegisterNamedClassAsInstance() {
        asciidoctor.javaExtensionRegistry().inlineMacro(new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "AnotherTest"), OptionsBuilder.options());
        check("Hello ANOTHER TEST", result);
    }

    @Test
    public void testRegisterNamedClassAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.javaExtensionRegistry().inlineMacro(blockName, new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(blockName, "YetAnotherTest"), OptionsBuilder.options());
        check("Hello YET ANOTHER TEST", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterClassWithoutExplicitName() {
        asciidoctor.javaExtensionRegistry().inlineMacro(AbstractTestProcessor.class);
    }


    @Test
    public void testRegisterClassAsClassWithExplicitName() {
        final String explicitblockname = "anotherexplicitname";
        asciidoctor.javaExtensionRegistry().inlineMacro(explicitblockname, AbstractTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "ExplicitClass"), OptionsBuilder.options());
        check("Hello EXPLICIT CLASS", result);
    }

    @Test(expected = AsciidoctorCoreException.class)
    public void testRegisterClassAsInstance() {
        asciidoctor.javaExtensionRegistry().inlineMacro(new AbstractTestProcessor());
        asciidoctor.convert(document("foo", "HelloExplicitInstance"), OptionsBuilder.options());
    }

    @Test
    public void testRegisterClassAsInstanceWithExplicitName() {
        final String explicitblockname = "someexplicitname";
        asciidoctor.javaExtensionRegistry().inlineMacro(explicitblockname, new AbstractTestProcessor());
        final String result = asciidoctor.convert(document(explicitblockname, "ExplicitInstance"), OptionsBuilder.options());
        check("Hello EXPLICIT INSTANCE", result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRegisterClassWithNameAsClass() {
        asciidoctor.javaExtensionRegistry().inlineMacro(TestProcessorWithName.class);
    }

    @Test
    public void testRegisterClassWithNameAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.javaExtensionRegistry().inlineMacro(explicitblockname, TestProcessorWithName.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Explicit"), OptionsBuilder.options());
        check("Hello EXPLICIT", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstance() {
        asciidoctor.javaExtensionRegistry().inlineMacro(new TestProcessorWithName());
        final String result = asciidoctor.convert(document(TestProcessorWithName.NAME, "AnotherTest"), OptionsBuilder.options());
        check("Hello ANOTHER TEST", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.javaExtensionRegistry().inlineMacro(blockName, new TestProcessorWithName());
        final String result = asciidoctor.convert(document(blockName, "YetAnotherTest"), OptionsBuilder.options());
        check("Hello YET ANOTHER TEST", result);
    }

    private void check(String expected, String html) {
        assertEquals(expected, Jsoup.parse(html).select("div.paragraph > p").first().text());
    }
}
