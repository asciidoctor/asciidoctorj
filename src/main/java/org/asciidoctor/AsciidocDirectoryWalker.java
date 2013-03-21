package org.asciidoctor;

import java.io.File;
import java.util.regex.Pattern;



public class AsciidocDirectoryWalker extends DirectoryWalker {

	private static final String ASCIIDOC_REG_EXP_EXTENSION = ".*\\.a((sc(iidoc)?)|d(oc)?)$"; 
	
	private static final Pattern ASCIIDOC_EXTENSION_PATTERN = Pattern.compile(ASCIIDOC_REG_EXP_EXTENSION); 

	public AsciidocDirectoryWalker(String baseDir) {
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