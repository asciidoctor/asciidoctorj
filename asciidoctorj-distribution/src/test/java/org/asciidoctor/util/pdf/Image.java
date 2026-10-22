package org.asciidoctor.util.pdf;

/**
 * Contains the information about an image found in a PDF
 *
 * @author abelsromero
 */
public final class Image {

    // Page where the image is localed
    private final int page;
    // Position inside the page
    private final float xPosition;
    private final float yPosition;
    // size in pixels
    private final int originalWidth;
    private final int originalHeight;

    Image(int page, float xPosition, float yPosition, int originalWidth, int originalHeight) {
        this.page = page;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
    }

    public int getPage() {
        return page;
    }

    public float getxPosition() {
        return xPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }
}
