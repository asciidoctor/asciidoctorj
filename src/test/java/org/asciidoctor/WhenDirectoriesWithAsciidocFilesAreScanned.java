package org.asciidoctor;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class WhenDirectoriesWithAsciidocFilesAreScanned {

	@Test
	public void only_asciidoc_files_should_be_returned() {

		DirectoryWalker directoryWalker = new AsciiDocDirectoryWalker("target/test-classes/src");
		List<File> asciidocFiles = directoryWalker.scan();

		assertThat(
				asciidocFiles,
				containsInAnyOrder(new File("target/test-classes/src/documents/sample.ad"), new File(
						"target/test-classes/src/documents/sample.adoc"), new File(
						"target/test-classes/src/documents/sample.asciidoc"), new File(
						"target/test-classes/src/documents/sample.asc")));

	}

}
