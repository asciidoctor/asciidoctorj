package org.asciidoctor.cli;

import com.beust.jcommander.JCommander;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenAsciidoctorAPICallIsCalled {

    @Test
    public void api_parameters_should_be_transformed_to_cli_command() {
        final String currentDirectory = new File("").getAbsolutePath() + File.separator;
        var parameters = new String[]{
                "-T", Path.of(currentDirectory, "a").toAbsolutePath().toString(),
                "-T", Path.of(currentDirectory, "b").toAbsolutePath().toString(),
                "-S", "UNSAFE",
                "-b", "docbook",
                "-a", "myAttribute=myValue",
                "-a", "sectnums",
                "-a", "copycss!",
                "file.adoc"
        };

        final AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
        final JCommander jCommander = new JCommander(asciidoctorCliOptions);
        jCommander.parse(parameters);

        assertThat(asciidoctorCliOptions.getTemplateDir()).containsExactlyInAnyOrder(currentDirectory + "a", currentDirectory + "b");
        assertThat(asciidoctorCliOptions.getSafeMode()).isEqualTo(SafeMode.UNSAFE);
        assertThat(asciidoctorCliOptions.getBackend()).isEqualTo("docbook");
        assertThat(asciidoctorCliOptions.getParameters()).containsExactly("file.adoc");
        assertThat(asciidoctorCliOptions.getAttributes())
                .containsEntry("myAttribute", "myValue")
                .containsEntry("sectnums", "")
                .containsEntry("copycss!", "");
    }
}
