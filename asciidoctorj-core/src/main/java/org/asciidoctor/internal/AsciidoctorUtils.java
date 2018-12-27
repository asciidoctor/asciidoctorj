package org.asciidoctor.internal;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.asciidoctor.api.Options;
import org.asciidoctor.api.SafeMode;
import org.asciidoctor.cli.AsciidoctorCliOptions;

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
    
    public static String toAsciidoctorCommand(Map<String, Object> options,
            String inputPath) {

        String command = RUNNER +" "+ getOptions(options) + inputPath;

        return command;

    }

    private static String getOptions(Map<String, Object> options) {

        StringBuilder optionsAndAttributes = new StringBuilder();

        if (options.containsKey(Options.DESTINATION_DIR)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.DESTINATION_DIR)
                    .append(" ").append(options.get(Options.DESTINATION_DIR))
                    .append(" ");
        }

        if (options.containsKey(Options.BASEDIR)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.BASE_DIR)
            .append(" ").append(options.get(Options.BASEDIR))
            .append(" ");
        }

        if (options.containsKey(Options.TEMPLATE_DIRS)) {
            List<String> templates = (List<String>) options.get(Options.TEMPLATE_DIRS);
            
            for (String template : templates) {
                optionsAndAttributes.append(AsciidoctorCliOptions.TEMPLATE_DIR)
                .append(" ").append(template)
                .append(" ");
            }
        }

        if (options.containsKey(Options.TEMPLATE_ENGINE)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.TEMPLATE_ENGINE)
            .append(" ").append(options.get(Options.TEMPLATE_ENGINE))
            .append(" ");
        }

        if (options.containsKey(Options.COMPACT)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.COMPACT).append(" ");
        }

        if (options.containsKey(Options.ERUBY)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.ERUBY)
            .append(" ").append(options.get(Options.ERUBY))
            .append(" ");
        }

        if (options.containsKey(Options.HEADER_FOOTER)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.NO_HEADER_FOOTER).append(" ");
        }

        if (options.containsKey(Options.SAFE)) {
            Integer level = (Integer) options.get(Options.SAFE);
            SafeMode getSafeMode = SafeMode.safeMode(level);
            optionsAndAttributes.append(AsciidoctorCliOptions.SAFE)
            .append(" ").append(getSafeMode)
            .append(" ");
        }

        if (options.containsKey(Options.TO_FILE)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.OUTFILE)
            .append(" ").append(options.get(Options.TO_FILE))
            .append(" ");
        }

        if (options.containsKey(Options.DOCTYPE)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.DOCTYPE)
            .append(" ").append(options.get(Options.DOCTYPE))
            .append(" ");
        }

        if (options.containsKey(Options.BACKEND)) {
            optionsAndAttributes.append(AsciidoctorCliOptions.BACKEND)
            .append(" ").append(options.get(Options.BACKEND))
            .append(" ");
        }

        if (options.containsKey(Options.ATTRIBUTES)) {
            optionsAndAttributes.append(getAttributesSyntax((Map<String, Object>) options.get(Options.ATTRIBUTES)));
        }

        return optionsAndAttributes.toString();
        
    }
    
    private static String getAttributesSyntax(Map<String, Object> attributes) {
        StringBuilder attributesOutput = new StringBuilder();

        Set<Entry<String, Object>> entrySet = attributes.entrySet();

        for (Entry<String, Object> entry : entrySet) {
            attributesOutput.append(
                    getAttributeSyntax(entry.getKey(), entry.getValue())).append(" ");
        }

        return attributesOutput.toString();

    }

    private static String getAttributeSyntax(String attributeName,
            Object attributeValue) {

        String attribute = "-a " + attributeName;

        if (attributeValue != null && !"".equals(attributeValue.toString().trim())) {
            attribute += "=" + attributeValue.toString();
        }
        
        if(attributeValue == null) {
            attribute += "!";
        }

        return attribute;

    }

}
