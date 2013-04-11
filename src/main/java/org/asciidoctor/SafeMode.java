package org.asciidoctor;

public enum SafeMode {

	UNSAFE(0), SAFE(1), SERVER(10), SECURE(20);
	
	private int level;
	
	private SafeMode(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
}
