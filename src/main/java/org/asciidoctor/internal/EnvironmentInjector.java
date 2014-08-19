package org.asciidoctor.internal;

import org.jruby.RubyInstanceConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class EnvironmentInjector {

    private RubyInstanceConfig config;

    EnvironmentInjector(RubyInstanceConfig config) {
        this.config = config;
    }

    void inject(Map<String, Object> environmentVars) {
        if (!environmentVars.isEmpty()) {
            Map<String, Object> replacementEnv = new HashMap<String, Object>(System.getenv());
            for (Map.Entry<String, Object> envVar : environmentVars.entrySet()) {
                String key = envVar.getKey();
                Object val = envVar.getValue();
                if (val == null) {
                    replacementEnv.remove(envVar.getKey());
                    if ("GEM_PATH".equals(key) && !environmentVars.containsKey("GEM_HOME")) {
                        replacementEnv.remove("GEM_HOME");
                    }
                } else {
                    replacementEnv.put(key, val);
                    if ("GEM_PATH".equals(key) && !environmentVars.containsKey("GEM_HOME")) {
                        String gemHome = val.toString().split(File.pathSeparator)[0];
                        replacementEnv.put("GEM_HOME", gemHome);
                    }

                }

            }

            config.setEnvironment(replacementEnv);
        }
    }
}
