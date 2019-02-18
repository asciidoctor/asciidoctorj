package unusual.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.jruby.internal.WhenLoadingExtensionFromUnusualPackage;

/**
 * This processor is used only for checking we are able to load extensions from "unusual packages".
 *
 * @see WhenLoadingExtensionFromUnusualPackage
 * @see <a href="https://github.com/asciidoctor/asciidoctorj/issues/250">Issue #250</a>
 */
public class BoldifyPostProcessor extends Postprocessor {
  @Override public String process(Document document, String output) {
    if (document.isBasebackend("html")) {
      return output.replaceAll("bold", "<b>bold</b>");
    } else {
      return output;
    }
  }
}
