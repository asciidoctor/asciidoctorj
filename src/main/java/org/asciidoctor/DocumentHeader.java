package org.asciidoctor;

import java.util.Map;

public class DocumentHeader {

	private String documentTitle;
	private int numberOfBlocks;
	
	private Map<String, Object> attributes;
	
	private DocumentHeader() {
		super();
	}
	
	public String getDocumentTitle() {
		return documentTitle;
	}
	
	public int getNumberOfBlocks() {
		return numberOfBlocks;
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public static DocumentHeader createDocumentHeader(String documentTitle, int numberOfBlocks, Map<String, Object> attributes) {
	
		DocumentHeader documentHeader = new DocumentHeader();
		
		documentHeader.documentTitle = documentTitle;
		documentHeader.numberOfBlocks = numberOfBlocks;
		documentHeader.attributes = attributes;
		
		return documentHeader;
	}
	
}
