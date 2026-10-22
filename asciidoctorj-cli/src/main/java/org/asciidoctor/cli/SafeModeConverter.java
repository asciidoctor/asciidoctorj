package org.asciidoctor.cli;

import com.beust.jcommander.IStringConverter;
import org.asciidoctor.SafeMode;

public class SafeModeConverter implements IStringConverter<SafeMode> {

    @Override
    public SafeMode convert(String argument) {
        return SafeMode.valueOf(argument.toUpperCase());
    }

}
