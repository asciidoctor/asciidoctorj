package org.asciidoctor.util.pdf;

/**
 * Contains the information about an image found in a PDF
 *
 * @author abelsromero
 */
public class Image {

    // Page where the image is localed
    private int page;

    // Position inside the page
    private float xPosition;
    private float yPosition;

    // size in pixels
    private int originalWidth;
    private int originalHeight;

    // size in pixels
    private int renderedWidth;
    private int renderedHeight;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public float getXPosition() {
        return xPosition;
    }

    public void setXPosition(float xPosition) {
        this.xPosition = xPosition;
    }

   public float getYPosition() {
        return yPosition;
    }

    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    public int getRenderedWidth() {
        return renderedWidth;
    }

    public void setRenderedWidth(int renderedWidth) {
        this.renderedWidth = renderedWidth;
    }

    public int getRenderedHeight() {
        return renderedHeight;
    }

    public void setRenderedHeight(int renderedHeight) {
        this.renderedHeight = renderedHeight;
    }
}
