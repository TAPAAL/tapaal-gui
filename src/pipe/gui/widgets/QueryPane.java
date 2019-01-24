package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import pipe.gui.MessengerImpl;
import pipe.gui.Verifier;
import pipe.gui.undo.AddQueryCommand;
import pipe.gui.undo.RemoveQueriesCommand;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.QueryDialog.QueryDialogueOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.SortQueriesCommand;
import dk.aau.cs.gui.components.NonsearchableJList;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;

public class QueryPane extends JPanel {
	private static final long serialVersionUID = 4062539545170994654L;
	private JPanel queryCollectionPanel;
	private JPanel buttonsPanel;
	private DefaultListModel listModel;
	private JList queryList;
	private List<TAPNQuery> selectedQueries;
	private JScrollPane queryScroller;
	private Messenger messenger =  new MessengerImpl();

	private JButton addQueryButton;
	private JButton editQueryButton;
	private JButton verifyButton;

	private JButton removeQueryButton;
	private TabContent tabContent;
	private UndoManager undoManager;
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

	public QueryPane(ArrayList<TAPNQuery> queriesToSet,	TabContent tabContent) {
		this.tabContent = tabContent;
		undoManager = tabContent.drawingSurface().getUndoManager();
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

		queryList = new NonsearchableJList(listModel);
		queryList.setCellRenderer(new QueryCellRenderer());
		queryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		queryList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!(e.getValueIsAdjusting())) {
					queryList.ensureIndexIsVisible(queryList.getSelectedIndex());
					updateQueryButtons();
				}
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
			int minimumHegiht = QueryPane.this.getMinimumSize().height;
			public void componentShown(ComponentEvent e) {
			}

			
			public void componentResized(ComponentEvent e) {
				if(QueryPane.this.getSize().height <= minimumHegiht){
					sortButton.setVisible(false);
				} else {
					sortButton.setVisible(true);
				}
			}

			
			public void componentMoved(ComponentEvent e) {
			}

			
			public void componentHidden(ComponentEvent e) {
			}
		});

		this.setMinimumSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height - sortButton.getMinimumSize().height));
	}
	
	public void updateQueryButtons() {
		TAPNQuery query = (TAPNQuery)queryList.getSelectedValue();
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

		moveUpButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png")));
		moveUpButton.setEnabled(false);
		moveUpButton.setToolTipText(toolTipMoveUp);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = queryList.getSelectedIndex();

				if(index > 0 && queryList.getSelectedIndices().length == 1) {
					swapQueries(index, index-1);
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

		moveDownButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png")));
		moveDownButton.setEnabled(false);
		moveDownButton.setToolTipText(toolTipMoveDown);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = queryList.getSelectedIndex();

				if(index < listModel.size()-1 && queryList.getSelectedIndices().length == 1) {
					swapQueries(index, index+1);
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
		sortButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Sort.png")));
		sortButton.setToolTipText(toolTipSortQueries);
		sortButton.setEnabled(false);
		sortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Command c = new SortQueriesCommand(listModel);
				undoManager.addNewEdit(c);
				c.redo();
			}
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
		editQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(queryList.getSelectedIndices().length == 1)
					showEditDialog();
				else
					messenger.displayErrorMessage("It is only possible to edit 1 query at a time. Only verification can be done with multiple queries.");
			}
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
		verifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				verifyQuery();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(verifyButton, gbc);

		removeQueryButton = new JButton("Remove");
		removeQueryButton.setEnabled(false);
		removeQueryButton.setToolTipText(toolTipRemoveQuery);
		removeQueryButton.setPreferredSize(dimension);
		removeQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeQueries();
			}
		});
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
				int openCTLDialog = JOptionPane.YES_OPTION;
				boolean netIsUntimed = tabContent.network().isUntimed();
				String optionText = "Do you want to create a CTL query (use for untimed nets) \n or a Reachability query (use for timed nets)?";
				
				// YES_OPTION = CTL dialog, NO_OPTION = Reachability dialog
				Object[] options = {
					"CTL",
					"Reachability"};
				
				TAPNQuery q = null;
				if(netIsUntimed){
					openCTLDialog = JOptionPane.showOptionDialog(CreateGui.getApp(), optionText, "Query Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if(openCTLDialog == JOptionPane.YES_OPTION){
						q = CTLQueryDialog.showQueryDialogue(CTLQueryDialog.QueryDialogueOption.Save, null, tabContent.network(), tabContent.getGuiModels());
					} else if(openCTLDialog == JOptionPane.NO_OPTION){
						q = QueryDialog.showQueryDialogue(QueryDialogueOption.Save, null, tabContent.network(), tabContent.getGuiModels());
					}
				} else{
					q = QueryDialog.showQueryDialogue(QueryDialogueOption.Save, null, tabContent.network(), tabContent.getGuiModels());
				}
				if (q != null) {
					undoManager.addNewEdit(new AddQueryCommand(q, tabContent));
					addQuery(q);
				}
				updateQueryButtons();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(addQueryButton, gbc);
	}

	private void swapQueries(int currentIndex, int newIndex) {
		TAPNQuery temp = (TAPNQuery)listModel.get(currentIndex);
		listModel.set(currentIndex, listModel.get(newIndex));
		listModel.set(newIndex, temp);
	}
	
	private void removeQueries() {
		undoManager.addNewEdit(new RemoveQueriesCommand((List<TAPNQuery>) queryList.getSelectedValuesList(), tabContent));
		if(listModel.getSize() > 0 && isQueryPossible()){
			for(Object o : queryList.getSelectedValuesList()) {
				listModel.removeElement(o);
			}
			updateQueryButtons();
		}
	}

	public void showEditDialog() {
		int openCTLDialog = JOptionPane.YES_OPTION;
		boolean netIsUntimed = tabContent.network().isUntimed();
		String optionText = "The net is untimed and the query can be converted for the use with untimed CTL engine.\nDo you want to convert the query (recommended answer is yes if you plan to use only untimed net)?";

		// YES_OPTION = CTL dialog, NO_OPTION = Reachability dialog
		Object[] options = {
				"yes",
				"no"};

		TAPNQuery q = (TAPNQuery) queryList.getSelectedValue();
		TAPNQuery newQuery = null;

		if(q.isActive()) {
			if(netIsUntimed && q.getCategory() != TAPNQuery.QueryCategory.CTL){
				openCTLDialog = JOptionPane.showOptionDialog(CreateGui.getApp(), optionText, "Query Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if(openCTLDialog == JOptionPane.YES_OPTION){
					newQuery = CTLQueryDialog.showQueryDialogue(CTLQueryDialog.QueryDialogueOption.Save, q, tabContent.network(), tabContent.getGuiModels());
				} else if(openCTLDialog == JOptionPane.NO_OPTION){
					newQuery = QueryDialog.showQueryDialogue(QueryDialogueOption.Save, q, tabContent.network(), tabContent.getGuiModels());
				}
			} else {
				if(q.getCategory() == TAPNQuery.QueryCategory.CTL) {
					newQuery = CTLQueryDialog.showQueryDialogue(CTLQueryDialog.QueryDialogueOption.Save, q, tabContent.network(), tabContent.getGuiModels());
				} else {
					newQuery = QueryDialog.showQueryDialogue(QueryDialogueOption.Save, q, tabContent.network(), tabContent.getGuiModels());
				}
			}

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
			queries.add((TAPNQuery) listModel.get(i));
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

	private class QueryCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 3071924451912979500L;

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
			if(!isEnabled()) 
				setToolTipText("This query is disabled because it contains propositions involving places from a deactivated component");
			else
				setToolTipText("Double-click or press the edit button to edit this query");
			setFont(list.getFont());
			setOpaque(true);
			return superRenderer;
		}
	}

	public void selectFirst() {
		queryList.setSelectedIndex(0);

	}

	private void verifyQuery() {
		TAPNQuery query = (TAPNQuery) queryList.getSelectedValue();
		int NumberOfSelectedElements = queryList.getSelectedIndices().length;
		
		
		if(NumberOfSelectedElements == 1) {
			if(query.getReductionOption() == ReductionOption.VerifyTAPN || query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification || query.getReductionOption() == ReductionOption.VerifyPN)
				Verifier.runVerifyTAPNVerification(tabContent.network(), query, null, this.tabContent.getGuiModels());
			else
				Verifier.runUppaalVerification(tabContent.network(), query);
		}
		else if(NumberOfSelectedElements > 1) {
			saveNetAndRunBatchProcessing();
		}
	}
	
	private void saveNetAndRunBatchProcessing() {
		getSelectedQueriesForProcessing();
		//Saves the net in a temporary file which is used in batchProcessing
		//File is deleted on exit
		try {
			tempFile = File.createTempFile(CreateGui.getAppGui().getCurrentTabName(), ".xml");
			CreateGui.getAppGui().saveNet(CreateGui.getApp().getSelectedTabIndex(), tempFile, selectedQueries);
			BatchProcessingDialog.showBatchProcessingDialog(queryList);
			tempFile.deleteOnExit();
			if(tempFile == null) {
				throw new IOException();
			}
		}catch(IOException e) {
			messenger.displayErrorMessage("Creation of temporary file needed for verification failed.");
		}
	}
	
	private void getSelectedQueriesForProcessing() {
		selectedQueries = queryList.getSelectedValuesList();
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
}
