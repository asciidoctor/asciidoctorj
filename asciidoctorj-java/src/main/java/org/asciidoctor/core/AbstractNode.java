package org.asciidoctor.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.asciidoctor.core.converter.Converter;

/**
 * An abstract base class that provides state and methods for managing a node of AsciiDoc content. The state and
 * methods on this class are common to all content segments in an AsciiDoc document.
 */
public abstract class AbstractNode extends Substitutors {
    protected Map<String, Object> attributes;
    protected String context;
    protected Document document;
    protected String id;
    protected String nodeName;
    protected AbstractNode parent;
    // the node of the AsciidoctorJ API that wraps this node (created on demand)
    private Object apiNode;

    protected AbstractNode(AbstractNode parent, String context, Map<String, Object> attributes) {
        // document is a special case, should refer to itself
        if ("document".equals(context)) {
            document = (Document) this;
        } else if (parent != null) {
            this.parent = parent;
            document = parent.getDocument();
        }
        this.context = context;
        nodeName = context;
        // NOTE the value of the attributes option may be null on an Inline node
        this.attributes = attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(attributes);
    }

    protected AbstractNode(AbstractNode parent, String context) {
        this(parent, context, null);
    }

    /**
     * @return the Map of attributes for this node
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @return the String context for this node
     */
    @Override
    public String getContext() {
        return context;
    }

    /**
     * @return the Document to which this node belongs
     */
    @Override
    public Document getDocument() {
        return document;
    }

    /**
     * @return the String id of this node
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the String name of this node
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @return the parent element of this node
     */
    @Override
    public AbstractNode getParent() {
        return parent;
    }

    /**
     * @return the node of the AsciidoctorJ API that wraps this node, or null if it hasn't been created yet
     */
    public Object getApiNode() {
        return apiNode;
    }

    public void setApiNode(Object apiNode) {
        this.apiNode = apiNode;
    }

    /**
     * Associate this node with a new parent node.
     *
     * @param parent The node to set as the parent of this node
     */
    public void setParent(AbstractNode parent) {
        this.parent = parent;
        this.document = parent.getDocument();
    }

    /**
     * @return whether this node is a block
     */
    public abstract boolean isBlock();

    /**
     * @return whether this node is an inline node
     */
    public abstract boolean isInline();

    /**
     * @return the Converter instance being used to convert the current Document.
     */
    public Converter<?> getConverter() {
        return document.getConverter();
    }

    /**
     * Converts this node using the converter of the document.
     *
     * @return the converted String
     */
    public abstract String convert();

    /**
     * Get the value of the specified attribute. If the attribute is not found on this node, fallbackName is set, and
     * this node is not the Document node, get the value of the specified attribute from the Document node.
     *
     * <p>Look for the specified attribute in the attributes on this node and return the value of the attribute, if
     * found. Otherwise, if fallbackName is set and this node is not the Document node, look for that attribute on the
     * Document node and return its value, if found. Otherwise, return the default value (default: null).
     *
     * @param name The String name of the attribute to resolve.
     * @param defaultValue The Object value to return if the attribute is not found (default: null).
     * @param fallbackName The String name of the attribute to resolve on the Document if the attribute is not found on
     *            this node (default: null).
     * @return the Object value (typically a String) of the attribute or defaultValue if the attribute is not found.
     */
    public Object getAttribute(String name, Object defaultValue, String fallbackName) {
        Object value = attributes.get(name);
        if (isTruthy(value)) {
            return value;
        }
        if (fallbackName != null && parent != null) {
            Object fallbackValue = document.getAttributes().get(fallbackName);
            if (isTruthy(fallbackValue)) {
                return fallbackValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get the value of the specified attribute, falling back to the attribute with the same name on the Document if
     * inherit is true.
     */
    public Object getAttribute(String name, Object defaultValue, boolean inherit) {
        return getAttribute(name, defaultValue, inherit ? name : null);
    }

    public Object getAttribute(String name, Object defaultValue) {
        return getAttribute(name, defaultValue, null);
    }

    @Override
    public Object getAttribute(String name) {
        return getAttribute(name, null, null);
    }

    /**
     * Get the value of the specified attribute as a String. See {@link #getAttribute(String, Object, String)}.
     */
    public String getStringAttribute(String name, Object defaultValue, String fallbackName) {
        return Strings.valueOf(getAttribute(name, defaultValue, fallbackName));
    }

    public String getStringAttribute(String name, Object defaultValue) {
        return getStringAttribute(name, defaultValue, null);
    }

    public String getStringAttribute(String name) {
        return getStringAttribute(name, null, null);
    }

    private static boolean isTruthy(Object value) {
        return value != null && !Boolean.FALSE.equals(value);
    }

    /**
     * Check if the specified attribute is defined using the same logic as {@link #getAttribute(String, Object, String)},
     * optionally performing a comparison with the expected value if specified.
     *
     * <p>Look for the specified attribute in the attributes on this node. If not found, fallbackName is specified, and
     * this node is not the Document node, look for that attribute on the Document node. In either case, if the
     * attribute is found, and the comparison value is non-null, return whether the two values match. Otherwise, return
     * whether the attribute was found.
     *
     * @param name The String name of the attribute to resolve.
     * @param expectedValue The expected Object value of the attribute (default: null).
     * @param fallbackName The String name of the attribute to resolve on the Document if the attribute is not found on
     *            this node (default: null).
     * @return a Boolean indicating whether the attribute exists and, if a non-null comparison value is specified,
     *         whether the value of the attribute matches the comparison value.
     */
    public boolean hasAttribute(String name, Object expectedValue, String fallbackName) {
        if (isTruthy(expectedValue)) {
            Object value = attributes.get(name);
            if (!isTruthy(value)) {
                value = fallbackName != null && parent != null ? document.getAttributes().get(fallbackName) : null;
            }
            return valueEquals(expectedValue, value);
        }
        return attributes.containsKey(name)
                || (fallbackName != null && parent != null && document.getAttributes().containsKey(fallbackName));
    }

    private static boolean valueEquals(Object expected, Object actual) {
        if (expected instanceof Number e && actual instanceof Number a) {
            return e.doubleValue() == a.doubleValue();
        }
        return expected.equals(actual);
    }

    /**
     * Check if the specified attribute is defined on this node.
     */
    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Check if the specified attribute is defined on this node, falling back to the attribute with the same name on the
     * Document if inherit is true.
     */
    public boolean hasAttribute(String name, boolean inherit) {
        return hasAttribute(name, null, inherit ? name : null);
    }

    /**
     * Check if the specified attribute is defined on this node and whether its value matches the expected value.
     */
    public boolean isAttribute(String name, Object expectedValue) {
        return hasAttribute(name, expectedValue, null);
    }

    /**
     * Check if the specified attribute is defined on this node (or the Document if inherit is true) and whether its
     * value matches the expected value.
     */
    public boolean isAttribute(String name, Object expectedValue, boolean inherit) {
        return hasAttribute(name, expectedValue, inherit ? name : null);
    }

    /**
     * Assign the value to the attribute name for the current node.
     *
     * @param name The String attribute name to assign
     * @param value The Object value to assign to the attribute (default: '')
     * @param overwrite A Boolean indicating whether to assign the attribute if currently present in the attributes Map
     *            (default: true)
     * @return a Boolean indicating whether the assignment was performed
     */
    public boolean setAttribute(String name, Object value, boolean overwrite) {
        if (!overwrite && attributes.containsKey(name)) {
            return false;
        }
        attributes.put(name, value);
        return true;
    }

    public boolean setAttribute(String name, Object value) {
        return setAttribute(name, value, true);
    }

    public boolean setAttribute(String name) {
        return setAttribute(name, "", true);
    }

    /**
     * Remove the attribute from the current node.
     *
     * @param name The String attribute name to remove
     * @return the previous value, or null if the attribute was not present.
     */
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    /**
     * A convenience method to check if the specified option attribute is enabled on the current node.
     *
     * <p>Check if the option is enabled. This method simply checks to see if the &lt;name&gt;-option attribute is
     * defined on the current node.
     *
     * @param name the String name of the option
     * @return a Boolean indicating whether the option has been specified
     */
    @Override
    public boolean isOption(String name) {
        return isTruthy(attributes.get(name + "-option"));
    }

    /**
     * Set the specified option on this node.
     *
     * <p>This method sets the specified option on this node by setting the &lt;name&gt;-option attribute.
     *
     * @param name the String name of the option
     */
    public void setOption(String name) {
        attributes.put(name + "-option", "");
    }

    /**
     * Retrieve the Set of option names that are enabled on this node.
     *
     * @return a Set of option names
     */
    public Set<String> getEnabledOptions() {
        Set<String> options = new LinkedHashSet<>();
        for (String key : attributes.keySet()) {
            if (key.endsWith("-option")) {
                options.add(key.substring(0, key.length() - 7));
            }
        }
        return options;
    }

    /**
     * Update the attributes of this node with the new values in the attributes argument.
     *
     * <p>If an attribute already exists with the same key, it's value will be overwritten.
     *
     * @param newAttributes A Map of additional attributes to assign to this node.
     * @return the updated attributes Map on this node.
     */
    public Map<String, Object> updateAttributes(Map<String, ?> newAttributes) {
        attributes.putAll(newAttributes);
        return attributes;
    }

    /**
     * Retrieves the space-separated String role for this node.
     *
     * @return the role as a space-separated String.
     */
    public String getRole() {
        return Strings.valueOf(attributes.get("role"));
    }

    /**
     * Retrieves the String role names for this node as a List.
     *
     * @return the role names as a String List, which is empty if the role attribute is absent on this node.
     */
    public List<String> getRoles() {
        Object val = attributes.get("role");
        return val == null ? new ArrayList<>() : Strings.splitOnWhitespace(val.toString());
    }

    /**
     * Checks if the role attribute is set on this node.
     *
     * @return a Boolean indicating whether the role attribute is set on this node.
     */
    public boolean isRole() {
        return attributes.containsKey("role");
    }

    /**
     * Checks if the role attribute is set on this node and whether the space-separated role matches the expected
     * value.
     *
     * @param expectedValue The expected String value of the role (optional, default: null)
     * @return a Boolean indicating whether the role attribute is set on this node and, if an expected value is given,
     *         whether the space-separated role matches that value.
     */
    public boolean isRole(String expectedValue) {
        return expectedValue != null ? expectedValue.equals(attributes.get("role")) : attributes.containsKey("role");
    }

    /**
     * Checks if the specified role is present in the list of roles for this node.
     *
     * @param name The String name of the role to find.
     * @return a Boolean indicating whether this node has the specified role.
     */
    public boolean hasRole(String name) {
        // NOTE center + include? is faster than split + include?
        Object val = attributes.get("role");
        return val != null && (" " + val + " ").contains(" " + name + " ");
    }

    /**
     * Sets the value of the role attribute on this node.
     *
     * @param names A single role name or a space-separated String of role names
     */
    public void setRole(String names) {
        attributes.put("role", names);
    }

    /**
     * Sets the value of the role attribute on this node.
     *
     * @param names A List of role names
     */
    public void setRole(List<String> names) {
        attributes.put("role", String.join(" ", names));
    }

    /**
     * Adds the given role directly to this node.
     *
     * @return a Boolean indicating whether the role was added.
     */
    public boolean addRole(String name) {
        Object val = attributes.get("role");
        if (val != null) {
            // NOTE center + include? is faster than split + include?
            if ((" " + val + " ").contains(" " + name + " ")) {
                return false;
            }
            attributes.put("role", val + " " + name);
            return true;
        }
        attributes.put("role", name);
        return true;
    }

    /**
     * Removes the given role directly from this node.
     *
     * @return a Boolean indicating whether the role was removed.
     */
    public boolean removeRole(String name) {
        Object val = attributes.get("role");
        if (val != null) {
            List<String> roles = Strings.splitOnWhitespace(val.toString());
            if (roles.remove(name)) {
                roles.removeIf(name::equals);
                if (roles.isEmpty()) {
                    attributes.remove("role");
                } else {
                    attributes.put("role", String.join(" ", roles));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * A convenience method that returns the value of the reftext attribute with substitutions applied.
     */
    public String getReftext() {
        Object val = attributes.get("reftext");
        return val != null ? applyReftextSubs(val.toString()) : null;
    }

    /**
     * A convenience method that checks if the reftext attribute is defined.
     */
    public boolean isReftext() {
        return attributes.containsKey("reftext");
    }

    /**
     * Generate cross reference text (xreftext) that can be used to refer to this node.
     *
     * @param xrefstyle An optional String that specifies the style to use to format the xreftext ('full', 'short', or
     *            'basic') (default: null).
     * @return the String xreftext or null if there isn't sufficient information to generate one.
     */
    public String getXreftext(String xrefstyle) {
        return getReftext();
    }

    public String getXreftext() {
        return getXreftext(null);
    }

    /**
     * Construct a reference or data URI to an icon image for the specified icon name.
     *
     * <p>If the 'icon' attribute is set on this block, the name is ignored and the value of this attribute is used as
     * the target image path. Otherwise, construct a target image path by concatenating the value of the 'iconsdir'
     * attribute, the icon name, and the value of the 'icontype' attribute (defaulting to 'png').
     *
     * <p>The target image path is then passed through the {@link #imageUri(String, String)} method. If the 'data-uri'
     * attribute is set on the document, the image will be safely converted to a data URI.
     *
     * <p>The return value of this method can be safely used in an image tag.
     *
     * @param name The String name of the icon
     * @return A String reference or data URI for an icon image
     */
    public String iconUri(String name) {
        String icon;
        if (hasAttribute("icon")) {
            icon = getStringAttribute("icon");
            // QUESTION should we be adding the extension if the icon is an absolute URI?
            if (!Helpers.hasExtname(icon)) {
                icon = icon + "." + document.getStringAttribute("icontype", "png");
            }
        } else {
            icon = name + "." + document.getStringAttribute("icontype", "png");
        }
        return imageUri(icon, "iconsdir");
    }

    /**
     * Construct a URI reference or data URI to the target image.
     *
     * <p>If the target image is a URI reference, then leave it untouched.
     *
     * <p>The target image is resolved relative to the directory retrieved from the specified attribute key, if
     * provided.
     *
     * <p>If the 'data-uri' attribute is set on the document, and the safe mode level is less than SafeMode::SECURE,
     * the image will be safely converted to a data URI by reading it from the same directory. If neither of these
     * conditions are satisfied, a relative path (i.e., URL) will be returned.
     *
     * <p>The return value of this method can be safely used in an image tag.
     *
     * @param targetImage A String path to the target image
     * @param assetDirKey The String attribute key used to lookup the directory where the image is located (default:
     *            'imagesdir')
     * @return A String reference or data URI for the target image
     */
    public String imageUri(String targetImage, String assetDirKey) {
        Document doc = document;
        if (doc.getSafe() < SafeMode.SECURE.getLevel() && doc.hasAttribute("data-uri")) {
            String imagesBase;
            if (Helpers.isUriish(targetImage)) {
                targetImage = Helpers.encodeSpacesInUri(targetImage);
            } else if (assetDirKey != null && (imagesBase = getStringAttribute(assetDirKey, null, assetDirKey)) != null
                    && Helpers.isUriish(imagesBase)) {
                targetImage = normalizeWebPath(targetImage, imagesBase, false);
            } else {
                return generateDataUri(targetImage, assetDirKey);
            }
            return doc.hasAttribute("allow-uri-read")
                    ? generateDataUriFromUri(targetImage, doc.hasAttribute("cache-uri"))
                    : targetImage;
        }
        return normalizeWebPath(targetImage, assetDirKey != null ? getStringAttribute(assetDirKey, null, assetDirKey) : null, true);
    }

    public String imageUri(String targetImage) {
        return imageUri(targetImage, "imagesdir");
    }

    /**
     * Construct a URI reference to the target media.
     *
     * <p>If the target media is a URI reference, then leave it untouched.
     *
     * <p>The target media is resolved relative to the directory retrieved from the specified attribute key, if
     * provided.
     *
     * <p>The return value can be safely used in a media tag (img, audio, video).
     *
     * @param target A String reference to the target media
     * @param assetDirKey The String attribute key used to lookup the directory where the media is located (default:
     *            'imagesdir')
     * @return A String reference for the target media
     */
    public String mediaUri(String target, String assetDirKey) {
        return normalizeWebPath(target, assetDirKey != null ? getStringAttribute(assetDirKey, null, assetDirKey) : null, true);
    }

    public String mediaUri(String target) {
        return mediaUri(target, "imagesdir");
    }

    /**
     * Generate a data URI that can be used to embed an image in the output document.
     *
     * <p>First, and foremost, the target image path is cleaned if the document safe mode level is set to at least
     * SafeMode::SAFE (a condition which is true by default) to prevent access to ancestor paths in the filesystem. The
     * image data is then read and converted to base64. Finally, a data URI is built which can be used in an image tag.
     *
     * @param targetImage A String path to the target image
     * @param assetDirKey The String attribute key used to lookup the directory where the image is located (default:
     *            null)
     * @return A String data URI containing the content of the target image
     */
    public String generateDataUri(String targetImage, String assetDirKey) {
        String ext = Helpers.extname(targetImage, null);
        String mimetype;
        if (ext != null) {
            mimetype = ext.equals(".svg") ? "image/svg+xml" : "image/" + ext.substring(1);
        } else {
            mimetype = "application/octet-stream";
        }

        String imagePath;
        if (assetDirKey != null) {
            imagePath = normalizeSystemPath(targetImage, getStringAttribute(assetDirKey, null, assetDirKey), null,
                    Map.of("target_name", "image"));
        } else {
            imagePath = normalizeSystemPath(targetImage);
        }

        Path path = Helpers.toPath(imagePath);
        if (path != null && Files.isReadable(path) && Files.isRegularFile(path)) {
            try {
                return "data:" + mimetype + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            } catch (IOException e) {
                // fall through to warning
            }
        }
        getLogger().warn("image to embed not found or not readable: " + imagePath);
        return "data:" + mimetype + ";base64,";
        // uncomment to return 1 pixel white dot instead
        //return "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==";
    }

    public String generateDataUri(String targetImage) {
        return generateDataUri(targetImage, null);
    }

    /**
     * Read the image data from the specified URI and generate a data URI.
     *
     * <p>The image data is read from the URI and converted to Base64. A data URI is constructed from the content_type
     * header and Base64 data and returned, which can then be used in an image tag.
     *
     * @param imageUri The URI from which to read the image data. Can be http://, https:// or ftp://
     * @param cacheUri A Boolean to control caching (not currently supported; ignored)
     * @return A data URI string built from Base64 encoded data read from the URI and the mime type specified in the
     *         Content Type header.
     */
    public String generateDataUriFromUri(String imageUri, boolean cacheUri) {
        try {
            URLConnection connection = URI.create(imageUri).toURL().openConnection();
            String mimetype = connection.getContentType();
            byte[] bindata;
            try (InputStream in = connection.getInputStream()) {
                bindata = in.readAllBytes();
            }
            if (mimetype != null && mimetype.contains(";")) {
                mimetype = Strings.strip(mimetype.substring(0, mimetype.indexOf(';')));
            }
            return "data:" + mimetype + ";base64," + Base64.getEncoder().encodeToString(bindata);
        } catch (IOException | RuntimeException e) {
            getLogger().warn("could not retrieve image data from URI: " + imageUri);
            return imageUri;
            // uncomment to return empty data (however, mimetype needs to be resolved)
            //return "data:" + mimetype + ":base64,";
            // uncomment to return 1 pixel white dot instead
            //return "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==";
        }
    }

    /**
     * Normalize the asset file or directory to a concrete and rinsed path.
     *
     * <p>Delegates to normalizeSystemPath, with the start path set to the value of the base_dir instance variable on
     * the Document object.
     */
    public String normalizeAssetPath(String assetRef, String assetName, boolean autocorrect) {
        Map<String, Object> opts = new LinkedHashMap<>();
        opts.put("target_name", assetName);
        opts.put("recover", autocorrect);
        return normalizeSystemPath(assetRef, document.getBaseDir(), null, opts);
    }

    public String normalizeAssetPath(String assetRef) {
        return normalizeAssetPath(assetRef, "path", true);
    }

    /**
     * Resolve and normalize a secure path from the target and start paths using the PathResolver.
     *
     * <p>See {@link PathResolver#systemPath(String, String, String, Map)} for details.
     *
     * <p>The most important functionality in this method is to prevent resolving a path outside of the jail (which
     * defaults to the directory of the source file, stored in the base_dir instance variable on Document) if the
     * document safe level is set to SafeMode::SAFE or greater (a condition which is true by default).
     *
     * @param target the String target path
     * @param start the String start (i.e., parent) path
     * @param jail the String jail path to confine the resolved path
     * @param opts an optional Map of options to control processing (default: {}):
     *            <ul>
     *            <li>recover is used to control whether the processor should automatically recover when an illegal
     *            path is encountered</li>
     *            <li>target_name is used in messages to refer to the path being resolved</li>
     *            </ul>
     * @return the String path resolved from the start and target paths, with any parent references resolved and self
     *         references removed. If a jail is provided, this path will be guaranteed to be contained within the
     *         jail.
     * @throws SecurityException if a jail is specified and the resolved path is outside the jail.
     */
    public String normalizeSystemPath(String target, String start, String jail, Map<String, Object> opts) {
        Document doc = document;
        if (doc.getSafe() < SafeMode.SAFE.getLevel()) {
            if (start != null) {
                if (!doc.getPathResolver().isRoot(start)) {
                    start = Helpers.joinPath(doc.getBaseDir(), start);
                }
            } else {
                start = doc.getBaseDir();
            }
        } else {
            if (start == null) {
                start = doc.getBaseDir();
            }
            if (jail == null) {
                jail = doc.getBaseDir();
            }
        }
        return doc.getPathResolver().systemPath(target, start, jail, opts);
    }

    public String normalizeSystemPath(String target, String start) {
        return normalizeSystemPath(target, start, null, Map.of());
    }

    public String normalizeSystemPath(String target) {
        return normalizeSystemPath(target, null, null, Map.of());
    }

    /**
     * Normalize the web path using the PathResolver.
     *
     * <p>See {@link PathResolver#webPath(String, String)} for details about path resolution and encoding.
     *
     * @param target the String target path
     * @param start the String start (i.e, parent) path (optional, default: null)
     * @param preserveUriTarget a Boolean indicating whether target should be preserved if contains a URI (default:
     *            true)
     * @return the resolved String path
     */
    public String normalizeWebPath(String target, String start, boolean preserveUriTarget) {
        if (preserveUriTarget && Helpers.isUriish(target)) {
            return Helpers.encodeSpacesInUri(target);
        }
        return document.getPathResolver().webPath(target, start);
    }

    public String normalizeWebPath(String target, String start) {
        return normalizeWebPath(target, start, true);
    }

    public String normalizeWebPath(String target) {
        return normalizeWebPath(target, null, true);
    }

    /**
     * Read the contents of the file at the specified path. This method assumes that the path is safe to read. It
     * checks that the file is readable before attempting to read it.
     *
     * @param path the String path from which to read the contents
     * @param opts a Map of options to control processing (default: {})
     *            <ul>
     *            <li>warn_on_failure a Boolean that controls whether a warning is issued if the file cannot be read
     *            (default: false)</li>
     *            <li>normalize a Boolean that controls whether the lines are normalized and coerced to UTF-8 (default:
     *            false)</li>
     *            <li>label the String label of the file used in the warning message (default: file)</li>
     *            </ul>
     * @return the String content of the file at the specified path, or null if the file does not exist.
     */
    public String readAsset(String path, Map<String, Object> opts) {
        if (opts == null) {
            opts = Map.of();
        }
        Path file = Helpers.toPath(path);
        if (file != null && Files.isReadable(file) && Files.isRegularFile(file)) {
            try {
                String content = Helpers.decode(Files.readAllBytes(file), StandardCharsets.UTF_8);
                // QUESTION should we chomp content if normalize is false?
                return Boolean.TRUE.equals(opts.get("normalize"))
                        ? String.join(Asciidoctor.LF, Helpers.prepareSourceString(content)) : content;
            } catch (IOException e) {
                // fall through
            }
        }
        if (Boolean.TRUE.equals(opts.get("warn_on_failure"))) {
            Object docfile = document.getAttribute("docfile");
            Object label = opts.get("label");
            getLogger().warn((docfile == null ? "<stdin>" : docfile) + ": " + (label == null ? "file" : label)
                    + " does not exist or cannot be read: " + path);
        }
        return null;
    }

    public String readAsset(String path, boolean warnOnFailure) {
        return readAsset(path, Map.of("warn_on_failure", warnOnFailure));
    }

    public String readAsset(String path) {
        return readAsset(path, Map.of());
    }

    /**
     * Resolve the URI or system path to the specified target, then read and return its contents.
     *
     * <p>The URI or system path of the target is first resolved. If the resolved path is a URI, read the contents from
     * the URI if the allow-uri-read attribute is set. If the resolved path is not a URI, read the contents of the file
     * from the file system. If the normalize option is set, the data will be normalized.
     *
     * @param target The URI or local path from which to read the data.
     * @param opts a Map of options to control processing (default: {})
     *            <ul>
     *            <li>label the String label of the target to use in warning messages (default: 'asset')</li>
     *            <li>normalize a Boolean that indicates whether the data should be normalized (default: false)</li>
     *            <li>start the String relative base path to use when resolving the target (default: null)</li>
     *            <li>warn_on_failure a Boolean that indicates whether warnings are issued if the target cannot be read
     *            (default: true)</li>
     *            <li>warn_if_empty a Boolean that indicates whether a warning is issued if contents of target is empty
     *            (default: false)</li>
     *            </ul>
     * @return the contents of the resolved target or null if the resolved target cannot be read
     */
    public String readContents(String target, Map<String, Object> opts) {
        if (opts == null) {
            opts = Map.of();
        }
        Document doc = document;
        String contents = null;
        Object labelOpt = opts.get("label");
        String label = labelOpt == null ? "asset" : labelOpt.toString();
        boolean warnOnFailure = !Boolean.FALSE.equals(opts.getOrDefault("warn_on_failure", true));
        String start = Strings.valueOf(opts.get("start"));
        boolean uri = Helpers.isUriish(target);
        if (!uri && start != null && Helpers.isUriish(start)) {
            target = doc.getPathResolver().webPath(target, start);
            uri = true;
        }
        if (uri) {
            if (doc.hasAttribute("allow-uri-read")) {
                try (InputStream in = URI.create(target).toURL().openStream()) {
                    String data = Helpers.decode(in.readAllBytes(), StandardCharsets.UTF_8);
                    contents = Boolean.TRUE.equals(opts.get("normalize"))
                            ? String.join(Asciidoctor.LF, Helpers.prepareSourceString(data)) : data;
                } catch (IOException | RuntimeException e) {
                    if (warnOnFailure) {
                        getLogger().warn("could not retrieve contents of " + label + " at URI: " + target);
                    }
                }
            } else if (warnOnFailure) {
                getLogger().warn("cannot retrieve contents of " + label + " at URI: " + target
                        + " (allow-uri-read attribute not enabled)");
            }
        } else {
            target = normalizeSystemPath(target, start, null, Map.of("target_name", label));
            Map<String, Object> readOpts = new LinkedHashMap<>();
            readOpts.put("normalize", opts.get("normalize"));
            readOpts.put("warn_on_failure", warnOnFailure);
            readOpts.put("label", opts.get("label"));
            contents = readAsset(target, readOpts);
        }
        if (contents != null && Boolean.TRUE.equals(opts.get("warn_if_empty")) && contents.isEmpty()) {
            getLogger().warn("contents of " + label + " is empty: " + target);
        }
        return contents;
    }

    public String readContents(String target) {
        return readContents(target, Map.of());
    }

    /**
     * Check whether the specified String is a URI by matching it against the {@link Rx#URI_SNIFF_RX} regex.
     *
     * @deprecated Use {@link Helpers#isUriish(String)} instead
     */
    @Deprecated
    public boolean isUri(String str) {
        return Helpers.isUriish(str);
    }
}
