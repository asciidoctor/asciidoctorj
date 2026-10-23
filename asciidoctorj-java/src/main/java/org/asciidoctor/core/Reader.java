package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.LF;
import static org.asciidoctor.core.Asciidoctor.LIST_CONTINUATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Methods for retrieving lines from AsciiDoc source files.
 */
public class Reader implements Logging {

    /**
     * A location in the source (file, directory, path and line number).
     */
    public static class Cursor {
        private final String file;
        private final String dir;
        private final String path;
        private int lineno;

        public Cursor(String file, String dir, String path, int lineno) {
            this.file = file;
            this.dir = dir;
            this.path = path;
            this.lineno = lineno;
        }

        public Cursor(String file, String dir, String path) {
            this(file, dir, path, 1);
        }

        public Cursor(String file, String dir) {
            this(file, dir, null, 1);
        }

        public Cursor(String file) {
            this(file, null, null, 1);
        }

        public String getFile() {
            return file;
        }

        public String getDir() {
            return dir;
        }

        public String getPath() {
            return path;
        }

        public int getLineNumber() {
            return lineno;
        }

        public void advance(int num) {
            lineno += num;
        }

        public String getLineInfo() {
            return path + ": line " + lineno;
        }

        /**
         * @return a copy of this cursor
         */
        public Cursor copy() {
            return new Cursor(file, dir, path, lineno);
        }

        @Override
        public String toString() {
            return getLineInfo();
        }
    }

    /**
     * Options that control how {@link #readLinesUntil(ReadOptions, Predicate)} reads lines.
     */
    public static final class ReadOptions {
        private String terminator;
        private boolean breakOnBlankLines;
        private boolean breakOnListContinuation;
        private boolean skipFirstLine;
        private boolean preserveLastLine;
        private boolean readLastLine;
        private boolean skipLineComments;
        private boolean skipProcessing;
        private boolean contextSet;
        private String context;
        private Cursor cursor;
        private boolean cursorAtMark;

        public static ReadOptions create() {
            return new ReadOptions();
        }

        /** Specifies the contents of the line at which the reader should stop. */
        public ReadOptions terminator(String value) {
            this.terminator = value;
            return this;
        }

        /** Specifies to break on blank lines. */
        public ReadOptions breakOnBlankLines() {
            return breakOnBlankLines(true);
        }

        public ReadOptions breakOnBlankLines(boolean value) {
            this.breakOnBlankLines = value;
            return this;
        }

        /** Specifies to break on a list continuation line. */
        public ReadOptions breakOnListContinuation() {
            this.breakOnListContinuation = true;
            return this;
        }

        /** Tells the reader to advance beyond the first line before beginning the scan. */
        public ReadOptions skipFirstLine() {
            this.skipFirstLine = true;
            return this;
        }

        /** Specifies that the line causing the method to stop processing lines should be pushed back onto the stack. */
        public ReadOptions preserveLastLine() {
            this.preserveLastLine = true;
            return this;
        }

        /** Specifies that the line causing the method to stop processing lines should be included in the result. */
        public ReadOptions readLastLine() {
            this.readLastLine = true;
            return this;
        }

        /** Specifies to look for and skip line comments. */
        public ReadOptions skipLineComments() {
            return skipLineComments(true);
        }

        public ReadOptions skipLineComments(boolean value) {
            this.skipLineComments = value;
            return this;
        }

        /** Disables line (pre)processing for the duration of the read. */
        public ReadOptions skipProcessing() {
            return skipProcessing(true);
        }

        public ReadOptions skipProcessing(boolean value) {
            this.skipProcessing = value;
            return this;
        }

        /**
         * Specifies the name of the block context used in the warning when the terminator is not found. A null value
         * disables the warning. If not specified, the terminator is used.
         */
        public ReadOptions context(String value) {
            this.contextSet = true;
            this.context = value;
            return this;
        }

        /** Specifies the cursor to report if the terminator is not found. */
        public ReadOptions cursor(Cursor value) {
            this.cursor = value;
            return this;
        }

        /** Specifies that the cursor at the mark should be reported if the terminator is not found. */
        public ReadOptions cursorAtMark() {
            this.cursorAtMark = true;
            return this;
        }
    }

    private record SavedState(List<String> lines, String file, String dir, String path, int lineno, Cursor mark,
            int lookAhead, boolean processLines, boolean unescapeNextLine, Boolean unterminated) {
    }

    protected String file;
    protected String dir;
    protected String path;
    protected int lineno;
    protected List<String> sourceLines;
    // NOTE lines are stored in reverse order (the next line is at the end of the list)
    protected List<String> lines;
    protected Cursor mark;
    protected int lookAhead;
    protected boolean processLines;
    protected boolean unescapeNextLine;
    protected Boolean unterminated;
    private SavedState saved;

    /**
     * Initialize the Reader object.
     *
     * @param data the source data as a String, a byte array (decoded using the BOM, if present, or UTF-8) or a List of
     *            String lines
     * @param cursor the starting cursor (either a {@link Cursor} or a String file path) or null
     * @param normalize whether to normalize the lines (Boolean.TRUE, "chomp" or null)
     */
    public Reader(Object data, Object cursor, Object normalize) {
        this(cursor, Deferred.LINES);
        sourceLines = doPrepareLines(data, normalize);
        lines = new ArrayList<>(sourceLines);
        Collections.reverse(lines);
    }

    /** A marker used to select the constructor that defers the initialization of the lines. */
    protected enum Deferred {
        LINES
    }

    /**
     * Initialize the Reader object with the specified cursor and no lines. Intended to be used by subclasses, which
     * must then initialize the lines using {@link #initLines(List)}.
     */
    protected Reader(Object cursor, Deferred deferred) {
        if (cursor == null) {
            file = null;
            dir = ".";
            path = "<stdin>";
            lineno = 1;
        } else if (cursor instanceof String fileStr) {
            file = fileStr;
            dir = Helpers.dirname(file);
            path = Helpers.basename(file);
            lineno = 1;
        } else {
            Cursor c = (Cursor) cursor;
            if ((file = c.getFile()) != null) {
                dir = c.getDir() == null ? Helpers.dirname(file) : c.getDir();
                path = c.getPath() == null ? Helpers.basename(file) : c.getPath();
            } else {
                dir = c.getDir() == null ? "." : c.getDir();
                path = c.getPath() == null ? "<stdin>" : c.getPath();
            }
            lineno = c.getLineNumber() == 0 ? 1 : c.getLineNumber();
        }
        sourceLines = new ArrayList<>();
        lines = new ArrayList<>();
        mark = null;
        lookAhead = 0;
        processLines = true;
        unescapeNextLine = false;
        unterminated = null;
        saved = null;
    }

    public Reader(Object data, Object cursor) {
        this(data, cursor, null);
    }

    public Reader() {
        this(null, null, null);
    }

    public Reader(Object data) {
        this(data, null, null);
    }

    public String getFile() {
        return file;
    }

    public String getDir() {
        return dir;
    }

    public String getPath() {
        return path;
    }

    /**
     * @return the 1-based offset of the current line.
     */
    public int getLineNumber() {
        return lineno;
    }

    /**
     * @return the document source as a String List of lines.
     */
    public List<String> getSourceLines() {
        return sourceLines;
    }

    /**
     * @return whether lines are processed on first visit (default: true)
     */
    public boolean isProcessLines() {
        return processLines;
    }

    public void setProcessLines(boolean processLines) {
        this.processLines = processLines;
    }

    /**
     * @return true if the end of the reader was reached with a delimited block still open
     */
    public Boolean getUnterminated() {
        return unterminated;
    }

    public void setUnterminated(Boolean unterminated) {
        this.unterminated = unterminated;
    }

    /**
     * Check whether there are any lines left to read.
     *
     * @return True if there are more lines, False if there are not.
     */
    public boolean hasMoreLines() {
        if (lines.isEmpty()) {
            lookAhead = 0;
            return false;
        }
        return true;
    }

    /**
     * Check whether this reader is empty (contains no lines).
     *
     * @return true if there are no more lines to peek, otherwise false.
     */
    public boolean isEmpty() {
        if (lines.isEmpty()) {
            lookAhead = 0;
            return true;
        }
        return false;
    }

    public boolean isEof() {
        return isEmpty();
    }

    /**
     * Peek at the next line and check if it's empty (i.e., whitespace only).
     *
     * <p>This method does not consume the line from the stack.
     *
     * @return True if the there are no more lines or if the next line is empty
     */
    public boolean isNextLineEmpty() {
        return Strings.isNullOrEmpty(peekLine());
    }

    /**
     * Peek at the next line of source data. Processes the line if not already marked as processed, but does not
     * consume it.
     *
     * <p>This method will probe the reader for more lines. If there is a next line that has not previously been
     * visited, the line is passed to the {@link #processLine(String)} method to be initialized. This call gives
     * sub-classes the opportunity to do preprocessing. If the return value of processLine is null, the data is assumed
     * to be changed and peekLine is invoked again to perform further processing.
     *
     * @param direct A Boolean flag to bypasses the check for more lines and immediately returns the first element of
     *            the internal lines stack. (default: false)
     * @return the next line of the source data as a String if there are lines remaining, null otherwise.
     */
    public String peekLine(boolean direct) {
        while (true) {
            String nextLine = lines.isEmpty() ? null : lines.get(lines.size() - 1);
            if (direct || lookAhead > 0) {
                return unescapeNextLine && nextLine != null ? nextLine.substring(1) : nextLine;
            }
            if (nextLine != null) {
                // FIXME the problem with this approach is that we aren't retaining the modified line (hence the
                // unescapeNextLine tweak) perhaps we need a stack of proxied lines
                String line = processLine(nextLine);
                if (line != null) {
                    return line;
                }
            } else {
                lookAhead = 0;
                return null;
            }
        }
    }

    public String peekLine() {
        return peekLine(false);
    }

    /**
     * Peek at the next multiple lines of source data. Processes the lines if not already marked as processed, but does
     * not consume them.
     *
     * <p>This method delegates to {@link #readLine()} to process and collect the line, then restores the lines to the
     * stack before returning them. This allows the lines to be processed and marked as such so that subsequent reads
     * will not need to process the lines again.
     *
     * @param num The positive Integer number of lines to peek or null to peek all lines (default: null).
     * @param direct A Boolean indicating whether processing should be disabled when reading lines (default: false).
     * @return A String List of the next multiple lines of source data, or an empty List if there are no more lines in
     *         this Reader.
     */
    public List<String> peekLines(Integer num, boolean direct) {
        int oldLookAhead = lookAhead;
        List<String> result = new ArrayList<>();
        long max = num == null ? Asciidoctor.MAX_INT : num;
        for (long i = 0; i < max; i++) {
            String line = direct ? shift() : readLine();
            if (line != null) {
                result.add(line);
            } else {
                if (direct) {
                    lineno--;
                }
                break;
            }
        }

        if (!result.isEmpty()) {
            unshiftAll(result);
            if (direct) {
                lookAhead = oldLookAhead;
            }
        }

        return result;
    }

    public List<String> peekLines(Integer num) {
        return peekLines(num, false);
    }

    public List<String> peekLines() {
        return peekLines(null, false);
    }

    /**
     * Get the next line of source data. Consumes the line returned.
     *
     * @return the String of the next line of the source data if data is present, null otherwise.
     */
    public String readLine() {
        // hasMoreLines triggers preprocessor
        return lookAhead > 0 || hasMoreLines() ? shift() : null;
    }

    /**
     * Get the remaining lines of source data.
     *
     * <p>This method calls {@link #readLine()} repeatedly until all lines are consumed and returns the lines as a
     * String List. This method differs from {@link #getLines()} in that it processes each line in turn, hence
     * triggering any preprocessors implemented in sub-classes.
     *
     * @return the lines read as a String List
     */
    public List<String> readLines() {
        List<String> result = new ArrayList<>();
        // hasMoreLines triggers preprocessor
        while (hasMoreLines()) {
            result.add(shift());
        }
        return result;
    }

    /**
     * Get the remaining lines of source data joined as a String.
     *
     * @return the lines read joined as a String
     */
    public String read() {
        return String.join(LF, readLines());
    }

    /**
     * Advance to the next line by discarding the line at the front of the stack.
     *
     * @return a Boolean indicating whether there was a line to discard.
     */
    public boolean advance() {
        return shift() != null;
    }

    /**
     * Push the String line onto the beginning of the List of source data.
     *
     * <p>A line pushed on the reader using this method is not processed again. The method assumes the line was
     * previously retrieved from the reader or does not otherwise contain preprocessor directives. Therefore, it is
     * marked as processed immediately.
     *
     * @param lineToRestore the line to restore onto the stack
     */
    public void unshiftLine(String lineToRestore) {
        unshift(lineToRestore);
    }

    public void restoreLine(String lineToRestore) {
        unshiftLine(lineToRestore);
    }

    /**
     * Push a List of lines onto the front of the List of source data.
     *
     * <p>Lines pushed on the reader using this method are not processed again. The method assumes the lines were
     * previously retrieved from the reader or do not otherwise contain preprocessor directives. Therefore, they are
     * marked as processed immediately.
     */
    public void unshiftLines(List<String> linesToRestore) {
        unshiftAll(linesToRestore);
    }

    public void restoreLines(List<String> linesToRestore) {
        unshiftLines(linesToRestore);
    }

    /**
     * Replace the next line with the specified line.
     *
     * <p>Calls {@link #advance()} to consume the current line, then calls {@link #unshift(String)} to push the
     * replacement onto the top of the line stack.
     *
     * @param replacement The String line to put in place of the next line (i.e., the line at the cursor).
     * @return true.
     */
    public boolean replaceNextLine(String replacement) {
        shift();
        unshift(replacement);
        return true;
    }

    /**
     * Skip blank lines at the cursor.
     *
     * <pre>
     *   reader.getLines()
     *   => ["", "", "Foo", "Bar", ""]
     *   reader.skipBlankLines()
     *   => 2
     *   reader.getLines()
     *   => ["Foo", "Bar", ""]
     * </pre>
     *
     * @return the number of lines skipped or null if all lines have been consumed (even if lines were skipped by this
     *         method).
     */
    public Integer skipBlankLines() {
        if (isEmpty()) {
            return null;
        }

        int numSkipped = 0;
        // optimized code for shortest execution path
        String nextLine;
        while ((nextLine = peekLine()) != null) {
            if (!nextLine.isEmpty()) {
                return numSkipped;
            }
            shift();
            numSkipped++;
        }
        return null;
    }

    /**
     * Skip consecutive comment lines and block comments.
     */
    public void skipCommentLines() {
        if (isEmpty()) {
            return;
        }

        String nextLine;
        while ((nextLine = peekLine()) != null && !nextLine.isEmpty()) {
            if (!nextLine.startsWith("//")) {
                break;
            }
            if (nextLine.startsWith("///")) {
                int ll = nextLine.length();
                if (!(ll > 3 && nextLine.equals("/".repeat(ll)))) {
                    break;
                }
                readLinesUntil(ReadOptions.create().terminator(nextLine).skipFirstLine().readLastLine()
                        .skipProcessing().context("comment"));
            } else {
                shift();
            }
        }
    }

    /**
     * Skip consecutive comment lines and return them.
     *
     * <p>This method assumes the reader only contains simple lines (no blocks).
     */
    public List<String> skipLineComments() {
        List<String> commentLines = new ArrayList<>();
        if (isEmpty()) {
            return commentLines;
        }
        String nextLine;
        // optimized code for shortest execution path
        while ((nextLine = peekLine()) != null && !nextLine.isEmpty()) {
            if (!nextLine.startsWith("//")) {
                break;
            }
            commentLines.add(shift());
        }
        return commentLines;
    }

    /**
     * Advance to the end of the reader, consuming all remaining lines.
     */
    public void terminate() {
        lineno += lines.size();
        lines.clear();
        lookAhead = 0;
    }

    /**
     * Return all the lines from the stack until we (1) run out them, (2) find a blank line with breakOnBlankLines, or
     * (3) find a line for which the given predicate evaluates to true.
     *
     * <pre>
     *   data = ["First line", "Second line", "", "Third line"];
     *   reader = new Reader(data, null, true);
     *
     *   reader.readLinesUntil(ReadOptions.create().breakOnBlankLines(), null)
     *   => ["First line", "Second line"]
     * </pre>
     *
     * @param options the options that control processing
     * @param breakCondition an optional predicate that signals the reader to stop when it returns true
     * @return the List of lines forming the next segment.
     */
    public List<String> readLinesUntil(ReadOptions options, Predicate<String> breakCondition) {
        List<String> result = new ArrayList<>();
        boolean restoreProcessLines = false;
        if (processLines && options.skipProcessing) {
            processLines = false;
            restoreProcessLines = true;
        }
        String terminator = options.terminator;
        Object startCursor = null;
        boolean breakOnBlankLines;
        boolean breakOnListContinuation;
        if (terminator != null) {
            startCursor = options.cursorAtMark ? "at_mark" : (options.cursor != null ? options.cursor : cursor());
            breakOnBlankLines = false;
            breakOnListContinuation = false;
        } else {
            breakOnBlankLines = options.breakOnBlankLines;
            breakOnListContinuation = options.breakOnListContinuation;
        }
        boolean preserveLastLine = options.preserveLastLine;
        boolean skipComments = options.skipLineComments;
        boolean lineRead = false;
        boolean lineRestored = false;
        if (options.skipFirstLine) {
            shift();
        }
        String line;
        while ((line = readLine()) != null) {
            boolean stop;
            if (terminator != null) {
                stop = line.equals(terminator);
            } else if (breakOnBlankLines && line.isEmpty()) {
                stop = true;
            } else if (breakOnListContinuation && lineRead && line.equals(LIST_CONTINUATION)) {
                preserveLastLine = true;
                stop = true;
            } else {
                stop = breakCondition != null && breakCondition.test(line);
            }
            if (stop) {
                if (options.readLastLine) {
                    result.add(line);
                }
                if (preserveLastLine) {
                    unshift(line);
                    lineRestored = true;
                }
                break;
            }
            if (!(skipComments && line.startsWith("//") && !line.startsWith("///"))) {
                result.add(line);
                lineRead = true;
            }
        }
        if (restoreProcessLines) {
            processLines = true;
            if (lineRestored && terminator == null) {
                lookAhead--;
            }
        }
        if (terminator != null && !terminator.equals(line)) {
            String context = options.contextSet ? options.context : terminator;
            if (context != null) {
                Cursor reportCursor = "at_mark".equals(startCursor) ? cursorAtMark() : (Cursor) startCursor;
                getLogger().warn(Logging.messageWithContext("unterminated " + context + " block", reportCursor));
                unterminated = true;
            }
        }
        return result;
    }

    public List<String> readLinesUntil(ReadOptions options) {
        return readLinesUntil(options, null);
    }

    /**
     * Internal: Shift the line off the stack and increment the lineno.
     *
     * <p>This method can be used directly when you've already called peekLine and determined that you do, in fact,
     * want to pluck that line off the stack. Use readLine if the line hasn't (or many not have been) visited yet.
     *
     * @return The String line at the top of the stack
     */
    public String shift() {
        lineno++;
        if (lookAhead != 0) {
            lookAhead--;
        }
        return lines.isEmpty() ? null : lines.remove(lines.size() - 1);
    }

    /**
     * Internal: Restore the line to the stack and decrement the lineno.
     */
    public void unshift(String line) {
        lineno--;
        lookAhead++;
        lines.add(line);
    }

    /**
     * Internal: Restore the lines to the stack and decrement the lineno.
     */
    public void unshiftAll(List<String> linesToRestore) {
        lineno -= linesToRestore.size();
        lookAhead += linesToRestore.size();
        for (int i = linesToRestore.size() - 1; i >= 0; i--) {
            lines.add(linesToRestore.get(i));
        }
    }

    public Cursor cursor() {
        return new Cursor(file, dir, path, lineno);
    }

    public Cursor cursorAtLine(int lineno) {
        return new Cursor(file, dir, path, lineno);
    }

    public Cursor cursorAtMark() {
        return mark != null ? mark.copy() : cursor();
    }

    public Cursor cursorBeforeMark() {
        if (mark != null) {
            return new Cursor(mark.getFile(), mark.getDir(), mark.getPath(), mark.getLineNumber() - 1);
        }
        return new Cursor(file, dir, path, lineno - 1);
    }

    public Cursor cursorAtPrevLine() {
        return new Cursor(file, dir, path, lineno - 1);
    }

    public Cursor mark() {
        mark = new Cursor(file, dir, path, lineno);
        return mark;
    }

    /**
     * Get information about the last line read, including file name and line number.
     *
     * @return A String summary of the last line read
     */
    public String getLineInfo() {
        return path + ": line " + lineno;
    }

    /**
     * Get a copy of the remaining List of String lines managed by this Reader.
     *
     * @return A copy of the String List of lines remaining in this Reader
     */
    public List<String> getLines() {
        List<String> result = new ArrayList<>(lines);
        Collections.reverse(result);
        return result;
    }

    /**
     * Get a copy of the remaining lines managed by this Reader joined as a String.
     */
    public String getString() {
        return String.join(LF, getLines());
    }

    /**
     * Get the source lines for this Reader joined as a String.
     */
    public String getSource() {
        return String.join(LF, sourceLines);
    }

    /**
     * Internal: Save the state of the reader at cursor.
     */
    public void save() {
        saved = new SavedState(new ArrayList<>(lines), file, dir, path, lineno, mark, lookAhead, processLines,
                unescapeNextLine, unterminated);
        saveExtraState();
    }

    /**
     * Internal: Restore the state of the reader at cursor.
     */
    public void restoreSave() {
        if (saved == null) {
            return;
        }
        lines = saved.lines();
        file = saved.file();
        dir = saved.dir();
        path = saved.path();
        lineno = saved.lineno();
        mark = saved.mark();
        lookAhead = saved.lookAhead();
        processLines = saved.processLines();
        unescapeNextLine = saved.unescapeNextLine();
        unterminated = saved.unterminated();
        saved = null;
        restoreExtraState();
    }

    /**
     * Internal: Discard a previous saved state.
     */
    public void discardSave() {
        saved = null;
    }

    /**
     * Hook for subclasses to save additional state.
     */
    protected void saveExtraState() {
    }

    /**
     * Hook for subclasses to restore additional state.
     */
    protected void restoreExtraState() {
    }

    @Override
    public String toString() {
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {path: " + Strings.inspect(path)
                + ", line: " + lineno + "}>";
    }

    /**
     * Internal: Prepare the source data for parsing.
     *
     * <p>Converts the source data into a List of lines ready for parsing. If the normalize option is set, this method
     * strips trailing whitespace, including the newline. (This whitespace cleaning is very important to how Asciidoctor
     * works). Subclasses may choose to perform additional preparation.
     *
     * @param data A String List or String of source data to be normalized.
     * @param normalize Enables line normalization, which removes trailing whitespace; "chomp" removes trailing newline
     *            only (optional, not set).
     * @param include whether the data is the content of an include file (only used by subclasses)
     * @param indent the indentation to apply (only used by subclasses)
     * @param skipFrontMatter whether to skip the front matter (only used by subclasses)
     * @return A String List of source lines. If the source data is a List, this method returns a copy.
     */
    protected List<String> prepareLines(Object data, Object normalize, boolean include, Object indent,
            boolean skipFrontMatter) {
        return doPrepareLines(data, normalize);
    }

    /**
     * Internal: Initialize the source lines and the line stack.
     */
    protected void initLines(List<String> preparedLines) {
        sourceLines = preparedLines;
        lines = new ArrayList<>(sourceLines);
        Collections.reverse(lines);
    }

    @SuppressWarnings("unchecked")
    private static List<String> doPrepareLines(Object data, Object normalize) {
        if (data instanceof byte[] bytes) {
            data = Helpers.decode(bytes, null);
        }
        if (normalize != null && !Boolean.FALSE.equals(normalize)) {
            boolean trimEnd = !"chomp".equals(normalize);
            return data instanceof List<?> list ? Helpers.prepareSourceArray((List<String>) list, trimEnd)
                    : Helpers.prepareSourceString((String) data, trimEnd);
        } else if (data instanceof List<?> list) {
            return new ArrayList<>((List<String>) list);
        } else if (data != null) {
            return Strings.split(Strings.chomp(data.toString()), LF, -1);
        }
        return new ArrayList<>();
    }

    /**
     * Internal: Processes a previously unvisited line.
     *
     * <p>By default, this method marks the line as processed by incrementing the look ahead counter and returns the
     * line unmodified.
     *
     * @return The String line the Reader should make available to the next invocation of readLine or null if the
     *         Reader should drop the line, advance to the next line and process it.
     */
    protected String processLine(String line) {
        if (processLines) {
            lookAhead++;
        }
        return line;
    }
}
