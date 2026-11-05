package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import org.junit.jupiter.api.*;

@DisplayName("Path Resolver")
public class PathsTest extends TestHelper {
    PathResolver resolver;
    MemoryLogger logger;

    @Nested
    @DisplayName("Web Paths")
    class WebPaths {

        @BeforeEach
        void setup() throws Exception {
            resolver = new PathResolver();
        }

        @Test
        @DisplayName("target with absolute path")
        void targetWithAbsolutePath() throws Exception {
            assertEquals("/images", resolver.webPath("/images"));
            assertEquals("/images", resolver.webPath("/images", ""));
            assertEquals("/images", resolver.webPath("/images", null));
        }

        @Test
        @DisplayName("target with relative path")
        void targetWithRelativePath() throws Exception {
            assertEquals("images", resolver.webPath("images"));
            assertEquals("images", resolver.webPath("images", ""));
            assertEquals("images", resolver.webPath("images", null));
        }

        @Test
        @DisplayName("target with hidden relative path")
        void targetWithHiddenRelativePath() throws Exception {
            assertEquals(".images", resolver.webPath(".images"));
            assertEquals(".images", resolver.webPath(".images", ""));
            assertEquals(".images", resolver.webPath(".images", null));
        }

        @Test
        @DisplayName("target with path relative to current directory")
        void targetWithPathRelativeToCurrentDirectory() throws Exception {
            assertEquals("./images", resolver.webPath("./images"));
            assertEquals("./images", resolver.webPath("./images", ""));
            assertEquals("./images", resolver.webPath("./images", null));
        }

        @Test
        @DisplayName("target with absolute path ignores start path")
        void targetWithAbsolutePathIgnoresStartPath() throws Exception {
            assertEquals("/images", resolver.webPath("/images", "foo"));
            assertEquals("/images", resolver.webPath("/images", "/foo"));
            assertEquals("/images", resolver.webPath("/images", "./foo"));
        }

        @Test
        @DisplayName("target with relative path appended to start path")
        void targetWithRelativePathAppendedToStartPath() throws Exception {
            assertEquals("assets/images", resolver.webPath("images", "assets"));
            assertEquals("/assets/images", resolver.webPath("images", "/assets"));
            assertEquals("./assets/images", resolver.webPath("images", "./assets"));
            assertEquals("/theme.css", resolver.webPath("theme.css", "/"));
            assertEquals("/css/theme.css", resolver.webPath("theme.css", "/css/"));
        }

        @Test
        @DisplayName("target with path relative to current directory appended to start path")
        void targetWithPathRelativeToCurrentDirectoryAppendedToStartPath() throws Exception {
            assertEquals("assets/images", resolver.webPath("./images", "assets"));
            assertEquals("/assets/images", resolver.webPath("./images", "/assets"));
            assertEquals("./assets/images", resolver.webPath("./images", "./assets"));
        }

        @Test
        @DisplayName("target with relative path appended to url start path")
        void targetWithRelativePathAppendedToUrlStartPath() throws Exception {
            assertEquals("http://www.example.com/assets/images", resolver.webPath("images", "http://www.example.com/assets"));
        }

        @Test
        @DisplayName("normalize target")
        void normalizeTarget() throws Exception {
            assertEquals("../images", resolver.webPath("../images/../images"));
        }

        @Test
        @DisplayName("append target to start path and normalize")
        void appendTargetToStartPathAndNormalize() throws Exception {
            assertEquals("../images", resolver.webPath("../images/../images", "../images"));
            assertEquals("../../images", resolver.webPath("../images", ".."));
        }

        @Test
        @DisplayName("normalize parent directory that follows root")
        void normalizeParentDirectoryThatFollowsRoot() throws Exception {
            assertEquals("/tiger.png", resolver.webPath("/../tiger.png"));
            assertEquals("/tiger.png", resolver.webPath("/../../tiger.png"));
        }

        @Test
        @DisplayName("uses start when target is empty")
        void usesStartWhenTargetIsEmpty() throws Exception {
            assertEquals("assets/images", resolver.webPath("", "assets/images"));
            assertEquals("assets/images", resolver.webPath(null, "assets/images"));
        }

        @Test
        @DisplayName("posixifies windows paths")
        void posixifiesWindowsPaths() throws Exception {
            resolver.setFileSeparator("\\");
            assertEquals("/images", resolver.webPath("\\images"));
            assertEquals("../images", resolver.webPath("..\\images"));
            assertEquals("/images", resolver.webPath("\\..\\images"));
            assertEquals("assets/images", resolver.webPath("assets\\images"));
            assertEquals("../assets/images", resolver.webPath("assets\\images", "..\\images\\.."));
        }

        @Test
        @DisplayName("URL encode spaces in path")
        void urlEncodeSpacesInPath() throws Exception {
            assertEquals("assets%20and%20stuff/lots%20of%20images", resolver.webPath("lots of images", "assets and stuff"));
        }
    }

    @Nested
    @DisplayName("System Paths")
    class SystemPaths {
        static final String JAIL = "/home/doctor/docs";
        Logger defaultLogger = LoggerManager.getLogger();

        @BeforeEach
        void setup() throws Exception {
            resolver = new PathResolver();
            logger = new MemoryLogger();
            LoggerManager.setLogger(logger);
        }

        @AfterEach
        void teardown() throws Exception {
            LoggerManager.setLogger(defaultLogger);
        }

        @Test
        @DisplayName("raises security error if jail is not an absolute path")
        void raisesSecurityErrorIfJailIsNotAnAbsolutePath() throws Exception {
            assertThrows(SecurityException.class, () -> {
                resolver.systemPath("images/tiger.png", "/etc", "foo");
            });
        }

        @Test
        @DisplayName("prevents access to paths outside of jail")
        void preventsAccessToPathsOutsideOfJail() throws Exception {
            var result = resolver.systemPath("../../../../../css", (str(JAIL) + "/assets/stylesheets"), JAIL);
            assertEquals((str(JAIL) + "/css"), result);
            assertMessage(logger, "WARN", "path has illegal reference to ancestor of jail; recovering automatically");
            logger.clear();
            result = resolver.systemPath("/../../../../../css", (str(JAIL) + "/assets/stylesheets"), JAIL);
            assertEquals((str(JAIL) + "/css"), result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            logger.clear();
            result = resolver.systemPath("../../../css", "../../..", JAIL);
            assertEquals((str(JAIL) + "/css"), result);
            assertMessage(logger, "WARN", "path has illegal reference to ancestor of jail; recovering automatically");
        }

        @Test
        @DisplayName("throws exception for illegal path access if recover is false")
        void throwsExceptionForIllegalPathAccessIfRecoverIsFalse() throws Exception {
            assertThrows(SecurityException.class, () -> {
                resolver.systemPath("../../../../../css", (str(JAIL) + "/assets/stylesheets"), JAIL, Options.of("recover", false));
            });
        }

        @Test
        @DisplayName("resolves start path if target is empty")
        void resolvesStartPathIfTargetIsEmpty() throws Exception {
            assertEquals((str(JAIL) + "/assets/stylesheets"), resolver.systemPath("", (str(JAIL) + "/assets/stylesheets"), JAIL));
            assertEquals((str(JAIL) + "/assets/stylesheets"), resolver.systemPath(null, (str(JAIL) + "/assets/stylesheets"), JAIL));
        }

        @Test
        @DisplayName("expands parent references in start path if target is empty")
        void expandsParentReferencesInStartPathIfTargetIsEmpty() throws Exception {
            assertEquals((str(JAIL) + "/stylesheets"), resolver.systemPath("", (str(JAIL) + "/assets/../stylesheets"), JAIL));
        }

        @Test
        @DisplayName("expands parent references in start path if target is not empty")
        void expandsParentReferencesInStartPathIfTargetIsNotEmpty() throws Exception {
            assertEquals((str(JAIL) + "/stylesheets/site.css"), resolver.systemPath("site.css", (str(JAIL) + "/assets/../stylesheets"), JAIL));
        }

        @Test
        @DisplayName("resolves start path if target is dot")
        void resolvesStartPathIfTargetIsDot() throws Exception {
            assertEquals((str(JAIL) + "/assets/stylesheets"), resolver.systemPath(".", (str(JAIL) + "/assets/stylesheets"), JAIL));
            assertEquals((str(JAIL) + "/assets/stylesheets"), resolver.systemPath("./", (str(JAIL) + "/assets/stylesheets"), JAIL));
        }

        @Test
        @DisplayName("treats absolute target outside of jail as relative when jail is specified")
        void treatsAbsoluteTargetOutsideOfJailAsRelativeWhenJailIsSpecified() throws Exception {
            var result = resolver.systemPath("/", (str(JAIL) + "/assets/stylesheets"), JAIL);
            assertEquals(JAIL, result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            logger.clear();
            result = resolver.systemPath("/foo", (str(JAIL) + "/assets/stylesheets"), JAIL);
            assertEquals((str(JAIL) + "/foo"), result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            logger.clear();
            result = resolver.systemPath("/../foo", (str(JAIL) + "/assets/stylesheets"), JAIL);
            assertEquals((str(JAIL) + "/foo"), result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            logger.clear();
            resolver.setFileSeparator("\\");
            result = resolver.systemPath("baz.adoc", "C:/foo", "C:/bar");
            assertEquals("C:/bar/baz.adoc", result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
        }

        @Test
        @DisplayName("allows use of absolute target or start if resolved path is sub-path of jail")
        void allowsUseOfAbsoluteTargetOrStartIfResolvedPathIsSubPathOfJail() throws Exception {
            assertEquals((str(JAIL) + "/my/path"), resolver.systemPath((str(JAIL) + "/my/path"), "", JAIL));
            assertEquals((str(JAIL) + "/my/path"), resolver.systemPath((str(JAIL) + "/my/path"), null, JAIL));
            assertEquals((str(JAIL) + "/my/path"), resolver.systemPath("", (str(JAIL) + "/my/path"), JAIL));
            assertEquals((str(JAIL) + "/my/path"), resolver.systemPath(null, (str(JAIL) + "/my/path"), JAIL));
            assertEquals((str(JAIL) + "/my/path"), resolver.systemPath("path", (str(JAIL) + "/my"), JAIL));
            assertEquals("/foo/bar/baz.adoc", resolver.systemPath("/foo/bar/baz.adoc", null, "/"));
            assertEquals("/foo/bar/baz.adoc", resolver.systemPath("baz.adoc", "/foo/bar", "/"));
            assertEquals("/foo/bar/baz.adoc", resolver.systemPath("baz.adoc", "foo/bar", "/"));
        }

        @Test
        @DisplayName("uses jail path if start path is empty")
        void usesJailPathIfStartPathIsEmpty() throws Exception {
            assertEquals((str(JAIL) + "/images/tiger.png"), resolver.systemPath("images/tiger.png", "", JAIL));
            assertEquals((str(JAIL) + "/images/tiger.png"), resolver.systemPath("images/tiger.png", null, JAIL));
        }

        @Test
        @DisplayName("warns if start is not contained within jail")
        void warnsIfStartIsNotContainedWithinJail() throws Exception {
            var result = resolver.systemPath("images/tiger.png", "/etc", JAIL);
            assertEquals((str(JAIL) + "/images/tiger.png"), result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            logger.clear();
            result = resolver.systemPath(".", "/etc", JAIL);
            assertEquals(JAIL, result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            logger.clear();
            resolver.setFileSeparator("\\");
            result = resolver.systemPath(".", "C:/foo", "C:/bar");
            assertEquals("C:/bar", result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
        }

        @Test
        @DisplayName("allows start path to be parent of jail if resolved target is inside jail")
        void allowsStartPathToBeParentOfJailIfResolvedTargetIsInsideJail() throws Exception {
            assertEquals((str(JAIL) + "/foo/path"), resolver.systemPath("foo/path", JAIL, (str(JAIL) + "/foo")));
            resolver.setFileSeparator("\\");
            assertEquals("C:/dev/project/README.adoc", resolver.systemPath("project/README.adoc", "C:/dev", "C:/dev/project"));
        }

        @Test
        @DisplayName("relocates target to jail if resolved value fails outside of jail")
        void relocatesTargetToJailIfResolvedValueFailsOutsideOfJail() throws Exception {
            var result = resolver.systemPath("bar/baz.adoc", JAIL, (str(JAIL) + "/foo"));
            assertEquals((str(JAIL) + "/foo/bar/baz.adoc"), result);
            assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            logger.clear();
            resolver.setFileSeparator("\\");
            result = resolver.systemPath("bar/baz.adoc", "D:/", "C:/foo");
            assertEquals("C:/foo/bar/baz.adoc", result);
            assertMessage(logger, "WARN", "~outside of jail root");
        }

        @Test
        @DisplayName("raises security error if start is not contained within jail and recover is disabled")
        void raisesSecurityErrorIfStartIsNotContainedWithinJailAndRecoverIsDisabled() throws Exception {
            assertThrows(SecurityException.class, () -> {
                resolver.systemPath("images/tiger.png", "/etc", JAIL, Options.of("recover", false));
            });
            assertThrows(SecurityException.class, () -> {
                resolver.systemPath(".", "/etc", JAIL, Options.of("recover", false));
            });
        }

        @Test
        @DisplayName("expands parent references in absolute path if jail is not specified")
        void expandsParentReferencesInAbsolutePathIfJailIsNotSpecified() throws Exception {
            assertEquals("/etc/stylesheet.css", resolver.systemPath("/usr/share/../../etc/stylesheet.css"));
        }

        @Test
        @DisplayName("resolves absolute directory if jail is not specified")
        void resolvesAbsoluteDirectoryIfJailIsNotSpecified() throws Exception {
            assertEquals("/usr/share/stylesheet.css", resolver.systemPath("/usr/share/stylesheet.css", "/home/dallen/docs/assets/stylesheets"));
        }

        @Test
        @DisplayName("resolves ancestor directory of start if jail is not specified")
        void resolvesAncestorDirectoryOfStartIfJailIsNotSpecified() throws Exception {
            assertEquals("/usr/share/stylesheet.css", resolver.systemPath("../../../../../usr/share/stylesheet.css", "/home/dallen/docs/assets/stylesheets"));
        }

        @Test
        @DisplayName("resolves absolute path if start is absolute and target is relative")
        void resolvesAbsolutePathIfStartIsAbsoluteAndTargetIsRelative() throws Exception {
            assertEquals("/usr/share/assets/stylesheet.css", resolver.systemPath("assets/stylesheet.css", "/usr/share"));
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("File.dirname preserves UNC path root on Windows")
        void fileDirnamePreservesUNCPathRootOnWindows() throws Exception {
            assertEquals("\\\\server\\docs", fileDirname("\\\\server\\docs\\file.html"));
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("File.dirname preserves posix-style UNC path root on Windows")
        void fileDirnamePreservesPosixStyleUNCPathRootOnWindows() throws Exception {
            assertEquals("//server/docs", fileDirname("//server/docs/file.html"));
        }

        @Test
        @DisplayName("resolves UNC path if start is absolute and target is relative")
        void resolvesUNCPathIfStartIsAbsoluteAndTargetIsRelative() throws Exception {
            assertEquals("//QA/c$/users/asciidoctor/assets/stylesheet.css", resolver.systemPath("assets/stylesheet.css", "//QA/c$/users/asciidoctor"));
        }

        @Test
        @DisplayName("resolves UNC path if target is UNC path")
        void resolvesUNCPathIfTargetIsUNCPath() throws Exception {
            resolver.setFileSeparator("\\");
            assertEquals("//server/docs/output.html", resolver.systemPath("\\\\server\\docs\\output.html"));
        }

        @Test
        @DisplayName("resolves UNC path if target is posix-style UNC path")
        void resolvesUNCPathIfTargetIsPosixStyleUNCPath() throws Exception {
            assertEquals("//server/docs/output.html", resolver.systemPath("//server/docs/output.html"));
        }

        @Test
        @Disabled("JRuby only")
        @DisplayName("resolves classloader path if start is classloader path and target is relative")
        void resolvesClassloaderPathIfStartIsClassloaderPathAndTargetIsRelative() throws Exception {
        }

        @Test
        @Disabled("JRuby only")
        @DisplayName("resolves classloader path if start is root-relative classloader path and target is relative")
        void resolvesClassloaderPathIfStartIsRootRelativeClassloaderPathAndTargetIsRelative() throws Exception {
        }

        @Test
        @Disabled("JRuby only")
        @DisplayName("preserves classloader path if start is absolute path and target is classloader path")
        void preservesClassloaderPathIfStartIsAbsolutePathAndTargetIsClassloaderPath() throws Exception {
        }

        @Test
        @DisplayName("resolves relative target relative to current directory if start is empty")
        void resolvesRelativeTargetRelativeToCurrentDirectoryIfStartIsEmpty() throws Exception {
            var pwd = fileExpandPath(Helpers.workingDirectory());
            assertEquals((str(pwd) + "/images/tiger.png"), resolver.systemPath("images/tiger.png", ""));
            assertEquals((str(pwd) + "/images/tiger.png"), resolver.systemPath("images/tiger.png", null));
            assertEquals((str(pwd) + "/images/tiger.png"), resolver.systemPath("images/tiger.png"));
        }

        @Test
        @DisplayName("resolves relative hidden target relative to current directory if start is empty")
        void resolvesRelativeHiddenTargetRelativeToCurrentDirectoryIfStartIsEmpty() throws Exception {
            var pwd = fileExpandPath(Helpers.workingDirectory());
            assertEquals((str(pwd) + "/.images/tiger.png"), resolver.systemPath(".images/tiger.png", ""));
            assertEquals((str(pwd) + "/.images/tiger.png"), resolver.systemPath(".images/tiger.png", null));
        }

        @Test
        @DisplayName("resolves and normalizes start when target is empty")
        void resolvesAndNormalizesStartWhenTargetIsEmpty() throws Exception {
            var pwd = fileExpandPath(Helpers.workingDirectory());
            assertEquals("/home/doctor/docs", resolver.systemPath("", "/home/doctor/docs"));
            assertEquals("/home/doctor/docs", resolver.systemPath("", "/home/doctor/./docs"));
            assertEquals("/home/doctor/docs", resolver.systemPath(null, "/home/doctor/docs"));
            assertEquals("/home/doctor/docs", resolver.systemPath(null, "/home/doctor/./docs"));
            assertEquals((str(pwd) + "/assets/images"), resolver.systemPath(null, "assets/images"));
            resolver.systemPath("", "../assets/images", JAIL);
            assertMessage(logger, "WARN", "path has illegal reference to ancestor of jail; recovering automatically");
        }

        @Test
        @DisplayName("posixifies windows paths")
        void posixifiesWindowsPaths2() throws Exception {
            resolver.setFileSeparator("\\");
            assertEquals((str(JAIL) + "/assets/css"), resolver.systemPath("..\\css", "assets\\stylesheets", JAIL));
        }

        @Test
        @DisplayName("resolves windows paths when file separator is backlash")
        void resolvesWindowsPathsWhenFileSeparatorIsBacklash() throws Exception {
            resolver.setFileSeparator("\\");
            assertEquals("C:/data/docs", resolver.systemPath("..", "C:\\data\\docs\\assets", "C:\\data\\docs"));
            var result = resolver.systemPath("..\\..", "C:\\data\\docs\\assets", "C:\\data\\docs");
            assertEquals("C:/data/docs", result);
            assertMessage(logger, "WARN", "path has illegal reference to ancestor of jail; recovering automatically");
            logger.clear();
            result = resolver.systemPath("..\\..\\css", "C:\\data\\docs\\assets", "C:\\data\\docs");
            assertEquals("C:/data/docs/css", result);
            assertMessage(logger, "WARN", "path has illegal reference to ancestor of jail; recovering automatically");
        }

        @Test
        @DisplayName("should calculate relative path")
        void shouldCalculateRelativePath() throws Exception {
            var filename = resolver.systemPath("part1/chapter1/section1.adoc", null, JAIL);
            assertEquals((str(JAIL) + "/part1/chapter1/section1.adoc"), filename);
            assertEquals("part1/chapter1/section1.adoc", resolver.relativePath(filename, JAIL));
        }

        @Test
        @DisplayName("should resolve relative path to filename outside of base directory")
        void shouldResolveRelativePathToFilenameOutsideOfBaseDirectory() throws Exception {
            var filename = "/home/shared/partials";
            var baseDir = "/home/user/docs";
            var result = resolver.relativePath(filename, baseDir);
            assertEquals("../../shared/partials", result);
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should return original path if relative path cannot be computed")
        void shouldReturnOriginalPathIfRelativePathCannotBeComputed() throws Exception {
            var filename = "D:/path/to/include/file.txt";
            var baseDir = "C:/docs";
            var result = resolver.relativePath(filename, baseDir);
            assertEquals("D:/path/to/include/file.txt", result);
        }

        @Test
        @DisplayName("should resolve relative path relative to base dir in unsafe mode")
        void shouldResolveRelativePathRelativeToBaseDirInUnsafeMode() throws Exception {
            var baseDir = fixturePath("base");
            var doc = emptyDocument(Options.of("base_dir", baseDir, "safe", SafeMode.UNSAFE.getLevel()));
            var expected = joinPath(baseDir, "images", "tiger.png");
            var actual = doc.normalizeSystemPath("tiger.png", "images");
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("should resolve absolute path as absolute in unsafe mode")
        void shouldResolveAbsolutePathAsAbsoluteInUnsafeMode() throws Exception {
            var baseDir = fixturePath("base");
            var doc = emptyDocument(Options.of("base_dir", baseDir, "safe", SafeMode.UNSAFE.getLevel()));
            var actual = doc.normalizeSystemPath("tiger.png", "/etc/images");
            assertEquals("/etc/images/tiger.png", actual);
        }
    }
}
