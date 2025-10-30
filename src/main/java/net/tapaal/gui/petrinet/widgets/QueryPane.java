package net.tapaal.gui.petrinet.widgets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.tapaal.gui.petrinet.undo.MoveElementDownCommand;
import net.tapaal.gui.petrinet.undo.MoveElementUpCommand;
import net.tapaal.resourcemanager.ResourceManager;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import pipe.gui.MessengerImpl;
import net.tapaal.gui.petrinet.dialog.QueryDialog;
import net.tapaal.gui.petrinet.verification.Verifier;
import net.tapaal.gui.petrinet.undo.AddQueryCommand;
import net.tapaal.gui.petrinet.undo.RemoveQueriesCommand;
import pipe.gui.petrinet.undo.UndoManager;
import net.tapaal.gui.petrinet.dialog.QueryDialog.QueryDialogueOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.dialog.BatchProcessingDialog;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.SortQueriesCommand;
import net.tapaal.gui.swingcomponents.NonsearchableJList;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;
import javax.swing.JOptionPane;

public class QueryPane extends JPanel implements SidePane {

	private final JPanel queryCollectionPanel;
	private final JPanel buttonsPanel;

	private final DefaultListModel<TAPNQuery> listModel;
	private final JList<TAPNQuery> queryList;
    private JScrollPane queryScroller;
	private final Messenger messenger =  new MessengerImpl();

	private JButton addQueryButton;
	private JButton editQueryButton;
	private JButton verifyButton;

	private JButton removeQueryButton;
	private final PetriNetTab tabContent;
	private final UndoManager undoManager;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton sortButton;
	private static File tempFile;

	private static final String toolTipNewQuery = "Create a new query";
	private static final String toolTipEditQuery="Edit the selected query";
	private static final String toolTipRemoveQuery="Remove the selected query";
	private static final String toolTipVerifyQuery="Verify the selected query";
	private static final String toolTipSortQueries="Sort the queries alphabetically";
	private final static String toolTipMoveUp = "Move the selected query up; only one query can be moved at a time";
	private final static String toolTipMoveDown = "Move the selected query down; only one query can be moved at a time";

	//private static final String toolTipQueryPane = "Here you can manage queries. Queries can explore properties of the Net.";

	public QueryPane(PetriNetTab tabContent) {
		this.tabContent = tabContent;
		undoManager = tabContent.getUndoManager();
		queryCollectionPanel = new JPanel(new GridBagLayout());
		buttonsPanel = new JPanel(new GridBagLayout());
		listModel = new DefaultListModel();

		listModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent arg0) {
			}

			public void intervalAdded(ListDataEvent arg0) {
				queryList.setSelectedIndex(arg0.getIndex0());
				queryList.ensureIndexIsVisible(arg0.getIndex0());
			}

			public void intervalRemoved(ListDataEvent arg0) {
				int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
				queryList.setSelectedIndex(index);
				queryList.ensureIndexIsVisible(index);
			}
		});

		queryList = new NonsearchableJList<>(listModel);
		queryList.setCellRenderer(new QueryCellRenderer());
		queryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		queryList.addListSelectionListener(e -> {
			if (!(e.getValueIsAdjusting())) {
				queryList.ensureIndexIsVisible(queryList.getSelectedIndex());
				updateQueryButtons();
			}
		});
		queryList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!queryList.isSelectionEmpty()) {
					if (arg0.getButton() == MouseEvent.BUTTON1
							&& arg0.getClickCount() == 2) {
						int index = queryList.locationToIndex(arg0.getPoint());
						queryList.ensureIndexIsVisible(index);

						showEditDialog();
					}
				}
			}
		});

		addQueriesComponents();
		addButtons();

		setLayout(new BorderLayout());
		this.add(queryCollectionPanel, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.PAGE_END);
		//this.setToolTipText(toolTipQueryPane);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Queries"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		this.setToolTipText("List of verification queries (double click to edit)");

		this.addComponentListener(new ComponentListener() {
			final int minimumHegiht = QueryPane.this.getMinimumSize().height;
			public void componentShown(ComponentEvent e) {}

			public void componentResized(ComponentEvent e) {
                sortButton.setVisible(QueryPane.this.getSize().height > minimumHegiht);
			}

			public void componentMoved(ComponentEvent e) {}

			public void componentHidden(ComponentEvent e) {}
		});

		this.setMinimumSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height - sortButton.getMinimumSize().height));
	}
	
	public void updateQueryButtons() {
		TAPNQuery query = queryList.getSelectedValue();
		if (queryList.getSelectedIndex() == -1 || !query.isActive()) {
			editQueryButton.setEnabled(false);
			verifyButton.setEnabled(false);
			removeQueryButton.setEnabled(false);
			sortButton.setEnabled(false);
		} else {
			editQueryButton.setEnabled(true);
			verifyButton.setEnabled(true);
			removeQueryButton.setEnabled(true);
			sortButton.setEnabled(true);
		}
		if(queryList.getModel().getSize() >= 2)
			sortButton.setEnabled(true);
		else
			sortButton.setEnabled(false);
		
		int index = queryList.getSelectedIndex();
		if(index > 0 && queryList.getSelectedIndices().length == 1)
			moveUpButton.setEnabled(true);
		else
			moveUpButton.setEnabled(false);


		if(index < listModel.size()-1 && queryList.getSelectedIndices().length == 1)
			moveDownButton.setEnabled(true);
		else
			moveDownButton.setEnabled(false);
	}

	private void addQueriesComponents() {
		queryScroller = new JScrollPane(queryList);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		queryCollectionPanel.add(queryScroller, gbc);

		moveUpButton = new JButton(ResourceManager.getIcon("Up.png"));
		moveUpButton.setMargin(new Insets(2,2,2,2));
		moveUpButton.setEnabled(false);
		moveUpButton.setToolTipText(toolTipMoveUp);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = queryList.getSelectedIndex();

				if(index > 0 && queryList.getSelectedIndices().length == 1) {
                    Command c = new MoveElementUpCommand(QueryPane.this, index, index-1);
                    undoManager.addNewEdit(c);
                    c.redo();
					queryList.setSelectedIndex(index-1);
				}
				else
					moveUpButton.setEnabled(false);
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.SOUTH;
		queryCollectionPanel.add(moveUpButton,gbc);

		moveDownButton = new JButton(ResourceManager.getIcon("Down.png"));
		moveDownButton.setMargin(new Insets(2,2,2,2));
		moveDownButton.setEnabled(false);
		moveDownButton.setToolTipText(toolTipMoveDown);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                int index = queryList.getSelectedIndex();

                if(index < listModel.size()-1 && queryList.getSelectedIndices().length == 1) {
                    Command c = new MoveElementDownCommand(QueryPane.this, index, index+1);
                    undoManager.addNewEdit(c);
                    c.redo();
                    queryList.setSelectedIndex(index+1);
                }
                else
                    moveDownButton.setEnabled(false);

			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		queryCollectionPanel.add(moveDownButton,gbc);

		//Sort button
		sortButton = new JButton(ResourceManager.getIcon("Sort.png"));
		sortButton.setMargin(new Insets(2,2,2,2));
		sortButton.setToolTipText(toolTipSortQueries);
		sortButton.setEnabled(false);
		sortButton.addActionListener(e -> {
			Command c = new SortQueriesCommand(listModel);
			undoManager.addNewEdit(c);
			c.redo();
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		queryCollectionPanel.add(sortButton,gbc);
	}

	private void addButtons() {
		editQueryButton = new JButton("Edit");
		editQueryButton.setEnabled(false);
		editQueryButton.setToolTipText(toolTipEditQuery);
		Dimension dimension = new Dimension(82, 23);
		editQueryButton.setPreferredSize(dimension);
		editQueryButton.addActionListener(e -> {
			if(queryList.getSelectedIndices().length == 1)
				showEditDialog();
			else
				messenger.displayErrorMessage("It is only possible to edit 1 query at a time. Only verification can be done with multiple queries.");
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(editQueryButton, gbc);

		verifyButton = new JButton("Verify");
		verifyButton.setEnabled(false);
		verifyButton.setToolTipText(toolTipVerifyQuery);
		verifyButton.setPreferredSize(dimension);
		verifyButton.addActionListener(e -> verifyQuery());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(verifyButton, gbc);

		removeQueryButton = new JButton("Remove");
		removeQueryButton.setEnabled(false);
		removeQueryButton.setToolTipText(toolTipRemoveQuery);
		removeQueryButton.setPreferredSize(dimension);
		removeQueryButton.addActionListener(e -> removeQueries());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(removeQueryButton, gbc);

		addQueryButton = new JButton("New");
		addQueryButton.setPreferredSize(dimension);
		addQueryButton.setToolTipText(toolTipNewQuery);
		addQueryButton.addActionListener(new ActionListener() {

		    public void actionPerformed(ActionEvent e) {
                TimedArcPetriNetNetwork network = tabContent.network();
                TAPNLens lens = tabContent.getLens();

                if (lens.isStochastic() && !network.isNonStrict()) {
                    JOptionPane.showMessageDialog(null, "SMC queries are only allowed for models with nonstrict intervals.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

				QueryDialog.showQueryDialogue(QueryDialogueOption.Save, null, network, tabContent.getGuiModels(), lens, tabContent);

				updateQueryButtons();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(addQueryButton, gbc);
	}

    public UndoManager getUndoManager() {
        return undoManager;
    }

	private void swapQueries(int currentIndex, int newIndex) {
		TAPNQuery temp = listModel.get(currentIndex);
		listModel.set(currentIndex, listModel.get(newIndex));
		listModel.set(newIndex, temp);
	}
	
	private void removeQueries() {
		undoManager.addNewEdit(new RemoveQueriesCommand(queryList.getSelectedValuesList(), tabContent));
		if(listModel.getSize() > 0 && isQueryPossible()){
			for(Object o : queryList.getSelectedValuesList()) {
				listModel.removeElement(o);
			}
			updateQueryButtons();
		}
	}

	public void showEditDialog() {
		TAPNQuery q = queryList.getSelectedValue();
		TAPNQuery newQuery;

		if(q.isActive()) {
            newQuery = QueryDialog.showQueryDialogue(QueryDialogueOption.Save, q, tabContent.network(), tabContent.getGuiModels(), tabContent.getLens(), tabContent);

			if (newQuery != null)
				updateQuery(q, newQuery);
		}
	}

	public void addQuery(TAPNQuery query) {
		listModel.addElement(query);
	}

	private void updateQuery(TAPNQuery oldQuery, TAPNQuery newQuery) {
		newQuery.setActive(oldQuery.isActive());
		listModel.set(listModel.indexOf(oldQuery), newQuery);
	}

	public Iterable<TAPNQuery> getQueries() {
		ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();

		for (int i = 0; i < listModel.size(); ++i) {
			queries.add(listModel.get(i));
		}

		return queries;
	}

	public void setQueries(Iterable<TAPNQuery> queries) {
		Require.that(queries != null, "Queries cannot be null");

		listModel.removeAllElements();

		for (TAPNQuery query : queries) {
			listModel.addElement(query);
		}
	}

	public void removeQuery(TAPNQuery queryToRemove) {
		listModel.removeElement(queryToRemove);
	}

	private static class QueryCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component superRenderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if(value instanceof TAPNQuery)
				setText(((TAPNQuery)value).getName());
			else
				setText("");
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			setEnabled(list.isEnabled() && ((TAPNQuery)value).isActive());

			if(!isEnabled()){
                setToolTipText("This query is disabled because it contains propositions involving places from a deactivated component");
            } else {
			    String queryToolTipString = getFormattedQueryToolTipString(((TAPNQuery)value).getQuery());
                setToolTipText(queryToolTipString);
            }

			setFont(list.getFont());
			setOpaque(true);
			return superRenderer;
		}

        private String getFormattedQueryToolTipString(String qString) {
		    int stringLength = qString.length();
		    int newLineAt = 100;
		    if (stringLength > newLineAt) {
		        int numOfLineBreaks = (int)Math.floor(stringLength / newLineAt);

                StringBuilder sb = new StringBuilder(qString);
                sb.insert(0, "<html>");

		        for(int i = 1; i <= numOfLineBreaks; i++) {
		            int newLineIndex =  sb.indexOf(" ", newLineAt * i);

                    if (newLineIndex > 0) {
                        sb.insert(newLineIndex, "<br>");
                    }
                }

                sb.insert(sb.length(), "</html>");
		        return sb.toString();
            }
            return qString;
        }
	}

	public void selectFirst() {
		queryList.setSelectedIndex(0);

	}

	private void verifyQuery() {
		TAPNQuery query = queryList.getSelectedValue();
		int NumberOfSelectedElements = queryList.getSelectedIndices().length;
		
		boolean isSmc = query.getCategory() == QueryCategory.SMC;

		if (isSmc && !tabContent.network().isNonStrict()) {
			JOptionPane.showMessageDialog(null, "The model has strict intervals and can therefore not be verified", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (NumberOfSelectedElements == 1) {
			if (query.getReductionOption() == ReductionOption.VerifyTAPN || query.getReductionOption() == ReductionOption.VerifyDTAPN || query.getReductionOption() == ReductionOption.VerifyPN)
				Verifier.runVerifyTAPNVerification(tabContent.network(), query, null, tabContent.getGuiModels(), false, tabContent.lens);
			else
				Verifier.runUppaalVerification(tabContent.network(), query);
		} else if (NumberOfSelectedElements > 1) {
			saveNetAndRunBatchProcessing();
		}
	}
	
	private void saveNetAndRunBatchProcessing() {
        List<TAPNQuery> selectedQueries = queryList.getSelectedValuesList();
        //Saves the net in a temporary file which is used in batchProcessing File is deleted on exit
		try {
			tempFile = File.createTempFile(tabContent.getTabTitle(), ".xml");

            tabContent.writeNetToFile(tempFile, selectedQueries, tabContent.getLens());
			//XXX is it not an error that the tempFile is not passed down to the batchProcessing?
            // I would think it runs the query on the unsaved net -- kyrke 2022-02-21
            BatchProcessingDialog.showBatchProcessingDialog(queryList);
			tempFile.deleteOnExit();
			if (tempFile == null) {
				throw new IOException();
			}
		} catch(IOException e) {
			messenger.displayErrorMessage("Creation of temporary file needed for verification failed.");
		}
	}

    public static File getTemporaryFile() {
		return tempFile;
	}
	
	public boolean isQueryPossible() {
		return (queryList.getModel().getSize() > 0 );
	}

	public void verifySelectedQuery() {
		verifyQuery();
	}

	@Override
	public void moveUp(int index){
        swapQueries(index, index-1);
    }
    @Override
    public void moveDown(int index){
        swapQueries(index, index+1);
    }
    @Override
    public JList getJList(){
	    return queryList;
    }
}
