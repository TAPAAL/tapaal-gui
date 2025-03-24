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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;

public class SearchBar extends JPanel {
    private static final String SEARCH_TOOLTIP = "Search for places and transitions in the net";

    private final JTextField searchField;
    private final JPopupMenu resultsPopup;
    private Consumer<String> onSearchTextChanged;
    private Consumer<PlaceTransitionObject> onResultSelected;

    public SearchBar() {
        super(new BorderLayout());
        
        searchField = new JTextField(15);
        searchField.setToolTipText(SEARCH_TOOLTIP);
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
            public void focusLost(FocusEvent evt) {
                if (!evt.isTemporary() && !resultsPopup.isVisible()) {
                    resultsPopup.setVisible(false);
                }
            }
        });
    }

    public void setOnSearchTextChanged(Consumer<String> consumer) {
        onSearchTextChanged = consumer;
    }

    public void setOnResultSelected(Consumer<PlaceTransitionObject> consumer) {
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

    public void showResults(List<PlaceTransitionObject> matches) {
        resultsPopup.removeAll();
        
        if (matches == null || matches.isEmpty()) {
            JLabel noResults = new JLabel("No matches found");
            noResults.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            noResults.setForeground(Color.GRAY);
            resultsPopup.add(noResults);
        } else {
            for (PlaceTransitionObject match : matches) {
                JButton resultButton = new JButton(match.getName());
                resultButton.setHorizontalAlignment(SwingConstants.LEFT);
                resultButton.setBorderPainted(false);
                resultButton.setBackground(Color.WHITE);
                resultButton.setFocusable(false); 
                
                resultButton.addActionListener(e -> {
                    if (onResultSelected != null) {
                        onResultSelected.accept(match);
                    }

                    resultsPopup.setVisible(false);
                    searchField.requestFocusInWindow();
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
                resultsPopup.add(resultButton);
            }
        }
        
        if (resultsPopup.getComponentCount() > 0) {
            int width = searchField.getWidth();
            resultsPopup.setPreferredSize(new Dimension(width, resultsPopup.getComponentCount() * 26));
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
