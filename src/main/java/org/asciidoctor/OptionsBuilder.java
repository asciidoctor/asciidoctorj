package org.asciidoctor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class OptionsBuilder {

	private static final String IN_PLACE = "in_place";
	private static final String ATTRIBUTES = "attributes";
	private static final String HEADER_FOOTER = "header_footer";
	private static final String TEMPLATE_DIR = "template_dir";
	private static final String TEMPLATE_ENGINE = "template_engine";
	private static final String TO_FILE = "to_file";
	private static final String TO_DIR = "to_dir";
	private static final String MKDIRS = "mkdirs";
	private static final String SAFE = "safe";
	private static final String ERUBY = "eruby";
	private static final String COMPACT = "compact";
	private static final String DESTINATION_DIR = "destination_dir";

	private Map<String, Object> options = new HashMap<String, Object>();
	
	private OptionsBuilder() {
		super();
	}
	
	public static OptionsBuilder options() {
		return new OptionsBuilder();
	}
	
	public OptionsBuilder inPlace(boolean inPlace) {
		this.options.put(IN_PLACE, inPlace);
		return this;
	}
	
	public OptionsBuilder headerFooter(boolean headerFooter) {
		this.options.put(HEADER_FOOTER, headerFooter);
		return this;
	}
	
	public OptionsBuilder templateDir(String templateDir) {
		this.options.put(TEMPLATE_DIR, templateDir);
		return this;
	}
	
	public OptionsBuilder templateEngine(String templateEngine) {
		this.options.put(TEMPLATE_ENGINE, templateEngine);
		return this;
	}
	
	public OptionsBuilder attributes(Map<String, Object> attributes) {
		this.options.put(ATTRIBUTES, attributes);
		return this;
	}
	
	public OptionsBuilder toFile(String toFile) {
		this.options.put(TO_FILE, toFile);
		return this;
	}
	
	public OptionsBuilder toDir(File directory) {
		this.options.put(TO_DIR, directory.getAbsolutePath());
		return this;
	}
	
	public OptionsBuilder mkDirs(boolean mkDirs) {
		this.options.put(MKDIRS, mkDirs);
		return this;
	}
	
	public OptionsBuilder safe(SafeMode safeMode) {
		this.options.put(SAFE, safeMode.getLevel());
		return this;
	}
	
	public OptionsBuilder eruby(String eruby) {
		this.options.put(ERUBY, eruby);
		return this;
	}
	
	public OptionsBuilder compact(boolean compact) {
		this.options.put(COMPACT, compact);
		return this;
	}
	
	public OptionsBuilder destinationDir(File destinationDir) {
		this.options.put(DESTINATION_DIR, destinationDir.getAbsolutePath());
		return this;
	}
	
	
	public OptionsBuilder option(String option, Object value) {
		this.options.put(option, value);
		return this;
	}
	
	public Map<String, Object> asMap() {
		return this.options;
	}
	
}
