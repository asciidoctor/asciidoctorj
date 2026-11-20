package org.asciidoctor.jruby;

/**
 * @deprecated Use {@link org.asciidoctor.util.GlobDirectoryWalker} instead.
 */
@Deprecated
public class GlobDirectoryWalker extends org.asciidoctor.util.GlobDirectoryWalker implements DirectoryWalker {

    public GlobDirectoryWalker(String globExpression) {
        super(globExpression);
    }
}
