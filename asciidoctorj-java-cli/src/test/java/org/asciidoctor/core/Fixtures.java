package org.asciidoctor.core;

import java.nio.file.Path;

// The paths of the test resources and of the fixtures for the CLI tests.
// The CLI tests use relative paths on purpose, and the resources are always in the same place. So the relative path
// depends on the working directory: in the JVM it is the same as in the module asciidoctorj-java, which has the
// fixtures and is the working directory of the task test.
public final class Fixtures {

    private Fixtures() {
    }

    // The path of the file or directory in the test resources, relative to the working directory
    public static String relativeTestPath(String path) {
        Path workingDir = Path.of("").toAbsolutePath().normalize();
        return workingDir.relativize(Path.of(TestHelper.testdir(), path)).toString().replace('\\', '/');
    }

    // The path of the fixture, relative to the working directory
    public static String relativeFixturePath(String name) {
        return relativeTestPath("fixtures/" + name);
    }
}
