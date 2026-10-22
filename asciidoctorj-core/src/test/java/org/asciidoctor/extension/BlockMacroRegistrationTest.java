package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class BlockMacroRegistrationTest {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    private String document(String blockName, String text) {
        return "= Test\n" +
                "\n" +
                "== A section\n" +
                "\n" +
                blockName + "::" + text + "[]";
    }

    public static class AbstractTestProcessor extends BlockMacroProcessor {
        @Override
        public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
            return createBlock(parent, "paragraph", target.toUpperCase());
        }
    }

    public static class TestProcessorWithName extends BlockMacroProcessor {

        public static final String NAME = "somename";

        public TestProcessorWithName() {
            super(NAME);
        }

        @Override
        public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
            return createBlock(parent, "paragraph", target.toUpperCase());
        }
    }

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Name(MetaNameAnnotation.NAME)
    public @interface MetaNameAnnotation {
        String NAME = "annotated";
    }

    @Name(AnnotatedTestProcessor.NAME)
    public static class AnnotatedTestProcessor extends AbstractTestProcessor {
        public static final String NAME = "annotated";
    }

    @MetaNameAnnotation
    public static class AnnotatedTestProcessorWithMetaNameAnnotation extends AbstractTestProcessor {
    }

    @MetaNameAnnotation
    @Name(AnnotatedTestProcessorWithMultipleNameAnnotations.NAME)
    public static class AnnotatedTestProcessorWithMultipleNameAnnotations extends AbstractTestProcessor {
        public static final String NAME = "anotherName";
    }

    @Test
    public void testRegisterNamedClassAsClass() {
        asciidoctor.javaExtensionRegistry().blockMacro(AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "Hello World"), emptyOptions());
        check("HELLO WORLD", result);
    }

    @Test
    public void testRegisterClassWithMetaNameAnnotation() {
        asciidoctor.javaExtensionRegistry().blockMacro(AnnotatedTestProcessorWithMetaNameAnnotation.class);
        final String result = asciidoctor.convert(document(MetaNameAnnotation.NAME, "Hello World"), emptyOptions());
        check("HELLO WORLD", result);
    }

    @Test
    public void testRegisterClassWithMultipleNameAnnotations() {
        asciidoctor.javaExtensionRegistry().blockMacro(AnnotatedTestProcessorWithMultipleNameAnnotations.class);
        String result = asciidoctor.convert(document(MetaNameAnnotation.NAME, "Hello World"), emptyOptions());
        check("HELLO WORLD", result);
        result = asciidoctor.convert(document(AnnotatedTestProcessorWithMultipleNameAnnotations.NAME, "Hello World"), emptyOptions());
        check("HELLO WORLD", result);
    }

    @Test
    public void testRegisterNamedClassAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.javaExtensionRegistry().blockMacro(explicitblockname, AnnotatedTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit"), emptyOptions());
        check("HELLO EXPLICIT", result);
    }

    @Test
    public void testRegisterNamedClassAsInstance() {
        asciidoctor.javaExtensionRegistry().blockMacro(new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(AnnotatedTestProcessor.NAME, "Another Test"), emptyOptions());
        check("ANOTHER TEST", result);
    }

    @Test
    public void testRegisterNamedClassAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.javaExtensionRegistry().blockMacro(blockName, new AnnotatedTestProcessor());
        final String result = asciidoctor.convert(document(blockName, "Yet Another Test"), emptyOptions());
        check("YET ANOTHER TEST", result);
    }

    @Test
    public void testRegisterClassWithoutExplicitName() {
        assertThrows(IllegalArgumentException.class, () -> asciidoctor.javaExtensionRegistry().blockMacro(AbstractTestProcessor.class));
    }

    @Test
    public void testRegisterClassAsClassWithExplicitName() {
        final String explicitblockname = "anotherexplicitname";
        asciidoctor.javaExtensionRegistry().blockMacro(explicitblockname, AbstractTestProcessor.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit Class"), emptyOptions());
        check("HELLO EXPLICIT CLASS", result);
    }

    @Test
    public void testRegisterClassAsInstance() {
        assertThrows(AsciidoctorCoreException.class, () -> {
            asciidoctor.javaExtensionRegistry().blockMacro(new AbstractTestProcessor());
            asciidoctor.convert(document("foo", "Hello Explicit Instance"), emptyOptions());
        });
    }

    @Test
    public void testRegisterClassAsInstanceWithExplicitName() {
        final String explicitblockname = "aname";
        asciidoctor.javaExtensionRegistry().blockMacro(explicitblockname, new AbstractTestProcessor());
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit Instance"), emptyOptions());
        check("HELLO EXPLICIT INSTANCE", result);
    }


    @Test
    public void testRegisterClassWithNameAsClass() {
        assertThrows(IllegalArgumentException.class, () -> asciidoctor.javaExtensionRegistry().blockMacro(TestProcessorWithName.class));
    }

    @Test
    public void testRegisterClassWithNameAsClassWithExplicitName() {
        final String explicitblockname = "explicitblockname";
        asciidoctor.javaExtensionRegistry().blockMacro(explicitblockname, TestProcessorWithName.class);
        final String result = asciidoctor.convert(document(explicitblockname, "Hello Explicit"), emptyOptions());
        check("HELLO EXPLICIT", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstance() {
        asciidoctor.javaExtensionRegistry().blockMacro(new TestProcessorWithName());
        final String result = asciidoctor.convert(document(TestProcessorWithName.NAME, "Another Test"), emptyOptions());
        check("ANOTHER TEST", result);
    }

    @Test
    public void testRegisterClassWithNameAsInstanceWithExplicitName() {
        final String blockName = "somename";
        asciidoctor.javaExtensionRegistry().blockMacro(blockName, new TestProcessorWithName());
        final String result = asciidoctor.convert(document(blockName, "Yet Another Test"), emptyOptions());
        check("YET ANOTHER TEST", result);
    }

    private void check(String expected, String html) {
        assertEquals(expected, Jsoup.parse(html).select("div.paragraph > p").first().text());
    }
}
