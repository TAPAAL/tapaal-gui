package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
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
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.UpwardsClosedVisitor;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.UPPAAL.UppaalExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNExporter;

public class QueryDialog extends JPanel {

	private static final String NO_UPPAAL_XML_FILE_SAVED = "No Uppaal XML file saved.";
	private static final String NO_VERIFYTAPN_XML_FILE_SAVED = "No verifytapn XML file saved.";
	private static final String UNSUPPORTED_MODEL_TEXT = "The model is not supported chosen reduction";
	private static final String UNSUPPPORTED_QUERY_TEXT = "The chosen query property is not supported by the chosen reduction";
	private static final String EXPORT_UPPAAL_BTN_TEXT = "Export UPPAAL XML";
	private static final String EXPORT_VERIFYTAPN_BTN_TEXT = "Export verifytapn XML";
	
	private static final String UPPAAL_SOME_TRACE_STRING = "Some encountered trace (only without symmetry reduction)";
	private static final String VERIFYTAPN_SOME_TRACE_STRING = "Some encountered trace";
	private static final String SHARED = "Shared";

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
	private JButton truePredicateButton;
	private JButton falsePredicateButton;

	// Uppaal options panel (search + trace options)
	// search options panel
	private JPanel searchOptionsPanel;
	private JPanel uppaalOptionsPanel;
	private ButtonGroup searchRadioButtonGroup;
	private JRadioButton breadthFirstSearch;
	private JRadioButton depthFirstSearch;
	private JRadioButton randomSearch;
	private JRadioButton heuristicSearch;

	// Trace options panel
	private JPanel traceOptionsPanel;

	private ButtonGroup traceRadioButtonGroup;
	private JRadioButton noTraceRadioButton;
	private JRadioButton someTraceRadioButton;

	// Reduction options panel
	private JPanel reductionOptionsPanel;
	private JComboBox reductionOption;
	private JCheckBox symmetryReduction;
	private JCheckBox discreteInclusion;
	private JButton selectInclusionPlacesButton;
	
	// Buttons in the bottom of the dialogue
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton saveButton;
	private JButton saveAndVerifyButton;
	private JButton saveUppaalXMLButton;

	// Private Members
	private StringPosition currentSelection = null;
	
	private final TimedArcPetriNetNetwork tapnNetwork;
	private QueryConstructionUndoManager undoManager;
	private UndoableEditSupport undoSupport;
	private boolean isNetDegree2;
	private InclusionPlaces inclusionPlaces;

	private String name_verifyTAPN = "TAPAAL Engine (verifytapn)";
	private String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private String name_STANDARD = "UPPAAL: Standard Reduction";
	private String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private boolean userChangedAtomicPropSelection = true;

	private TCTLAbstractProperty newProperty;
	private JTextField queryName;
	
	public QueryDialog(EscapableDialog me, QueryDialogueOption option,
			TAPNQuery queryToCreateFrom, TimedArcPetriNetNetwork tapnNetwork) {
		this.tapnNetwork = tapnNetwork;
		this.inclusionPlaces = queryToCreateFrom == null ? new InclusionPlaces() : queryToCreateFrom.inclusionPlaces();
		this.newProperty = queryToCreateFrom == null ? new TCTLPathPlaceHolder() : queryToCreateFrom.getProperty();
		rootPane = me.getRootPane();
		isNetDegree2 = checkForDegree2();
		
		setLayout(new GridBagLayout());

		init(option, queryToCreateFrom);
	}

	private boolean checkForDegree2() {
		if(tapnNetwork.hasInhibitorArcs())
			return false;
		
		TAPNComposer composer = new TAPNComposer();
		Tuple<TimedArcPetriNet,NameMapping> composedModel = composer.transformModel(tapnNetwork);
		
		for(TimedTransition t : composedModel.value1().transitions()) {
			if(t.presetSize() > 2 || t.postsetSize() > 2)
				return false;
		}
		
		return true;
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

		TAPNQuery.TraceOption traceOption = getTraceOption();

		TAPNQuery.SearchOption searchOption = getSearchOption();

		ReductionOption reductionOptionToSet = getReductionOption();
		boolean symmetry = getSymmetry();
		
		TAPNQuery query = new TAPNQuery(name, capacity, newProperty.copy(), traceOption, searchOption, reductionOptionToSet, symmetry,/* hashTableSizeToSet */ null, /* extrapolationOptionToSet */null, inclusionPlaces);
		if(reductionOptionToSet.equals(ReductionOption.VerifyTAPN)){
			query.setDiscreteInclusion(discreteInclusion.isSelected());
		}
		return query;
	}

	private boolean getSymmetry() {
		return symmetryReduction.isSelected();
	}

	private int getCapacity() {
		return (Integer) ((JSpinner) boundednessCheckPanel.getComponent(1)).getValue();
	}

	private String getQueryComment() {
		return ((JTextField) namePanel.getComponent(1)).getText();
	}

	private TraceOption getTraceOption() {
		if(someTraceRadioButton.isSelected())
			return TraceOption.SOME;
		else
			return TraceOption.NONE;
	}

	private SearchOption getSearchOption() {
		if(depthFirstSearch.isSelected())
			return SearchOption.DFS;
		else if(randomSearch.isSelected())
			return SearchOption.RANDOM;
		else if(heuristicSearch.isSelected())
			return SearchOption.HEURISTIC;
		else
			return SearchOption.BFS;
	}
	
	private ReductionOption getReductionOption() {
		String reductionOptionString = (String)reductionOption.getSelectedItem();
		
		if (reductionOptionString.equals(name_STANDARD))
			return ReductionOption.STANDARD;
		else if (reductionOptionString.equals(name_OPTIMIZEDSTANDARD))
			return ReductionOption.OPTIMIZEDSTANDARD;
		else if (reductionOptionString.equals(name_BROADCASTDEG2))
			return ReductionOption.DEGREE2BROADCAST;
		else if (reductionOptionString.equals(name_verifyTAPN))
			return ReductionOption.VerifyTAPN;
		else
			return ReductionOption.BROADCAST;
	}
	
	private String getReductionOptionAsString() {
		return (String)reductionOption.getSelectedItem();
	}

	private void refreshTraceOptions() {
		TraceOption traceOption = getTraceOption();
		if(((String)reductionOption.getSelectedItem()).equals(name_verifyTAPN)) {
			someTraceRadioButton.setText(VERIFYTAPN_SOME_TRACE_STRING);
			someTraceRadioButton.setEnabled(true);
			someTraceRadioButton.setSelected(someTraceRadioButton.isSelected());
			noTraceRadioButton.setSelected(noTraceRadioButton.isSelected());
		}
		else if (symmetryReduction.isSelected()) {
			someTraceRadioButton.setText(UPPAAL_SOME_TRACE_STRING);
			someTraceRadioButton.setEnabled(false);
			noTraceRadioButton.setSelected(true);
		} else {
			someTraceRadioButton.setText(UPPAAL_SOME_TRACE_STRING);
			someTraceRadioButton.setEnabled(true);
		}
		
		if(traceOption == TraceOption.SOME && someTraceRadioButton.isEnabled())
			someTraceRadioButton.setSelected(true);
		else
			noTraceRadioButton.setSelected(true);
	}

	private void refreshSearchOptions() {
//		SearchOption searchOption = getSearchOption();
//		
//		if(((String)reductionOption.getSelectedItem()).equals(name_verifyTAPN))
//		{
//			depthFirstSearch.setEnabled(true);
//			breadthFirstSearch.setEnabled(true);
//			breadthFirstSearch.setSelected(true);
//			randomSearch.setEnabled(false);
//		}
//		else {
//			depthFirstSearch.setEnabled(true);
//			breadthFirstSearch.setEnabled(true);
//			randomSearch.setEnabled(true);
//		}
//		
//		
//		if(searchOption == SearchOption.DFS && depthFirstSearch.isEnabled())
//			depthFirstSearch.setSelected(true);
//		else if(searchOption == SearchOption.RDFS && randomSearch.isEnabled())
//			randomSearch.setSelected(true);
//		else
//			breadthFirstSearch.setSelected(true);
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

	public static TAPNQuery showQueryDialogue(QueryDialogueOption option, TAPNQuery queryToRepresent, TimedArcPetriNetNetwork tapnNetwork) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(),	"Edit Query", true);
		
		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add query editor
		QueryDialog queryDialogue = new QueryDialog(guiDialog, option, queryToRepresent, tapnNetwork);
		contentPane.add(queryDialogue);

		guiDialog.setResizable(false);

		guiDialog.setMinimumSize(new Dimension(885,585));
		
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
	// representation used for updating when selecting with the mouse.
	private void updateSelection() {
		int index = queryField.getCaretPosition();
		StringPosition position = newProperty.objectAt(index);
		if (position == null)
			return;
		queryField.select(position.getStart(), position.getEnd());
		currentSelection = position;
		if(currentSelection != null) {
			setEnabledOptionsAccordingToCurrentReduction();
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
		if(currentSelection != null) {		
			setEnabledOptionsAccordingToCurrentReduction();
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
			if(node.getTemplate().equals(""))
				templateBox.setSelectedItem(SHARED);
			else
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
				queryChanged();
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

	private void setEnabledReductionOptions() {
		String reductionOptionString = getReductionOptionAsString();
		
		String[] options;
		if (getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")) {
			if(isNetDegree2)
				options = new String[]{ name_BROADCAST, name_BROADCASTDEG2, name_OPTIMIZEDSTANDARD };
			else
				options = new String[]{ name_BROADCAST, name_BROADCASTDEG2 };
		} else if(tapnNetwork.hasInhibitorArcs()) {
			options = new String[]{ name_verifyTAPN, name_BROADCAST, name_BROADCASTDEG2 };
		} else {
			options = new String[] { name_verifyTAPN, name_OPTIMIZEDSTANDARD, name_STANDARD, name_BROADCAST, name_BROADCASTDEG2};
		}
		
		reductionOption.removeAllItems();
		boolean selectedOptionStillAvailable = false;	
		boolean symmetry = symmetryReduction == null ? false : symmetryReduction.isSelected();
		for (String s : options) {
			reductionOption.addItem(s);
			if (s.equals(reductionOptionString)) {
				selectedOptionStillAvailable = true;
			}
		}

		if (selectedOptionStillAvailable) {
			reductionOption.setSelectedItem(reductionOptionString);
			symmetryReduction.setSelected(symmetry);
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
		truePredicateButton.setEnabled(false);
		falsePredicateButton.setEnabled(false);
	}

	private void enableOnlyPathButtons() {
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
		truePredicateButton.setEnabled(false);
		falsePredicateButton.setEnabled(false);
	}

	private void enableOnlyStateButtons() {
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
		truePredicateButton.setEnabled(true);
		falsePredicateButton.setEnabled(true);
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
		
		setEnabledReductionOptions();
	}

	private void changeToEditMode() {
		setQueryFieldEditable(true);
		resetButton.setText("Parse query");
		editQueryButton.setText("Cancel");
		clearSelection();
		disableAllQueryButtons();
		disableEditingButtons();
		setSaveButtonsEnabled();
	}

	private void updateQueryOnAtomicPropositionChange() {
		if (currentSelection != null && currentSelection.getObject() instanceof TCTLAtomicPropositionNode) {
			Object item = templateBox.getSelectedItem();
			String template = item.equals(SHARED) ? "" : item.toString();
			TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
					template,
					(String) placesBox.getSelectedItem(),
					(String) relationalOperatorBox.getSelectedItem(),
					(Integer) placeMarking.getValue());
			if (!property.equals(currentSelection.getObject())) {
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),	property);
				updateSelection(property);
				undoSupport.postEdit(edit);
			}
			queryChanged();
		}
	}

	// /////////////////////////////////////////////////////////////////////
	// Initialization of the dialogue
	// /////////////////////////////////////////////////////////////////////

	private void init(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		//setPreferredSize(new Dimension(942, 517));
		
		initQueryNamePanel();
		initBoundednessCheckPanel();
		initQueryPanel();
		initUppaalOptionsPanel();
		initReductionOptionsPanel();
		initButtonPanel(option);
		
		if(queryToCreateFrom != null)
			setupFromQuery(queryToCreateFrom);

		rootPane.setDefaultButton(saveButton);
		disableAllQueryButtons();
		setSaveButtonsEnabled();

		// initilize the undo.redo system
		undoManager = new QueryConstructionUndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoAdapter());
		refreshUndoRedo();

		setEnabledOptionsAccordingToCurrentReduction();
	}

	private void setupFromQuery(TAPNQuery queryToCreateFrom) {
		queryName.setText(queryToCreateFrom.getName());
		numberOfExtraTokensInNet.setValue(queryToCreateFrom.getCapacity());
		
		setupQuantificationFromQuery(queryToCreateFrom);
		setupSearchOptionsFromQuery(queryToCreateFrom);		
		setupReductionOptionsFromQuery(queryToCreateFrom);
		setEnabledReductionOptions(); // fix for if an query with an invalid reduction option had been saved due to a bug.
		setupTraceOptionsFromQuery(queryToCreateFrom);
	}

	private void setupReductionOptionsFromQuery(TAPNQuery queryToCreateFrom) {
		String reduction = "";
		boolean symmetry = queryToCreateFrom.useSymmetry();

		if (queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST) {
			reduction = name_BROADCAST;
		} else if (queryToCreateFrom.getReductionOption() == ReductionOption.DEGREE2BROADCAST) {
			reduction = name_BROADCASTDEG2;
		} else if (getQuantificationSelection().equals("E<>") || getQuantificationSelection().equals("A[]")) {
			if (queryToCreateFrom.getReductionOption() == ReductionOption.STANDARD) {
				reduction = name_STANDARD;
			} else if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD) {
				reduction = name_OPTIMIZEDSTANDARD;
			} else if (queryToCreateFrom.getReductionOption() == ReductionOption.VerifyTAPN) {
				reduction = name_verifyTAPN;
			}
		} else {
			if (queryToCreateFrom.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD) {
				reduction = name_OPTIMIZEDSTANDARD;
			}
		}
		reductionOption.setSelectedItem(reduction);
		symmetryReduction.setSelected(symmetry);
		discreteInclusion.setSelected(queryToCreateFrom.discreteInclusion());
		if(queryToCreateFrom.discreteInclusion()) selectInclusionPlacesButton.setEnabled(true);
	}

	private void setupTraceOptionsFromQuery(TAPNQuery queryToCreateFrom) {
		if (queryToCreateFrom.getTraceOption() == TraceOption.SOME) {
			someTraceRadioButton.setSelected(true);
		} else if (queryToCreateFrom.getTraceOption() == TraceOption.NONE) {
			noTraceRadioButton.setSelected(true);
		}
	}

	private void setupSearchOptionsFromQuery(TAPNQuery queryToCreateFrom) {
		if (queryToCreateFrom.getSearchOption() == SearchOption.BFS) {
			breadthFirstSearch.setSelected(true);
		} else if (queryToCreateFrom.getSearchOption() == SearchOption.DFS) {
			depthFirstSearch.setSelected(true);
		} else if (queryToCreateFrom.getSearchOption() == SearchOption.RANDOM) {
			randomSearch.setSelected(true);
		} else if (queryToCreateFrom.getSearchOption() == SearchOption.HEURISTIC){
			heuristicSearch.setSelected(true);
		}
	}

	private void setupQuantificationFromQuery(TAPNQuery queryToCreateFrom) {
		// bit of a hack, possible because quantifier node is always the first
		// node (we cant have nested quantifiers)	
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

	private void initQueryNamePanel() {
		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Query name: "));
		
		queryName = new JTextField("Query Comment/Name Here", 25);

		namePanel.add(queryName);

		queryName.getDocument().addDocumentListener(new DocumentListener() {

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
		gridBagConstraints.insets = new Insets(0,10,0,10);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(namePanel, gridBagConstraints);
	}

	private void initBoundednessCheckPanel() {

		// Number of extra tokens field
		boundednessCheckPanel = new JPanel();
		boundednessCheckPanel.setLayout(new BoxLayout(boundednessCheckPanel, BoxLayout.X_AXIS));
		boundednessCheckPanel.add(new JLabel("Extra number of tokens: "));

		numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(3, 0, Integer.MAX_VALUE, 1));	
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(65, 30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(65, 30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(65, 30));
		boundednessCheckPanel.add(numberOfExtraTokensInNet);

		// Boundedness button
		kbounded = new JButton("Check Boundedness");
		kbounded.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Verifier.analyzeKBound(tapnNetwork, getCapacity(), numberOfExtraTokensInNet);
			}

		});
		boundednessCheckPanel.add(kbounded);
		boundednessCheckPanel.add(Box.createHorizontalStrut(350));
		
		JButton infoButton = new JButton("Help on the query options");	
		infoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(QueryDialog.this, getMessageComponent(), "Help", JOptionPane.INFORMATION_MESSAGE);
			}
			
			private Object getMessageComponent(){
				JTextPane pane = new JTextPane();
				pane.setContentType("text/html");
				pane.setText(getHelpMessage());
				pane.setEditable(false);
				pane.setCaretPosition(0);
				for(MouseListener listener : pane.getMouseListeners()){
					pane.removeMouseListener(listener);
				}
				Dimension dim = new Dimension(500,400);
				pane.setPreferredSize(dim);  
				pane.setMargin(new Insets(5,5,5,5));  
				JScrollPane scrollPane = new JScrollPane(pane);  
				scrollPane.setPreferredSize(dim);  
				return scrollPane;  
			}
			
			private String getHelpMessage(){ 
				// There is automatic word wrapping in the control that displays the text, so you don't need line breaks in paragraphs.
				StringBuffer buffer = new StringBuffer();
				buffer.append("<html>");
				buffer.append("<b>Boundedness</b><br/>");
				buffer.append("The query dialog allows you to specify the extra number of tokens that TAPAAL is allowed to use during the verification. ");
				buffer.append("Because TAPAAL models can produce additional tokens by firing transitions (e.g. a transition that has a single input place ");
				buffer.append("and two output places) you may need to use additional tokens compared to those that are already in the net. By ");
				buffer.append("specifying an extra number of tokens you can ask TAPAAL to check if your net is bounded for this number of extra tokens (i.e. ");
				buffer.append("whether there is no reachable marking in the net that would exceed the predefined number of tokens. ");
				buffer.append("<br/><br/>");
				buffer.append("<b>Search Strategies</b><br/>");
				buffer.append("A search strategy determines how the chosen verification method performs the search. The possible search strategies are: ");
				buffer.append("<ul>");
				buffer.append("<li>Heuristic Search<br/> If discrete inclusion optimization is not enabled, this strategy performs a breadth first search. ");
				buffer.append("If discrete inclusion is enabled, the search attempts to maximize the number of tokens in places where the engine checks for discrete inclusion.</li>");
				buffer.append("<li>Breadth First Search<br/>Explores markings in a breadth first manner.</li>");
				buffer.append("<li>Depth First Search<br/>Explores markings in a depth first manner.</li>");
				buffer.append("<li>Random Search<br/>Performs a random exploration of the state space.</li>");
				buffer.append("</ul>");
				buffer.append("<br/>");
				buffer.append("<b>Verification Methods</b><br/>");
				buffer.append("TAPAAL supports verification via its own included engine verifytapn or via a translation to networks of timed automata and then using the tool UPPAAL (requires a separate installation).");
				buffer.append("The TAPAAL engine supports also the discrete inclusion optimization that works for EF queries where the propositions state only ");
				buffer.append("lower bounds on the number of tokens and for AG queries with only the upper bounds constraints. On some models this technique gives a considerable speedup. ");
				buffer.append("The user selected set of places that are considered for the discrete inclusion can further finetune the performance of the engine. Try to include places where you expect to see many tokens during the execution. ");
				buffer.append("The different UPPAAL verification methods perform different reductions to networks of timed automata. The broadcast reductions supports ");
				buffer.append("all query types, while standard and optimized standard support only EF and AG queries but can be often faster.");
				buffer.append("<br/>");				
				buffer.append("</html>");
				return buffer.toString();
			}
		});
		boundednessCheckPanel.add(infoButton);


		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(0,10,0,10);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(boundednessCheckPanel, gridBagConstraints);
	}

	private void initQueryPanel() {
		queryPanel = new JPanel(new GridBagLayout());
		queryPanel.setBorder(BorderFactory.createTitledBorder("Query (click on the part of the query you want to change)"));

		initQueryField();
		initQuantificationPanel();
		initLogicPanel();
		initPredicationConstructionPanel();
		initQueryEditingPanel();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(5,10,5,10);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		add(queryPanel, gbc);

	}

	private void initQueryField() {
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
		queryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension(750, 80);
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

		queryPanel.add(queryScrollPane, gbc);
	}

	private void initQuantificationPanel() {
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
				TCTLEGNode property = new TCTLEGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),	property);
				updateSelection(property);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		});

		existsDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLEFNode property = new TCTLEFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),	property);
				updateSelection(property);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		});

		forAllBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAGNode property = new TCTLAGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),	property);
				updateSelection(property);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		});

		forAllDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAFNode property = new TCTLAFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(),	property);
				updateSelection(property);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		});
	}

	private void initLogicPanel() {
		logicButtonPanel = new JPanel(new GridBagLayout());
		logicButtonPanel.setBorder(BorderFactory.createTitledBorder("Logic"));

		logicButtonGroup = new ButtonGroup();
		conjunctionButton = new JButton("and");
		disjunctionButton = new JButton("or");
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
					andListNode = new TCTLAndListNode((TCTLAndListNode) currentSelection.getObject());
					andListNode.addConjunct(new TCTLStatePlaceHolder());
					UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), andListNode);
					newProperty = newProperty.replace(currentSelection.getObject(), andListNode);
					updateSelection(andListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLOrListNode) {
					andListNode = new TCTLAndListNode(((TCTLOrListNode) currentSelection.getObject()).getProperties());
					UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), andListNode);
					newProperty = newProperty.replace(currentSelection.getObject(), andListNode);
					updateSelection(andListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
					TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty) currentSelection
					.getObject();
					TCTLAbstractProperty parentNode = prop.getParent();

					if (parentNode instanceof TCTLAndListNode) {
						// current selection is child of an andList node => add
						// new placeholder conjunct to it
						andListNode = new TCTLAndListNode((TCTLAndListNode) parentNode);
						andListNode.addConjunct(new TCTLStatePlaceHolder());
						UndoableEdit edit = new QueryConstructionEdit(parentNode, andListNode);
						newProperty = newProperty.replace(parentNode, andListNode);
						updateSelection(andListNode);
						undoSupport.postEdit(edit);
					} else {
						TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
						andListNode = new TCTLAndListNode(getStateProperty(currentSelection.getObject()),	ph);
						UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), andListNode);
						newProperty = newProperty.replace(currentSelection.getObject(), andListNode);
						updateSelection(andListNode);
						undoSupport.postEdit(edit);
					}
				}
				queryChanged();
			}

		}

		);

		disjunctionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLOrListNode orListNode;
				if (currentSelection.getObject() instanceof TCTLOrListNode) {
					orListNode = new TCTLOrListNode((TCTLOrListNode) currentSelection.getObject());
					orListNode.addDisjunct(new TCTLStatePlaceHolder());
					UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), orListNode);
					newProperty = newProperty.replace(currentSelection.getObject(), orListNode);
					updateSelection(orListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLAndListNode) {
					orListNode = new TCTLOrListNode(((TCTLAndListNode) currentSelection.getObject()).getProperties());
					UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), orListNode);
					newProperty = newProperty.replace(currentSelection.getObject(), orListNode);
					updateSelection(orListNode);
					undoSupport.postEdit(edit);
				} else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
					TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty) currentSelection.getObject();
					TCTLAbstractProperty parentNode = prop.getParent();

					if (parentNode instanceof TCTLOrListNode) {
						// current selection is child of an orList node => add
						// new placeholder disjunct to it
						orListNode = new TCTLOrListNode((TCTLOrListNode) parentNode);
						orListNode.addDisjunct(new TCTLStatePlaceHolder());
						UndoableEdit edit = new QueryConstructionEdit(parentNode, orListNode);
						newProperty = newProperty.replace(parentNode, orListNode);
						updateSelection(orListNode);
						undoSupport.postEdit(edit);
					} else {
						TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
						orListNode = new TCTLOrListNode(getStateProperty(currentSelection.getObject()),	ph);
						UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), orListNode);
						newProperty = newProperty.replace(currentSelection.getObject(), orListNode);
						updateSelection(orListNode);
						undoSupport.postEdit(edit);
					}
				}
				queryChanged();
			}

		});

		negationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLNotNode property = new TCTLNotNode(getStateProperty(currentSelection.getObject()));
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		});
	}

	private void initPredicationConstructionPanel() {
		predicatePanel = new JPanel(new GridBagLayout());
		predicatePanel.setBorder(BorderFactory.createTitledBorder("Predicates"));

		placesBox = new JComboBox();
		Dimension d = new Dimension(150, 27);
		placesBox.setMaximumSize(d);

		Vector<Object> items = new Vector<Object>(tapnNetwork.activeTemplates().size()+1);
		items.addAll(tapnNetwork.activeTemplates());
		if(tapnNetwork.numberOfSharedPlaces() > 0) items.add(SHARED);

		templateBox = new JComboBox(new DefaultComboBoxModel(items));
		templateBox.addActionListener(new ActionListener() {
			private Object currentlySelected = null;

			public void actionPerformed(ActionEvent e) {
				if(!templateBox.getSelectedItem().equals(SHARED)){
					TimedArcPetriNet tapn = (TimedArcPetriNet) templateBox.getSelectedItem();
					if (!tapn.equals(currentlySelected)) {
						Vector<String> placeNames = new Vector<String>();
						for (TimedPlace place : tapn.places()) {
							if(!place.isShared()){
								placeNames.add(place.name());
							}
						}
						
						Collections.sort(placeNames, new Comparator<String>() {
							public int compare(String o1, String o2) {
								return o1.compareToIgnoreCase(o2);
							}
						});
						placesBox.setModel(new DefaultComboBoxModel(placeNames));

						currentlySelected = tapn;
						setEnablednessOfAddPredicateButton();
						if (userChangedAtomicPropSelection && placeNames.size() > 0)
							updateQueryOnAtomicPropositionChange();
					}
				}else{
					Vector<String> placeNames = new Vector<String>();
					for (SharedPlace place : tapnNetwork.sharedPlaces()) {
							placeNames.add(place.name());
					}
					Collections.sort(placeNames, new Comparator<String>() {
						public int compare(String o1, String o2) {
							return o1.compareToIgnoreCase(o2);
						}
					});
					placesBox.setModel(new DefaultComboBoxModel(placeNames));

					currentlySelected = SHARED;
					setEnablednessOfAddPredicateButton();
					if (userChangedAtomicPropSelection && placeNames.size() > 0)
						updateQueryOnAtomicPropositionChange();
				}
			}
		});
		Dimension dim = new Dimension(235, 27);
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
		relationalOperatorBox = new JComboBox(new DefaultComboBoxModel(relationalSymbols));

		gbc.gridx = 1;
		predicatePanel.add(relationalOperatorBox, gbc);

		int currentValue = 0;
		int min = 0;
		int step = 1;
		placeMarking = new JSpinner(new SpinnerNumberModel(currentValue, min,Integer.MAX_VALUE, step));
		placeMarking.setMaximumSize(new Dimension(60, 30));
		placeMarking.setMinimumSize(new Dimension(60, 30));
		placeMarking.setPreferredSize(new Dimension(60, 30));

		gbc.gridx = 2;
		predicatePanel.add(placeMarking, gbc);

		addPredicateButton = new JButton("Add predicate to the query");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		predicatePanel.add(addPredicateButton, gbc);
		
		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setEnabled(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(2, 0, 2, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		predicatePanel.add(separator,gbc);
		
		truePredicateButton = new JButton("True");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		predicatePanel.add(truePredicateButton, gbc);
		
		falsePredicateButton = new JButton("False");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		predicatePanel.add(falsePredicateButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(predicatePanel, gbc);
		
		// Action listeners for predicate panel
		addPredicateButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String template = templateBox.getSelectedItem().toString();
				if(template.equals(SHARED)) template = "";
				TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
						template,
						(String) placesBox.getSelectedItem(),
						(String) relationalOperatorBox.getSelectedItem(),
						(Integer) placeMarking.getValue());
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		}

		);
		
		truePredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLTrueNode trueNode = new TCTLTrueNode();
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), trueNode);
				newProperty = newProperty.replace(currentSelection.getObject(), trueNode);
				updateSelection(trueNode);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		});
		
		falsePredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLFalseNode falseNode = new TCTLFalseNode();
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), falseNode);
				newProperty = newProperty.replace(currentSelection.getObject(), falseNode);
				updateSelection(falseNode);
				undoSupport.postEdit(edit);
				queryChanged();
			}
		});

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

		templateBox.setSelectedIndex(0); // Fills placesBox with correct places. Must be called here to ensure addPredicateButton is not null
	}

	private void initQueryEditingPanel() {
		// Editing buttons panel
		editingButtonPanel = new JPanel(new GridBagLayout());
		editingButtonPanel.setBorder(BorderFactory.createTitledBorder("Editing"));

		editingButtonsGroup = new ButtonGroup();
		deleteButton = new JButton("Delete selection");
		resetButton = new JButton("Reset query");
		undoButton = new JButton("Undo");
		redoButton = new JButton("Redo");
		editQueryButton = new JButton("Edit query");

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
				if (queryField.isEditable()) { // in edit mode, this button is now the parse query button.
					// User has potentially altered the query, so try to parse it
					TAPAALQueryParser queryParser = new TAPAALQueryParser();
					TCTLAbstractProperty newQuery = null;

					try {
						newQuery = queryParser.parse(queryField.getText());
					} catch (Exception ex) {
						int choice = JOptionPane.showConfirmDialog(
								CreateGui.getApp(),
								"TAPAAL encountered an error trying to parse the specified query.\n\nWe recommend using the query construction buttons unless you are an experienced user.\n\n The specified query has not been saved. Do you want to edit it again?",
								"Error Parsing Query",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.ERROR_MESSAGE);
						if (choice == JOptionPane.NO_OPTION)
							returnFromManualEdit(null);
						else
							return;
						
					}

					if (newQuery != null) // new query parsed successfully
					{
						// check correct place names are used in atomic propositions
						ArrayList<Tuple<String,String>> templatePlaceNames = new ArrayList<Tuple<String,String>>();
						for(TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
							for(TimedPlace p : tapn.places()) {
								templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
							}
						}
						
						for(TimedPlace p : tapnNetwork.sharedPlaces()) {
							templatePlaceNames.add(new Tuple<String, String>("", p.name()));
						}

						VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);

						VerifyPlaceNamesVisitor.Context c = nameChecker.VerifyPlaceNames(newQuery);

						if (!c.getResult()) {
							StringBuilder s = new StringBuilder();
							s.append("The following places was used in the query, but are not present in your model:\n\n");

							for (String placeName : c.getIncorrectPlaceNames()) {
								s.append(placeName);
								s.append('\n');
							}

							s.append("\nThe specified query has not been saved. Do you want to edit it again?");
							int choice = JOptionPane.showConfirmDialog(
									CreateGui.getApp(), s.toString(),
									"Error Parsing Query",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.ERROR_MESSAGE);
							if (choice == JOptionPane.NO_OPTION) {
								returnFromManualEdit(null);
							}
						} else {
							UndoableEdit edit = new QueryConstructionEdit(newProperty, newQuery);
							returnFromManualEdit(newQuery);
							undoSupport.postEdit(edit);
						}
					} else {
						returnFromManualEdit(null);
					}
				} else { // we are not in edit mode so the button should reset
					// the query

					TCTLPathPlaceHolder ph = new TCTLPathPlaceHolder();
					UndoableEdit edit = new QueryConstructionEdit(newProperty, ph);
					newProperty = ph;
					resetQuantifierSelectionButtons();
					updateSelection(newProperty);
					undoSupport.postEdit(edit);
				}
				queryChanged();
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
					queryChanged();
					setEnabledReductionOptions();
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
					queryChanged();
					setEnabledReductionOptions();
				}
			}
		});

		editQueryButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (queryField.isEditable()) { // we are in edit mode so the user pressed cancel
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

	private void initUppaalOptionsPanel() {

		uppaalOptionsPanel = new JPanel(new GridBagLayout());

		initSearchOptionsPanel();
		initTraceOptionsPanel();

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = new Insets(5,10,5,10);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(uppaalOptionsPanel, gridBagConstraints);

	}

	private void initSearchOptionsPanel() {
		searchOptionsPanel = new JPanel(new GridBagLayout());

		searchOptionsPanel.setBorder(BorderFactory.createTitledBorder("Search Strategy"));
		searchRadioButtonGroup = new ButtonGroup();
		breadthFirstSearch = new JRadioButton("Breadth First Search");
		depthFirstSearch = new JRadioButton("Depth First Search");
		randomSearch = new JRadioButton("Random Search");
		heuristicSearch = new JRadioButton("Heuristic Search");
		searchRadioButtonGroup.add(heuristicSearch);
		searchRadioButtonGroup.add(breadthFirstSearch);
		searchRadioButtonGroup.add(depthFirstSearch);
		searchRadioButtonGroup.add(randomSearch);
		
		heuristicSearch.setSelected(true);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		searchOptionsPanel.add(heuristicSearch, gridBagConstraints);
		gridBagConstraints.gridy = 1;
		searchOptionsPanel.add(breadthFirstSearch, gridBagConstraints);
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		searchOptionsPanel.add(depthFirstSearch, gridBagConstraints);
		gridBagConstraints.gridy = 1;
		searchOptionsPanel.add(randomSearch, gridBagConstraints);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		uppaalOptionsPanel.add(searchOptionsPanel, gridBagConstraints);

	}

	private void initTraceOptionsPanel() {
		traceOptionsPanel = new JPanel(new GridBagLayout());
		traceOptionsPanel.setBorder(BorderFactory.createTitledBorder("Trace Options"));
		traceRadioButtonGroup = new ButtonGroup();
		someTraceRadioButton = new JRadioButton(UPPAAL_SOME_TRACE_STRING);
		noTraceRadioButton = new JRadioButton("No trace");
		traceRadioButtonGroup.add(someTraceRadioButton);
		traceRadioButtonGroup.add(noTraceRadioButton);

		someTraceRadioButton.setEnabled(false);
		noTraceRadioButton.setSelected(true);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		traceOptionsPanel.add(someTraceRadioButton, gridBagConstraints);
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		traceOptionsPanel.add(noTraceRadioButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		uppaalOptionsPanel.add(traceOptionsPanel, gridBagConstraints);

	}

	private void initReductionOptionsPanel() {
		reductionOptionsPanel = new JPanel(new GridBagLayout());
		reductionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Verification Method"));
		Dimension d = new Dimension(898, 100);
		reductionOptionsPanel.setPreferredSize(d);
		reductionOption = new JComboBox();
		setEnabledReductionOptions();

		reductionOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox)e.getSource();
				String selectedItem = (String)source.getSelectedItem();
				if(selectedItem != null) {
					setEnabledOptionsAccordingToCurrentReduction();
				}
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(new JLabel("  Choose verification method:"), gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(reductionOption, gbc);

		symmetryReduction = new JCheckBox("Use Symmetry Reduction");
		symmetryReduction.setSelected(true);
		symmetryReduction.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				refreshTraceOptions();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(symmetryReduction, gbc);

		discreteInclusion = new JCheckBox("Use Discrete Inclusion");
		discreteInclusion.setVisible(true);
		discreteInclusion.setToolTipText("<html>This optimization will perform more advanced inclusion check<br/>" +
										 "in an attempt to reduce the number of explored states.<br/>" +
										 "<b>Note:</b> This may have an adverse affect on performance on some models!</html>");
		discreteInclusion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectInclusionPlacesButton.setEnabled(discreteInclusion.isSelected());
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);	
		reductionOptionsPanel.add(discreteInclusion, gbc);
		
		selectInclusionPlacesButton = new JButton("Select Inclusion Places");
		selectInclusionPlacesButton.setEnabled(false);
		selectInclusionPlacesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inclusionPlaces = ChooseInclusionPlacesDialog.showInclusionPlacesDialog(tapnNetwork, inclusionPlaces);
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);	
		reductionOptionsPanel.add(selectInclusionPlacesButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		add(reductionOptionsPanel, gbc);
	}

	protected void setEnabledOptionsAccordingToCurrentReduction() {
		refreshQueryEditingButtons();
		refreshSymmetryReduction();
		refreshDiscreteInclusion();
		refreshTraceOptions();
		refreshSearchOptions();
		refreshExportButtonText();
	}

	private void refreshDiscreteInclusion() {
		ReductionOption reduction = getReductionOption();
		if(reduction.equals(ReductionOption.VerifyTAPN)){
			discreteInclusion.setVisible(true);
			selectInclusionPlacesButton.setVisible(true);
			queryChanged(); // This ensures the checkbox is disabled if query is not upward closed
		}else{
			discreteInclusion.setVisible(false);
			selectInclusionPlacesButton.setVisible(false);
		}
	}

	private void refreshExportButtonText() {
		ReductionOption reduction = getReductionOption();
		
		saveUppaalXMLButton.setText(reduction == ReductionOption.VerifyTAPN ? EXPORT_VERIFYTAPN_BTN_TEXT : EXPORT_UPPAAL_BTN_TEXT);
	}

	private void refreshQueryEditingButtons() {
		if(currentSelection != null) {
			if(currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
				enableOnlyPathButtons();
			} else if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
				enableOnlyStateButtons();
			}
			updateQueryButtonsAccordingToSelection();
		}
		String reduction = getReductionOptionAsString();
		if(reduction.equals(name_verifyTAPN) || reduction.equals(name_STANDARD) || (reduction.equals(name_OPTIMIZEDSTANDARD) && !isNetDegree2)) {
			existsBox.setEnabled(false);
			forAllDiamond.setEnabled(false);
		}
	}

	private void refreshSymmetryReduction() {
//		if(((String)reductionOption.getSelectedItem()).equals(name_verifyTAPN)) {
//			//symmetryReduction.setSelected(true);
//			//symmetryReduction.setEnabled(false);
//		}
//		else{
//			symmetryReduction.setSelected(symmetryReduction.isSelected());
//			symmetryReduction.setEnabled(true);
//		}
	}
	
	private void queryChanged(){
		UpwardsClosedVisitor visitor = new UpwardsClosedVisitor();
		boolean isUpwardClosed = visitor.isUpwardClosed(newProperty);
		discreteInclusion.setEnabled(isUpwardClosed);
		discreteInclusion.setSelected(isUpwardClosed ? discreteInclusion.isSelected() : false);
	}
	

	private void initButtonPanel(QueryDialogueOption option) {
		buttonPanel = new JPanel(new FlowLayout());
		if (option == QueryDialogueOption.Save) {
			saveButton = new JButton("Save");
			saveAndVerifyButton = new JButton("Save and Verify");
			cancelButton = new JButton("Cancel");
			saveUppaalXMLButton = new JButton(EXPORT_UPPAAL_BTN_TEXT);

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
					ReductionOption reduction = getReductionOption();
					try {
						FileBrowser browser = new FileBrowser(reduction == ReductionOption.VerifyTAPN ? "Verifytapn XML" : "Uppaal XML",	"xml", xmlFile);
						xmlFile = browser.saveFile();
						if (xmlFile != null) {
							String[] a = xmlFile.split(".xml");
							queryFile = a[0] + ".q";
						}

					} catch (Exception ex) {
						JOptionPane.showMessageDialog(CreateGui.getApp(),
								"There were errors performing the requested action:\n"
								+ e, "Error",
								JOptionPane.ERROR_MESSAGE);
					}

					if (xmlFile != null && queryFile != null) {
						TAPNComposer composer = new TAPNComposer();
						Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(QueryDialog.this.tapnNetwork);

						TAPNQuery tapnQuery = getQuery();
						dk.aau.cs.model.tapn.TAPNQuery clonedQuery = new dk.aau.cs.model.tapn.TAPNQuery(tapnQuery.getProperty().copy(), tapnQuery.getCapacity());
						
						RenameAllPlacesVisitor visitor = new RenameAllPlacesVisitor(transformedModel.value2());
						clonedQuery.getProperty().accept(visitor, null);
						
						if(reduction == ReductionOption.VerifyTAPN) {
							VerifyTAPNExporter exporter = new VerifyTAPNExporter();
							exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile));
						} else {
							UppaalExporter exporter = new UppaalExporter();
							try {
								exporter.export(transformedModel.value1(), clonedQuery, tapnQuery.getReductionOption(), new File(xmlFile), new File(queryFile), tapnQuery.useSymmetry());
							} catch(Exception exportException) {
								StringBuilder s = new StringBuilder();
								if(exportException instanceof UnsupportedModelException)
									s.append(UNSUPPORTED_MODEL_TEXT + "\n\n");
								else if(exportException instanceof UnsupportedQueryException)
									s.append(UNSUPPPORTED_QUERY_TEXT + "\n\n");
								
								if(reduction == ReductionOption.VerifyTAPN)
									s.append(NO_VERIFYTAPN_XML_FILE_SAVED);
								else
									s.append(NO_UPPAAL_XML_FILE_SAVED);
								
								JOptionPane.showMessageDialog(CreateGui.getApp(), s.toString());
							}
						}
					} else {
						JOptionPane.showMessageDialog(CreateGui.getApp(), reduction == ReductionOption.VerifyTAPN ? NO_VERIFYTAPN_XML_FILE_SAVED : NO_UPPAAL_XML_FILE_SAVED);
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
