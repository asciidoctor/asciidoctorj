package org.asciidoctor.jruby.internal;

import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AsciidoctorUtils {

    private static final String RUNNER = "asciidoctor";

    /**
     * Asciidoctor gem CLI options.
     * See class 'org.asciidoctor.jruby.cli.AsciidoctorCliOptions'
     */
    private class CliOptions {
        private static final String DESTINATION_DIR_LONG = "destination_dir";

        private static final String DESTINATION_DIR = "-D";
        private static final String BASE_DIR = "-B";
        private static final String TEMPLATE_DIR = "-T";
        private static final String TEMPLATE_ENGINE = "-E";
        private static final String COMPACT = "-C";
        private static final String ERUBY = "-e";
        private static final String NO_HEADER_FOOTER = "-s";
        private static final String SAFE = "-S";
        private static final String OUTFILE = "-o";
        private static final String DOCTYPE = "-d";
        private static final String BACKEND = "-b";
    }

    private AsciidoctorUtils() {
        super();
    }

    public static boolean isOptionWithAttribute(Map<String, Object> options, String attributeName, String attributeValue) {
        return Optional.ofNullable((Map<String, String>) options.get(Options.ATTRIBUTES))
                .map(attributes -> attributes.get(attributeName))
                .map(value -> value.equals(attributeValue))
                .orElse(Boolean.FALSE);
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

        if (options.containsKey(CliOptions.DESTINATION_DIR_LONG)) {
            optionsAndAttributes.add(CliOptions.DESTINATION_DIR);
            optionsAndAttributes.add(options.get(CliOptions.DESTINATION_DIR_LONG).toString());
        }

        if (options.containsKey(Options.BASEDIR)) {
            optionsAndAttributes.add(CliOptions.BASE_DIR);
            optionsAndAttributes.add(options.get(Options.BASEDIR).toString());
        }

        if (options.containsKey(Options.TEMPLATE_DIRS)) {
            List<String> templates = (List<String>) options.get(Options.TEMPLATE_DIRS);

            for (String template : templates) {
                optionsAndAttributes.add(CliOptions.TEMPLATE_DIR);
                optionsAndAttributes.add(template);
            }
        }

        if (options.containsKey(Options.TEMPLATE_ENGINE)) {
            optionsAndAttributes.add(CliOptions.TEMPLATE_ENGINE);
            optionsAndAttributes.add(options.get(Options.TEMPLATE_ENGINE).toString());
        }

        if (options.containsKey(Options.COMPACT)) {
            optionsAndAttributes.add(CliOptions.COMPACT);
        }

        if (options.containsKey(Options.ERUBY)) {
            optionsAndAttributes.add(CliOptions.ERUBY);
            optionsAndAttributes.add(options.get(Options.ERUBY).toString());
        }

        if (options.containsKey(Options.HEADER_FOOTER)) {
            optionsAndAttributes.add(CliOptions.NO_HEADER_FOOTER);
        }

        if (options.containsKey(Options.SAFE)) {
            Integer level = (Integer) options.get(Options.SAFE);
            SafeMode getSafeMode = SafeMode.safeMode(level);
            optionsAndAttributes.add(CliOptions.SAFE);
            optionsAndAttributes.add(getSafeMode.toString());
        }

        if (options.containsKey(Options.TO_FILE)) {
            optionsAndAttributes.add(CliOptions.OUTFILE);
            optionsAndAttributes.add(options.get(Options.TO_FILE).toString());
        }

        if (options.containsKey(Options.DOCTYPE)) {
            optionsAndAttributes.add(CliOptions.DOCTYPE);
            optionsAndAttributes.add(options.get(Options.DOCTYPE).toString());
        }

        if (options.containsKey(Options.BACKEND)) {
            optionsAndAttributes.add(CliOptions.BACKEND);
            optionsAndAttributes.add(options.get(Options.BACKEND).toString());
        }

        if (options.containsKey(Options.ATTRIBUTES)) {
            optionsAndAttributes.addAll(getAttributesSyntax((Map<String, Object>) options.get(Options.ATTRIBUTES)));
        }

        return optionsAndAttributes;

    }

    private static List<String> getAttributesSyntax(Map<String, Object> attributes) {
        return attributes.entrySet()
                .stream()
                .flatMap(entry -> getAttributeSyntax(entry.getKey(), entry.getValue()).stream())
                .collect(Collectors.toList());
    }

    private static List<String> getAttributeSyntax(String attributeName,
                                                   Object attributeValue) {

        List<String> attribute = new ArrayList<>();
        attribute.add("-a");

        StringBuilder argument = new StringBuilder(attributeName);

        if (attributeValue != null && !"".equals(attributeValue.toString().trim())) {
            argument.append("=");
            argument.append(attributeValue);
        }

        if (attributeValue == null) {
            argument.append("!");
        }
        attribute.add(argument.toString());

        return attribute;

    }

}
