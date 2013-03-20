package org.asciidoctor;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.Test;


public class MyTest {

	@Test
	public void test() {
		
		 final ScriptEngineManager engineManager = new ScriptEngineManager();
	        final ScriptEngine rubyEngine = engineManager.getEngineByName("jruby");
	        final Bindings bindings = new SimpleBindings();

	        File sourceDirectory = new File("target/test-classes/src/asciidoc");
	        File outputDirectory = new File("target/asciidoc-output");
	        
	        bindings.put("srcDir", sourceDirectory.getAbsolutePath());
	        bindings.put("outputDir", outputDirectory.getAbsolutePath());
	        bindings.put("backend", "html");

	        try {
	            final InputStream script = getClass().getClassLoader().getResourceAsStream("execute_asciidoctor.rb");
	            final InputStreamReader streamReader = new InputStreamReader(script);
	            rubyEngine.eval(streamReader, bindings);
	        } catch (ScriptException e) {
	            e.printStackTrace();
	        }
		
	}

}
