package pipe.gui.widgets;

import java.awt.BorderLayout;
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
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.Verifier;
import pipe.gui.widgets.QueryDialog.QueryDialogueOption;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;

public class LeftQueryPane extends JPanel {
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

	public LeftQueryPane(ArrayList<TAPNQuery> queriesToSet,
			TabContent tabContent) {
		this.tabContent = tabContent;
		queryCollectionPanel = new JPanel(new BorderLayout());
		buttonsPanel = new JPanel(new GridBagLayout());
		listModel = new DefaultListModel();

		queryList = new JList(listModel);
		queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queryList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (queryList.getSelectedIndex() == -1) {
						editQueryButton.setEnabled(false);
						verifyButton.setEnabled(false);
						removeQueryButton.setEnabled(false);

					} else {
						editQueryButton.setEnabled(true);
						verifyButton.setEnabled(true);
						removeQueryButton.setEnabled(true);
					}
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

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Queries"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
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
				listModel.remove(queryList.getSelectedIndex());
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(removeQueryButton, gbc);

		addQueryButton = new JButton("New..");
		addQueryButton.setPreferredSize(dimension);
		addQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TAPNQuery q = QueryDialog.ShowUppaalQueryDialogue(
						QueryDialogueOption.Save, null, tabContent.network());
				if (q != null) {
					addQuery(q);
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(addQueryButton, gbc);
	}

	private void showEditDialog() {
		TAPNQuery q = (TAPNQuery) queryList.getSelectedValue();
		TAPNQuery newQuery = QueryDialog.ShowUppaalQueryDialogue(
				QueryDialogueOption.Save, q, tabContent.network());

		if (newQuery != null)
			updateQuery(q, newQuery);
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
}
