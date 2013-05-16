package org.asciidoctor.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jruby.Ruby;

class EnvironmentInjector {

	private Ruby runtime;
	
	EnvironmentInjector(Ruby runtime) {
		this.runtime = runtime;
	}
	
	void inject(Map<String, Object> environmentVars) {
		
		Set<Entry<String, Object>> environmentVariablesAndValues = environmentVars.entrySet();
		
		for (Entry<String, Object> entry : environmentVariablesAndValues) {
			runtime.evalScriptlet(String.format("ENV['%s']=\"%s\"", entry.getKey(), entry.getValue()));
		}
		
	}
	
}
