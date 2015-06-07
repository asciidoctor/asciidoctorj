package org.asciidoctor.arquillian;

import org.junit.rules.TemporaryFolder;

public class ScopedTemporaryFolder {

    private TemporaryFolder sharedTemporaryFolder;

    private TemporaryFolder unsharedTemporaryFolder;

    public TemporaryFolder getSharedTemporaryFolder() {
        return sharedTemporaryFolder;
    }

    public void setSharedTemporaryFolder(TemporaryFolder sharedTemporaryFolder) {
        this.sharedTemporaryFolder = sharedTemporaryFolder;
    }

    public TemporaryFolder getUnsharedTemporaryFolder() {
        return unsharedTemporaryFolder;
    }

    public void setUnsharedTemporaryFolder(TemporaryFolder unsharedTemporaryFolder) {
        this.unsharedTemporaryFolder = unsharedTemporaryFolder;
    }
}
