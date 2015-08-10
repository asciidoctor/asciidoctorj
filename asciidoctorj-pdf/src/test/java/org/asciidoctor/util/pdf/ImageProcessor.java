package org.asciidoctor.util.pdf;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processes a PDF document to extract the metadata of the contained images.
 *
 * Based on https://svn.apache.org/viewvc/pdfbox/tags/1.8.9/examples/src/main/java/org/apache/pdfbox/examples/util/PrintImageLocations.java
 *
 * @author abelsromero
 */
public class ImageProcessor extends PDFStreamEngine {
    
    private static final String INVOKE_OPERATOR = "Do";

    private int currentPage = 0;
    private List<Image> images = new ArrayList<Image>();

    /**
     * Default constructor
     *
     * @throws IOException If there is an error loading text stripper properties.
     */
    public ImageProcessor() throws IOException {
        super(ResourceLoader.loadProperties(
                "org/apache/pdfbox/resources/PDFTextStripper.properties", true));
    }

    /**
     * Parses a document extracting the images
     *
     * @param filename PDF document path
     */
    public void parse(String filename) throws IOException {
        PDDocument document = null;
        try {
            document = PDDocument.load(filename, false);
            List allPages = document.getDocumentCatalog().getAllPages();
            for( int i=0; i<allPages.size(); i++ ) {
                PDPage page = (PDPage)allPages.get( i );
                currentPage = i;
                processStream( page, page.findResources(), page.getContents().getStream() );
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * This is used to handle an operation
     *
     * @param operator The operation to perform
     * @param arguments The list of arguments
     *
     * @throws IOException If there is an error processing the operation.
     */
    protected void processOperator( PDFOperator operator, List arguments ) throws IOException {
        String operation = operator.getOperation();

        if (INVOKE_OPERATOR.equals(operation)) {
            COSName objectName = (COSName)arguments.get( 0 );
            Map<String, PDXObject> xobjects = getResources().getXObjects();
            PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );

            if (xobject instanceof PDXObjectImage)  {
                PDXObjectImage image = (PDXObjectImage)xobject;
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();
                PDPage page = getCurrentPage();
                double pageHeight = page.getMediaBox().getHeight();

                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                float yScaling = ctmNew.getYScale();
                float angle = (float)Math.acos(ctmNew.getValue(0, 0)/ctmNew.getXScale());
                if (ctmNew.getValue(0, 1) < 0 && ctmNew.getValue(1, 0) > 0) {
                    angle = (-1)*angle;
                }
                ctmNew.setValue(2, 1, (float)(pageHeight - ctmNew.getYPosition() - Math.cos(angle)*yScaling));
                ctmNew.setValue(2, 0, (float)(ctmNew.getXPosition() - Math.sin(angle)*yScaling));
                // because of the moved 0,0-reference, we have to shear in the opposite direction
                ctmNew.setValue(0, 1, (-1)*ctmNew.getValue(0, 1));
                ctmNew.setValue(1, 0, (-1)*ctmNew.getValue(1, 0));

                Image im = new Image();
                im.setPage(currentPage);
                im.setXPosition(ctmNew.getXPosition());
                im.setYPosition(ctmNew.getYPosition());
                im.setOriginalWidth(imageWidth);
                im.setOriginalHeight(imageHeight);
                im.setRenderedWidth(Math.round(ctmNew.getXScale()));
                im.setRenderedHeight(Math.round(ctmNew.getYScale()));

                images.add(im);
            } else if (xobject instanceof PDXObjectForm) {
                // save the graphics state
                getGraphicsStack().push( (PDGraphicsState)getGraphicsState().clone() );
                PDPage page = getCurrentPage();
                
                PDXObjectForm form = (PDXObjectForm)xobject;
                COSStream invoke = (COSStream)form.getCOSObject();
                PDResources pdResources = form.getResources();
                if(pdResources == null)
                {
                    pdResources = page.findResources();
                }
                // if there is an optional form matrix, we have to
                // map the form space to the user space
                Matrix matrix = form.getMatrix();
                if (matrix != null) 
                {
                    Matrix xobjectCTM = matrix.multiply( getGraphicsState().getCurrentTransformationMatrix());
                    getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
                }
                processSubStream( page, pdResources, invoke );
                
                // restore the graphics state
                setGraphicsState( (PDGraphicsState)getGraphicsStack().pop() );
            }
        } else {
            super.processOperator( operator, arguments );
        }
    }

    /**
     * Returns the list of found images after parsing a file.
     *
     * @return List of found images
     */
    public List<Image> getImages() {
        return images;
    }
}
