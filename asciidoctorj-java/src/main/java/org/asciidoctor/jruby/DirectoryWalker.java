package org.asciidoctor.jruby;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public interface DirectoryWalker extends Iterable<File> {

    List<File> scan();

    @Override
    default Iterator<File> iterator() {
        return scan().iterator();
    }
}