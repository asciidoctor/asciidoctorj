package org.asciidoctor.jruby;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class which inspects parent directory and all subdirectories and returns all files which match certain
 * condition.
 * 
 * @author lordofthejars
 * 
 */
public abstract class AbstractDirectoryWalker implements DirectoryWalker {

    private final String baseDir;

    public AbstractDirectoryWalker(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Method that finds all files that meets some criteria.
     * 
     * @return List of files which meets the defined criteria.
     */
    @Override
    public List<File> scan() {

        File baseDirFile = new File(baseDir);

        List<File> includedFiles = walkDirectory(baseDirFile);
        return includedFiles;
    }

    private List<File> walkDirectory(File parentDir) {

        final List<File> includedAsciidocFiles = new ArrayList<File>();

        final File[] listOfCurrentFiles = parentDir.listFiles();

        if (listOfCurrentFiles != null) {

            for (File currentFile : listOfCurrentFiles) {
                if (currentFile.isDirectory()) {

                    if (!currentFile.getName().startsWith(".") && !currentFile.getName().startsWith("_")) {
                        List<File> asciidocFiles = walkDirectory(currentFile);
                        includedAsciidocFiles.addAll(asciidocFiles);
                    }

                } else {

                    if (isAcceptedFile(currentFile)) {
                        includedAsciidocFiles.add(currentFile);
                    }

                }
            }

        }

        return includedAsciidocFiles;

    }

    /**
     * Method to implement which is called to decide if file should be filtered or not.
     * 
     * @param filename
     *            current file.
     * @return true if file should be added to returned list, false otherwise.
     */
    protected abstract boolean isAcceptedFile(File filename);

}
