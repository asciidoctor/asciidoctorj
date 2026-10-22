package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class AsciiDoctorJClassloaderTestRunnable implements Runnable {

    private boolean loadingsucceeded = false;
    private ClassLoader classloader = null;

    public boolean getLoadingsucceeded() {
        return loadingsucceeded;
    }

    public void setClassloader(ClassLoader newclassloader) {
        classloader = newclassloader;
    }

    public void run() {
        try {
            if (classloader == null) {
                Asciidoctor.Factory.create();
            } else {
                JRubyAsciidoctor.create(classloader);
            }
            loadingsucceeded = true;
        } catch (org.jruby.exceptions.RaiseException exp) {
            loadingsucceeded = false;
        }
    }
}

public class WhenClassloaderIsRequired {

    /*
        Runs the initialisation of Asciidoctor in a different Thread with a different ContextClassLoader
        causing: org.jruby.exceptions.RaiseException: (LoadError) no such file to load -- jruby/java
        just as it does when running Asciidoctor.Factory.create inside sbt, and probably ANT.
        we expect to catch org.jruby.exceptions.RaiseException: (LoadError) no such file to load -- jruby/java
        inside the runnable to verify we have created the same error as happens inside sbt.
     */
    @Test
    @Disabled("Behavior changed in JRuby 9000. It no longer only uses the TCCL by default")
    public void contentsOfJRubyCompleteShouldFailToLoadWithoutPassingClassloader() throws Exception {
        ClassLoader currentclassloader = this.getClass().getClassLoader();
        ClassLoader rootclassloader = currentclassloader.getParent();
        AsciiDoctorJClassloaderTestRunnable runnable = new AsciiDoctorJClassloaderTestRunnable();
        final Thread thread = new Thread(runnable);
        // make the thread use  classloader context  without JRuby and all
        thread.setContextClassLoader(rootclassloader);
        thread.start();
        thread.join();
        assertThat(runnable.getLoadingsucceeded(), is(false));
    }

    @Test
    public void contentsOfJRubyCompleteShouldSucceedWhenPassingTheCorrectClassloader() throws Exception {
        ClassLoader currentclassloader = this.getClass().getClassLoader();
        ClassLoader rootclassloader = currentclassloader.getParent();
        AsciiDoctorJClassloaderTestRunnable runnable = new AsciiDoctorJClassloaderTestRunnable();
        runnable.setClassloader(currentclassloader);
        final Thread thread = new Thread(runnable);
        // make the thread use  classloader context  without JRuby and all
        thread.setContextClassLoader(rootclassloader);
        thread.start();
        thread.join();
        assertThat(runnable.getLoadingsucceeded(), is(true));
    }

}
