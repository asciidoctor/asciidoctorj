package org.asciidoctor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class DirectoryWalker {

private final String baseDir;
	
	public DirectoryWalker(String baseDir) {
		this.baseDir = baseDir;
	}
	
	public List<File> scan() {
		
		File baseDirFile = new File(baseDir);
		
		List<File> includedFiles = walkDirectory(baseDirFile);
		return includedFiles;
	}

	private List<File> walkDirectory(File parentDir) {
		
		final List<File> includedAsciidocFiles = new ArrayList<File>();
		
		final File[] listOfCurrentFiles = parentDir.listFiles();
		
		for (File currentFile : listOfCurrentFiles) {
			if(currentFile.isDirectory()) {
			
				List<File> asciidocFiles = walkDirectory(currentFile);
				includedAsciidocFiles.addAll(asciidocFiles);
			
			} else {
				
				if(isAcceptedFile(currentFile)) {
					includedAsciidocFiles.add(currentFile);
				}
				
			}
		}
		
		return includedAsciidocFiles;
		
	}
	
	protected abstract boolean isAcceptedFile(File filename);
	
}
