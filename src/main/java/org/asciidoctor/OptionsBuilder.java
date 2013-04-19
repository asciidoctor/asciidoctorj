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
	private static final String BACKEND = "backend";
	private static final String DOCTYPE = "doctype";

	private Map<String, Object> options = new HashMap<String, Object>();
	
	private OptionsBuilder() {
		super();
	}
	
	/**
	 * Creates options builder instance.
	 * @return options builder instance.
	 */
	public static OptionsBuilder options() {
		return new OptionsBuilder();
	}
	
	/**
	 * Sets backend option.
	 * @param backend value.
	 * @return this instance.
	 */
	public OptionsBuilder backend(String backend) {
		this.options.put(BACKEND, backend);
		return this;
	}
	
	/**
	 * Sets doctype option.
	 * @param docType value.
	 * @return this instance.
	 */
	public OptionsBuilder docType(String docType) {
		this.options.put(DOCTYPE, docType);
		return this;
	}
	
	/**
	 * Sets in place attribute. 
	 * @param inPlace value.
	 * @return this instance.
	 */
	public OptionsBuilder inPlace(boolean inPlace) {
		this.options.put(IN_PLACE, inPlace);
		return this;
	}
	
	/**
	 * Sets header footer attribute.
	 * @param headerFooter value.
	 * @return this instance.
	 */
	public OptionsBuilder headerFooter(boolean headerFooter) {
		this.options.put(HEADER_FOOTER, headerFooter);
		return this;
	}
	
	/**
	 * Sets template directory.
	 * @param templateDir directory where templates are stored.
	 * @return this instance.
	 */
	public OptionsBuilder templateDir(String templateDir) {
		this.options.put(TEMPLATE_DIR, templateDir);
		return this;
	}
	
	/**
	 * Sets the template engine.
	 * @param templateEngine used to render the document.
	 * @return this instance.
	 */
	public OptionsBuilder templateEngine(String templateEngine) {
		this.options.put(TEMPLATE_ENGINE, templateEngine);
		return this;
	}
	
	/**
	 * Sets attributes used for rendering input.
	 * @param attributes map.
	 * @return this instance.
	 */
	public OptionsBuilder attributes(Map<String, Object> attributes) {
		this.options.put(ATTRIBUTES, attributes);
		return this;
	}
	
	/**
	 * Sets to file value. This is the destination file name.
	 * @param toFile name of output file.
	 * @return this instance.
	 */
	public OptionsBuilder toFile(String toFile) {
		this.options.put(TO_FILE, toFile);
		return this;
	}
	
	/**
	 * Sets to dir value. This is the destination directory.
	 * @param directory where output is generated.
	 * @return this instance.
	 */
	public OptionsBuilder toDir(File directory) {
		this.options.put(TO_DIR, directory.getAbsolutePath());
		return this;
	}
	
	/**
	 * Sets if asciidoctor should create output directory if it does not exist or not. 
	 * @param mkDirs true if directory must be created, false otherwise.
	 * @return this instance.
	 */
	public OptionsBuilder mkDirs(boolean mkDirs) {
		this.options.put(MKDIRS, mkDirs);
		return this;
	}
	
	/**
	 * Sets the safe mode.
	 * @param safeMode to run asciidoctor.
	 * @return this instance.
	 */
	public OptionsBuilder safe(SafeMode safeMode) {
		this.options.put(SAFE, safeMode.getLevel());
		return this;
	}
	
	/**
	 * Sets eruby implementation.
	 * @param eruby implementation.
	 * @return this instance.
	 */
	public OptionsBuilder eruby(String eruby) {
		this.options.put(ERUBY, eruby);
		return this;
	}
	
	/**
	 * Compact the output removing blank lines.
	 * @param compact value.
	 * @return this instance.
	 */
	public OptionsBuilder compact(boolean compact) {
		this.options.put(COMPACT, compact);
		return this;
	}
	
	/**
	 * Destination output directory.
	 * @param destinationDir destination directory. 
	 * @return this instance.
	 */
	public OptionsBuilder destinationDir(File destinationDir) {
		this.options.put(DESTINATION_DIR, destinationDir.getAbsolutePath());
		return this;
	}
	
	/**
	 * Sets a custom or unlisted option.
	 * @param option name.
	 * @param value for given option.
	 * @return this instance.
	 */
	public OptionsBuilder option(String option, Object value) {
		this.options.put(option, value);
		return this;
	}
	
	/**
	 * Gets a map with configured options.
	 * @return map with all options. By default an empty map is returned.
	 */
	public Map<String, Object> asMap() {
		return this.options;
	}
	
}
