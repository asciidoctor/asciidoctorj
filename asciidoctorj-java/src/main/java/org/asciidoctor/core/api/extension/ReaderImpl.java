package org.asciidoctor.core.api.extension;

import java.util.List;

import org.asciidoctor.extension.Reader;

/**
 * The {@link Reader} of the AsciidoctorJ API, which delegates to a reader of the implementation.
 */
public class ReaderImpl implements Reader {
    protected final org.asciidoctor.core.Reader delegate;

    public ReaderImpl(org.asciidoctor.core.Reader delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the reader of the implementation
     */
    public org.asciidoctor.core.Reader getDelegate() {
        return delegate;
    }

    /**
     * @return the reader of the implementation that corresponds to the reader of the API, or null if the reader is null
     */
    static org.asciidoctor.core.Reader unwrap(Reader reader) {
        return reader == null ? null : ((ReaderImpl) reader).delegate;
    }

    @Override
    public int getLineNumber() {
        return delegate.getLineNumber();
    }

    @Override
    public String getFile() {
        return delegate.getFile();
    }

    @Override
    public String getDir() {
        return delegate.getDir();
    }

    @Override
    public boolean hasMoreLines() {
        return delegate.hasMoreLines();
    }

    @Override
    public boolean isNextLineEmpty() {
        return delegate.isNextLineEmpty();
    }

    @Override
    public String read() {
        return delegate.read();
    }

    @Override
    public List<String> readLines() {
        return delegate.readLines();
    }

    @Override
    public String readLine() {
        return delegate.readLine();
    }

    @Override
    public List<String> getLines() {
        return delegate.getLines();
    }

    @Override
    public String getSource() {
        return delegate.getSource();
    }

    @Override
    public List<String> getSourceLines() {
        return delegate.getSourceLines();
    }

    @Override
    public void restoreLine(String line) {
        delegate.restoreLine(line);
    }

    @Override
    public void restoreLines(List<String> lines) {
        delegate.restoreLines(lines);
    }

    @Override
    public String peekLine() {
        return delegate.peekLine();
    }

    @Override
    public List<String> peekLines(int lineCount) {
        return delegate.peekLines(lineCount);
    }

    @Override
    public boolean advance() {
        return delegate.advance();
    }

    @Override
    public void terminate() {
        delegate.terminate();
    }
}
