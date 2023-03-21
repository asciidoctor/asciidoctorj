package org.asciidoctor.jruby.cli;

import com.beust.jcommander.converters.EnumConverter;
import org.asciidoctor.log.Severity;

public class SeverityConverter extends EnumConverter<Severity> {

    public SeverityConverter() {
        super("--failure-level", Severity.class);
    }

    @Override
    public Severity convert(String argument) {
        return super.convert(argument.toUpperCase());
    }

}
