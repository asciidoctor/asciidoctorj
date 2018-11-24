package org.asciidoctor.extension;

import java.util.List;

public interface Reader {

    /**
     * Get the 1-based offset of the current line.
     *
     * @return 1-based offset.
     * @deprecated Please use {@link #getLineNumber()}
     */
    @Deprecated
    int getLineno();

    /**
     * Get the 1-based offset of the current line.
     *
     * @return 1-based offset.
     */
    int getLineNumber();

    /**
     * Get the name of the file of the current line.
     *
     * @return file name
     */
    String getFile();

    /**
     * Get the name of the directory of the current file
     *
     * @return directory name
     */
    String getDir();

    /**
     * Check whether there are any lines left to read. If a previous call to this method resulted in a value of false,
     * immediately returned the cached value. Otherwise, delegate to peek_line to determine if there is a next line
     * available.
     *
     * @return True if there are more lines, False if there are not.
     */
    boolean hasMoreLines();

    /**
     * Peek at the next line and check if it's empty (i.e., whitespace only)
     *
     * This method Does not consume the line from the stack.
     *
     * @return True if the there are no more lines or if the next line is empty
     */
    boolean isNextLineEmpty();

    /**
     * Get the remaining lines of source data joined as a String. Delegates to Reader#read_lines, then joins the result.
     *
     * @return the lines read joined as a String
     */
    String read();

    /**
     * Get the remaining lines of source data. This method calls Reader#read_line repeatedly until all lines are
     * consumed and returns the lines as a String Array. This method differs from Reader#lines in that it processes each
     * line in turn, hence triggering any preprocessors implemented in sub-classes.
     *
     * @return the lines read as a String Array
     */
    List<String> readLines();

    /**
     * Get the next line of source data. Consumes the line returned.
     *
     * @return the String of the next line of the source data if data is present or
     * nulll if there is no more data.
    */
    String readLine();

    List<String> lines();

    /**
     * Push the String line onto the beginning of the Array of source data.
     *
     * Since this line was (assumed to be) previously retrieved through the
     * reader, it is marked as seen.
     */
    void restoreLine(String line);

    /**
     * Push multiple lines onto the beginning of the Array of source data.
     *
     * Since this lines were (assumed to be) previously retrieved through the
     * reader, they are marked as seen.
     */
    void restoreLines(List<String> line);

    /**
     * Peek at the next line of source data. Processes the line, if not
     * already marked as processed, but does not consume it.
     *
     * This method will probe the reader for more lines.
     */
    String peekLine();

    /**
     * Peek at the next multiple lines of source data. Processes the lines, if not
     * already marked as processed, but does not consume them.
     *
     * @param lineCount The Integer number of lines to peek.
     * @return
     */
    List<String> peekLines(int lineCount);

    /**
     * Advance to the next line by discarding the line at the front of the stack
     *
     * @return a Boolean indicating whether there was a line to discard.
     */
    boolean advance();

    /**
     *
     * Public: Advance to the end of the reader, consuming all remaining lines
     */
    void terminate();
}
