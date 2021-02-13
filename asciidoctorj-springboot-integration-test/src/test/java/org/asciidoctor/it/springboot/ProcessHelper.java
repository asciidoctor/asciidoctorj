package org.asciidoctor.it.springboot;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProcessHelper {

    static ProcessOutput run(Map<String, String> env, String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(new File("springboot-app"));

        processBuilder.environment().clear();
        String javaHome = System.getProperty("java.home");
        processBuilder.environment().put("JAVA_HOME", javaHome);
        processBuilder.environment().put("PATH", javaHome + "/bin");
        processBuilder.environment().putAll(env);

        Process process = processBuilder.start();

        final InputStream is = process.getInputStream();
        final InputStream es = process.getErrorStream();
        boolean status = process.waitFor(10l, TimeUnit.SECONDS);
        if (!status) {
            byte[] buffer = new byte[1800];
            IOUtils.read(is, buffer);
            return new ProcessOutput(process, new String(buffer), new String());
        }

        return new ProcessOutput(IOUtils.toString(es), IOUtils.toString(is));
    }


    static class ProcessOutput {
        final Process process;
        final String out, err;

        public ProcessOutput(String sto, String err) {
            this(null, sto, err);
        }

        public ProcessOutput(Process process, String sto, String err) {
            this.process = process;
            this.out = sto;
            this.err = err;
        }
    }
}
