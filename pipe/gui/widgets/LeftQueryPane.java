package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import pipe.gui.Verifier;
import pipe.gui.widgets.QueryDialogue.QueryDialogueOption;

public class LeftQueryPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4062539545170994654L;
	private JPanel queryCollectionPanel;
	private JPanel buttonsPanel;
	private DefaultListModel listModel;
	private JList queryList;
	private JSplitPane splitPane;
	private JScrollPane queryScroller;
	
	private JButton addQueryButton;
	private JButton editQueryButton;
	private JButton verifyButton;
	
	private JButton removeQueryButton;
	
	public LeftQueryPane(ArrayList<TAPNQuery> queriesToSet) {		
		queryCollectionPanel = new JPanel(new BorderLayout());
		buttonsPanel = new JPanel(new GridLayout(2,1));
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
						removeQueryButton.setEnabled(false);

					} else {
						editQueryButton.setEnabled(true);
						verifyButton.setEnabled(true);
						removeQueryButton.setEnabled(true);
					}
				}
			}
		});
		queryList.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(!queryList.isSelectionEmpty()){
					if(arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2){
						int index = queryList.locationToIndex(arg0.getPoint());
					    queryList.ensureIndexIsVisible(index);						
						
						showEditDialog();
					}	
				}				
			}
		});
	
		addQueriesComponents();
		addButtons();
		
		splitPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, queryCollectionPanel, buttonsPanel);
		setLayout(new BorderLayout());
				
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setDividerLocation(0.8);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Queries"), 
				BorderFactory.createEmptyBorder(3,3,3,3))
			);
		
		showQueries();
	}

	private void addQueriesComponents() {
		queryScroller = new JScrollPane(queryList);
		queryCollectionPanel.add(queryScroller, BorderLayout.CENTER);
	}

	private void addButtons() {
		JPanel p1 = new JPanel();
		editQueryButton = new JButton("Edit");
		editQueryButton.setEnabled(false);
		Dimension dimension = new Dimension(82,23);
		editQueryButton.setPreferredSize(dimension);
		editQueryButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showEditDialog();
			}
		});
		p1.add(editQueryButton);
		
		verifyButton = new JButton("Verify");
		verifyButton.setEnabled(false);
		verifyButton.setPreferredSize(dimension);
		verifyButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TAPNQuery q = (TAPNQuery)queryList.getSelectedValue();
				Verifier.runUppaalVerification(CreateGui.getModel(), q);
			}			
		});
		p1.add(verifyButton);
		buttonsPanel.add(p1);
		
		JPanel p2 = new JPanel();
		removeQueryButton = new JButton("Remove");
		removeQueryButton.setEnabled(false);
		removeQueryButton.setPreferredSize(dimension);
		removeQueryButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TAPNQuery q = (TAPNQuery)queryList.getSelectedValue();	
				CreateGui.getModel().getQueries().remove(q);
				showQueries();
			}
		});
		p2.add(removeQueryButton);
		
		addQueryButton = new JButton("New..");
		addQueryButton.setPreferredSize(dimension);
		addQueryButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TAPNQuery q = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.Save, null);
				if (q != null){
					addQuery(q);
					showQueries();
				}
			}
		});

		p2.add(addQueryButton);
		buttonsPanel.add(p2);
	}
		
	private void showEditDialog() {
		TAPNQuery q = (TAPNQuery)queryList.getSelectedValue();
		TAPNQuery newQuery = QueryDialogue.ShowUppaalQueryDialogue(QueryDialogueOption.Save, q);
		
		if(newQuery != null)
			updateQuery(q, newQuery);
		
		showQueries();
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
}
