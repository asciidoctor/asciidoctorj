package org.asciidoctor.jruby;

/**
 * @deprecated Use {@link org.asciidoctor.util.AsciiDocDirectoryWalker} instead.
 */
@Deprecated
public class AsciiDocDirectoryWalker extends org.asciidoctor.util.AsciiDocDirectoryWalker implements DirectoryWalker {

    public AsciiDocDirectoryWalker(String baseDir) {
        super(baseDir);
    }
}
