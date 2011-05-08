package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.Verifier;
import pipe.gui.undo.AddQueryCommand;
import pipe.gui.undo.RemoveQueryCommand;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.QueryDialog.QueryDialogueOption;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;

public class QueryPane extends JPanel {
	private static final long serialVersionUID = 4062539545170994654L;
	private JPanel queryCollectionPanel;
	private JPanel buttonsPanel;
	private DefaultListModel listModel;
	private JList queryList;
	private JScrollPane queryScroller;

	private JButton addQueryButton;
	private JButton editQueryButton;
	private JButton verifyButton;

	private JButton removeQueryButton;
	private TabContent tabContent;
	private UndoManager undoManager;
	private JButton moveUpButton;
	private JButton moveDownButton;

	public QueryPane(ArrayList<TAPNQuery> queriesToSet,	TabContent tabContent) {
		this.tabContent = tabContent;
		this.undoManager = tabContent.drawingSurface().getUndoManager();
		queryCollectionPanel = new JPanel(new BorderLayout());
		buttonsPanel = new JPanel(new GridBagLayout());
		listModel = new DefaultListModel();

		queryList = new JList(listModel);
		queryList.setCellRenderer(new QueryCellRenderer());
		queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queryList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
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

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Queries"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	}
	
	public void updateQueryButtons() {
		TAPNQuery query = (TAPNQuery)queryList.getSelectedValue();
		if (queryList.getSelectedIndex() == -1 || !query.isActive()) {
			editQueryButton.setEnabled(false);
			verifyButton.setEnabled(false);
			removeQueryButton.setEnabled(false);
		} else {
			editQueryButton.setEnabled(true);
			verifyButton.setEnabled(true);
			removeQueryButton.setEnabled(true);
		}
		int index = queryList.getSelectedIndex();
		if(index > 0)
			moveUpButton.setEnabled(true);
		else
			moveUpButton.setEnabled(false);
				
			
		if(index < listModel.size()-1)
			moveDownButton.setEnabled(true);
		else
			moveDownButton.setEnabled(false);
	}

	private void addQueriesComponents() {
		queryScroller = new JScrollPane(queryList);
		queryCollectionPanel.add(queryScroller, BorderLayout.CENTER);
	}

	private void addButtons() {
		editQueryButton = new JButton("Edit");
		editQueryButton.setEnabled(false);
		Dimension dimension = new Dimension(82, 23);
		editQueryButton.setPreferredSize(dimension);
		editQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showEditDialog();
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(editQueryButton, gbc);

		verifyButton = new JButton("Verify");
		verifyButton.setEnabled(false);
		verifyButton.setPreferredSize(dimension);
		verifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TAPNQuery query = (TAPNQuery) queryList.getSelectedValue();
				
				if(query.getReductionOption() == ReductionOption.VerifyTAPN)
					Verifier.runVerifyTAPNVerification(tabContent.network(), query);
				else
					Verifier.runUppaalVerification(tabContent.network(), query);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(verifyButton, gbc);

		removeQueryButton = new JButton("Remove");
		removeQueryButton.setEnabled(false);
		removeQueryButton.setPreferredSize(dimension);
		removeQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TAPNQuery query = (TAPNQuery) queryList.getSelectedValue();
				undoManager.addNewEdit(new RemoveQueryCommand(query, tabContent));
				listModel.remove(queryList.getSelectedIndex());
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(removeQueryButton, gbc);

		addQueryButton = new JButton("New");
		addQueryButton.setPreferredSize(dimension);
		addQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TAPNQuery q = QueryDialog.showQueryDialogue(QueryDialogueOption.Save, null, tabContent.network());
				if (q != null) {
					undoManager.addNewEdit(new AddQueryCommand(q, tabContent));
					addQuery(q);
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(addQueryButton, gbc);
		
		moveUpButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png")));
		moveUpButton.setEnabled(false);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = queryList.getSelectedIndex();
				
				if(index > 0) {
					swapQueries(index, index-1);
					queryList.setSelectedIndex(index-1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		buttonsPanel.add(moveUpButton,gbc);
		
		moveDownButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png")));
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = queryList.getSelectedIndex();
				
				if(index < listModel.size()-1) {
					swapQueries(index, index+1);
					queryList.setSelectedIndex(index+1);
				}
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		buttonsPanel.add(moveDownButton,gbc);
	}
	
	private void swapQueries(int currentIndex, int newIndex) {
		TAPNQuery temp = (TAPNQuery)listModel.get(currentIndex);
		listModel.set(currentIndex, listModel.get(newIndex));
		listModel.set(newIndex, temp);
	}

	private void showEditDialog() {
		TAPNQuery q = (TAPNQuery) queryList.getSelectedValue();
		if(q.isActive()) {
			TAPNQuery newQuery = QueryDialog.showQueryDialogue(
					QueryDialogueOption.Save, q, tabContent.network());
	
			if (newQuery != null)
				updateQuery(q, newQuery);
		}
	}

	public void addQuery(TAPNQuery query) {
		listModel.addElement(query);
	}

	private void updateQuery(TAPNQuery oldQuery, TAPNQuery newQuery) {
		oldQuery.set(newQuery);
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
	
	private class QueryCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 3071924451912979500L;

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
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
			return this;
		}
	}
}
