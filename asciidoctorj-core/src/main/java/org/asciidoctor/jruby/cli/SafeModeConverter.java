package org.asciidoctor.jruby.cli;

import org.asciidoctor.api.SafeMode;

import com.beust.jcommander.IStringConverter;

public class SafeModeConverter implements IStringConverter<SafeMode> {

    @Override
    public SafeMode convert(String argument) {
        return SafeMode.valueOf(argument.toUpperCase());
    }

}
