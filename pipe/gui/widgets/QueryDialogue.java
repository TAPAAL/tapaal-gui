package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Verifier;
import dk.aau.cs.TCTL.StringPosition;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractPathProperty;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.UPPAAL.UppaalExporter;

public class QueryDialogue extends JPanel{

	private static final long serialVersionUID = 7852107237344005546L;
	public enum QueryDialogueOption {VerifyNow, Save, Export}

	private boolean querySaved = false;

	private JRootPane rootPane;

	// Query Name Panel;
	private JPanel namePanel;

	// Boundedness check panel
	private JPanel boundednessCheckPanel;
	private JSpinner numberOfExtraTokensInNet;
	private JButton kbounded;
	private JButton kboundedOptimize;

	// Query Panel
	private JPanel queryPanel;

	private JPanel quantificationPanel;
	private ButtonGroup quantificationRadioButtonGroup;
	private JRadioButton existsDiamond;
	private JRadioButton existsBox;
	private JRadioButton forAllDiamond;
	private JRadioButton forAllBox;	

	private JTextPane queryField;

	private JPanel logicButtonPanel;
	private ButtonGroup logicButtonGroup;
	private JButton conjunctionButton;
	private JButton disjunctionButton;
	private JButton negationButton;

	private JPanel editingButtonPanel;
	private ButtonGroup editingButtonsGroup;
	private JButton deleteButton;
	private JButton resetButton;
	private JButton undoButton;
	private JButton redoButton;
	private JButton editQueryButton;

	private JPanel predicatePanel;
	private JButton addPredicateButton;
	private JComboBox placesBox;
	private JComboBox relationalOperatorBox;
	private JSpinner placeMarking;



	// Uppaal options panel (search + trace options)
	// search options panel
	private JPanel searchOptionsPanel;
	private JPanel uppaalOptionsPanel;
	private ButtonGroup searchRadioButtonGroup;
	private JRadioButton bFS;
	private JRadioButton dFS;
	private JRadioButton rDFS;
	private JRadioButton closestToTargetFirst;

	// Trace options panel
	private JPanel traceOptionsPanel;

	private ButtonGroup traceRadioButtonGroup;
	private JRadioButton none;
	private JRadioButton some;
	private JRadioButton fastest;

	// Reduction options panel
	private JPanel reductionOptionsPanel;
	private JComboBox reductionOption;
	private JCheckBox symmetryReduction;

	// Buttons in the bottom of the dialogue
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton saveButton;
	private JButton saveAndVerifyButton;
	private JButton saveUppaalXMLButton;

	// Private Members
	private StringPosition currentSelection = null;

	private DataLayer datalayer;
	private QueryConstructionUndoManager undoManager;
	private UndoableEditSupport undoSupport;

	private String name_ADVNOSYM = "Optimised Standard";
	private String name_NAIVE = "Standard";
	private String name_BROADCAST = "Broadcast Reduction";
	private String name_BROADCASTDEG2 = "Broadcast Degree 2 Reduction";
	private boolean userChangedAtomicPropSelection = true;

	private TCTLAbstractProperty newProperty;

	public QueryDialogue (EscapableDialog me, DataLayer datalayer, QueryDialogueOption option, TAPNQuery queryToCreateFrom){

		this.datalayer = datalayer;
		this.newProperty = queryToCreateFrom==null ? new TCTLPathPlaceHolder() : queryToCreateFrom.getProperty();
		rootPane = me.getRootPane();
		setLayout(new GridBagLayout());

		init(option, queryToCreateFrom);
	}



	public void setQueryFieldEditable(boolean isEditable)
	{
		queryField.setEditable(isEditable);
	}

	public TAPNQuery getQuery() {
		if (!querySaved){
			return null;
		}


		String name = getQueryComment();
		int capacity = getCapacity();


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

		ReductionOption reductionOptionToSet = null;
		String reductionOptionString = ""+reductionOption.getSelectedItem();
		boolean symmetry = symmetryReduction.isSelected();

		if (reductionOptionString.equals(name_NAIVE) && !symmetry){
			reductionOptionToSet = ReductionOption.STANDARD;
		}else if(reductionOptionString.equals(name_NAIVE) && symmetry){
			reductionOptionToSet = ReductionOption.STANDARDSYMMETRY;
		}else if (reductionOptionString.equals(name_ADVNOSYM) && !symmetry){
			reductionOptionToSet = ReductionOption.OPTIMIZEDSTANDARD;
		}else if (reductionOptionString.equals(name_ADVNOSYM) && symmetry){
			reductionOptionToSet = ReductionOption.OPTIMIZEDSTANDARDSYMMETRY;
		}else if(reductionOptionString.equals(name_BROADCAST) && !symmetry){
			reductionOptionToSet = ReductionOption.BROADCAST;
		}else if(reductionOptionString.equals(name_BROADCAST) && symmetry){
			reductionOptionToSet = ReductionOption.BROADCASTSYMMETRY;
		}else if(reductionOptionString.equals(name_BROADCASTDEG2) && !symmetry){
			reductionOptionToSet = ReductionOption.DEGREE2BROADCAST;
		}else if(reductionOptionString.equals(name_BROADCASTDEG2) && symmetry){
			reductionOptionToSet = ReductionOption.DEGREE2BROADCASTSYMMETRY;
		}


		return new TAPNQuery(name, capacity, newProperty.copy(), traceOption, searchOption, reductionOptionToSet, /*hashTableSizeToSet*/null, /*extrapolationOptionToSet*/ null);
	}

	private int getCapacity(){
		return (Integer) ((JSpinner)boundednessCheckPanel.getComponent(1)).getValue();
	}

	private String getQueryComment() {
		return ((JTextField)namePanel.getComponent(1)).getText();
	}

	private String getTraceOptions() {
		String toReturn = null;
		for (Object radioButton : traceOptionsPanel.getComponents()){
			if( (radioButton instanceof JRadioButton)){
				if ( ((JRadioButton)radioButton).isSelected() ){
					toReturn = ((JRadioButton)radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private String getSearchOptions() {
		String toReturn = null;
		for (Object radioButton : searchOptionsPanel.getComponents()){
			if( (radioButton instanceof JRadioButton)){
				if ( ((JRadioButton)radioButton).isSelected() ){

					toReturn = ((JRadioButton)radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private void refreshTraceOptions() {
		if(symmetryReduction.isSelected())
		{
			some.setEnabled(false);
			fastest.setEnabled(false);
			none.setSelected(true);
		}
		else
		{
			some.setEnabled(true);
			fastest.setEnabled(true);
		}
	}

	private void resetQuantifierSelectionButtons() {
		quantificationRadioButtonGroup.clearSelection();
	}

	private void exit(){
		rootPane.getParent().setVisible(false);
	}

	public String getQuantificationSelection() {
		if (existsDiamond.isSelected()){
			return "E<>";
		}else if (existsBox.isSelected()){
			return "E[]";
		}else if (forAllDiamond.isSelected()){
			return "A<>";
		}else if (forAllBox.isSelected()){
			return "A[]";
		} else {
			return "";
		}
	}

	private void enableColorsReductionOptions() {
		String[] options = new String[]{name_BROADCAST, name_BROADCASTDEG2};

		String reductionOptionString = ""+reductionOption.getSelectedItem();
		boolean selectedOptionStillAvailable = false;

		reductionOption.removeAllItems();

		for (String s : options){
			reductionOption.addItem(s);
			if(s.equals(reductionOptionString))
			{
				selectedOptionStillAvailable = true;
			}
		}

		if(selectedOptionStillAvailable)
			reductionOption.setSelectedItem(reductionOptionString);
	}

	private void enableOnlyLivenessReductionOptions(){
		String[] options = null;
		if(!this.datalayer.hasTAPNInhibitorArcs()){
			options = new String[]{name_ADVNOSYM, name_BROADCAST, name_BROADCASTDEG2};
		}else{
			options = new String[]{name_BROADCAST, name_BROADCASTDEG2};
		}

		String reductionOptionString = ""+reductionOption.getSelectedItem();
		boolean selectedOptionStillAvailable = false;

		reductionOption.removeAllItems();

		for (String s : options){
			reductionOption.addItem(s);
			if(s.equals(reductionOptionString))
			{
				selectedOptionStillAvailable = true;
			}
		}

		if(selectedOptionStillAvailable)
			reductionOption.setSelectedItem(reductionOptionString);

	}

	private void enableAllReductionOptions(){
		String reductionOptionString = ""+reductionOption.getSelectedItem();
		reductionOption.removeAllItems();
		if(!this.datalayer.hasTAPNInhibitorArcs()){
			String[] options = {name_NAIVE, name_ADVNOSYM, name_BROADCAST, name_BROADCASTDEG2};

			for (String s : options){
				reductionOption.addItem(s);
			}
		}else {
			//reductionOption.addItem(name_INHIBSTANDARD);
			//reductionOption.addItem(name_INHIBSYM);
			reductionOption.addItem(name_BROADCAST);
			//reductionOption.addItem(name_BROADCASTSYM);
			reductionOption.addItem(name_BROADCASTDEG2);
			//reductionOption.addItem(name_BROADCASTDEG2SYM);
			//reductionOption.addItem(name_ADVBROADCASTSYM);
			//reductionOption.addItem(name_OPTBROADCAST);
			//reductionOption.addItem(name_OPTBROADCASTSYM);
			//reductionOption.addItem(name_SUPERBROADCAST);
			//reductionOption.addItem(name_SUPERBROADCASTSYM);
		}

		reductionOption.setSelectedItem(reductionOptionString);


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

		guiDialog.setResizable(false); 


		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

		return  queryDialogue.getQuery();
	}


	private TCTLAbstractStateProperty getStateProperty(TCTLAbstractProperty property) {
		if (property instanceof TCTLAbstractStateProperty) {
			return (TCTLAbstractStateProperty)property.copy();
		} else {
			return new TCTLStatePlaceHolder();
		}
	}

	private TCTLAbstractStateProperty getCurrentSelectionChild(int number, TCTLAbstractProperty property) {
		StringPosition[] children = property.getChildren();
		int count = 0;
		for (int i = 0; i < children.length; i++) {
			TCTLAbstractProperty child = children[i].getObject();
			if (child instanceof TCTLAbstractStateProperty) {
				count++;
				if (count == number) {
					return (TCTLAbstractStateProperty)child;
				}
			}
		}
		return new TCTLStatePlaceHolder();
	}

	// Update current selection based on position of the caret in the string representation
	// used for updating when selecting with the mouse.
	private void updateSelection() {
		int index = queryField.getCaretPosition();
		StringPosition position = newProperty.objectAt(index);
		if (position == null) return;
		queryField.select(position.getStart(),position.getEnd());
		currentSelection = position;
		if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
			enableStateButtons();
		} else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
			enablePathButtons();
		} else {
			disableAllQueryButtons();
		}

		updateQueryButtonsAccordingToSelection();

	}



	

	// update selection based on some change to the query.
	// If the query contains place holders we want to select 
	// the first placeholder to speed up query construction
	private void updateSelection(TCTLAbstractProperty newSelection) {
		queryField.setText(newProperty.toString());

		StringPosition position;

		if(newProperty.containsPlaceHolder())
		{
			TCTLAbstractProperty ph = newProperty.findFirstPlaceHolder();
			position = newProperty.indexOf(ph);
		}
		else {
			position = newProperty.indexOf(newSelection);
		}

		queryField.select(position.getStart(),position.getEnd());
		currentSelection = position;
		if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
			enableStateButtons();
		} else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
			enablePathButtons();
		} else {
			disableAllQueryButtons();
		}
		
		updateQueryButtonsAccordingToSelection();
	}
	
	private void updateQueryButtonsAccordingToSelection() {
		if(currentSelection.getObject() instanceof TCTLAtomicPropositionNode)
		{
			TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode)currentSelection.getObject();
			
			// bit of a hack to prevent posting edits to the undo manager when we programmatically
			// change the selection in the atomic proposition comboboxes etc. because a different
			// atomic proposition was selected
			userChangedAtomicPropSelection = false;
			placesBox.setSelectedItem(node.getPlace());
			relationalOperatorBox.setSelectedItem(node.getOp());
			placeMarking.setValue(node.getN());
			userChangedAtomicPropSelection = true;
		}
		else if(currentSelection.getObject() instanceof TCTLEFNode)
		{
			existsDiamond.setSelected(true);
		}
		else if(currentSelection.getObject() instanceof TCTLEGNode)
		{
			existsBox.setSelected(true);
		}
		else if(currentSelection.getObject() instanceof TCTLAGNode)
		{
			forAllBox.setSelected(true);
		}
		else if(currentSelection.getObject() instanceof TCTLAFNode)
		{
			forAllDiamond.setSelected(true);
		}
	}

	// Delete current selection
	private void deleteSelection() {
		if(currentSelection != null)
		{
			TCTLAbstractProperty replacement = null;
			if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
				replacement = getCurrentSelectionChild(1, currentSelection.getObject());
			} else if(currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
				replacement = new TCTLPathPlaceHolder();
			}
			if(replacement !=null) {

				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), replacement);

				newProperty = newProperty.replace(currentSelection.getObject(), replacement);

				if(currentSelection.getObject() instanceof TCTLAbstractPathProperty)
					resetQuantifierSelectionButtons();

				updateSelection(replacement);

				undoSupport.postEdit(edit);
			}
		}
	}

	private void clearSelection() {
		queryField.select(0, 0);
		currentSelection = null;
		disableAllQueryButtons();
	}

	private void setSaveButtonsEnabled(){
		if(!queryField.isEditable()) {
			boolean isQueryOk = getQueryComment().length() > 0 && !newProperty.containsPlaceHolder();
			saveButton.setEnabled(isQueryOk);
			saveAndVerifyButton.setEnabled(isQueryOk);
			saveUppaalXMLButton.setEnabled(isQueryOk);
		}
		else {
			saveButton.setEnabled(false);
			saveAndVerifyButton.setEnabled(false);
			saveUppaalXMLButton.setEnabled(false);
		}
	}

	private void setEnabledReductionOptions() {
		if(this.datalayer.isUsingColors()) {
			enableColorsReductionOptions();
		}
		else if (getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")) {
			enableOnlyLivenessReductionOptions();
		} else {
			enableAllReductionOptions();
		}

	}

	private void disableAllQueryButtons() {
		existsBox.setEnabled(false);
		existsDiamond.setEnabled(false);
		forAllBox.setEnabled(false);
		forAllDiamond.setEnabled(false);
		conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		negationButton.setEnabled(false);
		placesBox.setEnabled(false);
		relationalOperatorBox.setEnabled(false);
		placeMarking.setEnabled(false);
		addPredicateButton.setEnabled(false);

	}

	private void enablePathButtons() {
		existsBox.setEnabled(true);
		existsDiamond.setEnabled(true);
		forAllBox.setEnabled(true);
		forAllDiamond.setEnabled(true);
		conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		negationButton.setEnabled(false);	
		placesBox.setEnabled(false);
		relationalOperatorBox.setEnabled(false);
		placeMarking.setEnabled(false);
		addPredicateButton.setEnabled(false);
	}

	private void enableStateButtons() {
		existsBox.setEnabled(false);
		existsDiamond.setEnabled(false);
		forAllBox.setEnabled(false);
		forAllDiamond.setEnabled(false);
		conjunctionButton.setEnabled(true);
		disjunctionButton.setEnabled(true);
		negationButton.setEnabled(true);
		placesBox.setEnabled(true);
		relationalOperatorBox.setEnabled(true);
		placeMarking.setEnabled(true);
		if(placesBox.getSelectedItem() == null)
			addPredicateButton.setEnabled(false);
		else
			addPredicateButton.setEnabled(true);

	}

	private void disableEditingButtons() {
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	private void enableEditingButtons() {
		refreshUndoRedo();
		if(currentSelection != null)
			deleteButton.setEnabled(true);
	}

	private void returnFromManualEdit(TCTLAbstractProperty newQuery) {
		setQueryFieldEditable(false);

		if(newQuery != null)
			newProperty = newQuery;

		updateSelection(newProperty);
		resetButton.setText("Reset Query");
		editQueryButton.setText("Edit Query");
		enableEditingButtons();
	}

	private void changeToEditMode() {
		setQueryFieldEditable(true);
		resetButton.setText("Parse Query");
		editQueryButton.setText("Cancel");
		clearSelection();
		disableAllQueryButtons();
		disableEditingButtons();
		setSaveButtonsEnabled();
	}

	private void updateQueryOnAtomicPropositionChange() {
		if(currentSelection.getObject() instanceof TCTLAtomicPropositionNode){
			TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode((String)placesBox.getSelectedItem(), (String)relationalOperatorBox.getSelectedItem(), (Integer) placeMarking.getValue());
			if(!property.equals(currentSelection.getObject())) {
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////
	// Initialization of the dialogue
	///////////////////////////////////////////////////////////////////////

	private void init(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		initQueryNamePanel(queryToCreateFrom);
		initBoundednessCheckPanel(queryToCreateFrom);
		initQueryPanel(queryToCreateFrom);
		initUppaalOptionsPanel(queryToCreateFrom);
		initReductionOptionsPanel(queryToCreateFrom);
		initButtonPanel(option,queryToCreateFrom);

		rootPane.setDefaultButton(saveButton);
		disableAllQueryButtons();
		setSaveButtonsEnabled();

		// initilize the undo.redo system
		undoManager= new QueryConstructionUndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoAdapter());
		refreshUndoRedo();

		setEnabledReductionOptions();
		refreshTraceOptions();

	}

	private void initQueryNamePanel(final TAPNQuery queryToCreateFrom) {
		// Query comment field starts here:		
		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Query comment: "));
		JTextField queryComment;
		if (queryToCreateFrom==null){
			queryComment = new JTextField("Query Comment/Name Here",25);
		}else{
			queryComment = new JTextField(queryToCreateFrom.getName(),25);	
		}

		namePanel.add(queryComment);

		queryComment.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void insertUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void changedUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}
		});



		GridBagConstraints gridBagConstraints = new GridBagConstraints();		
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(namePanel, gridBagConstraints);
	}

	private void initBoundednessCheckPanel(final TAPNQuery queryToCreateFrom) {

		// Number of extra tokens field
		boundednessCheckPanel = new JPanel(new FlowLayout());
		boundednessCheckPanel.add(new JLabel("Extra number of tokens: "));

		if (queryToCreateFrom == null){
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(3,0,Integer.MAX_VALUE, 1));
		}else{
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(queryToCreateFrom.getCapacity(),0,Integer.MAX_VALUE, 1));
		}
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(50,30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(50,30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(50,30));
		boundednessCheckPanel.add(numberOfExtraTokensInNet);

		// Boundedness button
		kbounded = new JButton("Check Boundedness");
		kbounded.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						Verifier.analyseKBounded(CreateGui.getModel(), getCapacity());
					}

				}
		);		
		boundednessCheckPanel.add(kbounded);

		kboundedOptimize = new JButton("Optimize Number of Tokens");
		kboundedOptimize.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent evt){
						Verifier.analyzeAndOptimizeKBound(CreateGui.getModel(), getCapacity(), numberOfExtraTokensInNet);
					}
				}
		);
		boundednessCheckPanel.add(kboundedOptimize);

		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(boundednessCheckPanel, gridBagConstraints);
	}

	private void initQueryPanel(final TAPNQuery queryToCreateFrom) {
		queryPanel = new JPanel(new GridBagLayout());
		queryPanel.setBorder(BorderFactory.createTitledBorder("Query (click on the part of the query you want to change)"));


		initQueryField();
		initQuantificationPanel(queryToCreateFrom);		
		initLogicPanel();		
		initPredicationConstructionPanel();
		initQueryEditingPanel();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		add(queryPanel, gbc);

	}

	private void initQueryField() {
		// Query Text Field
		queryField = new JTextPane();

		StyledDocument doc = queryField.getStyledDocument();

		//  Set alignment to be centered for all paragraphs

		MutableAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
		StyleConstants.setFontSize(standard, 14);
		doc.setParagraphAttributes(0, 0, standard, true);


		queryField.setBackground(Color.white);
		queryField.setText(newProperty.toString());
		queryField.setEditable(false);

		//Put the text pane in a scroll pane.
		JScrollPane queryScrollPane = new JScrollPane(queryField);
		queryScrollPane.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension(750, 56);
		queryScrollPane.setPreferredSize(d);
		queryScrollPane.setMinimumSize(d);



		queryField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(!queryField.isEditable())
					updateSelection();

			}
		}
		);	

		queryField.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void insertUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void changedUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}
		});

		queryField.addKeyListener(new KeyAdapter(){

			@Override
			public void keyPressed(KeyEvent e) {
				if(!queryField.isEditable()){
					if(e.getKeyChar() == KeyEvent.VK_DELETE || e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
						deleteSelection();
					}
				}
				else {
					if(e.getKeyChar() == KeyEvent.VK_ENTER) {
						resetButton.doClick(); // we are in manual edit mode, so the reset button is now the Parse Query button
						e.consume();
					}
				}

			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 4;

		queryPanel.add(queryScrollPane,gbc);
	}

	private void initQuantificationPanel(final TAPNQuery queryToCreateFrom) {
		// Quantification Panel
		quantificationPanel = new JPanel(new GridBagLayout());
		quantificationPanel.setBorder(BorderFactory.createTitledBorder("Quantification"));
		quantificationRadioButtonGroup = new ButtonGroup();

		existsDiamond = new JRadioButton("(EF) There exists some reachable marking that satisifies:");
		existsBox = new JRadioButton("(EG) There exists a trace on which every marking satisfies:");
		forAllDiamond = new JRadioButton("(AF) On all traces there is eventually a marking that satisfies:");
		forAllBox = new JRadioButton("(AG) All reachable markings satisfy:");

		quantificationRadioButtonGroup.add(existsDiamond);
		quantificationRadioButtonGroup.add(existsBox);
		quantificationRadioButtonGroup.add(forAllDiamond);
		quantificationRadioButtonGroup.add(forAllBox);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		quantificationPanel.add(existsDiamond, gbc);

		// bit of a hack, possible because quantifier node is always the first node (we cant have nested quantifiers)
		if (queryToCreateFrom != null){
			if (queryToCreateFrom.getProperty() instanceof TCTLEFNode){
				existsDiamond.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLEGNode){
				existsBox.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLAFNode){
				forAllDiamond.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLAGNode){
				forAllBox.setSelected(true);
			}
		}

		gbc.gridy = 1;
		quantificationPanel.add(existsBox, gbc);

		gbc.gridy = 2;
		quantificationPanel.add(forAllDiamond, gbc);

		gbc.gridy = 3;
		quantificationPanel.add(forAllBox, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(quantificationPanel,gbc);

		//Add action listeners to the query options
		existsBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLEGNode property = new TCTLEGNode(getCurrentSelectionChild(1,currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);	
				undoSupport.postEdit(edit);
			}
		});

		existsDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLEFNode property = new TCTLEFNode(getCurrentSelectionChild(1,currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		});

		forAllBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAGNode property = new TCTLAGNode(getCurrentSelectionChild(1,currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		});

		forAllDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAFNode property = new TCTLAFNode(getCurrentSelectionChild(1,currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		});
	}

	private void initLogicPanel() {
		// Logic panel
		logicButtonPanel = new JPanel(new GridBagLayout());
		logicButtonPanel.setBorder(BorderFactory.createTitledBorder("Logic"));

		logicButtonGroup = new ButtonGroup();
		conjunctionButton = new JButton("And");
		disjunctionButton = new JButton("Or");
		negationButton = new JButton("not");

		logicButtonGroup.add(conjunctionButton);
		logicButtonGroup.add(disjunctionButton);
		logicButtonGroup.add(negationButton);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		logicButtonPanel.add(conjunctionButton,gbc);

		gbc.gridy = 1;
		logicButtonPanel.add(disjunctionButton,gbc);

		gbc.gridy = 2;
		logicButtonPanel.add(negationButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(logicButtonPanel,gbc);

		// Add Action listener for logic buttons
		conjunctionButton.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						TCTLAndListNode andListNode = null;
						if(currentSelection.getObject() instanceof TCTLAndListNode) {
							andListNode = new TCTLAndListNode((TCTLAndListNode)currentSelection.getObject());
							andListNode.addConjunct(new TCTLStatePlaceHolder());
							UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), andListNode);
							newProperty = newProperty.replace(currentSelection.getObject(), andListNode);
							updateSelection(andListNode); 
							undoSupport.postEdit(edit);
						}
						else if(currentSelection.getObject() instanceof TCTLOrListNode) {
							andListNode = new TCTLAndListNode(((TCTLOrListNode)currentSelection.getObject()).getProperties());
							UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), andListNode);
							newProperty = newProperty.replace(currentSelection.getObject(), andListNode);
							updateSelection(andListNode);
							undoSupport.postEdit(edit);
						}
						else if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) 
						{
							TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty)currentSelection.getObject();
							TCTLAbstractProperty parentNode = prop.getParent();

							if(parentNode instanceof TCTLAndListNode) {
								// current selection is child of an andList node => add new placeholder conjunct to it
								andListNode = new TCTLAndListNode((TCTLAndListNode)parentNode);
								andListNode.addConjunct(new TCTLStatePlaceHolder());
								UndoableEdit edit = new QueryConstructionEdit(parentNode, andListNode);
								newProperty = newProperty.replace(parentNode, andListNode);
								updateSelection(andListNode); 
								undoSupport.postEdit(edit);
							}
							else {
								TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
								andListNode = new TCTLAndListNode(getStateProperty(currentSelection.getObject()),ph);
								UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), andListNode);
								newProperty = newProperty.replace(currentSelection.getObject(), andListNode);
								updateSelection(andListNode);
								undoSupport.postEdit(edit);
							}
						}
					}

				}


		);	

		disjunctionButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TCTLOrListNode orListNode;
						if(currentSelection.getObject() instanceof TCTLOrListNode) {
							orListNode = new TCTLOrListNode((TCTLOrListNode)currentSelection.getObject());
							orListNode.addDisjunct(new TCTLStatePlaceHolder());
							UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), orListNode);
							newProperty = newProperty.replace(currentSelection.getObject(), orListNode);
							updateSelection(orListNode); 
							undoSupport.postEdit(edit);
						}
						else if(currentSelection.getObject() instanceof TCTLAndListNode) {
							orListNode = new TCTLOrListNode(((TCTLAndListNode)currentSelection.getObject()).getProperties());
							UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), orListNode);
							newProperty = newProperty.replace(currentSelection.getObject(), orListNode);
							updateSelection(orListNode);
							undoSupport.postEdit(edit);
						}
						else if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) 
						{
							TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty)currentSelection.getObject();
							TCTLAbstractProperty parentNode = prop.getParent();

							if(parentNode instanceof TCTLOrListNode) {
								// current selection is child of an orList node => add new placeholder disjunct to it
								orListNode = new TCTLOrListNode((TCTLOrListNode)parentNode);
								orListNode.addDisjunct(new TCTLStatePlaceHolder());
								UndoableEdit edit = new QueryConstructionEdit(parentNode, orListNode);
								newProperty = newProperty.replace(parentNode, orListNode);
								updateSelection(orListNode); 
								undoSupport.postEdit(edit);
							}
							else {
								TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
								orListNode = new TCTLOrListNode(getStateProperty(currentSelection.getObject()),ph);
								UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), orListNode);
								newProperty = newProperty.replace(currentSelection.getObject(), orListNode);
								updateSelection(orListNode);
								undoSupport.postEdit(edit);
							}
						}
					}


				}
		);

		negationButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TCTLNotNode property = new TCTLNotNode(getStateProperty(currentSelection.getObject()));
						UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
						newProperty = newProperty.replace(currentSelection.getObject(), property);
						updateSelection(property);
						undoSupport.postEdit(edit);
					}
				}
		);
	}


	private void initPredicationConstructionPanel() {
		// Predicate specification panel
		predicatePanel = new JPanel(new GridBagLayout());
		predicatePanel.setBorder(BorderFactory.createTitledBorder("Predicates"));

		String[] places = new String[datalayer.getPlaces().length];
		for (int i=0; i< places.length; i++){
			places[i] = datalayer.getPlaces()[i].getName();
		}
		placesBox = new JComboBox(new DefaultComboBoxModel(places));

		Dimension d = placesBox.getMaximumSize();		
		d.width = 150;
		placesBox.setMaximumSize(d);



		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		predicatePanel.add(placesBox,gbc);

		String[] relationalSymbols= {"=","<=","<",">=",">"};
		relationalOperatorBox = new JComboBox(new DefaultComboBoxModel(relationalSymbols));

		gbc.gridx = 1;
		predicatePanel.add(relationalOperatorBox,gbc);

		int currentValue = 0;
		int min = 0;
		int step = 1;		
		placeMarking = new JSpinner(new SpinnerNumberModel(currentValue, min, Integer.MAX_VALUE, step));
		placeMarking.setMaximumSize(new Dimension(50,30));
		placeMarking.setMinimumSize(new Dimension(50,30));
		placeMarking.setPreferredSize(new Dimension(50,30));

		gbc.gridx = 2;
		predicatePanel.add(placeMarking,gbc);

		addPredicateButton = new JButton("Add Predicate to Query");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		predicatePanel.add(addPredicateButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(predicatePanel,gbc);

		// Action listeners for predicate panel
		addPredicateButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode((String)placesBox.getSelectedItem(), (String)relationalOperatorBox.getSelectedItem(), (Integer) placeMarking.getValue());
						UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
						newProperty = newProperty.replace(currentSelection.getObject(), property);
						updateSelection(property);
						undoSupport.postEdit(edit);
					}
				}

		);

		placesBox.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(userChangedAtomicPropSelection) {
							updateQueryOnAtomicPropositionChange();
						}
					}
				}
		);

		relationalOperatorBox.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(userChangedAtomicPropSelection) {
							updateQueryOnAtomicPropositionChange();
						}
						
					}
				}
		);

		placeMarking.addChangeListener(
				new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						if(userChangedAtomicPropSelection) {
							updateQueryOnAtomicPropositionChange();
						}
					}
				}
		);
	}



	private void initQueryEditingPanel() {
		// Editing buttons panel
		editingButtonPanel = new JPanel(new GridBagLayout());
		editingButtonPanel.setBorder(BorderFactory.createTitledBorder("Editing"));

		editingButtonsGroup = new ButtonGroup();
		deleteButton = new JButton("Delete Selection");
		resetButton = new JButton("Reset Query");
		undoButton = new JButton("Undo");
		redoButton = new JButton("Redo");
		editQueryButton = new JButton("Edit Query");

		editingButtonsGroup.add(deleteButton);
		editingButtonsGroup.add(resetButton);
		editingButtonsGroup.add(undoButton);
		editingButtonsGroup.add(redoButton);
		editingButtonsGroup.add(editQueryButton);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		editingButtonPanel.add(undoButton,gbc);

		gbc.gridx = 1;
		editingButtonPanel.add(redoButton,gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		editingButtonPanel.add(deleteButton,gbc);

		gbc.gridy = 2;
		editingButtonPanel.add(resetButton, gbc);

		gbc.gridy = 3;
		editingButtonPanel.add(editQueryButton,gbc);



		// Add action Listeners
		deleteButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						deleteSelection();
					}
				}	
		);

		resetButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						if(queryField.isEditable()) { // in edit mode, this button is now the parse query button. User has potentially altered the query, so try to parse it
							TAPAALQueryParser queryParser = new TAPAALQueryParser();
							TCTLAbstractProperty newQuery = null;

							try {
								newQuery = queryParser.parse(queryField.getText());
							} catch (Exception ex) {
								int choice = JOptionPane.showConfirmDialog(CreateGui.getApp(), "TAPAAL encountered an error trying to parse the specified query.\n\nWe recommend using the query construction buttons unless you are an experienced user.\n\n The specified query has not been saved. Do you want to edit it again?", "Error Parsing Query", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
								if(choice == JOptionPane.NO_OPTION)
								{
									returnFromManualEdit(null);
								}
							}

							if(newQuery != null) // new query parsed successfully
							{
								// check correct place names are used in atomic propositions
								ArrayList<String> places = new ArrayList<String>();
								for (int i=0; i< datalayer.getPlaces().length; i++){
									places.add(datalayer.getPlaces()[i].getName());
								}

								VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(places);

								VerifyPlaceNamesVisitor.Context c = nameChecker.VerifyPlaceNames(newQuery);

								if(!c.getResult())
								{
									StringBuilder s = new StringBuilder();
									s.append("The following places was used in the query, but are not present in your model:\n\n");

									for (String placeName : c.getIncorrectPlaceNames()) {
										s.append(placeName);
										s.append("\n");
									}

									s.append("\nThe specified query has not been saved. Do you want to edit it again?");
									int choice = JOptionPane.showConfirmDialog(CreateGui.getApp(), s.toString(), "Error Parsing Query", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
									if(choice == JOptionPane.NO_OPTION)
									{
										returnFromManualEdit(null);
									}
								} 
								else {
									UndoableEdit edit = new QueryConstructionEdit(newProperty, newQuery);
									returnFromManualEdit(newQuery);
									undoSupport.postEdit(edit);
								}
							}
							else
							{
								returnFromManualEdit(null);
							}
						}
						else { // we are not in edit mode so the button should reset the query

							TCTLPathPlaceHolder ph = new TCTLPathPlaceHolder();
							UndoableEdit edit = new QueryConstructionEdit(newProperty, ph);
							newProperty = ph;
							resetQuantifierSelectionButtons();
							updateSelection(newProperty);
							undoSupport.postEdit(edit);
						}
					}
				}
		);

		undoButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						UndoableEdit edit = undoManager.GetNextEditToUndo();

						if(edit instanceof QueryConstructionEdit)
						{
							TCTLAbstractProperty original = ((QueryConstructionEdit)edit).getOriginal();
							undoManager.undo();
							refreshUndoRedo();
							updateSelection(original);
						}

					}
				}
		);

		redoButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						UndoableEdit edit = undoManager.GetNextEditToRedo();
						if(edit instanceof QueryConstructionEdit)
						{
							TCTLAbstractProperty replacement = ((QueryConstructionEdit)edit).getReplacement();
							undoManager.redo();
							refreshUndoRedo();
							updateSelection(replacement);
						}
					}
				}
		);

		editQueryButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						if(queryField.isEditable()) { // we are in edit mode so the user pressed cancel
							returnFromManualEdit(null);
						}
						else { // user wants to edit query manually
							changeToEditMode();
						}
					}
				}
		);



		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(editingButtonPanel,gbc);
	}




	private void initUppaalOptionsPanel(final TAPNQuery queryToCreateFrom) {

		uppaalOptionsPanel = new JPanel(new GridBagLayout());

		initSearchOptionsPanel(queryToCreateFrom);
		initTraceOptionsPanel(queryToCreateFrom);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		add(uppaalOptionsPanel, gridBagConstraints);

	}

	private void initSearchOptionsPanel(final TAPNQuery queryToCreateFrom) {
		//verification option-radio buttons starts here:		
		searchOptionsPanel = new JPanel(new GridBagLayout());

		searchOptionsPanel.setBorder(BorderFactory.createTitledBorder("Analysis Options"));
		searchRadioButtonGroup = new ButtonGroup();
		bFS = new JRadioButton("Breadth First Search");
		dFS = new JRadioButton("Depth First Search");
		rDFS = new JRadioButton("Random Depth First Search");
		closestToTargetFirst = new JRadioButton("Search by Closest To Target First");
		searchRadioButtonGroup.add(bFS);
		searchRadioButtonGroup.add(dFS);
		searchRadioButtonGroup.add(rDFS);
		searchRadioButtonGroup.add(closestToTargetFirst);

		if (queryToCreateFrom==null){
			bFS.setSelected(true);
		}else{
			if (queryToCreateFrom.getSearchOption() == SearchOption.BFS){
				bFS.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.DFS){
				dFS.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.RDFS){
				rDFS.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.CLOSE_TO_TARGET_FIRST){
				closestToTargetFirst.setSelected(true);
			}	
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		searchOptionsPanel.add(bFS,gridBagConstraints);
		gridBagConstraints.gridy = 1;
		searchOptionsPanel.add(dFS,gridBagConstraints);
		gridBagConstraints.gridy = 2;
		searchOptionsPanel.add(rDFS,gridBagConstraints);
		gridBagConstraints.gridy = 3;
		searchOptionsPanel.add(closestToTargetFirst,gridBagConstraints);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		uppaalOptionsPanel.add(searchOptionsPanel, gridBagConstraints);

	}

	private void initTraceOptionsPanel(final TAPNQuery queryToCreateFrom) {
		traceOptionsPanel = new JPanel(new GridBagLayout());
		traceOptionsPanel.setBorder(BorderFactory.createTitledBorder("Trace Options"));
		traceRadioButtonGroup = new ButtonGroup();
		some = new JRadioButton("Some encountered trace (only without symmetry reduction)");
		fastest = new JRadioButton("Fastest trace (only without symmetry reduction)");
		none = new JRadioButton("No trace");
		traceRadioButtonGroup.add(some);
		traceRadioButtonGroup.add(fastest);
		traceRadioButtonGroup.add(none);

		if (queryToCreateFrom==null){
			some.setEnabled(false);
			fastest.setEnabled(false);
			none.setSelected(true);
		}else{
			if (queryToCreateFrom.getTraceOption() == TraceOption.SOME){
				some.setSelected(true);
			} else if (queryToCreateFrom.getTraceOption() == TraceOption.FASTEST){
				fastest.setSelected(true);
			} else if (queryToCreateFrom.getTraceOption() == TraceOption.NONE){
				none.setSelected(true);
			}	
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		traceOptionsPanel.add(some,gridBagConstraints);
		gridBagConstraints.gridy = 1;
		traceOptionsPanel.add(fastest,gridBagConstraints);
		gridBagConstraints.gridy = 2;
		traceOptionsPanel.add(none,gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		uppaalOptionsPanel.add(traceOptionsPanel, gridBagConstraints);

	}

	private void initReductionOptionsPanel(final TAPNQuery queryToCreateFrom) {


		//ReductionOptions starts here:
		reductionOptionsPanel = new JPanel(new FlowLayout());
		reductionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Reduction Options"));
		String[] reductionOptions = {name_NAIVE, name_ADVNOSYM, name_BROADCAST, name_BROADCASTDEG2};
		reductionOption = new JComboBox(reductionOptions);
		reductionOption.setSelectedIndex(3);


		reductionOptionsPanel.add(new JLabel("  Choose reduction method:"));
		reductionOptionsPanel.add(reductionOption);

		symmetryReduction = new JCheckBox("Use Symmetry Reduction");
		symmetryReduction.setSelected(true);
		symmetryReduction.addItemListener(new ItemListener(){


			public void itemStateChanged(ItemEvent e) {
				refreshTraceOptions();
			}
		});

		reductionOptionsPanel.add(symmetryReduction);

		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		add(reductionOptionsPanel, gridBagConstraints);

		//Update the selected reduction
		if (queryToCreateFrom!=null){
			String reduction = "";
			boolean symmetry = false;

			if(queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST){
				reduction = name_BROADCAST;
				symmetry = false;
				//enableTraceOptions();
			}else if(queryToCreateFrom.getReductionOption() == ReductionOption.BROADCASTSYMMETRY){
				reduction = name_BROADCAST;
				symmetry = true;
				//disableTraceOptions();
			}else if(queryToCreateFrom.getReductionOption() == ReductionOption.DEGREE2BROADCAST){
				reduction = name_BROADCASTDEG2;
				symmetry = false;
				//disableTraceOptions();
			}else if(queryToCreateFrom.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY){
				reduction = name_BROADCASTDEG2;
				symmetry = true;
				//disableTraceOptions();
			}
			else if (getQuantificationSelection().equals("E<>") || getQuantificationSelection().equals("A[]")){
				if (queryToCreateFrom.getReductionOption() == ReductionOption.STANDARD){
					reduction = name_NAIVE;
					symmetry = false;
					//enableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.STANDARDSYMMETRY){
					reduction = name_NAIVE;
					symmetry = true;
					//disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY){
					reduction = name_ADVNOSYM;
					symmetry = true;
					//disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD){
					reduction = name_ADVNOSYM;
					symmetry = false;
					//enableTraceOptions();
				}
			} else {
				if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY){
					reduction = name_ADVNOSYM;
					symmetry = true;
					//disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD){
					reduction = name_ADVNOSYM;
					symmetry = false;
					//enableTraceOptions();
				}
			}

			reductionOption.setSelectedItem(reduction);
			symmetryReduction.setSelected(symmetry);
		}

	}

	private void initButtonPanel(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		buttonPanel = new JPanel(new FlowLayout());
		if (option == QueryDialogueOption.Save){
			saveButton = new JButton("Save");
			saveAndVerifyButton = new JButton("Save and Verify");
			cancelButton = new JButton("Cancel");
			saveUppaalXMLButton = new JButton("Save UPPAAL XML");

			saveButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							//TODO make save
							//save();
							querySaved = true;
							exit();
						}
					}
			);
			saveAndVerifyButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							querySaved = true;
							exit();
							Verifier.runUppaalVerification(CreateGui.getModel(), getQuery());
						}
					}
			);
			cancelButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							exit();
						}
					}
			);

			saveUppaalXMLButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							querySaved = true;

							String xmlFile = null, queryFile = null;
							try {
								FileBrowser browser = new FileBrowser("Uppaal XML","xml",xmlFile);
								xmlFile = browser.saveFile();
								if(xmlFile != null)
								{
									String[] a = xmlFile.split(".xml");
									queryFile= a[0]+".q";
								}

							} catch (Exception ex) {
								JOptionPane.showMessageDialog(CreateGui.getApp(),
										"There were errors performing the requested action:\n" + e,
										"Error", JOptionPane.ERROR_MESSAGE
								);				
							}

							if(xmlFile != null && queryFile != null){
								PipeTapnToAauTapnTransformer transformer = new PipeTapnToAauTapnTransformer();

								TAPN model=null;
								try {
									model = transformer.getAAUTAPN(CreateGui.getModel(), 0);
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(QueryDialogue.this, "An error occured during export.", "Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								
								UppaalExporter exporter = new UppaalExporter();
								TAPNQuery query = getQuery();
								dk.aau.cs.petrinet.TAPNQuery tapnQuery = new dk.aau.cs.petrinet.TAPNQuery(query.getProperty(), query.getCapacity() + model.getNumberOfTokens());
								exporter.export(model, tapnQuery, query.getReductionOption(), new File(xmlFile), new File(queryFile));
								//Export.exportUppaalXMLFromQuery(CreateGui.getModel(), getQuery(), xmlFile, queryFile);
							}else{
								JOptionPane.showMessageDialog(CreateGui.getApp(), "No Uppaal XML file saved.");
							}
						}
					}
			);
		}else if (option == QueryDialogueOption.Export){
			saveButton = new JButton("export");
			cancelButton = new JButton("Cancel");

			saveButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							querySaved = true;
							exit();
						}
					}
			);
			cancelButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							exit();
						}
					}
			);		
		}
		if (option == QueryDialogueOption.Save){
			buttonPanel.add(cancelButton);

			buttonPanel.add(saveButton);

			buttonPanel.add(saveAndVerifyButton);

			buttonPanel.add(saveUppaalXMLButton);
		}else {
			buttonPanel.add(cancelButton);

			buttonPanel.add(saveButton);

			//			buttonPanel.add(saveUppaalXMLButton);
		}


		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		add(buttonPanel, gridBagConstraints);

	}


	///////////////////////////////////////////////////////////////////////
	// Undo support stuff
	///////////////////////////////////////////////////////////////////////
	private void refreshUndoRedo() {
		undoButton.setEnabled(undoManager.canUndo());
		redoButton.setEnabled(undoManager.canRedo());
	}


	private class UndoAdapter implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent arg0) {
			UndoableEdit edit = arg0.getEdit();
			undoManager.addEdit( edit );
			refreshUndoRedo();
		}
	}

	private class QueryConstructionUndoManager extends UndoManager {
		private static final long serialVersionUID = 1L;

		public UndoableEdit GetNextEditToUndo() { return editToBeUndone(); }
		public UndoableEdit GetNextEditToRedo() { return editToBeRedone(); }
	}

	public class QueryConstructionEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1L;

		private TCTLAbstractProperty original;
		private TCTLAbstractProperty replacement;

		public TCTLAbstractProperty getOriginal() {
			return original;
		}

		public TCTLAbstractProperty getReplacement() {
			return replacement;
		}

		public QueryConstructionEdit(TCTLAbstractProperty original, TCTLAbstractProperty replacement) {
			this.original = original;
			this.replacement = replacement;
		}

		@Override
		public void undo() throws CannotUndoException {
			newProperty = newProperty.replace(replacement, original);
		}

		@Override
		public void redo() throws CannotRedoException {
			newProperty = newProperty.replace(original, replacement);
		}

		@Override
		public boolean canUndo() { return true; }

		@Override
		public boolean canRedo() { return true; }

	}


}