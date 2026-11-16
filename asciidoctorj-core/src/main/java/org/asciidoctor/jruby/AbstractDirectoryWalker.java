package org.asciidoctor.jruby;

/**
 * @deprecated Use {@link org.asciidoctor.util.AbstractDirectoryWalker} instead.
 */
@Deprecated
public abstract class AbstractDirectoryWalker extends org.asciidoctor.util.AbstractDirectoryWalker implements DirectoryWalker {

    public AbstractDirectoryWalker(String baseDir) {
        super(baseDir);
    }
}
