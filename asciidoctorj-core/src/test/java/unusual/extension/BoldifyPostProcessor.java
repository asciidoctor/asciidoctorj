package unusual.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;

/**
 * This processor is used only for checking we are able to load extensions from "unusual packages".
 *
 * @see org.asciidoctor.internal.WhenLoadingExtensionFromUnusualPackage
 * @see <a href="https://github.com/asciidoctor/asciidoctorj/issues/250">Issue #250</a>
 */
public class BoldifyPostProcessor extends Postprocessor {
  @Override public String process(Document document, String output) {
    if (document.basebackend("html")) {
      return output.replaceAll("bold", "<b>bold</b>");
    } else {
      return output;
    }
  }
}
