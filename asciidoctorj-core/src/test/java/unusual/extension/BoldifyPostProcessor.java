package unusual.extension;

import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.Postprocessor;

/**
 * This processor is used only for checking we are able to load extensions from "unusual packages".
 *
 * @see org.asciidoctor.internal.WhenLoadingExtensionFromUnusualPackage
 * @see <a href="https://github.com/asciidoctor/asciidoctorj/issues/250">Issue #250</a>
 */
public class BoldifyPostProcessor extends Postprocessor {
  @Override public String process(DocumentRuby document, String output) {
    if (document.isBasebackend("html")) {
      return output.replaceAll("bold", "<b>bold</b>");
    } else {
      return output;
    }
  }
}
