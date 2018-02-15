package unusual.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;

/**
 * This processor is used only for checking we are able to load extensions from "unusual packages".
 *
 * @see org.asciidoctor.internal.WhenLoadingExtensionFromUnusualPackage
 * @see <a href="https://github.com/asciidoctor/asciidoctorj/issues/250">Issue #250</a>
 */
public class UnusualTreeProcessor extends Treeprocessor {

  @Override
  public Document process(Document document) {
    return document;
  }
}
