package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

import javax.swing.*;
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
import pipe.dataLayer.NetWriter;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.MessengerImpl;
import pipe.gui.Verifier;
import pipe.gui.Zoomer;
import dk.aau.cs.TCTL.StringPosition;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAXNode;
import dk.aau.cs.TCTL.TCTLAUNode;
import dk.aau.cs.TCTL.TCTLAbstractPathProperty;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLEXNode;
import dk.aau.cs.TCTL.TCTLEUNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLPathToStateConverter;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLStateToPathConverter;
import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.CTLParsing.TAPAALCTLQueryParser;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.visitors.FixAbbrivPlaceNames;
import dk.aau.cs.TCTL.visitors.FixAbbrivTransitionNames;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.IsReachabilityVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.TCTL.visitors.VerifyTransitionNamesVisitor;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.UPPAAL.UppaalExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNExporter;
import pipe.gui.widgets.filebrowser.FileBrowser;

public class CTLQueryDialog extends JPanel {

	private static final String NO_UPPAAL_XML_FILE_SAVED = "No Uppaal XML file saved.";
	private static final String NO_VERIFYTAPN_XML_FILE_SAVED = "No verifytapn XML file saved.";
	private static final String UNSUPPORTED_MODEL_TEXT = "The model is not supported by the chosen reduction.";
	private static final String UNSUPPPORTED_QUERY_TEXT = "The query is not supported by the chosen reduction.";
	private static final String EXPORT_UPPAAL_BTN_TEXT = "Export UPPAAL XML";
	private static final String EXPORT_VERIFYTAPN_BTN_TEXT = "Export TAPAAL XML";
	private static final String EXPORT_VERIFYPN_BTN_TEXT = "Export PN XML";
	private static final String EXPORT_COMPOSED_BTN_TEXT = "Open composed net";
	
	private static final String SHARED = "Shared";

	private static final long serialVersionUID = 7852107237344005546L;

	public enum QueryDialogueOption {
		VerifyNow, Save, Export
	}

	private boolean querySaved = false;

	private JRootPane rootPane;
	private static EscapableDialog guiDialog;

	// Query Name Panel;
	private JPanel namePanel;
	private JButton advancedButton;

	// Boundedness check panel
	private JPanel boundednessCheckPanel;
	private CustomJSpinner numberOfExtraTokensInNet;
	private JButton kbounded;

	// Query Panel
	private JPanel queryPanel;

	private JPanel quantificationPanel;
	private ButtonGroup quantificationButtonGroup;
	private JButton existsDiamond;
	private JButton existsBox;
	private JButton existsNext;
	private JButton existsUntil;
	private JButton forAllDiamond;
	private JButton forAllBox;
	private JButton forAllNext;
	private JButton forAllUntil;

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
	private JComboBox placesTransitionsBox;
	private JComboBox relationalOperatorBox;
	private JLabel transitionIsEnabledLabel;
	private CustomJSpinner placeMarking;
	private JButton truePredicateButton;
	private JButton falsePredicateButton;
	private JButton deadLockPredicateButton;

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
	private JComboBox<String> reductionOption;
	private ButtonGroup verifOptionsRadioButtonGroup;
	private JCheckBox useSiphonTrap;
	private JCheckBox useQueryReduction;
	private JCheckBox useStubbornReduction;
	private JCheckBox useReduction;

	// Buttons in the bottom of the dialogue
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton saveButton;
	private JButton saveAndVerifyButton;
	private JButton saveUppaalXMLButton;
	private JButton openComposedNetButton;

	// Private Members
	private StringPosition currentSelection = null;

	private final TimedArcPetriNetNetwork tapnNetwork;
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels;
	private QueryConstructionUndoManager undoManager;
	private UndoableEditSupport undoSupport;
	private boolean isNetDegree2;
	private boolean hasInhibitorArcs;
	private InclusionPlaces inclusionPlaces;

	private String name_verifyTAPN = "TAPAAL: Continous Engine (verifytapn)";
	private String name_COMBI = "UPPAAL: Optimized Broadcast Reduction";
	private String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private String name_STANDARD = "UPPAAL: Standard Reduction";
	private String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private String name_DISCRETE = "TAPAAL: Discrete Engine (verifydtapn)";
	private String name_UNTIMED = "TAPAAL: Untimed Engine (verifypn)";
	
	private boolean userChangedAtomicPropSelection = true;

	private TCTLAbstractProperty newProperty;
	private JTextField queryName;

	private static Boolean advancedView = false;

	//Strings for tool tips
	//Tool tips for top panel
	private static final String TOOL_TIP_QUERYNAME = "Enter the name of the query.";
	private static final String TOOL_TIP_INFO_BUTTON = "Get help on the different verification options.";
	private static final String TOOL_TIP_ADVANCED_VIEW_BUTTON = "Switch to the advanced view.";
	private static final String TOOL_TIP_SIMPLE_VIEW_BUTTON = "Switch to the simple view.";

	//Tool tip for query field
	private final static String TOOL_TIP_QUERY_FIELD = "<html>Click on a part of the query you want to edit.<br />" +
			"(Queries can be edited also manually by pressing the \"Edit Query\" button.)</html>";

	//Tool tips for quantification panel
	private static final String TOOL_TIP_EXISTS_DIAMOND = "Check if the given marking is reachable in the net.";
	private static final String TOOL_TIP_EXISTS_BOX = "Check if there is a trace on which all markings satisfy the given property. (Available only for some verification engines.)";
	private static final String TOOL_TIP_FORALL_DIAMOND = "Check if on any maxiaml trace there is marking that satisfies the given property. (Available only for some verification engines.)";
	private static final String TOOL_TIP_FORALL_BOX = "Check if every reachable marking in the net satifies the given property.";
	
	private static final String TOOL_TIP_EXISTS_UNTIL = "There is a computation where the first formula holds until the second one holds.";
	private static final String TOOL_TIP_EXISTS_NEXT = "There is a transition firing after which the reached marking satisfies the given property.";
	private static final String TOOL_TIP_FORALL_UNTIL = "On every computation the first formula holds until the second one holds";
	private static final String TOOL_TIP_FORALL_NEXT = "After any transition firing the reached marking satisfies the given property.";
	
	
	//Tool tips for logic panel
	private static final String TOOL_TIP_CONJUNCTIONBUTTON = "Expand the currently selected part of the query with a conjunction.";
	private static final String TOOL_TIP_DISJUNCTIONBUTTON = "Expand the currently selected part of the query with a disjunction.";
	private static final String TOOL_TIP_NEGATIONBUTTON = "Negate the currently selected part of the query.";

	//Tool tips for query panel
	private static final String TOOL_TIP_PLACESTRANSITIONSBOX = "Choose a place or transition for the predicate.";
	private static final String TOOL_TIP_TEMPLATEBOX = "Choose a component considered for this predicate.";
	private static final String TOOL_TIP_RELATIONALOPERATORBOX = "Choose a relational operator comparing the number of tokens in the chosen place.";
	private static final String TOOL_TIP_PLACEMARKING = "Choose a number of tokens.";
	private static final String TOOL_TIP_ADDPREDICATEBUTTON = "Add the predicate specified above to the query.";
	private static final String TOOL_TIP_TRUEPREDICATEBUTTON = "Add the value true to the query.";
	private static final String TOOL_TIP_FALSEPREDICATEBUTTON = "Add the value false to the query.";
	private static final String TOOL_TIP_DEADLOCKPREDICATEBUTTON = "<html>Add the deadlock predicate to the query.<br />" +
			"<br />A marking is a deadlock if there is no delay<br /> after which at least one transition gets enabled.</html>";

	//Tool tips for editing panel
	private static final String TOOL_TIP_DELETEBUTTON = "Delete the currently selected part of the query.";
	private static final String TOOL_TIP_RESETBUTTON = "Completely reset the query.";
	private static final String TOOL_TIP_UNDOBUTTON = "Undo the last action.";
	private static final String TOOL_TIP_REDOBUTTON = "Redo the last undone action.";
	private static final String TOOL_TIP_EDITQUERYBUTTON = "Edit the query manually.";
	private final static String TOOL_TIP_PARSE_QUERY = "Parse the manually edited query.";
	private final static String TOOL_TIP_CANCEL_QUERY = "Cancel manual query creating.";

	//Tool tips for boundedness check panel
	private static final String TOOL_TIP_NUMBEROFEXTRATOKENSINNET = "A number of extra tokens allowed in the net.";
	private static final String TOOL_TIP_KBOUNDED = "Check wheather the net is bounded for the given number of extra tokens.";

	//Tool tips for reduction options panel
	private final static String TOOL_TIP_REDUCTION_OPTION = "Choose a verification engine.";
	
	//Tool tips for search options panel
	private final static String TOOL_TIP_HEURISTIC_SEARCH = "<html>Uses a heuiristic method in state space exploration.<br />" +
			"If heuristic search is not applicable, DFS is used instead.<br/>Click the button <em>Help on the query options</em> to get more info.</html>";
	private final static String TOOL_TIP_BREADTH_FIRST_SEARCH = "Explores markings in a breadth first manner.";
	private final static String TOOL_TIP_DEPTH_FIRST_SEARCH = "Explores markings in a depth first manner.";
	private final static String TOOL_TIP_RANDOM_SEARCH = "Performs a random exploration of the state space.";

	//Tool tips for trace options panel
	private final static String TOOL_TIP_FASTEST_TRACE = "Show a fastest concrete trace if applicable (verification can be slower with this trace option).";
	private final static String TOOL_TIP_SOME_TRACE = "Show a concrete trace whenever applicable (only available for EF/AG reachability queries).";
	private final static String TOOL_TIP_NO_TRACE = "Do not display any trace information.";

	//Tool tips for buttom panel
	private final static String TOOL_TIP_SAVE_BUTTON = "Save the query.";
	private final static String TOOL_TIP_SAVE_AND_VERIFY_BUTTON = "Save and verify the query.";
	private final static String TOOL_TIP_CANCEL_BUTTON = "Cancel the changes made in this dialog.";
	private final static String TOOL_TIP_SAVE_UPPAAL_BUTTON = "Export an xml file that can be opened in UPPAAL GUI.";
	private final static String TOOL_TIP_SAVE_COMPOSED_BUTTON = "Export an xml file of composed net and approximated net if enabled";
	private final static String TOOL_TIP_SAVE_TAPAAL_BUTTON = "Export an xml file that can be used as input for the TAPAAL engine.";
	
	//Tool tips for approximation panel
	private final static String TOOL_TIP_APPROXIMATION_METHOD_NONE = "No approximation method is used.";
	private final static String TOOL_TIP_APPROXIMATION_METHOD_OVER = "Approximate by dividing all intervals with the approximation constant and enlarging the intervals.";
	
	//Tool tips for verification options
	private final static String TOOL_TIP_USE_STRUCTURALREDUCTION = "Apply structural reductions to reduce the size of the net.";
	private final static String TOOL_TIP_USE_SIPHONTRAP = "For a deadlock query, attempt to prove deadlock-freeness by using siphon-trap analysis via linear programming.";
	private final static String TOOL_TIP_USE_QUERY_REDUCTION = "Use query rewriting rules and linear programming (state equations) to reduce the size of the query.";
	private final static String TOOL_TIP_USE_STUBBORN_REDUCTION = "Use stubborn set reduction for queries where it is applicable.";
	
	
	//Tool tips for query types
	private final static String TOOL_TIP_QUERYTYPE = "Choose a query type";
	
	public CTLQueryDialog(EscapableDialog me, QueryDialogueOption option,
			TAPNQuery queryToCreateFrom, TimedArcPetriNetNetwork tapnNetwork, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		this.tapnNetwork = tapnNetwork;
		this.guiModels = guiModels;
		inclusionPlaces = queryToCreateFrom == null ? new InclusionPlaces() : queryToCreateFrom.inclusionPlaces();
		
		// Attempt to parse and possibly transform the string query using the manual edit parser
		try {
			newProperty = TAPAALCTLQueryParser.parse(queryToCreateFrom.getProperty().toString());
		} catch (Throwable ex) {
			newProperty = queryToCreateFrom == null ? new TCTLPathPlaceHolder() : queryToCreateFrom.getProperty();
		}
		
		rootPane = me.getRootPane();
		isNetDegree2 = tapnNetwork.isDegree2();
		hasInhibitorArcs = tapnNetwork.hasInhibitorArcs();

		setLayout(new GridBagLayout());

		init(option, queryToCreateFrom);
		toggleAdvancedSimpleView(false);
	}

	private boolean checkIfSomeReductionOption() {
		if (reductionOption.getSelectedItem() == null){
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"No verification engine supports the combination of this query and the current model",
					"No verification engine", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void setQueryFieldEditable(boolean isEditable) {
		queryField.setEditable(isEditable);
		queryField.setToolTipText(isEditable ? null : TOOL_TIP_QUERY_FIELD);
		//XXX Workaround to fix SWING bug where caret is sometimes not shown in edit mode -- Mathias
		queryField.setFocusable(false);
		queryField.setFocusable(true);
		queryField.requestFocus(true);
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

		// These are only set because the TAPNQuery constructor needs them. Should make a new constructor. 
		boolean symmetry = false;
		boolean timeDarts = false;
		boolean pTrie = false;
		boolean gcd = false;
		boolean overApproximation = false;
		boolean reduction = useReduction.isSelected();
		boolean overApproximationEnable = false;
		boolean underApproximationEnable = false;
		int overApproximationDenominator = 0;
		
		TAPNQuery query = new TAPNQuery(name, capacity, newProperty.copy(), traceOption, searchOption, reductionOptionToSet, symmetry, gcd, timeDarts, pTrie, overApproximation, reduction, /* hashTableSizeToSet */ null, /* extrapolationOptionToSet */null, inclusionPlaces, overApproximationEnable, underApproximationEnable, overApproximationDenominator);
		query.setCategory(TAPNQuery.QueryCategory.CTL);
		query.setUseSiphontrap(useSiphonTrap.isSelected());
		query.setUseQueryReduction(useQueryReduction.isSelected());
		query.setUseStubbornReduction(useStubbornReduction.isSelected());
		return query;
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
		else if(breadthFirstSearch.isSelected())
			return SearchOption.BFS;
		else
			return SearchOption.DEFAULT;
	}

	private ReductionOption getReductionOption() {
		String reductionOptionString = (String)reductionOption.getSelectedItem();
		if (reductionOptionString == null)
			return null;
		else if (reductionOptionString.equals(name_STANDARD))
			return ReductionOption.STANDARD;
		else if (reductionOptionString.equals(name_COMBI))
			return ReductionOption.COMBI;
		else if (reductionOptionString.equals(name_OPTIMIZEDSTANDARD))
			return ReductionOption.OPTIMIZEDSTANDARD;
		else if (reductionOptionString.equals(name_BROADCASTDEG2))
			return ReductionOption.DEGREE2BROADCAST;
		else if (reductionOptionString.equals(name_verifyTAPN))
			return ReductionOption.VerifyTAPN;
		else if (reductionOptionString.equals(name_DISCRETE))
			return ReductionOption.VerifyTAPNdiscreteVerification;
		else if (reductionOptionString.equals(name_UNTIMED))
			return ReductionOption.VerifyPN;
		else
			return ReductionOption.BROADCAST;
	}

	private String getReductionOptionAsString() {
		return (String)reductionOption.getSelectedItem();
	}

	private void refreshTraceOptions() {
		if(reductionOption.getSelectedItem() == null){
			return;
		}

		if(queryIsReachability()){
			someTraceRadioButton.setEnabled(true);
			noTraceRadioButton.setEnabled(true);

			if(getTraceOption() == TraceOption.NONE) {
				noTraceRadioButton.setSelected(true);
			} else {
				someTraceRadioButton.setSelected(true);
			}

		} else {
			someTraceRadioButton.setEnabled(false);
			noTraceRadioButton.setEnabled(false);
			noTraceRadioButton.setSelected(true);
		}
	}

	private void resetQuantifierSelectionButtons() {
		//quantificationRadioButtonGroup.clearSelection();
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	public boolean queryHasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(newProperty);
	}
	public boolean queryIsReachability(){
		return new IsReachabilityVisitor().isReachability(newProperty);
	}

	public static TAPNQuery showQueryDialogue(QueryDialogueOption option, TAPNQuery queryToRepresent, TimedArcPetriNetNetwork tapnNetwork, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		if(CreateGui.getCurrentTab().network().hasWeights() && !CreateGui.getCurrentTab().network().isNonStrict()){
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"No reduction option supports both strict intervals and weigthed arcs", 
					"No reduction option", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		guiDialog = new EscapableDialog(CreateGui.getApp(),	"Edit CTL Query", true);

		Container contentPane = guiDialog.getContentPane();
		// 1 Set layout
		//contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		contentPane.setLayout(new GridBagLayout());

		// 2 Add query editor
		CTLQueryDialog queryDialogue = new CTLQueryDialog(guiDialog, option, queryToRepresent, tapnNetwork, guiModels);
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
	
	private TCTLAbstractPathProperty getPathProperty(TCTLAbstractProperty property) {
		if (property instanceof TCTLAbstractPathProperty) {
			return (TCTLAbstractPathProperty) property.copy();
		} else {
			return new TCTLPathPlaceHolder();
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
		TCTLAbstractProperty current = currentSelection.getObject();
		if(current instanceof TCTLStateToPathConverter){
		    current = ((TCTLStateToPathConverter)current).getProperty();
		}
		
		if (current instanceof TCTLAtomicPropositionNode) {
		    TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) current;
		    
		    // bit of a hack to prevent posting edits to the undo manager when
		    // we programmatically change the selection in the atomic proposition comboboxes etc.
		    // because a different atomic proposition was selected
		    userChangedAtomicPropSelection = false;
		    if (node.getLeft() instanceof TCTLPlaceNode) {
			TCTLPlaceNode placeNode = (TCTLPlaceNode) node.getLeft();
			if(placeNode.getTemplate().equals("")){
			    templateBox.setSelectedItem(SHARED);
			} else {
			    templateBox.setSelectedItem(tapnNetwork.getTAPNByName(placeNode.getTemplate()));
			}
			placesTransitionsBox.setSelectedItem(placeNode.getPlace());
		    } else {
			if (placesTransitionsBox.getItemCount() > 0){
			    placesTransitionsBox.setSelectedIndex(0);
			}
		    }
		    
		    relationalOperatorBox.setSelectedItem(node.getOp());

		    if(node.getRight() instanceof TCTLConstNode) {
			TCTLConstNode placeMarkingNode = (TCTLConstNode) node.getRight();
			placeMarking.setValue(placeMarkingNode.getConstant());
		    }
		    userChangedAtomicPropSelection = true;
		} else if (current instanceof TCTLTransitionNode) {
		    TCTLTransitionNode transitionNode = (TCTLTransitionNode)current;
		    userChangedAtomicPropSelection = false;
		    if(transitionNode.getTemplate().equals("")){
			    templateBox.setSelectedItem(SHARED);
		    } else {
			    templateBox.setSelectedItem(tapnNetwork.getTAPNByName(transitionNode.getTemplate()));
		    }
		    placesTransitionsBox.setSelectedItem(transitionNode.getTransition());
		    userChangedAtomicPropSelection = true;
		}
		
		setEnablednessOfOperatorAndMarkingBoxes();
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
			openComposedNetButton.setEnabled(isQueryOk);
		} else {
			saveButton.setEnabled(false);
			saveAndVerifyButton.setEnabled(false);
			saveUppaalXMLButton.setEnabled(false);
			openComposedNetButton.setEnabled(false);
		}
	}
	
	private void setEnabledReductionOptions(){
		reductionOption.removeAllItems();
		reductionOption.addItem(name_UNTIMED);
	}

	private void updateSearchStrategies(){
		if(reductionOption.getSelectedItem() == null){
			return;
		}

		JRadioButton currentselected;
		if(heuristicSearch.isSelected()){
			currentselected = heuristicSearch;
		}else if(breadthFirstSearch.isSelected()){
			currentselected = breadthFirstSearch;
		}else if(depthFirstSearch.isSelected()){
			currentselected = depthFirstSearch;
		}else{
			currentselected = randomSearch;
		}

		breadthFirstSearch.setEnabled(true);
		depthFirstSearch.setEnabled(true);
		heuristicSearch.setEnabled(true);
		randomSearch.setEnabled(true);
		
		// TODO: Should breadthFirstSearch be disabled here?
		/*
		if(getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")){
			breadthFirstSearch.setEnabled(false);
		}
		 */
		
		if(!currentselected.isEnabled()){
			if(depthFirstSearch.isEnabled()){
				depthFirstSearch.setSelected(true);
			} else {
				breadthFirstSearch.setSelected(true);
			}
		}
	}

	private void disableAllQueryButtons() {
		existsBox.setEnabled(false);
		existsDiamond.setEnabled(false);
		forAllBox.setEnabled(false);
		forAllDiamond.setEnabled(false);
		existsUntil.setEnabled(false);
		existsNext.setEnabled(false);
		forAllUntil.setEnabled(false);
		forAllNext.setEnabled(false);
		
		conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		negationButton.setEnabled(false);
		templateBox.setEnabled(false);
		placesTransitionsBox.setEnabled(false);
		relationalOperatorBox.setEnabled(false);
		placeMarking.setEnabled(false);
		addPredicateButton.setEnabled(false);
		truePredicateButton.setEnabled(false);
		falsePredicateButton.setEnabled(false);
		deadLockPredicateButton.setEnabled(false);
	}

	private void enableOnlyPathButtons() {
		existsBox.setEnabled(true);
		existsDiamond.setEnabled(true);
		forAllBox.setEnabled(true);
		forAllDiamond.setEnabled(true);
		existsUntil.setEnabled(true);
		existsNext.setEnabled(true);
		forAllUntil.setEnabled(true);
		forAllNext.setEnabled(true);
		
		conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		negationButton.setEnabled(false);
		templateBox.setEnabled(false);
		placesTransitionsBox.setEnabled(false);
		relationalOperatorBox.setEnabled(false);
		placeMarking.setEnabled(false);
		addPredicateButton.setEnabled(false);
		truePredicateButton.setEnabled(false);
		falsePredicateButton.setEnabled(false);
		deadLockPredicateButton.setEnabled(false);
	}

	private void enableOnlyStateButtons() {
		existsBox.setEnabled(true);
		existsDiamond.setEnabled(true);
		forAllBox.setEnabled(true);
		forAllDiamond.setEnabled(true);
		existsUntil.setEnabled(true);
		existsNext.setEnabled(true);
		forAllUntil.setEnabled(true);
		forAllNext.setEnabled(true);
		
		conjunctionButton.setEnabled(true);
		disjunctionButton.setEnabled(true);
		negationButton.setEnabled(true);
		templateBox.setEnabled(true);
		placesTransitionsBox.setEnabled(true);
		relationalOperatorBox.setEnabled(true);
		placeMarking.setEnabled(true);
		truePredicateButton.setEnabled(true);
		falsePredicateButton.setEnabled(true);
		deadLockPredicateButton.setEnabled(true);
		setEnablednessOfAddPredicateButton();
		setEnablednessOfOperatorAndMarkingBoxes();

	}

	private void setEnablednessOfAddPredicateButton() {
		if (placesTransitionsBox.getSelectedItem() == null)
			addPredicateButton.setEnabled(false);
		else
			addPredicateButton.setEnabled(true);
	}
	
	private void setEnablednessOfOperatorAndMarkingBoxes(){
		if (transitionIsSelected()){
			placeMarking.setVisible(false);
			relationalOperatorBox.setVisible(false);
			transitionIsEnabledLabel.setVisible(true);
		} else{
			transitionIsEnabledLabel.setVisible(false);
			placeMarking.setVisible(true);
			relationalOperatorBox.setVisible(true);
		}
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
		transitionIsEnabledLabel.setEnabled(true);
		resetButton.setToolTipText(TOOL_TIP_RESETBUTTON);
		editQueryButton.setToolTipText(TOOL_TIP_EDITQUERYBUTTON);
		enableEditingButtons();
		
		queryChanged();
	}

	private void changeToEditMode() {
		setQueryFieldEditable(true);
		resetButton.setText("Parse query");
		editQueryButton.setText("Cancel");
		transitionIsEnabledLabel.setEnabled(false);
		resetButton.setToolTipText(TOOL_TIP_PARSE_QUERY);
		editQueryButton.setToolTipText(TOOL_TIP_CANCEL_QUERY);
		clearSelection();
		disableAllQueryButtons();
		disableEditingButtons();
		setSaveButtonsEnabled();
		
		// Set default caret location to end of query
		queryField.setCaretPosition(queryField.getText().length());
	}

	private void updateQueryOnAtomicPropositionChange() {
		if (currentSelection != null && 
			(currentSelection.getObject() instanceof TCTLAtomicPropositionNode || 
			currentSelection.getObject() instanceof TCTLTransitionNode)) {
			Object item = templateBox.getSelectedItem();
			String template = item.equals(SHARED) ? "" : item.toString();
			TCTLAbstractStateProperty property;
			
			if (transitionIsSelected()){
                            property = new TCTLTransitionNode(template, (String) placesTransitionsBox.getSelectedItem());
			} else {
                            property = new TCTLAtomicPropositionNode(
				new TCTLPlaceNode(template, (String) placesTransitionsBox.getSelectedItem()),
                                (String) relationalOperatorBox.getSelectedItem(),
                                new TCTLConstNode((Integer) placeMarking.getValue()));
			}
			
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

		initQueryPanel();
		initUppaalOptionsPanel();
		initReductionOptionsPanel();
		initButtonPanel(option);

		if(queryToCreateFrom != null) {
			setupFromQuery(queryToCreateFrom);
		}

		refreshTraceOptions();
		setEnabledReductionOptions();
		
		rootPane.setDefaultButton(saveAndVerifyButton);
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
		setupSearchOptionsFromQuery(queryToCreateFrom);
		setupTraceOptionsFromQuery(queryToCreateFrom);
		setupReductionOptionsFromQuery(queryToCreateFrom);
	}

	private void setupReductionOptionsFromQuery(TAPNQuery queryToCreateFrom) {
		String reduction = "";
	
		if(queryToCreateFrom.getReductionOption() == ReductionOption.VerifyPN){
			reduction = name_UNTIMED;
		}

		reductionOption.addItem(reduction); 
		reductionOption.setSelectedItem(reduction);
		useSiphonTrap.setSelected(queryToCreateFrom.isSiphontrapEnabled());
		useQueryReduction.setSelected(queryToCreateFrom.isQueryReductionEnabled());
		useStubbornReduction.setSelected(queryToCreateFrom.isStubbornReductionEnabled());
		useReduction.setSelected(queryToCreateFrom.useReduction());
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

	private void initQueryNamePanel() {

		JPanel splitter = new JPanel(new BorderLayout());


		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Query name: "));

		queryName = new JTextField("Query Comment/Name Here", 25);
		queryName.setToolTipText(TOOL_TIP_QUERYNAME);

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
		
		advancedButton = new JButton("Advanced view");
		advancedButton.setToolTipText(TOOL_TIP_ADVANCED_VIEW_BUTTON);
		advancedButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				toggleAdvancedSimpleView(true);
			}
		});

		JButton infoButton = new JButton("Help on the query options");	
		infoButton.setToolTipText(TOOL_TIP_INFO_BUTTON);
		infoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(CTLQueryDialog.this, getMessageComponent(), "Help", JOptionPane.INFORMATION_MESSAGE);
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
				buffer.append("<b>Boundedness Options</b><br/>");
				buffer.append("The query dialog allows you to specify the extra number of tokens that TAPAAL is allowed to use during the verification. ");
				buffer.append("Because TAPAAL models can produce additional tokens by firing transitions (e.g. a transition that has a single input place ");
				buffer.append("and two output places) you may need to use additional tokens compared to those that are already in the net. By ");
				buffer.append("specifying an extra number of tokens you can ask TAPAAL to check if your net is bounded for this number of extra tokens (i.e. ");
				buffer.append("whether there is no reachable marking in the net that would exceed the predefined number of tokens. ");
				buffer.append("<br/><br/>");
				buffer.append("<b>Search Strategy Options</b><br/>");
				buffer.append("A search strategy determines how the chosen verification engine performs the search. The possible search strategies are: ");
				buffer.append("<ul>");
				buffer.append("<li>Heuristic Search<br/> If available, the search is guided according to the query so that the most likely places where the query is satisfied are visited first. For CTL queries that cannot be simplified to reachability queries, the engine will run DFS if heuristic strategy is selected.");
				buffer.append("<li>Breadth First Search<br/>Explores markings in a breadth first manner. Only available for pure reachability queries, for CTL queries use DFS.</li>");
				buffer.append("<li>Depth First Search<br/>Explores markings in a depth first manner.</li>");
				buffer.append("<li>Random Search<br/>Performs a random exploration of the state space. Only available for pure reachability queries, for CTL queries use DFS.</li>");
				buffer.append("</ul>");
				buffer.append("<br/>");
				buffer.append("<b>Verification Options</b><br/>");
				buffer.append("For CTL queries TAPAAL uses its own untimed engine. If your net is untimed, we recommend that you always use the CTL query creation dialog even for reachability queries. Here you can select whether structural reduction prior to the state-space exploration should be used, whether to use siphon-trap analysis for detecting deadlock freedom, and whether to apply query reductions and use stubborn sets during the state-space exploration.");
				buffer.append("<br/>");
				buffer.append("</html>");
				return buffer.toString();
			}

		});
		JPanel topButtonPanel = new JPanel(new FlowLayout());
		topButtonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		topButtonPanel.add(advancedButton);
		topButtonPanel.add(infoButton);
		splitter.add(namePanel, BorderLayout.LINE_START);
		splitter.add(topButtonPanel, BorderLayout.LINE_END);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(5,10,0,10);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(splitter, gridBagConstraints);
	}

	public static void setAdvancedView(boolean advanced){
		advancedView = advanced;
	}

	public static boolean getAdvancedView(){
		return advancedView;
	}

	private void toggleAdvancedSimpleView(boolean changeState){
		//Make sure that the right properties are set when the pane is initialized
		if(changeState){
			setAdvancedView(!advancedView);
		}
		Point location = guiDialog.getLocation();
		searchOptionsPanel.setVisible(advancedView);
		reductionOptionsPanel.setVisible(advancedView);
		if(getReductionOption() != ReductionOption.VerifyPN)
			saveUppaalXMLButton.setVisible(advancedView);
		openComposedNetButton.setVisible(advancedView);
		
		if(advancedView){
			advancedButton.setText("Simple view");
			advancedButton.setToolTipText(TOOL_TIP_SIMPLE_VIEW_BUTTON);
		} else {
			advancedButton.setText("Advanced view");
			advancedButton.setToolTipText(TOOL_TIP_ADVANCED_VIEW_BUTTON);
		}

		guiDialog.pack();
		guiDialog.setLocation(location);		
	}

	private void initBoundednessCheckPanel() {

		// Number of extra tokens field
		boundednessCheckPanel = new JPanel();
		boundednessCheckPanel.setBorder(BorderFactory.createTitledBorder("Boundedness Options"));
		boundednessCheckPanel.setLayout(new BoxLayout(boundednessCheckPanel, BoxLayout.X_AXIS));
		boundednessCheckPanel.add(new JLabel(" Number of extra tokens:  "));

		numberOfExtraTokensInNet = new CustomJSpinner(4, 0, Integer.MAX_VALUE);	
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(65, 30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(65, 30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(65, 30));
		numberOfExtraTokensInNet.setToolTipText(TOOL_TIP_NUMBEROFEXTRATOKENSINNET);
		boundednessCheckPanel.add(numberOfExtraTokensInNet);

		boundednessCheckPanel.add(new JLabel("  "));

		// Boundedness button
		kbounded = new JButton("Check boundedness");
		kbounded.setToolTipText(TOOL_TIP_KBOUNDED);
		kbounded.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Verifier.analyzeKBound(tapnNetwork, getCapacity(), numberOfExtraTokensInNet);
			}

		});
		boundednessCheckPanel.add(kbounded);

		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		uppaalOptionsPanel.add(boundednessCheckPanel, gridBagConstraints);
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
		queryField.setToolTipText(TOOL_TIP_QUERY_FIELD); 

		// Put the text pane in a scroll pane.
		JScrollPane queryScrollPane = new JScrollPane(queryField);
		queryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension(880, 80);
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
					}else if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT){
						e.consume();
						int position = queryField.getSelectionEnd();
						if(e.getKeyCode() == KeyEvent.VK_LEFT){
							position = queryField.getSelectionStart();
						}
						changeToEditMode();
						queryField.setCaretPosition(position);
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

	private void initQuantificationPanel(){
		// Instantiate panel
		quantificationPanel = new JPanel(new GridBagLayout());
		quantificationPanel.setBorder(BorderFactory.createTitledBorder("Quantification"));
		quantificationButtonGroup = new ButtonGroup();
		Dimension d = new Dimension(150, 100);
		quantificationPanel.setPreferredSize(d);

		// Instantiate buttons
		existsDiamond = new JButton("EF");
		existsBox = new JButton("EG");
		forAllDiamond = new JButton("AF");
		forAllBox = new JButton("AG");
		existsUntil = new JButton("EU");
		existsNext = new JButton("EX");
		forAllUntil = new JButton("AU");
		forAllNext = new JButton("AX");

		// Add tool-tips
		existsDiamond.setToolTipText(TOOL_TIP_EXISTS_DIAMOND);
		existsBox.setToolTipText(TOOL_TIP_EXISTS_BOX);
		forAllDiamond.setToolTipText(TOOL_TIP_FORALL_DIAMOND);
		forAllBox.setToolTipText(TOOL_TIP_FORALL_BOX);
		existsUntil.setToolTipText(TOOL_TIP_EXISTS_UNTIL);
		existsNext.setToolTipText(TOOL_TIP_EXISTS_NEXT);
		forAllUntil.setToolTipText(TOOL_TIP_FORALL_UNTIL);
		forAllNext.setToolTipText(TOOL_TIP_FORALL_NEXT);

		// Add buttons to panel
		quantificationButtonGroup.add(existsDiamond);
		quantificationButtonGroup.add(existsBox);
		quantificationButtonGroup.add(forAllDiamond);
		quantificationButtonGroup.add(forAllBox);
		quantificationButtonGroup.add(existsUntil);
		quantificationButtonGroup.add(existsNext);
		quantificationButtonGroup.add(forAllUntil);
		quantificationButtonGroup.add(forAllNext);

		// Place buttons in GUI
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;

		// First column of buttons
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,5,0);
		quantificationPanel.add(existsDiamond, gbc);
		gbc.gridy = 1;
		quantificationPanel.add(existsBox, gbc);
		gbc.gridy = 2;
		quantificationPanel.add(existsUntil, gbc);
		gbc.gridy = 3;
		quantificationPanel.add(existsNext, gbc);

		// Second column of buttons
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,5,0);
		quantificationPanel.add(forAllDiamond, gbc);
		gbc.gridy = 1;
		quantificationPanel.add(forAllBox, gbc);
		gbc.gridy = 2;
		quantificationPanel.add(forAllUntil, gbc);
		gbc.gridy = 3;
		quantificationPanel.add(forAllNext, gbc);

		// Add quantification panel to query panel
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(quantificationPanel, gbc);

		// Action Listeners
		existsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLEGNode((TCTLAbstractStateProperty) currentSelection.getObject());
                } else {
                    property = new TCTLEGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
				addPropertyToQuery(property);
			}
		});

		existsDiamond.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLEFNode((TCTLAbstractStateProperty)currentSelection.getObject());
                } else {
                    property = new TCTLEFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
			}
		});

		forAllBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLAGNode((TCTLAbstractStateProperty)currentSelection.getObject());
                } else{
                    property = new TCTLAGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
			}
		});

		forAllDiamond.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLAFNode((TCTLAbstractStateProperty)currentSelection.getObject());
                } else {
                    property = new TCTLAFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
			}
		});

		existsNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLEXNode((TCTLAbstractStateProperty)currentSelection.getObject());
                } else {
                    property = new TCTLEXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
			}
		});

		existsUntil.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty){
                    property = new TCTLEUNode((TCTLAbstractStateProperty)currentSelection.getObject(),
                            new TCTLStatePlaceHolder());
                } else {
                    property = new TCTLEUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                            getSpecificChildOfProperty(2, currentSelection.getObject()));
                }
				addPropertyToQuery(property);
			}
		});

		forAllNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLAXNode((TCTLAbstractStateProperty)currentSelection.getObject());
                } else {
                    property = new TCTLAXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
				addPropertyToQuery(property);
			}
		});

		forAllUntil.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLAbstractPathProperty property;
                if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLAUNode((TCTLAbstractStateProperty)currentSelection.getObject(),
                            new TCTLStatePlaceHolder());
                } else{
                    property = new TCTLAUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                            getSpecificChildOfProperty(2, currentSelection.getObject()));
                }
				addPropertyToQuery(property);
			}
		});
	}

	private void addPropertyToQuery(TCTLAbstractPathProperty property){
		if (currentSelection.getObject() instanceof TCTLAbstractStateProperty){
			addPropertyToQuery(ConvertToStateProperty(property));
			return;
		}
		
		UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
		newProperty = newProperty.replace(currentSelection.getObject(),	property);
		updateSelection(property);
		undoSupport.postEdit(edit);
		queryChanged();
	}
	
	private void addPropertyToQuery(TCTLAbstractStateProperty property){
		if (currentSelection.getObject() instanceof TCTLAbstractPathProperty){
			addPropertyToQuery(ConvertToPathProperty(property));
			return;
		}
		
		UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
		newProperty = newProperty.replace(currentSelection.getObject(),	property);
		updateSelection(property);
		undoSupport.postEdit(edit);
		queryChanged();
	}

	private void initLogicPanel() {
		logicButtonPanel = new JPanel(new GridBagLayout());
		logicButtonPanel.setBorder(BorderFactory.createTitledBorder("Logic"));
		Dimension d = new Dimension(100, 100);
		logicButtonPanel.setPreferredSize(d);

		logicButtonGroup = new ButtonGroup();
		conjunctionButton = new JButton("and");
		disjunctionButton = new JButton("or");
		negationButton = new JButton("not");

		//Add tool tips
		conjunctionButton.setToolTipText(TOOL_TIP_CONJUNCTIONBUTTON);
		disjunctionButton.setToolTipText(TOOL_TIP_DISJUNCTIONBUTTON);
		negationButton.setToolTipText(TOOL_TIP_NEGATIONBUTTON);

		logicButtonGroup.add(conjunctionButton);
		logicButtonGroup.add(disjunctionButton);
		logicButtonGroup.add(negationButton);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 5, 0);
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
					TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty) currentSelection.getObject();
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
				} else if (currentSelection.getObject() instanceof TCTLStateToPathConverter){
					TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();

					TCTLAbstractStateProperty prop = ((TCTLStateToPathConverter)currentSelection.getObject()).getProperty();

					if (prop instanceof TCTLAndListNode){
						andListNode = new TCTLAndListNode((TCTLAndListNode) prop);
						andListNode.addConjunct(new TCTLStatePlaceHolder());
					} else if (prop instanceof TCTLOrListNode){
						andListNode = new TCTLAndListNode(((TCTLOrListNode)prop).getProperties());
					} else {
						andListNode = new TCTLAndListNode(getStateProperty(prop), ph);
					}

					TCTLAbstractPathProperty property = new TCTLStateToPathConverter(andListNode);
					UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
					newProperty = newProperty.replace(currentSelection.getObject(),	property);
					updateSelection(property);
					undoSupport.postEdit(edit);
				} else if(currentSelection.getObject() instanceof  TCTLAbstractPathProperty){
					TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
					andListNode = new TCTLAndListNode(getStateProperty(
							new TCTLPathToStateConverter((TCTLAbstractPathProperty)currentSelection.getObject())), ph);

					TCTLAbstractPathProperty property = new TCTLStateToPathConverter(andListNode);
					UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
					newProperty = newProperty.replace(currentSelection.getObject(),	property);
					updateSelection(property);
					undoSupport.postEdit(edit);
				}
				queryChanged();
			}

		});

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
				} else if (currentSelection.getObject() instanceof TCTLStateToPathConverter){
                    TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();

                    TCTLAbstractStateProperty prop = ((TCTLStateToPathConverter)currentSelection.getObject()).getProperty();

                    if (prop instanceof TCTLOrListNode){
                        orListNode = new TCTLOrListNode((TCTLOrListNode) prop);
                        orListNode.addDisjunct(new TCTLStatePlaceHolder());
					} else if (prop instanceof TCTLAndListNode){
						orListNode = new TCTLOrListNode(((TCTLAndListNode)prop).getProperties());
					} else {
						orListNode = new TCTLOrListNode(getStateProperty(prop), ph);
					}

                    TCTLAbstractPathProperty property = new TCTLStateToPathConverter(orListNode);
                    UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
                    newProperty = newProperty.replace(currentSelection.getObject(),	property);
                    updateSelection(property);
                    undoSupport.postEdit(edit);
				} else if(currentSelection.getObject() instanceof  TCTLAbstractPathProperty){
					TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
					orListNode = new TCTLOrListNode(getStateProperty(
							new TCTLPathToStateConverter((TCTLAbstractPathProperty)currentSelection.getObject())), ph);

					TCTLAbstractPathProperty property = new TCTLStateToPathConverter(orListNode);
					UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
					newProperty = newProperty.replace(currentSelection.getObject(),	property);
					updateSelection(property);
					undoSupport.postEdit(edit);
				}
				queryChanged();
			}

		});

		negationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure root is state property
				TCTLAbstractStateProperty root;
				if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
					root = ConvertToStateProperty(getPathProperty(currentSelection.getObject()));
				} else{
					root = getStateProperty(currentSelection.getObject());
				}

				TCTLNotNode property = new TCTLNotNode(root);
				addPropertyToQuery(property);
			}
		});
	}
	
	private void initPredicationConstructionPanel() {
		predicatePanel = new JPanel(new GridBagLayout());
		predicatePanel.setBorder(BorderFactory.createTitledBorder("Predicates"));
		//predicatePanel.setPreferredSize(new Dimension(300, 150));

		placesTransitionsBox = new JComboBox();
		Dimension d = new Dimension(121, 27);
		placesTransitionsBox.setMaximumSize(d);
		placesTransitionsBox.setPreferredSize(d);


		Vector<Object> items = new Vector<Object>(tapnNetwork.activeTemplates().size()+1);
		items.addAll(tapnNetwork.activeTemplates());
		if(tapnNetwork.numberOfSharedPlaces() > 0 || tapnNetwork.numberOfSharedTransitions() > 0) items.add(SHARED);

		templateBox = new JComboBox(new DefaultComboBoxModel(items));
		templateBox.addActionListener(new ActionListener() {
			private Object currentlySelected = null;

			public void actionPerformed(ActionEvent e) {
				if(!templateBox.getSelectedItem().equals(SHARED)){
					TimedArcPetriNet tapn = (TimedArcPetriNet) templateBox.getSelectedItem();
					if (!tapn.equals(currentlySelected)) {
						Vector<String> placeTransitionNames = new Vector<String>();
						for (TimedPlace place : tapn.places()) {
							if(!place.isShared()){
								placeTransitionNames.add(place.name());
							}
						}						
						for (TimedTransition transition : tapn.transitions()) {
							if(!transition.isShared()){
								placeTransitionNames.add(transition.name());
							}
						}
						Collections.sort(placeTransitionNames, new Comparator<String>() {
							public int compare(String o1, String o2) {
								return o1.compareToIgnoreCase(o2);
							}
						});
						placesTransitionsBox.setModel(new DefaultComboBoxModel(placeTransitionNames));

						currentlySelected = tapn;
						setEnablednessOfAddPredicateButton();
						if (userChangedAtomicPropSelection && placeTransitionNames.size() > 0)
							updateQueryOnAtomicPropositionChange();
					}
				}else{
					Vector<String> placeTransitionNames = new Vector<String>();
					for (SharedPlace place : tapnNetwork.sharedPlaces()) {
						placeTransitionNames.add(place.name());
					}
					for (SharedTransition transition : tapnNetwork.sharedTransitions()) {
						placeTransitionNames.add(transition.name());
					}
					Collections.sort(placeTransitionNames, new Comparator<String>() {
						public int compare(String o1, String o2) {
							return o1.compareToIgnoreCase(o2);
						}
					});
					placesTransitionsBox.setModel(new DefaultComboBoxModel(placeTransitionNames));

					currentlySelected = SHARED;
					setEnablednessOfAddPredicateButton();
					if (userChangedAtomicPropSelection && placeTransitionNames.size() > 0)
						updateQueryOnAtomicPropositionChange();
				}
				setEnablednessOfOperatorAndMarkingBoxes();
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
				
		JPanel templateRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
		predicatePanel.add(templateRow, gbc);
		templateBox.setPreferredSize(new Dimension(292, 27));
		templateRow.add(templateBox);
		
		JPanel placeTransitionRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
		gbc.gridy = 1;
		predicatePanel.add(placeTransitionRow, gbc);
		placeTransitionRow.add(placesTransitionsBox);
		
		String[] relationalSymbols = { "=", "!=", "<=", "<", ">=", ">" };
		relationalOperatorBox = new JComboBox(new DefaultComboBoxModel(relationalSymbols));
		relationalOperatorBox.setPreferredSize(new Dimension(80, 27));
		placeTransitionRow.add(relationalOperatorBox);

		placeMarking = new CustomJSpinner(0);
		placeMarking.setPreferredSize(new Dimension(80, 27));
		placeTransitionRow.add(placeMarking);
		
		transitionIsEnabledLabel = new JLabel(" is enabled");
		transitionIsEnabledLabel.setPreferredSize(new Dimension(165, 27));
		placeTransitionRow.add(transitionIsEnabledLabel);
		
		JPanel addPredicateRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
		gbc.gridy = 2;
		predicatePanel.add(addPredicateRow, gbc);
		addPredicateButton = new JButton("Add predicate to the query");
		addPredicateButton.setPreferredSize(new Dimension(292, 27));
		addPredicateRow.add(addPredicateButton);

		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setEnabled(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(2, 0, 2, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		predicatePanel.add(separator,gbc);

		JPanel trueFalseDeadlock = new JPanel(new FlowLayout(FlowLayout.CENTER));
		truePredicateButton = new JButton("True");
		falsePredicateButton = new JButton("False");
		deadLockPredicateButton = new JButton("Deadlock");
		truePredicateButton.setPreferredSize(new Dimension(90, 27));
		falsePredicateButton.setPreferredSize(new Dimension(90, 27));
		deadLockPredicateButton.setPreferredSize(new Dimension(103, 27));
		trueFalseDeadlock.add(truePredicateButton);
		trueFalseDeadlock.add(falsePredicateButton);
		trueFalseDeadlock.add(deadLockPredicateButton);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.CENTER;
		predicatePanel.add(trueFalseDeadlock, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(predicatePanel, gbc);

		//Add tool tips for predicate panel
		placesTransitionsBox.setToolTipText(TOOL_TIP_PLACESTRANSITIONSBOX);
		templateBox.setToolTipText(TOOL_TIP_TEMPLATEBOX);
		relationalOperatorBox.setToolTipText(TOOL_TIP_RELATIONALOPERATORBOX);
		placeMarking.setToolTipText(TOOL_TIP_PLACEMARKING);
		addPredicateButton.setToolTipText(TOOL_TIP_ADDPREDICATEBUTTON);
		truePredicateButton.setToolTipText(TOOL_TIP_TRUEPREDICATEBUTTON);
		falsePredicateButton.setToolTipText(TOOL_TIP_FALSEPREDICATEBUTTON);
		deadLockPredicateButton.setToolTipText(TOOL_TIP_DEADLOCKPREDICATEBUTTON);

		// Action listeners for predicate panel
		addPredicateButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String template = templateBox.getSelectedItem().toString();
				if(template.equals(SHARED)) template = "";
				ArrayList<TCTLAbstractStateProperty> list = new ArrayList<TCTLAbstractStateProperty>();
				
				if (transitionIsSelected()){
					addPropertyToQuery(new TCTLTransitionNode(template, (String) placesTransitionsBox.getSelectedItem()));
				} else {
					TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
						new TCTLPlaceNode(template, (String) placesTransitionsBox.getSelectedItem()), 
						(String) relationalOperatorBox.getSelectedItem(),
						new TCTLConstNode((Integer) placeMarking.getValue()));
					addPropertyToQuery(property);
				}	
			}
		});

		truePredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLTrueNode property = new TCTLTrueNode();
				addPropertyToQuery(property);
			}
		});

		falsePredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLFalseNode property = new TCTLFalseNode();
				addPropertyToQuery(property);
			}
		});

		deadLockPredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLDeadlockNode property = new TCTLDeadlockNode();
				addPropertyToQuery(property);
			}
		});

		placesTransitionsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (userChangedAtomicPropSelection) {
					updateQueryOnAtomicPropositionChange();
				}
				setEnablednessOfOperatorAndMarkingBoxes();
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
	
	private boolean transitionIsSelected(){		
		String itemName = (String)placesTransitionsBox.getSelectedItem();
                if (itemName == null) return false;
		boolean transitionSelected = false;
		boolean sharedTransitionSelected = false;
		for(TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
                    if (tapn.getTransitionByName(itemName) != null) {
                        transitionSelected = true;
                        break;
                    }
		}
		if (!transitionSelected){
                    sharedTransitionSelected = tapnNetwork.getSharedTransitionByName(itemName)!= null ? true : false;			
		}
		return transitionSelected || sharedTransitionSelected;
	}

	private void initQueryEditingPanel() {
		// Editing buttons panel
		editingButtonPanel = new JPanel(new GridBagLayout());
		editingButtonPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
		editingButtonPanel.setPreferredSize(new Dimension(260, 150));

		editingButtonsGroup = new ButtonGroup();
		deleteButton = new JButton("Delete selection");
		resetButton = new JButton("Reset query");
		undoButton = new JButton("Undo");
		redoButton = new JButton("Redo");
		editQueryButton = new JButton("Edit query");
		editQueryButton.setEnabled(true);
		
		//Add tool tips
		deleteButton.setToolTipText(TOOL_TIP_DELETEBUTTON);
		resetButton.setToolTipText(TOOL_TIP_RESETBUTTON);
		undoButton.setToolTipText(TOOL_TIP_UNDOBUTTON);
		redoButton.setToolTipText(TOOL_TIP_REDOBUTTON);
		editQueryButton.setToolTipText(TOOL_TIP_EDITQUERYBUTTON);

		editingButtonsGroup.add(deleteButton);
		editingButtonsGroup.add(resetButton);
		editingButtonsGroup.add(undoButton);
		editingButtonsGroup.add(redoButton);
		editingButtonsGroup.add(editQueryButton);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		editingButtonPanel.add(undoButton, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 10, 5, 0);
		editingButtonPanel.add(redoButton, gbc);

		gbc.insets = new Insets(0, 0, 5, 0);
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
					TCTLAbstractPathProperty newQuery = null;

					try {
						newQuery = TAPAALCTLQueryParser.parse(queryField.getText());
					} catch (Throwable ex) {
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
                                                                if(!p.isShared()){
                                                                    templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
                                                                }
							}
						}

						for(TimedPlace p : tapnNetwork.sharedPlaces()) {
							templatePlaceNames.add(new Tuple<String, String>("", p.name()));
						}

                                                FixAbbrivPlaceNames.fixAbbrivPlaceNames(templatePlaceNames, newQuery); 
						VerifyPlaceNamesVisitor placeNameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);
						VerifyPlaceNamesVisitor.Context c1 = placeNameChecker.verifyPlaceNames(newQuery);
                                                
                                                // check correct transition names are used in atomic propositions
						ArrayList<Tuple<String,String>> templateTransitionNames = new ArrayList<Tuple<String,String>>();
						for(TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
							for(TimedTransition t : tapn.transitions()) {
                                                                if(!t.isShared()){
                                                                    templateTransitionNames.add(new Tuple<String, String>(tapn.name(), t.name()));
                                                                }
							}
						}

						for(SharedTransition t : tapnNetwork.sharedTransitions()) {
							templateTransitionNames.add(new Tuple<String, String>("", t.name()));
						}

                                                FixAbbrivTransitionNames.fixAbbrivTransitionNames(templateTransitionNames, newQuery);                                                
						VerifyTransitionNamesVisitor transitionNameChecker = new VerifyTransitionNamesVisitor(templateTransitionNames);
						VerifyTransitionNamesVisitor.Context c2 = transitionNameChecker.verifyTransitionNames(newQuery);

						if (!c1.getResult() || !c2.getResult()) {
							StringBuilder s = new StringBuilder();
							s.append("The following places or transitions could not be added to the query:\n\n");

							for (String placeName : c1.getIncorrectPlaceNames()) {
								s.append(placeName);
								s.append('\n');
							}
                                                        
                                                        for (String transitionNames : c2.getIncorrectTransitionNames()) {
								s.append(transitionNames);
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
		initBoundednessCheckPanel();

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,10,5,10);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(uppaalOptionsPanel, gridBagConstraints);

	}

	private void initSearchOptionsPanel() {
		searchOptionsPanel = new JPanel(new GridBagLayout());
		searchOptionsPanel.setVisible(false);

		searchOptionsPanel.setBorder(BorderFactory.createTitledBorder("Search Strategy Options"));
		searchRadioButtonGroup = new ButtonGroup();
		breadthFirstSearch = new JRadioButton("Breadth first search    ");
		depthFirstSearch = new JRadioButton("Depth first search    ");
		randomSearch = new JRadioButton("Random search    ");
		heuristicSearch = new JRadioButton("Heuristic search    ");

		breadthFirstSearch.setToolTipText(TOOL_TIP_BREADTH_FIRST_SEARCH);
		depthFirstSearch.setToolTipText(TOOL_TIP_DEPTH_FIRST_SEARCH);
		randomSearch.setToolTipText(TOOL_TIP_RANDOM_SEARCH);
		heuristicSearch.setToolTipText(TOOL_TIP_HEURISTIC_SEARCH);

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
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(0, 5, 0, 0);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		uppaalOptionsPanel.add(searchOptionsPanel, gridBagConstraints);

	}

	private void initTraceOptionsPanel() {
		traceOptionsPanel = new JPanel(new GridBagLayout());
		traceOptionsPanel.setBorder(BorderFactory.createTitledBorder("Trace Options"));
		traceRadioButtonGroup = new ButtonGroup();
		someTraceRadioButton = new JRadioButton("Some trace");
		noTraceRadioButton = new JRadioButton("No trace");
		someTraceRadioButton.setToolTipText(TOOL_TIP_SOME_TRACE);
		noTraceRadioButton.setToolTipText(TOOL_TIP_NO_TRACE);
		traceRadioButtonGroup.add(someTraceRadioButton);
		traceRadioButtonGroup.add(noTraceRadioButton);

		someTraceRadioButton.setEnabled(false);
		noTraceRadioButton.setSelected(true);

		Enumeration<AbstractButton> buttons = traceRadioButtonGroup.getElements();

		while(buttons.hasMoreElements()){
			AbstractButton button = buttons.nextElement();
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setEnabledReductionOptions();
					setEnabledOptionsAccordingToCurrentReduction();
				}
			});
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		traceOptionsPanel.add(noTraceRadioButton, gridBagConstraints);

		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		traceOptionsPanel.add(someTraceRadioButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		uppaalOptionsPanel.add(traceOptionsPanel, gridBagConstraints);

	}
	
	private void initReductionOptionsPanel() {
		reductionOptionsPanel = new JPanel(new GridBagLayout());
		reductionOptionsPanel.setVisible(false);
		reductionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Verification Options"));
		Dimension d = new Dimension(810, 120);
		reductionOptionsPanel.setPreferredSize(d);
		reductionOption = new JComboBox<String>();
		reductionOption.setToolTipText(TOOL_TIP_REDUCTION_OPTION);

		reductionOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledOptionsAccordingToCurrentReduction();
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(new JLabel("  Verification engine:"), gbc);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(reductionOption, gbc);
		
		verifOptionsRadioButtonGroup = new ButtonGroup();

		useReduction = new JCheckBox("Apply net reductions");		
		useSiphonTrap = new JCheckBox("Use siphon-trap analysis");
		useQueryReduction = new JCheckBox("Use query reduction");
		useStubbornReduction = new JCheckBox("Use stubborn reduction");
		useReduction.setVisible(true);
		useSiphonTrap.setVisible(true);
		useQueryReduction.setVisible(true);
		useStubbornReduction.setVisible(true);
		useReduction.setSelected(true);
		useSiphonTrap.setSelected(false);
		useQueryReduction.setSelected(true);
		useStubbornReduction.setSelected(true);
		useReduction.setToolTipText(TOOL_TIP_USE_STRUCTURALREDUCTION);
		useSiphonTrap.setToolTipText(TOOL_TIP_USE_SIPHONTRAP);
		useQueryReduction.setToolTipText(TOOL_TIP_USE_QUERY_REDUCTION);
		useStubbornReduction.setToolTipText(TOOL_TIP_USE_STUBBORN_REDUCTION);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);	
		reductionOptionsPanel.add(useReduction, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(useSiphonTrap, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(useQueryReduction, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(useStubbornReduction, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 0, 10);
		add(reductionOptionsPanel, gbc);	
	}
	
	protected void setEnabledOptionsAccordingToCurrentReduction() {
		refreshQueryEditingButtons();
		refreshTraceOptions();
		updateSearchStrategies();
		refreshExportButtonText();
	}

	private void refreshExportButtonText() {
		ReductionOption reduction = getReductionOption();
		if (reduction == null || reduction == ReductionOption.VerifyPN) {saveUppaalXMLButton.setEnabled(false);}
		else {
			saveUppaalXMLButton.setText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification ? EXPORT_VERIFYTAPN_BTN_TEXT : EXPORT_UPPAAL_BTN_TEXT);
			saveUppaalXMLButton.setToolTipText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification ? TOOL_TIP_SAVE_TAPAAL_BUTTON : TOOL_TIP_SAVE_UPPAAL_BUTTON);
			saveUppaalXMLButton.setEnabled(true);
		}
	}

	private void refreshQueryEditingButtons() {
		if(currentSelection != null) {
			enableOnlyStateButtons();
			updateQueryButtonsAccordingToSelection();
		}
	}


	private void queryChanged(){
		setEnabledReductionOptions();
	}

	
	private void initButtonPanel(QueryDialogueOption option) {
		buttonPanel = new JPanel(new BorderLayout());
		if (option == QueryDialogueOption.Save) {
			saveButton = new JButton("Save");
			saveAndVerifyButton = new JButton("Save and Verify");
			cancelButton = new JButton("Cancel");
			
			openComposedNetButton = new JButton(EXPORT_COMPOSED_BTN_TEXT);
			openComposedNetButton.setVisible(false);
			
			saveUppaalXMLButton = new JButton(EXPORT_UPPAAL_BTN_TEXT);
			//Only show in advanced mode
			saveUppaalXMLButton.setVisible(false);
			
			//Add tool tips
			saveButton.setToolTipText(TOOL_TIP_SAVE_BUTTON);
			saveAndVerifyButton.setToolTipText(TOOL_TIP_SAVE_AND_VERIFY_BUTTON);
			cancelButton.setToolTipText(TOOL_TIP_CANCEL_BUTTON);
			saveUppaalXMLButton.setToolTipText(TOOL_TIP_SAVE_UPPAAL_BUTTON);
			openComposedNetButton.setToolTipText(TOOL_TIP_SAVE_COMPOSED_BUTTON);
			
			saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					// TODO make save 
					// save();
					if (checkIfSomeReductionOption()) {
						querySaved = true;
						// Now if a query is saved, the net is marked as modified
						CreateGui.getDrawingSurface().setNetChanged(true);
						exit();
					}
				}
			});
			saveAndVerifyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkIfSomeReductionOption()) {
						querySaved = true;
						// Now if a query is saved and verified, the net is marked as modified
						CreateGui.getDrawingSurface().setNetChanged(true);
						exit();
						TAPNQuery query = getQuery();

						if(query.getReductionOption() == ReductionOption.VerifyTAPN || query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification || query.getReductionOption() == ReductionOption.VerifyPN)
							Verifier.runVerifyTAPNVerification(tapnNetwork, query, null, guiModels);
						else
							Verifier.runUppaalVerification(tapnNetwork, query);
					}}
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
						FileBrowser browser = FileBrowser.constructor(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification || reduction == ReductionOption.VerifyPN ? "Verifytapn XML" : "Uppaal XML",	"xml", xmlFile);
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
						ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
						Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(CTLQueryDialog.this.tapnNetwork);

						TAPNQuery tapnQuery = getQuery();
						dk.aau.cs.model.tapn.TAPNQuery clonedQuery = new dk.aau.cs.model.tapn.TAPNQuery(tapnQuery.getProperty().copy(), tapnQuery.getCapacity());

						RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(transformedModel.value2());
                                                RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(transformedModel.value2());
						clonedQuery.getProperty().accept(placeVisitor, null);
                                                clonedQuery.getProperty().accept(transitionVisitor, null);

						if(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification) {
							VerifyTAPNExporter exporter = new VerifyTAPNExporter();
							exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile), tapnQuery);
						} else if(reduction == ReductionOption.VerifyPN){
							VerifyPNExporter exporter = new VerifyPNExporter();
							exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile), tapnQuery);
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

								if(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification || reduction == ReductionOption.VerifyPN)
									s.append(NO_VERIFYTAPN_XML_FILE_SAVED);
								else
									s.append(NO_UPPAAL_XML_FILE_SAVED);

								JOptionPane.showMessageDialog(CreateGui.getApp(), s.toString());
							}
						}
					}
				}
			});
			
			openComposedNetButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, true);
					Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(tapnNetwork);
					
					ArrayList<Template> templates = new ArrayList<Template>(1);
					querySaved = true;	//Setting this to true will make sure that new values will be used.
					templates.add(new Template(transformedModel.value1(), ((TAPNComposer) composer).getGuiModel(), new Zoomer()));
					
					// Create a constant store
					ConstantStore newConstantStore = new ConstantStore();

					
					TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(newConstantStore);
					
					network.add(transformedModel.value1());
					
					NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<pipe.dataLayer.TAPNQuery>(0), new ArrayList<Constant>(0));
			
					try {
						ByteArrayOutputStream outputStream = tapnWriter.savePNML();
						String composedName = "composed-" + CreateGui.getApp().getCurrentTabName();
						composedName = composedName.replace(".tapn", "");
						CreateGui.getApp().createNewTabFromFile(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
						exit();
					} catch (Exception e1) {
						System.console().printf(e1.getMessage());
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
			JPanel leftButtomPanel = new JPanel(new FlowLayout());
			JPanel rightButtomPanel = new JPanel(new FlowLayout());
			leftButtomPanel.add(openComposedNetButton, FlowLayout.LEFT);
			leftButtomPanel.add(saveUppaalXMLButton, FlowLayout.LEFT);
			
			
			rightButtomPanel.add(cancelButton);

			rightButtomPanel.add(saveButton);

			rightButtomPanel.add(saveAndVerifyButton);

			buttonPanel.add(leftButtomPanel, BorderLayout.LINE_START);
			buttonPanel.add(rightButtomPanel, BorderLayout.LINE_END);

		} else {
			buttonPanel.add(cancelButton);

			buttonPanel.add(saveButton);
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 10, 5, 10);
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
	
	public TCTLAbstractStateProperty ConvertToStateProperty(TCTLAbstractPathProperty p){
		if(p instanceof TCTLStateToPathConverter){
			return ((TCTLStateToPathConverter) p).getProperty();
		}
		else return new TCTLPathToStateConverter(p);
	}

	public TCTLAbstractPathProperty ConvertToPathProperty(TCTLAbstractStateProperty p){
		if(p instanceof TCTLPathToStateConverter){
			return ((TCTLPathToStateConverter) p).getProperty();
		}
		else return new TCTLStateToPathConverter(p);
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
