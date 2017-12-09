package org.asciidoctor.converter;

import org.asciidoctor.ast.ContentNode;

import java.util.Map;

public interface Converter<T> {

    /**
     * Converts an {@link ContentNode} using the specified transform along
     * with additional options. If a transform is not specified, implementations
     * typically derive one from the {@link ContentNode#getNodeName()} property.
     *
     * <p>Implementations are free to decide how to carry out the conversion. In
     * the case of the built-in converters, the tranform value is used to
     * dispatch to a handler method. The TemplateConverter uses the value of
     * the transform to select a template to render.
     *
     * @param node The concrete instance of FlowNode to convert
     * @param transform An optional String transform that hints at which transformation
     *             should be applied to this node. If a transform is not specified,
     *             the transform is typically derived from the value of the
     *             node's node_name property. (optional, default: null)
     * @param opts An optional map of options that provide additional hints about
     *             how to convert the node. (optional, default: empty map)
     * @return the converted result
     */
    T convert(ContentNode node, String transform, Map<Object, Object> opts);

    Map<String, Object> getOptions();

    /**
     * To change the extension of the generated file invoke this method in the constructor of the converter implementation.
     * The default extension is {@code .html}.
     * @param outfilesuffix The file extension for the generated file, e.g. {@code .txt}
     */
    void setOutfileSuffix(String outfilesuffix);

    /**
     * @return The file extension of the generated files.
     */
    String getOutfileSuffix();
}
