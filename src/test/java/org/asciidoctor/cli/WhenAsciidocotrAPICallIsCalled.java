package org.asciidoctor.cli;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.internal.AsciidoctorUtils;
import org.junit.Test;

public class WhenAsciidocotrAPICallIsCalled {

    @Test
    public void api_parameters_should_be_transformed_to_cli_command() {

        AttributesBuilder attributesBuilder = AttributesBuilder.attributes()
                .attribute("myAtribute", "myValue").sectionNumbers(true)
                .copyCss(false);

        OptionsBuilder optionsBuilder = OptionsBuilder.options()
                .backend("docbook").templateDirs(new File("a"), new File("b"))
                .safe(SafeMode.UNSAFE).attributes(attributesBuilder.get());

        String command = AsciidoctorUtils.toAsciidoctorCommand(
                optionsBuilder.asMap(), "file.adoc");

        String currentDirectory = new File( "" ).getAbsolutePath() + File.separator;

        assertThat(
                command,
                is( "asciidoctor -T "
                    + currentDirectory + "a -T "
                    + currentDirectory + "b -S UNSAFE -b docbook -a numbered -a copycss! -a myAtribute=myValue file.adoc" ) );

    }

}
