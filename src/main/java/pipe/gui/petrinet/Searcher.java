package pipe.gui.petrinet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.regex.Pattern;

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
     * @return List of all matches ordered by relevance (highest score first)
     */
    public List<T> findAllMatches(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }
        
        PriorityQueue<ScoredItem> allMatches = new PriorityQueue<>();
        String lowerQuery = query.toLowerCase();
        
        // Score all items
        for (T item : items) {
            String name = nameExtractor.apply(item);
            String lowerName = name.toLowerCase();
            double score = calculateScore(lowerName, lowerQuery);
            
            if (score > 0) {
                allMatches.add(new ScoredItem(item, score));
            }
        }
        
        List<T> results = new ArrayList<>(allMatches.size());
        while (!allMatches.isEmpty()) {
            results.add(0, allMatches.poll().item); 
        }
        
        return results;
    }
    
    /**
     * Calculate relevance score for an item based on how well it matches the query
     */
    private double calculateScore(String text, String query) {
        // Exact match
        if (text.equals(query)) {
            return 1.0;
        }
    
        double positionBonus = 0.0;
        if (text.startsWith(query)) {
            // Prefix match gets highest position bonus
            positionBonus = 0.3;
        } else if (text.matches(".*(_|^)" + Pattern.quote(query) + "($|_).*")) {
            // Word boundary match gets medium position bonus
            positionBonus = 0.2;
        } else if (text.contains(query)) {
            // Contains anywhere gets smallest position bonus
            positionBonus = 0.1;
        } else {
            // No match
            return 0.0;
        }

        double matchRatio = Math.min((double)query.length() / text.length(), 0.69);
        
        return matchRatio + positionBonus;
    }
    
    private class ScoredItem implements Comparable<ScoredItem> {
        final T item;
        final double score;
        
        ScoredItem(T item, double score) {
            this.item = item;
            this.score = score;
        }
        
        @Override
        public int compareTo(ScoredItem other) {
            return Double.compare(score, other.score);
        }
    }
}
