package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.asciidoctor.core.extension.TreeProcessor;
import org.asciidoctor.core.extension.Registry;
import org.asciidoctor.core.extension.ProcessorExtension;
import org.asciidoctor.core.extension.Preprocessor;
import org.asciidoctor.core.extension.Postprocessor;
import org.asciidoctor.core.extension.InlineMacroProcessor;
import org.asciidoctor.core.extension.IncludeProcessor;
import org.asciidoctor.core.extension.Group;
import org.asciidoctor.core.extension.DocinfoProcessor;
import org.asciidoctor.core.extension.BlockProcessor;
import org.asciidoctor.core.extension.BlockMacroProcessor;

import org.asciidoctor.core.extension.Extensions;

import org.junit.jupiter.api.*;

@DisplayName("Extensions")
public class ExtensionsTest extends TestHelper {
    // FIXME constant: SampleTreeProcessor = SampleTreeprocessor
    static final Object SampleTreeProcessor = SampleTreeprocessor.class;
    Object contentCache;

    @Nested
    @DisplayName("Register")
    class Register {

        @Test
        @DisplayName("should not activate registry if no extension groups are registered")
        void shouldNotActivateRegistryIfNoExtensionGroupsAreRegistered() throws Exception {
            var doc = emptyDocument();
            assertFalsy(doc.hasExtensions(), "Extensions should not be enabled if not groups are registered");
        }

        @Test
        @DisplayName("should register extension group class")
        void shouldRegisterExtensionGroupClass() throws Exception {
            try {
                Extensions.register("sample", SampleExtensionGroup.class);
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                assertEquals(SampleExtensionGroup.class, get(Extensions.getGroups(), "sample"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should self register extension group class")
        void shouldSelfRegisterExtensionGroupClass() throws Exception {
            try {
                Extensions.register("sample", SampleExtensionGroup.class);
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                assertEquals(SampleExtensionGroup.class, get(Extensions.getGroups(), "sample"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should register extension group from class name")
        void shouldRegisterExtensionGroupFromClassName() throws Exception {
            try {
                Extensions.register("sample", "org.asciidoctor.core.ExtensionsTest$SampleExtensionGroup");
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                assertEquals(SampleExtensionGroup.class, get(Extensions.getGroups(), "sample"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should register extension group from instance")
        void shouldRegisterExtensionGroupFromInstance() throws Exception {
            try {
                Extensions.register("sample", new SampleExtensionGroup());
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                assertInstanceOf(SampleExtensionGroup.class, get(Extensions.getGroups(), "sample"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should register extension block")
        void shouldRegisterExtensionBlock() throws Exception {
            try {
                Extensions.register("sample", (Group) registry -> {

                });
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                assertInstanceOf(Group.class, get(Extensions.getGroups(), "sample"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should coerce group name to symbol when registering")
        void shouldCoerceGroupNameToSymbolWhenRegistering() throws Exception {
            try {
                Extensions.register("sample", SampleExtensionGroup.class);
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                assertEquals(SampleExtensionGroup.class, get(Extensions.getGroups(), "sample"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should unregister extension group by symbol name")
        void shouldUnregisterExtensionGroupBySymbolName() throws Exception {
            try {
                Extensions.register("sample", SampleExtensionGroup.class);
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                Extensions.unregister("sample");
                assertEquals(0, size(Extensions.getGroups()));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should unregister extension group by string name")
        void shouldUnregisterExtensionGroupByStringName() throws Exception {
            try {
                Extensions.register("sample", SampleExtensionGroup.class);
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                Extensions.unregister("sample");
                assertEquals(0, size(Extensions.getGroups()));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should unregister multiple extension groups by name")
        void shouldUnregisterMultipleExtensionGroupsByName() throws Exception {
            try {
                Extensions.register("sample1", SampleExtensionGroup.class);
                Extensions.register("sample2", SampleExtensionGroup.class);
                assertNotNull(Extensions.getGroups());
                assertEquals(2, size(Extensions.getGroups()));
                Extensions.unregister("sample1", "sample2");
                assertEquals(0, size(Extensions.getGroups()));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not fail to unregister extension group if not registered")
        void shouldNotFailToUnregisterExtensionGroupIfNotRegistered() throws Exception {
            assertNotNull(Extensions.getGroups());
            assertEquals(0, size(Extensions.getGroups()));
            Extensions.unregister("sample");
            assertEquals(0, size(Extensions.getGroups()));
        }

        @Test
        @DisplayName("should not fail to unregister extension group if extension groups are not initialized")
        void shouldNotFailToUnregisterExtensionGroupIfExtensionGroupsAreNotInitialized() throws Exception {
            Extensions.unregisterAll();
            Extensions.unregister("sample");
            assertEquals(0, size(Extensions.getGroups()));
        }

        @Test
        @DisplayName("should raise NameError if extension class cannot be resolved from string")
        void shouldRaiseNameErrorIfExtensionClassCannotBeResolvedFromString() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block("foobar");
                });
                var ex = assertThrows(IllegalArgumentException.class, () -> emptyDocument());
                assertMatch(rx("^Could not resolve class for name: foobar$"), ex.getMessage());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should allow standalone registry to be created but not registered")
        void shouldAllowStandaloneRegistryToBeCreatedButNotRegistered() throws Exception {
            var registry = Extensions.create("sample", (Group) reg -> {
                reg.block(processor -> {
                    processor.named("whisper");
                    processor.onContext("paragraph");
                    processor.parseContentAs(ContentModel.SIMPLE);
                    processor.process((parent, reader, attributes) -> processor.createParagraph(parent, map(lines(reader), (String it) -> it.toLowerCase()), attributes));
                });
            });
            assertInstanceOf(Registry.class, registry);
            assertNotNull(registry.getGroups());
            assertEquals(1, size(registry.getGroups()));
            assertEquals("sample", first(keys(registry.getGroups())));
            assertEquals(0, size(Extensions.getGroups()));
        }
    }

    @Nested
    @DisplayName("Activate")
    class Activate {

        @Test
        @DisplayName("should call activate on extension group class")
        void shouldCallActivateOnExtensionGroupClass() throws Exception {
            try {
                var doc = new Document();
                Extensions.register("sample", SampleExtensionGroup.class);
                var registry = new Registry();
                registry.activate(doc);
                assertTruthy(doc.hasAttribute("activate-method-called"));
                assertTruthy(registry.hasPreprocessors());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should reset registry if activate is called again")
        void shouldResetRegistryIfActivateIsCalledAgain() throws Exception {
            try {
                Extensions.register("sample", SampleExtensionGroup.class);
                var doc = new Document();
                var registry = new Registry();
                registry.activate(doc);
                assertTruthy(doc.hasAttribute("activate-method-called"));
                assertTruthy(registry.hasPreprocessors());
                assertEquals(1, size(registry.getPreprocessors()));
                assertSame(doc, registry.getDocument());
                doc = new Document();
                registry.activate(doc);
                assertTruthy(doc.hasAttribute("activate-method-called"));
                assertTruthy(registry.hasPreprocessors());
                assertEquals(1, size(registry.getPreprocessors()));
                assertSame(doc, registry.getDocument());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke extension block")
        void shouldInvokeExtensionBlock() throws Exception {
            try {
                var doc = new Document();
                Extensions.register((Group) registry -> {
                    registry.getDocument().getAttributes().put("block-called", "");
                    registry.preprocessor(SamplePreprocessor.class);
                });
                var registry = new Registry();
                registry.activate(doc);
                assertTruthy(doc.hasAttribute("block-called"));
                assertTruthy(registry.hasPreprocessors());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should create registry in Document if extensions are loaded")
        void shouldCreateRegistryInDocumentIfExtensionsAreLoaded() throws Exception {
            try {
                Extensions.register(SampleExtensionGroup.class);
                var doc = new Document();
                assertTruthy(doc.hasExtensions());
                assertInstanceOf(Registry.class, doc.getExtensions());
            } finally {
                Extensions.unregisterAll();
            }
        }
    }

    @Nested
    @DisplayName("Instantiate")
    class Instantiate {

        @Test
        @DisplayName("should instantiate preprocessors")
        void shouldInstantiatePreprocessors() throws Exception {
            var registry = new Registry();
            registry.preprocessor(SamplePreprocessor.class);
            registry.activate(new Document());
            assertTruthy(registry.hasPreprocessors());
            var extensions = registry.getPreprocessors();
            assertEquals(1, size(extensions));
            assertInstanceOf(ProcessorExtension.class, first(extensions));
            assertInstanceOf(SamplePreprocessor.class, first(extensions).getInstance());
        }

        @Test
        @DisplayName("should instantiate include processors")
        void shouldInstantiateIncludeProcessors() throws Exception {
            var registry = new Registry();
            registry.includeProcessor(SampleIncludeProcessor.class);
            registry.activate(new Document());
            assertTruthy(registry.hasIncludeProcessors());
            var extensions = registry.getIncludeProcessors();
            assertEquals(1, size(extensions));
            assertInstanceOf(ProcessorExtension.class, first(extensions));
            assertInstanceOf(SampleIncludeProcessor.class, first(extensions).getInstance());
            assertTrue(first(extensions).getInstance().handles(null, "include.adoc"));
        }

        @Test
        @DisplayName("should instantiate docinfo processors")
        void shouldInstantiateDocinfoProcessors() throws Exception {
            var registry = new Registry();
            registry.docinfoProcessor(SampleDocinfoProcessor.class);
            registry.activate(new Document());
            assertTruthy(registry.hasDocinfoProcessors());
            assertTruthy(registry.hasDocinfoProcessors("head"));
            var extensions = registry.getDocinfoProcessors();
            assertEquals(1, size(extensions));
            assertInstanceOf(ProcessorExtension.class, first(extensions));
            assertInstanceOf(SampleDocinfoProcessor.class, first(extensions).getInstance());
        }

        @Test
        @DisplayName("should instantiate tree processors")
        void shouldInstantiateTreeProcessors() throws Exception {
            var registry = new Registry();
            registry.treeProcessor(SampleTreeprocessor.class);
            registry.activate(new Document());
            assertTruthy(registry.hasTreeProcessors());
            var extensions = registry.getTreeProcessors();
            assertEquals(1, size(extensions));
            assertInstanceOf(ProcessorExtension.class, first(extensions));
            assertInstanceOf(SampleTreeprocessor.class, first(extensions).getInstance());
        }

        @Test
        @DisplayName("should instantiate postprocessors")
        void shouldInstantiatePostprocessors() throws Exception {
            var registry = new Registry();
            registry.postprocessor(SamplePostprocessor.class);
            registry.activate(new Document());
            assertTruthy(registry.hasPostprocessors());
            var extensions = registry.getPostprocessors();
            assertEquals(1, size(extensions));
            assertInstanceOf(ProcessorExtension.class, first(extensions));
            assertInstanceOf(SamplePostprocessor.class, first(extensions).getInstance());
        }

        @Test
        @DisplayName("should instantiate block processor")
        void shouldInstantiateBlockProcessor() throws Exception {
            var registry = new Registry();
            registry.block(SampleBlock.class, "sample");
            registry.activate(new Document());
            assertTruthy(registry.hasBlocks());
            assertTruthy(registry.isRegisteredForBlock("sample", "paragraph"));
            var extension = registry.findBlockExtension("sample");
            assertInstanceOf(ProcessorExtension.class, extension);
            assertInstanceOf(SampleBlock.class, extension.getInstance());
        }

        @Test
        @DisplayName("should not match block processor for unsupported context")
        void shouldNotMatchBlockProcessorForUnsupportedContext() throws Exception {
            var registry = new Registry();
            registry.block(SampleBlock.class, "sample");
            registry.activate(new Document());
            assertFalsy(registry.isRegisteredForBlock("sample", "sidebar"));
        }

        @Test
        @DisplayName("should instantiate block macro processor")
        void shouldInstantiateBlockMacroProcessor() throws Exception {
            var registry = new Registry();
            registry.blockMacro(SampleBlockMacro.class, "sample");
            registry.activate(new Document());
            assertTruthy(registry.hasBlockMacros());
            assertTruthy(registry.isRegisteredForBlockMacro("sample"));
            var extension = registry.findBlockMacroExtension("sample");
            assertInstanceOf(ProcessorExtension.class, extension);
            assertInstanceOf(SampleBlockMacro.class, extension.getInstance());
        }

        @Test
        @DisplayName("should instantiate inline macro processor")
        void shouldInstantiateInlineMacroProcessor() throws Exception {
            var registry = new Registry();
            registry.inlineMacro(SampleInlineMacro.class, "sample");
            registry.activate(new Document());
            assertTruthy(registry.hasInlineMacros());
            assertTruthy(registry.isRegisteredForInlineMacro("sample"));
            var extension = registry.findInlineMacroExtension("sample");
            assertInstanceOf(ProcessorExtension.class, extension);
            assertInstanceOf(SampleInlineMacro.class, extension.getInstance());
        }

        @Test
        @DisplayName("should allow processors to be registered by a string name")
        void shouldAllowProcessorsToBeRegisteredByAStringName() throws Exception {
            var registry = new Registry();
            registry.preprocessor("org.asciidoctor.core.ExtensionsTest$SamplePreprocessor");
            registry.activate(new Document());
            assertTruthy(registry.hasPreprocessors());
            var extensions = registry.getPreprocessors();
            assertEquals(1, size(extensions));
            assertInstanceOf(ProcessorExtension.class, first(extensions));
        }
    }

    @Nested
    @DisplayName("Integration")
    class Integration {

        @Test
        @DisplayName("does not crash when querying for extensions if none are registered")
        void doesNotCrashWhenQueryingForExtensionsIfNoneAreRegistered() throws Exception {
            var registry = Extensions.create();
            var doc = documentFromString("= Document Title\n\ncontent", Options.of("extension_registry", registry));
            assertNotNull(doc.getExtensions());
            assertFalsy(doc.getExtensions().isRegisteredForBlock("unknown", "paragraph"));
            assertNull(doc.getExtensions().findBlockExtension("unknown"));
            assertFalsy(doc.getExtensions().isRegisteredForBlockMacro("unknown"));
            assertNull(doc.getExtensions().findBlockMacroExtension("unknown"));
            assertFalsy(doc.getExtensions().isRegisteredForInlineMacro("unknown"));
            assertNull(doc.getExtensions().findInlineMacroExtension("unknown"));
            assertEmpty(doc.getExtensions().getInlineMacros());
        }

        @Test
        @DisplayName("can provide extension registry as an option")
        void canProvideExtensionRegistryAsAnOption() throws Exception {
            var registry = Extensions.create((Group) reg -> {
                reg.treeProcessor(SampleTreeProcessor);
            });
            var doc = documentFromString("= Document Title\n\ncontent", Options.of("extension_registry", registry));
            assertNotNull(doc.getExtensions());
            assertEquals(1, size(doc.getExtensions().getGroups()));
            assertTruthy(doc.getExtensions().hasTreeProcessors());
            assertEquals(1, size(doc.getExtensions().getTreeProcessors()));
            assertEquals(0, size(Extensions.getGroups()));
        }

        @Test
        @DisplayName("can provide extension registry created without any groups as option")
        void canProvideExtensionRegistryCreatedWithoutAnyGroupsAsOption() throws Exception {
            var registry = Extensions.create();
            registry.treeProcessor(SampleTreeProcessor);
            var doc = documentFromString("= Document Title\n\ncontent", Options.of("extension_registry", registry));
            assertNotNull(doc.getExtensions());
            assertEquals(0, size(doc.getExtensions().getGroups()));
            assertTruthy(doc.getExtensions().hasTreeProcessors());
            assertEquals(1, size(doc.getExtensions().getTreeProcessors()));
            assertEquals(0, size(Extensions.getGroups()));
        }

        @Test
        @DisplayName("can provide extensions proc as option")
        void canProvideExtensionsProcAsOption() throws Exception {
            var doc = documentFromString("= Document Title\n\ncontent", Options.of("extensions", (Group) registry -> registry.treeProcessor(SampleTreeprocessor.class)));
            assertNotNull(doc.getExtensions());
            assertEquals(1, size(doc.getExtensions().getGroups()));
            assertTruthy(doc.getExtensions().hasTreeProcessors());
            assertEquals(1, size(doc.getExtensions().getTreeProcessors()));
            assertEquals(0, size(Extensions.getGroups()));
        }

        @Test
        @DisplayName("should not activate global registry if :extensions option is false")
        void shouldNotActivateGlobalRegistryIfExtensionsOptionIsFalse() throws Exception {
            try {
                Extensions.register("sample", (Group) registry -> {

                });
                assertNotNull(Extensions.getGroups());
                assertEquals(1, size(Extensions.getGroups()));
                var doc = emptyDocument(Options.of("extensions", false));
                assertFalsy(doc.hasExtensions(), "Extensions should not be enabled if :extensions option is false");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke preprocessors before parsing document")
        void shouldInvokePreprocessorsBeforeParsingDocument() throws Exception {
            var input = """
                    junk line

                    = Document Title

                    sample content
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.preprocessor(ScrubHeaderPreprocessor.class);
                });
                var doc = documentFromString(input);
                assertTruthy(doc.hasAttribute("skipped"));
                assertEquals("junk line", Strings.strip(str(doc.getAttribute("skipped"))));
                assertTruthy(doc.hasHeader());
                assertEquals("Document Title", doc.getDoctitle());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke include processor to process include directive")
        void shouldInvokeIncludeProcessorToProcessIncludeDirective() throws Exception {
            var input = """
                    before

                    include::lorem-ipsum.txt[]

                    after
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.includeProcessor(BoilerplateTextIncludeProcessor.class);
                });
                var result = convertString(input, Options.of("safe", "secure"));
                assertCss(".paragraph > p", result, 3);
                assertIncludes(result, "before");
                assertIncludes(result, "Lorem ipsum");
                assertIncludes(result, "after");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke include processor if it requests to handle include directive")
        void shouldInvokeIncludeProcessorIfItRequestsToHandleIncludeDirective() throws Exception {
            var input = """
                    include::skip-me.adoc[]
                    line after skip

                    include::include-file.adoc[]

                    include::fixtures/grandchild-include.adoc[]

                    last line
                    """;
            var registry = Extensions.create((Group) reg -> {
                reg.includeProcessor(processor -> {
                    processor.handles((doc, target) -> Objects.equals(target, "skip-me.adoc"));
                    processor.process((doc, reader, target, attributes) -> {

                    });
                });
                reg.includeProcessor(processor -> {
                    processor.handles((doc, target) -> Objects.equals(target, "include-file.adoc"));
                    processor.process((doc, reader, target, attributes) -> {
                        var content = list(("found include target '" + str(target) + "' at line " + str(reader.cursorAtPrevLine().getLineNumber()) + "\r\n"), "\r\n", "middle line\r\n");
                        reader.pushInclude(content, target, target, 1, attributes);
                    });
                });
            });
            var document = emptyDocument(Options.of("base_dir", testdir(), "extension_registry", registry, "safe", "safe"));
            var reader = new PreprocessorReader(document, input, null, Options.of("normalize", true));
            var lines = new ArrayList<String>();
            lines.add(reader.readLine());
            assertEquals("line after skip", last(lines));
            lines.add(reader.readLine());
            lines.add(reader.readLine());
            assertEquals("found include target 'include-file.adoc' at line 4", last(lines));
            assertEquals("include-file.adoc: line 2", reader.getLineInfo());
            while (reader.hasMoreLines()) {
                lines.add(reader.readLine());
            }
            var source = join(lines, Asciidoctor.LF);
            assertMatch(rx("^found include target 'include-file.adoc' at line 4$"), source);
            assertMatch(rx("^middle line$"), source);
            assertMatch(rx("^last line of grandchild$"), source);
            assertMatch(rx("^last line$"), source);
        }

        @Test
        @DisplayName("should invoke include processor if it requests to handle include directive using legacy method")
        void shouldInvokeIncludeProcessorIfItRequestsToHandleIncludeDirectiveUsingLegacyMethod() throws Exception {
            var input = "include::include-file.adoc[]\ninclude::include-file.adoc[]";
            var contentCache = new HashMap<String, String>();
            var registry = Extensions.create((Group) reg -> {
                reg.includeProcessor(new IncludeProcessor() {
                    // demonstrate that we can use instance variables
                    private Map<String, String> cache;

                    @Override
                    public boolean handles(String target) {
                        return Objects.equals(target, "include-file.adoc");
                    }

                    @Override
                    public void process(Document doc, PreprocessorReader reader, String target, Map<String, Object> attributes) {
                        if (cache == null) {
                            cache = contentCache;
                        }
                        var content = cache.computeIfAbsent("include-file.adoc", key -> "contents of include-file.adoc");
                        reader.pushInclude(content, target, target, 1, attributes);
                    }
                });
            });
            var document = emptyDocument(Options.of("base_dir", testdir(), "extension_registry", registry, "safe", "safe"));
            var reader = new PreprocessorReader(document, input, null, Options.of("normalize", true));
            var lines = new ArrayList<String>();
            lines.add(reader.readLine());
            lines.add(reader.readLine());
            assertEquals("contents of include-file.adoc", last(lines));
            assertEquals(1, contentCache.size());
            assertEquals(contentCache.get("include-file.adoc"), last(lines));
        }

        @Test
        @DisplayName("should invoke tree processors after parsing document")
        void shouldInvokeTreeProcessorsAfterParsingDocument() throws Exception {
            var input = """
                    = Document Title
                    Doc Writer

                    content
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.treeProcessor(ReplaceAuthorTreeProcessor.class);
                });
                var doc = documentFromString(input);
                assertEquals("Ghost Writer", doc.getAuthor());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should set source_location on document before invoking tree processors")
        void shouldSetSource_locationOnDocumentBeforeInvokingTreeProcessors() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.treeProcessor(processor -> {
                        processor.process(doc -> {
                            var para = processor.createParagraph((AbstractBlock) last(doc.getBlocks()).getParent(), ("file: " + str(doc.getFile()) + ", lineno: " + str(doc.getLineNumber())), Options.of());
                            return doc.append(para);
                        });
                    });
                });
                var sampleDoc = fixturePath("sample.adoc");
                var doc = Asciidoctor.loadFile(sampleDoc, Options.of("sourcemap", true));
                assertIncludes(doc.convert(), "file: sample.adoc, lineno: 1");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should allow tree processor to replace tree")
        void shouldAllowTreeProcessorToReplaceTree() throws Exception {
            var input = """
                    = Original Document
                    Doc Writer

                    content
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.treeProcessor(ReplaceTreeTreeProcessor.class);
                });
                var doc = documentFromString(input);
                assertEquals("Replacement Document", doc.getDoctitle());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should honor block title assigned in tree processor")
        void shouldHonorBlockTitleAssignedInTreeProcessor() throws Exception {
            var input = """
                    = Document Title
                    :!example-caption:

                    .Old block title
                    ====
                    example block content
                    ====
                    """;
            var oldTitle = new Ref<String>(null);
            try {
                Extensions.register((Group) registry -> {
                    registry.treeProcessor(processor -> {
                        processor.process(doc -> {
                            var ex = doc.findBy(AbstractBlock.Selector.any().withContext("example")).get(0);
                            oldTitle.set(ex.getTitle());
                            ex.setTitle("New block title");
                            return null;
                        });
                    });
                });
                var doc = documentFromString(input);
                assertEquals("Old block title", oldTitle.get());
                assertEquals("New block title", doc.findBy(AbstractBlock.Selector.any().withContext("example")).get(0).getTitle());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should be able to register preferred tree processor")
        void shouldBeAbleToRegisterPreferredTreeProcessor() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.treeProcessor(processor -> {
                        processor.process(doc -> {
                            doc.append(processor.createParagraph(doc, "d", Options.of()));
                            return null;
                        });
                    });
                    registry.treeProcessor(processor -> {
                        processor.prefer();
                        processor.process(doc -> {
                            doc.append(processor.createParagraph(doc, "c", Options.of()));
                            return null;
                        });
                    });
                    registry.prefer(registry.treeProcessor(processor -> {
                        processor.process(doc -> {
                            doc.append(processor.createParagraph(doc, "b", Options.of()));
                            return null;
                        });
                    }));
                    registry.prefer(registry.treeProcessor(processor -> {
                        processor.process(doc -> {
                            doc.append(processor.createParagraph(doc, "a", Options.of()));
                            return null;
                        });
                    }));
                    registry.prefer(registry.treeProcessor(SelfSigningTreeProcessor.class));
                });
                var doc = emptyDocument();
                doc.convert();
                assertEquals(list("SelfSigningTreeProcessor", "a", "b", "c", "d"), map(map(doc.getBlocks(), it -> send(it, "lines")), it -> send(it, "first")));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke postprocessors after converting document")
        void shouldInvokePostprocessorsAfterConvertingDocument() throws Exception {
            var input = """
                    * one
                    * two
                    * three
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.postprocessor(StripAttributesPostprocessor.class);
                });
                var output = convertString(input);
                refuteMatch(rx("<div class=\"ulist\">"), output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should yield to document processor block if block has non-zero arity")
        void shouldYieldToDocumentProcessorBlockIfBlockHasNonZeroArity() throws Exception {
            var input = "hi!\n";
            try {
                Extensions.register((Group) registry -> {
                    registry.treeProcessor(processor -> {
                        processor.process(doc -> doc.append(processor.createParagraph(doc, "bye!", Options.of())));
                    });
                });
                var output = convertStringToEmbedded(input);
                assertXpath("//p", output, 2);
                assertXpath("//p[text()=\"hi!\"]", output, 1);
                assertXpath("//p[text()=\"bye!\"]", output, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke processor for custom block")
        void shouldInvokeProcessorForCustomBlock() throws Exception {
            var input = """
                    [yell]
                    Hi there!

                    [yell,chars=aeiou]
                    Hi there!
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.block(UppercaseBlock.class);
                });
                var output = convertStringToEmbedded(input);
                assertXpath("//p", output, 2);
                assertXpath("(//p)[1][text()=\"HI THERE!\"]", output, 1);
                assertXpath("(//p)[2][text()=\"hI thErE!\"]", output, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke processor for custom block in an AsciiDoc table cell")
        void shouldInvokeProcessorForCustomBlockInAnAsciiDocTableCell() throws Exception {
            var input = """
                    |===
                    a|
                    [yell]
                    Hi there!
                    |===
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.block(UppercaseBlock.class);
                });
                var output = convertStringToEmbedded(input);
                assertXpath("/table//p", output, 1);
                assertXpath("/table//p[text()=\"HI THERE!\"]", output, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should yield to syntax processor block if block has non-zero arity")
        void shouldYieldToSyntaxProcessorBlockIfBlockHasNonZeroArity() throws Exception {
            var input = """
                    [eval]
                    ....
                    'yolo' * 5
                    ....
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.block("eval", processor -> {
                        processor.onContext("literal");
                        processor.process((parent, reader, attrs) -> processor.createParagraph(parent, "yolo".repeat(5), Options.of()));
                    });
                });
                var output = convertStringToEmbedded(input);
                assertXpath("//p[text()=\"yoloyoloyoloyoloyolo\"]", output, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should pass cloaked context in attributes passed to process method of custom block")
        void shouldPassCloakedContextInAttributesPassedToProcessMethodOfCustomBlock() throws Exception {
            var input = """
                    [custom]
                    ****
                    sidebar
                    ****
                    """;
            var cloakedContext = new Ref<String>(null);
            try {
                Extensions.register((Group) registry -> {
                    registry.block("custom", processor -> {
                        processor.onContext("sidebar");
                        processor.process((doc, reader, attrs) -> {
                            cloakedContext.set((String) get(attrs, "cloaked-context"));
                            return null;
                        });
                    });
                });
                convertStringToEmbedded(input);
                assertEquals("sidebar", cloakedContext.get());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should allow extension to promote paragraph to compound block")
        void shouldAllowExtensionToPromoteParagraphToCompoundBlock() throws Exception {
            var input = """
                    [ex]
                    example
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.block("ex", processor -> {
                        processor.onContext("paragraph");
                        processor.process((parent, reader, ignored) -> processor.createExampleBlock(parent, reader.readLines(), Options.of(), Options.of("content_model", "compound")));
                    });
                });
                var output = convertStringToEmbedded(input);
                assertCss(".exampleblock .paragraph", output, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke processor for custom block macro")
        void shouldInvokeProcessorForCustomBlockMacro() throws Exception {
            var input = "snippet::12345[mode=edit]";
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro(SnippetMacro.class, "snippet");
                });
                var output = convertStringToEmbedded(input);
                assertIncludes(output, "<script src=\"http://example.com/12345.js?_mode=edit\"></script>");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not parse attributes on custom block macro when resolve attributes is false")
        void shouldNotParseAttributesOnCustomBlockMacroWhenResolveAttributesIsFalse() throws Exception {
            var input = "log::[hello, world!]";
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro("log", processor -> {
                        processor.resolveAttributes(false);
                        processor.process((parent, ignored, attrs) -> {
                            parent.getLogger().info(get(attrs, "text"));
                            return null;
                        });
                    });
                });
                try (var logger = usingMemoryLogger(Logger.Severity.INFO)) {
                    var output = convertStringToEmbedded(input);
                    assertEmpty(output);
                    assertMessage(logger, "INFO", "hello, world!");
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not parse attributes on custom block macro when content model is :text")
        void shouldNotParseAttributesOnCustomBlockMacroWhenContentModelIsText() throws Exception {
            var input = "log::[hello, world!]";
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro("log", processor -> {
                        processor.contentModel(ContentModel.TEXT);
                        processor.process((parent, ignored, attrs) -> {
                            parent.getLogger().info(get(attrs, "text"));
                            return null;
                        });
                    });
                });
                try (var logger = usingMemoryLogger(Logger.Severity.INFO)) {
                    var output = convertStringToEmbedded(input);
                    assertEmpty(output);
                    assertMessage(logger, "INFO", "hello, world!");
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should substitute attributes in target of custom block macro")
        void shouldSubstituteAttributesInTargetOfCustomBlockMacro() throws Exception {
            var input = "snippet::{gist-id}[mode=edit]";
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro(SnippetMacro.class, "snippet");
                });
                var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("gist-id", "12345")));
                assertIncludes(output, "<script src=\"http://example.com/12345.js?_mode=edit\"></script>");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should log debug message if custom block macro is unknown")
        void shouldLogDebugMessageIfCustomBlockMacroIsUnknown() throws Exception {
            var input = "unknown::[]";
            try (var logger = usingMemoryLogger(Logger.Severity.DEBUG)) {
                var result = convertStringToEmbedded(input);
                assertMessage(logger, "DEBUG", "<stdin>: line 1: unknown name for block macro: unknown", LogMessage.class);
                assertXpath(("/*[@class=\"paragraph\"]/p[text()=\"" + str(input) + "\"]"), result, 1);
            }
        }

        @Test
        @DisplayName("should log debug message if custom block macro is unknown when custom block macros are registered")
        void shouldLogDebugMessageIfCustomBlockMacroIsUnknownWhenCustomBlockMacrosAreRegistered() throws Exception {
            try {
                var input = "unknown::[]";
                Extensions.register((Group) registry -> {
                    registry.blockMacro(SampleBlockMacro.class, "sample");
                });
                try (var logger = usingMemoryLogger(Logger.Severity.DEBUG)) {
                    var result = convertStringToEmbedded(input);
                    assertMessage(logger, "DEBUG", "<stdin>: line 1: unknown name for block macro: unknown", LogMessage.class);
                    assertXpath(("/*[@class=\"paragraph\"]/p[text()=\"" + str(input) + "\"]"), result, 1);
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not log debug message if line is not a custom block macro and block macros are registered")
        void shouldNotLogDebugMessageIfLineIsNotACustomBlockMacroAndBlockMacrosAreRegistered() throws Exception {
            try {
                var input = "* xref:component::page.adoc[link text]";
                Extensions.register((Group) registry -> {
                    registry.blockMacro(SampleBlockMacro.class, "sample");
                });
                try (var logger = usingMemoryLogger(Logger.Severity.DEBUG)) {
                    var result = convertStringToEmbedded(input);
                    assertEmpty(logger.getMessages());
                    assertCss("ul li a[href=\"component::page.html\"]", result, 1);
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should drop block macro line if target references missing attribute and attribute-missing is drop-line")
        void shouldDropBlockMacroLineIfTargetReferencesMissingAttributeAndAttributeMissingIsDropLine() throws Exception {
            var input = """
                    [.rolename]
                    snippet::{gist-ns}12345[mode=edit]

                    following paragraph
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro(SnippetMacro.class, "snippet");
                });
                var doc = new Ref<Document>(null);
                var output = new Ref<String>(null);
                try (var logger = usingMemoryLogger()) {
                    doc.set(documentFromString(input, Options.of("attributes", Options.of("attribute-missing", "drop-line"))));
                    assertEquals(1, size(doc.get().getBlocks()));
                    assertEquals("paragraph", doc.get().getBlocks().get(0).getContext());
                    output.set(doc.get().convert());
                    assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: gist-ns");
                }
                assertCss(".paragraph", output.get(), 1);
                assertCss(".rolename", output.get(), 0);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke processor for custom block macro in an AsciiDoc table cell")
        void shouldInvokeProcessorForCustomBlockMacroInAnAsciiDocTableCell() throws Exception {
            var input = """
                    |===
                    a|message::hi[]
                    |===
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro("message", processor -> {
                        processor.process((parent, target, attrs) -> processor.createParagraph(parent, target.toUpperCase(), Options.of()));
                    });
                });
                var output = convertStringToEmbedded(input);
                assertXpath("/table//p[text()=\"HI\"]", output, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should match short form of block macro")
        void shouldMatchShortFormOfBlockMacro() throws Exception {
            var input = "custom-toc::[]";
            var resolvedTarget = new Ref<String>(null);
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro(processor -> {
                        processor.named("custom-toc");
                        processor.process((parent, target, attrs) -> {
                            resolvedTarget.set(target);
                            return processor.createPassBlock(parent, "<!-- custom toc goes here -->", Options.of(), Options.of("content_model", "raw"));
                        });
                    });
                });
                var output = convertStringToEmbedded(input);
                assertEquals("<!-- custom toc goes here -->", output);
                assertEquals("", resolvedTarget.get());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should fail to convert if name of block macro is illegal")
        void shouldFailToConvertIfNameOfBlockMacroIsIllegal() throws Exception {
            var input = "illegal name::target[]";
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro(processor -> {
                        processor.named("illegal name");
                        processor.process((parent, target, attrs) -> null);
                    });
                });
                assertThrows(IllegalArgumentException.class, () -> {
                    convertStringToEmbedded(input);
                });
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should honor legacy :pos_attrs option set via static method")
        void shouldHonorLegacyPos_attrsOptionSetViaStaticMethod() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro(LegacyPosAttrsBlockMacro.class, "diag");
                });
                var result = convertStringToEmbedded("diag::[filename,png]");
                assertCss("img[src=\"filename.png\"]", result, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should honor legacy :pos_attrs option set via DSL")
        void shouldHonorLegacyPos_attrsOptionSetViaDSL() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro("diag", processor -> {
                        processor.option("pos_attrs", list("target", "format"));
                        processor.process((parent, ignored, attrs) -> processor.createImageBlock(parent, Options.of("target", (str(get(attrs, "target")) + "." + str(get(attrs, "format"))))));
                    });
                });
                var result = convertStringToEmbedded("diag::[filename,png]");
                assertCss("img[src=\"filename.png\"]", result, 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should be able to set header attribute in block macro processor")
        void shouldBeAbleToSetHeaderAttributeInBlockMacroProcessor() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro(processor -> {
                        processor.named("attribute");
                        processor.resolveAttributes("1:value");
                        processor.process((parent, target, attrs) -> {
                            parent.getDocument().setAttribute(target, get(attrs, "value"));
                            return null;
                        });
                    });
                    registry.blockMacro(processor -> {
                        processor.named("header_attribute");
                        processor.resolveAttributes("1:value");
                        processor.process((parent, target, attrs) -> {
                            parent.getDocument().setHeaderAttribute(target, get(attrs, "value"));
                            return null;
                        });
                    });
                });
                var input = """
                        attribute::yin[yang]

                        header_attribute::foo[bar]
                        """;
                var doc = documentFromString(input);
                assertNull(doc.getAttribute("yin"));
                assertEquals("bar", doc.getAttribute("foo"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke processor for custom inline macro")
        void shouldInvokeProcessorForCustomInlineMacro() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(TemperatureMacro.class, "deg");
                });
                var output = convertStringToEmbedded("Room temperature is deg:25[C,precision=0].", Options.of("attributes", Options.of("temperature-unit", "F")));
                assertIncludes(output, "Room temperature is 25 &#176;C.");
                output = convertStringToEmbedded("Normal body temperature is deg:37[].", Options.of("attributes", Options.of("temperature-unit", "F")));
                assertIncludes(output, "Normal body temperature is 98.6 &#176;F.");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not parse attributes on custom inline macro when resolve attributes is false")
        void shouldNotParseAttributesOnCustomInlineMacroWhenResolveAttributesIsFalse() throws Exception {
            var input = "Line is del:[good]great.";
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro("del", processor -> {
                        processor.matchFormat("short");
                        processor.resolveAttributes(false);
                        processor.process((parent, ignored, attrs) -> processor.createInline(parent, "quoted", (String) get(attrs, "text"), Options.of("type", "unquoted", "attributes", Options.of("role", "line-through"))));
                    });
                });
                var output = convertStringToEmbedded(input);
                assertIncludes(output, "<span class=\"line-through\">good</span>");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not parse attributes on custom inline macro when content model is text")
        void shouldNotParseAttributesOnCustomInlineMacroWhenContentModelIsText() throws Exception {
            var input = "Line is del:[good]great.";
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro("del", processor -> {
                        processor.matchFormat("short");
                        processor.contentModel(ContentModel.TEXT);
                        processor.process((parent, ignored, attrs) -> processor.createInline(parent, "quoted", (String) get(attrs, "text"), Options.of("type", "unquoted", "attributes", Options.of("role", "line-through"))));
                    });
                });
                var output = convertStringToEmbedded(input);
                assertIncludes(output, "<span class=\"line-through\">good</span>");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should resolve regexp for inline macro lazily")
        void shouldResolveRegexpForInlineMacroLazily() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("label");
                        processor.matchFormat("short");
                        processor.parseContentAs(ContentModel.TEXT);
                        processor.process((parent, ignored, attrs) -> processor.createInlinePass(parent, ("<label>" + str(get(attrs, "text")) + "</label>")));
                    });
                });
                var output = convertStringToEmbedded("label:[Checkbox]");
                assertIncludes(output, "<label>Checkbox</label>");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should map unparsed attrlist to target when format is short")
        void shouldMapUnparsedAttrlistToTargetWhenFormatIsShort() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("label");
                        processor.matchFormat("short");
                        processor.process((parent, target, ignored) -> processor.createInlinePass(parent, ("<label>" + str(target) + "</label>")));
                    });
                });
                var output = convertStringToEmbedded("label:[Checkbox]");
                assertIncludes(output, "<label>Checkbox</label>");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should parse text in square brackets as attrlist by default")
        void shouldParseTextInSquareBracketsAsAttrlistByDefault() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("json");
                        processor.matchFormat("short");
                        processor.process((parent, ignored, attrs) -> processor.createInlinePass(parent, ("{ " + str(join(map(attrs, (k, v) -> ("\"" + str(k) + "\": \"" + str(v) + "\"")), ", ")) + " }")));
                    });
                    registry.inlineMacro(processor -> {
                        processor.named("data");
                        processor.process((parent, target, attrs) -> {
                            if (Objects.equals(target, "json")) {
                                return processor.createInlinePass(parent, ("{ " + str(join(map(attrs, (k, v) -> ("\"" + str(k) + "\": \"" + str(v) + "\"")), ", ")) + " }"));
                            }
                            return null;
                        });
                    });
                });
                var output = convertStringToEmbedded("json:[a=A,b=B,c=C]", Options.of("doctype", "inline"));
                assertEquals("{ \"a\": \"A\", \"b\": \"B\", \"c\": \"C\" }", output);
                output = convertStringToEmbedded("data:json[a=A,b=B,c=C]", Options.of("doctype", "inline"));
                assertEquals("{ \"a\": \"A\", \"b\": \"B\", \"c\": \"C\" }", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should assign captures correctly for inline macros")
        void shouldAssignCapturesCorrectlyForInlineMacros() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("short_attributes");
                        processor.matchFormat("short");
                        processor.resolveAttributes("1:name");
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("target=" + str(Strings.inspect(target)) + ", attributes=" + inspectAttributes(attrs))));
                    });
                    registry.inlineMacro(processor -> {
                        processor.named("short_text");
                        processor.matchFormat("short");
                        processor.resolveAttributes(false);
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("target=" + str(Strings.inspect(target)) + ", attributes=" + inspectAttributes(attrs))));
                    });
                    registry.inlineMacro(processor -> {
                        processor.named("full-attributes");
                        processor.resolveAttributes(Options.of("1:name", null));
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("target=" + str(Strings.inspect(target)) + ", attributes=" + inspectAttributes(attrs))));
                    });
                    registry.inlineMacro(processor -> {
                        processor.named("full-text");
                        processor.resolveAttributes(false);
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("target=" + str(Strings.inspect(target)) + ", attributes=" + inspectAttributes(attrs))));
                    });
                    registry.inlineMacro(processor -> {
                        processor.named("@short_match");
                        processor.match(rx("@(\\w+)"));
                        processor.resolveAttributes(false);
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("target=" + str(Strings.inspect(target)) + ", attributes=" + inspectAttributes(attrs))));
                    });
                });
                var input = """
                        [subs=normal]
                        ++++
                        short_attributes:[]
                        short_attributes:[value,key=val]
                        short_text:[]
                        short_text:[[text\\]]
                        full-attributes:target[]
                        full-attributes:target[value,key=val]
                        full-text:target[]
                        full-text:target[[text\\]]
                        @target
                        ++++
                        """;
                var expected = Strings.chop("""
                        target="",attributes={}
                        target="value,key=val",attributes={1=>"value","key"=>"val","name"=>"value"}
                        target="",attributes={"text"=>""}
                        target="[text]",attributes={"text"=>"[text]"}
                        target="target",attributes={}
                        target="target",attributes={1=>"value","key"=>"val","name"=>"value"}
                        target="target",attributes={"text"=>""}
                        target="target",attributes={"text"=>"[text]"}
                        target="target",attributes={}
                        """);
                var output = gsub(gsub(convertStringToEmbedded(input), " => ", "=>"), ", ", ",");
                assertEquals(expected, output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should invoke convert on return value if value is an inline node")
        void shouldInvokeConvertOnReturnValueIfValueIsAnInlineNode() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("mention");
                        processor.resolveAttributes(false);
                        processor.process((parent, target, attrs) -> {
                            var text = (String) get(attrs, "text");
                            if (isEmpty(text)) {
                                text = ("@" + str(target));
                            }
                            return processor.createAnchor(parent, text, Options.of("type", "link", "target", ("https://github.com/" + str(target))));
                        });
                    });
                });
                var output = convertStringToEmbedded("mention:mojavelinux[Dan]");
                assertIncludes(output, "<a href=\"https://github.com/mojavelinux\">Dan</a>");
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should allow return value of inline macro to be nil")
        void shouldAllowReturnValueOfInlineMacroToBeNil() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("skipme");
                        processor.matchFormat("short");
                        processor.process((ignored1, ignored2, ignored3) -> null);
                    });
                });
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded("-skipme:[]-", Options.of("doctype", "inline"));
                    assertEquals("--", output);
                    assertEmpty(logger);
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should warn if return value of inline macro is a string")
        void shouldWarnIfReturnValueOfInlineMacroIsAString() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("say");
                        processor.process((parent, target, attrs) -> target);
                    });
                });
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded("say:yo[]", Options.of("doctype", "inline"));
                    assertEquals("yo", output);
                    assertMessage(logger, "INFO", "expected substitution value for custom inline macro to be of type Inline; got String: say:yo[]");
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not apply subs to inline node returned by process method by default")
        void shouldNotApplySubsToInlineNodeReturnedByProcessMethodByDefault() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("say");
                        processor.process((parent, target, attrs) -> processor.createInline(parent, "quoted", ("*" + str(target) + "*"), Options.of("type", "emphasis")));
                    });
                });
                var output = convertStringToEmbedded("say:yo[]", Options.of("doctype", "inline"));
                assertEquals("<em>*yo*</em>", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should apply subs specified as symbol to inline node returned by process method")
        void shouldApplySubsSpecifiedAsSymbolToInlineNodeReturnedByProcessMethod() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("say");
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("*" + str(target) + "*"), Options.of("attributes", Options.of("subs", "normal"))));
                    });
                });
                var output = convertStringToEmbedded("say:yo[]", Options.of("doctype", "inline"));
                assertEquals("<strong>yo</strong>", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should apply subs specified as array to inline node returned by process method")
        void shouldApplySubsSpecifiedAsArrayToInlineNodeReturnedByProcessMethod() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("say");
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("*" + str(target) + "*"), Options.of("attributes", Options.of("subs", list("specialchars", "quotes")))));
                    });
                });
                var output = convertStringToEmbedded("say:{lt}message{gt}[]", Options.of("doctype", "inline"));
                assertEquals("<strong>&lt;message&gt;</strong>", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should apply subs specified as string to inline node returned by process method")
        void shouldApplySubsSpecifiedAsStringToInlineNodeReturnedByProcessMethod() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro(processor -> {
                        processor.named("say");
                        processor.process((parent, target, attrs) -> processor.createInlinePass(parent, ("*" + str(target) + "*"), Options.of("attributes", Options.of("subs", "specialchars,quotes"))));
                    });
                });
                var output = convertStringToEmbedded("say:{lt}message{gt}[]", Options.of("doctype", "inline"));
                assertEquals("<strong>&lt;message&gt;</strong>", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should prefer attributes parsed from inline macro over default attributes")
        void shouldPreferAttributesParsedFromInlineMacroOverDefaultAttributes() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro("attrs", processor -> {
                        processor.matchFormat("short");
                        processor.defaultAttributes(Options.of("1", "a", "2", "b", "foo", "baz"));
                        processor.positionalAttributes("a", "b");
                        processor.process((parent, ignored, attrs) -> processor.createInlinePass(parent, ("a=" + str(get(attrs, "a")) + ",2=" + str(attrs.get("2")) + ",b=" + str(orElse(get(attrs, "b"), "nil")) + ",foo=" + str(get(attrs, "foo")))));
                    });
                });
                var output = convertStringToEmbedded("attrs:[A,foo=bar]", Options.of("doctype", "inline"));
                assertEquals("a=A,2=b,b=nil,foo=bar", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should coerce names of positional attributes to strings")
        void shouldCoerceNamesOfPositionalAttributesToStrings() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro("attrs", processor -> {
                        processor.matchFormat("short");
                        processor.positionalAttributes("a", "b");
                        processor.process((parent, ignored, attrs) -> processor.createInlinePass(parent, ("a=" + str(get(attrs, "a")) + ",b=" + str(get(attrs, "b")))));
                    });
                });
                var output = convertStringToEmbedded("attrs:[A,B]", Options.of("doctype", "inline"));
                assertEquals("a=A,b=B", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not carry over attributes if block processor returns nil")
        void shouldNotCarryOverAttributesIfBlockProcessorReturnsNil() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("skip-me");
                        processor.onContext("paragraph");
                        processor.parseContentAs(ContentModel.RAW);
                        processor.process((parent, reader, attrs) -> null);
                    });
                });
                var input = """
                        .unused title
                        [skip-me]
                        not shown

                        --
                        shown
                        --
                        """;
                var doc = documentFromString(input);
                assertEquals(1, size(doc.getBlocks()));
                assertNull(get(doc.getBlocks().get(0).getAttributes(), "title"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not invoke process method or carry over attributes if block processor declares skip content model")
        void shouldNotInvokeProcessMethodOrCarryOverAttributesIfBlockProcessorDeclaresSkipContentModel() throws Exception {
            try {
                var processMethodCalled = new Ref<>(false);
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("ignore");
                        processor.onContext("paragraph");
                        processor.parseContentAs(ContentModel.SKIP);
                        processor.process((parent, reader, attrs) -> {
                            processMethodCalled.set(true);
                            return null;
                        });
                    });
                });
                var input = """
                        .unused title
                        [ignore]
                        not shown

                        --
                        shown
                        --
                        """;
                var doc = documentFromString(input);
                assertFalsy(processMethodCalled.get());
                assertEquals(1, size(doc.getBlocks()));
                assertNull(get(doc.getBlocks().get(0).getAttributes(), "title"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should pass attributes by value to block processor")
        void shouldPassAttributesByValueToBlockProcessor() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("foo");
                        processor.onContext("paragraph");
                        processor.parseContentAs(ContentModel.RAW);
                        processor.process((parent, reader, attrs) -> {
                            var originalAttrs = copy(attrs);
                            attrs.remove("title");
                            return processor.createParagraph(parent, reader.readLines(), merge(originalAttrs, Options.of("id", "value")));
                        });
                    });
                });
                var input = """
                        .title
                        [foo]
                        content
                        """;
                var doc = documentFromString(input);
                assertEquals(1, size(doc.getBlocks()));
                assertEquals("title", get(doc.getBlocks().get(0).getAttributes(), "title"));
                assertEquals("value", doc.getBlocks().get(0).getId());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should allow extension to replace custom block with a list")
        void shouldAllowExtensionToReplaceCustomBlockWithAList() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("lst");
                        processor.onContext("paragraph");
                        processor.process((parent, reader, ignored) -> {
                            return eachWithObject(reader.readLines(), processor.createList(parent, "ulist"), (line, list) -> list.append(processor.createListItem(list, line)));
                        });
                    });
                });
                var input = """
                        before

                        [lst]
                        a
                        b
                        c

                        after
                        """;
                var doc = documentFromString(input);
                assertEquals(3, size(doc.getBlocks()));
                var list = doc.getBlocks().get(1);
                assertEquals("ulist", list.getContext());
                assertEquals(3, size(((List) list).getItems()));
                assertEquals("a", ((ListItem) ((List) list).getItems().get(0)).getText());
                assertCss("li", doc.convert(), 3);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should allow extension to replace custom block with a section")
        void shouldAllowExtensionToReplaceCustomBlockWithASection() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("sect");
                        processor.onContext("open");
                        processor.process((parent, ignored, attrs) -> processor.createSection(parent, (String) get(attrs, "title"), Options.of()));
                    });
                });
                var input = """
                        .Section Title
                        [sect]
                        --
                        a

                        b
                        --
                        """;
                var doc = documentFromString(input);
                assertEquals(1, size(doc.getBlocks()));
                var sect = doc.getBlocks().get(0);
                assertEquals("section", sect.getContext());
                assertEquals("Section Title", sect.getTitle());
                assertEquals(2, size(sect.getBlocks()));
                assertEquals("paragraph", sect.getBlocks().get(0).getContext());
                assertEquals("paragraph", sect.getBlocks().get(1).getContext());
                assertCss("p", doc.convert(), 2);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("can use parse_content to append blocks to current parent")
        void canUseParse_contentToAppendBlocksToCurrentParent() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("csv");
                        processor.onContext("literal");
                        processor.process((parent, reader, ignored) -> {
                            processor.parseContent(parent, plus(plus(list(",==="), reader.readLines()), list(",===")));
                            return null;
                        });
                    });
                });
                var input = """
                        before

                        [csv]
                        ....
                        a,b,c
                        ....

                        after
                        """;
                var doc = documentFromString(input);
                assertEquals(3, size(doc.getBlocks()));
                var table = doc.getBlocks().get(1);
                assertEquals("table", table.getContext());
                assertCss("td", doc.convert(), 3);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should ignore return value of custom block if value is parent")
        void shouldIgnoreReturnValueOfCustomBlockIfValueIsParent() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("unwrap");
                        processor.onContext("open");
                        processor.process((parent, reader, ignored) -> processor.parseContent(parent, reader.readLines()));
                    });
                });
                var input = """
                        [unwrap]
                        --
                        a

                        b

                        c
                        --
                        """;
                var doc = documentFromString(input);
                assertEquals(3, size(doc.getBlocks()));
                for (var block : doc.getBlocks()) {
                    assertEquals("paragraph", block.getContext());
                }
                assertEquals("a", ((Block) doc.getBlocks().get(0)).getSource());
                assertCss("p", doc.convert(), 3);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should ignore return value of custom block macro if value is parent")
        void shouldIgnoreReturnValueOfCustomBlockMacroIfValueIsParent() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro("para", processor -> {
                        processor.process((parent, target, ignored) -> processor.parseContent(parent, target));
                    });
                });
                var input = "para::text[]\n";
                var doc = documentFromString(input);
                assertEquals(1, size(doc.getBlocks()));
                assertEquals("paragraph", doc.getBlocks().get(0).getContext());
                assertEquals("text", ((Block) doc.getBlocks().get(0)).getSource());
                assertCss("p", doc.convert(), 1);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("parse_content should not share attributes between parsed blocks")
        void parse_contentShouldNotShareAttributesBetweenParsedBlocks() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("wrap");
                        processor.onContext("open");
                        processor.process((parent, reader, attrs) -> {
                            var wrap = new Ref<>(processor.createOpenBlock(parent, null, attrs));
                            return processor.parseContent(wrap.get(), reader.readLines());
                        });
                    });
                });
                var input = """
                        [wrap]
                        --
                        [foo=bar]
                        ====
                        content
                        ====

                        [baz=qux]
                        ====
                        content
                        ====
                        --
                        """;
                var doc = documentFromString(input);
                assertEquals(1, size(doc.getBlocks()));
                var wrap = new Ref<>(doc.getBlocks().get(0));
                assertEquals(2, size(wrap.get().getBlocks()));
                assertEquals(2, size(wrap.get().getBlocks().get(0).getAttributes()));
                assertEquals(2, size(wrap.get().getBlocks().get(1).getAttributes()));
                assertNull(get(wrap.get().getBlocks().get(1).getAttributes(), "foo"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("can use parse_attributes to parse attrlist")
        void canUseParse_attributesToParseAttrlist() throws Exception {
            try {
                var parsedAttrs = new Ref<Map<String, Object>>(null);
                Extensions.register((Group) registry -> {
                    registry.block(processor -> {
                        processor.named("attrs");
                        processor.onContext("open");
                        processor.process((parent, reader, attrs) -> {
                            parsedAttrs.set(processor.parseAttributes(parent, reader.readLine(), Options.of("positional_attributes", list("a", "b"))));
                            parsedAttrs.get().putAll(processor.parseAttributes(parent, "foo={foo}", Options.of("sub_attributes", true)));
                            return null;
                        });
                    });
                });
                var input = """
                        :foo: bar

                        [attrs]
                        --
                        a,b,c,key=val
                        --
                        """;
                convertStringToEmbedded(input);
                assertEquals("a", get(parsedAttrs.get(), "a"));
                assertEquals("b", get(parsedAttrs.get(), "b"));
                assertEquals("val", get(parsedAttrs.get(), "key"));
                assertEquals("bar", get(parsedAttrs.get(), "foo"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("create_section should set up all section properties")
        void create_sectionShouldSetUpAllSectionProperties() throws Exception {
            try {
                var sect = new Ref<Section>(null);
                Extensions.register((Group) registry -> {
                    registry.blockMacro(processor -> {
                        processor.named("sect");
                        processor.process((parent, ignored, attrs) -> {
                            var level = attrs.remove("level");
                            Map<String, Object> opts = level != null ? Options.of("level", toInt(level)) : Options.of();
                            if (Objects.equals(get(attrs, "id"), "false")) {
                                attrs.put("id", false);
                            }
                            if (Objects.equals(parent.getContext(), "preamble")) {
                                parent = (AbstractBlock) parent.getParent();
                            }
                            sect.set(processor.createSection(parent, "Section Title", attrs, opts));
                            return null;
                        });
                    });
                });
                var inputTpl = """
                        = Document Title
                        :doctype: book
                        :sectnums:

                        sect::[%s]
                        """;
                var cases = new LinkedHashMap<String, java.util.List<Object>>();
                cases.put("", list("chapter", 1, false, true, "_section_title", null));
                cases.put("level=0", list("part", 0, false, false, "_section_title", null));
                cases.put("level=0,alt", list("part", 0, false, true, "_section_title", Options.of("partnums", "")));
                cases.put("level=0,style=appendix", list("appendix", 1, true, true, "_section_title", null));
                cases.put("style=appendix", list("appendix", 1, true, true, "_section_title", null));
                cases.put("style=glossary", list("glossary", 1, true, false, "_section_title", null));
                cases.put("style=glossary,alt", list("glossary", 1, true, "chapter", "_section_title", Options.of("sectnums", "all")));
                cases.put("style=abstract", list("chapter", 1, false, true, "_section_title", null));
                cases.put("id=section-title", list("chapter", 1, false, true, "section-title", null));
                cases.put("id=false", list("chapter", 1, false, true, null, null));
                for (var entry : cases.entrySet()) {
                    var expected = entry.getValue();
                    var input = inputTpl.formatted(entry.getKey());
                    documentFromString(input, Options.of("safe", "server", "attributes", expected.get(5)));
                    assertEquals(expected.get(0), sect.get().getSectionName());
                    assertEquals(expected.get(1), sect.get().getLevel());
                    assertEquals(expected.get(2), sect.get().isSpecial());
                    assertEquals(expected.get(3), sect.get().getNumbered());
                    if (expected.get(4) != null) {
                        assertEquals(expected.get(4), sect.get().getId());
                    } else {
                        assertNull(sect.get().getId());
                    }
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should add docinfo to document")
        void shouldAddDocinfoToDocument() throws Exception {
            var input = """
                    = Document Title

                    sample content
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.docinfoProcessor(MetaRobotsDocinfoProcessor.class);
                });
                var doc = documentFromString(input);
                assertEquals(SafeMode.SECURE.getLevel(), doc.getSafe());
                assertEquals("<meta name=\"robots\" content=\"index,follow\">", doc.getDocinfo());
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should add multiple docinfo to document")
        void shouldAddMultipleDocinfoToDocument() throws Exception {
            var input = """
                    = Document Title

                    sample content
                    """;
            try {
                Extensions.register((Group) registry -> {
                    registry.docinfoProcessor(MetaAppDocinfoProcessor.class);
                    registry.docinfoProcessor(MetaRobotsDocinfoProcessor.class, Options.of("position", ">>"));
                    registry.docinfoProcessor(processor -> {
                        processor.atLocation("footer");
                        processor.process(doc -> "<script><!-- analytics code --></script>");
                    });
                });
                var doc = documentFromString(input, Options.of("safe", "server"));
                assertEquals("<meta name=\"robots\" content=\"index,follow\">\n<meta name=\"application-name\" content=\"Asciidoctor App\">", doc.getDocinfo());
                assertEquals("<script><!-- analytics code --></script>", doc.getDocinfo("footer"));
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should append docinfo to document")
        void shouldAppendDocinfoToDocument() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.docinfoProcessor(MetaRobotsDocinfoProcessor.class);
                });
                var sampleInputPath = fixturePath("basic.adoc");
                var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "")));
                refuteEmpty(output);
                assertCss("script[src=\"modernizr.js\"]", output, 1);
                assertCss("meta[name=\"robots\"]", output, 1);
                assertCss("meta[http-equiv=\"imagetoolbar\"]", output, 0);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should return extension instance after registering")
        void shouldReturnExtensionInstanceAfterRegistering() throws Exception {
            try {
                var exts = new ArrayList<Object>();
                Extensions.register((Group) registry -> {
                    exts.add(registry.preprocessor(SamplePreprocessor.class));
                    exts.add(registry.includeProcessor(SampleIncludeProcessor.class));
                    exts.add(registry.treeProcessor(SampleTreeProcessor));
                    exts.add(registry.docinfoProcessor(SampleDocinfoProcessor.class));
                    exts.add(registry.postprocessor(SamplePostprocessor.class));
                });
                emptyDocument();
                for (var ext : exts) {
                    assertInstanceOf(ProcessorExtension.class, ext);
                }
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should raise exception if document processor extension does not provide process method")
        void shouldRaiseExceptionIfDocumentProcessorExtensionDoesNotProvideProcessMethod() throws Exception {
            var extensionRegistry = Extensions.create((Group) registry -> {
                registry.treeProcessor(processor -> {

                });
            });
            var exception = assertThrows(IllegalStateException.class, () -> convertStringToEmbedded("content", Options.of("extension_registry", extensionRegistry)));
            assertMatch(rx("No block specified to process tree processor extension"), exception.getMessage());
        }

        @Test
        @DisplayName("should raise exception if syntax processor extension does not provide process method")
        void shouldRaiseExceptionIfSyntaxProcessorExtensionDoesNotProvideProcessMethod() throws Exception {
            var extensionRegistry = Extensions.create((Group) registry -> {
                registry.blockMacro("foo", processor -> {

                });
            });
            var exception = assertThrows(IllegalStateException.class, () -> convertStringToEmbedded("foo::bar[]", Options.of("extension_registry", extensionRegistry)));
            assertMatch(rx("No block specified to process block macro extension"), exception.getMessage());
        }

        @Test
        @Disabled("a singleton method on a String is specific to Ruby")
        @DisplayName("should raise exception if syntax processor extension does not provide a name")
        void shouldRaiseExceptionIfSyntaxProcessorExtensionDoesNotProvideAName() throws Exception {
        }

        @Test
        @DisplayName("should raise an exception if mandatory target attribute is not provided for image block")
        void shouldRaiseAnExceptionIfMandatoryTargetAttributeIsNotProvidedForImageBlock() throws Exception {
            var input = "cat_in_sink::[]";
            var exception = assertThrows(IllegalArgumentException.class, () -> convertStringToEmbedded(input, Options.of("extension_registry", createCatInSinkBlockMacro())));
            assertMatch(rx("target attribute is required"), exception.getMessage());
        }

        @Test
        @DisplayName("should assign alt attribute to image block if alt is not provided")
        void shouldAssignAltAttributeToImageBlockIfAltIsNotProvided() throws Exception {
            var input = "cat_in_sink::25[]";
            var doc = documentFromString(input, Options.of("standalone", false, "extension_registry", createCatInSinkBlockMacro()));
            var image = doc.getBlocks().get(0);
            assertEquals("cat in sink day 25", image.getAttribute("alt"));
            assertEquals("cat in sink day 25", image.getAttribute("default-alt"));
            var output = doc.convert();
            assertIncludes(output, "<img src=\"cat-in-sink-day-25.png\" alt=\"cat in sink day 25\">");
        }

        @Test
        @DisplayName("should create an image block if mandatory attributes are provided")
        void shouldCreateAnImageBlockIfMandatoryAttributesAreProvided() throws Exception {
            var input = "cat_in_sink::30[cat in sink (yes)]";
            var doc = documentFromString(input, Options.of("standalone", false, "extension_registry", createCatInSinkBlockMacro()));
            var image = doc.getBlocks().get(0);
            assertEquals("cat in sink (yes)", image.getAttribute("alt"));
            assertFalsy(image.hasAttribute("default-alt"));
            var output = doc.convert();
            assertIncludes(output, "<img src=\"cat-in-sink-day-30.png\" alt=\"cat in sink (yes)\">");
        }

        @Test
        @DisplayName("should not assign caption on image block if title is not set on custom block macro")
        void shouldNotAssignCaptionOnImageBlockIfTitleIsNotSetOnCustomBlockMacro() throws Exception {
            var input = "cat_in_sink::30[]";
            var doc = documentFromString(input, Options.of("standalone", false, "extension_registry", createCatInSinkBlockMacro()));
            var output = doc.convert();
            assertXpath("/*[@class=\"imageblock\"]/*[@class=\"title\"]", output, 0);
        }

        @Test
        @DisplayName("should assign caption on image block if title is set on custom block macro")
        void shouldAssignCaptionOnImageBlockIfTitleIsSetOnCustomBlockMacro() throws Exception {
            var input = """
                    .Cat in Sink?
                    cat_in_sink::30[]
                    """;
            var doc = documentFromString(input, Options.of("standalone", false, "extension_registry", createCatInSinkBlockMacro()));
            var output = doc.convert();
            assertXpath("/*[@class=\"imageblock\"]/*[@class=\"title\"][text()=\"Figure 1. Cat in Sink?\"]", output, 1);
        }

        @Test
        @DisplayName("should not fail if alt attribute is not set on block image node")
        void shouldNotFailIfAltAttributeIsNotSetOnBlockImageNode() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.blockMacro("no_alt", processor -> {
                        processor.process((parent, target, attrs) -> processor.createBlock(parent, "image", null, Options.of("target", "picture.jpg")));
                    });
                });
                var output = Asciidoctor.convert("no_alt::[]");
                assertInclude("<img src=\"picture.jpg\" alt=\"\">", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should not fail if alt attribute is not set on inline image node")
        void shouldNotFailIfAltAttributeIsNotSetOnInlineImageNode() throws Exception {
            try {
                Extensions.register((Group) registry -> {
                    registry.inlineMacro("no_alt", processor -> {
                        processor.matchFormat("short");
                        processor.process((parent, target, attrs) -> processor.createInline(parent, "image", null, Options.of("target", "picture.jpg")));
                    });
                });
                var output = Asciidoctor.convert("no_alt:[]");
                assertInclude("<span class=\"image\"><img src=\"picture.jpg\" alt=\"\"></span>", output);
            } finally {
                Extensions.unregisterAll();
            }
        }

        @Test
        @DisplayName("should assign id and role on list items unordered")
        void shouldAssignIdAndRoleOnListItemsUnordered() throws Exception {
            var input = "santa_list::ulist[]";
            var doc = documentFromString(input, Options.of("standalone", false, "extension_registry", createSantaListBlockMacro()));
            var output = doc.convert();
            assertXpath("/div[@class=\"ulist\"]/ul/li[@class=\"friendly\"][@id=\"santa-list-guillaume\"]", output, 1);
            assertXpath("/div[@class=\"ulist\"]/ul/li[@class=\"kind contributor java\"]", output, 1);
            assertXpath("/div[@class=\"ulist\"]/ul/li[@class=\"kind contributor java\"][not(@id)]", output, 1);
            assertXpath("/div[@class=\"ulist\"]/ul/li[@id=\"santa-list-pepijn\"][not(@class)]", output, 1);
            assertXpath("/div[@class=\"ulist\"]/ul/li[@id=\"santa-list-dan\"][@class=\"naughty\"]", output, 1);
            assertXpath("/div[@class=\"ulist\"]/ul/li[not(@id)][not(@class)]/p[text()=\"Sarah\"]", output, 1);
        }

        @Test
        @DisplayName("should assign id and role on list items ordered")
        void shouldAssignIdAndRoleOnListItemsOrdered() throws Exception {
            var input = "santa_list::olist[]";
            var doc = documentFromString(input, Options.of("standalone", false, "extension_registry", createSantaListBlockMacro()));
            var output = doc.convert();
            assertXpath("/div[@class=\"olist\"]/ol/li[@class=\"friendly\"][@id=\"santa-list-guillaume\"]", output, 1);
            assertXpath("/div[@class=\"olist\"]/ol/li[@class=\"kind contributor java\"]", output, 1);
            assertXpath("/div[@class=\"olist\"]/ol/li[@class=\"kind contributor java\"][not(@id)]", output, 1);
            assertXpath("/div[@class=\"olist\"]/ol/li[@id=\"santa-list-pepijn\"][not(@class)]", output, 1);
            assertXpath("/div[@class=\"olist\"]/ol/li[@id=\"santa-list-dan\"][@class=\"naughty\"]", output, 1);
            assertXpath("/div[@class=\"olist\"]/ol/li[not(@id)][not(@class)]/p[text()=\"Sarah\"]", output, 1);
        }
    }

    // the sample extensions (the equivalent of the classes defined at the top of extensions_test.rb)

    public static class SamplePreprocessor extends Preprocessor {
        @Override
        public Reader process(Document document, PreprocessorReader reader) {
            return null;
        }
    }

    public static class SampleIncludeProcessor extends IncludeProcessor {
        // NOTE legacy signature; in Java, a boolean is returned instead of 'affirmative'
        @Override
        public boolean handles(String target) {
            return "include.adoc".equals(target);
        }
    }

    public static class SampleDocinfoProcessor extends DocinfoProcessor {
    }

    public static class SampleTreeprocessor extends TreeProcessor {
        @Override
        public Document process(Document document) {
            return null;
        }
    }

    public static class SamplePostprocessor extends Postprocessor {
    }

    public static class SampleBlock extends BlockProcessor {
        public SampleBlock(String name, Map<String, Object> config) {
            super(name, config);
        }
    }

    public static class SampleBlockMacro extends BlockMacroProcessor {
        public SampleBlockMacro(String name, Map<String, Object> config) {
            super(name, config);
        }
    }

    public static class SampleInlineMacro extends InlineMacroProcessor {
        public SampleInlineMacro(String name, Map<String, Object> config) {
            super(name, config);
        }
    }

    public static class ScrubHeaderPreprocessor extends Preprocessor {
        @Override
        public Reader process(Document doc, PreprocessorReader reader) {
            var lines = reader.getLines();
            var skipped = new ArrayList<String>();
            while (!lines.isEmpty() && !lines.get(0).startsWith("=")) {
                skipped.add(lines.remove(0));
                reader.advance();
            }
            doc.setAttribute("skipped", String.join("\n", skipped));
            return reader;
        }
    }

    public static class BoilerplateTextIncludeProcessor extends IncludeProcessor {
        @Override
        public boolean handles(String target) {
            return target.endsWith(".txt");
        }

        @Override
        public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
            if (target.equals("lorem-ipsum.txt")) {
                var content = list("Lorem ipsum dolor sit amet...\n");
                reader.pushInclude(content, target, target, 1, attributes);
            }
        }
    }

    public static class ReplaceAuthorTreeProcessor extends TreeProcessor {
        @Override
        public Document process(Document document) {
            document.getAttributes().put("firstname", "Ghost");
            document.getAttributes().put("author", "Ghost Writer");
            return document;
        }
    }

    public static class ReplaceTreeTreeProcessor extends TreeProcessor {
        @Override
        public Document process(Document document) {
            if ("Original Document".equals(document.getDoctitle())) {
                return Asciidoctor.load("== Replacement Document\nReplacement Author\n\ncontent");
            }
            return document;
        }
    }

    public static class SelfSigningTreeProcessor extends TreeProcessor {
        @Override
        public Document process(Document document) {
            document.append(createParagraph(document, getClass().getSimpleName(), new LinkedHashMap<>()));
            return null;
        }
    }

    public static class StripAttributesPostprocessor extends Postprocessor {
        @Override
        public String process(Document document, String output) {
            return gsub(output, rx("<(\\w+).*?>", "m"), "<\\1>");
        }
    }

    public static class UppercaseBlock extends BlockProcessor {
        public UppercaseBlock(String name, Map<String, Object> config) {
            super(name, config);
            if (this.name == null) {
                named("yell");
            }
            onContext("paragraph");
            positionalAttributes("chars");
            parseContentAs(ContentModel.SIMPLE);
        }

        @Override
        public AbstractBlock process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
            if (attributes.get("chars") instanceof String chars) {
                var upcaseChars = chars.toUpperCase();
                return createParagraph(parent, map(reader.getLines(), l -> Strings.tr(l.toLowerCase(), chars, upcaseChars)), attributes);
            }
            return createParagraph(parent, map(reader.getLines(), String::toUpperCase), attributes);
        }
    }

    public static class SnippetMacro extends BlockMacroProcessor {
        public SnippetMacro(String name, Map<String, Object> config) {
            super(name, config);
        }

        @Override
        public AbstractBlock process(AbstractBlock parent, String target, Map<String, Object> attributes) {
            return createPassBlock(parent, "<script src=\"http://example.com/" + target + ".js?_mode=" + attributes.get("mode") + "\"></script>",
                    new LinkedHashMap<>(), Options.of("content_model", ContentModel.RAW));
        }
    }

    public static class LegacyPosAttrsBlockMacro extends BlockMacroProcessor {
        public LegacyPosAttrsBlockMacro(String name, Map<String, Object> config) {
            super(name, config);
            option("pos_attrs", list("target", "format"));
        }

        @Override
        public AbstractBlock process(AbstractBlock parent, String target, Map<String, Object> attrs) {
            return createImageBlock(parent, Options.of("target", attrs.get("target") + "." + attrs.get("format")));
        }
    }

    public static class TemperatureMacro extends InlineMacroProcessor {
        public TemperatureMacro(String name, Map<String, Object> config) {
            super(name, config);
            if (this.name == null) {
                named("degrees");
            }
            resolveAttributes("1:units", "precision=1");
        }

        @Override
        public Object process(AbstractNode parent, String target, Map<String, Object> attributes) {
            var units = attributes.get("units") != null ? attributes.get("units").toString()
                    : String.valueOf(parent.getDocument().getAttribute("temperature-unit", "C"));
            var precision = Strings.toInt(attributes.get("precision"));
            var c = Strings.toDouble(target);
            return switch (units) {
                case "C" -> createInline(parent, "quoted", round(c, precision) + " &#176;C", Options.of("type", "unquoted"));
                case "F" -> createInline(parent, "quoted", round(c * 1.8 + 32, precision) + " &#176;F", Options.of("type", "unquoted"));
                default -> throw new IllegalArgumentException("Unknown temperature units: " + units);
            };
        }

        /** Rounds the value like Float#round in Ruby (an Integer if the precision is 0). */
        private static String round(double value, int precision) {
            var rounded = java.math.BigDecimal.valueOf(value).setScale(precision, java.math.RoundingMode.HALF_UP);
            return precision == 0 ? rounded.toBigInteger().toString() : rounded.toPlainString();
        }
    }

    public static class MetaRobotsDocinfoProcessor extends DocinfoProcessor {
        @Override
        public String process(Document document) {
            return "<meta name=\"robots\" content=\"index,follow\">";
        }
    }

    public static class MetaAppDocinfoProcessor extends DocinfoProcessor {
        public MetaAppDocinfoProcessor() {
            atLocation("head");
        }

        @Override
        public String process(Document document) {
            return "<meta name=\"application-name\" content=\"Asciidoctor App\">";
        }
    }

    public static class SampleExtensionGroup implements Group {
        @Override
        public void activate(Registry registry) {
            registry.getDocument().getAttributes().put("activate-method-called", "");
            registry.preprocessor(SamplePreprocessor.class);
        }
    }

    static Registry createCatInSinkBlockMacro() {
        return Extensions.create(registry -> registry.blockMacro(processor -> {
            processor.named("cat_in_sink");
            processor.process((parent, target, attrs) -> {
                Map<String, Object> imageAttrs = new LinkedHashMap<>();
                if (!Strings.isNullOrEmpty(target)) {
                    imageAttrs.put("target", "cat-in-sink-day-" + target + ".png");
                }
                var title = attrs.remove("title");
                if (title != null) {
                    imageAttrs.put("title", title);
                }
                var alt = attrs.remove("1");
                if (alt != null) {
                    imageAttrs.put("alt", alt);
                }
                return processor.createImageBlock(parent, imageAttrs);
            });
        }));
    }

    static Registry createSantaListBlockMacro() {
        return Extensions.create(registry -> registry.blockMacro(processor -> {
            processor.named("santa_list");
            processor.process((parent, target, attrs) -> {
                var list = processor.createList(parent, target);
                var guillaume = processor.createListItem(list, "Guillaume");
                guillaume.addRole("friendly");
                guillaume.setId("santa-list-guillaume");
                list.append(guillaume);
                var robert = processor.createListItem(list, "Robert");
                robert.addRole("kind");
                robert.addRole("contributor");
                robert.addRole("java");
                list.append(robert);
                var pepijn = processor.createListItem(list, "Pepijn");
                pepijn.setId("santa-list-pepijn");
                list.append(pepijn);
                var dan = processor.createListItem(list, "Dan");
                dan.addRole("naughty");
                dan.setId("santa-list-dan");
                list.append(dan);
                var sarah = processor.createListItem(list, "Sarah");
                list.append(sarah);
                return list;
            });
        }));
    }
}
