package org.asciidoctor.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import com.beust.jcommander.Parameter;

public class AsciidoctorCliOptions {

	private static final char ATTRIBUTE_SEPARATOR = '=';
	public static final String MONITOR_OPTION_NAME = "monitor";	
	
	@Parameter(names = { "-v", "--verbose" }, description = "enable verbose mode (default: false)")
	private boolean verbose = false;
	
	@Parameter(names = {"-b", "--backend"}, description = "set output format backend (default: html5)")
	private String backend = "html5";
	
	@Parameter(names = {"-d", "--doctype"},  description = "document type to use when rendering output: [article, book, inline] (default: article)")
	private String doctype = "article";
	
	@Parameter(names = {"-o", "--out-file"}, description = "output file (default: based on input file path); use - to output to STDOUT")
	private String outFile;
	
	@Parameter(names = {"--safe"}, description = "set safe mode level to safe (default: unsafe)")
	private boolean safe = false;
	
	@Parameter(names = {"-S", "--safe-mode"}, converter = SafeModeConverter.class, description = "set safe mode level explicitly: [unsafe, safe, server, secure] (default: unsafe)")
	private SafeMode safeMode = SafeMode.UNSAFE;
	
	@Parameter(names = {"-s", "--no-header-footer"}, description = "suppress output of header and footer (default: false)")
	private boolean noHeaderFooter = false;
	
	@Parameter(names = {"-n", "--section-numbers"}, description = "auto-number section titles in the HTML backend; disabled by default")
	private boolean sectionNumbers = false;
	
	@Parameter(names = {"-e", "--eruby"}, description = "specify eRuby implementation to render built-in templates: [erb, erubis] (default: erb)")
	private String eruby = "erb";
	
	@Parameter(names = {"-C", "--compact"}, description = "compact the output by removing blank lines (default: false)")
	private boolean compact = false;
	
	@Parameter(names = {"-E","--template-engine"}, description = "template engine to use for the custom render templates (loads gem on demand)")
	private String templateEngine;
	
	@Parameter(names = {"-T", "--template-dir"}, description = "directory containing custom render templates the override the built-in set")
	private List<String> templateDir;
	
	@Parameter(names = {"-B", "--base-dir"}, description = "base directory containing the document and resources (default: directory of source file)")
	private String baseDir;
	
	@Parameter(names = {"-D", "--destination-dir"}, description = "destination output directory (default: directory of source file)")
	private String destinationDir;
	
	@Parameter(names = {"--trace"}, description = "include backtrace information on errors (default: false)")
	private boolean trace = false;
	
	@Parameter(names = {"-h", "--help"}, help = true, description = "show this message")
	private boolean help = false;
	
	@Parameter(names = {"-a", "--attribute"}, description = "a list of attributes, in the form key or key=value pair, to set on the document")
	private List<String> attributes = new ArrayList<String>();
	
	@Parameter(description = "input files")
	private List<String> parameters = new ArrayList<String>();

	public List<String> getParameters() {
		return parameters;
	}
	
	public boolean isVerbose() {
		return verbose;
	}

	public String getBackend() {
		return backend;
	}

	public String getDoctype() {
		return doctype;
	}

	public String getOutFile() {
		return outFile;
	}
	
	public boolean isOutFileOption() {
		return outFile != null;
	}

	public boolean isSafe() {
		return safe;
	}

	public SafeMode getSafeMode() {
		return safeMode;
	}

	public boolean isNoHeaderFooter() {
		return noHeaderFooter;
	}

	public boolean isSectionNumbers() {
		return sectionNumbers;
	}

	public String getEruby() {
		return eruby;
	}

	public boolean isCompact() {
		return compact;
	}

	public List<String> getTemplateDir() {
		return templateDir;
	}

	public boolean isTemplateDirOption() {
		return templateDir != null;
	}
	
	public String getBaseDir() {
		return baseDir;
	}

	public boolean isBaseDirOption() {
		return baseDir != null;
	}
	
	public String getDestinationDir() {
		return destinationDir;
	}

	public boolean isDestinationDirOption() {
		return destinationDir != null;
	}
	
	public boolean isTemplateEngineOption() {
	    return templateEngine != null;
	}
	
	public boolean isTrace() {
		return trace;
	}

	public boolean isHelp() {
		return help;
	}

	private boolean isOutputStdout() {
		return "-".equals(getOutFile());
	}
	
	private boolean isInPlaceRequired() {
		return !isOutFileOption() && !isDestinationDirOption() && !isOutputStdout();
	}
	
	public Options parse() {
		OptionsBuilder optionsBuilder = OptionsBuilder.options();
		AttributesBuilder attributesBuilder = AttributesBuilder.attributes();
		
		optionsBuilder.backend(this.backend).safe(safeMode).docType(doctype).eruby(eruby);
		
		if(isOutFileOption() && !isOutputStdout()) {
			optionsBuilder.toFile(new File(outFile));
		}
		
		if(this.safe) {
			optionsBuilder.safe(SafeMode.SAFE);
		}
		
		if(this.noHeaderFooter) {
			optionsBuilder.headerFooter(false);
		}
		
		if(this.sectionNumbers) {
			attributesBuilder.sectionNumbers(this.sectionNumbers);
		}
		
		if(this.compact) {
			optionsBuilder.compact(this.compact);
		}
		
		if(isBaseDirOption()) {
			optionsBuilder.baseDir(new File(this.baseDir));
		}
		
		if(isTemplateEngineOption()) {
		    optionsBuilder.templateEngine(this.templateEngine);
		}
		
		if(isTemplateDirOption()) {
		    for (String templateDir : this.templateDir) {
		        optionsBuilder.templateDir(new File(templateDir));                
            }
		}
		
		if(isDestinationDirOption() && !isOutputStdout()) {
			optionsBuilder.toDir(new File(this.destinationDir));
		}
		
		if(isInPlaceRequired()) {
			optionsBuilder.inPlace(true);
		}
		
		if(this.verbose) {
			optionsBuilder.option(MONITOR_OPTION_NAME, new HashMap<Object, Object>());
		}
		
		attributesBuilder.attributes(getAttributes());
		optionsBuilder.attributes(attributesBuilder.get());
		return optionsBuilder.get();
		
	}
	
	private Map<String, Object> getAttributes() {
		
		Map<String, Object> attributeValues = new HashMap<String, Object>();
		
		for (String attribute : this.attributes) {
			int equalsIndex = -1;
			if((equalsIndex = attribute.indexOf(ATTRIBUTE_SEPARATOR)) > -1) {
				extractAttributeNameAndValue(attributeValues, attribute, equalsIndex);
			} else {
				attributeValues.put(attribute, "");
			}
		}
		
		return attributeValues;
	}

	private void extractAttributeNameAndValue(Map<String, Object> attributeValues, String attribute, int equalsIndex) {
		String attributeName = attribute.substring(0, equalsIndex);
		String attributeValue = attribute.substring(equalsIndex+1, attribute.length());
		
		attributeValues.put(attributeName, attributeValue);
	}
}
