package pipe.gui.widgets;

import java.awt.*;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
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

import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.CTLParsing.TAPAALCTLQueryParser;
import dk.aau.cs.TCTL.LTLParsing.TAPAALLTLQueryParser;
import dk.aau.cs.TCTL.visitors.*;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.*;
import net.tapaal.swinghelpers.CustomJSpinner;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetWriter;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.*;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
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
	private static final String EXPORT_COMPOSED_BTN_TEXT = "Merge net components";
    private static final String OPEN_REDUCED_BTN_TEXT = "Open reduced net";

	private static final String UPPAAL_SOME_TRACE_STRING = "Some trace       ";
	private static final String SOME_TRACE_STRING = "Some trace       ";
	private static final String FASTEST_TRACE_STRING = "Fastest trace       ";
	private static final String SHARED = "Shared";

	public enum QueryDialogueOption {
		VerifyNow, Save, Export
	}

	private boolean querySaved = false;

	private JRootPane rootPane;
	private static EscapableDialog guiDialog;

	// Query Name Panel;
	private JPanel namePanel;
	private JComboBox queryType;
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
    private JButton globallyButton;
    private JButton finallyButton;
    private JButton nextButton;
    private JButton untilButton;
    private JButton aButton;
    private JButton eButton;

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
	private JComboBox<String> placeTransitionBox;
	private JComboBox<String> relationalOperatorBox;
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
    private JCheckBox useSiphonTrap;
    private JCheckBox useQueryReduction;
    private JCheckBox useReduction;
	private JCheckBox useStubbornReduction;
    private JCheckBox useTraceRefinement;
    private JCheckBox useTarjan;

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
	private JButton mergeNetComponentsButton;
	private JButton openReducedNetButton;

	// Private Members
	private StringPosition currentSelection = null;

	private final TimedArcPetriNetNetwork tapnNetwork;
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels;
	private QueryConstructionUndoManager undoManager;
	private UndoableEditSupport undoSupport;
	private boolean isNetDegree2;
	private int highestNetDegree;
	private boolean hasInhibitorArcs;
	private InclusionPlaces inclusionPlaces;
	private TabContent.TAPNLens lens;

	private static final String name_verifyTAPN = "TAPAAL: Continous Engine (verifytapn)";
	private static final String name_COMBI = "UPPAAL: Optimized Broadcast Reduction";
	private static final String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private static final String name_STANDARD = "UPPAAL: Standard Reduction";
	private static final String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_DISCRETE = "TAPAAL: Discrete Engine (verifydtapn)";
	private static final String name_UNTIMED = "TAPAAL: Untimed Engine (verifypn)";
	private boolean userChangedAtomicPropSelection = true;

	//In order: name of engine, support fastest trace, support deadlock with net degree 2 and (EF or AG), support deadlock with EG or AF, support deadlock with inhibitor arcs
    //support weights, support inhibitor arcs, support urgent transitions, support EG or AF, support strict nets, support timed nets/time intervals, support deadlock with net degree > 2
	private final static EngineSupportOptions verifyTAPNOptions= new VerifyTAPNEngineOptions();
    private final static EngineSupportOptions UPPAALCombiOptions= new UPPAALCombiOptions();
    private final static EngineSupportOptions UPPAALOptimizedStandardOptions = new UPPAALOptimizedStandardOptions();
    private final static EngineSupportOptions UPPAAALStandardOptions = new UPPAAALStandardOptions();
    private final static EngineSupportOptions UPPAALBroadcastOptions = new UPPAALBroadcastOptions();
    private final static EngineSupportOptions UPPAALBroadcastDegree2Options = new UPPAALBroadcastDegree2Options();
    private final static EngineSupportOptions verifyDTAPNOptions= new VerifyDTAPNEngineOptions();
    private final static EngineSupportOptions verifyPNOptions = new VerifyPNEngineOptions();

    private final static EngineSupportOptions[] engineSupportOptions = new EngineSupportOptions[]{verifyDTAPNOptions,verifyTAPNOptions,UPPAALCombiOptions,UPPAALOptimizedStandardOptions,UPPAAALStandardOptions,UPPAALBroadcastOptions,UPPAALBroadcastDegree2Options,verifyPNOptions};

    private TCTLAbstractProperty newProperty;
	private JTextField queryName;

	private static Boolean advancedView = false;

	private static boolean hasForcedDisabledTimeDarts = false;
	private static boolean hasForcedDisabledStubbornReduction = false;
	private static boolean hasForcedDisabledGCD = false;
	private static boolean disableSymmetryUpdate = false;
	private boolean wasCTLType = true;

	//Strings for tool tips
	//Tool tips for top panel
	private static final String TOOL_TIP_QUERYNAME = "Enter the name of the query.";
    private static final String TOOL_TIP_INFO_BUTTON = "Get help on the different verification options.";
    private static final String TOOL_TIP_QUERY_TYPE = "Choose the type of query.";
	private static final String TOOL_TIP_ADVANCED_VIEW_BUTTON = "Switch to the advanced view.";
	private static final String TOOL_TIP_SIMPLE_VIEW_BUTTON = "Switch to the simple view.";

	//Tool tip for query field
	private final static String TOOL_TIP_QUERY_FIELD = "<html>Click on a part of the query you want to edit.<br />" +
			"(Queries can also be edited manually by pressing the \"Edit Query\" button.)</html>";

	//Tool tips for quantification panel
	private static final String TOOL_TIP_EXISTS_DIAMOND = "Check if the given marking is reachable in the net.";
	private static final String TOOL_TIP_EXISTS_BOX = "Check if there is a trace on which all markings satisfy the given property. (Only available for some verification engines.)";
	private static final String TOOL_TIP_FORALL_DIAMOND = "Check if on any maximal trace there is a marking that satisfies the given property. (Only available for some verification engines.)";
	private static final String TOOL_TIP_FORALL_BOX = "Check if every reachable marking in the net satifies the given property.";

    private static final String TOOL_TIP_EXISTS_UNTIL = "There is a computation where the first formula holds until the second one holds.";
    private static final String TOOL_TIP_EXISTS_NEXT = "There is a transition firing after which the reached marking satisfies the given property.";
    private static final String TOOL_TIP_FORALL_UNTIL = "On every computation the first formula holds until the second one holds";
    private static final String TOOL_TIP_FORALL_NEXT = "After any transition firing the reached marking satisfies the given property.";

    private static final String TOOL_TIP_G = "Globally";
    private static final String TOOL_TIP_F = "Eventually";
    private static final String TOOL_TIP_U = "Until";
    private static final String TOOL_TIP_X = "Next";
    private static final String TOOL_TIP_E = "Switch to check if there exists a computation where the formula holds.";
    private static final String TOOL_TIP_A = "Switch to check if the formula holds for every computation.";

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
	private static final String TOOL_TIP_KBOUNDED = "Check whether the net is bounded for the given number of extra tokens.";

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
    private final static String TOOL_TIP_USE_STRUCTURALREDUCTION = "Apply structural reductions to reduce the size of the net.";
    private final static String TOOL_TIP_USE_SIPHONTRAP = "For a deadlock query, attempt to prove deadlock-freedom by using siphon-trap analysis via linear programming.";
    private final static String TOOL_TIP_USE_QUERY_REDUCTION = "Use query rewriting rules and linear programming (state equations) to reduce the size of the query.";
    private final static String TOOL_TIP_USE_TRACE_REFINEMENT = "Enables Trace Abstraction Refinement for reachability properties";
    private final static String TOOL_TIP_USE_TARJAN= "Uses the Tarjan algorithm when verifying. If not selected it will verify using the nested DFS algorithm.";

	//Tool tips for search options panel
	private final static String TOOL_TIP_HEURISTIC_SEARCH = "<html>Uses a heuristic method in state space exploration.<br />" +
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
	private final static String TOOL_TIP_SAVE_COMPOSED_BUTTON = "Open a composed net in a new tab and use approximated net if enabled";
	private final static String TOOL_TIP_OPEN_REDUCED_BUTTON = "Open the net produced after applying structural reduction rules";
	private final static String TOOL_TIP_SAVE_TAPAAL_BUTTON = "Export an xml file that can be used as input for the TAPAAL engine.";
	private final static String TOOL_TIP_SAVE_PN_BUTTON = "Export an xml file that can be used as input for the untimed Petri net engine.";

	//Tool tips for approximation panel
	private final static String TOOL_TIP_APPROXIMATION_METHOD_NONE = "No approximation method is used.";
	private final static String TOOL_TIP_APPROXIMATION_METHOD_OVER = "Approximate by dividing all intervals with the approximation constant and enlarging the intervals.";
	private final static String TOOL_TIP_APPROXIMATION_METHOD_UNDER = "Approximate by dividing all intervals with the approximation constant and shrinking the intervals.";
	private final static String TOOL_TIP_APPROXIMATION_CONSTANT = "Choose approximation constant";

	public QueryDialog(EscapableDialog me, QueryDialogueOption option, TAPNQuery queryToCreateFrom, TimedArcPetriNetNetwork tapnNetwork, HashMap<TimedArcPetriNet, DataLayer> guiModels, TabContent.TAPNLens lens) {
		this.tapnNetwork = tapnNetwork;
		this.guiModels = guiModels;
		this.lens = lens;
		inclusionPlaces = queryToCreateFrom == null ? new InclusionPlaces() : queryToCreateFrom.inclusionPlaces();
		newProperty = queryToCreateFrom == null ? new TCTLPathPlaceHolder() : queryToCreateFrom.getProperty();
		rootPane = me.getRootPane();
		isNetDegree2 = tapnNetwork.isDegree2();
		highestNetDegree = tapnNetwork.getHighestNetDegree();
		hasInhibitorArcs = tapnNetwork.hasInhibitorArcs();

		setLayout(new GridBagLayout());

		init(option, queryToCreateFrom);
        makeShortcuts();
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

        if (!lens.isGame() && !lens.isTimed()) {
            return getUntimedQuery(name, capacity, traceOption, searchOption, reductionOptionToSet);
        } else {
            return getTimedQuery(name, capacity, traceOption, searchOption, reductionOptionToSet);
        }
    }

    private TAPNQuery getTimedQuery(String name, int capacity, TraceOption traceOption, SearchOption searchOption, ReductionOption reductionOptionToSet) {
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

    private TAPNQuery getUntimedQuery(String name, int capacity, TraceOption traceOption, SearchOption searchOption, ReductionOption reductionOptionToSet) {
        boolean reduction = useReduction.isSelected();

        TAPNQuery query = new TAPNQuery(
            name,
            capacity,
            newProperty.copy(),
            traceOption,
            searchOption,
            reductionOptionToSet,
            /* symmetry */false,
            /* gcd */false,
            /* timeDart */false,
            /* pTrie */false,
            /* overApproximation */false,
            reduction,
            /* hashTableSizeToSet */ null,
            /* extrapolationOptionToSet */null,
            inclusionPlaces,
            /* enableOverApproximation */false,
            /* enableUnderApproximation */false,
            0
        );
        if (queryType.getSelectedIndex() == 1) {
            query.setCategory(TAPNQuery.QueryCategory.LTL);
        } else {
            query.setCategory(TAPNQuery.QueryCategory.CTL);
        }
        query.setUseSiphontrap(useSiphonTrap.isSelected());
        query.setUseQueryReduction(useQueryReduction.isSelected());
        query.setUseStubbornReduction(useStubbornReduction.isSelected());
        query.setUseTarOption(useTraceRefinement.isSelected());
        query.setUseTarjan(useTarjan.isSelected());
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

		if (lens.isGame()) {
		    fastestTraceRadioButton.setEnabled(false);
		    someTraceRadioButton.setEnabled(false);
		    noTraceRadioButton.setEnabled(true);
        } else if (lens.isTimed()) {
            fastestTraceRadioButton.setEnabled(tapnNetwork.isNonStrict() && !queryHasDeadlock() &&
                !(newProperty instanceof TCTLEGNode || newProperty instanceof TCTLAFNode));
            someTraceRadioButton.setEnabled(true);
            noTraceRadioButton.setEnabled(true);
        } else if (queryIsReachability() || queryType.getSelectedIndex() == 1) {
            fastestTraceRadioButton.setEnabled(false);
            someTraceRadioButton.setEnabled(true);
            noTraceRadioButton.setEnabled(true);
        } else {
            fastestTraceRadioButton.setEnabled(false);
            someTraceRadioButton.setEnabled(false);
            noTraceRadioButton.setEnabled(false);
            noTraceRadioButton.setSelected(true);
        }

		if(getTraceOption() == TraceOption.FASTEST) {
			if(fastestTraceRadioButton.isEnabled()){
				fastestTraceRadioButton.setSelected(true);
			} else if (someTraceRadioButton.isEnabled()) {
                someTraceRadioButton.setSelected(true);
            } else {
                noTraceRadioButton.setSelected(true);
            }
		}
	}

    private boolean queryIsReachability() {
        return new IsReachabilityVisitor().isReachability(newProperty);
    }

    private void resetQuantifierSelectionButtons() {
		quantificationButtonGroup.clearSelection();
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	public boolean queryHasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(newProperty);
	}

	public static TAPNQuery showQueryDialogue(QueryDialogueOption option, TAPNQuery queryToRepresent, TimedArcPetriNetNetwork tapnNetwork,
                                           HashMap<TimedArcPetriNet, DataLayer> guiModels, TabContent.TAPNLens lens) {
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
		QueryDialog queryDialogue = new QueryDialog(guiDialog, option, queryToRepresent, tapnNetwork, guiModels, lens);
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
        if (current instanceof TCTLStateToPathConverter && !lens.isTimed()) {
            current = ((TCTLStateToPathConverter) current).getProperty();
        }
        if (current instanceof TCTLAtomicPropositionNode) {
			TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) current;

			// bit of a hack to prevent posting edits to the undo manager when
			// we programmatically change the selection in the atomic proposition comboboxes etc.
			// because a different atomic proposition was selected
			userChangedAtomicPropSelection = false;
            if (node.getLeft() instanceof TCTLPlaceNode) {
                TCTLPlaceNode placeNode = (TCTLPlaceNode) node.getLeft();
                if (placeNode.getTemplate().equals("")) {
                    templateBox.setSelectedItem(SHARED);
                } else {
                    templateBox.setSelectedItem(tapnNetwork.getTAPNByName(placeNode.getTemplate()));
                }
            }
            if (!lens.isGame() && !lens.isTimed()) {
                updateUntimedQueryButtons(node);
            } else {
                updateTimedQueryButtons(node);
            }
        } else if (current instanceof TCTLTransitionNode) {
            TCTLTransitionNode transitionNode = (TCTLTransitionNode) current;
            userChangedAtomicPropSelection = false;
            if (transitionNode.getTemplate().equals("")) {
                templateBox.setSelectedItem(SHARED);
            } else {
                templateBox.setSelectedItem(tapnNetwork.getTAPNByName(transitionNode.getTemplate()));
            }
            placeTransitionBox.setSelectedItem(transitionNode.getTransition());
            userChangedAtomicPropSelection = true;
        }
        if (!lens.isTimed() && !lens.isGame()) {
            setEnablednessOfOperatorAndMarkingBoxes();
        }
        if (current instanceof LTLANode || current instanceof LTLENode ||
            (queryType.getSelectedIndex() == 1 && current instanceof TCTLPathPlaceHolder)) {
            negationButton.setEnabled(false);
        } else {
            negationButton.setEnabled(true);
        }
	}

    private void updateTimedQueryButtons(TCTLAtomicPropositionNode node) {
        if (!(node.getLeft() instanceof TCTLPlaceNode && node.getRight() instanceof TCTLConstNode)) {
            return;
        }
        TCTLPlaceNode placeNode = (TCTLPlaceNode) node.getLeft();
        TCTLConstNode placeMarkingNode = (TCTLConstNode) node.getRight();

        placeTransitionBox.setSelectedItem(placeNode.getPlace());
        relationalOperatorBox.setSelectedItem(node.getOp());
        placeMarking.setValue(placeMarkingNode.getConstant());
        userChangedAtomicPropSelection = true;
    }

    private void updateUntimedQueryButtons(TCTLAtomicPropositionNode node) {
        userChangedAtomicPropSelection = false;
        if (node.getLeft() instanceof TCTLPlaceNode) {
            TCTLPlaceNode placeNode = (TCTLPlaceNode) node.getLeft();
            placeTransitionBox.setSelectedItem(placeNode.getPlace());
        } else {
            if (placeTransitionBox.getItemCount() > 0) {
                placeTransitionBox.setSelectedIndex(0);
            }
        }
        relationalOperatorBox.setSelectedItem(node.getOp());

        if (node.getRight() instanceof TCTLConstNode) {
            TCTLConstNode placeMarkingNode = (TCTLConstNode) node.getRight();
            placeMarking.setValue(placeMarkingNode.getConstant());
        }
        userChangedAtomicPropSelection = true;
    }

    private void setEnablednessOfOperatorAndMarkingBoxes() {
        if (transitionIsSelected()) {
            placeMarking.setVisible(false);
            relationalOperatorBox.setVisible(false);
            transitionIsEnabledLabel.setVisible(true);
        } else {
            transitionIsEnabledLabel.setVisible(false);
            placeMarking.setVisible(true);
            relationalOperatorBox.setVisible(true);
        }
    }

    private boolean transitionIsSelected() {
        String itemName = (String) placeTransitionBox.getSelectedItem();
        if (itemName == null) return false;
        boolean transitionSelected = false;
        boolean sharedTransitionSelected = false;
        for (TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
            if (tapn.getTransitionByName(itemName) != null) {
                transitionSelected = true;
                break;
            }
        }
        if (!transitionSelected) {
            sharedTransitionSelected = tapnNetwork.getSharedTransitionByName(itemName) != null;
        }
        return transitionSelected || sharedTransitionSelected;
    }

	private void deleteSelection() {
		if (currentSelection != null) {
			TCTLAbstractProperty replacement = null;
            TCTLAbstractProperty selection = currentSelection.getObject();

			if (selection instanceof TCTLAbstractStateProperty) {
				replacement = new TCTLStatePlaceHolder();
			} else if (selection instanceof TCTLAbstractPathProperty) {
				replacement = new TCTLPathPlaceHolder();
			}
			if (replacement != null) {
				UndoableEdit edit = new QueryConstructionEdit(selection, replacement);
				newProperty = newProperty.replace(selection,	replacement);

				if (selection instanceof TCTLAbstractPathProperty)
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
			mergeNetComponentsButton.setEnabled(isQueryOk);
            openReducedNetButton.setEnabled(isQueryOk && useReduction.isSelected());
		} else {
			saveButton.setEnabled(false);
			saveAndVerifyButton.setEnabled(false);
			saveUppaalXMLButton.setEnabled(false);
			mergeNetComponentsButton.setEnabled(false);
            openReducedNetButton.setEnabled(false);
		}
	}

	private void setEnabledReductionOptions(){
		String reductionOptionString = getReductionOptionAsString();

		ArrayList<String> options = new ArrayList<String>();

		disableSymmetryUpdate = true;
		//The order here should be the same as in EngineSupportOptions
        boolean[] queryOptions = new boolean[]{
            fastestTraceRadioButton.isSelected(),
            (queryHasDeadlock() && (newProperty.toString().contains("EF") || newProperty.toString().contains("AG")) && highestNetDegree <= 2),
            (queryHasDeadlock() && (newProperty.toString().contains("EG") || newProperty.toString().contains("AF"))),
            (queryHasDeadlock() && hasInhibitorArcs),
            tapnNetwork.hasWeights(),
            hasInhibitorArcs,
            tapnNetwork.hasUrgentTransitions(),
            (newProperty.toString().contains("EG") || newProperty.toString().contains("AF")),
            //we want to know if it is strict
            !tapnNetwork.isNonStrict(),
            //we want to know if it is timed
            lens.isTimed(),
            (queryHasDeadlock() && highestNetDegree > 2),
            lens.isGame(),
            (newProperty.toString().contains("EG") || newProperty.toString().contains("AF")) && highestNetDegree > 2,
            newProperty.hasNestedPathQuantifiers()
        };


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

        if (tapnNetwork.isNonStrict()) {
            // disable timedarts if liveness and deadlock prop
            if((newProperty.toString().contains("EG") ||
                newProperty.toString().contains("AF"))){
                if (useTimeDarts != null) {
                    if(useTimeDarts.isSelected()){
                        hasForcedDisabledTimeDarts = true;
                    }
                    useTimeDarts.setEnabled(false);
                    useTimeDarts.setSelected(false);
                }
            }
        }
        if (lens.isTimed()) {
            for (EngineSupportOptions engine : engineSupportOptions) {
                if (engine.areOptionsSupported(queryOptions)) {
                    options.add(engine.nameString);
                }
            }
        } else if (!lens.isGame()) {
            options.add(name_UNTIMED);
        } else {
            options.add(name_DISCRETE);
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

		if (fastestTraceRadioButton.isSelected()) {
			breadthFirstSearch.setEnabled(false);
			depthFirstSearch.setEnabled(false);
			heuristicSearch.setEnabled(false);
			randomSearch.setEnabled(false);
			return;
		} else if (queryType.getSelectedIndex() == 1) {
            breadthFirstSearch.setEnabled(false);
            heuristicSearch.setEnabled(true);
            depthFirstSearch.setEnabled(true);
            randomSearch.setEnabled(true);

            if (!useTarjan.isSelected()) {
                heuristicSearch.setEnabled(false);
                if (someTraceRadioButton.isSelected()) {
                    randomSearch.setEnabled(false);
                }
            }
        } else {
			breadthFirstSearch.setEnabled(true);
			depthFirstSearch.setEnabled(true);
			heuristicSearch.setEnabled(true);
			randomSearch.setEnabled(true);
		}

		String reductionOptionString = getReductionOptionAsString();
		if (lens.isGame()) {
		    heuristicSearch.setEnabled(false);
        } else if (lens.isTimed() && (newProperty.toString().contains("EG") || newProperty.toString().contains("AF"))){
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
        existsUntil.setEnabled(false);
        existsNext.setEnabled(false);
        forAllUntil.setEnabled(false);
        forAllNext.setEnabled(false);
        globallyButton.setEnabled(false);
        finallyButton.setEnabled(false);
        nextButton.setEnabled(false);
        untilButton.setEnabled(false);
        aButton.setEnabled(false);
        eButton.setEnabled(false);

        conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		negationButton.setEnabled(false);
		templateBox.setEnabled(false);
		placeTransitionBox.setEnabled(false);
		relationalOperatorBox.setEnabled(false);
		placeMarking.setEnabled(false);
		addPredicateButton.setEnabled(false);
		truePredicateButton.setEnabled(false);
		falsePredicateButton.setEnabled(false);
		deadLockPredicateButton.setEnabled(false);
	}

	private void disableAllLTLButtons() {
        globallyButton.setEnabled(false);
        finallyButton.setEnabled(false);
        nextButton.setEnabled(false);
        untilButton.setEnabled(false);
        aButton.setEnabled(false);
        eButton.setEnabled(false);

        conjunctionButton.setEnabled(false);
        disjunctionButton.setEnabled(false);
        negationButton.setEnabled(false);
        templateBox.setEnabled(false);
        placeTransitionBox.setEnabled(false);
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
        if (!lens.isTimed()) {
            existsUntil.setEnabled(true);
            existsNext.setEnabled(true);
            forAllUntil.setEnabled(true);
            forAllNext.setEnabled(true);
        }

        conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		negationButton.setEnabled(false);
		templateBox.setEnabled(false);
		placeTransitionBox.setEnabled(false);
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
        existsUntil.setEnabled(false);
        existsNext.setEnabled(false);
        forAllUntil.setEnabled(false);
        forAllNext.setEnabled(false);

        conjunctionButton.setEnabled(true);
		disjunctionButton.setEnabled(true);
		negationButton.setEnabled(true);
		templateBox.setEnabled(true);
		placeTransitionBox.setEnabled(true);
		relationalOperatorBox.setEnabled(true);
		placeMarking.setEnabled(true);
		truePredicateButton.setEnabled(true);
		falsePredicateButton.setEnabled(true);
		deadLockPredicateButton.setEnabled(true);
		setEnablednessOfAddPredicateButton();
	}

    private void enableOnlyUntimedStateButtons() {
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
        placeTransitionBox.setEnabled(true);
        relationalOperatorBox.setEnabled(true);
        placeMarking.setEnabled(true);
        truePredicateButton.setEnabled(true);
        falsePredicateButton.setEnabled(true);
        deadLockPredicateButton.setEnabled(true);

        if (queryType.getSelectedIndex() == 1) {
            updateLTLButtons();
        }

        setEnablednessOfAddPredicateButton();
    }

    private void enableOnlyForAll() {
        existsBox.setEnabled(false);
        existsDiamond.setEnabled(false);
        forAllBox.setEnabled(true);
        forAllDiamond.setEnabled(true);
        if (!lens.isTimed()) {
            existsUntil.setEnabled(false);
            existsNext.setEnabled(false);
            forAllUntil.setEnabled(false);
            forAllNext.setEnabled(false);
        }

        conjunctionButton.setEnabled(false);
        disjunctionButton.setEnabled(false);
        negationButton.setEnabled(false);
        templateBox.setEnabled(false);
        placeTransitionBox.setEnabled(false);
        relationalOperatorBox.setEnabled(false);
        placeMarking.setEnabled(false);
        addPredicateButton.setEnabled(false);
        truePredicateButton.setEnabled(false);
        falsePredicateButton.setEnabled(false);
        deadLockPredicateButton.setEnabled(false);
    }

	private void setEnablednessOfAddPredicateButton() {
		if (placeTransitionBox.getSelectedItem() == null ||
            (queryType.getSelectedIndex() == 1 && currentSelection.getObject() == newProperty))
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
		if (currentSelection != null && (currentSelection.getObject() instanceof TCTLAtomicPropositionNode ||
            (!lens.isTimed() && currentSelection.getObject() instanceof TCTLTransitionNode))) {

		    Object item = templateBox.getSelectedItem();
			String template = item.equals(SHARED) ? "" : item.toString();
			TCTLAbstractStateProperty property;

            if (!lens.isTimed() && transitionIsSelected()) {
                property = new TCTLTransitionNode(template, (String) placeTransitionBox.getSelectedItem());
            } else {
                property = new TCTLAtomicPropositionNode(
                    new TCTLPlaceNode(template, (String) placeTransitionBox.getSelectedItem()),
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

        if (lens.isTimed() || lens.isGame()) {
            setupQuantificationFromQuery(queryToCreateFrom);
            setupApproximationOptionsFromQuery(queryToCreateFrom);
        }

        setupQueryCategoryFromQuery(queryToCreateFrom);
		setupSearchOptionsFromQuery(queryToCreateFrom);
		setupReductionOptionsFromQuery(queryToCreateFrom);
		setupTraceOptionsFromQuery(queryToCreateFrom);
		setupTarOptionsFromQuery(queryToCreateFrom);
        setupTarjanOptionsFromQuery(queryToCreateFrom);
	}

	private void setupTarOptionsFromQuery(TAPNQuery queryToCreateFrom) {
	    if (queryToCreateFrom.isTarOptionEnabled()) {
	        useTraceRefinement.setSelected(true);
        }
    }

    private void setupTarjanOptionsFromQuery(TAPNQuery queryToCreateFrom) {
        useTarjan.setSelected(queryToCreateFrom.isTarjan());
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
		} else if(queryToCreateFrom.getReductionOption() == ReductionOption.VerifyPN){
			reduction = name_UNTIMED;
		} else if(queryToCreateFrom.getReductionOption() == ReductionOption.COMBI){
			reduction = name_COMBI;
		} else if (newProperty.toString().contains("EF") || newProperty.toString().contains("AG")) {
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

        if (lens.isTimed() || lens.isGame()) {
            setupTimedReductionOptions(queryToCreateFrom);
        } else {
            setupUntimedReductionOptions(queryToCreateFrom);
        }
	}

    private void setupTimedReductionOptions(TAPNQuery queryToCreateFrom) {
        symmetryReduction.setSelected(queryToCreateFrom.useSymmetry());
        useTimeDarts.setSelected(queryToCreateFrom.useTimeDarts());
        usePTrie.setSelected(queryToCreateFrom.usePTrie());
        useStubbornReduction.setSelected(queryToCreateFrom.isStubbornReductionEnabled());
        useGCD.setSelected(queryToCreateFrom.useGCD());
        useOverApproximation.setSelected(queryToCreateFrom.useOverApproximation());
        useReduction.setSelected(queryToCreateFrom.useReduction());
        discreteInclusion.setSelected(queryToCreateFrom.discreteInclusion());

        if (queryToCreateFrom.discreteInclusion()) {
            selectInclusionPlacesButton.setEnabled(true);
        }
    }

    private void setupUntimedReductionOptions(TAPNQuery queryToCreateFrom) {
        useSiphonTrap.setSelected(queryToCreateFrom.isSiphontrapEnabled());
        useQueryReduction.setSelected(queryToCreateFrom.isQueryReductionEnabled());
        useStubbornReduction.setSelected(queryToCreateFrom.isStubbornReductionEnabled());
        useReduction.setSelected(queryToCreateFrom.useReduction());
        useTraceRefinement.setSelected(queryToCreateFrom.isTarOptionEnabled());
        useTarjan.setSelected(queryToCreateFrom.isTarjan());
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

	private void setupQueryCategoryFromQuery(TAPNQuery queryToCreateFrom) {
        if (!lens.isTimed() && !lens.isGame()) {
            TAPNQuery.QueryCategory category = queryToCreateFrom.getCategory();
            if (category.equals(TAPNQuery.QueryCategory.CTL)) {
                queryType.setSelectedIndex(0);
            } else if (category.equals(TAPNQuery.QueryCategory.LTL)) {
                queryType.setSelectedIndex(1);
            }
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
		queryType = new JComboBox(new String[]{"CTL/Reachability", "LTL"});
		queryType.setToolTipText(TOOL_TIP_QUERY_TYPE);
		queryType.addActionListener(arg0 -> toggleDialogType());

		advancedButton = new JButton("Advanced view");
		advancedButton.setToolTipText(TOOL_TIP_ADVANCED_VIEW_BUTTON);
		advancedButton.addActionListener(arg0 -> toggleAdvancedSimpleView(true));

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
				StringBuilder buffer = new StringBuilder();
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
		if (!lens.isTimed() && !lens.isGame()) topButtonPanel.add(queryType);
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
		if (lens.isTimed() || lens.isGame()) {
		    saveUppaalXMLButton.setVisible(advancedView);
		    overApproximationOptionsPanel.setVisible(advancedView);
        } else if (!lens.isGame()){
            openReducedNetButton.setVisible(advancedView);
        }
		mergeNetComponentsButton.setVisible(advancedView);


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

	private void toggleDialogType() {
       if (queryType.getSelectedIndex() == 1 && wasCTLType) {
           String ltlType = checkLTLType();
           boolean isA = ltlType.equals("A");
           if (convertPropertyType(false, newProperty, true, isA) == null &&
               !(newProperty instanceof TCTLStatePlaceHolder)) {
               if (showWarningMessage(false) == JOptionPane.YES_OPTION) {
                   deleteProperty();
               } else {
                   queryType.setSelectedIndex(0);
                   return;
               }
           } else if (isA) {
              addAllPathsToProperty(newProperty, null);
           } else if (ltlType.equals("E")) {
               addExistsPathsToProperty(newProperty, null);
           }
           showLTLButtons(true);
           updateShiphonTrap(true);
           queryChanged();
           wasCTLType = false;
       } else if (queryType.getSelectedIndex() == 0 && !wasCTLType) {
           if (convertPropertyType(true, newProperty, true, newProperty instanceof LTLANode) == null &&
               !(newProperty instanceof TCTLStatePlaceHolder)) {
               if (showWarningMessage(true) == JOptionPane.YES_OPTION) {
                   deleteProperty();
                   newProperty = removeExistsAllPathsFromProperty(newProperty);
               } else {
                   queryType.setSelectedIndex(1);
                   return;
               }
           }
           showLTLButtons(false);
           updateShiphonTrap(false);
           wasCTLType = true;
       }
       if (undoManager != null) undoManager.discardAllEdits();
       if (undoButton != null) undoButton.setEnabled(false);
       if (redoButton != null) redoButton.setEnabled(false);
       setEnabledOptionsAccordingToCurrentReduction();
    }

    private String checkLTLType() {
	    if (newProperty.toString().equals("<*>"))
	        return "placeholder";
	    if (newProperty.toString().startsWith("A"))
	        return "A";
        if (newProperty.toString().startsWith("E"))
            return "E";
        if (newProperty.toString().startsWith("A", 2))
            return "A";
        if (newProperty.toString().startsWith("E", 2))
            return "E";
        if (newProperty.toString().startsWith("A", 3))
            return "A";
        if (newProperty.toString().startsWith("E", 3))
            return "E";
        return "placeholder";
    }

    private TCTLAbstractProperty convertPropertyType(boolean toCTL, TCTLAbstractProperty property, boolean isFirst, boolean isA) {
        if (property != null) {
            property = removeExistsAllPathsFromProperty(removeConverter(property));

            if (!toCTL && (property instanceof TCTLDeadlockNode || !canBeConverted(property, isA))) {
                return null;
            } else if (property.isSimpleProperty() && !(property instanceof TCTLNotNode)) {
                if (!isFirst) {
                    return property;
                } else if (property instanceof TCTLTrueNode || property instanceof TCTLFalseNode ||
                           property instanceof TCTLAtomicPropositionNode || property instanceof TCTLTransitionNode) {
                    property = ConvertToPathProperty((TCTLAbstractStateProperty) property);
                    return replaceProperty(property);
                }
            }

            TCTLAbstractProperty replacement = getReplacement(toCTL, property, isA);

            if (!isFirst) {
                return replacement;
            }
            return replaceProperty(replacement);
        }
        return null;
	}

	private boolean canBeConverted(TCTLAbstractProperty property, boolean isA) {
	    if (isA && property.toString().startsWith("E")) {
	        return false;
        } else if (!isA && property.toString().startsWith("A")) {
            return false;
        }
	    return true;
	}

	private TCTLAbstractProperty removeConverter(TCTLAbstractProperty property) {
	    while (property instanceof TCTLPathToStateConverter || property instanceof TCTLStateToPathConverter) {
            if (property instanceof TCTLStateToPathConverter) {
                property = ConvertToStateProperty((TCTLStateToPathConverter) property);
            } else {
                property = ConvertToPathProperty((TCTLPathToStateConverter) property);
            }
        }
        return property;
    }

	private TCTLAbstractProperty getReplacement(boolean toCTL, TCTLAbstractProperty property, boolean isA) {
	    TCTLAbstractProperty replacement = null;
        TCTLAbstractStateProperty firstChild = getChild(toCTL, property, 1, isA);
        TCTLAbstractStateProperty secondChild = getChild(toCTL, property, 2, isA);
        property = removeConverter(property);

        if (firstChild == null || secondChild == null)
            return null;
        if (toCTL) {
            if (property instanceof LTLGNode) {
                replacement = isA? new TCTLAGNode(firstChild) : new TCTLEGNode(firstChild);
            } else if (property instanceof LTLFNode) {
                replacement = isA ? new TCTLAFNode(firstChild) : new TCTLEFNode(firstChild);
            } else if (property instanceof LTLXNode) {
                replacement = isA ? new TCTLAXNode(firstChild) : new TCTLEXNode(firstChild);
            } else if (property instanceof LTLUNode) {
                replacement = isA ? new TCTLAUNode(firstChild, secondChild): new TCTLEUNode(firstChild, secondChild);
            }
        } else {
            if (property instanceof TCTLAGNode || property instanceof TCTLEGNode) {
                replacement = new LTLGNode(firstChild);
            } else if (property instanceof TCTLAFNode || property instanceof TCTLEFNode) {
                replacement = new LTLFNode(firstChild);
            } else if (property instanceof TCTLAXNode || property instanceof TCTLEXNode) {
                replacement = new LTLXNode(firstChild);
            } else if (property instanceof TCTLAUNode || property instanceof TCTLEUNode) {
                replacement = new LTLUNode(firstChild, secondChild);
            }
        }

        if (replacement == null) {
            if (property instanceof TCTLStatePlaceHolder || property instanceof TCTLPathPlaceHolder) {
                return property;
            } else if (property instanceof TCTLNotNode) {
                return new TCTLNotNode(firstChild);
            } else if (property instanceof TCTLAndListNode) {
                return new TCTLAndListNode(firstChild, secondChild);
            } else if (property instanceof TCTLOrListNode) {
                return new TCTLOrListNode(firstChild, secondChild);
            } else {
                replacement = property;
            }
        }
	    return replacement;
    }

    private TCTLAbstractStateProperty getChild(boolean toCTL, TCTLAbstractProperty property, int childNumber, boolean isA) {
        property = removeConverter(property);
        TCTLAbstractProperty child = getSpecificChildOfProperty(childNumber, property);
        child = removeConverter(child);

        if (!(child instanceof TCTLStatePlaceHolder || child instanceof TCTLPathPlaceHolder)) {
           if (!child.isSimpleProperty() || child instanceof TCTLNotNode) {
                TCTLAbstractProperty replacement = convertPropertyType(toCTL, child, false, isA);
                if (replacement == null) {
                    return null;
                }
                replacement = removeConverter(replacement);
                child = child.replace(child, replacement);
          } else if (child instanceof TCTLDeadlockNode) {
               return null;
           }
        }
        if (child instanceof TCTLAbstractPathProperty) {
            return ConvertToStateProperty((TCTLAbstractPathProperty)child);
        }

        return (TCTLAbstractStateProperty) child;
    }

    private TCTLAbstractProperty replaceProperty(TCTLAbstractProperty replacement) {
        if (replacement != null) {
            newProperty = removeConverter(newProperty);
            if ((newProperty instanceof LTLANode || newProperty instanceof LTLENode)
                && !(replacement instanceof TCTLAbstractPathProperty)) {
                replacement = ConvertToPathProperty((TCTLAbstractStateProperty)replacement);
            }
            newProperty = newProperty.replace(newProperty, replacement);
            replacement = removeConverter(replacement);

            if (newProperty instanceof TCTLAbstractPathProperty) resetQuantifierSelectionButtons();

            updateSelection(replacement);
            queryChanged();

            return newProperty;
        }
        return null;
    }

    private void deleteProperty() {
        if (newProperty != null) {
            TCTLAbstractProperty replacement = null;
            newProperty = removeConverter(newProperty);
            if (newProperty instanceof TCTLAbstractStateProperty) {
                replacement = new TCTLStatePlaceHolder();
            } else if (newProperty instanceof TCTLAbstractPathProperty) {
                replacement = new TCTLPathPlaceHolder();
            }
            replaceProperty(replacement);
        }
    }

    private int showWarningMessage(boolean toCTL) {
	    String category = toCTL ? "CTL" : "LTL";
	    String message = "The query property will be deleted, because it is not compatible with "+category+"-queries.\n" +
            "Are you sure you want to change query category?";
	    String title = "Incompatible query";

	    return JOptionPane.showConfirmDialog(
            CreateGui.getApp(),
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    }

    private void addAllPathsToProperty(TCTLAbstractProperty oldProperty, TCTLAbstractProperty selection) {
        TCTLAbstractProperty property = null;

        if (oldProperty instanceof LTLANode) {
            property = oldProperty;
        } else if (oldProperty instanceof TCTLPathPlaceHolder) {
            property = new LTLANode();
        } else if (oldProperty instanceof TCTLAbstractPathProperty) {
            property = new LTLANode(ConvertToStateProperty((TCTLAbstractPathProperty) oldProperty));
        } else if (oldProperty instanceof TCTLNotNode) {
            property = new LTLANode((TCTLNotNode) oldProperty);
            property = ConvertToStateProperty((TCTLAbstractPathProperty) property);
        } else if (oldProperty instanceof TCTLAbstractStateProperty && (selection == null || selection instanceof LTLANode)) {
            property = new LTLANode((TCTLAbstractStateProperty) oldProperty);
            if (!(newProperty instanceof TCTLAbstractPathProperty)) newProperty = ConvertToPathProperty((TCTLAbstractStateProperty) newProperty);
        }

        if (property != null && selection != null) {
            UndoableEdit edit = new QueryConstructionEdit(selection, property);
            newProperty = newProperty.replace(newProperty, property);
            updateSelection(property);
            undoSupport.postEdit(edit);
            queryChanged();
        } else if (property != null) {
            newProperty = newProperty.replace(newProperty, property);
            updateSelection(property);
            queryChanged();
        }
    }

    private void addExistsPathsToProperty(TCTLAbstractProperty oldProperty, TCTLAbstractProperty selection) {
        TCTLAbstractProperty property = null;

        if (oldProperty instanceof LTLENode) {
            property = oldProperty;
        } else if (oldProperty instanceof TCTLPathPlaceHolder) {
            property = new LTLENode();
        } else if (oldProperty instanceof TCTLAbstractPathProperty) {
            property = new LTLENode(ConvertToStateProperty((TCTLAbstractPathProperty) oldProperty));
        } else if (oldProperty instanceof TCTLNotNode) {
            property = new LTLENode((TCTLNotNode) oldProperty);
            property = ConvertToStateProperty((TCTLAbstractPathProperty) property);
        } else if (oldProperty instanceof TCTLAbstractStateProperty && (selection == null || selection instanceof LTLENode)) {
            property = new LTLENode((TCTLAbstractStateProperty) oldProperty);
            if (!(newProperty instanceof TCTLAbstractPathProperty)) newProperty = ConvertToPathProperty((TCTLAbstractStateProperty) newProperty);
        }

        if (property != null && selection != null) {
            UndoableEdit edit = new QueryConstructionEdit(selection, property);
            newProperty = newProperty.replace(newProperty, property);
            updateSelection(property);
            undoSupport.postEdit(edit);
            queryChanged();
        } else if (property != null) {
            newProperty = newProperty.replace(newProperty, property);
            updateSelection(property);
            queryChanged();
        }
    }

    private TCTLAbstractProperty removeExistsAllPathsFromProperty(TCTLAbstractProperty oldProperty) {
        TCTLAbstractProperty property = oldProperty;
        TCTLAbstractStateProperty firstChild = getSpecificChildOfProperty(1, oldProperty);

        if (oldProperty instanceof TCTLPathToStateConverter) {
            oldProperty = ((TCTLPathToStateConverter) oldProperty).getProperty();
            firstChild = getSpecificChildOfProperty(1, oldProperty);
        }
        if (oldProperty instanceof LTLANode) {
            TCTLAbstractPathProperty child = ConvertToPathProperty(firstChild);
            property = oldProperty.replace(oldProperty, child);
        }
        if (oldProperty instanceof LTLENode) {
            TCTLAbstractPathProperty child = ConvertToPathProperty(firstChild);
            property = oldProperty.replace(oldProperty, child);
        }

        return property;
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
		kbounded.addActionListener(evt -> Verifier.analyzeKBound(tapnNetwork, getCapacity(), numberOfExtraTokensInNet));
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
		queryField.setEditable(false);
        queryField.setText(newProperty.toString());
		queryField.setToolTipText(TOOL_TIP_QUERY_FIELD);

		// Put the text pane in a scroll pane.
		JScrollPane queryScrollPane = new JScrollPane(queryField);
		queryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension d = new Dimension(900, 80);
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

        if (lens.isGame()) {
            queryScrollPane.setColumnHeaderView( new JLabel("control: ", SwingConstants.CENTER));
        }

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 4;
		queryPanel.add(queryScrollPane, gbc);
	}

	private void initQuantificationPanel() {
		quantificationPanel = new JPanel(new GridBagLayout());
		quantificationPanel.setBorder(BorderFactory.createTitledBorder("Quantification"));
        quantificationButtonGroup = new ButtonGroup();
		approximationRadioButtonGroup = new ButtonGroup();

        // Instantiate buttons
        existsDiamond = new JButton("EF");
        existsBox = new JButton("EG");
        forAllDiamond = new JButton("AF");
        forAllBox = new JButton("AG");
        existsUntil = new JButton("EU");
        existsNext = new JButton("EX");
        forAllUntil = new JButton("AU");
        forAllNext = new JButton("AX");
        globallyButton = new JButton("G");
        finallyButton = new JButton("F");
        nextButton = new JButton("X");
        untilButton = new JButton("U");
        aButton = new JButton("A");
        eButton = new JButton("E");

        // Add tool-tips
        existsDiamond.setToolTipText(TOOL_TIP_EXISTS_DIAMOND);
        existsBox.setToolTipText(TOOL_TIP_EXISTS_BOX);
        forAllDiamond.setToolTipText(TOOL_TIP_FORALL_DIAMOND);
        forAllBox.setToolTipText(TOOL_TIP_FORALL_BOX);
        existsUntil.setToolTipText(TOOL_TIP_EXISTS_UNTIL);
        existsNext.setToolTipText(TOOL_TIP_EXISTS_NEXT);
        forAllUntil.setToolTipText(TOOL_TIP_FORALL_UNTIL);
        forAllNext.setToolTipText(TOOL_TIP_FORALL_NEXT);
        globallyButton.setToolTipText(TOOL_TIP_G);
        finallyButton.setToolTipText(TOOL_TIP_F);
        nextButton.setToolTipText(TOOL_TIP_X);
        untilButton.setToolTipText(TOOL_TIP_U);
        aButton.setToolTipText(TOOL_TIP_A);
        eButton.setToolTipText(TOOL_TIP_E);

        // Add buttons to panel
        quantificationButtonGroup.add(existsDiamond);
        quantificationButtonGroup.add(existsBox);
        quantificationButtonGroup.add(forAllDiamond);
        quantificationButtonGroup.add(forAllBox);
        quantificationButtonGroup.add(existsUntil);
        quantificationButtonGroup.add(existsNext);
        quantificationButtonGroup.add(forAllUntil);
        quantificationButtonGroup.add(forAllNext);
        quantificationButtonGroup.add(globallyButton);
        quantificationButtonGroup.add(finallyButton);
        quantificationButtonGroup.add(nextButton);
        quantificationButtonGroup.add(untilButton);
        quantificationButtonGroup.add(aButton);
        quantificationButtonGroup.add(eButton);

        // Place buttons in GUI
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // First column of buttons
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        quantificationPanel.add(existsDiamond, gbc);
        quantificationPanel.add(globallyButton, gbc);
        gbc.gridy = 1;
        quantificationPanel.add(existsBox, gbc);
        quantificationPanel.add(finallyButton, gbc);
        gbc.gridy = 2;
        quantificationPanel.add(existsUntil, gbc);
        gbc.gridy = 3;
        quantificationPanel.add(existsNext, gbc);
        gbc.gridy = 4;
        quantificationPanel.add(aButton, gbc);

        // Second column of buttons
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        quantificationPanel.add(forAllDiamond, gbc);
        quantificationPanel.add(nextButton, gbc);
        gbc.gridy = 1;
        quantificationPanel.add(forAllBox, gbc);
        quantificationPanel.add(untilButton, gbc);
        gbc.gridy = 2;
        quantificationPanel.add(forAllUntil, gbc);
        gbc.gridy = 3;
        quantificationPanel.add(forAllNext, gbc);
        gbc.gridy = 4;
        quantificationPanel.add(eButton, gbc);

        // Add quantification panel to query panel
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(quantificationPanel, gbc);

		if (lens.isTimed()|| lens.isGame()) {
            addTimedQuantificationListeners();
            showLTLButtons(false);
        } else {
            addUntimedQuantificationListeners();
            showLTLButtons(false);
        }
    }

    private void addTimedQuantificationListeners() {
		// Add action listeners to the query options
		existsBox.addActionListener(e -> {
			TCTLEGNode property = new TCTLEGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            existsBox.setSelected(true);
            addPropertyToQuery(property);
            unselectButtons();
		});

		existsDiamond.addActionListener(e -> {
			TCTLEFNode property = new TCTLEFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            existsDiamond.setSelected(true);
            addPropertyToQuery(property);
            unselectButtons();
		});

		forAllBox.addActionListener(e -> {
			TCTLAGNode property = new TCTLAGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            forAllBox.setSelected(true);
            addPropertyToQuery(property);
            unselectButtons();
		});

		forAllDiamond.addActionListener(e -> {
			TCTLAFNode property = new TCTLAFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            forAllDiamond.setSelected(true);
            addPropertyToQuery(property);
            unselectButtons();
		});
	}

    private void unselectButtons() {
        existsDiamond.setSelected(false);
        existsBox.setSelected(false);
        forAllBox.setSelected(false);
        forAllDiamond.setSelected(false);
    }

    private void addUntimedQuantificationListeners() {
        addTimedQuantificationListeners();

        existsNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLEXNode((TCTLAbstractStateProperty) currentSelection.getObject());
                } else {
                    property = new TCTLEXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
            }
        });

        existsUntil.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLEUNode((TCTLAbstractStateProperty) currentSelection.getObject(),
                        new TCTLStatePlaceHolder());
                } else {
                    property = new TCTLEUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                        getSpecificChildOfProperty(2, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
            }
        });

        globallyButton.addActionListener(e -> {
            LTLGNode property = new LTLGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            addPropertyToQuery(property);
            unselectButtons();
        });

        finallyButton.addActionListener(e -> {
            LTLFNode property = new LTLFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            addPropertyToQuery(property);
            unselectButtons();
        });

        forAllNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLAXNode((TCTLAbstractStateProperty) currentSelection.getObject());
                } else {
                    property = new TCTLAXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
            }
        });

        forAllUntil.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new TCTLAUNode((TCTLAbstractStateProperty) currentSelection.getObject(),
                        new TCTLStatePlaceHolder());
                } else {
                    property = new TCTLAUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                        getSpecificChildOfProperty(2, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new LTLXNode((TCTLAbstractStateProperty) currentSelection.getObject());
                } else {
                    property = new LTLXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
            }
        });

        untilButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractPathProperty property;
                if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    property = new LTLUNode((TCTLAbstractStateProperty) currentSelection.getObject(),
                        new TCTLStatePlaceHolder());
                } else {
                    property = new LTLUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                        getSpecificChildOfProperty(2, currentSelection.getObject()));
                }
                addPropertyToQuery(property);
            }
        });

        aButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractProperty oldProperty = newProperty;

                newProperty = removeExistsAllPathsFromProperty(newProperty);
                addAllPathsToProperty(newProperty, null);
                UndoableEdit edit = new QueryConstructionEdit(oldProperty, newProperty);
                undoSupport.postEdit(edit);

                queryChanged();
            }
        });

        eButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TCTLAbstractProperty oldProperty = newProperty;

                newProperty = removeExistsAllPathsFromProperty(newProperty);
                addExistsPathsToProperty(newProperty, null);
                UndoableEdit edit = new QueryConstructionEdit(oldProperty, newProperty);
                undoSupport.postEdit(edit);

                queryChanged();
            }
        });
    }

    private void showLTLButtons(boolean isVisible) {
        globallyButton.setVisible(isVisible);
        finallyButton.setVisible(isVisible);
        nextButton.setVisible(isVisible);
        untilButton.setVisible(isVisible);
        aButton.setVisible(isVisible);
        eButton.setVisible(isVisible);
        if (deadLockPredicateButton != null) deadLockPredicateButton.setVisible(!isVisible);
        showCTLButtons(!isVisible);
    }

    private void showCTLButtons(boolean isVisible) {
        forAllBox.setVisible(isVisible);
        forAllDiamond.setVisible(isVisible);
        forAllNext.setVisible(isVisible);
        forAllUntil.setVisible(isVisible);
        existsBox.setVisible(isVisible);
        existsDiamond.setVisible(isVisible);
        existsNext.setVisible(isVisible);
        existsUntil.setVisible(isVisible);
    }
    private void updateShiphonTrap(boolean isLTL) {
        useSiphonTrap.setEnabled(!isLTL);
    }

    private void addPropertyToQuery(TCTLAbstractPathProperty property) {
	    TCTLAbstractProperty selection = currentSelection.getObject();
	    if (selection instanceof TCTLAbstractStateProperty) {
            addPropertyToQuery(ConvertToStateProperty(property));
            return;
        }

        if (selection instanceof LTLANode) {
            newProperty = newProperty.replace(selection, property);
            addAllPathsToProperty(newProperty, selection);
            return;
        } else if (selection instanceof LTLENode) {
            newProperty = newProperty.replace(selection, property);
            addExistsPathsToProperty(newProperty, selection);
            return;
        }

        UndoableEdit edit = new QueryConstructionEdit(selection, property);
        newProperty = newProperty.replace(selection, property);
        updateSelection(property);
        undoSupport.postEdit(edit);
        queryChanged();
    }

    private void addPropertyToQuery(TCTLAbstractStateProperty property) {
        if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
            addPropertyToQuery(ConvertToPathProperty(property));
            return;
        }

        UndoableEdit edit = new QueryConstructionEdit(currentSelection.getObject(), property);
        newProperty = newProperty.replace(currentSelection.getObject(), property);
        updateSelection(property);
        undoSupport.postEdit(edit);
        queryChanged();
    }

    private TCTLAbstractStateProperty ConvertToStateProperty(TCTLAbstractPathProperty p) {
        if (p instanceof TCTLStateToPathConverter) {
            return ((TCTLStateToPathConverter) p).getProperty();
        } else return new TCTLPathToStateConverter(p);
    }

    private TCTLAbstractPathProperty ConvertToPathProperty(TCTLAbstractStateProperty p) {
        if (p instanceof TCTLPathToStateConverter) {
            return ((TCTLPathToStateConverter) p).getProperty();
        } else return new TCTLStateToPathConverter(p);
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
					addPropertyToQuery(andListNode);
				} else if (currentSelection.getObject() instanceof TCTLOrListNode) {
					andListNode = new TCTLAndListNode(((TCTLOrListNode) currentSelection.getObject()).getProperties());
					addPropertyToQuery(andListNode);
				} else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
					TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty) currentSelection
							.getObject();
					TCTLAbstractProperty parentNode = prop.getParent();

					if (parentNode instanceof TCTLAndListNode) {
						// current selection is child of an andList node => add
						// new placeholder conjunct to it
						andListNode = new TCTLAndListNode((TCTLAndListNode) parentNode);
						andListNode.addConjunct(new TCTLStatePlaceHolder());
						addPropertyToQuery(andListNode);
					} else {
						TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
						andListNode = new TCTLAndListNode(getStateProperty(currentSelection.getObject()),	ph);
						addPropertyToQuery(andListNode);
					}
				} else if (!lens.isTimed()) {
                    checkUntimedAndNode();
                }
			}

		});

		disjunctionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLOrListNode orListNode;
				if (currentSelection.getObject() instanceof TCTLOrListNode) {
					orListNode = new TCTLOrListNode((TCTLOrListNode) currentSelection.getObject());
					orListNode.addDisjunct(new TCTLStatePlaceHolder());
                    addPropertyToQuery(orListNode);
                } else if (currentSelection.getObject() instanceof TCTLAndListNode) {
					orListNode = new TCTLOrListNode(((TCTLAndListNode) currentSelection.getObject()).getProperties());
                    addPropertyToQuery(orListNode);
                } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
					TCTLAbstractStateProperty prop = (TCTLAbstractStateProperty) currentSelection.getObject();
					TCTLAbstractProperty parentNode = prop.getParent();

					if (parentNode instanceof TCTLOrListNode) {
						// current selection is child of an orList node => add
						// new placeholder disjunct to it
						orListNode = new TCTLOrListNode((TCTLOrListNode) parentNode);
						orListNode.addDisjunct(new TCTLStatePlaceHolder());
                        addPropertyToQuery(orListNode);
                    } else {
						TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
						orListNode = new TCTLOrListNode(getStateProperty(currentSelection.getObject()),	ph);
                        addPropertyToQuery(orListNode);
                    }
				} else if (!lens.isTimed()) {
                    checkUntimedOrNode();
                }
			}
		});

		negationButton.addActionListener(e -> {
            if (lens.isTimed() || lens.isGame()) {
                TCTLNotNode property = new TCTLNotNode(getStateProperty(currentSelection.getObject()));
                addPropertyToQuery(property);
            } else {
                TCTLAbstractStateProperty root;
                if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
                    root = ConvertToStateProperty(getPathProperty(currentSelection.getObject()));
                } else {
                    root = getStateProperty(currentSelection.getObject());
                }
                TCTLNotNode property = new TCTLNotNode(root);
                addPropertyToQuery(property);
            }
		});
	}

    private void checkUntimedAndNode() {
        TCTLAndListNode andListNode;
        if (currentSelection.getObject() instanceof TCTLStateToPathConverter) {
            TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();

            TCTLAbstractStateProperty prop = ((TCTLStateToPathConverter) currentSelection.getObject()).getProperty();

            if (prop instanceof TCTLAndListNode) {
                andListNode = new TCTLAndListNode((TCTLAndListNode) prop);
                andListNode.addConjunct(new TCTLStatePlaceHolder());
            } else if (prop instanceof TCTLOrListNode) {
                andListNode = new TCTLAndListNode(((TCTLOrListNode) prop).getProperties());
            } else {
                andListNode = new TCTLAndListNode(getStateProperty(prop), ph);
            }

            TCTLAbstractPathProperty property = new TCTLStateToPathConverter(andListNode);
            addPropertyToQuery(property);
        } else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
            TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();

            TCTLAbstractProperty oldProperty = removeExistsAllPathsFromProperty(currentSelection.getObject());

            andListNode = new TCTLAndListNode(getStateProperty(
                new TCTLPathToStateConverter((TCTLAbstractPathProperty) oldProperty)), ph);

            TCTLAbstractPathProperty property = new TCTLStateToPathConverter(andListNode);
            addPropertyToQuery(property);
        }
    }

    private void checkUntimedOrNode() {
        TCTLOrListNode orListNode;
        if (currentSelection.getObject() instanceof TCTLStateToPathConverter) {
            TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();

            TCTLAbstractStateProperty prop = ((TCTLStateToPathConverter) currentSelection.getObject()).getProperty();

            if (prop instanceof TCTLOrListNode) {
                orListNode = new TCTLOrListNode((TCTLOrListNode) prop);
                orListNode.addDisjunct(new TCTLStatePlaceHolder());
            } else if (prop instanceof TCTLAndListNode) {
                orListNode = new TCTLOrListNode(((TCTLAndListNode) prop).getProperties());
            } else {
                orListNode = new TCTLOrListNode(getStateProperty(prop), ph);
            }

            TCTLAbstractPathProperty property = new TCTLStateToPathConverter(orListNode);
            addPropertyToQuery(property);
        } else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
            TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
            TCTLAbstractProperty oldProperty = removeExistsAllPathsFromProperty(currentSelection.getObject());

            orListNode = new TCTLOrListNode(getStateProperty(
                new TCTLPathToStateConverter((TCTLAbstractPathProperty) oldProperty)), ph);

            TCTLAbstractPathProperty property = new TCTLStateToPathConverter(orListNode);
            addPropertyToQuery(property);
        }
    }

	private void initPredicationConstructionPanel() {
		predicatePanel = new JPanel(new GridBagLayout());
		predicatePanel.setBorder(BorderFactory.createTitledBorder("Predicates"));

		placeTransitionBox = new JComboBox();
		Dimension d = new Dimension(125, 27);
		placeTransitionBox.setMaximumSize(d);
		placeTransitionBox.setPreferredSize(d);

		Vector<Object> items = new Vector<Object>(tapnNetwork.activeTemplates().size()+1);
		items.addAll(tapnNetwork.activeTemplates());
		if(tapnNetwork.numberOfSharedPlaces() > 0) items.add(SHARED);

		templateBox = new JComboBox<>(new DefaultComboBoxModel<>(items));
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
                        if (!lens.isTimed() && !lens.isGame()) {
                            for (TimedTransition transition : tapn.transitions()) {
                                if (!transition.isShared()) {
                                    placeNames.add(transition.name());
                                }
                            }
                        }

						placeNames.sort(String::compareToIgnoreCase);
						placeTransitionBox.setModel(new DefaultComboBoxModel<>(placeNames));

						currentlySelected = tapn;
						setEnablednessOfAddPredicateButton();
						if (userChangedAtomicPropSelection && placeNames.size() > 0) {
                            updateQueryOnAtomicPropositionChange();
                        }
					}
				}else{
					Vector<String> placeNames = new Vector<String>();
					for (SharedPlace place : tapnNetwork.sharedPlaces()) {
						placeNames.add(place.name());
					}
                    if (lens.isTimed() || lens.isGame()) {
                        for (SharedTransition transition : tapnNetwork.sharedTransitions()) {
                            placeNames.add(transition.name());
                        }
                    }
					placeNames.sort(String::compareToIgnoreCase);
					placeTransitionBox.setModel(new DefaultComboBoxModel<>(placeNames));

					currentlySelected = SHARED;
					setEnablednessOfAddPredicateButton();
					if (userChangedAtomicPropSelection && placeNames.size() > 0) {
                        updateQueryOnAtomicPropositionChange();
                    }
				}
                if (!lens.isTimed() && !lens.isGame()) setEnablednessOfOperatorAndMarkingBoxes();

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

        JPanel templateRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        predicatePanel.add(templateRow, gbc);
        templateBox.setPreferredSize(new Dimension(292, 27));
        templateRow.add(templateBox);

        JPanel placeRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gbc.gridy = 1;
        predicatePanel.add(placeRow, gbc);
        placeRow.add(placeTransitionBox);

        String[] relationalSymbols = { "=", "!=", "<=", "<", ">=", ">" };
        relationalOperatorBox = new JComboBox(new DefaultComboBoxModel(relationalSymbols));
        relationalOperatorBox.setPreferredSize(new Dimension(80, 27));
        placeRow.add(relationalOperatorBox);

        placeMarking = new CustomJSpinner(0);
        placeMarking.setPreferredSize(new Dimension(80, 27));
        placeRow.add(placeMarking);

        transitionIsEnabledLabel = new JLabel(" is enabled");
        transitionIsEnabledLabel.setPreferredSize(new Dimension(165, 27));
        if (!lens.isTimed() && !lens.isGame()) placeRow.add(transitionIsEnabledLabel);

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

        truePredicateButton = new JButton("True");
        truePredicateButton.setPreferredSize(new Dimension(90, 27));

        falsePredicateButton = new JButton("False");
        falsePredicateButton.setPreferredSize(new Dimension(90, 27));

        deadLockPredicateButton = new JButton("Deadlock");
        deadLockPredicateButton.setPreferredSize(new Dimension(103, 27));

        JPanel trueFalseDeadlock = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
		placeTransitionBox.setToolTipText(TOOL_TIP_PLACESBOX);
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

                if ((!lens.isTimed() && !lens.isGame()) && transitionIsSelected()) {
                    addPropertyToQuery(new TCTLTransitionNode(template, (String) placeTransitionBox.getSelectedItem()));
                } else {
                    TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode(
                        new TCTLPlaceNode(template, (String) placeTransitionBox.getSelectedItem()),
                        (String) relationalOperatorBox.getSelectedItem(),
                        new TCTLConstNode((Integer) placeMarking.getValue()));
                    addPropertyToQuery(property);
                }
			}
		});

		truePredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLTrueNode trueNode = new TCTLTrueNode();
                addPropertyToQuery(trueNode);
            }
		});

		falsePredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLFalseNode falseNode = new TCTLFalseNode();
                addPropertyToQuery(falseNode);
            }
		});

		deadLockPredicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TCTLDeadlockNode deadLockNode = new TCTLDeadlockNode();
                addPropertyToQuery(deadLockNode);
            }
		});

		placeTransitionBox.addActionListener(e -> {
			if (userChangedAtomicPropSelection) {
				updateQueryOnAtomicPropositionChange();
			}
			if (!lens.isTimed() && !lens.isGame()) {
                setEnablednessOfOperatorAndMarkingBoxes();
            }
		});

		relationalOperatorBox.addActionListener(e -> {
			if (userChangedAtomicPropSelection) {
				updateQueryOnAtomicPropositionChange();
			}

		});

		placeMarking.addChangeListener(arg0 -> {
			if (userChangedAtomicPropSelection) {
				updateQueryOnAtomicPropositionChange();
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
        JPanel row = new JPanel(new GridLayout(1, 2));
        row.add(undoButton);
        row.add(redoButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editingButtonPanel.add(row, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
        gbc.weightx = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editingButtonPanel.add(deleteButton, gbc);

		gbc.gridy = 2;
		editingButtonPanel.add(resetButton, gbc);

		gbc.gridy = 3;
		editingButtonPanel.add(editQueryButton, gbc);

		// Add action Listeners
		deleteButton.addActionListener(e -> deleteSelection());

		resetButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (queryField.isEditable()) { // in edit mode, this button is now the parse query button.
					// User has potentially altered the query, so try to parse it
					TCTLAbstractProperty newQuery = null;

					try {
					    if (queryField.getText().trim().equals("<*>")) {
                            int choice = JOptionPane.showConfirmDialog(
                                CreateGui.getApp(),
                                "It is not possible to parse an empty query.\nThe specified query has not been saved. Do you want to edit it again?",
                                "Error Parsing Query",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.ERROR_MESSAGE);
                            if (choice == JOptionPane.NO_OPTION)
                                returnFromManualEdit(null);
                            else
                                return;
                        } else if (lens.isTimed()) {
                            newQuery = TAPAALQueryParser.parse(queryField.getText());
                        } else if (queryType.getSelectedIndex() == 0) {
						    newQuery = TAPAALCTLQueryParser.parse(queryField.getText());
                        } else if (queryType.getSelectedIndex() == 1) {
					        newQuery = TAPAALLTLQueryParser.parse(queryField.getText());
                        } else {
					        throw new Exception();
                        }
					} catch (Throwable ex) {
					    String message = ex.getMessage() == null ? "TAPAAL encountered an error while trying to parse the specified query\n" :
                            "TAPAAL encountered the following error while trying to parse the specified query:\n\n"+ex.getMessage();
						int choice = JOptionPane.showConfirmDialog(
								CreateGui.getApp(),
								message+"\nWe recommend using the query construction buttons unless you are an experienced user.\n\n The specified query has not been saved. Do you want to edit it again?",
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
						VerifyPlaceNamesVisitor.Context placeContext = getPlaceContext(newQuery);
                        VerifyTransitionNamesVisitor.Context transitionContext = getTransitionContext(newQuery);

                        boolean isResultFalse;

                        if (lens.isTimed() || lens.isGame()) {
                            isResultFalse = !placeContext.getResult();
                        } else {
                            isResultFalse = !transitionContext.getResult() || !placeContext.getResult();
                        }

						if (isResultFalse) {
							StringBuilder s = new StringBuilder();

							s.append("The following places" + (lens.isTimed() ? "" : " or transitions") +
                                " were used in the query, but are not present in your model:\n\n");

							for (String placeName : placeContext.getIncorrectPlaceNames()) {
								s.append(placeName);
								s.append('\n');
							}

                            for (String transitionName : transitionContext.getIncorrectTransitionNames()) {
                                s.append(transitionName);
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

                    if (queryType.getSelectedIndex() == 1) {
                        TCTLAbstractProperty oldProperty = newProperty;
                        addAllPathsToProperty(new TCTLPathPlaceHolder(), oldProperty);
                        resetQuantifierSelectionButtons();
                        return;
                    }

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
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		queryPanel.add(editingButtonPanel, gbc);
	}

    private VerifyPlaceNamesVisitor.Context getPlaceContext(TCTLAbstractProperty newQuery) {
        // check correct place names are used in atomic propositions
        ArrayList<Tuple<String,String>> templatePlaceNames = new ArrayList<Tuple<String,String>>();
        for(TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
            for(TimedPlace p : tapn.places()) {
                if (lens.isTimed() || !p.isShared() || lens.isGame()) {
                    templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
                }
            }
        }

        for(TimedPlace p : tapnNetwork.sharedPlaces()) {
            templatePlaceNames.add(new Tuple<String, String>("", p.name()));
        }

        FixAbbrivPlaceNames.fixAbbrivPlaceNames(templatePlaceNames, newQuery);
        VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);
        return nameChecker.verifyPlaceNames(newQuery);
    }

    private VerifyTransitionNamesVisitor.Context getTransitionContext(TCTLAbstractProperty newQuery) {
        // check correct transition names are used in atomic propositions
        ArrayList<Tuple<String,String>> templateTransitionNames = new ArrayList<Tuple<String,String>>();
        for (TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
            for (TimedTransition t : tapn.transitions()) {
                if (lens.isTimed() || !t.isShared() || lens.isGame()) {
                    templateTransitionNames.add(new Tuple<>(tapn.name(), t.name()));
                }
            }
        }

        for (SharedTransition t : tapnNetwork.sharedTransitions()) {
            templateTransitionNames.add(new Tuple<>("", t.name()));
        }

        FixAbbrivTransitionNames.fixAbbrivTransitionNames(templateTransitionNames, newQuery);
        VerifyTransitionNamesVisitor nameChecker = new VerifyTransitionNamesVisitor(templateTransitionNames);
        return nameChecker.verifyTransitionNames(newQuery);
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
			button.addActionListener(e -> {
				setEnabledReductionOptions();
				setEnabledOptionsAccordingToCurrentReduction();
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

		if (lens.isTimed() || lens.isGame()) {
            gridBagConstraints.gridy = 2;
            gridBagConstraints.weightx = 1;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            traceOptionsPanel.add(fastestTraceRadioButton, gridBagConstraints);
        }

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
			button.addActionListener(e -> setEnabledOptionsAccordingToCurrentReduction());
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
        Dimension d = lens.isTimed() ? new Dimension(898, 100) : new Dimension(810, 130);
		reductionOptionsPanel.setPreferredSize(d);
		reductionOption = new JComboBox<String>();
		reductionOption.setToolTipText(TOOL_TIP_REDUCTION_OPTION);

		reductionOption.addActionListener(e -> setEnabledOptionsAccordingToCurrentReduction());

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

        useReduction = new JCheckBox("Apply net reductions");
        useSiphonTrap = new JCheckBox("Use siphon-trap analysis");
        useQueryReduction = new JCheckBox("Use query reduction");
        useStubbornReduction = new JCheckBox("Use stubborn reduction");
        symmetryReduction = new JCheckBox("Use symmetry reduction");
        discreteInclusion = new JCheckBox("Use discrete inclusion");
        selectInclusionPlacesButton = new JButton("Select Inclusion Places");
        useTimeDarts = new JCheckBox("Use Time Darts");
        useGCD = new JCheckBox("Use GCD");
        usePTrie = new JCheckBox("Use PTrie");
        useOverApproximation = new JCheckBox("Use untimed state-equations check");
        useTraceRefinement = new JCheckBox("Use trace abstraction refinement");
        useTarjan = new JCheckBox("Use Tarjan");

        useReduction.setSelected(true);
        useSiphonTrap.setSelected(false);
        useQueryReduction.setSelected(true);
        useStubbornReduction.setSelected(true);
        symmetryReduction.setSelected(true);
        discreteInclusion.setVisible(true);
        selectInclusionPlacesButton.setEnabled(false);
        useTimeDarts.setSelected(false);
        useGCD.setSelected(true);
        usePTrie.setSelected(true);
        useOverApproximation.setSelected(true);
        useTraceRefinement.setSelected(false);
        useTarjan.setSelected(true);

        useReduction.setToolTipText(TOOL_TIP_USE_STRUCTURALREDUCTION);
        useSiphonTrap.setToolTipText(TOOL_TIP_USE_SIPHONTRAP);
        useQueryReduction.setToolTipText(TOOL_TIP_USE_QUERY_REDUCTION);
        useStubbornReduction.setToolTipText(TOOL_TIP_STUBBORN_REDUCTION);
        symmetryReduction.setToolTipText(TOOL_TIP_SYMMETRY_REDUCTION);
        discreteInclusion.setToolTipText(TOOL_TIP_DISCRETE_INCLUSION);
        selectInclusionPlacesButton.setToolTipText(TOOL_TIP_SELECT_INCLUSION_PLACES);
        useTimeDarts.setToolTipText(TOOL_TIP_TIME_DARTS);
        useGCD.setToolTipText(TOOL_TIP_GCD);
        usePTrie.setToolTipText(TOOL_TIP_PTRIE);
        useOverApproximation.setToolTipText(TOOL_TIP_OVERAPPROX);
        useTraceRefinement.setToolTipText(TOOL_TIP_USE_TRACE_REFINEMENT);
        useTarjan.setToolTipText(TOOL_TIP_USE_TARJAN);

        useTarjan.addActionListener(e -> updateSearchStrategies());

        if (lens.isTimed() || lens.isGame()) {
            initTimedReductionOptions();
        } else {
            useReduction.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                        openReducedNetButton.setEnabled(useReduction.isSelected() && getQueryComment().length() > 0
                            && !newProperty.containsPlaceHolder());
                }
            });
            initUntimedReductionOptions();
        }

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 0, 10);
		add(reductionOptionsPanel, gbc);
	}

    private void initTimedReductionOptions() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0,5,0,5);
        reductionOptionsPanel.add(symmetryReduction, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        reductionOptionsPanel.add(discreteInclusion, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        reductionOptionsPanel.add(useStubbornReduction, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        reductionOptionsPanel.add(useOverApproximation, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        reductionOptionsPanel.add(selectInclusionPlacesButton, gbc);
        gbc.gridx = 3;
        gbc.gridy = 2;
        reductionOptionsPanel.add(useTimeDarts, gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        reductionOptionsPanel.add(useGCD, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        reductionOptionsPanel.add(selectInclusionPlacesButton, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        reductionOptionsPanel.add(usePTrie, gbc);

        discreteInclusion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectInclusionPlacesButton.setEnabled(discreteInclusion.isSelected());
            }
        });

        selectInclusionPlacesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inclusionPlaces = ChooseInclusionPlacesDialog.showInclusionPlacesDialog(tapnNetwork, inclusionPlaces);
            }
        });

        useTimeDarts.addActionListener(e -> setEnabledOptionsAccordingToCurrentReduction());
    }

    private void initUntimedReductionOptions() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0,5,0,5);
        reductionOptionsPanel.add(useReduction, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        reductionOptionsPanel.add(useSiphonTrap, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        reductionOptionsPanel.add(useQueryReduction, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        reductionOptionsPanel.add(useStubbornReduction, gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        reductionOptionsPanel.add(useTraceRefinement, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        reductionOptionsPanel.add(useTarjan, gbc);
    }

	protected void setEnabledOptionsAccordingToCurrentReduction() {
		refreshQueryEditingButtons();
		refreshTraceOptions();
        if (lens.isTimed() || lens.isGame()) {
            refreshSymmetryReduction();
            refreshStubbornReduction();
            refreshDiscreteOptions();
            refreshDiscreteInclusion();
            refreshOverApproximationOption();
        } else if (!lens.isTimed()) {
            refreshTraceRefinement();
            refreshTarjan();
        }
		updateSearchStrategies();
		refreshExportButtonText();
	}

	private void refreshTraceRefinement() {
	    ReductionOption reduction = getReductionOption();
	    useTraceRefinement.setEnabled(false);

	    if (queryType.getSelectedIndex() != 1 && reduction != null && reduction.equals(ReductionOption.VerifyPN) &&
            (newProperty.toString().startsWith("AG") || newProperty.toString().startsWith("EF")) &&
            !hasInhibitorArcs && !newProperty.hasNestedPathQuantifiers()) {
	        useTraceRefinement.setEnabled(true);
        }
    }

    private void refreshTarjan() {
        if (queryType.getSelectedIndex() == 1) {
            useTarjan.setVisible(true);
        } else {
            useTarjan.setVisible(false);
        }
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
		if (reduction == null) {
		    saveUppaalXMLButton.setEnabled(false);
		}
		else {
			saveUppaalXMLButton.setText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification ? EXPORT_VERIFYTAPN_BTN_TEXT : reduction == ReductionOption.VerifyPN ? EXPORT_VERIFYPN_BTN_TEXT : EXPORT_UPPAAL_BTN_TEXT);
			saveUppaalXMLButton.setToolTipText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification ? TOOL_TIP_SAVE_TAPAAL_BUTTON : reduction == ReductionOption.VerifyPN ? TOOL_TIP_SAVE_PN_BUTTON : TOOL_TIP_SAVE_UPPAAL_BUTTON);
			saveUppaalXMLButton.setEnabled(true);
		}
	}

	private void refreshQueryEditingButtons() {
		if(currentSelection != null) {
            if (lens.isGame()) {
                if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
                    forAllBox.setSelected(false);
                    enableOnlyForAll();
                } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    enableOnlyStateButtons();
                }
            } else if (lens.isTimed()) {
                if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
                    enableOnlyPathButtons();
                } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    enableOnlyStateButtons();
                }
                updateQueryButtonsAccordingToSelection();
            } else {
                enableOnlyUntimedStateButtons();
                updateQueryButtonsAccordingToSelection();
            }
		}
	}

	private void refreshSymmetryReduction() {
		if(disableSymmetryUpdate){
			return;
		}
		else if(reductionOption.getSelectedItem() == null){
			symmetryReduction.setVisible(false);
		}
		else if(reductionOption.getSelectedItem().equals(name_DISCRETE) || reductionOption.getSelectedItem().equals(name_UNTIMED)) {
			symmetryReduction.setVisible(true);
			symmetryReduction.setEnabled(false);
		}
		else if((reductionOption.getSelectedItem().equals(name_COMBI) ||
				reductionOption.getSelectedItem().equals(name_OPTIMIZEDSTANDARD) ||
				reductionOption.getSelectedItem().equals(name_STANDARD) ||
				reductionOption.getSelectedItem().equals(name_BROADCAST) ||
				reductionOption.getSelectedItem().equals(name_BROADCASTDEG2)) &&
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
	    if (queryHasDeadlock() || newProperty.toString().contains("EG") || newProperty.toString().contains("AF")){
			useOverApproximation.setSelected(false);
			useOverApproximation.setEnabled(false);
		} else {
			if(!useOverApproximation.isEnabled()){
				useOverApproximation.setSelected(true);
			}
			useOverApproximation.setEnabled(true);
		}

        if (lens.isGame()) {
            noApproximationEnable.setEnabled(true);
            overApproximationEnable.setEnabled(false);
            underApproximationEnable.setEnabled(false);
            overApproximationDenominator.setEnabled(false);
        } else if(fastestTraceRadioButton.isSelected()){
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
		else if(reductionOption.getSelectedItem().equals(name_DISCRETE)) {
			useGCD.setVisible(true);
			usePTrie.setVisible(true);
			useStubbornReduction.setVisible(true);
			useTimeDarts.setVisible(true);

			if(tapnNetwork.hasUrgentTransitions() || fastestTraceRadioButton.isSelected() || lens.isGame()){
				hasForcedDisabledTimeDarts = useTimeDarts.isSelected();
				useTimeDarts.setSelected(false);
				useTimeDarts.setEnabled(false);
			}

			// Disable GCD calculation for EG/AF or deadlock queries
			if(queryHasDeadlock() || newProperty.toString().contains("EG") || newProperty.toString().contains("AF") ||
               lens.isGame()){
				if(useGCD.isSelected())	hasForcedDisabledGCD = true;
				useGCD.setSelected(false);
				useGCD.setEnabled(false);
			}

			// Disable time darts for EG/AF with deadlock
			if(queryHasDeadlock() && (newProperty.toString().contains("EG") || newProperty.toString().contains("AF"))){
				hasForcedDisabledTimeDarts = useTimeDarts.isSelected();
				useTimeDarts.setSelected(false);
				useTimeDarts.setEnabled(false);
				symmetryReduction.setSelected(false);
				symmetryReduction.setEnabled(false);
			}

			// Disable stubborn reduction for EG/AF queries
			if(newProperty.toString().contains("EG") || newProperty.toString().contains("AF")){
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

	private void updateLTLButtons() {
        if (currentSelection.getObject() == newProperty) {
            String ltlType = checkLTLType();
            disableAllLTLButtons();
            if (ltlType.equals("placeholder")) {
                aButton.setEnabled(true);
                eButton.setEnabled(true);
            } else if (ltlType.equals("A")) {
                eButton.setEnabled(true);
            } else {
                aButton.setEnabled(true);
            }
        } else {
            aButton.setEnabled(false);
            eButton.setEnabled(false);
            globallyButton.setEnabled(true);
            finallyButton.setEnabled(true);
            nextButton.setEnabled(true);
            untilButton.setEnabled(true);
        }
    }


	private void queryChanged(){
        setEnabledReductionOptions();
        if (lens.isTimed()) refreshOverApproximationOption();
        if (queryType.getSelectedIndex() == 1) {
            updateLTLButtons();
        }
	}

	private void initButtonPanel(QueryDialogueOption option) {
		buttonPanel = new JPanel(new BorderLayout());
		if (option == QueryDialogueOption.Save) {
			saveButton = new JButton("Save");
			saveAndVerifyButton = new JButton("Save and Verify");
			cancelButton = new JButton("Cancel");

			mergeNetComponentsButton = new JButton(EXPORT_COMPOSED_BTN_TEXT);
			mergeNetComponentsButton.setVisible(false);

			openReducedNetButton = new JButton(OPEN_REDUCED_BTN_TEXT);
            openReducedNetButton.setVisible(false);


			saveUppaalXMLButton = new JButton(EXPORT_UPPAAL_BTN_TEXT);
			//Only show in advanced mode
			saveUppaalXMLButton.setVisible(false);

			//Add tool tips
			saveButton.setToolTipText(TOOL_TIP_SAVE_BUTTON);
			saveAndVerifyButton.setToolTipText(TOOL_TIP_SAVE_AND_VERIFY_BUTTON);
			cancelButton.setToolTipText(TOOL_TIP_CANCEL_BUTTON);
			saveUppaalXMLButton.setToolTipText(TOOL_TIP_SAVE_UPPAAL_BUTTON);
			mergeNetComponentsButton.setToolTipText(TOOL_TIP_SAVE_COMPOSED_BUTTON);
            openReducedNetButton.setToolTipText(TOOL_TIP_OPEN_REDUCED_BUTTON);

			saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					// TODO make save
					// save();
					if (checkIfSomeReductionOption()) {
						querySaved = true;
						// Now if a query is saved, the net is marked as modified
						CreateGui.getCurrentTab().setNetChanged(true);
						exit();
					}
				}
			});
			saveAndVerifyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkIfSomeReductionOption()) {
						querySaved = true;
						// Now if a query is saved and verified, the net is marked as modified
						CreateGui.getCurrentTab().setNetChanged(true);
						exit();
						TAPNQuery query = getQuery();

						if(query.getReductionOption() == ReductionOption.VerifyTAPN || query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification || query.getReductionOption() == ReductionOption.VerifyPN)
							Verifier.runVerifyTAPNVerification(tapnNetwork, query, false, null);
						else
							Verifier.runUppaalVerification(tapnNetwork, query);
					}}
			});
			cancelButton.addActionListener(evt -> exit());

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
                        if (!lens.isTimed()) {
                            RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(transformedModel.value2());
                            clonedQuery.getProperty().accept(transitionVisitor, null);
                        }
						if(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyTAPNdiscreteVerification) {
							VerifyTAPNExporter exporter = new VerifyTAPNExporter();
							exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile), tapnQuery, lens, transformedModel.value2());

						} else if(reduction == ReductionOption.VerifyPN){
							VerifyPNExporter exporter = new VerifyPNExporter();
							exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile), tapnQuery, lens, transformedModel.value2());

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

			mergeNetComponentsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, true, true);
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
						underaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator(), composer.getGuiModel());
					}
					templates.add(new Template(transformedModel.value1(), composer.getGuiModel(), new Zoomer()));

					// Create a constant store
					ConstantStore newConstantStore = new ConstantStore();


					TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(newConstantStore);

					network.add(transformedModel.value1());

					NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<pipe.dataLayer.TAPNQuery>(0), new ArrayList<Constant>(0), lens);

					try {
						ByteArrayOutputStream outputStream = tapnWriter.savePNML();
						String composedName = "composed-" + CreateGui.getApp().getCurrentTabName();
						composedName = composedName.replace(".tapn", "");
						CreateGui.openNewTabFromStream(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
						exit();
					} catch (Exception e1) {
						System.console().printf(e1.getMessage());
					}
				}
			});

            openReducedNetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {


                    if (checkIfSomeReductionOption()) {
                        querySaved = true;
                        // Now if a query is saved and verified, the net is marked as modified
                        CreateGui.getCurrentTab().setNetChanged(true);

                        TAPNQuery query = getQuery();
                        if(query.getReductionOption() != ReductionOption.VerifyPN) {
                            JOptionPane.showMessageDialog(CreateGui.getApp(),
                                "The selected verification engine does not support application of reduction rules",
                                "Reduction rules unsupported", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        exit();

                        Verifier.runVerifyTAPNVerification(tapnNetwork, query,true, null);

                        File reducedNetFile = new File(Verifier.getReducedNetFilePath());

                        if(reducedNetFile.exists() && reducedNetFile.isFile() && reducedNetFile.canRead()){
                            try {
                                TabContent reducedNetTab = TabContent.createNewTabFromPNMLFile(reducedNetFile);
                                //Ensure that a net was created by the query reduction
                                if(reducedNetTab.currentTemplate().guiModel().getPlaces().length  > 0
                                    || reducedNetTab.currentTemplate().guiModel().getTransitions().length > 0){
                                    reducedNetTab.setInitialName("reduced-" + CreateGui.getAppGui().getCurrentTabName());
                                    TAPNQuery convertedQuery = query.convertPropertyForReducedNet(reducedNetTab.currentTemplate().toString());
                                    reducedNetTab.addQuery(convertedQuery);
                                    CreateGui.openNewTabFromStream(reducedNetTab);
                                }
                            } catch (Exception e1){
                                JOptionPane.showMessageDialog(CreateGui.getApp(),
                                    e1.getMessage(),
                                    "Error loading reduced net file",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });


		} else if (option == QueryDialogueOption.Export) {
			saveButton = new JButton("export");
			cancelButton = new JButton("Cancel");

			saveButton.addActionListener(evt -> {
				querySaved = true;
				exit();
			});
			cancelButton.addActionListener(evt -> exit());
		}

		if (option == QueryDialogueOption.Save) {
			JPanel leftButtomPanel = new JPanel(new FlowLayout());
			JPanel rightButtomPanel = new JPanel(new FlowLayout());
			leftButtomPanel.add(mergeNetComponentsButton, FlowLayout.LEFT);
			leftButtomPanel.add(openReducedNetButton, FlowLayout.LEFT);
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

	private static class QueryConstructionUndoManager extends UndoManager {
		public UndoableEdit GetNextEditToUndo() {
			return editToBeUndone();
		}

		public UndoableEdit GetNextEditToRedo() {
			return editToBeRedone();
		}
	}

	public class QueryConstructionEdit extends AbstractUndoableEdit {
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

    private void makeShortcuts(){
        int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        ActionMap am = this.getActionMap();
        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoButton.doClick();
            }
        });
        am.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redoButton.doClick();
            }
        });
        InputMap im = this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke('Z', shortcutkey), "undo");
        im.put(KeyStroke.getKeyStroke('Y', shortcutkey), "redo");
    }

}
