package org.asciidoctor.util.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

/**
 * Parses a PDF document looking for certain words, if found it stores the
 * associated colors.
 * <p>
 * Note: currently stores the color of the last character in fact.
 * Based on https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/util/PrintTextColors.java?revision=1904918&view=markup
 *
 * @author abelsromero
 */
public class ColorsProcessor extends PDFTextStripper {

    /**
     * List of words to search for in the document
     */
    private List<String> words;
    /**
     * List of words mapped to the different colors in which they appear
     */
    private Map<String, List<Color>> colors = new HashMap<>();

    public ColorsProcessor(String... words) {
        addOperator(new SetStrokingColorSpace(this));
        addOperator(new SetNonStrokingColorSpace(this));
        addOperator(new SetStrokingDeviceCMYKColor(this));
        addOperator(new SetNonStrokingDeviceCMYKColor(this));
        addOperator(new SetNonStrokingDeviceRGBColor(this));
        addOperator(new SetStrokingDeviceRGBColor(this));
        addOperator(new SetNonStrokingDeviceGrayColor(this));
        addOperator(new SetStrokingDeviceGrayColor(this));
        addOperator(new SetStrokingColor(this));
        addOperator(new SetStrokingColorN(this));
        addOperator(new SetNonStrokingColor(this));
        addOperator(new SetNonStrokingColorN(this));
        setSortByPosition(true);
        this.words = Arrays.asList(words);
    }

    public void parse(String filename) throws IOException {
        PDDocument document = Loader.loadPDF(new File(filename));
        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        writeText(document, dummy);
    }

    /**
     * Characters buffer
     */
    private StringBuffer charsBuffer = new StringBuffer();
    /**
     * Previous character processed
     */
    private TextPosition previousText;
    /**
     * Color of the previous character
     */
    private PDColor previousColor;

    /**
     * Terminal signs using to split words
     * <p>
     * Note: \00A0: non break space
     */
    private static final List<String> TERMINALS = Arrays.asList(" ", "\n", "\t", "(", ")", "\u00A0");

    /**
     * Processes text events.
     * <p>
     * Stores characters in a buffer until a terminal symbol is found
     * (e.g. space), then treats the characters stored as a single word.
     *
     * @param text The text to be processed
     */
    @Override
    protected void processTextPosition(TextPosition text) {
//        super.processTextPosition(text);

        String chars = text.toString();
        // Some line breaks do not enter here, I ignore why
        if (TERMINALS.contains(chars)) {
            String word = charsBuffer.toString();
            if (words.contains(word)) {
                registerColor(charsBuffer.toString(), previousColor);
            }
            charsBuffer = new StringBuffer();
        } else {
            charsBuffer.append(chars);
            previousText = text;
            previousColor = getGraphicsState().getNonStrokingColor();
        }
    }

    private void registerColor(String word, PDColor color) {
        List<Color> values = colors.get(word);
        if (values == null) {
            List<Color> aux = new ArrayList<>();
            aux.add(toRGB(color));
            colors.put(word, aux);
        } else {
            values.add(toRGB(color));
        }
    }

    private Color toRGB(PDColor pdColor) {
        float[] components = pdColor.getComponents();
        // Rough conversion, but enough for out tests
        int r = Float.valueOf(256 * components[0]).intValue();
        int g = Float.valueOf(256 * components[1]).intValue();
        int b = Float.valueOf(256 * components[2]).intValue();
        return new Color(r, g, b);
    }

    public Map<String, List<Color>> getColors() {
        return colors;
    }
}
