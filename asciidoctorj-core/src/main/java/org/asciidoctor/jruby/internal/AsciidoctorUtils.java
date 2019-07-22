package org.asciidoctor.jruby.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.jruby.cli.AsciidoctorCliOptions;

public class AsciidoctorUtils {

    private static final String RUNNER = "asciidoctor";

    private AsciidoctorUtils() {
        super();
    }
    
    public static boolean isOptionWithAttribute(Map<String, Object> options, String attributeName, String attributeValue) {
        
        if(options.containsKey(Options.ATTRIBUTES)) {
            Map<String, Object> attributes = (Map<String, Object>) options.get(Options.ATTRIBUTES);
            
            if(attributes.containsKey(attributeName)) {
                String configuredAttributeValue = (String) attributes.get(attributeName);
                
                if(configuredAttributeValue.equals(attributeValue)) {
                    return true;
                }
                
            }
            
        }
        
        return false;
    }
    
    public static List<String> toAsciidoctorCommand(Map<String, Object> options,
                                                    String inputPath) {

        List<String> command = new ArrayList<>();
        command.add(RUNNER);
        command.addAll(getOptions(options));
        command.add(inputPath);

        return command;

    }

    private static List<String> getOptions(Map<String, Object> options) {

        List<String> optionsAndAttributes = new ArrayList<>();

        if (options.containsKey(Options.DESTINATION_DIR)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.DESTINATION_DIR);
            optionsAndAttributes.add(options.get(Options.DESTINATION_DIR).toString());
        }

        if (options.containsKey(Options.BASEDIR)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.BASE_DIR);
            optionsAndAttributes.add(options.get(Options.BASEDIR).toString());
        }

        if (options.containsKey(Options.TEMPLATE_DIRS)) {
            List<String> templates = (List<String>) options.get(Options.TEMPLATE_DIRS);
            
            for (String template : templates) {
                optionsAndAttributes.add(AsciidoctorCliOptions.TEMPLATE_DIR);
                optionsAndAttributes.add(template);
            }
        }

        if (options.containsKey(Options.TEMPLATE_ENGINE)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.TEMPLATE_ENGINE);
            optionsAndAttributes.add(options.get(Options.TEMPLATE_ENGINE).toString());
        }

        if (options.containsKey(Options.COMPACT)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.COMPACT);
        }

        if (options.containsKey(Options.ERUBY)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.ERUBY);
            optionsAndAttributes.add(options.get(Options.ERUBY).toString());
        }

        if (options.containsKey(Options.HEADER_FOOTER)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.NO_HEADER_FOOTER);
        }

        if (options.containsKey(Options.SAFE)) {
            Integer level = (Integer) options.get(Options.SAFE);
            SafeMode getSafeMode = SafeMode.safeMode(level);
            optionsAndAttributes.add(AsciidoctorCliOptions.SAFE);
            optionsAndAttributes.add(getSafeMode.toString());
        }

        if (options.containsKey(Options.TO_FILE)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.OUTFILE);
            optionsAndAttributes.add(options.get(Options.TO_FILE).toString());
        }

        if (options.containsKey(Options.DOCTYPE)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.DOCTYPE);
            optionsAndAttributes.add(options.get(Options.DOCTYPE).toString());
        }

        if (options.containsKey(Options.BACKEND)) {
            optionsAndAttributes.add(AsciidoctorCliOptions.BACKEND);
            optionsAndAttributes.add(options.get(Options.BACKEND).toString());
        }

        if (options.containsKey(Options.ATTRIBUTES)) {
            optionsAndAttributes.addAll(getAttributesSyntax((Map<String, Object>) options.get(Options.ATTRIBUTES)));
        }

        return optionsAndAttributes;
        
    }
    
    private static List<String> getAttributesSyntax(Map<String, Object> attributes) {
        List<String> attributesOutput = new ArrayList<>();

        Set<Entry<String, Object>> entrySet = attributes.entrySet();

        for (Entry<String, Object> entry : entrySet) {
            attributesOutput.addAll(
                    getAttributeSyntax(entry.getKey(), entry.getValue()));
        }

        return attributesOutput;

    }

    private static List<String> getAttributeSyntax(String attributeName,
            Object attributeValue) {

        List<String> attribute = new ArrayList<>();
        attribute.add("-a");

        StringBuilder argument = new StringBuilder(attributeName);

        if (attributeValue != null && !"".equals(attributeValue.toString().trim())) {
            argument.append("=");
            argument.append(attributeValue.toString());
        }
        
        if(attributeValue == null) {
            argument.append("!");
        }
        attribute.add(argument.toString());

        return attribute;

    }

}
