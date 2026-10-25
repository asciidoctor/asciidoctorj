package org.asciidoctor.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maintains a catalog of callouts and their associations.
 */
public class Callouts {
    private record Callout(int ordinal, String id) {
    }

    private final List<List<Callout>> lists = new ArrayList<>();
    private int listIndex;
    private int coIndex;

    public Callouts() {
        nextList();
    }

    /**
     * Register a new callout for the given list item ordinal.
     *
     * <p>Generates a unique id for this callout based on the index of the next callout list in the document and the
     * index of this callout since the end of the last callout list.
     *
     * <pre>
     *   callouts = new Callouts();
     *   callouts.register("1");
     *   // => "CO1-1"
     *   callouts.nextList();
     *   callouts.register("2");
     *   // => "CO2-1"
     * </pre>
     *
     * @param liOrdinal the ordinal (1-based) of the list item to which this callout is to be associated
     * @return The unique String id of this callout
     */
    public String register(String liOrdinal) {
        String id = generateNextCalloutId();
        currentList().add(new Callout(Strings.toInt(liOrdinal), id));
        coIndex++;
        return id;
    }

    public String register(int liOrdinal) {
        return register(String.valueOf(liOrdinal));
    }

    /**
     * Get the next callout index in the document.
     *
     * <p>Reads the next callout index in the document and advances the pointer. This method is used during conversion
     * to retrieve the unique id of the callout that was generated during parsing.
     *
     * @return The unique String id of the next callout in the document
     */
    public String readNextId() {
        String id = null;
        List<Callout> list = currentList();
        if (coIndex <= list.size()) {
            id = list.get(coIndex - 1).id();
        }
        coIndex++;
        return id;
    }

    /**
     * Get a space-separated list of callout ids for the specified list item.
     *
     * @param liOrdinal the ordinal (1-based) of the list item for which to retrieve the callouts
     * @return A space-separated String of callout ids associated with the specified list item
     */
    public String calloutIds(int liOrdinal) {
        return currentList().stream()
                .filter(callout -> callout.ordinal() == liOrdinal)
                .map(Callout::id)
                .collect(Collectors.joining(" "));
    }

    /**
     * @return the number of callouts in the current list
     */
    public int currentListSize() {
        return currentList().size();
    }

    private List<Callout> currentList() {
        return lists.get(listIndex - 1);
    }

    /**
     * Advance to the next callout list in the document.
     */
    public void nextList() {
        listIndex++;
        if (lists.size() < listIndex) {
            lists.add(new ArrayList<>());
        }
        coIndex = 1;
    }

    /**
     * Rewind the list index pointer, intended to be used when switching from the parsing to conversion phase.
     */
    public void rewind() {
        listIndex = 1;
        coIndex = 1;
    }

    private String generateNextCalloutId() {
        return generateCalloutId(listIndex, coIndex);
    }

    private String generateCalloutId(int listIndex, int coIndex) {
        return "CO" + listIndex + "-" + coIndex;
    }
}
