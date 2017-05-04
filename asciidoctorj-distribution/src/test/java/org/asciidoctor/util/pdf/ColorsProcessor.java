package org.asciidoctor.util.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextPosition;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Parses a PDF document looking for certain words, if found it stores the
 * associated colors.
 *
 * Note: currently stores the color of the last character in fact.
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
    private Map<String,List<Color>> colors = new HashMap<String,List<Color>>();

    /**
     * Constructor
     *
     * @param words Words to look for into the document
     *
     * @throws java.io.IOException If there is an error loading text stripper properties.
     */
    public ColorsProcessor(String... words) throws IOException {
        super(ResourceLoader.loadProperties(
                "pdfbox/resources/ColorsProcessor.properties", true));
        super.setSortByPosition(true);
        this.words = Arrays.asList(words);
    }

    /**
     * Parses a document extracting the colors for the specified words in
     * the constructor
     *
     * @param filename PDF document path
     */
    public void parse (String filename) throws IOException {
        PDDocument document = null;
        try {
            document = PDDocument.load(filename, false);
            List allPages = document.getDocumentCatalog().getAllPages();
            for( int i=0; i<allPages.size(); i++ ) {
                PDPage page = (PDPage)allPages.get( i );
                PDStream contents = page.getContents();
                if (contents != null) {
                    processStream( page, page.getResources(),
                        page.getContents().getStream() );
                }
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }
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
    private Color previousColor;

    /**
     * Terminal signs using to split words
     *
     * Note: \00A0: non break space
     */
    private static final List<String> TERMINALS = Arrays.asList(" ", "\n", "\t", "(", ")", "\u00A0");

    /**
     * Processes text events.
     *
     * Stores characters in a buffer until a terminal symbol is found
     * (e.g. space), then treats the characters stored as a single word.
     *
     * @param text The text to be processed
     */
    @Override
    protected void processTextPosition( TextPosition text ) {
        String chars = text.getCharacter();
        // Some line breaks do not enter here, I ignore why
        if (TERMINALS.contains(chars)) {
            String word = charsBuffer.toString();
            if (words.contains(word)) {
                addColor(charsBuffer.toString(), previousColor);
            }
            charsBuffer = new StringBuffer();
        } else {
            charsBuffer.append(chars);
            previousText = text;
            try {
                previousColor = getGraphicsState().getNonStrokingColor().getJavaColor();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Adds a color mapping to the colors attribute
     *
     * @param word Word to add
     * @param color Color of the word
     */
    private void addColor(String word, Color color) {
        List<Color> values = colors.get(word);
        if (values == null) {
            List<Color> aux = new ArrayList<Color>();
            aux.add(color);
            colors.put(word, aux);
        } else {
            values.add(color);
        }
    }

    /**
     * Returns the words and their colors after parsing a file
     *
     * @return List of found images
     */
    public Map<String, List<Color>> getColors() {
        return colors;
    }

}
