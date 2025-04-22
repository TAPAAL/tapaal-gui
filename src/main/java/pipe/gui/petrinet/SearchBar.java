package pipe.gui.petrinet;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Pair;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
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
    private List<Pair<?, String>> currentMatches;
    private int maxVisibleItems = 10;
    private boolean useSharedPrefix = true;
    
    public SearchBar() {
        this("");
    }

    public SearchBar(String label) {
        super(new BorderLayout());
       
        searchLabel = new JLabel(label);
        
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

        // Select first match on enter
        searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "selectFirstMatch");
        searchField.getActionMap().put("selectFirstMatch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resultsPopup.isVisible() && currentMatches != null && !currentMatches.isEmpty()) {
                    if (onResultSelected != null) {
                        onResultSelected.accept(currentMatches.get(0));
                    }

                    resultsPopup.setVisible(false);
                }
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
                if (!evt.isTemporary() && onFocusLost != null) {
                    resultsPopup.setVisible(false);
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

    public void useSharedPrefix(boolean useSharedPrefix) {
        this.useSharedPrefix = useSharedPrefix;
    }

    public void showResults(List<Pair<?, String>> matches) {
        currentMatches = matches;
        
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
                Object firstElem = match.getFirst();
                boolean isShared = firstElem instanceof TimedPlace && ((TimedPlace)firstElem).isShared() ||
                                   firstElem instanceof TimedTransition && ((TimedTransition) firstElem).isShared();

                String firstElemStr = firstElem.toString();
                if (isShared && firstElem instanceof TimedTransition && firstElemStr.contains(".")) {
                    firstElemStr = firstElemStr.split("\\.")[1];
                }

                String matchStr;
                if (firstElem instanceof TimedTransition) {
                    if (isShared) {
                        matchStr = (useSharedPrefix ? match.getSecond() + "." : "") + firstElemStr + " (shared transition)";
                    } else {
                        matchStr = firstElemStr + " (transition)";
                    }
                } else {
                    if (isShared) {
                        matchStr = (useSharedPrefix ? match.getSecond() + "." : "") + firstElemStr + " (shared place)";
                    } else {
                        matchStr = firstElemStr + " (place)";
                    }
                }
                
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

    public void clear() {
        searchField.setText("");
    }

    @Override
    public boolean requestFocusInWindow() {
        return searchField.requestFocusInWindow();
    }

    public void setMaxVisibleItems(int maxVisibleItems) {
        this.maxVisibleItems = maxVisibleItems;
    }
}
