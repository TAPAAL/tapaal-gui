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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Pair;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class SearchBar extends JPanel {
    private static final String SEARCH_TOOLTIP = "Search for places and transitions in the net";
    private static final int MAX_VISIBLE_ITEMS = 10;
    private static final Color HIGHLIGHT_COLOR = new Color(230, 230, 250);

    private final JLabel searchLabel;
    private final JTextField searchField;
    private final JPopupMenu resultsPopup;
    private Consumer<String> onSearchTextChanged;
    private Consumer<Pair<?, String>> onResultSelected;
    private Runnable onFocusGained;
    private Runnable onFocusLost;
    private List<Pair<?, String>> currentMatches;
    private List<JButton> resultButtons = new ArrayList<>();
    private int selectedIndex = -1;
    
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

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (resultsPopup.isVisible() && !resultButtons.isEmpty()) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        selectNextMatch();
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        selectPreviousMatch();
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (currentMatches != null && !currentMatches.isEmpty()) {
                            if (selectedIndex >= 0 && selectedIndex < currentMatches.size()) {
                                if (onResultSelected != null) {
                                    onResultSelected.accept(currentMatches.get(selectedIndex));
                                }
                            } else if (!currentMatches.isEmpty()) {
                                if (onResultSelected != null) {
                                    onResultSelected.accept(currentMatches.get(0));
                                }
                            }
                        }

                        e.consume();
                    }
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
                if (!evt.isTemporary()) {
                    hideResults();
                    if (onFocusLost != null) {
                        onFocusLost.run();
                    }
                }
            }
        });
    }

    private void selectMatch(int direction) {
        if (resultButtons.isEmpty()) return;
    
        if (selectedIndex >= 0 && selectedIndex < resultButtons.size()) {
            resultButtons.get(selectedIndex).setBackground(Color.WHITE);
        }
        
        selectedIndex = (selectedIndex + direction + resultButtons.size()) % resultButtons.size();
        
        JButton selected = resultButtons.get(selectedIndex);
        selected.setBackground(HIGHLIGHT_COLOR);
        
        JScrollPane scrollPane = null;
        if (resultsPopup.getComponentCount() > 0 && resultsPopup.getComponent(0) instanceof JScrollPane) {
            scrollPane = (JScrollPane)resultsPopup.getComponent(0);
        }
        
        if (scrollPane != null) {
            Rectangle viewRect = scrollPane.getViewport().getViewRect();
            Rectangle buttonBounds = selected.getBounds();
            if (buttonBounds.y < viewRect.y) {
                scrollPane.getVerticalScrollBar().setValue(buttonBounds.y);
            } else if (buttonBounds.y + buttonBounds.height > viewRect.y + viewRect.height) {
                int newValue = buttonBounds.y + buttonBounds.height - viewRect.height;
                scrollPane.getVerticalScrollBar().setValue(newValue);
            }
        }
    }
    
    private void selectNextMatch() {
        selectMatch(1);
    }
    
    private void selectPreviousMatch() {
        selectMatch(-1);
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
        currentMatches = matches;
        resultButtons.clear();
        selectedIndex = -1;
        
        resultsPopup.removeAll();
        
        final int width = searchField.getWidth();
        
        if (matches == null || matches.isEmpty()) {
            JPanel noResultsPanel = new JPanel(new BorderLayout());
            noResultsPanel.setBackground(Color.WHITE);
            
            JLabel noResults = new JLabel("No matches found");
            noResults.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            noResults.setForeground(Color.GRAY);
            
            noResultsPanel.add(noResults, BorderLayout.WEST);
            resultsPopup.add(noResultsPanel);
            
            resultsPopup.setPreferredSize(new Dimension(width, 30));
        } else {
            JPanel resultsPanel = new JPanel();
            resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
            resultsPanel.setBackground(Color.WHITE);
            
            for (int i = 0; i < matches.size(); i++) {
                Pair<?, String> match = matches.get(i);
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
                        matchStr = match.getSecond() + "." + firstElemStr + " (shared transition)";
                    } else {
                        matchStr = firstElemStr + " (transition)";
                    }
                } else {
                    if (isShared) {
                        matchStr = match.getSecond() + "." + firstElemStr + " (shared place)";
                    } else {
                        matchStr = firstElemStr + " (place)";
                    }
                }
                
                JButton resultButton = new JButton(matchStr);
                resultButtons.add(resultButton);
    
                resultButton.setHorizontalAlignment(SwingConstants.LEFT);
                resultButton.setBorderPainted(false);
                resultButton.setBackground(Color.WHITE);
                resultButton.setFocusable(false);

                resultButton.addActionListener(e -> {
                    if (onResultSelected != null) {
                        onResultSelected.accept(match);
                    }
                });
            
                final int index = i;
                resultButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (selectedIndex >= 0 && selectedIndex < resultButtons.size()) {
                            resultButtons.get(selectedIndex).setBackground(Color.WHITE);
                        }
                        
                        resultButton.setBackground(HIGHLIGHT_COLOR);
                        selectedIndex = index;
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (selectedIndex != index) {
                            resultButton.setBackground(Color.WHITE);
                        }
                    }
                });
                
                resultButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                    resultButton.getPreferredSize().height));
                resultsPanel.add(resultButton);
            }
            
            int itemHeight = resultButtons.get(0).getPreferredSize().height + 1;
            int itemCount = matches.size();
            
            // Determine if we need scrolling
            boolean needsScrolling = itemCount > MAX_VISIBLE_ITEMS;
            int visibleItems = needsScrolling ? MAX_VISIBLE_ITEMS : itemCount;
            int height = visibleItems * itemHeight;
            
            if (needsScrolling) {
                JScrollPane scrollPane = new JScrollPane(resultsPanel);
                scrollPane.setBorder(null);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                
                resultsPopup.add(scrollPane);
            } else {
                resultsPopup.add(resultsPanel);
            }
            
            resultsPopup.setPreferredSize(new Dimension(width, height));
        }
        
        if (resultsPopup.getComponentCount() > 0) {
            resultsPopup.pack();
            resultsPopup.show(searchField, 0, searchField.getHeight());

            SwingUtilities.invokeLater(() -> {
                searchField.requestFocusInWindow();
                if (!resultButtons.isEmpty()) {
                    selectedIndex = 0;
                    resultButtons.get(0).setBackground(HIGHLIGHT_COLOR);
                    resultButtons.get(0).repaint();
                }
            });
        } else {
            hideResults();
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
}