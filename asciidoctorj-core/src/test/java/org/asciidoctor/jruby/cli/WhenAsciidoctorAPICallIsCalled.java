package org.asciidoctor.jruby.cli;

import com.beust.jcommander.JCommander;
import org.asciidoctor.*;
import org.asciidoctor.jruby.internal.AsciidoctorUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;

public class WhenAsciidoctorAPICallIsCalled {

    @Test
    public void api_parameters_should_be_transformed_to_cli_command() {

        Attributes attributes = Attributes.builder()
                .attribute("myAtribute", "myValue")
                .sectionNumbers(true)
                .copyCss(false)
                .build();

        OptionsBuilder optionsBuilder = Options.builder()
                .backend("docbook")
                .templateDirs(new File("a"), new File("b"))
                .safe(SafeMode.UNSAFE)
                .attributes(attributes);

        List<String> command = AsciidoctorUtils.toAsciidoctorCommand(
                optionsBuilder.asMap(), "file.adoc");

        String currentDirectory = new File("").getAbsolutePath() + File.separator;

        String[] parameters = command.subList(1, command.size()).toArray(new String[0]);

        final AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
        final JCommander jCommander = new JCommander(asciidoctorCliOptions);
        jCommander.parse(parameters);

        assertThat(asciidoctorCliOptions.getTemplateDir(), containsInAnyOrder(currentDirectory + "a", currentDirectory + "b"));
        assertThat(asciidoctorCliOptions.getSafeMode(), is(SafeMode.UNSAFE));
        assertThat(asciidoctorCliOptions.getBackend(), is("docbook"));
        assertThat(asciidoctorCliOptions.getParameters(), containsInAnyOrder("file.adoc"));

        assertThat(asciidoctorCliOptions.getAttributes(), hasEntry("myAtribute", "myValue"));
        assertThat(asciidoctorCliOptions.getAttributes(), hasKey("sectnums"));
        assertThat(asciidoctorCliOptions.getAttributes(), hasKey("copycss!"));

    }

}
