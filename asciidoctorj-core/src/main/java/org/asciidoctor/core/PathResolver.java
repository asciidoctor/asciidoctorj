package org.asciidoctor.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all operations for resolving, cleaning and joining paths. This class includes operations for handling both
 * web paths (request URIs) and system paths.
 *
 * <p>The main emphasis of the class is on creating clean and secure paths. Clean paths are void of duplicate parent
 * and current directory references in the path name. Secure paths are paths which are restricted from accessing
 * directories outside of a jail path, if specified.
 *
 * <p>Since joining two paths can result in an insecure path, this class also handles the task of joining a parent
 * (start) and child (target) path.
 *
 * <p>This class makes no use of path utilities from the Java libraries. Instead, it handles all aspects of path
 * manipulation. The main benefit of internalizing these operations is that the class is able to handle both posix and
 * windows paths independent of the operating system on which it runs. This makes the class both deterministic and
 * easier to test.
 *
 * <pre>
 *   resolver = new PathResolver();
 *
 *   // Web Paths
 *
 *   resolver.webPath("images")
 *   => "images"
 *
 *   resolver.webPath("./images")
 *   => "./images"
 *
 *   resolver.webPath("/images")
 *   => "/images"
 *
 *   resolver.webPath("./images/../assets/images")
 *   => "./assets/images"
 *
 *   resolver.webPath("/../images")
 *   => "/images"
 *
 *   resolver.webPath("images", "assets")
 *   => "assets/images"
 *
 *   resolver.webPath("tiger.png", "../assets/images")
 *   => "../assets/images/tiger.png"
 *
 *   // System Paths
 *
 *   resolver.getWorkingDir()
 *   => "/path/to/docs"
 *
 *   resolver.systemPath("images")
 *   => "/path/to/docs/images"
 *
 *   resolver.systemPath("../images")
 *   => "/path/to/images"
 *
 *   resolver.systemPath("/etc/images")
 *   => "/etc/images"
 *
 *   resolver.systemPath("images", "/etc")
 *   => "/etc/images"
 *
 *   resolver.systemPath("", "/etc/images")
 *   => "/etc/images"
 *
 *   resolver.systemPath(null, null, "/path/to/docs")
 *   => "/path/to/docs"
 *
 *   resolver.systemPath("..", null, "/path/to/docs")
 *   => "/path/to/docs"
 *
 *   resolver.systemPath("../../../css", null, "/path/to/docs")
 *   => "/path/to/docs/css"
 *
 *   resolver.systemPath("../../../css", "../../..", "/path/to/docs")
 *   => "/path/to/docs/css"
 *
 *   resolver.systemPath("..", "C:\\data\\docs\\assets", "C:\\data\\docs")
 *   => "C:/data/docs"
 *
 *   resolver.systemPath("..\\..\\css", "C:\\data\\docs\\assets", "C:\\data\\docs")
 *   => "C:/data/docs/css"
 *
 *   try {
 *     resolver.systemPath("../../../css", "../../..", "/path/to/docs", Map.of("recover", false));
 *   } catch (SecurityException e) {
 *     System.out.println(e.getMessage());
 *   }
 *   => "path ../../../../../../css refers to location outside jail: /path/to/docs (disallowed in safe mode)"
 *
 *   resolver.systemPath("/path/to/docs/images", null, "/path/to/docs")
 *   => "/path/to/docs/images"
 *
 *   try {
 *     resolver.systemPath("images", "/etc", "/path/to/docs", Map.of("recover", false));
 *   } catch (SecurityException e) {
 *     System.out.println(e.getMessage());
 *   }
 *   => "start path /etc is outside of jail: /path/to/docs"
 * </pre>
 */
public class PathResolver implements Logging {
    public static final String DOT = ".";
    public static final String DOT_DOT = "..";
    public static final String DOT_SLASH = "./";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String DOUBLE_SLASH = "//";
    private static final Pattern WINDOWS_ROOT_RX = Rx.compile("^(?:[a-zA-Z]:)?[\\\\/]");

    /** A partitioned path: the path segments and the root, which is null unless the path is absolute. */
    public record PartitionedPath(List<String> segments, String root) {
    }

    private String fileSeparator;
    private String workingDir;
    private final Map<String, PartitionedPath> partitionPathSys = new HashMap<>();
    private final Map<String, PartitionedPath> partitionPathWeb = new HashMap<>();

    /**
     * Construct a new instance of PathResolver, optionally specifying the file separator (to override the system
     * default) and the working directory (to override the present working directory). The working directory will be
     * expanded to an absolute path inside the constructor.
     *
     * @param fileSeparator the String file separator to use for path operations (optional, default: the system file
     *            separator)
     * @param workingDir the String working directory (optional, default: the present working directory)
     */
    public PathResolver(String fileSeparator, String workingDir) {
        this.fileSeparator = fileSeparator == null ? java.io.File.separator : fileSeparator;
        if (workingDir == null) {
            this.workingDir = Helpers.workingDirectory();
        } else {
            this.workingDir = isRoot(workingDir) ? posixify(workingDir) : Helpers.expandPath(workingDir, null);
        }
    }

    public PathResolver(String fileSeparator) {
        this(fileSeparator, null);
    }

    public PathResolver() {
        this(null, null);
    }

    public String getFileSeparator() {
        return fileSeparator;
    }

    public void setFileSeparator(String fileSeparator) {
        this.fileSeparator = fileSeparator;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Check whether the specified path is an absolute path.
     *
     * <p>This operation considers both posix paths and Windows paths. The path does not have to be posixified
     * beforehand. This operation does not handle URIs.
     *
     * <p>Unix absolute paths start with a slash. UNC paths can start with a slash or backslash. Windows roots can start
     * with a drive letter.
     *
     * @param path the String path to check
     * @return a Boolean indicating whether the path is an absolute root path
     */
    public boolean isAbsolutePath(String path) {
        return path.startsWith(SLASH) || (fileSeparator.equals(BACKSLASH) && WINDOWS_ROOT_RX.matcher(path).find());
    }

    /**
     * Check if the specified path is an absolute root path.
     *
     * @param path the String path to check
     * @return a Boolean indicating whether the path is an absolute root path
     */
    public boolean isRoot(String path) {
        return isAbsolutePath(path);
    }

    /**
     * Determine if the path is a UNC (root) path.
     *
     * @param path the String path to check
     * @return a Boolean indicating whether the path is a UNC path
     */
    public boolean isUnc(String path) {
        return path.startsWith(DOUBLE_SLASH);
    }

    /**
     * Determine if the path is an absolute (root) web path.
     *
     * @param path the String path to check
     * @return a Boolean indicating whether the path is an absolute (root) web path
     */
    public boolean isWebRoot(String path) {
        return path.startsWith(SLASH);
    }

    /**
     * Determine whether path descends from base.
     *
     * <p>If path equals base, or base is a parent of path, return true.
     *
     * @param path The String path to check. Can be relative.
     * @param base The String base path to check against. Can be relative.
     * @return If path descends from base, return the offset, otherwise -1.
     */
    public int descendsFrom(String path, String base) {
        if (base.equals(path)) {
            return 0;
        } else if (base.equals(SLASH)) {
            return path.startsWith(SLASH) ? 1 : -1;
        } else {
            return path.startsWith(base + SLASH) ? base.length() + 1 : -1;
        }
    }

    /**
     * Calculate the relative path to this absolute path from the specified base directory.
     *
     * <p>If neither path or base are absolute paths, the path is not contained within the base directory, or the
     * relative path cannot be computed, the original path is returned work is done.
     *
     * @param path an absolute filename.
     * @param base an absolute base directory.
     * @return the relative path of the specified path calculated from the base directory.
     */
    public String relativePath(String path, String base) {
        if (isRoot(path)) {
            int offset = descendsFrom(path, base);
            if (offset >= 0) {
                return path.substring(offset);
            }
            try {
                return posixify(Path.of(base).relativize(Path.of(path)).toString());
            } catch (RuntimeException e) {
                return path;
            }
        }
        return path;
    }

    /**
     * Normalize path by converting any backslashes to forward slashes.
     *
     * @param path the String path to normalize
     * @return a String path with any backslashes replaced with forward slashes
     */
    public String posixify(String path) {
        if (path == null) {
            return "";
        }
        return fileSeparator.equals(BACKSLASH) && path.contains(BACKSLASH) ? path.replace('\\', '/') : path;
    }

    /**
     * Expand the specified path by converting the path to a posix path, resolving parent references (..), and removing
     * self references (.).
     *
     * @param path the String path to expand
     * @return a String path as a posix path with parent references resolved and self references removed. The result
     *         will be relative if the path is relative and absolute if the path is absolute.
     */
    public String expandPath(String path) {
        PartitionedPath partitioned = partitionPath(path);
        if (path.contains(DOT_DOT)) {
            List<String> resolvedSegments = new ArrayList<>();
            for (String segment : partitioned.segments()) {
                if (segment.equals(DOT_DOT)) {
                    if (!resolvedSegments.isEmpty()) {
                        resolvedSegments.remove(resolvedSegments.size() - 1);
                    }
                } else {
                    resolvedSegments.add(segment);
                }
            }
            return joinPath(resolvedSegments, partitioned.root());
        }
        return joinPath(partitioned.segments(), partitioned.root());
    }

    /**
     * Partition the path into path segments and remove self references (.) and the trailing slash, if present. Prior
     * to being partitioned, the path is converted to a posix path.
     *
     * <p>Parent references are not resolved by this method since the consumer often needs to handle this resolution in
     * a certain context (checking for the breach of a jail, for instance).
     *
     * @param path the String path to partition
     * @param web a Boolean indicating whether the path should be handled as a web path (optional, default: false)
     * @return the path segments and the path root (e.g., '/', './', 'c:/', or '//'), which is null unless the path is
     *         absolute.
     */
    public PartitionedPath partitionPath(String path, boolean web) {
        Map<String, PartitionedPath> cache = web ? partitionPathWeb : partitionPathSys;
        PartitionedPath result = cache.get(path);
        if (result != null) {
            return result;
        }

        String posixPath = posixify(path);
        String root = null;

        if (web) {
            // ex. /sample/path
            if (isWebRoot(posixPath)) {
                root = SLASH;
            // ex. ./sample/path
            } else if (posixPath.startsWith(DOT_SLASH)) {
                root = DOT_SLASH;
            }
            // otherwise ex. sample/path
        } else if (isRoot(posixPath)) {
            // ex. //sample/path
            if (isUnc(posixPath)) {
                root = DOUBLE_SLASH;
            // ex. /sample/path
            } else if (posixPath.startsWith(SLASH)) {
                root = SLASH;
            // ex. C:/sample/path
            } else {
                root = posixPath.substring(0, posixPath.indexOf(SLASH) + 1);
            }
        // ex. ./sample/path
        } else if (posixPath.startsWith(DOT_SLASH)) {
            root = DOT_SLASH;
        }
        // otherwise ex. sample/path

        List<String> pathSegments = Strings.split(root == null ? posixPath : posixPath.substring(root.length()), SLASH);
        // strip out all dot entries
        pathSegments.removeIf(DOT::equals);
        result = new PartitionedPath(pathSegments, root);
        cache.put(path, result);
        return result;
    }

    public PartitionedPath partitionPath(String path) {
        return partitionPath(path, false);
    }

    /**
     * Join the segments using the posix file separator (since Java knows how to work with paths specified this way,
     * regardless of OS). Use the root, if specified, to construct an absolute path. Otherwise join the segments as a
     * relative path.
     *
     * @param segments a String List of path segments
     * @param root a String path root (optional, default: null)
     * @return a String path formed by joining the segments using the posix file separator and prepending the root, if
     *         specified
     */
    public String joinPath(List<String> segments, String root) {
        return root == null ? String.join(SLASH, segments) : root + String.join(SLASH, segments);
    }

    public String joinPath(List<String> segments) {
        return joinPath(segments, null);
    }

    /**
     * Securely resolve a system path.
     *
     * <p>Resolves the target to an absolute path on the current filesystem. The target is assumed to be relative to
     * the start path, jail path, or working directory (specified in the constructor), in that order. If a jail path is
     * specified, the resolved path is forced to descend from the jail path. If a jail path is not provided, the
     * resolved path may be any location on the system. If the target is an absolute path, use it as is (unless it
     * breaches the jail path). Expands all parent and self references in the resolved path.
     *
     * @param target the String target path
     * @param start the String start path from which to resolve a relative target; falls back to jail, if specified, or
     *            the working directory specified in the constructor (default: null)
     * @param jail the String jail path to which to confine the resolved path, if specified; must be an absolute path
     *            (default: null)
     * @param opts an optional Map of options to control processing (default: {}):
     *            <ul>
     *            <li>recover is used to control whether the processor should automatically recover when an illegal
     *            path is encountered</li>
     *            <li>target_name is used in messages to refer to the path being resolved</li>
     *            </ul>
     * @return an absolute String path relative to the start path, if specified, and confined to the jail path, if
     *         specified. The path is posixified and all parent and self references in the path are expanded.
     * @throws SecurityException if the resolved path is outside of the jail and recover is false
     */
    public String systemPath(String target, String start, String jail, Map<String, Object> opts) {
        if (opts == null) {
            opts = Map.of();
        }
        boolean recover = !Boolean.FALSE.equals(opts.getOrDefault("recover", true));
        Object targetNameOpt = opts.get("target_name");
        String targetName = targetNameOpt == null ? "path" : targetNameOpt.toString();

        if (jail != null) {
            if (!isRoot(jail)) {
                throw new SecurityException("Jail is not an absolute path: " + jail);
            }
            jail = posixify(jail);
        }

        List<String> targetSegments;
        if (target != null) {
            if (isRoot(target)) {
                String targetPath = expandPath(target);
                if (jail != null && descendsFrom(targetPath, jail) < 0) {
                    if (!recover) {
                        throw new SecurityException(targetName + " " + target + " is outside of jail: " + jail
                                + " (disallowed in safe mode)");
                    }
                    getLogger().warn(targetName + " is outside of jail; recovering automatically");
                    List<String> segments = partitionPath(targetPath).segments();
                    PartitionedPath jailPartitioned = partitionPath(jail);
                    List<String> joined = new ArrayList<>(jailPartitioned.segments());
                    joined.addAll(segments);
                    return joinPath(joined, jailPartitioned.root());
                }
                return targetPath;
            }
            targetSegments = partitionPath(target).segments();
        } else {
            targetSegments = List.of();
        }

        if (targetSegments.isEmpty()) {
            if (Strings.isNullOrEmpty(start)) {
                return jail != null ? jail : workingDir;
            } else if (isRoot(start)) {
                if (jail == null) {
                    return expandPath(start);
                }
                start = posixify(start);
            } else {
                targetSegments = partitionPath(start).segments();
                start = jail != null ? jail : workingDir;
            }
        } else if (Strings.isNullOrEmpty(start)) {
            start = jail != null ? jail : workingDir;
        } else if (isRoot(start)) {
            if (jail != null) {
                start = posixify(start);
            }
        } else {
            start = Strings.chomp(jail != null ? jail : workingDir, "/") + "/" + start;
        }

        // both jail and start have been posixified at this point if jail is set
        List<String> startSegments;
        List<String> jailSegments = null;
        String jailRoot;
        boolean recheck = jail != null && descendsFrom(start, jail) < 0;
        if (recheck && fileSeparator.equals(BACKSLASH)) {
            PartitionedPath startPartitioned = partitionPath(start);
            startSegments = startPartitioned.segments();
            PartitionedPath jailPartitioned = partitionPath(jail);
            jailSegments = jailPartitioned.segments();
            jailRoot = jailPartitioned.root();
            if (!java.util.Objects.equals(startPartitioned.root(), jailRoot)) {
                if (!recover) {
                    throw new SecurityException("start path for " + targetName + " " + start
                            + " refers to location outside jail root: " + jail + " (disallowed in safe mode)");
                }
                getLogger().warn("start path for " + targetName + " is outside of jail root; recovering automatically");
                startSegments = jailSegments;
                recheck = false;
            }
        } else {
            PartitionedPath startPartitioned = partitionPath(start);
            startSegments = startPartitioned.segments();
            jailRoot = startPartitioned.root();
        }

        List<String> resolvedSegments = new ArrayList<>(startSegments);
        resolvedSegments.addAll(targetSegments);
        if (resolvedSegments.contains(DOT_DOT)) {
            List<String> unresolvedSegments = resolvedSegments;
            resolvedSegments = new ArrayList<>();
            if (jail != null) {
                if (jailSegments == null) {
                    jailSegments = partitionPath(jail).segments();
                }
                boolean warned = false;
                for (String segment : unresolvedSegments) {
                    if (segment.equals(DOT_DOT)) {
                        if (resolvedSegments.size() > jailSegments.size()) {
                            resolvedSegments.remove(resolvedSegments.size() - 1);
                        } else if (recover) {
                            if (!warned) {
                                getLogger().warn(targetName
                                        + " has illegal reference to ancestor of jail; recovering automatically");
                                warned = true;
                            }
                        } else {
                            throw new SecurityException(targetName + " " + target + " refers to location outside jail: "
                                    + jail + " (disallowed in safe mode)");
                        }
                    } else {
                        resolvedSegments.add(segment);
                    }
                }
            } else {
                for (String segment : unresolvedSegments) {
                    if (segment.equals(DOT_DOT)) {
                        if (!resolvedSegments.isEmpty()) {
                            resolvedSegments.remove(resolvedSegments.size() - 1);
                        }
                    } else {
                        resolvedSegments.add(segment);
                    }
                }
            }
        }

        if (recheck) {
            String targetPath = joinPath(resolvedSegments, jailRoot);
            if (descendsFrom(targetPath, jail) >= 0) {
                return targetPath;
            } else if (recover) {
                getLogger().warn(targetName + " is outside of jail; recovering automatically");
                if (jailSegments == null) {
                    jailSegments = partitionPath(jail).segments();
                }
                List<String> joined = new ArrayList<>(jailSegments);
                joined.addAll(targetSegments);
                return joinPath(joined, jailRoot);
            } else {
                throw new SecurityException(targetName + " " + target + " is outside of jail: " + jail
                        + " (disallowed in safe mode)");
            }
        }
        return joinPath(resolvedSegments, jailRoot);
    }

    public String systemPath(String target, String start, String jail) {
        return systemPath(target, start, jail, Map.of());
    }

    public String systemPath(String target, String start) {
        return systemPath(target, start, null, Map.of());
    }

    public String systemPath(String target) {
        return systemPath(target, null, null, Map.of());
    }

    /**
     * Resolve a web path from the target and start paths. The main function of this operation is to resolve any parent
     * references and remove any self references.
     *
     * <p>The target is assumed to be a path, not a qualified URI. That check should happen before this method is
     * invoked.
     *
     * @param target the String target path
     * @param start the String start (i.e., parent) path
     * @return a String path that joins the target path with the start path with any parent references resolved and self
     *         references removed
     */
    public String webPath(String target, String start) {
        target = posixify(target);
        start = posixify(start);
        String uriPrefix = null;

        if (!(Strings.isNullOrEmpty(start) || isWebRoot(target))) {
            String joined = start + (start.endsWith(SLASH) ? "" : SLASH) + target;
            Matcher uriMatch;
            if (joined.contains(":") && (uriMatch = Rx.match(Rx.URI_SNIFF_RX, joined)) != null) {
                uriPrefix = uriMatch.group();
                target = joined.substring(uriPrefix.length());
            } else {
                target = joined;
            }
        }

        PartitionedPath partitioned = partitionPath(target, true);
        String targetRoot = partitioned.root();
        List<String> resolvedSegments = new ArrayList<>();
        for (String segment : partitioned.segments()) {
            if (segment.equals(DOT_DOT)) {
                if (resolvedSegments.isEmpty()) {
                    if (targetRoot == null || targetRoot.equals(DOT_SLASH)) {
                        resolvedSegments.add(segment);
                    }
                } else if (resolvedSegments.get(resolvedSegments.size() - 1).equals(DOT_DOT)) {
                    resolvedSegments.add(segment);
                } else {
                    resolvedSegments.remove(resolvedSegments.size() - 1);
                }
            } else {
                resolvedSegments.add(segment);
            }
        }

        String resolvedPath = joinPath(resolvedSegments, targetRoot);
        if (resolvedPath.contains(" ")) {
            resolvedPath = resolvedPath.replace(" ", "%20");
        }

        return uriPrefix == null ? resolvedPath : uriPrefix + resolvedPath;
    }

    public String webPath(String target) {
        return webPath(target, null);
    }
}
