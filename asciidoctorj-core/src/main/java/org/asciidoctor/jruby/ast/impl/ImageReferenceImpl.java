package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.ImageReference;
import org.jruby.RubyStruct;
import org.jruby.javasupport.JavaEmbedUtils;

public class ImageReferenceImpl implements ImageReference {

    private static final String IMAGESDIR_KEY_NAME = "imagesdir";
    private static final String TARGET_KEY_NAME = "target";

    private final String target;
    private final String imagesdir;

    private ImageReferenceImpl(String target, String imagesdir) {
        this.target = target;
        this.imagesdir = imagesdir;
    }

    static ImageReference getInstance(RubyStruct rubyFootnote) {
        final String target = (String) aref(rubyFootnote, TARGET_KEY_NAME);
        final String imagesdir = (String) aref(rubyFootnote, IMAGESDIR_KEY_NAME);
        return new ImageReferenceImpl(target, imagesdir);
    }

    private static Object aref(RubyStruct s, String key) {
        return JavaEmbedUtils.rubyToJava(s.aref(s.getRuntime().newString(key)));
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getImagesdir() {
        return imagesdir;
    }
}
