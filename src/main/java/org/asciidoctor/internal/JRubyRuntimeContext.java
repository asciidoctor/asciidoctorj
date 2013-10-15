package org.asciidoctor.internal;

import org.jruby.Ruby;

/**
 * This class is intend to provide a way to get current JRuby runtime to be injected inside Extensions.
 * Extension instantiation are managed by Ruby part, so there is no way to inject the JRuby runtime, and it is required by extensions to be able to create for example Blocks.
 * In previous version JRuby runtime was passed as an Asciidoctor attribute, which makes Gradle crash.
 * 
 * This implementation doesn't work if you instantiate more than one instance of Asciidoctor at the same time, but I think it is unlike that this happens.
 * Maybe we can throw an exception if happens?
 * 
 * @author alex
 *
 */
public class JRubyRuntimeContext {

    private ThreadLocal<Ruby> rubyRuntime = new ThreadLocal<Ruby>();
    
    JRubyRuntimeContext(Ruby ruby) {
        rubyRuntime.set(ruby);
    }
    
    public Ruby get() {
        Ruby ruby = rubyRuntime.get();
        
        if(ruby == null) {
            throw new IllegalStateException("Ruby runtime should be set, was not present in current Thread instance.");
        }
        
        return ruby;
        
    }
    
}
