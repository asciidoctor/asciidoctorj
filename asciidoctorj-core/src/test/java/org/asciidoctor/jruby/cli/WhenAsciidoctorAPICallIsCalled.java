package org.asciidoctor.jruby.cli;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;

import java.io.File;
import java.util.List;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.jruby.internal.AsciidoctorUtils;
import org.junit.Test;

import com.beust.jcommander.JCommander;

public class WhenAsciidoctorAPICallIsCalled {

    @Test
    public void api_parameters_should_be_transformed_to_cli_command() {

        AttributesBuilder attributesBuilder = AttributesBuilder.attributes()
                .attribute("myAtribute", "myValue").sectionNumbers(true)
                .copyCss(false);

        OptionsBuilder optionsBuilder = OptionsBuilder.options()
                .backend("docbook").templateDirs(new File("a"), new File("b"))
                .safe(SafeMode.UNSAFE).attributes(attributesBuilder.get());

        List<String> command = AsciidoctorUtils.toAsciidoctorCommand(
                optionsBuilder.asMap(), "file.adoc");

        String currentDirectory = new File( "" ).getAbsolutePath() + File.separator;

        String[] parameters = command.subList(1, command.size()).toArray(new String[0]);
        
        AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
        new JCommander(asciidoctorCliOptions,
                parameters);
        
        assertThat(asciidoctorCliOptions.getTemplateDir(), containsInAnyOrder(currentDirectory+"a", currentDirectory+"b"));
        assertThat(asciidoctorCliOptions.getSafeMode(), is(SafeMode.UNSAFE));
        assertThat(asciidoctorCliOptions.getBackend(), is("docbook"));
        assertThat(asciidoctorCliOptions.getParameters(), containsInAnyOrder("file.adoc"));
        
        assertThat(asciidoctorCliOptions.getAttributes(), hasEntry("myAtribute", (Object)"myValue"));
        assertThat(asciidoctorCliOptions.getAttributes(), hasKey("numbered"));
        assertThat(asciidoctorCliOptions.getAttributes(), hasKey("copycss!"));
        
    }

}
