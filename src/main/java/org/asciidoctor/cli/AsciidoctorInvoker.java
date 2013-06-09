package org.asciidoctor.cli;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.internal.RubyHashUtil;
import org.jruby.RubySymbol;

import com.beust.jcommander.JCommander;

public class AsciidoctorInvoker {

	public void invoke(String... parameters) {

		AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
		JCommander jCommander = new JCommander(asciidoctorCliOptions, parameters);

		if (asciidoctorCliOptions.isHelp() || parameters.length == 0) {
			jCommander.setProgramName("asciidoctor");
			jCommander.usage();
		} else {
			
			String inputFile = getInputFile(asciidoctorCliOptions);
			Options options = asciidoctorCliOptions.parse();
			String output = renderInput(options, inputFile);
			
			if(asciidoctorCliOptions.isVerbose()) {
				
				Map<String, Object> optionsMap = options.map();
				Map<String, Object> monitor = RubyHashUtil.convertRubyHashMapToMap((Map<RubySymbol, Object>) optionsMap.get(AsciidoctorCliOptions.MONITOR_OPTION_NAME));
				
				System.out.println(String.format("Time to read and parse source: %05.5f", monitor.get("parse")));
		        System.out.println(String.format("Time to render document: %05.5f", monitor.get("render"))); 
		        System.out.println(String.format("Total time to read, parse and render: %05.5f", monitor.get("load_render")));
		        
			}
			
			if(output != null) {
				System.out.println(output);
			}
		}
	}

	private String renderInput(Options options, String inputFile) {
		Asciidoctor asciidoctor = JRubyAsciidoctor.create();

		//jcommander bug makes this code not working.
//		if("-".equals(inputFile)) {
//			return asciidoctor.render(readInputFromStdIn(), options);
//		}
		
		return asciidoctor.renderFile(new File(inputFile), options);
	}

	private String readInputFromStdIn() {
		 Scanner in = new Scanner(System.in);
		 String content = in.nextLine();
	     in.close();
	     
	     return content;
	}
	
	private String getInputFile(AsciidoctorCliOptions asciidoctorCliOptions) {

		List<String> parameters = asciidoctorCliOptions.getParameters();

		if (parameters.isEmpty()) {
			throw new IllegalArgumentException("asciidoctor: FAILED: input file missing");
		}

		if (parameters.size() > 1) {
			throw new IllegalArgumentException("asciidoctor: FAILED: extra arguments detected (unparsed arguments: "
					+ parameters);
		}

		String inputFile = parameters.get(0);

		if (inputFile.startsWith("-")) {
			throw new IllegalArgumentException("asciidoctor:  FAILED: input file is required instead of an argument.");
		}

		return inputFile;

	}

	public static void main(String args[]) {
		new AsciidoctorInvoker().invoke(args);
	}

}
