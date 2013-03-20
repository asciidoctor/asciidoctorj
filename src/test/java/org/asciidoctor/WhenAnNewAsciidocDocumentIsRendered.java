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

public class WhenAnNewAsciidocDocumentIsRendered {

	@Test
	public void htmlFileShouldBeCreated() {
		
		final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine rubyEngine = engineManager.getEngineByName("jruby");
        final Bindings bindings = new SimpleBindings();

        File sourceDirectory = new File("bin");
        
        bindings.put("srcDir", sourceDirectory.getAbsolutePath());
        System.out.println(sourceDirectory.getAbsolutePath());
        bindings.put("outputDir", sourceDirectory.getAbsolutePath());
        bindings.put("backend", "html");
        
        try {
            final InputStream script = getClass().getClassLoader().getResourceAsStream("execute_asciidoctor.rb");
            final InputStreamReader streamReader = new InputStreamReader(script);
            rubyEngine.eval(streamReader, bindings);
        } catch (ScriptException e) {
           throw new IllegalStateException("Error running ruby script", e);
        }
		
	}

}
