package org.asciidoctor.api;

import java.io.File;
import java.util.List;

public interface DirectoryWalker {

    List<File> scan();

}
