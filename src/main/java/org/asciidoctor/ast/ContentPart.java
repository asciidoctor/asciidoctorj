package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public class ContentPart {
    private String id;
    private int level;
    private String context;
    private String style;
    private String role;
    private String title;
    private Map<String, Object> attributes;
    private String content;
    private List<ContentPart> parts;

    private ContentPart() {
        super();
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public String getContext() {
        return context;
    }

    public String getStyle() {
        return style;
    }

    public String getRole() {
        return role;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getContent() {
        return content;
    }

    public List<ContentPart> getParts() {
        return parts;
    }

    public void setParts(List<ContentPart> parts) {
        this.parts = parts;
    }

    public static ContentPart createContentPart(String id, int level, String context, String title, String style,
            String role, Map<String, Object> attributes, String textContent) {
        ContentPart contentPart = new ContentPart();
        contentPart.level = level;
        contentPart.id = id;
        contentPart.context = context;
        contentPart.style = style;
        contentPart.role = role;
        contentPart.title = title;
        contentPart.attributes = attributes;
        contentPart.content = textContent;

        return contentPart;
    }

}
