package org.asciidoctor.jruby.cli;

import com.beust.jcommander.IStringConverter;
import org.asciidoctor.SafeMode;
import org.asciidoctor.log.Severity;

public class SeverityConverter implements IStringConverter<Severity> {

    @Override
    public Severity convert(String argument) { return Severity.valueOf(argument.toUpperCase()); }

}
