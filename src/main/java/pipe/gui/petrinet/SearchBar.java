package pipe.gui.petrinet;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dk.aau.cs.util.Pair;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SearchBar extends JPanel {
    private static final String SEARCH_TOOLTIP = "Search for places and transitions in the net";

    private final JLabel searchLabel;
    private final JTextField searchField;
    private final JPopupMenu resultsPopup;
    private Consumer<String> onSearchTextChanged;
    private Consumer<Pair<?, String>> onResultSelected;
    private Runnable onFocusGained;
    private Runnable onFocusLost;
    

    public SearchBar() {
        super(new BorderLayout());
       
        searchLabel = new JLabel("Search: ");
        
        searchField = new JTextField();
        searchField.setToolTipText(SEARCH_TOOLTIP);

        add(searchLabel, BorderLayout.WEST);
        add(searchField, BorderLayout.CENTER);
        
        resultsPopup = new JPopupMenu();
        resultsPopup.setLayout(new BoxLayout(resultsPopup, BoxLayout.Y_AXIS));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifySearchTextChanged();
            }
        
            @Override
            public void removeUpdate(DocumentEvent e) {
                notifySearchTextChanged();
            }
        
            @Override
            public void changedUpdate(DocumentEvent e) {
                notifySearchTextChanged();
            }
        });

        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                int endIdx = searchField.getText().length();
                searchField.setSelectionStart(endIdx);
                searchField.setSelectionEnd(endIdx);

                if (onFocusGained != null) {
                    onFocusGained.run();
                }
            }
            
            public void focusLost(FocusEvent evt) {
                if (!evt.isTemporary() && !resultsPopup.isVisible()) {
                    resultsPopup.setVisible(false);
                }
                
                if (!evt.isTemporary() && onFocusLost != null) {
                    onFocusLost.run();
                }
            }
        });
    }

    public void setOnFocusGained(Runnable callback) {
        onFocusGained = callback;
    }

    public void setOnFocusLost(Runnable callback) {
        onFocusLost = callback;
    }

    public void setOnSearchTextChanged(Consumer<String> consumer) {
        onSearchTextChanged = consumer;
    }

    public void setOnResultSelected(Consumer<Pair<?, String>> consumer) {
        onResultSelected = consumer;
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public JTextField getSearchField() {
        return searchField;
    }

    private void notifySearchTextChanged() {
        if (onSearchTextChanged != null) {
            onSearchTextChanged.accept(searchField.getText());
        }
    }

    public void showResults(List<Pair<?, String>> matches) {
        resultsPopup.removeAll();
        
        if (matches == null || matches.isEmpty()) {
            JLabel noResults = new JLabel("No matches found");
            noResults.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            noResults.setForeground(Color.GRAY);
            resultsPopup.add(noResults);
        } else {
            // Create a panel to hold all result buttons
            JPanel resultsPanel = new JPanel();
            resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
            resultsPanel.setBackground(Color.WHITE);
            
            for (Pair<?, String> match : matches) {
                String matchStr = match.getFirst().toString().contains(".") ? match.getFirst().toString() : match.getSecond() + "." + match.getFirst().toString() + " (shared)";
                JButton resultButton = new JButton(matchStr);

                resultButton.setHorizontalAlignment(SwingConstants.LEFT);
                resultButton.setBorderPainted(false);
                resultButton.setBackground(Color.WHITE);
                resultButton.setFocusable(false); 
                
                resultButton.addActionListener(e -> {
                    if (onResultSelected != null) {
                        onResultSelected.accept(match);
                    }

                    resultsPopup.setVisible(false);
                });
            
                resultButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        resultButton.setBackground(new Color(230, 230, 250));
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        resultButton.setBackground(Color.WHITE);
                    }
                });
                
                resultButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                    resultButton.getPreferredSize().height));
                resultsPanel.add(resultButton);
            }
            
            JScrollPane scrollPane = new JScrollPane(resultsPanel);
            scrollPane.setBorder(null);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            resultsPopup.add(scrollPane);
        }
        
        if (resultsPopup.getComponentCount() > 0) {
            final int width = searchField.getWidth();
        
            int itemHeight = 26;
            int maxVisibleItems = 5;
            int itemCount = matches != null ? Math.min(maxVisibleItems, matches.size()) : 1;
            int height = itemCount * itemHeight;
            
            resultsPopup.setPreferredSize(new Dimension(width, height));
            resultsPopup.pack();
            
            resultsPopup.setLightWeightPopupEnabled(true);
            resultsPopup.show(searchField, 0, searchField.getHeight());
            searchField.requestFocusInWindow();
        } else {
            resultsPopup.setVisible(false);
        }
    }

    public void hideResults() {
        resultsPopup.setVisible(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        searchField.setEnabled(enabled);
    }

    @Override
    public boolean isFocusOwner() {
        return searchField.isFocusOwner();
    }
}
