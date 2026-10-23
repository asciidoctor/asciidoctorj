package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.core.Reader;

/**
 * The {@link Cursor} of the AsciidoctorJ API.
 */
public class CursorImpl implements Cursor {
    private final Reader.Cursor delegate;

    public CursorImpl(Reader.Cursor delegate) {
        this.delegate = delegate;
    }

    public static Cursor wrap(Reader.Cursor cursor) {
        return cursor == null ? null : new CursorImpl(cursor);
    }

    /**
     * @return the cursor of the implementation
     */
    public Reader.Cursor getDelegate() {
        return delegate;
    }

    /**
     * @return the cursor of the implementation that corresponds to the cursor of the API
     */
    public static Reader.Cursor unwrap(Cursor cursor) {
        if (cursor == null) {
            return null;
        } else if (cursor instanceof CursorImpl cursorImpl) {
            return cursorImpl.delegate;
        }
        return new Reader.Cursor(cursor.getFile(), cursor.getDir(), cursor.getPath(), cursor.getLineNumber());
    }

    @Override
    public int getLineNumber() {
        return delegate.getLineNumber();
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public String getDir() {
        return delegate.getDir();
    }

    @Override
    public String getFile() {
        return delegate.getFile();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
