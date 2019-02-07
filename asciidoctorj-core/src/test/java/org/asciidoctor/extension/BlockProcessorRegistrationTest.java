package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class BlockProcessorRegistrationTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    private String document(String blockName, String text) {
        return "= Test\n" +
            "\n" +
            "== A section\n" +
            "\n" +
            "[" + blockName + "]\n" +
            text + "\n";
    }

    public static class AbstractTestProcessor extends BlockProcessor {
        @Override
        public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
            List<String> s = reader.readLines().stream()
                .map(line -> 
                    line.chars()
                        .mapToObj(c -> Character.toString((char) c))
                        .collect(joining(" ")))
                .collect(toList());
            return createBlock(parent, "paragraph", s);
        }
    }

    public static class TestProcessorWithName extends BlockProcessor {

        public static final String NAME = "somename";

        public TestProcessorWithName() {
            super(NAME);
        }

        @Override
        public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
            List<String> s = reader.readLines().stream()
                .map(line ->
                    line.chars()
                        .mapToObj(c -> Character.toString((char) c))
                        .collect(joining(" ")))
                .collect(toList());
            return createBlock(parent, "paragraph", s);
        }
    }

    @Name(AnnotatedTestProcessor.NAME)
    public static class AnnotatedTestProcessor extends AbstractTestProcessor {
        public static final String NAME = "annotated";
    }

    @Test
    public void testRegisterNamedClassAsClass() {
        asciidoctor.javaExtensionRegistry().block(AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "Hello World"), OptionsBuilder.options());
        check("H e l l o W o r l d", result);
    }

    @Test
    public void testRegisterNamedClassAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.javaExtensionRegistry().block(explicitblockname, AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit"), OptionsBuilder.options());
        check("H e l l o E x p l i c i t", result);
    }

    @Test
    public void testRegisterNamedClassAsInstance() {
        asciidoctor.javaExtensionRegistry().block(new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "Another Test"), OptionsBuilder.options());
        check("A n o t h e r T e s t", result);
    }

    @Test
    public void testRegisterNamedClassAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.javaExtensionRegistry().block(blockName, new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(blockName, "Yet Another Test"), OptionsBuilder.options());
        check("Y e t A n o t h e r T e s t", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterClassWithoutExplicitName() {
        asciidoctor.javaExtensionRegistry().block(AbstractTestProcessor.class);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRegisterClassAsClass() {
        asciidoctor.javaExtensionRegistry().block(AbstractTestProcessor.class);
    }

    @Test
    public void testRegisterClassAsClassWithExplicitName() {
        final String explicitblockname = "anotherexplicitname";
        asciidoctor.javaExtensionRegistry().block(explicitblockname, AbstractTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit Class"), OptionsBuilder.options());
        check("H e l l o E x p l i c i t C l a s s", result);
    }

    @Test(expected = AsciidoctorCoreException.class)
    public void testRegisterClassAsInstance() {
        asciidoctor.javaExtensionRegistry().block(new AbstractTestProcessor());
        asciidoctor.convert(document("foo", "Hello Explicit Instance"), OptionsBuilder.options());
    }

    @Test
    public void testRegisterClassAsInstanceWithExplicitName() {
        final String explicitblockname = "aname";
        asciidoctor.javaExtensionRegistry().block(explicitblockname, new AbstractTestProcessor());
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit Instance"), OptionsBuilder.options());
        check("H e l l o E x p l i c i t I n s t a n c e", result);
    }



    @Test(expected = IllegalArgumentException.class)
    public void testRegisterClassWithNameAsClass() {
        asciidoctor.javaExtensionRegistry().block(TestProcessorWithName.class);
    }

    @Test
    public void testRegisterClassWithNameAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.javaExtensionRegistry().block(explicitblockname, TestProcessorWithName.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit"), OptionsBuilder.options());
        check("H e l l o E x p l i c i t", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstance() {
        asciidoctor.javaExtensionRegistry().block(new TestProcessorWithName());
        final String result = asciidoctor.convert(document(TestProcessorWithName.NAME, "Another Test"), OptionsBuilder.options());
        check("A n o t h e r T e s t", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.javaExtensionRegistry().block(blockName, new TestProcessorWithName());
        final String result = asciidoctor.convert(document(blockName, "Yet Another Test"), OptionsBuilder.options());
        check("Y e t A n o t h e r T e s t", result);
    }


    private void check(String expected, String html) {
        assertEquals(expected, Jsoup.parse(html).select("div.paragraph > p").first().text());
    }
}
