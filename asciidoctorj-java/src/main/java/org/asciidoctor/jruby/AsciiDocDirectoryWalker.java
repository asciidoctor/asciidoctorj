package org.asciidoctor.jruby;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 
 * Directory walker that finds all asciidoc files inside a folder and in all its subfolders.
 * 
 * It returns only the files which their extensions are: .asc, .asciidoc, .ad or .adoc.
 * 
 * @author lordofthejars
 *
 */
public class AsciiDocDirectoryWalker extends AbstractDirectoryWalker {

    private static final String ASCIIDOC_REG_EXP_EXTENSION = "^[^_.].*\\.a((sc(iidoc)?)|d(oc)?)$";

    private static final Pattern ASCIIDOC_EXTENSION_PATTERN = Pattern.compile(ASCIIDOC_REG_EXP_EXTENSION);

    public AsciiDocDirectoryWalker(String baseDir) {
        super(baseDir);
    }

    @Override
    protected boolean isAcceptedFile(File file) {
        String fileName = file.getName();
        return isAsciidocExtension(fileName);
    }

    private boolean isAsciidocExtension(String fileName) {
        return ASCIIDOC_EXTENSION_PATTERN.matcher(fileName).matches();
    }

}