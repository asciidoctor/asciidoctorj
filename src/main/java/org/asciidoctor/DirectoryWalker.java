package org.asciidoctor;

import java.io.File;
import java.util.List;

public interface DirectoryWalker {

    List<File> scan();

}