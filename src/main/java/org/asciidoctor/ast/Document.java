package org.asciidoctor.ast;

import java.util.Map;

import org.jruby.Ruby;

public class Document extends AbstractBlockImpl implements DocumentRuby {

	private DocumentRuby documentDelegate;
	
	public Document(DocumentRuby documentRuby, Ruby runtime) {
		super(documentRuby, runtime);
		this.documentDelegate = documentRuby;
	}

	public DocumentRuby getDocumentRuby() {
		return documentDelegate;
	}

	@Override
	public String doctitle() {
		return documentDelegate.doctitle();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes();
	}

	@Override
	public boolean basebackend(String backend) {
		return documentDelegate.basebackend(backend);
	}

}
