package org.asciidoctor.util.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes a PDF document to extract the metadata of the contained images.
 * <p>
 * Based on https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/util/PrintImageLocations.java?revision=1904918&view=markup
 */
public class ImageProcessor extends PDFStreamEngine {

    private int currentPage = 0;
    private final List<Image> images = new ArrayList<>();

    public ImageProcessor() {
        addOperator(new Concatenate(this));
        addOperator(new DrawObject(this));
        addOperator(new SetGraphicsStateParameters(this));
        addOperator(new Save(this));
        addOperator(new Restore(this));
        addOperator(new SetMatrix(this));
    }

    public void parse(String filename) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filename))) {
            PDPageTree pages = document.getDocumentCatalog().getPages();
            for (int i = 0; i < pages.getCount(); i++) {
                currentPage = i;
                processPage(pages.get(i));
            }
        }
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {

        if (OperatorName.DRAW_OBJECT.equals(operator.getName())) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);

            if (xobject instanceof PDImageXObject) {
                final PDImageXObject pdImage = (PDImageXObject) xobject;
                final Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();

                final Image image = new Image(currentPage, ctmNew.getTranslateX(), ctmNew.getTranslateY(), pdImage.getWidth(), pdImage.getHeight());
                images.add(image);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    public List<Image> getImages() {
        return images;
    }
}
