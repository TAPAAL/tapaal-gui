package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.View;

import pipe.dataLayer.Constant;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.Export;
import pipe.gui.ModuleManager;
import pipe.gui.Verification;
import pipe.gui.widgets.QueryDialogue.QueryDialogueOption;

public class LeftQueryPane extends JPanel {
	private JPanel queryCollectionPanel;
	private JPanel buttonsPanel;
	private DefaultListModel listModel;
	private JList queryList;
	private JSplitPane splitPane;
	private JScrollPane queryScroller;
	
	private JButton addQueryButton;
	private JButton editQueryButton;
	private JButton verifyButton;
	
//	private JButton newQueryButton;
//	private JButton removeQueryButton;
	private JLabel verifyLabel;
	
	private HashMap<JPanel, TAPNQuery> queryMap;
	private ArrayList<JPanel> queries;
	private int nrOfqueriesUsed;
	private boolean extraUnusedQuery;
	
	public LeftQueryPane(ArrayList<TAPNQuery> queriesToSet) {
		queries = new ArrayList<JPanel>();
		queryMap = new HashMap<JPanel, TAPNQuery>();
		nrOfqueriesUsed = 0;
		extraUnusedQuery = true;	
		
		queryCollectionPanel = new JPanel(new BorderLayout());
		buttonsPanel = new JPanel();
		
		listModel = new DefaultListModel();
		
		queryList = new JList(listModel);
		queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queryList.setVisibleRowCount(-1);
		queryList.setLayoutOrientation(JList.VERTICAL);
		queryList.setAlignmentX(Component.LEFT_ALIGNMENT);
		queryList.setAlignmentY(Component.TOP_ALIGNMENT);
		queryList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				if (e.getValueIsAdjusting() == false) {
					if (queryList.getSelectedIndex() == -1) {
						editQueryButton.setEnabled(false);
						verifyButton.setEnabled(false);

					} else {
						editQueryButton.setEnabled(true);
						verifyButton.setEnabled(true);
					}
				}
			}
		});
//		
		addConstantsComponents();
		addButtons();
//		
		splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, queryCollectionPanel, buttonsPanel);
		setLayout(new BorderLayout());
				
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setDividerLocation(0.9);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
		
		
//		setLayout(new GridBagLayout());
//		
//		verifyLabel = new JLabel("Queries:");
//		
//		GridBagConstraints gbc = new GridBagConstraints();
//		gbc.gridx = 0;
//		gbc.gridy = 0;
//		gbc.anchor = GridBagConstraints.WEST;
//		add(verifyLabel, gbc);
//		
//		queryCollectionPanel = new JPanel(new GridBagLayout());
//
//		if (queriesToSet != null && queriesToSet.size() >0){
//			for (TAPNQuery queryInModel : queriesToSet){
//				gbc = new GridBagConstraints();
//				gbc.gridx = 0;
//				gbc.gridy = nrOfqueriesUsed;
//				queryCollectionPanel.add(createQuery(queryInModel), gbc);
//			}
//			gbc.gridy = nrOfqueriesUsed +1;
//			queryCollectionPanel.add(createNewQuery(),gbc);			
//		}else {
//			gbc = new GridBagConstraints();
//			gbc.gridx = 0;
//			gbc.gridy = 0;
//			queryCollectionPanel.add(createNewQuery(),gbc);
//		}
//			
//		gbc = new GridBagConstraints();
//		gbc.gridx = 0;
//		gbc.gridy = 1;
//		add(queryCollectionPanel, gbc);
	}

	private void addConstantsComponents() {
		verifyLabel = new JLabel("Queries:");
		verifyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		verifyLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		queryCollectionPanel.add(verifyLabel, BorderLayout.PAGE_START);
		
		queryScroller = new JScrollPane(queryList);
		queryCollectionPanel.add(queryScroller, BorderLayout.CENTER);
		
	}

	private void addButtons() {
		editQueryButton = new JButton("Edit");
		editQueryButton.setEnabled(false);
		editQueryButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TAPNQuery q = (TAPNQuery)queryList.getSelectedValue();
				TAPNQuery newQuery = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.Save, q);
				updateQuery(q, newQuery);
			}
		});
		buttonsPanel.add(editQueryButton);
		
		verifyButton = new JButton("Verify");
		verifyButton.setEnabled(false);
		verifyButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TAPNQuery q = (TAPNQuery)queryList.getSelectedValue();
				Verification.runUppaalVerification(CreateGui.getModel(), q, false);
			}			
		});
		buttonsPanel.add(verifyButton);
		
		addQueryButton = new JButton("New Query..");
		addQueryButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TAPNQuery q = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.Save, null);
				addQuery(q);
				showQueries();
			}
		});
		buttonsPanel.add(addQueryButton);
	}
		
	private void showQueries() {
		DataLayer model = CreateGui.getModel();
		if(model == null) return;
		
		listModel.removeAllElements();
		addQueriesToPanel(model);
		queryList.validate();
		
	}
	
	private void addQueriesToPanel(DataLayer model) {
		for(TAPNQuery query : model.getQueries())
		{
			listModel.addElement(query);
		}
	}
	
	private void addQuery(TAPNQuery query) {
		DataLayer model = CreateGui.getModel();
		model.addQuery(query);		
	}

	private void updateQuery(TAPNQuery oldQuery, TAPNQuery newQuery) {
		oldQuery.set(newQuery);	
	}
	
	private JPanel createQuery(TAPNQuery queryFromModel) {
		JPanel toReturn = createNewQuery();
		((JTextField) toReturn.getComponent(1)).setText(queryFromModel.name);
		((JTextField) toReturn.getComponent(1)).setCaretPosition(0);
		toReturn.getComponent(3).setEnabled(true);
		queryMap.put(toReturn, queryFromModel);
		queries.add(toReturn);
		return toReturn;
	}

	private JPanel createNewQuery(){
		final JPanel toReturn = new JPanel(new FlowLayout());
		toReturn.add(new JLabel("    "));
		final JTextField query = new JTextField("New query", 11);
		query.setEditable(false);
		query.setFocusable(false);
		query.setCaretPosition(0);
		toReturn.add(query);
		JButton editButton = new JButton("Edit");
		JButton verifyButton = new JButton("Verify");
		verifyButton.addActionListener(new ActionListener(){
			
			JPanel thisPanel = toReturn;
			
			public void actionPerformed(ActionEvent e) {
				Verification.runUppaalVerification(CreateGui.getModel(), queryMap.get(thisPanel), false);
			}
			
		});
		verifyButton.setEnabled(false);
		editButton.addActionListener(new ActionListener(){
			
			JPanel thisPanel = toReturn;
			
			public void actionPerformed(ActionEvent e) {
				TAPNQuery inputQuery;
				if (! queryMap.containsKey(thisPanel)){
					inputQuery = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.Save, null);
					thisPanel.getComponent(3).setEnabled(true);
					extraUnusedQuery = false;
					if (inputQuery!=null){
						queries.add(thisPanel);
					}
				}else{
					inputQuery = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.Save, queryMap.get(thisPanel));
				}
				if (inputQuery == null){
					if (! queryMap.containsKey(thisPanel)){
						thisPanel.getComponent(3).setEnabled(false);
					}
					return;
				}else{
					if (queryMap.containsKey(thisPanel)){
						queryMap.remove(thisPanel);
						queryMap.put(thisPanel, inputQuery );
					}else{
						queryMap.put(thisPanel, inputQuery );
					}

					ArrayList<TAPNQuery> queriesToSet = new ArrayList<TAPNQuery>();
					for (JPanel queryPanel : queries){
						queriesToSet.add( queryMap.get(queryPanel) );
					}
					CreateGui.getModel().setQueries(queriesToSet);
					query.setText(inputQuery.name);
					query.setCaretPosition(0);
					
					if (!extraUnusedQuery){
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = 0;
						gbc.gridy = nrOfqueriesUsed+1;
						queryCollectionPanel.add(createNewQuery(), gbc);
						extraUnusedQuery = true;
					}
					CreateGui.updateLeftPanel();
				}

			}
			
		});
		
		toReturn.add(editButton);
		toReturn.add(verifyButton);
		nrOfqueriesUsed++;
		return toReturn;
	}
}
