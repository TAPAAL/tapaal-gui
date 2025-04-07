package pipe.gui.petrinet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Generic search utility for finding best matches in a collection
 * @param <T> Type of object to search through
 */
public class Searcher<T> {
    private final List<T> items;
    private final Function<T, String> nameExtractor;
    
    /**
     * Creates a new search utility
     * @param items Collection of items to search through
     * @param nameExtractor Function to extract searchable text from items
     */
    public Searcher(List<T> items, Function<T, String> nameExtractor) {
        this.items = new ArrayList<>(items);
        this.nameExtractor = nameExtractor;
    }
    
    /**
     * Find all matches for a search query
     * @param query The search string
     * @return List of all matches ordered by alphabetical order)
     */
    public List<T> findAllMatches(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<T> matches = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // Score all items
        for (T item : items) {
            String name = nameExtractor.apply(item);
            String lowerName = name.toLowerCase();
            
            if (lowerName.contains(lowerQuery)) {
                matches.add(item);
            }
        }
        
        matches.sort(Comparator.comparing(nameExtractor, String.CASE_INSENSITIVE_ORDER));
        
        return matches;
    }
}
