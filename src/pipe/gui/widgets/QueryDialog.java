package pipe.gui.widgets;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import pipe.dataLayer.*;
import pipe.dataLayer.TAPNQuery.*;
import pipe.gui.*;
import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.Parsing.*;
import dk.aau.cs.TCTL.visitors.*;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.UPPAAL.UppaalExporter;

public class QueryDialog extends JPanel {

	private static final long serialVersionUID = 7852107237344005546L;

	public enum QueryDialogueOption {
		VerifyNow, Save, Export
	}

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
	private JComboBox templateBox;
	private JComboBox placesBox;
	private JComboBox relationalOperatorBox;
	private JSpinner placeMarking;

	// Uppaal options panel (search + trace options)
	// search options panel
	private JPanel searchOptionsPanel;
	private JPanel uppaalOptionsPanel;
	private ButtonGroup searchRadioButtonGroup;
	private JRadioButton breadthFirstSearch;
	private JRadioButton depthFirstSearch;
	private JRadioButton randomDepthFirstSearch;
	private JRadioButton closestToTargetFirstSearch;

	// Trace options panel
	private JPanel traceOptionsPanel;

	private ButtonGroup traceRadioButtonGroup;
	private JRadioButton noTraceRadioButton;
	private JRadioButton someTraceRadioButton;
	private JRadioButton fastestTraceRadioButton;

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

	private DataLayer datalayer; // TODO: Get rid of
	private final TimedArcPetriNetNetwork tapnNetwork;
	private QueryConstructionUndoManager undoManager;
	private UndoableEditSupport undoSupport;

	private String name_verifyTAPN = "VerifyTAPN";
	private String name_ADVNOSYM = "Optimised Standard";
	private String name_NAIVE = "Standard";
	private String name_BROADCAST = "Broadcast Reduction";
	private String name_BROADCASTDEG2 = "Broadcast Degree 2 Reduction";
	private boolean userChangedAtomicPropSelection = true;

	private TCTLAbstractProperty newProperty;

	public QueryDialog(EscapableDialog me, DataLayer datalayer,
			QueryDialogueOption option, TAPNQuery queryToCreateFrom,
			TimedArcPetriNetNetwork tapnNetwork) {
		this.tapnNetwork = tapnNetwork;
		this.datalayer = datalayer;
		this.newProperty = queryToCreateFrom == null ? new TCTLPathPlaceHolder() : queryToCreateFrom.getProperty();
		rootPane = me.getRootPane();
		setLayout(new GridBagLayout());

		init(option, queryToCreateFrom);
	}

	private void setQueryFieldEditable(boolean isEditable) {
		queryField.setEditable(isEditable);
	}

	public TAPNQuery getQuery() {
		if (!querySaved) {
			return null;
		}

		String name = getQueryComment();
		int capacity = getCapacity();

		TAPNQuery.TraceOption traceOption = null;
		String traceOptionString = getTraceOption();

		if (traceOptionString.toLowerCase().contains("some")) {
			traceOption = TraceOption.SOME;
		} else if (traceOptionString.toLowerCase().contains("fastest")) {
			traceOption = TraceOption.FASTEST;
		} else if (traceOptionString.toLowerCase().contains("no")) {
			traceOption = TraceOption.NONE;
		}

		TAPNQuery.SearchOption searchOption = null;
		String searchOptionString = getSearchOption();

		if (searchOptionString.toLowerCase().contains("breadth")) {
			searchOption = SearchOption.BFS;
		} else if (searchOptionString.toLowerCase().contains("depth")
				&& (!searchOptionString.toLowerCase().contains("random"))) {
			searchOption = SearchOption.DFS;
		} else if (searchOptionString.toLowerCase().contains("depth")
				&& searchOptionString.toLowerCase().contains("random")) {
			searchOption = SearchOption.RDFS;
		} else if (searchOptionString.toLowerCase().contains("target")) {
			searchOption = SearchOption.CLOSE_TO_TARGET_FIRST;
		}

		ReductionOption reductionOptionToSet = null;
		String reductionOptionString = (String)reductionOption.getSelectedItem();
		boolean symmetry = symmetryReduction.isSelected();

		if (reductionOptionString.equals(name_NAIVE) && !symmetry) {
			reductionOptionToSet = ReductionOption.STANDARD;
		} else if (reductionOptionString.equals(name_NAIVE) && symmetry) {
			reductionOptionToSet = ReductionOption.STANDARDSYMMETRY;
		} else if (reductionOptionString.equals(name_ADVNOSYM) && !symmetry) {
			reductionOptionToSet = ReductionOption.OPTIMIZEDSTANDARD;
		} else if (reductionOptionString.equals(name_ADVNOSYM) && symmetry) {
			reductionOptionToSet = ReductionOption.OPTIMIZEDSTANDARDSYMMETRY;
		} else if (reductionOptionString.equals(name_BROADCAST) && !symmetry) {
			reductionOptionToSet = ReductionOption.BROADCAST;
		} else if (reductionOptionString.equals(name_BROADCAST) && symmetry) {
			reductionOptionToSet = ReductionOption.BROADCASTSYMMETRY;
		} else if (reductionOptionString.equals(name_BROADCASTDEG2)
				&& !symmetry) {
			reductionOptionToSet = ReductionOption.DEGREE2BROADCAST;
		} else if (reductionOptionString.equals(name_BROADCASTDEG2) && symmetry) {
			reductionOptionToSet = ReductionOption.DEGREE2BROADCASTSYMMETRY;
		} else if (reductionOptionString.equals(name_verifyTAPN)) {
			reductionOptionToSet = ReductionOption.VerifyTAPN;
		}

		return new TAPNQuery(name, capacity, newProperty.copy(), traceOption,
				searchOption, reductionOptionToSet, /* hashTableSizeToSet */
				null, /* extrapolationOptionToSet */null);
	}

	private int getCapacity() {
		return (Integer) ((JSpinner) boundednessCheckPanel.getComponent(1)).getValue();
	}

	private String getQueryComment() {
		return ((JTextField) namePanel.getComponent(1)).getText();
	}

	private String getTraceOption() {
		String toReturn = null;
		for (Object radioButton : traceOptionsPanel.getComponents()) {
			if ((radioButton instanceof JRadioButton)) {
				if (((JRadioButton) radioButton).isSelected()) {
					toReturn = ((JRadioButton) radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private String getSearchOption() {
		String toReturn = null;
		for (Object radioButton : searchOptionsPanel.getComponents()) {
			if ((radioButton instanceof JRadioButton)) {
				if (((JRadioButton) radioButton).isSelected()) {

					toReturn = ((JRadioButton) radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private void refreshTraceOptions() {
		if(((String)reductionOption.getSelectedItem()).equals(name_verifyTAPN)) {
			someTraceRadioButton.setEnabled(true);
			fastestTraceRadioButton.setEnabled(false);
			noTraceRadioButton.setSelected(true);
		}
		else if (symmetryReduction.isSelected()) {
			someTraceRadioButton.setEnabled(false);
			fastestTraceRadioButton.setEnabled(false);
			noTraceRadioButton.setSelected(true);
		} else {
			someTraceRadioButton.setEnabled(true);
			fastestTraceRadioButton.setEnabled(true);
		}
	}
	
	private void refreshSearchOptions() {
		if(((String)reductionOption.getSelectedItem()).equals(name_verifyTAPN))
		{
			depthFirstSearch.setEnabled(true);
			breadthFirstSearch.setEnabled(true);
			breadthFirstSearch.setSelected(true);
			randomDepthFirstSearch.setEnabled(false);
			closestToTargetFirstSearch.setEnabled(false);
		}
		else {
			depthFirstSearch.setEnabled(true);
			breadthFirstSearch.setEnabled(true);
			randomDepthFirstSearch.setEnabled(true);
			closestToTargetFirstSearch.setEnabled(true);
		}
		
	}

	private void resetQuantifierSelectionButtons() {
		quantificationRadioButtonGroup.clearSelection();
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	public String getQuantificationSelection() {
		if (existsDiamond.isSelected()) {
			return "E<>";
		} else if (existsBox.isSelected()) {
			return "E[]";
		} else if (forAllDiamond.isSelected()) {
			return "A<>";
		} else if (forAllBox.isSelected()) {
			return "A[]";
		} else {
			return "";
		}
	}

	private void enableOnlyLivenessReductionOptions() {
		String[] options = new String[]{name_BROADCASTDEG2, name_BROADCAST};

		String reductionOptionString = "" + reductionOption.getSelectedItem();
		boolean selectedOptionStillAvailable = false;

		reductionOption.removeAllItems();

		for (String s : options) {
			reductionOption.addItem(s);
			if (s.equals(reductionOptionString)) {
				selectedOptionStillAvailable = true;
			}
		}

		if (selectedOptionStillAvailable)
			reductionOption.setSelectedItem(reductionOptionString);

	}

	private void enableAllReductionOptions() {
		reductionOption.removeAllItems();

		if(!this.datalayer.hasTAPNInhibitorArcs()){
			String[] options = null;
			if(!datalayer.hasTransportArcs() && !datalayer.hasInvariants()) {
				options = new String[] { name_verifyTAPN, name_ADVNOSYM, name_NAIVE, name_BROADCAST, name_BROADCASTDEG2};
			}else {
				options = new String[] { name_ADVNOSYM, name_NAIVE, name_BROADCAST, name_BROADCASTDEG2};

			}
			for (String s : options) {
				reductionOption.addItem(s);
			}
		} else {	
			reductionOption.addItem(name_BROADCAST);
			reductionOption.addItem(name_BROADCASTDEG2);
		}

		reductionOption.setSelectedItem("" + name_ADVNOSYM);

	}

	public static TAPNQuery ShowUppaalQueryDialogue(QueryDialogueOption option,
			TAPNQuery queryToRepresent, TimedArcPetriNetNetwork tapnNetwork) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(),	Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add query editor
		QueryDialog queryDialogue = new QueryDialog(guiDialog, CreateGui.getModel(), option, queryToRepresent, tapnNetwork);
		contentPane.add(queryDialogue);

		guiDialog.setResizable(false);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

		return queryDialogue.getQuery();
	}

	private TCTLAbstractStateProperty getStateProperty(TCTLAbstractProperty property) {
		if (property instanceof TCTLAbstractStateProperty) {
			return (TCTLAbstractStateProperty) property.copy();
		} else {
			return new TCTLStatePlaceHolder();
		}
	}

	private TCTLAbstractStateProperty getSpecificChildOfProperty(int number, TCTLAbstractProperty property) {
		StringPosition[] children = property.getChildren();
		int count = 0;
		for (int i = 0; i < children.length; i++) {
			TCTLAbstractProperty child = children[i].getObject();
			if (child instanceof TCTLAbstractStateProperty) {
				count++;
				if (count == number) {
					return (TCTLAbstractStateProperty) child;
				}
			}
		}
		return new TCTLStatePlaceHolder();
	}

	// Update current selection based on position of the caret in the string
	// representation
	// used for updating when selecting with the mouse.
	private void updateSelection() {
		int index = queryField.getCaretPosition();
		StringPosition position = newProperty.objectAt(index);
		if (position == null)
			return;
		queryField.select(position.getStart(), position.getEnd());
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

		if (newProperty.containsPlaceHolder()) {
			TCTLAbstractProperty ph = newProperty.findFirstPlaceHolder();
			position = newProperty.indexOf(ph);
		} else {
			position = newProperty.indexOf(newSelection);
		}

		queryField.select(position.getStart(), position.getEnd());
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
		if (currentSelection.getObject() instanceof TCTLAtomicPropositionNode) {
			TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) currentSelection.getObject();

			// bit of a hack to prevent posting edits to the undo manager when
			// we programmatically change the selection in the atomic proposition comboboxes etc.
			// because a different atomic proposition was selected
			userChangedAtomicPropSelection = false;
			templateBox.setSelectedItem(tapnNetwork.getTAPNByName(node.getTemplate()));
			placesBox.setSelectedItem(node.getPlace());
			relationalOperatorBox.setSelectedItem(node.getOp());
			placeMarking.setValue(node.getN());
			userChangedAtomicPropSelection = true;
		} else if (currentSelection.getObject() instanceof TCTLEFNode) {
			existsDiamond.setSelected(true);
		} else if (currentSelection.getObject() instanceof TCTLEGNode) {
			existsBox.setSelected(true);
		} else if (currentSelection.getObject() instanceof TCTLAGNode) {
			forAllBox.setSelected(true);
		} else if (currentSelection.getObject() instanceof TCTLAFNode) {
			forAllDiamond.setSelected(true);
		}
	}

	private void deleteSelection() {
		if (currentSelection != null) {
			TCTLAbstractProperty replacement = null;
			if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
				replacement = getSpecificChildOfProperty(1, currentSelection.getObject());
			} else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
				replacement = new TCTLPathPlaceHolder();
			}
			if (replacement != null) {

				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), replacement);

				newProperty = newProperty.replace(currentSelection.getObject(),	replacement);

				if (currentSelection.getObject() instanceof TCTLAbstractPathProperty)
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

	private void setSaveButtonsEnabled() {
		if (!queryField.isEditable()) {
			boolean isQueryOk = getQueryComment().length() > 0
			&& !newProperty.containsPlaceHolder();
			saveButton.setEnabled(isQueryOk);
			saveAndVerifyButton.setEnabled(isQueryOk);
			saveUppaalXMLButton.setEnabled(isQueryOk);
		} else {
			saveButton.setEnabled(false);
			saveAndVerifyButton.setEnabled(false);
			saveUppaalXMLButton.setEnabled(false);
		}
	}
	private void setEnabledReductionOptions(boolean keepSelection) {
		String reductionOptionString = ""+reductionOption.getSelectedItem();
		setEnabledReductionOptions();
		reductionOption.setSelectedItem(reductionOptionString);
	}
	private void setEnabledReductionOptions() {
		if (getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")) {
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
		templateBox.setEnabled(false);
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
		templateBox.setEnabled(false);
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
		templateBox.setEnabled(true);
		placesBox.setEnabled(true);
		relationalOperatorBox.setEnabled(true);
		placeMarking.setEnabled(true);
		setEnablednessOfAddPredicateButton();

	}

	private void setEnablednessOfAddPredicateButton() {
		if (placesBox.getSelectedItem() == null)
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
		if (currentSelection != null)
			deleteButton.setEnabled(true);
	}

	private void returnFromManualEdit(TCTLAbstractProperty newQuery) {
		setQueryFieldEditable(false);

		if (newQuery != null)
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
		if (currentSelection != null && currentSelection.getObject() instanceof TCTLAtomicPropositionNode) {
			TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
					templateBox.getSelectedItem().toString(),
					(String) placesBox.getSelectedItem(),
					(String) relationalOperatorBox.getSelectedItem(),
					(Integer) placeMarking.getValue());
			if (!property.equals(currentSelection.getObject())) {
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),	property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////
	// Initialization of the dialogue
	// /////////////////////////////////////////////////////////////////////

	private void init(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		initQueryNamePanel(queryToCreateFrom);
		initBoundednessCheckPanel(queryToCreateFrom);
		initQueryPanel(queryToCreateFrom);
		initUppaalOptionsPanel(queryToCreateFrom);
		initReductionOptionsPanel(queryToCreateFrom);
		initButtonPanel(option, queryToCreateFrom);

		rootPane.setDefaultButton(saveButton);
		disableAllQueryButtons();
		setSaveButtonsEnabled();

		// initilize the undo.redo system
		undoManager = new QueryConstructionUndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoAdapter());
		refreshUndoRedo();

		setEnabledReductionOptions(true);
		refreshTraceOptions();

	}

	private void initQueryNamePanel(final TAPNQuery queryToCreateFrom) {
		// Query comment field starts here:
		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Query comment: "));
		JTextField queryComment;
		if (queryToCreateFrom == null) {
			queryComment = new JTextField("Query Comment/Name Here", 25);
		} else {
			queryComment = new JTextField(queryToCreateFrom.getName(), 25);
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

		if (queryToCreateFrom == null) {
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(3,
					0, Integer.MAX_VALUE, 1));
		} else {
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(
					queryToCreateFrom.getCapacity(), 0, Integer.MAX_VALUE, 1));
		}
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(50, 30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(50, 30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(50, 30));
		boundednessCheckPanel.add(numberOfExtraTokensInNet);

		// Boundedness button
		kbounded = new JButton("Check Boundedness");
		kbounded.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Verifier.analyseKBounded(tapnNetwork, getCapacity());
			}

		});
		boundednessCheckPanel.add(kbounded);

		kboundedOptimize = new JButton("Optimize Number of Tokens");
		kboundedOptimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Verifier.analyzeAndOptimizeKBound(tapnNetwork, getCapacity(),
						numberOfExtraTokensInNet);
			}
		});
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
		queryPanel
		.setBorder(BorderFactory
				.createTitledBorder("Query (click on the part of the query you want to change)"));

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

		// Set alignment to be centered for all paragraphs

		MutableAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
		StyleConstants.setFontSize(standard, 14);
		doc.setParagraphAttributes(0, 0, standard, true);

		queryField.setBackground(Color.white);
		queryField.setText(newProperty.toString());
		queryField.setEditable(false);

		// Put the text pane in a scroll pane.
		JScrollPane queryScrollPane = new JScrollPane(queryField);
		queryScrollPane
		.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension(750, 56);
		queryScrollPane.setPreferredSize(d);
		queryScrollPane.setMinimumSize(d);

		queryField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!queryField.isEditable())
					updateSelection();

			}
		});

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

		queryField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (!queryField.isEditable()) {
					if (e.getKeyChar() == KeyEvent.VK_DELETE
							|| e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
						deleteSelection();
					}
				} else {
					if (e.getKeyChar() == KeyEvent.VK_ENTER) {
						resetButton.doClick(); // we are in manual edit mode, so
						// the reset button is now the
						// Parse Query button
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

		queryPanel.add(queryScrollPane, gbc);
	}

	private void initQuantificationPanel(final TAPNQuery queryToCreateFrom) {
		// Quantification Panel
		quantificationPanel = new JPanel(new GridBagLayout());
		quantificationPanel.setBorder(BorderFactory
				.createTitledBorder("Quantification"));
		quantificationRadioButtonGroup = new ButtonGroup();

		existsDiamond = new JRadioButton(
		"(EF) There exists some reachable marking that satisifies:");
		existsBox = new JRadioButton(
		"(EG) There exists a trace on which every marking satisfies:");
		forAllDiamond = new JRadioButton(
		"(AF) On all traces there is eventually a marking that satisfies:");
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

		// bit of a hack, possible because quantifier node is always the first
		// node (we cant have nested quantifiers)
		if (queryToCreateFrom != null) {
			if (queryToCreateFrom.getProperty() instanceof TCTLEFNode) {
				existsDiamond.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLEGNode) {
				existsBox.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLAFNode) {
				forAllDiamond.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLAGNode) {
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
		queryPanel.add(quantificationPanel, gbc);

		// Add action listeners to the query options
		existsBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLEGNode property = new TCTLEGNode(getSpecificChildOfProperty(
						1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection
						.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),
						property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		});

		existsDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLEFNode property = new TCTLEFNode(getSpecificChildOfProperty(
						1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection
						.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),
						property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		});

		forAllBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAGNode property = new TCTLAGNode(getSpecificChildOfProperty(
						1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection
						.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),
						property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		});

		forAllDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAFNode property = new TCTLAFNode(getSpecificChildOfProperty(
						1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection
						.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),
						property);
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
		logicButtonPanel.add(conjunctionButton, gbc);

		gbc.gridy = 1;
		logicButtonPanel.add(disjunctionButton, gbc);

		gbc.gridy = 2;
		logicButtonPanel.add(negationButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(logicButtonPanel, gbc);

		// Add Action listener for logic buttons
		conjunctionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				TCTLAndListNode andListNode = null;
				if (currentSelection.getObject() instanceof TCTLAndListNode) {
					andListNode = new TCTLAndListNode(
							(TCTLAndListNode) currentSelection.getObject());
					andListNode.addConjunct(new TCTLStatePlaceHolder());
					UndoableEdit edit = new QueryConstructionEdit(
							currentSelection.getObject(), andListNode);
					newProperty = newProperty.replace(currentSelection
							.getObject(), andListNode);
					updateSelection(andListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLOrListNode) {
					andListNode = new TCTLAndListNode(
							((TCTLOrListNode) currentSelection.getObject())
							.getProperties());
					UndoableEdit edit = new QueryConstructionEdit(
							currentSelection.getObject(), andListNode);
					newProperty = newProperty.replace(currentSelection
							.getObject(), andListNode);
					updateSelection(andListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
					TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty) currentSelection
					.getObject();
					TCTLAbstractProperty parentNode = prop.getParent();

					if (parentNode instanceof TCTLAndListNode) {
						// current selection is child of an andList node => add
						// new placeholder conjunct to it
						andListNode = new TCTLAndListNode(
								(TCTLAndListNode) parentNode);
						andListNode.addConjunct(new TCTLStatePlaceHolder());
						UndoableEdit edit = new QueryConstructionEdit(
								parentNode, andListNode);
						newProperty = newProperty.replace(parentNode,
								andListNode);
						updateSelection(andListNode);
						undoSupport.postEdit(edit);
					} else {
						TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
						andListNode = new TCTLAndListNode(
								getStateProperty(currentSelection.getObject()),
								ph);
						UndoableEdit edit = new QueryConstructionEdit(
								currentSelection.getObject(), andListNode);
						newProperty = newProperty.replace(currentSelection
								.getObject(), andListNode);
						updateSelection(andListNode);
						undoSupport.postEdit(edit);
					}
				}
			}

		}

		);

		disjunctionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLOrListNode orListNode;
				if (currentSelection.getObject() instanceof TCTLOrListNode) {
					orListNode = new TCTLOrListNode(
							(TCTLOrListNode) currentSelection.getObject());
					orListNode.addDisjunct(new TCTLStatePlaceHolder());
					UndoableEdit edit = new QueryConstructionEdit(
							currentSelection.getObject(), orListNode);
					newProperty = newProperty.replace(currentSelection
							.getObject(), orListNode);
					updateSelection(orListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLAndListNode) {
					orListNode = new TCTLOrListNode(
							((TCTLAndListNode) currentSelection.getObject())
							.getProperties());
					UndoableEdit edit = new QueryConstructionEdit(
							currentSelection.getObject(), orListNode);
					newProperty = newProperty.replace(currentSelection
							.getObject(), orListNode);
					updateSelection(orListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
					TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty) currentSelection
					.getObject();
					TCTLAbstractProperty parentNode = prop.getParent();

					if (parentNode instanceof TCTLOrListNode) {
						// current selection is child of an orList node => add
						// new placeholder disjunct to it
						orListNode = new TCTLOrListNode(
								(TCTLOrListNode) parentNode);
						orListNode.addDisjunct(new TCTLStatePlaceHolder());
						UndoableEdit edit = new QueryConstructionEdit(
								parentNode, orListNode);
						newProperty = newProperty.replace(parentNode,
								orListNode);
						updateSelection(orListNode);
						undoSupport.postEdit(edit);
					} else {
						TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
						orListNode = new TCTLOrListNode(
								getStateProperty(currentSelection.getObject()),
								ph);
						UndoableEdit edit = new QueryConstructionEdit(
								currentSelection.getObject(), orListNode);
						newProperty = newProperty.replace(currentSelection
								.getObject(), orListNode);
						updateSelection(orListNode);
						undoSupport.postEdit(edit);
					}
				}
			}

		});

		negationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLNotNode property = new TCTLNotNode(
						getStateProperty(currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection
						.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),
						property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		});
	}

	private void initPredicationConstructionPanel() {
		// Predicate specification panel
		predicatePanel = new JPanel(new GridBagLayout());
		predicatePanel
		.setBorder(BorderFactory.createTitledBorder("Predicates"));

		placesBox = new JComboBox();
		Dimension d = new Dimension(150, 27);
		placesBox.setMaximumSize(d);

		templateBox = new JComboBox(new DefaultComboBoxModel(tapnNetwork
				.templates().toArray()));
		templateBox.addActionListener(new ActionListener() {
			private TimedArcPetriNet currentlySelected = null;

			public void actionPerformed(ActionEvent e) {
				TimedArcPetriNet tapn = (TimedArcPetriNet) templateBox
				.getSelectedItem();
				if (!tapn.equals(currentlySelected)) {
					Vector<String> placeNames = new Vector<String>();
					for (TimedPlace place : tapn.places()) {
						placeNames.add(place.name());
					}
					placesBox.setModel(new DefaultComboBoxModel(placeNames));

					currentlySelected = tapn;
					setEnablednessOfAddPredicateButton();
					if (userChangedAtomicPropSelection && placeNames.size() > 0)
						updateQueryOnAtomicPropositionChange();
				}
			}
		});
		Dimension dim = new Dimension(200, 27);
		templateBox.setMaximumSize(dim);
		templateBox.setMinimumSize(dim);
		templateBox.setPreferredSize(dim);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		predicatePanel.add(templateBox, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		predicatePanel.add(placesBox, gbc);

		String[] relationalSymbols = { "=", "<=", "<", ">=", ">" };
		relationalOperatorBox = new JComboBox(new DefaultComboBoxModel(
				relationalSymbols));

		gbc.gridx = 1;
		predicatePanel.add(relationalOperatorBox, gbc);

		int currentValue = 0;
		int min = 0;
		int step = 1;
		placeMarking = new JSpinner(new SpinnerNumberModel(currentValue, min,
				Integer.MAX_VALUE, step));
		placeMarking.setMaximumSize(new Dimension(50, 30));
		placeMarking.setMinimumSize(new Dimension(50, 30));
		placeMarking.setPreferredSize(new Dimension(50, 30));

		gbc.gridx = 2;
		predicatePanel.add(placeMarking, gbc);

		addPredicateButton = new JButton("Add Predicate to Query");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		predicatePanel.add(addPredicateButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(predicatePanel, gbc);

		// Action listeners for predicate panel
		addPredicateButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
						templateBox.getSelectedItem().toString(),
						(String) placesBox.getSelectedItem(),
						(String) relationalOperatorBox.getSelectedItem(),
						(Integer) placeMarking.getValue());
				UndoableEdit edit = new QueryConstructionEdit(currentSelection
						.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),
						property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
		}

		);

		placesBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (userChangedAtomicPropSelection) {
					updateQueryOnAtomicPropositionChange();
				}
			}
		});

		relationalOperatorBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (userChangedAtomicPropSelection) {
					updateQueryOnAtomicPropositionChange();
				}

			}
		});

		placeMarking.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (userChangedAtomicPropSelection) {
					updateQueryOnAtomicPropositionChange();
				}
			}
		});

		templateBox.setSelectedIndex(0); // Fills placesBox with correct places.
		// Must be called here to ensure
		// addPredicateButton is not null
	}

	private void initQueryEditingPanel() {
		// Editing buttons panel
		editingButtonPanel = new JPanel(new GridBagLayout());
		editingButtonPanel.setBorder(BorderFactory
				.createTitledBorder("Editing"));

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
		editingButtonPanel.add(undoButton, gbc);

		gbc.gridx = 1;
		editingButtonPanel.add(redoButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		editingButtonPanel.add(deleteButton, gbc);

		gbc.gridy = 2;
		editingButtonPanel.add(resetButton, gbc);

		gbc.gridy = 3;
		editingButtonPanel.add(editQueryButton, gbc);

		// Add action Listeners
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				deleteSelection();
			}
		});

		resetButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (queryField.isEditable()) { // in edit mode, this button is
					// now the parse query button.
					// User has potentially altered
					// the query, so try to parse it
					TAPAALQueryParser queryParser = new TAPAALQueryParser();
					TCTLAbstractProperty newQuery = null;

					try {
						newQuery = queryParser.parse(queryField.getText());
					} catch (Exception ex) {
						int choice = JOptionPane
						.showConfirmDialog(
								CreateGui.getApp(),
								"TAPAAL encountered an error trying to parse the specified query.\n\nWe recommend using the query construction buttons unless you are an experienced user.\n\n The specified query has not been saved. Do you want to edit it again?",
								"Error Parsing Query",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.ERROR_MESSAGE);
						if (choice == JOptionPane.NO_OPTION) {
							returnFromManualEdit(null);
						}
					}

					if (newQuery != null) // new query parsed successfully
					{
						// check correct place names are used in atomic
						// propositions
						ArrayList<String> places = new ArrayList<String>();
						for (int i = 0; i < datalayer.getPlaces().length; i++) {
							places.add(datalayer.getPlaces()[i].getName());
						}

						VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(
								places);

						VerifyPlaceNamesVisitor.Context c = nameChecker
						.VerifyPlaceNames(newQuery);

						if (!c.getResult()) {
							StringBuilder s = new StringBuilder();
							s
							.append("The following places was used in the query, but are not present in your model:\n\n");

							for (String placeName : c.getIncorrectPlaceNames()) {
								s.append(placeName);
								s.append("\n");
							}

							s
							.append("\nThe specified query has not been saved. Do you want to edit it again?");
							int choice = JOptionPane.showConfirmDialog(
									CreateGui.getApp(), s.toString(),
									"Error Parsing Query",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.ERROR_MESSAGE);
							if (choice == JOptionPane.NO_OPTION) {
								returnFromManualEdit(null);
							}
						} else {
							UndoableEdit edit = new QueryConstructionEdit(
									newProperty, newQuery);
							returnFromManualEdit(newQuery);
							undoSupport.postEdit(edit);
						}
					} else {
						returnFromManualEdit(null);
					}
				} else { // we are not in edit mode so the button should reset
					// the query

					TCTLPathPlaceHolder ph = new TCTLPathPlaceHolder();
					UndoableEdit edit = new QueryConstructionEdit(newProperty,
							ph);
					newProperty = ph;
					resetQuantifierSelectionButtons();
					updateSelection(newProperty);
					undoSupport.postEdit(edit);
				}
			}
		});

		undoButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				UndoableEdit edit = undoManager.GetNextEditToUndo();

				if (edit instanceof QueryConstructionEdit) {
					TCTLAbstractProperty original = ((QueryConstructionEdit) edit)
					.getOriginal();
					undoManager.undo();
					refreshUndoRedo();
					updateSelection(original);
				}

			}
		});

		redoButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				UndoableEdit edit = undoManager.GetNextEditToRedo();
				if (edit instanceof QueryConstructionEdit) {
					TCTLAbstractProperty replacement = ((QueryConstructionEdit) edit)
					.getReplacement();
					undoManager.redo();
					refreshUndoRedo();
					updateSelection(replacement);
				}
			}
		});

		editQueryButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (queryField.isEditable()) { // we are in edit mode so the
					// user pressed cancel
					returnFromManualEdit(null);
				} else { // user wants to edit query manually
					changeToEditMode();
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(editingButtonPanel, gbc);
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
		// verification option-radio buttons starts here:
		searchOptionsPanel = new JPanel(new GridBagLayout());

		searchOptionsPanel.setBorder(BorderFactory
				.createTitledBorder("Analysis Options"));
		searchRadioButtonGroup = new ButtonGroup();
		breadthFirstSearch = new JRadioButton("Breadth First Search");
		depthFirstSearch = new JRadioButton("Depth First Search");
		randomDepthFirstSearch = new JRadioButton("Random Depth First Search");
		closestToTargetFirstSearch = new JRadioButton(
		"Search by Closest To Target First");
		searchRadioButtonGroup.add(breadthFirstSearch);
		searchRadioButtonGroup.add(depthFirstSearch);
		searchRadioButtonGroup.add(randomDepthFirstSearch);
		searchRadioButtonGroup.add(closestToTargetFirstSearch);

		if (queryToCreateFrom == null) {
			breadthFirstSearch.setSelected(true);
		} else {
			if (queryToCreateFrom.getSearchOption() == SearchOption.BFS) {
				breadthFirstSearch.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.DFS) {
				depthFirstSearch.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.RDFS) {
				randomDepthFirstSearch.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.CLOSE_TO_TARGET_FIRST) {
				closestToTargetFirstSearch.setSelected(true);
			}
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		searchOptionsPanel.add(breadthFirstSearch, gridBagConstraints);
		gridBagConstraints.gridy = 1;
		searchOptionsPanel.add(depthFirstSearch, gridBagConstraints);
		gridBagConstraints.gridy = 2;
		searchOptionsPanel.add(randomDepthFirstSearch, gridBagConstraints);
		gridBagConstraints.gridy = 3;
		searchOptionsPanel.add(closestToTargetFirstSearch, gridBagConstraints);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		uppaalOptionsPanel.add(searchOptionsPanel, gridBagConstraints);

	}

	private void initTraceOptionsPanel(final TAPNQuery queryToCreateFrom) {
		traceOptionsPanel = new JPanel(new GridBagLayout());
		traceOptionsPanel.setBorder(BorderFactory
				.createTitledBorder("Trace Options"));
		traceRadioButtonGroup = new ButtonGroup();
		someTraceRadioButton = new JRadioButton(
		"Some encountered trace (only without symmetry reduction)");
		fastestTraceRadioButton = new JRadioButton(
		"Fastest trace (only without symmetry reduction)");
		noTraceRadioButton = new JRadioButton("No trace");
		traceRadioButtonGroup.add(someTraceRadioButton);
		traceRadioButtonGroup.add(fastestTraceRadioButton);
		traceRadioButtonGroup.add(noTraceRadioButton);

		if (queryToCreateFrom == null) {
			someTraceRadioButton.setEnabled(false);
			fastestTraceRadioButton.setEnabled(false);
			noTraceRadioButton.setSelected(true);
		} else {
			if (queryToCreateFrom.getTraceOption() == TraceOption.SOME) {
				someTraceRadioButton.setSelected(true);
			} else if (queryToCreateFrom.getTraceOption() == TraceOption.FASTEST) {
				fastestTraceRadioButton.setSelected(true);
			} else if (queryToCreateFrom.getTraceOption() == TraceOption.NONE) {
				noTraceRadioButton.setSelected(true);
			}
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		traceOptionsPanel.add(someTraceRadioButton, gridBagConstraints);
		gridBagConstraints.gridy = 1;
		traceOptionsPanel.add(fastestTraceRadioButton, gridBagConstraints);
		gridBagConstraints.gridy = 2;
		traceOptionsPanel.add(noTraceRadioButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		uppaalOptionsPanel.add(traceOptionsPanel, gridBagConstraints);

	}

	private void initReductionOptionsPanel(final TAPNQuery queryToCreateFrom) {

		// ReductionOptions starts here:
		reductionOptionsPanel = new JPanel(new FlowLayout());
		reductionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Reduction Options"));
		String[] reductionOptions = { name_verifyTAPN, name_NAIVE, name_ADVNOSYM, name_BROADCAST, name_BROADCASTDEG2 };
		reductionOption = new JComboBox(reductionOptions);
		reductionOption.setSelectedIndex(4);
		
		reductionOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox)e.getSource();
				String selectedItem = (String)source.getSelectedItem();
				if(selectedItem != null) {
					setEnabledOptionsAccordingToCurrentReduction();					
				}
			}
		});

		reductionOptionsPanel.add(new JLabel("  Choose reduction method:"));
		reductionOptionsPanel.add(reductionOption);

		symmetryReduction = new JCheckBox("Use Symmetry Reduction");
		symmetryReduction.setSelected(true);
		symmetryReduction.addItemListener(new ItemListener() {

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

		// Update the selected reduction
		if (queryToCreateFrom != null) {
			String reduction = "";
			boolean symmetry = false;

			if (queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST) {
				reduction = name_BROADCAST;
				symmetry = false;
				// enableTraceOptions();
			} else if (queryToCreateFrom.getReductionOption() == ReductionOption.BROADCASTSYMMETRY) {
				reduction = name_BROADCAST;
				symmetry = true;
				// disableTraceOptions();
			} else if (queryToCreateFrom.getReductionOption() == ReductionOption.DEGREE2BROADCAST) {
				reduction = name_BROADCASTDEG2;
				symmetry = false;
				// disableTraceOptions();
			} else if (queryToCreateFrom.getReductionOption() == ReductionOption.DEGREE2BROADCASTSYMMETRY) {
				reduction = name_BROADCASTDEG2;
				symmetry = true;
				// disableTraceOptions();
			} else if (getQuantificationSelection().equals("E<>")
					|| getQuantificationSelection().equals("A[]")) {
				if (queryToCreateFrom.getReductionOption() == ReductionOption.STANDARD) {
					reduction = name_NAIVE;
					symmetry = false;
					// enableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.STANDARDSYMMETRY) {
					reduction = name_NAIVE;
					symmetry = true;
					// disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY) {
					reduction = name_ADVNOSYM;
					symmetry = true;
					// disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD) {
					reduction = name_ADVNOSYM;
					symmetry = false;
					// enableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.VerifyTAPN) {
					reduction = name_verifyTAPN;
					symmetry = true;
				}
			} else {
				if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARDSYMMETRY) {
					reduction = name_ADVNOSYM;
					symmetry = true;
					// disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD) {
					reduction = name_ADVNOSYM;
					symmetry = false;
					// enableTraceOptions();
				}
			}

			reductionOption.setSelectedItem(reduction);
			symmetryReduction.setSelected(symmetry);
		}

	}

	protected void setEnabledOptionsAccordingToCurrentReduction() {
		refreshSymmetryReduction();
		refreshTraceOptions();
		refreshSearchOptions();
	}

	private void refreshSymmetryReduction() {
		if(((String)reductionOption.getSelectedItem()).equals(name_verifyTAPN)) {
			symmetryReduction.setSelected(true);
			symmetryReduction.setEnabled(false);
		}
		else{
			symmetryReduction.setSelected(symmetryReduction.isSelected());
			symmetryReduction.setEnabled(true);
		}
	}

	private void initButtonPanel(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		buttonPanel = new JPanel(new FlowLayout());
		if (option == QueryDialogueOption.Save) {
			saveButton = new JButton("Save");
			saveAndVerifyButton = new JButton("Save and Verify");
			cancelButton = new JButton("Cancel");
			saveUppaalXMLButton = new JButton("Save UPPAAL XML");

			saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					// TODO make save
					// save();
					querySaved = true;
					exit();
				}
			});
			saveAndVerifyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					querySaved = true;
					exit();
					TAPNQuery query = getQuery();
					
					if(query.getReductionOption() == ReductionOption.VerifyTAPN)
						Verifier.runVerifyTAPNVerification(tapnNetwork, query);
					else
						Verifier.runUppaalVerification(tapnNetwork, query);
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {

					exit();
				}
			});

			saveUppaalXMLButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					querySaved = true;

					String xmlFile = null, queryFile = null;
					try {
						FileBrowser browser = new FileBrowser("Uppaal XML",
								"xml", xmlFile);
						xmlFile = browser.saveFile();
						if (xmlFile != null) {
							String[] a = xmlFile.split(".xml");
							queryFile = a[0] + ".q";
						}

					} catch (Exception ex) {
						JOptionPane
						.showMessageDialog(CreateGui.getApp(),
								"There were errors performing the requested action:\n"
								+ e, "Error",
								JOptionPane.ERROR_MESSAGE);
					}

					if (xmlFile != null && queryFile != null) {
						UppaalExporter exporter = new UppaalExporter();
						TAPNComposer composer = new TAPNComposer();
						Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(QueryDialog.this.tapnNetwork);

						// TODO: Get rid of this step by changing the underlying translations
						// etc.
						NewModelToOldModelTransformer transformer = new NewModelToOldModelTransformer();
						dk.aau.cs.petrinet.TimedArcPetriNet tapn = transformer.transformModel(transformedModel.value1());
						
						TAPNQuery tapnQuery = getQuery();
						dk.aau.cs.petrinet.TAPNQuery clonedQuery = new dk.aau.cs.petrinet.TAPNQuery(tapnQuery.getProperty().copy(), tapnQuery.getCapacity() + tapnNetwork.marking().size());
						
						RenameAllPlacesVisitor visitor = new RenameAllPlacesVisitor(transformedModel.value2());
						clonedQuery.getProperty().accept(visitor, null);
						
						exporter.export(transformedModel.value1(), tapn, clonedQuery, tapnQuery.getReductionOption(), new File(xmlFile), new File(queryFile));
					} else {
						JOptionPane.showMessageDialog(CreateGui.getApp(),
						"No Uppaal XML file saved.");
					}
				}
			});
		} else if (option == QueryDialogueOption.Export) {
			saveButton = new JButton("export");
			cancelButton = new JButton("Cancel");

			saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					querySaved = true;
					exit();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {

					exit();
				}
			});
		}
		if (option == QueryDialogueOption.Save) {
			buttonPanel.add(cancelButton);

			buttonPanel.add(saveButton);

			buttonPanel.add(saveAndVerifyButton);

			buttonPanel.add(saveUppaalXMLButton);
		} else {
			buttonPanel.add(cancelButton);

			buttonPanel.add(saveButton);

			// buttonPanel.add(saveUppaalXMLButton);
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		add(buttonPanel, gridBagConstraints);

	}

	// /////////////////////////////////////////////////////////////////////
	// Undo support stuff
	// /////////////////////////////////////////////////////////////////////
	private void refreshUndoRedo() {
		undoButton.setEnabled(undoManager.canUndo());
		redoButton.setEnabled(undoManager.canRedo());
	}

	private class UndoAdapter implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent arg0) {
			UndoableEdit edit = arg0.getEdit();
			undoManager.addEdit(edit);
			refreshUndoRedo();
		}
	}

	private class QueryConstructionUndoManager extends UndoManager {
		private static final long serialVersionUID = 1L;

		public UndoableEdit GetNextEditToUndo() {
			return editToBeUndone();
		}

		public UndoableEdit GetNextEditToRedo() {
			return editToBeRedone();
		}
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

		public QueryConstructionEdit(TCTLAbstractProperty original,
				TCTLAbstractProperty replacement) {
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
		public boolean canUndo() {
			return true;
		}

		@Override
		public boolean canRedo() {
			return true;
		}

	}

}
