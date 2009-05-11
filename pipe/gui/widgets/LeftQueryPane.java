package pipe.gui.widgets;

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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneLayout;
import javax.swing.text.View;

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
	private JButton newQueryButton;
	private JButton removeQueryButton;
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
		
		setLayout(new GridBagLayout());
		
		verifyLabel = new JLabel("Queries:");
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		add(verifyLabel, gbc);
		
		queryCollectionPanel = new JPanel(new GridBagLayout());

		if (queriesToSet != null && queriesToSet.size() >0){
			for (TAPNQuery queryInModel : queriesToSet){
				gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = nrOfqueriesUsed;
				queryCollectionPanel.add(createQuery(queryInModel), gbc);
			}
			gbc.gridy = nrOfqueriesUsed +1;
			queryCollectionPanel.add(createNewQuery(),gbc);			
		}else {
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			queryCollectionPanel.add(createNewQuery(),gbc);
		}
			
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(queryCollectionPanel, gbc);
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
