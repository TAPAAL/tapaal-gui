package pipe.gui.widgets;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ReductionOption;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.Export;
import pipe.gui.Pipe;
import pipe.gui.Verification;

public class QueryDialogue extends JPanel{
	/**
	 * Automatically generated
	 */
	private static final long serialVersionUID = 7852107237344005546L;
	public enum QueryDialogueOption {VerifyNow, Save, Export}
	private TAPNQuery queryToExport; 
	private JRootPane myRootPane;
	private JPanel queryPanel;
	private JPanel buttonPanel;
	private JPanel markingSpecificsPanel;
	private JPanel namePanel;
	private JPanel capacityPanel;
	private JPanel traceOptions;
	private JPanel searchOptions;
	private JPanel uppaalOptions;
	
	private JButton okButton;
	private JButton verifyButton;
	private JButton cancelButton;
	private JButton removeButton;

	private ButtonGroup quantificationRadioButtonGroup;
	private JRadioButtonMenuItem existsDiamond;
	private JRadioButtonMenuItem existsBox;
	private JRadioButtonMenuItem forAllDiamond;
	private JRadioButtonMenuItem forAllBox;	

	private ButtonGroup searchRadioButtonGroup;
	private JRadioButtonMenuItem bFS;
	private JRadioButtonMenuItem dFS;
	private JRadioButtonMenuItem rDFS;
	private JRadioButtonMenuItem closestToTargetFirst;
	
	private ButtonGroup traceRadioButtonGroup;
	private JRadioButtonMenuItem none;
	private JRadioButtonMenuItem some;
	private JRadioButtonMenuItem fastest;
	
	private JPanel reductionOptions;
	private JComboBox reductionOption;
	private JCheckBox saveUppaal;

//XXX shortest can be quite hard to guarantee, because, it might not be the shortest in UPPAAL
//	private JRadioButtonMenuItem shortest;
	
	private DataLayer datalayer;
	private EscapableDialog me;
	private ArrayList<ArrayList<JPanel>> conjunctionGroups;
	private ArrayList<Integer> conjunctionsUsed;
	private HashMap<JPanel, ActionListener> andActionListenerMap;
	private ArrayList<JPanel> disjunctionGroups;
	private int disjunctionsUsed;
	
	public QueryDialogue (EscapableDialog me, DataLayer datalayer, QueryDialogueOption option, TAPNQuery queryToCreateFrom){

		queryToExport = new TAPNQuery("!!notNull!!",0,"",null,null,null);
		this.datalayer = datalayer;
		this.me = me;
		andActionListenerMap = new HashMap<JPanel, ActionListener>();
		disjunctionGroups = new ArrayList<JPanel>();
		disjunctionsUsed = 0; 
		conjunctionGroups = new ArrayList<ArrayList<JPanel>>();
		conjunctionsUsed = new ArrayList<Integer>();
		myRootPane = me.getRootPane();
		setLayout(new GridBagLayout());

		init(option, queryToCreateFrom);
	}
	

	private void init(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		// Query comment field starts here:		
		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Query comment: "));
		JTextField queryComment;
		if (queryToCreateFrom==null){
			queryComment = new JTextField("Query Comment/Name Here",25);
		}else{
			queryComment = new JTextField(queryToCreateFrom.name,25);	
		}
		
		namePanel.add(queryComment);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();		
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(namePanel, gridBagConstraints);
		
//Capacity number field starts here:
		capacityPanel = new JPanel(new FlowLayout());
		capacityPanel.add(new JLabel("Extra number of tokens: "));
		JSpinner numberOfExtraTokensInNet;
		if (queryToCreateFrom == null){
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(3,0,Integer.MAX_VALUE, 1));
		}else{
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(queryToCreateFrom.capacity,0,Integer.MAX_VALUE, 1));
		}
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(50,30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(50,30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(50,30));
		capacityPanel.add(numberOfExtraTokensInNet);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(capacityPanel, gridBagConstraints);

// Query field starts here: 		
		queryPanel = new JPanel(new GridBagLayout());
		queryPanel.setBorder(BorderFactory.createTitledBorder("Query"));

		quantificationRadioButtonGroup = new ButtonGroup();
		existsDiamond = new JRadioButtonMenuItem("There exists some reachable marking that satisifies:");
		existsBox = new JRadioButtonMenuItem("There exists a trace on which every marking satisfies:");
		forAllDiamond = new JRadioButtonMenuItem("On all traces there is eventually a marking that satisfies:");
		forAllBox = new JRadioButtonMenuItem("All reachable markings satisfy:");
		
		quantificationRadioButtonGroup.add(existsDiamond);
		quantificationRadioButtonGroup.add(existsBox);
		quantificationRadioButtonGroup.add(forAllDiamond);
		quantificationRadioButtonGroup.add(forAllBox);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		queryPanel.add(existsDiamond, gridBagConstraints);
		if (queryToCreateFrom==null){
			existsDiamond.setSelected(true);
		}else {
			if (queryToCreateFrom.query.contains("E<>")){
				existsDiamond.setSelected(true);
			} else if (queryToCreateFrom.query.contains("E[]")){
				existsBox.setSelected(true);
			} else if (queryToCreateFrom.query.contains("A<>")){
				forAllDiamond.setSelected(true);
			} else if (queryToCreateFrom.query.contains("A[]")){
				forAllBox.setSelected(true);
			}
		}
		
		gridBagConstraints.gridy = 1;
		queryPanel.add(existsBox, gridBagConstraints);
		
		gridBagConstraints.gridy = 2;
		queryPanel.add(forAllDiamond, gridBagConstraints);
		
		gridBagConstraints.gridy = 3;
		queryPanel.add(forAllBox, gridBagConstraints);
		
		
		markingSpecificsPanel = new JPanel(new GridBagLayout());

		if (queryToCreateFrom==null){
			addNewDisjunction();
		}else {
			addQuery(queryToCreateFrom);	
		}
		
		gridBagConstraints.gridy = 4;
		queryPanel.add( markingSpecificsPanel , gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(queryPanel, gridBagConstraints);
		
//verification option-radio buttons starts here:		
		uppaalOptions = new JPanel(new GridBagLayout());
		
		searchOptions = new JPanel(new GridBagLayout());
		searchOptions.setBorder(BorderFactory.createTitledBorder("Analysis Options"));
		searchRadioButtonGroup = new ButtonGroup();
		bFS = new JRadioButtonMenuItem("Breadth First Search");
		dFS = new JRadioButtonMenuItem("Depth First Search");
		rDFS = new JRadioButtonMenuItem("Random Depth First Search");
		closestToTargetFirst = new JRadioButtonMenuItem("Search by Closest To Target First");
		searchRadioButtonGroup.add(bFS);
		searchRadioButtonGroup.add(dFS);
		searchRadioButtonGroup.add(rDFS);
		searchRadioButtonGroup.add(closestToTargetFirst);
		
		if (queryToCreateFrom==null){
			bFS.setSelected(true);
		}else{
			if (queryToCreateFrom.searchOption == SearchOption.BFS){
				bFS.setSelected(true);
			} else if (queryToCreateFrom.searchOption == SearchOption.DFS){
				dFS.setSelected(true);
			} else if (queryToCreateFrom.searchOption == SearchOption.RDFS){
				rDFS.setSelected(true);
			} else if (queryToCreateFrom.searchOption == SearchOption.CLOSE_TO_TARGET_FIRST){
				closestToTargetFirst.setSelected(true);
			}	
		}
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		searchOptions.add(bFS,gridBagConstraints);
		gridBagConstraints.gridy = 1;
		searchOptions.add(dFS,gridBagConstraints);
		gridBagConstraints.gridy = 2;
		searchOptions.add(rDFS,gridBagConstraints);
		gridBagConstraints.gridy = 3;
		searchOptions.add(closestToTargetFirst,gridBagConstraints);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		uppaalOptions.add(searchOptions, gridBagConstraints);
		
		traceOptions = new JPanel(new GridBagLayout());
		traceOptions.setBorder(BorderFactory.createTitledBorder("Trace Options"));
		traceRadioButtonGroup = new ButtonGroup();
		some = new JRadioButtonMenuItem("Some encountered trace");
		fastest = new JRadioButtonMenuItem("Fastest trace");
		none = new JRadioButtonMenuItem("No trace (fastest option)");
		traceRadioButtonGroup.add(some);
		traceRadioButtonGroup.add(fastest);
		traceRadioButtonGroup.add(none);

		if (queryToCreateFrom==null){
			none.setSelected(true);
		}else{
			if (queryToCreateFrom.traceOption == TraceOption.SOME){
				some.setSelected(true);
			} else if (queryToCreateFrom.traceOption == TraceOption.FASTEST){
				fastest.setSelected(true);
			} else if (queryToCreateFrom.traceOption == TraceOption.NONE){
				none.setSelected(true);
			}	
		}
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		traceOptions.add(some,gridBagConstraints);
		gridBagConstraints.gridy = 1;
		traceOptions.add(fastest,gridBagConstraints);
		gridBagConstraints.gridy = 2;
		traceOptions.add(none,gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		uppaalOptions.add(traceOptions, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		add(uppaalOptions, gridBagConstraints);

//ReductionOptions starts here:
		this.reductionOptions = new JPanel(new FlowLayout());
		this.reductionOptions.setBorder(BorderFactory.createTitledBorder("Reduction Options"));
		String[] reductionOptions = {"Standard", "Symmetry Reduction", "Optimised Symmetry Reduction"};
		reductionOption = new JComboBox(reductionOptions);
		if (queryToCreateFrom!=null){
			if (queryToCreateFrom.reductionOption == ReductionOption.NAIVE_UPPAAL_SYM){
				reductionOption.setSelectedIndex(1);
			} else if (queryToCreateFrom.reductionOption == ReductionOption.ADV_UPPAAL_SYM){
				reductionOption.setSelectedIndex(2);
			}
		}
		reductionOption.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				if ( ((String) reductionOption.getSelectedItem()).toLowerCase().contains("symmetry")){
					none.setSelected(true);
					some.setEnabled(false);
					fastest.setEnabled(false);
				}else{
					some.setEnabled(true);
					fastest.setEnabled(true);
				}
			}
			
		});
		
		saveUppaal = new JCheckBox("Save Uppaal xml");
		saveUppaal.setSelected(false);
		this.reductionOptions.add(saveUppaal);
		this.reductionOptions.add(new JLabel("  Choose reduction method:"));
		this.reductionOptions.add(reductionOption);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		add(this.reductionOptions, gridBagConstraints);
		
//add save and verify buttons starts here:
		buttonPanel = new JPanel(new FlowLayout());
		if (option == QueryDialogueOption.Save){
			okButton = new JButton("Save");
			verifyButton = new JButton("Save and Verify");
			cancelButton = new JButton("Cancel");
			removeButton = new JButton("Remove");

			okButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							//TODO make save
							//save();
							exit();
						}
					}
			);
			verifyButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							exit();
							Verification.runUppaalVerification(CreateGui.getModel(), getQuery(), saveUppaal.isSelected());
						}
					}
			);
			cancelButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							queryToExport = null;
							exit();
						}
					}
			);
			removeButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent evt) {
							queryToExport = null;
							datalayer.getQueries().remove(queryToCreateFrom);
							CreateGui.setLeftPaneToQueries();
							exit();
						}
					}
			);
		}else if (option == QueryDialogueOption.Export){
			okButton = new JButton("export");
			cancelButton = new JButton("Cancel");
			saveUppaal.setSelected(true);
			saveUppaal.setEnabled(false);
			okButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							exit();
						}
					}
			);
			cancelButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							queryToExport = null;
							exit();
						}
					}
			);		
		}
		if (option == QueryDialogueOption.Save){
			buttonPanel.add(cancelButton);
			
			if (queryToCreateFrom!=null){
				buttonPanel.add(removeButton);	
			}
			
			buttonPanel.add(okButton);

			buttonPanel.add(verifyButton);
			
		}else {
			buttonPanel.add(cancelButton);
			
			buttonPanel.add(okButton);
		}


		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		add(buttonPanel, gridBagConstraints);

		myRootPane.setDefaultButton(okButton);		
	}

	private void addQuery(TAPNQuery queryToCreateFrom) {
		String[] disjunctions = queryToCreateFrom.query.subSequence(3, queryToCreateFrom.query.length()).toString().split(" \\|\\| ") ;
//		System.out.println(queryToCreateFrom.query);
//		for (String s : disjunctions){
//			System.out.println(s);
//		}
		
		for (int i=0; i < disjunctions.length; i++){
			addNewDisjunction();
			
			String strippedDisjunction = "";
			
			for (int j=3; j < disjunctions[i].length()-3; j++){
				strippedDisjunction = strippedDisjunction + disjunctions[i].charAt(j);
			}
//			System.out.println(strippedDisjunction);
			
			if (disjunctions[i].contains("&&")){
				String[] conjunctions = strippedDisjunction.split(" \\) \\&\\& \\( ");
				for (int j=0; j < conjunctions.length-1; j++){
					createNewConjunction(i);	
				}
				for (int j=0; j < conjunctions.length; j++){
					String[] partedConjunction = partedComparison(conjunctions[j]);
//					for (String s : partedConjunction){
//						System.out.println(s);
//					}
					
					//set the placeDropdown to the correct place
					((JComboBox)conjunctionGroups.get(i).get(j).getComponent(0)).setSelectedItem(partedConjunction[0]);					

					//set the relational symbol
					((JComboBox)conjunctionGroups.get(i).get(j).getComponent(1)).setSelectedItem(partedConjunction[1]);
					
					//set the size
					((JSpinner)conjunctionGroups.get(i).get(j).getComponent(2)).setValue(Integer.parseInt(partedConjunction[2]));
					
					//set andButton to pressed
					if (j<conjunctions.length-1){
						((JButton)conjunctionGroups.get(i).get(j).getComponent(3)).setSelected(true);
					}
				}
				
			}else{
				String[] partedConjunction = partedComparison(strippedDisjunction);
				
				//set the placeDropdown to the correct place
				((JComboBox)conjunctionGroups.get(i).get(0).getComponent(0)).setSelectedItem(partedConjunction[0]);					

				//set the relational symbol
				((JComboBox)conjunctionGroups.get(i).get(0).getComponent(1)).setSelectedItem(partedConjunction[1]);
				
				//set the size
				((JSpinner)conjunctionGroups.get(i).get(0).getComponent(2)).setValue(Integer.parseInt(partedConjunction[2]));
			}
		}
	}
	
	private String[] partedComparison(String comparison){
		String[] toReturn = {"","",""};
		if (comparison.contains("<=")){
			toReturn[1] = "<=";
			String[] placeAndSize = comparison.split(" \\<\\= ");
			toReturn[0] = placeAndSize[0];
			toReturn[2] = placeAndSize[1];
		}else if (comparison.contains(">=")){
			toReturn[1] = ">=";
			String[] placeAndSize = comparison.split(" \\>\\= ");
			toReturn[0] = placeAndSize[0];
			toReturn[2] = placeAndSize[1];
		}else if (comparison.contains("=")){
			toReturn[1] = "=";
			String[] placeAndSize = comparison.split(" \\=\\= ");
			toReturn[0] = placeAndSize[0];
			toReturn[2] = placeAndSize[1];
		}else if (comparison.contains("<")){
			toReturn[1] = "<";
			String[] placeAndSize = comparison.split(" \\< ");
			toReturn[0] = placeAndSize[0];
			toReturn[2] = placeAndSize[1];
		}else if (comparison.contains(">")){
			toReturn[1] = ">";
			String[] placeAndSize = comparison.split(" \\> ");
			toReturn[0] = placeAndSize[0];
			toReturn[2] = placeAndSize[1];
		}		
		return toReturn;
	}

	public TAPNQuery getQuery() {
		if (queryToExport == null){
			return null;
		}
		String name = getQueryComment();
		int capacity = getCapacity();
		String query = composeQuery();
		
		TAPNQuery.TraceOption traceOption = null;
		String traceOptionString = getTraceOptions();
		
		if (traceOptionString.toLowerCase().contains("some")){
			traceOption = TraceOption.SOME;
		}else if (traceOptionString.toLowerCase().contains("fastest")){
			traceOption = TraceOption.FASTEST;
		}else if (traceOptionString.toLowerCase().contains("no")){
			traceOption = TraceOption.NONE;
		}
		
		TAPNQuery.SearchOption searchOption = null;
		String searchOptionString = getSearchOptions();
		
		if (searchOptionString.toLowerCase().contains("breadth")){
			searchOption = SearchOption.BFS;
		}else if (searchOptionString.toLowerCase().contains("depth") && (! searchOptionString.toLowerCase().contains("random")) ){
			searchOption = SearchOption.DFS;
		}else if (searchOptionString.toLowerCase().contains("depth") && searchOptionString.toLowerCase().contains("random") ){
			searchOption = SearchOption.RDFS;
		}else if (searchOptionString.toLowerCase().contains("target")){
			searchOption = SearchOption.CLOSE_TO_TARGET_FIRST;
		}
		
		TAPNQuery.ReductionOption reductionOptionToSet = null;
		String reductionOptionString = ""+reductionOption.getSelectedItem();
		if (reductionOptionString.toLowerCase().equals("symmetry reduction")){
			reductionOptionToSet = ReductionOption.NAIVE_UPPAAL_SYM;
		}else if (reductionOptionString.toLowerCase().contains("standard")){
			reductionOptionToSet = ReductionOption.NAIVE;
		}else if (reductionOptionString.toLowerCase().contains("optimised symmetry reduction")){
			reductionOptionToSet = ReductionOption.ADV_UPPAAL_SYM;
		}
		
		return new TAPNQuery(name, capacity, query, traceOption, searchOption, reductionOptionToSet);
		
	}

	private int getCapacity(){
		return (Integer) ((JSpinner)capacityPanel.getComponent(1)).getValue();
	}
	
	private String getQueryComment() {
		return ((JTextField)namePanel.getComponent(1)).getText();
	}

	private String getTraceOptions() {
		String toReturn = null;
		for (Object radioButton : traceOptions.getComponents()){
			if( (radioButton instanceof JRadioButtonMenuItem)){
				if ( ((JRadioButtonMenuItem)radioButton).isSelected() ){
			
					toReturn = ((JRadioButtonMenuItem)radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private String getSearchOptions() {
		String toReturn = null;
		for (Object radioButton : searchOptions.getComponents()){
			if( (radioButton instanceof JRadioButtonMenuItem)){
				if ( ((JRadioButtonMenuItem)radioButton).isSelected() ){
			
					toReturn = ((JRadioButtonMenuItem)radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private String composeQuery() {
		String toReturn = "";
		
		if (existsDiamond.isSelected()){
			toReturn = "E<>";
		}else if (existsBox.isSelected()){
			toReturn = "E[]";
		}else if (forAllDiamond.isSelected()){
			toReturn = "A<>";
		}else if (forAllBox.isSelected()){
			toReturn = "A[]";
		}
		
		for (int i=0; i < conjunctionGroups.size(); i++){
			toReturn = toReturn + "(";
			for (int j=0; j < conjunctionGroups.get(i).size(); j++){
				
				toReturn = toReturn + "( " + ((JComboBox)conjunctionGroups.get(i).get(j).getComponent(0)).getSelectedItem() + " ";
				if (((String)((JComboBox)conjunctionGroups.get(i).get(j).getComponent(1)).getSelectedItem()).equals("=")){
					toReturn = toReturn + "== ";	
				}else {
					toReturn = toReturn + ((JComboBox)conjunctionGroups.get(i).get(j).getComponent(1)).getSelectedItem() + " ";
				}
				toReturn = toReturn + ((JSpinner)conjunctionGroups.get(i).get(j).getComponent(2)).getValue() + " ";
				if(j < conjunctionGroups.get(i).size()-1 ){
					toReturn = toReturn + ") && ";
				}else{
					toReturn = toReturn + ")";
				}
			}
			if (i < conjunctionGroups.size()-1){
				toReturn = toReturn + ") || ";
			}else {
				toReturn = toReturn + ")";
			}
		}
		
		return toReturn;
	}
	
	private void exit(){
		myRootPane.getParent().setVisible(false);
	}

	private void addNewDisjunction() {
		JPanel disjunctionGroup = newDisjunction();
		disjunctionGroups.add(disjunctionGroup);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = disjunctionsUsed;
		gbc.anchor = GridBagConstraints.WEST;	
		markingSpecificsPanel.add(disjunctionGroup, gbc);

		conjunctionsUsed.add(new Integer(0));
		disjunctionsUsed++;
		
		me.pack();
	}

	private void removeDisjunction(int number){
		JPanel disjunctionGroupToRemove = disjunctionGroups.get(number+1);
		disjunctionGroupToRemove.setVisible(false);
		disjunctionGroups.remove(disjunctionGroupToRemove);
		disjunctionGroupToRemove.removeNotify();
		
		me.pack();
	}

	private void createNewConjunction(int inDisjunction){

		JPanel conjunctionGroup = newConjunction(new Integer(inDisjunction));
		
		conjunctionGroups.get(inDisjunction).add(conjunctionGroup);
		
		conjunctionsUsed.set(inDisjunction, conjunctionsUsed.get(inDisjunction) + 1);

		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = conjunctionsUsed.get(inDisjunction);
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		((JPanel)markingSpecificsPanel.getComponent(inDisjunction)).add(conjunctionGroup, gbc);
		
		me.pack();
	}
	private void removeConjunction(JPanel conjunctionGroupToRemove){		
		conjunctionGroupToRemove.setVisible(false);
		for (ArrayList<JPanel> disjunctionGroup : conjunctionGroups){
			if (disjunctionGroup.contains(conjunctionGroupToRemove)){
				disjunctionGroup.remove(conjunctionGroupToRemove);
				conjunctionGroupToRemove.removeNotify();
				disjunctionGroup.trimToSize();
				
				me.pack();
			}
		}
	}
	
	private JPanel newDisjunction(){
		JPanel disjunctionGroup = new JPanel(new GridBagLayout());
		
		JPanel conjunctionGroup = newConjunction(disjunctionsUsed);
		
		ArrayList<JPanel> conjunctionToInsert = new ArrayList<JPanel>();
		conjunctionToInsert.add(conjunctionGroup);
		conjunctionGroups.add(conjunctionToInsert);
		
		GridBagConstraints gbc = new GridBagConstraints();
		disjunctionGroup.add(conjunctionGroup, gbc);

		final JButton orButton = new JButton("OR");
		
		orButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent evt) {
						for (int i=0; i < disjunctionGroups.size(); i++) {
							//component 1 is a button, with "OR" on it
							if (disjunctionGroups.get(i).getComponent(1) == orButton){
								
								if ( ! orButton.isSelected() ){
									orButton.setSelected(true);
									//add a new disjunctionGroup.
									addNewDisjunction();
									break;
								}else {
									removeDisjunction(i);
									if ( i < disjunctionGroups.size()-1){
										break;
									}else {
										orButton.setSelected(false);
										break;
									}
								}
							}
						}
					}
				});
		
		//XXX - hacks
		gbc.gridy = 10;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		disjunctionGroup.add(orButton, gbc);
		
		return disjunctionGroup;
	}
		
	private JPanel newConjunction(final int disjunctionIndex){
		
		final JPanel conjunctionGroup = new JPanel(new FlowLayout());
		
		String[] relationalSymbols= {"=","<=","<",">=",">"};
		int currentValue = 0;
		int min = 0;
		int step = 1;		
		
		String[] places = new String[datalayer.getPlaces().length];
		for (int i=0; i< places.length; i++){
			places[i] = datalayer.getPlaces()[i].getName();
		}
		JComboBox placesBox = new JComboBox(new DefaultComboBoxModel(places));
		conjunctionGroup.add( placesBox );
		
		JComboBox relationalBox = new JComboBox(new DefaultComboBoxModel(relationalSymbols));
		conjunctionGroup.add(relationalBox);
		
		JSpinner placeMarking = new JSpinner(new SpinnerNumberModel(currentValue, min, Integer.MAX_VALUE, step));
		placeMarking.setMaximumSize(new Dimension(50,30));
		placeMarking.setMinimumSize(new Dimension(50,30));
		placeMarking.setPreferredSize(new Dimension(50,30));
//		placeMarking.setVisible(visibility);
		
		conjunctionGroup.add(placeMarking);

		final JButton andButton = new JButton("AND");
//		andButton.setVisible(visibility);
		
		andActionListenerMap.put(conjunctionGroup, 
				
				new ActionListener() {
					final int thisDisjunctionIndex = disjunctionIndex;
					final JPanel thisConjunctionGroup = conjunctionGroup;
					
					public void actionPerformed(ActionEvent evt) {

						JButton and = (JButton) thisConjunctionGroup.getComponent(3);

						if ( ! and.isSelected() ){
							and.setSelected(true);
							//add a new conjunction in the orGroupIndex'th disjunction.
							createNewConjunction(thisDisjunctionIndex);
						}else {
							removeConjunction( nextConjunctionGroup(thisConjunctionGroup) );
							if ( conjunctionGroups.get(thisDisjunctionIndex).indexOf(thisConjunctionGroup)+1 == conjunctionGroups.get(thisDisjunctionIndex).size() ){
								and.setSelected(false);
							}
						}								
					}
				}
				
		);
		
		andButton.addActionListener(andActionListenerMap.get(conjunctionGroup));
		
		conjunctionGroup.add(andButton);
		
		return conjunctionGroup;
	}


	protected JPanel nextConjunctionGroup(JPanel thisConjunctionGroup) {
		for (ArrayList<JPanel> disjunctionGroup : conjunctionGroups){
			if (disjunctionGroup.contains(thisConjunctionGroup)){
				return disjunctionGroup.get( disjunctionGroup.indexOf(thisConjunctionGroup)+1 );
			}
		}
		return null;
	}
	
	public static TAPNQuery ShowUppaalQueryDialogue(QueryDialogueOption option, TAPNQuery queryToRepresent){
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add query editor
		QueryDialogue queryDialogue = new QueryDialogue(guiDialog, CreateGui.getModel(), option, queryToRepresent); 
		contentPane.add( queryDialogue );
		
		guiDialog.setResizable(true);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
		
		return  queryDialogue.getQuery();
	}
			
}