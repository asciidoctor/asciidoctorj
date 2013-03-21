package org.asciidoctor;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class WhenDirectoriesWithAsciidocFilesAreScanned {

	@Test
	public void only_asciidoc_files_should_be_returned() {

		DirectoryWalker directoryWalker = new AsciidocDirectoryWalker("build/resources/test/src");
		List<File> asciidocFiles = directoryWalker.scan();

		assertThat(
				asciidocFiles,
				containsInAnyOrder(new File("build/resources/test/src/documents/sample.ad"), new File(
						"build/resources/test/src/documents/sample.adoc"), new File(
						"build/resources/test/src/documents/sample.asciidoc"), new File(
						"build/resources/test/src/documents/sample.asc")));

	}

}
