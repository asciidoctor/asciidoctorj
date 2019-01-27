package org.asciidoctor.ruby.cli;

import org.asciidoctor.SafeMode;

import com.beust.jcommander.IStringConverter;

public class SafeModeConverter implements IStringConverter<SafeMode> {

    @Override
    public SafeMode convert(String argument) {
        return SafeMode.valueOf(argument.toUpperCase());
    }

}
