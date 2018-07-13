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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractButton;
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
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
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
import dk.aau.cs.TCTL.TCTLAbstractPathProperty;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.visitors.FixAbbrivPlaceNames;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
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

public class QueryDialog extends JPanel {

	private static final String NO_UPPAAL_XML_FILE_SAVED = "No Uppaal XML file saved.";
	private static final String NO_VERIFYTAPN_XML_FILE_SAVED = "No verifytapn XML file saved.";
	private static final String UNSUPPORTED_MODEL_TEXT = "The model is not supported by the chosen reduction.";
	private static final String UNSUPPPORTED_QUERY_TEXT = "The query is not supported by the chosen reduction.";
	private static final String EXPORT_UPPAAL_BTN_TEXT = "Export UPPAAL XML";
	private static final String EXPORT_VERIFYTAPN_BTN_TEXT = "Export TAPAAL XML";
	private static final String EXPORT_VERIFYPN_BTN_TEXT = "Export PN XML";
	private static final String EXPORT_COMPOSED_BTN_TEXT = "Open composed net";
	
	private static final String UPPAAL_SOME_TRACE_STRING = "Some trace       ";
	private static final String SOME_TRACE_STRING = "Some trace       ";
	private static final String FASTEST_TRACE_STRING = "Fastest trace       ";
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
	private JRadioButton fastestTraceRadioButton;
	
	// Reduction options panel
	private JPanel reductionOptionsPanel;
	private JComboBox<String> reductionOption;
	private JCheckBox symmetryReduction;
	private JCheckBox discreteInclusion;
	private JButton selectInclusionPlacesButton;
	private JCheckBox useTimeDarts;
	private JCheckBox usePTrie;
	private JCheckBox useGCD;
	private JCheckBox useOverApproximation;
	private JCheckBox useReduction;
	private JCheckBox useStubbornReduction;
	
	// Approximation options panel
	private JPanel overApproximationOptionsPanel;
	private ButtonGroup approximationRadioButtonGroup;
	private JRadioButton noApproximationEnable;
	private JRadioButton overApproximationEnable;
	private JRadioButton underApproximationEnable;
	private CustomJSpinner overApproximationDenominator;
	
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
	
	private static boolean hasForcedDisabledTimeDarts = false;
	private static boolean hasForcedDisabledStubbornReduction = false;
	private static boolean hasForcedDisabledGCD = false;
	private static boolean disableSymmetryUpdate = false;

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

	//Tool tips for logic panel
	private static final String TOOL_TIP_CONJUNCTIONBUTTON = "Expand the currently selected part of the query with a conjunction.";
	private static final String TOOL_TIP_DISJUNCTIONBUTTON = "Expand the currently selected part of the query with a disjunction.";
	private static final String TOOL_TIP_NEGATIONBUTTON = "Negate the currently selected part of the query.";

	//Tool tips for query panel
	private static final String TOOL_TIP_PLACESBOX = "Choose a place for the predicate.";
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
	private final static String TOOL_TIP_SYMMETRY_REDUCTION = "Apply automatic symmetry reduction.";
	private final static String TOOL_TIP_DISCRETE_INCLUSION = "<html>This optimization will perform a more advanced inclusion check."; 
	private final static String TOOL_TIP_SELECT_INCLUSION_PLACES = "Manually select places considered for the inclusion check.";
	private final static String TOOL_TIP_TIME_DARTS = "Use the time dart optimization";
	private final static String TOOL_TIP_PTRIE = "Use the PTrie memory optimization";
	private final static String TOOL_TIP_STUBBORN_REDUCTION = "Apply partial order reduction (only for EF and AG queries and when Time Darts are disabled).";
	private final static String TOOL_TIP_GCD = "Calculate greatest common divisor to minimize constants in the model";
	private final static String TOOL_TIP_OVERAPPROX = "Run linear over-approximation check for EF and AG queries";	// TODO: write tooltip

	//Tool tips for search options panel
	private final static String TOOL_TIP_HEURISTIC_SEARCH = "<html>Uses a heuiristic method in state space exploration.<br />" +
			"If heuristic search is not applicable, BFS is used instead.<br/>Click the button <em>Help on the query options</em> to get more info.</html>";
	private final static String TOOL_TIP_BREADTH_FIRST_SEARCH = "Explores markings in a breadth first manner.";
	private final static String TOOL_TIP_DEPTH_FIRST_SEARCH = "Explores markings in a depth first manner.";
	private final static String TOOL_TIP_RANDOM_SEARCH = "Performs a random exploration of the state space.";

	//Tool tips for trace options panel
	private final static String TOOL_TIP_FASTEST_TRACE = "Show a fastest concrete trace if applicable (verification can be slower with this trace option).";
	private final static String TOOL_TIP_SOME_TRACE = "Show a concrete trace whenever applicable.";
	private final static String TOOL_TIP_NO_TRACE = "Do not display any trace information.";

	//Tool tips for buttom panel
	private final static String TOOL_TIP_SAVE_BUTTON = "Save the query.";
	private final static String TOOL_TIP_SAVE_AND_VERIFY_BUTTON = "Save and verify the query.";
	private final static String TOOL_TIP_CANCEL_BUTTON = "Cancel the changes made in this dialog.";
	private final static String TOOL_TIP_SAVE_UPPAAL_BUTTON = "Export an xml file that can be opened in UPPAAL GUI.";
	private final static String TOOL_TIP_SAVE_COMPOSED_BUTTON = "Export an xml file of composed net and approximated net if enabled";
	private final static String TOOL_TIP_SAVE_TAPAAL_BUTTON = "Export an xml file that can be used as input for the TAPAAL engine.";
	private final static String TOOL_TIP_SAVE_PN_BUTTON = "Export an xml file that can be used as input for the untimed Petri net engine.";
	
	//Tool tips for approximation panel
	private final static String TOOL_TIP_APPROXIMATION_METHOD_NONE = "No approximation method is used.";
	private final static String TOOL_TIP_APPROXIMATION_METHOD_OVER = "Approximate by dividing all intervals with the approximation constant and enlarging the intervals.";
	private final static String TOOL_TIP_APPROXIMATION_METHOD_UNDER = "Approximate by dividing all intervals with the approximation constant and shrinking the intervals.";
	private final static String TOOL_TIP_APPROXIMATION_CONSTANT = "Choose approximation constant";
	
	public QueryDialog(EscapableDialog me, QueryDialogueOption option,
			TAPNQuery queryToCreateFrom, TimedArcPetriNetNetwork tapnNetwork, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		this.tapnNetwork = tapnNetwork;
		this.guiModels = guiModels;
		inclusionPlaces = queryToCreateFrom == null ? new InclusionPlaces() : queryToCreateFrom.inclusionPlaces();
		newProperty = queryToCreateFrom == null ? new TCTLPathPlaceHolder() : queryToCreateFrom.getProperty();
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
		boolean symmetry = getSymmetry();
		boolean timeDarts = useTimeDarts.isSelected();
		boolean pTrie = usePTrie.isSelected();
		boolean gcd = useGCD.isSelected();
		boolean overApproximation = useOverApproximation.isSelected();
		boolean reduction = useReduction.isSelected();

		TAPNQuery query = new TAPNQuery(
				name,
				capacity,
				newProperty.copy(),
				traceOption,
				searchOption,
				reductionOptionToSet,
				symmetry,
				gcd,
				timeDarts,
				pTrie,
				overApproximation,
				reduction,
				/* hashTableSizeToSet */ null,
				/* extrapolationOptionToSet */null,
				inclusionPlaces,
				overApproximationEnable.isSelected(),
				underApproximationEnable.isSelected(),
				(Integer) overApproximationDenominator.getValue()
		);

		query.setUseStubbornReduction(useStubbornReduction.isSelected());

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
		if(fastestTraceRadioButton.isSelected())
			return TraceOption.FASTEST;
		else
			return TraceOption.NONE;
	}

	private SearchOption getSearchOption() {
		if(fastestTraceRadioButton.isSelected()){
			return SearchOption.DEFAULT;
		}
			
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
//		else if (reductionOptionString.equals(name_UNTIMED))
//			return ReductionOption.VerifyPN;
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

		fastestTraceRadioButton.setEnabled(tapnNetwork.isNonStrict() && !queryHasDeadlock() && !(newProperty instanceof TCTLEGNode || newProperty instanceof TCTLAFNode));
		
		someTraceRadioButton.setEnabled(true);
		noTraceRadioButton.setEnabled(true);

		if(getTraceOption() == TraceOption.FASTEST) {
			if(fastestTraceRadioButton.isEnabled()){
				fastestTraceRadioButton.setSelected(true);
			} else {
				someTraceRadioButton.setSelected(true);
			}
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

	public boolean queryHasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(newProperty);
	}

	public static TAPNQuery showQueryDialogue(QueryDialogueOption option, TAPNQuery queryToRepresent, TimedArcPetriNetNetwork tapnNetwork, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		if(CreateGui.getCurrentTab().network().hasWeights() && !CreateGui.getCurrentTab().network().isNonStrict()){
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"No reduction option supports both strict intervals and weigthed arcs", 
					"No reduction option", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		guiDialog = new EscapableDialog(CreateGui.getApp(),	"Edit Query", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		//contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		contentPane.setLayout(new GridBagLayout());

		// 2 Add query editor
		QueryDialog queryDialogue = new QueryDialog(guiDialog, option, queryToRepresent, tapnNetwork, guiModels);
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
			if(!(node.getLeft() instanceof TCTLPlaceNode && node.getRight() instanceof TCTLConstNode)){
				return;
			}
			TCTLPlaceNode placeNode = (TCTLPlaceNode) node.getLeft();
			TCTLConstNode placeMarkingNode = (TCTLConstNode) node.getRight();
			
			// bit of a hack to prevent posting edits to the undo manager when
			// we programmatically change the selection in the atomic proposition comboboxes etc.
			// because a different atomic proposition was selected
			userChangedAtomicPropSelection = false;
			if(placeNode.getTemplate().equals(""))
				templateBox.setSelectedItem(SHARED);
			else
				templateBox.setSelectedItem(tapnNetwork.getTAPNByName(placeNode.getTemplate()));
			placesBox.setSelectedItem(placeNode.getPlace());
			relationalOperatorBox.setSelectedItem(node.getOp());
			placeMarking.setValue(placeMarkingNode.getConstant());
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
			openComposedNetButton.setEnabled(isQueryOk);
		} else {
			saveButton.setEnabled(false);
			saveAndVerifyButton.setEnabled(false);
			saveUppaalXMLButton.setEnabled(false);
			openComposedNetButton.setEnabled(false);
		}
	}

	private void setEnabledReductionOptions(){
		String reductionOptionString = getReductionOptionAsString();

		ArrayList<String> options = new ArrayList<String>();
		
		disableSymmetryUpdate = true;

		/* The untimed engine is disabled for now. It is used in the CTL query dialog
		if(!fastestTraceRadioButton.isSelected() && (getQuantificationSelection().equals("E<>") || getQuantificationSelection().equals("A[]") || getQuantificationSelection().equals("")) && tapnNetwork.isUntimed()){
			options.add(name_UNTIMED);
		}
		*/
		
		if(useTimeDarts != null){
			if(hasForcedDisabledTimeDarts){
				hasForcedDisabledTimeDarts = false;
				useTimeDarts.setSelected(true);
			}
            useTimeDarts.setEnabled(true);     
        }

        if(useStubbornReduction != null){
			if(hasForcedDisabledStubbornReduction){
				hasForcedDisabledStubbornReduction = false;
				useStubbornReduction.setSelected(true);
			}
			useStubbornReduction.setEnabled(true);
		}
		
		if(useGCD != null){
			if(hasForcedDisabledGCD){
				hasForcedDisabledGCD = false;
				useGCD.setSelected(true);
			}
            useGCD.setEnabled(true);     
        }
		
        if (fastestTraceRadioButton.isSelected()) {
        	options.add(name_DISCRETE);
        } else if (queryHasDeadlock()) {
            if (tapnNetwork.isNonStrict()) {
                options.add(name_DISCRETE);
                // disable timedarts if liveness and deadlock prop
                if((getQuantificationSelection().equals("E[]") || 
                        getQuantificationSelection().equals("A<>"))){
                    if (useTimeDarts != null) {
                    	if(useTimeDarts.isSelected()){
                    		hasForcedDisabledTimeDarts = true;
                    	}
                        useTimeDarts.setEnabled(false);
                        useTimeDarts.setSelected(false);
                    }
                }
            }
            if (getQuantificationSelection().equals("E<>") || getQuantificationSelection().equals("A[]")) {
                if (isNetDegree2 && !hasInhibitorArcs) {
                	options.add(name_COMBI);
                	if(!tapnNetwork.hasWeights() && !hasInhibitorArcs) {
                		options.addAll(Arrays.asList(name_BROADCAST, name_BROADCASTDEG2));
                	}
                }
            }
            
		} else if(tapnNetwork.hasWeights()){
			if(tapnNetwork.isNonStrict()){
				options.add(name_DISCRETE);
			}
			options.add(name_COMBI);
		} else if(tapnNetwork.hasUrgentTransitions()){
			if(tapnNetwork.isNonStrict()){
				options.add(name_DISCRETE);
			}
			options.add(name_COMBI);
		} else if (getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")) {
			if(tapnNetwork.isNonStrict()){
				options.add(name_DISCRETE);
			}
			options.add(name_COMBI);
			if(isNetDegree2 && !hasInhibitorArcs)
				options.addAll(Arrays.asList( name_BROADCAST, name_BROADCASTDEG2, name_OPTIMIZEDSTANDARD));
			else
				options.addAll(Arrays.asList(name_BROADCAST, name_BROADCASTDEG2));
		} else if(tapnNetwork.hasInhibitorArcs()) {
			options.add( name_verifyTAPN );
			if(tapnNetwork.isNonStrict()){
				options.add(name_DISCRETE);
			}					
			options.addAll(Arrays.asList(name_COMBI, name_BROADCAST, name_BROADCASTDEG2 ));
		} else {
			options.add( name_verifyTAPN);
			if(tapnNetwork.isNonStrict()){
				options.add(name_DISCRETE);
			}
			options.addAll(Arrays.asList(name_COMBI, name_OPTIMIZEDSTANDARD, name_STANDARD, name_BROADCAST, name_BROADCASTDEG2));
		}

		reductionOption.removeAllItems();

		boolean selectedOptionStillAvailable = false;	
		TraceOption trace = getTraceOption();
		for (String s : options) {
			reductionOption.addItem(s);
			if (s.equals(reductionOptionString)) {
				selectedOptionStillAvailable = true;
			}
		}

		if (selectedOptionStillAvailable) {
			reductionOption.setSelectedItem(reductionOptionString);
			if(trace == TraceOption.SOME && someTraceRadioButton.isEnabled()){
				someTraceRadioButton.setSelected(true);
			}else if(trace == TraceOption.FASTEST && fastestTraceRadioButton.isEnabled()){
				fastestTraceRadioButton.setSelected(true);
			}
		}
		
		disableSymmetryUpdate = false;
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
		
		if(fastestTraceRadioButton.isSelected()){
			breadthFirstSearch.setEnabled(false);
			depthFirstSearch.setEnabled(false);
			heuristicSearch.setEnabled(false);
			randomSearch.setEnabled(false);
			return;
		}else{
			breadthFirstSearch.setEnabled(true);
			depthFirstSearch.setEnabled(true);
			heuristicSearch.setEnabled(true);
			randomSearch.setEnabled(true);
		}

		String reductionOptionString = getReductionOptionAsString();
		if(getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")){
			breadthFirstSearch.setEnabled(false);
			if(!(reductionOptionString.equals(name_verifyTAPN) || reductionOptionString.equals(name_DISCRETE))){
				heuristicSearch.setEnabled(false);
			}
		}

		if(!currentselected.isEnabled()){
			if(heuristicSearch.isEnabled()){
				heuristicSearch.setSelected(true);
			} else {
				depthFirstSearch.setSelected(true);
			}
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
		deadLockPredicateButton.setEnabled(false);
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
		deadLockPredicateButton.setEnabled(false);
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
		deadLockPredicateButton.setEnabled(true);
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

		resetButton.setToolTipText(TOOL_TIP_RESETBUTTON);
		editQueryButton.setToolTipText(TOOL_TIP_EDITQUERYBUTTON);
		enableEditingButtons();

		queryChanged();
	}

	private void changeToEditMode() {
		setQueryFieldEditable(true);
		resetButton.setText("Parse query");
		editQueryButton.setText("Cancel");
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
		if (currentSelection != null && currentSelection.getObject() instanceof TCTLAtomicPropositionNode) {
			Object item = templateBox.getSelectedItem();
			String template = item.equals(SHARED) ? "" : item.toString();
			TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
					new TCTLPlaceNode(template, (String) placesBox.getSelectedItem()), 
					(String) relationalOperatorBox.getSelectedItem(),
					new TCTLConstNode((Integer) placeMarking.getValue()));
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
		initOverApproximationPanel();
		initButtonPanel(option);

		if(queryToCreateFrom != null)
			setupFromQuery(queryToCreateFrom);
		
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

		setupQuantificationFromQuery(queryToCreateFrom);
		setupSearchOptionsFromQuery(queryToCreateFrom);		
		setupReductionOptionsFromQuery(queryToCreateFrom);
		setupTraceOptionsFromQuery(queryToCreateFrom);
		setupApproximationOptionsFromQuery(queryToCreateFrom);
	}
	
	private void setupApproximationOptionsFromQuery(TAPNQuery queryToCreateFrom) {
		if (queryToCreateFrom.isOverApproximationEnabled())
			overApproximationEnable.setSelected(true);
		else if (queryToCreateFrom.isUnderApproximationEnabled())
			underApproximationEnable.setSelected(true);
		else
			noApproximationEnable.setSelected(true);
		
		if (queryToCreateFrom.approximationDenominator() > 0) {
			overApproximationDenominator.setValue(queryToCreateFrom.approximationDenominator());
		}
	}

	private void setupReductionOptionsFromQuery(TAPNQuery queryToCreateFrom) {
		String reduction = "";
		boolean symmetry = queryToCreateFrom.useSymmetry();

		if (queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST) {
			reduction = name_BROADCAST;
		} else if (queryToCreateFrom.getReductionOption() == ReductionOption.DEGREE2BROADCAST) {
			reduction = name_BROADCASTDEG2;
		} else if(queryToCreateFrom.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification){
			reduction = name_DISCRETE;
//		} else if(queryToCreateFrom.getReductionOption() == ReductionOption.VerifyPN){
//			reduction = name_UNTIMED;
		} else if(queryToCreateFrom.getReductionOption() == ReductionOption.COMBI){
			reduction = name_COMBI;
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

		reductionOption.addItem(reduction); 
		reductionOption.setSelectedItem(reduction);
		symmetryReduction.setSelected(symmetry);
		useTimeDarts.setSelected(queryToCreateFrom.useTimeDarts());
		usePTrie.setSelected(queryToCreateFrom.usePTrie());
		useStubbornReduction.setSelected(queryToCreateFrom.isStubbornReductionEnabled());
		useGCD.setSelected(queryToCreateFrom.useGCD());
		useOverApproximation.setSelected(queryToCreateFrom.useOverApproximation());
		useReduction.setSelected(queryToCreateFrom.useReduction());
		discreteInclusion.setSelected(queryToCreateFrom.discreteInclusion());
		if(queryToCreateFrom.discreteInclusion()) selectInclusionPlacesButton.setEnabled(true);
	}

	private void setupTraceOptionsFromQuery(TAPNQuery queryToCreateFrom) {
		if (queryToCreateFrom.getTraceOption() == TraceOption.SOME) {
			someTraceRadioButton.setSelected(true);
		} else if (queryToCreateFrom.getTraceOption() == TraceOption.FASTEST) {
			fastestTraceRadioButton.setSelected(true);
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
			noApproximationEnable.setSelected(true);
		} else if (queryToCreateFrom.getProperty() instanceof TCTLAFNode) {
			forAllDiamond.setSelected(true);
			noApproximationEnable.setSelected(true);
		} else if (queryToCreateFrom.getProperty() instanceof TCTLAGNode) {
			forAllBox.setSelected(true);
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
				buffer.append("<li>Heuristic Search<br/> If available, the search is guided according to the query so that the most likely places where the query is satisfied are visited first. If discrete inclusion optimization is not enabled or the heuristic search is not available, this strategy performs a breadth first search. ");
				buffer.append("If discrete inclusion is enabled, the search attempts to maximize the number of tokens in places where the engine checks for discrete inclusion.</li>");
				buffer.append("<li>Breadth First Search<br/>Explores markings in a breadth first manner.</li>");
				buffer.append("<li>Depth First Search<br/>Explores markings in a depth first manner.</li>");
				buffer.append("<li>Random Search<br/>Performs a random exploration of the state space.</li>");
				buffer.append("</ul>");
				buffer.append("<br/>");
				buffer.append("<b>Verification Options</b><br/>");
				buffer.append("TAPAAL supports verification via its own included engines verifytapn and verifydtapn or via a translation to networks of timed automata and then using the tool UPPAAL (requires a separate installation). If you work with an untimed net, we recommend that you use the CTL query creation dialog and use the untimed verifypn engine.");
				buffer.append("The TAPAAL engine verifytapn supports also the discrete inclusion optimization. ");
				buffer.append("On some models this technique gives a considerable speedup. ");
				buffer.append("The user selected set of places that are considered for the discrete inclusion can further finetune the performance of the engine. Try to include places where you expect to see many tokens during the execution. ");
				buffer.append("The discrete verification engine verifydtapn performs a point-wise exploration of the state space but can be used only for models that do not contain strict intervals as in this situation it is guaranteed to give the same answers as the continuous time engine verifytapn. This discrete engine has options to handle delays in semi-symbolic way (time darts) recommended for models with larger constants and it has a memory optimization option feature (PTrie) that preserves lots of memory at the expense of a slightly longer verification time.");
				buffer.append("The different UPPAAL verification methods perform reductions to networks of timed automata. The broadcast reductions supports ");
				buffer.append("all query types, while standard and optimized standard support only EF and AG queries but can be sometimes faster.");
				buffer.append("<br/>");
				buffer.append("<b>Approximation Options</b><br/>");
				buffer.append("TAPAAL allows to approximate the time intervals on edges by deviding them by the given approximation constant and either enlarging the resulting intervals (over-approximation) or shrinking them (under-approximation). The larger the constant is, the faster is the verification but the more often the user can get an inconclusive answer.");
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
		saveUppaalXMLButton.setVisible(advancedView);
		openComposedNetButton.setVisible(advancedView);
		overApproximationOptionsPanel.setVisible(advancedView);
		
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

	private void initQuantificationPanel() {
		quantificationPanel = new JPanel(new GridBagLayout());
		quantificationPanel.setBorder(BorderFactory.createTitledBorder("Quantification"));
		quantificationRadioButtonGroup = new ButtonGroup();
		approximationRadioButtonGroup = new ButtonGroup();

		existsDiamond = new JRadioButton("(EF) There exists some reachable marking that satisifies:");
		existsBox = new JRadioButton("(EG) There exists a trace on which every marking satisfies:");
		forAllDiamond = new JRadioButton("(AF) On all traces there is eventually a marking that satisfies:");
		forAllBox = new JRadioButton("(AG) All reachable markings satisfy:");

		//Add tool tips 
		existsDiamond.setToolTipText(TOOL_TIP_EXISTS_DIAMOND);
		existsBox.setToolTipText(TOOL_TIP_EXISTS_BOX);
		forAllDiamond.setToolTipText(TOOL_TIP_FORALL_DIAMOND);
		forAllBox.setToolTipText(TOOL_TIP_FORALL_BOX);

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
		Dimension d = new Dimension(125, 27);
		placesBox.setMaximumSize(d);
		placesBox.setPreferredSize(d);


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

		placeMarking = new CustomJSpinner(0);
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
		gbc.insets = new Insets(0, -38, 0,0);
		predicatePanel.add(truePredicateButton, gbc);

		falsePredicateButton = new JButton("False");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.insets = new Insets(0, -88, 0,0);
		predicatePanel.add(falsePredicateButton, gbc);

		deadLockPredicateButton = new JButton("Deadlock");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(0, -35, 0,0);
		predicatePanel.add(deadLockPredicateButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(predicatePanel, gbc);

		//Add tool tips for predicate panel
		placesBox.setToolTipText(TOOL_TIP_PLACESBOX);
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
				TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
						new TCTLPlaceNode(template, (String) placesBox.getSelectedItem()), 
						(String) relationalOperatorBox.getSelectedItem(),
						new TCTLConstNode((Integer) placeMarking.getValue()));
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

		deadLockPredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLDeadlockNode deadLockNode = new TCTLDeadlockNode();
				UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), deadLockNode);
				newProperty = newProperty.replace(currentSelection.getObject(), deadLockNode);
				updateSelection(deadLockNode);
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
					TCTLAbstractProperty newQuery = null;

					try {
						newQuery = TAPAALQueryParser.parse(queryField.getText());
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
								templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
							}
						}

						for(TimedPlace p : tapnNetwork.sharedPlaces()) {
							templatePlaceNames.add(new Tuple<String, String>("", p.name()));
						}

                                                FixAbbrivPlaceNames.fixAbbrivPlaceNames(templatePlaceNames, newQuery);
                                                
						VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);

						VerifyPlaceNamesVisitor.Context c = nameChecker.verifyPlaceNames(newQuery);

						if (!c.getResult()) {
							StringBuilder s = new StringBuilder();
							s.append("The following places were used in the query, but are not present in your model:\n\n");

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
		someTraceRadioButton = new JRadioButton(UPPAAL_SOME_TRACE_STRING);
		noTraceRadioButton = new JRadioButton("No trace");
		fastestTraceRadioButton = new JRadioButton("Fastest trace");
		someTraceRadioButton.setToolTipText(TOOL_TIP_SOME_TRACE);
		noTraceRadioButton.setToolTipText(TOOL_TIP_NO_TRACE);
		fastestTraceRadioButton.setToolTipText(TOOL_TIP_FASTEST_TRACE);
		traceRadioButtonGroup.add(fastestTraceRadioButton);
		traceRadioButtonGroup.add(someTraceRadioButton);
		traceRadioButtonGroup.add(noTraceRadioButton);

		fastestTraceRadioButton.setEnabled(false);
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
		
		gridBagConstraints.gridy = 2;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		traceOptionsPanel.add(fastestTraceRadioButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		uppaalOptionsPanel.add(traceOptionsPanel, gridBagConstraints);

	}
	
	
	
	
	
	private void initOverApproximationPanel() {
		overApproximationOptionsPanel = new JPanel(new GridBagLayout());
		overApproximationOptionsPanel.setVisible(false);
		overApproximationOptionsPanel.setBorder(BorderFactory.createTitledBorder("Approximation Options"));
		approximationRadioButtonGroup = new ButtonGroup();
		
		noApproximationEnable = new JRadioButton("Exact analysis");
		noApproximationEnable.setVisible(true);
		noApproximationEnable.setSelected(true);
		noApproximationEnable.setToolTipText(TOOL_TIP_APPROXIMATION_METHOD_NONE);
		
		overApproximationEnable = new JRadioButton("Over-approximation");
		overApproximationEnable.setVisible(true);
		overApproximationEnable.setToolTipText(TOOL_TIP_APPROXIMATION_METHOD_OVER);
		
		underApproximationEnable = new JRadioButton("Under-approximation");
		underApproximationEnable.setVisible(true);
		underApproximationEnable.setToolTipText(TOOL_TIP_APPROXIMATION_METHOD_UNDER);

		approximationRadioButtonGroup.add(noApproximationEnable);
		approximationRadioButtonGroup.add(overApproximationEnable);
		approximationRadioButtonGroup.add(underApproximationEnable);
		
		Enumeration<AbstractButton> buttons = approximationRadioButtonGroup.getElements(); 
		
		while(buttons.hasMoreElements()){
			AbstractButton button = buttons.nextElement(); 
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setEnabledOptionsAccordingToCurrentReduction();
				}
			});
		}
		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		
		JLabel approximationDenominatorLabel = new JLabel("Approximation constant: ");	
		
		overApproximationDenominator = new CustomJSpinner(2, 2, Integer.MAX_VALUE);	
		overApproximationDenominator.setMaximumSize(new Dimension(65, 30));
		overApproximationDenominator.setMinimumSize(new Dimension(65, 30));
		overApproximationDenominator.setPreferredSize(new Dimension(65, 30));
		overApproximationDenominator.setToolTipText(TOOL_TIP_APPROXIMATION_CONSTANT);
		
		overApproximationOptionsPanel.add(noApproximationEnable, gridBagConstraints);
		overApproximationOptionsPanel.add(overApproximationEnable, gridBagConstraints);
		overApproximationOptionsPanel.add(underApproximationEnable, gridBagConstraints);
		overApproximationOptionsPanel.add(approximationDenominatorLabel, gridBagConstraints);
		overApproximationOptionsPanel.add(overApproximationDenominator);
	
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,10,5,10);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		
		add(overApproximationOptionsPanel, gridBagConstraints);
	}

	private void initReductionOptionsPanel() {
		reductionOptionsPanel = new JPanel(new GridBagLayout());
		reductionOptionsPanel.setVisible(false);
		reductionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Verification Options"));
		Dimension d = new Dimension(898, 100);
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

		symmetryReduction = new JCheckBox("Use symmetry reduction");
		symmetryReduction.setSelected(true);
		symmetryReduction.setToolTipText(TOOL_TIP_SYMMETRY_REDUCTION);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(symmetryReduction, gbc);

		discreteInclusion = new JCheckBox("Use discrete inclusion");
		discreteInclusion.setVisible(true);
		discreteInclusion.setToolTipText(TOOL_TIP_DISCRETE_INCLUSION);
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
		selectInclusionPlacesButton.setToolTipText(TOOL_TIP_SELECT_INCLUSION_PLACES);
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

		useTimeDarts = new JCheckBox("Use Time Darts");
		useTimeDarts.setSelected(false);
		useTimeDarts.setToolTipText(TOOL_TIP_TIME_DARTS);
		useTimeDarts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledOptionsAccordingToCurrentReduction();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(useTimeDarts, gbc);
		
		useGCD = new JCheckBox("Use GCD");
		useGCD.setSelected(true);
		useGCD.setToolTipText(TOOL_TIP_GCD);

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(useGCD, gbc);
		
		usePTrie = new JCheckBox("Use PTrie");
		usePTrie.setSelected(true);
		usePTrie.setToolTipText(TOOL_TIP_PTRIE);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);	
		reductionOptionsPanel.add(selectInclusionPlacesButton, gbc);

		useReduction = new JCheckBox("Apply net reductions");
		useReduction.setSelected(true);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);	
		reductionOptionsPanel.add(useReduction, gbc);
		

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(usePTrie, gbc);

		useStubbornReduction = new JCheckBox("Use stubborn reduction");
		useStubbornReduction.setSelected(true);
		useStubbornReduction.setToolTipText(TOOL_TIP_STUBBORN_REDUCTION);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(useStubbornReduction, gbc);

		useOverApproximation = new JCheckBox("Use untimed state-equations check");
		useOverApproximation.setSelected(true);
		useOverApproximation.setToolTipText(TOOL_TIP_OVERAPPROX);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,5,0,5);
		reductionOptionsPanel.add(useOverApproximation, gbc);

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
		refreshSymmetryReduction();
		refreshStubbornReduction();
		refreshDiscreteOptions();
		refreshDiscreteInclusion();
		refreshOverApproximationOption();
		updateSearchStrategies();
		refreshExportButtonText();
	}

	private void refreshDiscreteInclusion() {
		ReductionOption reduction = getReductionOption();
		if(reduction == null){
			discreteInclusion.setVisible(false);
			selectInclusionPlacesButton.setVisible(false);
		}
		else if(reduction.equals(ReductionOption.VerifyTAPN)){
			discreteInclusion.setVisible(true);
			selectInclusionPlacesButton.setVisible(true);
			//queryChanged(); // This ensures the checkbox is disabled if query is not upward closed
		}else{
			discreteInclusion.setVisible(false);
			selectInclusionPlacesButton.setVisible(false);
		}
	}

	private void refreshExportButtonText() {
		ReductionOption reduction = getReductionOption();
		if (reduction == null) {saveUppaalXMLButton.setEnabled(false);}
		else {
			saveUppaalXMLButton.setText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification ? EXPORT_VERIFYTAPN_BTN_TEXT : reduction == ReductionOption.VerifyPN ? EXPORT_VERIFYPN_BTN_TEXT : EXPORT_UPPAAL_BTN_TEXT);
			saveUppaalXMLButton.setToolTipText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification ? TOOL_TIP_SAVE_TAPAAL_BUTTON : reduction == ReductionOption.VerifyPN ? TOOL_TIP_SAVE_PN_BUTTON : TOOL_TIP_SAVE_UPPAAL_BUTTON);
			saveUppaalXMLButton.setEnabled(true);
		}
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
	}

	private void refreshSymmetryReduction() {
		if(disableSymmetryUpdate){
			return;
		}
		else if(reductionOption.getSelectedItem() == null){
			symmetryReduction.setVisible(false);
		} 
		else if(((String)reductionOption.getSelectedItem()).equals(name_DISCRETE) || ((String)reductionOption.getSelectedItem()).equals(name_UNTIMED)) {
			symmetryReduction.setVisible(true);
			symmetryReduction.setSelected(true);
			symmetryReduction.setEnabled(false);
		}
		else if((((String)reductionOption.getSelectedItem()).equals(name_COMBI) ||
				((String)reductionOption.getSelectedItem()).equals(name_OPTIMIZEDSTANDARD) ||
				((String)reductionOption.getSelectedItem()).equals(name_STANDARD) ||
				((String)reductionOption.getSelectedItem()).equals(name_BROADCAST) ||
				((String)reductionOption.getSelectedItem()).equals(name_BROADCASTDEG2)) &&
				(!noApproximationEnable.isSelected() ||
				someTraceRadioButton.isSelected()) 
				){
			symmetryReduction.setVisible(true);
			symmetryReduction.setSelected(false);
			symmetryReduction.setEnabled(false);
		} else {
			symmetryReduction.setVisible(true);
			if(!symmetryReduction.isEnabled())	symmetryReduction.setSelected(true);
			symmetryReduction.setEnabled(true);
		}
	}

	private void refreshOverApproximationOption() {
		if(queryHasDeadlock() || getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")){
			useOverApproximation.setSelected(false);
			useOverApproximation.setEnabled(false);
		}else{
			if(!useOverApproximation.isEnabled()){
				useOverApproximation.setSelected(true);
			}
			useOverApproximation.setEnabled(true);
		}
		
		if(fastestTraceRadioButton.isSelected()){
			noApproximationEnable.setEnabled(true);
			noApproximationEnable.setSelected(true);
			overApproximationEnable.setEnabled(false);
			underApproximationEnable.setEnabled(false);
			overApproximationDenominator.setEnabled(false);
		}
		else{
			noApproximationEnable.setEnabled(true);
			overApproximationEnable.setEnabled(true);
			underApproximationEnable.setEnabled(true);
			overApproximationDenominator.setEnabled(true);
		}
	}

	private void refreshDiscreteOptions(){
		useReduction.setVisible(false);
		
		if(reductionOption.getSelectedItem() == null){
			useGCD.setVisible(false);
			usePTrie.setVisible(false);
			useStubbornReduction.setVisible(false);
			useTimeDarts.setVisible(false);
		} 
		else if(((String)reductionOption.getSelectedItem()).equals(name_DISCRETE)) {
			useGCD.setVisible(true);
			usePTrie.setVisible(true);
			useStubbornReduction.setVisible(true);
			useTimeDarts.setVisible(true);

			if(tapnNetwork.hasUrgentTransitions() || fastestTraceRadioButton.isSelected()){
				hasForcedDisabledTimeDarts = useTimeDarts.isSelected();
				useTimeDarts.setSelected(false);
				useTimeDarts.setEnabled(false);
			}

			// Disable GCD calculation for EG/AF or deadlock queries
			if(queryHasDeadlock() || getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")){
				if(useGCD.isSelected())	hasForcedDisabledGCD = true;
				useGCD.setSelected(false);
				useGCD.setEnabled(false);
			}

			// Disable time darts for EG/AF with deadlock
			if(queryHasDeadlock() && (getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>"))){
				hasForcedDisabledTimeDarts = useTimeDarts.isSelected();
				useTimeDarts.setSelected(false);
				useTimeDarts.setEnabled(false);
			}

			// Disable stubborn reduction for EG/AF queries
			if(getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")){
				if(useStubbornReduction.isSelected())	hasForcedDisabledStubbornReduction = true;
				useStubbornReduction.setSelected(false);
				useStubbornReduction.setEnabled(false);
			}
		} else {
			useGCD.setVisible(false);
			usePTrie.setVisible(false);
			useStubbornReduction.setVisible(false);
			useTimeDarts.setVisible(false);

//			if(((String)reductionOption.getSelectedItem()).equals(name_UNTIMED)){
//				useReduction.setVisible(true);
//			}
		}
	}

	private void refreshStubbornReduction(){
		if(useTimeDarts.isSelected()){
			useStubbornReduction.setSelected(false);
			useStubbornReduction.setEnabled(false);
		} else {
			useStubbornReduction.setEnabled(true);
		}
	}


	private void queryChanged(){
		setEnabledReductionOptions();
		refreshOverApproximationOption();
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
						Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(QueryDialog.this.tapnNetwork);
						
						if (overApproximationEnable.isSelected())
						{
							OverApproximation overaprx = new OverApproximation();
							overaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator());
						}
						else if (underApproximationEnable.isSelected())
						{
							UnderApproximation underaprx = new UnderApproximation();
							underaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator());
						}						

						TAPNQuery tapnQuery = getQuery();
						dk.aau.cs.model.tapn.TAPNQuery clonedQuery = new dk.aau.cs.model.tapn.TAPNQuery(tapnQuery.getProperty().copy(), tapnQuery.getCapacity());

						RenameAllPlacesVisitor visitor = new RenameAllPlacesVisitor(transformedModel.value2());
						clonedQuery.getProperty().accept(visitor, null);

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
					if (overApproximationEnable.isSelected())
					{
						OverApproximation overaprx = new OverApproximation();
						overaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator());
					}
					else if (underApproximationEnable.isSelected())
					{
						UnderApproximation underaprx = new UnderApproximation();
						underaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator(), ((TAPNComposer) composer).getGuiModel());
					}
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
