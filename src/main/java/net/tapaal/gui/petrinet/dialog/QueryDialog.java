package net.tapaal.gui.petrinet.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.event.FocusAdapter;

import dk.aau.cs.TCTL.HyperLTLPathScopeNode;
import dk.aau.cs.TCTL.LTLANode;
import dk.aau.cs.TCTL.LTLENode;
import dk.aau.cs.TCTL.LTLFNode;
import dk.aau.cs.TCTL.LTLGNode;
import dk.aau.cs.TCTL.LTLUNode;
import dk.aau.cs.TCTL.LTLXNode;
import dk.aau.cs.TCTL.StringPosition;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAUNode;
import dk.aau.cs.TCTL.TCTLAXNode;
import dk.aau.cs.TCTL.TCTLAbstractPathProperty;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLEUNode;
import dk.aau.cs.TCTL.TCTLEXNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLPathToStateConverter;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLStateToPathConverter;
import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.CTLParsing.TAPAALCTLQueryParser;
import dk.aau.cs.TCTL.HyperLTLParsing.TAPAALHyperLTLQueryParser;
import dk.aau.cs.TCTL.LTLParsing.TAPAALLTLQueryParser;
import dk.aau.cs.verification.observations.Observation;
import net.tapaal.helpers.Enabler;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.SMCParsing.TAPAALSMCQueryParser;
import dk.aau.cs.TCTL.visitors.FixAbbrivPlaceNames;
import dk.aau.cs.TCTL.visitors.FixAbbrivTransitionNames;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.TCTL.visitors.HyperLTLTraceNameVisitor;
import dk.aau.cs.TCTL.visitors.IsReachabilityVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.TCTL.visitors.VerifyTransitionNamesVisitor;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.io.NetWriter;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.SMCSettings;
import dk.aau.cs.verification.SMCStats;
import dk.aau.cs.verification.SMCTraceType;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.UPPAAL.UppaalExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyCPNExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.undo.AddQueryCommand;
import net.tapaal.gui.petrinet.verification.ChooseInclusionPlacesDialog;
import net.tapaal.gui.petrinet.verification.EngineSupportOptions;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
import net.tapaal.gui.petrinet.verification.RunVerificationBase;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType;
import net.tapaal.gui.petrinet.verification.UPPAALBroadcastDegree2Options;
import net.tapaal.gui.petrinet.verification.UPPAALBroadcastOptions;
import net.tapaal.gui.petrinet.verification.UPPAALCombiOptions;
import net.tapaal.gui.petrinet.verification.UPPAALOptimizedStandardOptions;
import net.tapaal.gui.petrinet.verification.UPPAALStandardOptions;
import net.tapaal.gui.petrinet.verification.Verifier;
import net.tapaal.gui.petrinet.verification.VerifyDTAPNEngineOptions;
import net.tapaal.gui.petrinet.verification.VerifyPNEngineOptions;
import net.tapaal.gui.petrinet.verification.VerifyTAPNEngineOptions;
import net.tapaal.swinghelpers.CustomJSpinner;
import pipe.gui.MessengerImpl;
import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.SearchBar;
import pipe.gui.petrinet.Searcher;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.swingcomponents.EscapableDialog;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;

public class QueryDialog extends JPanel {
    private static final String NO_UPPAAL_XML_FILE_SAVED = "No Uppaal XML file saved.";
	private static final String NO_VERIFYTAPN_XML_FILE_SAVED = "No verifytapn XML file saved.";
	private static final String UNSUPPORTED_MODEL_TEXT = "The model is not supported by the chosen reduction.";
	private static final String UNSUPPORTED_QUERY_TEXT = "The query is not supported by the chosen reduction.";
	private static final String EXPORT_UPPAAL_BTN_TEXT = "Export UPPAAL XML";
	private static final String EXPORT_VERIFYTAPN_BTN_TEXT = "Export TAPAAL XML";
	private static final String EXPORT_VERIFYPN_BTN_TEXT = "Export PN XML";
	private static final String EXPORT_COMPOSED_BTN_TEXT = "Merge net components";
    private static final String OPEN_REDUCED_BTN_TEXT = "Open reduced net";
    public static final String UPDATE_VERIFICATION_TIME_BTN_TEXT = "Compute estimated verification time";
    public static final String UPDATE_PRECISION_BTN_TEXT = "Compute precision for the given verification time";

	private static final String UPPAAL_SOME_TRACE_STRING = "Some trace       ";
	private static final String SHARED = "Shared";

    public enum QueryDialogueOption {
		VerifyNow, Save, Export
	}

	private boolean querySaved = false;

	private final JRootPane rootPane;
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
    private SearchBar searchBar;
    private Searcher<Tuple<?, String>> searcher;
    private JComboBox templateBox;
    private JComboBox traceBox;
    private JComboBox traceBoxQuantification;
    private JComboBox<String> placeTransitionBox;
    private JComboBox<String> relationalOperatorBox;
    private JLabel transitionIsEnabledLabel;
    private CustomJSpinner placeMarking;
    private JButton addTraceButton;
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

    // Trace panel
    private JTextField traceTextField;
    private JButton addTracePanelButton;
    private JTextField traceNameTextField;
    private DefaultListModel traceModel;
    private DefaultListModel tempTraceModel;
    private JButton traceRemoveButton;
    private JList traceList;
    private JList tempTraceList;
    private JButton okButton;
    private EscapableDialog traceDialog;
    private JPanel traceRow;

    // Trace options panel
    private JPanel traceOptionsPanel;


    private ButtonGroup traceRadioButtonGroup;
    private JRadioButton noTraceRadioButton;
    private JRadioButton someTraceRadioButton;
    private JRadioButton fastestTraceRadioButton;

    // Unfolding options panel
    private JPanel verificationPanel;
    private JPanel unfoldingOptionsPanel;
    private JCheckBox usePartitioning;
    private JCheckBox useSymmetricvars;
    private JCheckBox useColorFixpoint;
    // Reduction options panel
    private JPanel reductionOptionsPanel;
    private JComboBox<String> reductionOption;
    private JCheckBox symmetryReduction;
    private JCheckBox discreteInclusion;
    private JButton selectInclusionPlacesButton;
    private JCheckBox useTimeDarts;
    private JCheckBox usePTrie;
    private JCheckBox useGCD;
    private JCheckBox skeletonAnalysis;
    private JCheckBox useSiphonTrap;
    private JCheckBox useQueryReduction;
    private JCheckBox useReduction;
    private JCheckBox useColoredReduction;
	private JCheckBox useStubbornReduction;
    private JCheckBox useTraceRefinement;
    private JCheckBox useTarjan;
    // Raw verification options panel
    private JPanel rawVerificationOptionsPanel;
    private JTextArea rawVerificationOptionsTextArea;
    private JCheckBox rawVerificationOptionsEnabled;
    private JButton rawVerificationOptionsHelpButton;

    // Approximation options panel
    private JPanel overApproximationOptionsPanel;
    private ButtonGroup approximationRadioButtonGroup;
    private JRadioButton noApproximationEnable;
    private JRadioButton overApproximationEnable;
    private JRadioButton underApproximationEnable;
    private CustomJSpinner overApproximationDenominator;

    // SMC options panel
    private JPanel smcSettingsPanel;
    private JComboBox<String> smcVerificationType;
    private JLabel smcVerificationTypeLabel;
    private JTextField smcTimeBoundValue;
    private JCheckBox smcTimeBoundInfinite;
    private JTextField smcStepBoundValue;
    private JCheckBox smcStepBoundInfinite;
    private JLabel smcNumericPrecisionLabel;
    private CustomJSpinner smcNumericPrecision;
    private JPanel quantitativePanel;
    private JLabel smcParallelLabel;
    private JCheckBox smcParallel;
    private JTextField smcConfidence;
    private QuerySlider smcConfidenceSlider;
    private QuerySlider smcPrecisionSlider;
    private QuerySlider smcEstimatedTimeSlider;
    private JTextField smcEstimationIntervalWidth;
    private JTextField smcTimeExpected;
    private JButton smcTimeEstimationButton;
    private JPanel qualitativePanel;
    private JTextField smcFalsePositives;
    private JTextField smcFalseNegatives;
    private JTextField smcIndifference;
    private JTextField smcComparisonFloat;
    private QuerySlider smcFalsePositivesSlider;
    private QuerySlider smcFalseNegativesSlider;
    private QuerySlider smcIndifferenceSlider;
    private QuerySlider smcComparisonFloatSlider;
    private JPanel smcTracePanel;
    private CustomJSpinner smcNumberOfTraces;
    private JComboBox<SMCTraceType> smcTraceType;
    private SMCSettings smcSettings;
    private boolean updatingSmcSettings = false;
    private boolean smcMustUpdateTime = true;
    private boolean doingBenchmark = false;
    private RunVerificationBase benchmarkThread = null;
    private List<Observation> smcObservations;
    
    private JTextField smcGranularityField;
    private JCheckBox smcMaxGranularityCheckbox;

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
    private final boolean isNetDegree2;
    private final int highestNetDegree;
    private final boolean hasInhibitorArcs;
    private InclusionPlaces inclusionPlaces;
    private final TAPNLens lens;
    private final PetriNetTab tab;

	private static final String name_verifyTAPN = "TAPAAL: Continuous Engine (verifytapn)";
	private static final String name_COMBI = "UPPAAL: Optimized Broadcast Reduction";
	private static final String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimized Standard Reduction";
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
    private final static EngineSupportOptions UPPAALStandardOptions = new UPPAALStandardOptions();
    private final static EngineSupportOptions UPPAALBroadcastOptions = new UPPAALBroadcastOptions();
    private final static EngineSupportOptions UPPAALBroadcastDegree2Options = new UPPAALBroadcastDegree2Options();
    private final static EngineSupportOptions verifyDTAPNOptions= new VerifyDTAPNEngineOptions();
    private final static EngineSupportOptions verifyPNOptions = new VerifyPNEngineOptions();

    private final static EngineSupportOptions[] engineSupportOptions = new EngineSupportOptions[]{verifyDTAPNOptions,verifyTAPNOptions,UPPAALCombiOptions,UPPAALOptimizedStandardOptions,UPPAALStandardOptions,UPPAALBroadcastOptions,UPPAALBroadcastDegree2Options,verifyPNOptions};

    private TCTLAbstractProperty newProperty;
    private TCTLAbstractProperty previousProp;
    private JTextField queryName;

    private static Boolean advancedView = false;

    private static boolean hasForcedDisabledTimeDarts = false;
    private static boolean hasForcedDisabledStubbornReduction = false;
    private static boolean hasForcedDisabledGCD = false;
    private static boolean disableSymmetryUpdate = false;
    private boolean wasCTLType = true;
    private boolean wasLTLType = true;
    private boolean wasHyperLTLType = true;
    private boolean isAllPath = false;
    private boolean isExistsPath = false;
    private boolean updateTraceBox = true;
    private boolean updateTraceBoxQuantification = true;

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
	private static final String TOOL_TIP_FORALL_BOX = "Check if every reachable marking in the net satisfies the given property.";

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

    // Tool tips for trace panel
    private static final String TOOL_TIP_TRACEBOX = "Choose a trace for this predicate.";
    private static final String TOOL_TIP_TRACEBOX_QUANTIFICATION = "Choose a trace for quantification.";
    private static final String TOOL_TIP_ADDTRACEBUTTON = "Add or remove new traces";

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
    private final static String TOOL_TIP_USE_COLORED_STRUCTURALREDUCTION = "Apply colored structural reductions to reduce the size of the net.";
    private final static String TOOL_TIP_USE_SIPHONTRAP = "For a deadlock query, attempt to prove deadlock-freedom by using siphon-trap analysis via linear programming.";
    private final static String TOOL_TIP_USE_QUERY_REDUCTION = "Use query rewriting rules and linear programming (state equations) to reduce the size of the query.";
    private final static String TOOL_TIP_USE_TRACE_REFINEMENT = "Enables Trace Abstraction Refinement for reachability properties";
    private final static String TOOL_TIP_USE_TARJAN= "Uses the Tarjan algorithm when verifying. If not selected it will verify using the nested DFS algorithm.";

    // Tool tips for raw verification options panel
    private final static String TOOL_TIP_RAW_VERIFICATION_ENABLED_CHECKBOX = "Enable verification options for the engine.";
    private final static String TOOL_TIP_RAW_VERIFICATION_TEXT_FIELD = "Enter verification options for the engine.";
    private final static String TOOL_TIP_RAW_VERIFICATION_HELP_BUTTON = "Show engine options.";

    //Tool tips for unfolding options panel
    private final static String TOOL_TIP_PARTITIONING = "Partitions the colors into logically equivalent groups before unfolding";
    private final static String TOOL_TIP_COLOR_FIXPOINT = "Explores the possible colored markings and only unfolds for those";
    private final static String TOOL_TIP_SYMMETRIC_VARIABLES = "Finds variables with equivalent behavior and treats them as the same variable";

    //Tool tips for search options panel
	private final static String TOOL_TIP_HEURISTIC_SEARCH = "<html>Uses a heuristic method in state space exploration.<br />" +
			"If heuristic search is not applicable, BFS is used instead.<br/>For reachability queries, uses an improved heuristic search with randomness.<br/>" +
            "Click the button <em>Help on the query options</em> to get more info.</html>";
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

    //Tool tips for SMC panel
    private final static String TOOL_TIP_ANALYSIS_TYPE = "Choose between probability quantitative estimation, qualitative hypothesis testing against a fixed probability, or trace generation.";
    private final static String TOOL_TIP_TIME_BOUND = "Bound each run by a maximum accumulated delay";
    private final static String TOOL_TIP_STEP_BOUND = "Bound each run by a maximum number of transition firings";
    private final static String TOOL_TIP_NUMERIC_PRECISION = "The number of digits (from 1 to 18) after the decimal point used in sampling from distributions.";
    private final static String TOOL_TIP_CONFIDENCE = "Between 0 and 1, confidence that the probability is indeed in the computed interval";
    private final static String TOOL_TIP_INTERVAL_WIDTH = "Between 0 and 1, error E of the computed probability (P±E)";
    private final static String TOOL_TIP_FALSE_POSITIVES = "Probability to accept the hypothesis if it is false";
    private final static String TOOL_TIP_FALSE_NEGATIVES = "Probability to reject the hypothesis if it is true";
    private final static String TOOL_TIP_INDIFFERENCE = "Width of the indifference region used as a threshold by the algorithm";
    private final static String TOOL_TIP_VERIFICATION_TIME = "Total estimated time (in seconds) to run the verification with the given confidence and precision";
    private final static String TOOL_TIP_QUALITATIVE_TEST = "Probability threshold to be tested";
    private final static String TOOL_TIP_N_TRACES = "Number of traces to be shown";
    private final static String TOOL_TIP_TRACE_TYPE = "Specifies the type of traces to be shown";

    private final static String TOOL_TIP_GRANULARITY = "Uses the given granularity for observations";

    private QueryDialog(EscapableDialog me, QueryDialogueOption option, TAPNQuery queryToCreateFrom, TimedArcPetriNetNetwork tapnNetwork, HashMap<TimedArcPetriNet, DataLayer> guiModels, TAPNLens lens, PetriNetTab tab) {
        this.tapnNetwork = tapnNetwork;
        this.guiModels = guiModels;
        this.lens = lens;
        this.tab = tab;
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
        if (lens.isStochastic()) toggleSmc();
    }

    private boolean checkIfSomeReductionOption() {
        if (reductionOption.getSelectedItem() == null){
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
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
        int oldCapacity = getCapacity();
        int capacity = oldCapacity;

        if (rawVerificationOptionsEnabled.isSelected()) {
            ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
            Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(QueryDialog.this.tapnNetwork);
            int tokensInModel = transformedModel.value1().getNumberOfTokensInNet();

            String rawOptions = rawVerificationOptionsTextArea.getText();
            Pattern pattern = Pattern.compile("(--k-bound|-k)\\s+(\\d+)");
            Matcher matcher = pattern.matcher(rawOptions);
    
            if (matcher.find()) {
                int totalTokens = Integer.parseInt(matcher.group(2));
                capacity = totalTokens - tokensInModel;
            }
        }

        TAPNQuery.TraceOption traceOption = getTraceOption();
        TAPNQuery.SearchOption searchOption = getSearchOption();
        ReductionOption reductionOptionToSet = getReductionOption();

        ArrayList<String> traceList = new ArrayList<>();
        if(queryType.getSelectedIndex() == 2) {
            for (int i = 0; i < traceModel.getSize(); i++) {
                traceList.add(traceModel.get(i).toString());
            }
        }

        if (!lens.isTimed()) {
            return getUntimedQuery(name, traceList, capacity, oldCapacity, traceOption, searchOption, reductionOptionToSet);
        } else {
            return getTimedQuery(name, capacity, oldCapacity, traceOption, searchOption, reductionOptionToSet);
        }
    }

    private TAPNQuery getTimedQuery(String name, int capacity, int oldCapacity, TraceOption traceOption, SearchOption searchOption, ReductionOption reductionOptionToSet) {
        boolean symmetry = getSymmetry();
		boolean timeDarts = useTimeDarts.isSelected();
		boolean pTrie = usePTrie.isSelected();
		boolean gcd = useGCD.isSelected() && !lens.isStochastic();
		boolean overApproximation = skeletonAnalysis.isSelected() && !lens.isStochastic();
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
            (Integer) overApproximationDenominator.getValue(),
            false,   //usePartitioning.isSelected(),
            false,   //useColorFixpoint.isSelected(),
            false,   //useSymmetricVars.isSelected()
            lens.isColored(),
            false,
            rawVerificationOptionsEnabled.isSelected(),
            rawVerificationOptionsTextArea.getText()
		);

        query.setOldCapacity(oldCapacity);

        query.setUseStubbornReduction(useStubbornReduction.isSelected());

        if (reductionOptionToSet != null && reductionOptionToSet.equals(ReductionOption.VerifyTAPN)) {
            query.setDiscreteInclusion(discreteInclusion.isSelected());
        }

        if(lens.isStochastic()) {
            query.setCategory(TAPNQuery.QueryCategory.SMC);
            query.setParallel(smcParallel.isSelected());
            VerificationType verificationType = VerificationType.fromOrdinal(smcVerificationType.getSelectedIndex());
            if (verificationType.equals(VerificationType.SIMULATE)) {
                SMCSettings newSettings = SMCSettings.Default();
                SMCSettings oldSettings = getSMCSettings();
                newSettings.setStepBound(oldSettings.getStepBound());
                newSettings.setTimeBound(oldSettings.getTimeBound());
                newSettings.setObservations(oldSettings.getObservations());
                newSettings.setNumericPrecision(oldSettings.getNumericPrecision());
                query.setSmcSettings(newSettings);
            } else {
                query.setSmcSettings(getSMCSettings());
            }
            
            query.setVerificationType(verificationType);
            query.setNumberOfTraces((Integer)smcNumberOfTraces.getValue());
            query.setSmcTraceType((SMCTraceType)smcTraceType.getSelectedItem());

            try {
                query.setGranularity(Integer.parseInt(smcGranularityField.getText()));
            } catch (NumberFormatException e) {}

            query.setMaxGranularity(smcMaxGranularityCheckbox.isSelected());
        }

        return query;
    }

    private TAPNQuery getUntimedQuery(String name, ArrayList<String> traceList, int capacity, int oldCapacity, TraceOption traceOption, SearchOption searchOption, ReductionOption reductionOptionToSet) {
        boolean reduction = useReduction.isSelected();
        boolean coloredReduction = useColoredReduction.isSelected();

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
            0,
            lens.isColored()? usePartitioning.isSelected(): false,
            lens.isColored()? useColorFixpoint.isSelected() : false,
            lens.isColored()? useSymmetricvars.isSelected() : false,
            lens.isColored(),
            coloredReduction,
            rawVerificationOptionsEnabled.isSelected(),
            rawVerificationOptionsTextArea.getText()
        );
        if (queryType.getSelectedIndex() == 1) {
            query.setCategory(TAPNQuery.QueryCategory.LTL);
        } else if (queryType.getSelectedIndex() == 2) {
            query.setCategory(TAPNQuery.QueryCategory.HyperLTL);
            query.setTraceList(traceList);
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

        if (depthFirstSearch.isSelected()) {
            return SearchOption.DFS;
        } else if (randomSearch.isSelected()) {
            return SearchOption.RANDOM;
        } else if (heuristicSearch.isSelected()) {
            if (!lens.isTimed() && !lens.isGame() && isReachabilityQuery())
                return SearchOption.RANDOMHEURISTIC;
            return SearchOption.HEURISTIC;
        } else if (breadthFirstSearch.isSelected()) {
            return SearchOption.BFS;
        } else {
            return SearchOption.DEFAULT;
        }
	}

    private ReductionOption getReductionOption() {
        String reductionOptionString = getReductionOptionAsString();

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
            return ReductionOption.VerifyDTAPN;
        else if (reductionOptionString.equals(name_UNTIMED))
            return ReductionOption.VerifyPN;
        else
            return ReductionOption.BROADCAST;
    }

    private String getReductionOptionAsString() {
        return (String)reductionOption.getSelectedItem();
    }

    private void refreshTraceOptions() {
        if (reductionOption.getSelectedItem() == null || rawVerificationOptionsEnabled.isSelected()) {
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
        } else if (queryIsReachability() || queryType.getSelectedIndex() == 1 || queryType.getSelectedIndex() == 2) {
            fastestTraceRadioButton.setEnabled(false);
            someTraceRadioButton.setEnabled(true);
            noTraceRadioButton.setEnabled(true);
        } else {
            fastestTraceRadioButton.setEnabled(false);
            someTraceRadioButton.setEnabled(false);
            noTraceRadioButton.setEnabled(false);
            noTraceRadioButton.setSelected(true);
        }

		if (getTraceOption() == TraceOption.FASTEST) {
			if (fastestTraceRadioButton.isEnabled()) {
				fastestTraceRadioButton.setSelected(true);
			} else if (someTraceRadioButton.isEnabled()) {
                someTraceRadioButton.setSelected(true);
            } else {
                noTraceRadioButton.setSelected(true);
            }
		}
	}

    private void updateSMCSettings() {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat precisionFormat = new DecimalFormat("#.#####", decimalFormatSymbols);
        smcSettings.compareToFloat = smcVerificationType.getSelectedIndex() == 1;
        try {
            smcSettings.timeBound = smcTimeBoundInfinite.isSelected() ?
                Integer.MAX_VALUE :
                Integer.parseInt(smcTimeBoundValue.getText());
        } catch(NumberFormatException e) {
            smcSettings.timeBound = 1000;
            smcTimeBoundValue.setText("1000");
        }
        smcTimeBoundValue.setEnabled(!smcTimeBoundInfinite.isSelected() && !doingBenchmark);
        try {
            smcSettings.stepBound = smcStepBoundInfinite.isSelected() ?
                Integer.MAX_VALUE :
                Integer.parseInt(smcStepBoundValue.getText());
        } catch(NumberFormatException e) {
            smcSettings.stepBound = 1000;
            smcStepBoundValue.setText("1000");
        }
        smcStepBoundValue.setEnabled(!smcStepBoundInfinite.isSelected());
        smcTimeBoundInfinite.setEnabled(!smcStepBoundInfinite.isSelected());
        smcStepBoundInfinite.setEnabled(!smcTimeBoundInfinite.isSelected());

        smcSettings.setNumericPrecision(((Integer)smcNumericPrecision.getValue()).longValue());

        try {
            smcSettings.confidence = Float.parseFloat(smcConfidence.getText());
        } catch(NumberFormatException e) {
            smcConfidence.setText(String.valueOf(smcSettings.confidence));
        }
        try {
            smcSettings.estimationIntervalWidth = (doingBenchmark && !smcMustUpdateTime) ?
                0.01f : Float.parseFloat(smcEstimationIntervalWidth.getText());
        } catch(NumberFormatException e) {
            smcEstimationIntervalWidth.setText(precisionFormat.format(smcSettings.estimationIntervalWidth));
        }
        try {
            smcSettings.falsePositives = Float.parseFloat(smcFalsePositives.getText());
        } catch(NumberFormatException e) {
            smcFalsePositives.setText(String.valueOf(smcSettings.falsePositives));
        }
        try {
            smcSettings.falseNegatives = Float.parseFloat(smcFalseNegatives.getText());
        } catch(NumberFormatException e) {
            smcFalseNegatives.setText(String.valueOf(smcSettings.falseNegatives));
        }
        try {
            smcSettings.indifferenceWidth = Float.parseFloat(smcIndifference.getText());
        } catch(NumberFormatException e) {
            smcIndifference.setText(String.valueOf(smcSettings.indifferenceWidth));
        }
        try {
            smcSettings.geqThan = Float.parseFloat(smcComparisonFloat.getText());
        } catch(NumberFormatException e) {
            smcComparisonFloat.setText(String.valueOf(smcSettings.geqThan));
        }
    }

    private SMCSettings getSMCSettings() {
        updateSMCSettings();
        return smcSettings;
    }

    private void setSMCSettings(SMCSettings settings) {
        updatingSmcSettings = true;
        smcSettings = settings;

        double desiredMinConfidence = smcConfidenceSlider.getDesiredMin();
        double desiredMaxConfidence = smcConfidenceSlider.getDesiredMax();
        double initialProportionConfidence = (settings.confidence - desiredMinConfidence) / (desiredMaxConfidence - desiredMinConfidence);
        int initialValueConfidence = (int) (initialProportionConfidence * smcConfidenceSlider.getMaximum());
        smcConfidenceSlider.setValue(
            Math.max(smcConfidenceSlider.getMinimum(), 
                    Math.min(initialValueConfidence, smcConfidenceSlider.getMaximum())));

        double desiredMinPrecision = smcPrecisionSlider.getDesiredMin();
        double desiredMaxPrecision = smcPrecisionSlider.getDesiredMax();
        double logMin = Math.log(desiredMinPrecision);
        double logMax = Math.log(desiredMaxPrecision);
        double initialProportionPrecision = (Math.log(settings.estimationIntervalWidth) - logMin) / (logMax - logMin);
        int initialValuePrecision = (int) (initialProportionPrecision * smcPrecisionSlider.getMaximum());
        
        smcPrecisionSlider.setValue(
            Math.max(smcPrecisionSlider.getMinimum(), 
                    Math.min(initialValuePrecision, smcPrecisionSlider.getMaximum())));
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat precisionFormat = new DecimalFormat("#.#####", decimalFormatSymbols);

        smcVerificationType.setSelectedIndex(settings.compareToFloat ? 1 : 0);
        smcTimeBoundValue.setText(smcSettings.timeBound < Integer.MAX_VALUE ?
            String.valueOf(smcSettings.timeBound) : "");
        smcTimeBoundInfinite.setSelected(smcSettings.timeBound == Integer.MAX_VALUE);
        smcTimeBoundValue.setEnabled(!smcTimeBoundInfinite.isSelected() && !doingBenchmark);
        smcStepBoundValue.setText(smcSettings.stepBound < Integer.MAX_VALUE ?
            String.valueOf(smcSettings.stepBound) : "");
        smcStepBoundInfinite.setSelected(smcSettings.stepBound == Integer.MAX_VALUE);
        smcStepBoundValue.setEnabled(!smcStepBoundInfinite.isSelected());
        smcTimeBoundInfinite.setEnabled(!smcStepBoundInfinite.isSelected());
        smcStepBoundInfinite.setEnabled(!smcTimeBoundInfinite.isSelected());
        
        smcNumericPrecision.setValue((int)settings.getNumericPrecision());

        smcObservations = settings.getObservations();

        smcConfidence.setText(String.valueOf(settings.confidence));
        smcConfidenceSlider.setToolTipText(String.format("Value: %.2f", settings.confidence));
        if (!doingBenchmark) {
            String formattedIntervalWidth = precisionFormat.format(settings.estimationIntervalWidth);
            smcEstimationIntervalWidth.setText(formattedIntervalWidth);
            smcPrecisionSlider.setToolTipText(String.format("Value: %s", formattedIntervalWidth));
        }

        double desiredMinFalsePositives = smcFalsePositivesSlider.getDesiredMin();
        double desiredMaxFalsePositives = smcFalsePositivesSlider.getDesiredMax();
        double initialProportionFalsePositives = (settings.falsePositives - desiredMinFalsePositives) / (desiredMaxFalsePositives - desiredMinFalsePositives);
        int initialValueFalsePositives = (int) (initialProportionFalsePositives * smcFalsePositivesSlider.getMaximum());
        smcFalsePositivesSlider.setValue(
            Math.max(smcFalsePositivesSlider.getMinimum(), 
                    Math.min(initialValueFalsePositives, smcFalsePositivesSlider.getMaximum())));
        smcFalsePositives.setText(String.valueOf(settings.falsePositives));
        smcFalsePositivesSlider.setToolTipText(String.format("Value: %.3f", settings.falsePositives));

        double desiredMinFalseNegatives = smcFalseNegativesSlider.getDesiredMin();
        double desiredMaxFalseNegatives = smcFalseNegativesSlider.getDesiredMax();
        double initialProportionFalseNegatives = (settings.falseNegatives - desiredMinFalseNegatives) / (desiredMaxFalseNegatives - desiredMinFalseNegatives);
        int initialValueFalseNegatives = (int) (initialProportionFalseNegatives * smcFalseNegativesSlider.getMaximum());
        smcFalseNegativesSlider.setValue(
            Math.max(smcFalseNegativesSlider.getMinimum(), 
                    Math.min(initialValueFalseNegatives, smcFalseNegativesSlider.getMaximum())));
        smcFalseNegatives.setText(String.valueOf(settings.falseNegatives));
        smcFalseNegativesSlider.setToolTipText(String.format("Value: %.3f", settings.falseNegatives));

        double desiredMinIndifference = smcIndifferenceSlider.getDesiredMin();
        double desiredMaxIndifference = smcIndifferenceSlider.getDesiredMax();
        double initialProportionIndifference = (settings.indifferenceWidth - desiredMinIndifference) / (desiredMaxIndifference - desiredMinIndifference);
        int initialValueIndifference = (int) (initialProportionIndifference * smcIndifferenceSlider.getMaximum());
        smcIndifferenceSlider.setValue(
            Math.max(smcIndifferenceSlider.getMinimum(), 
                    Math.min(initialValueIndifference, smcIndifferenceSlider.getMaximum())));
        smcIndifference.setText(String.valueOf(settings.indifferenceWidth));
        smcIndifferenceSlider.setToolTipText(String.format("Value: %.3f", settings.indifferenceWidth));

        double desiredMinComparison = smcComparisonFloatSlider.getDesiredMin();
        double desiredMaxComparison = smcComparisonFloatSlider.getDesiredMax();
        double initialProportionComparison = (settings.geqThan - desiredMinComparison) / (desiredMaxComparison - desiredMinComparison);

        int initialValueComparison = (int) (initialProportionComparison * smcComparisonFloatSlider.getMaximum());
        smcComparisonFloatSlider.setValue(
            Math.max(smcComparisonFloatSlider.getMinimum(), 
                    Math.min(initialValueComparison, smcComparisonFloatSlider.getMaximum())));
        smcComparisonFloat.setText(String.valueOf(settings.geqThan));
        smcComparisonFloatSlider.setToolTipText(String.format("Value: %.2f", settings.geqThan));
        updatingSmcSettings = false;
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
                                              HashMap<TimedArcPetriNet, DataLayer> guiModels, TAPNLens lens, PetriNetTab tab) {
		if (tapnNetwork.hasWeights() && !tapnNetwork.isNonStrict()) {
			JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
					"No reduction option supports both strict intervals and weighted arcs",
					"No reduction option", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		guiDialog = new EscapableDialog(TAPAALGUI.getApp(),	"Edit Query", true);
        
        Container contentPane = guiDialog.getContentPane();

        // 1 Set layout
        //contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.setLayout(new GridBagLayout());

        // 2 Add query editor
        QueryDialog queryDialogue = new QueryDialog(guiDialog, option, queryToRepresent, tapnNetwork, guiModels, lens, tab);

        guiDialog.setResizable(false);

        // setResizable seems to be platform dependent so use scrolling as a fallback
        JScrollPane scrollPane = new JScrollPane(queryDialogue);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        contentPane.add(scrollPane, gbc);

        // Make window fit contents' preferred size 
        guiDialog.pack();

        // 'hack' for hiding the trace drop-down menu for HyperLTL on intial launch of the query dialogue panel
        if(queryToRepresent != null && queryToRepresent.getCategory() == TAPNQuery.QueryCategory.HyperLTL) {
            queryDialogue.showHyperLTL(true);
            guiDialog.pack();
        } else {
            queryDialogue.showHyperLTL(false);
            guiDialog.pack();
        }

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

        refreshQueryEditingButtons();
	}

	// update selection based on some change to the query.
	// If the query contains placeholders we want to select
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

        updateQueryButtonsAccordingToSelection();

        if (currentSelection != null) {
            setEnabledOptionsAccordingToCurrentReduction();
        } else {
            disableAllQueryButtons();
        }
    }

    private void updateQueryButtonsAccordingToSelection() {
        TCTLAbstractProperty current = currentSelection.getObject();
        if (current instanceof TCTLStateToPathConverter && !lens.isTimed()) {
            current = ((TCTLStateToPathConverter) current).getProperty();
        }
        updatePredicatesAccordingToSelection(current);
        if (!lens.isTimed()) {
            setEnablednessOfOperatorAndMarkingBoxes();
        }

        if (lens.isGame() && 
            newProperty instanceof TCTLAbstractPathProperty && 
            !(newProperty instanceof TCTLPathPlaceHolder)) {
                enableOnlyStateButtons();
        }

        if ((lens.isGame() || lens.isTimed() || queryType.getSelectedIndex() != 0) &&
             (current instanceof TCTLAbstractPathProperty || newProperty instanceof TCTLPathPlaceHolder)) {
            boolean enableBooleanOperators = !(current instanceof LTLANode || current instanceof LTLENode) && queryType.getSelectedIndex() != 0 && isValidLTL();

            disjunctionButton.setEnabled(enableBooleanOperators);
            conjunctionButton.setEnabled(enableBooleanOperators);
            negationButton.setEnabled(enableBooleanOperators); 
        } else if (queryType.getSelectedIndex() == 0) {
            disjunctionButton.setEnabled(true);
            conjunctionButton.setEnabled(true);
            negationButton.setEnabled(true);
        }
	}

	private void updateSelectionPlaceNode(TCTLPlaceNode node) {
        if (node == null) return;
        if (node.getTemplate().equals("")) {
            templateBox.setSelectedItem(SHARED);
        } else {
            templateBox.setSelectedItem(tapnNetwork.getTAPNByName(node.getTemplate()));
        }
    }

    private void updatePredicatesAccordingToSelection(TCTLAbstractProperty current) {
        if (queryType.getSelectedIndex() == 2) updateTraceBox();
        if (current instanceof TCTLAtomicPropositionNode) {
            TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) current;

            // bit of a hack to prevent posting edits to the undo manager when
            // we programmatically change the selection in the atomic proposition comboboxes etc.
            // because a different atomic proposition was selected
            userChangedAtomicPropSelection = false;
            if (node.getLeft() instanceof TCTLPlaceNode) {
                updateSelectionPlaceNode((TCTLPlaceNode) node.getLeft());
            } else if (node.getLeft() instanceof HyperLTLPathScopeNode) {
                HyperLTLPathScopeNode scopeNode = (HyperLTLPathScopeNode) node.getLeft();
                updateTraceBox = false;
                traceBox.setSelectedItem(scopeNode.getTrace());
                updateTraceBox = true;

                if (scopeNode.getProperty() instanceof TCTLPlaceNode)
                    updateSelectionPlaceNode((TCTLPlaceNode) scopeNode.getProperty());
            }
            if (!lens.isTimed()) {
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
            if (!transitionNode.getTrace().equals("")) {
                traceBox.setSelectedItem(transitionNode.getTrace());
            }
            placeTransitionBox.setSelectedItem(transitionNode.getTransition());
            userChangedAtomicPropSelection = true;
        }
    }

    private void updateTimedQueryButtons(TCTLAtomicPropositionNode node) {
        if (!(node.getLeft() instanceof TCTLPlaceNode && node.getRight() instanceof TCTLConstNode) && !(node.getLeft() instanceof HyperLTLPathScopeNode)) {
            return;
        }
        TCTLPlaceNode placeNode = null;
        TCTLConstNode placeMarkingNode = (TCTLConstNode) node.getRight();

        if(queryType.getSelectedIndex() == 2) {
            placeNode = (TCTLPlaceNode) ((HyperLTLPathScopeNode) node.getLeft()).getProperty();
        } else {
            placeNode = (TCTLPlaceNode) node.getLeft();
        }

        placeTransitionBox.setSelectedItem(placeNode.getPlace());
        relationalOperatorBox.setSelectedItem(node.getOp());
        placeMarking.setValue(placeMarkingNode.getConstant());
        userChangedAtomicPropSelection = true;
    }

    private void updateUntimedQueryButtons(TCTLAtomicPropositionNode node) {
        userChangedAtomicPropSelection = false;
        if (node.getLeft() instanceof TCTLPlaceNode || node.getLeft() instanceof HyperLTLPathScopeNode) {
            TCTLPlaceNode placeNode = null;
            if(queryType.getSelectedIndex() == 2) {
                placeNode = (TCTLPlaceNode) ((HyperLTLPathScopeNode) node.getLeft()).getProperty();
            } else {
                placeNode = (TCTLPlaceNode) node.getLeft();
            }

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
				newProperty = newProperty.replace(selection, replacement);
            } else if (selection instanceof TCTLAbstractPathProperty) {
                replacement = new TCTLPathPlaceHolder();
            }
            if (replacement != null) {
                if ((selection instanceof LTLANode) || (selection instanceof LTLENode))
                if(((TCTLAbstractPathProperty) selection).getParent() != null) {
                    ((TCTLAbstractPathProperty) replacement).setParent(((TCTLAbstractPathProperty) selection).getParent());
                }

                UndoableEdit edit = new QueryConstructionEdit(selection, replacement);

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
            boolean isQueryOk = getQueryComment().length() > 0 && !newProperty.containsPlaceHolder();
            saveButton.setEnabled(isQueryOk);
            saveAndVerifyButton.setEnabled(isQueryOk);
            saveUppaalXMLButton.setEnabled(isQueryOk);
            mergeNetComponentsButton.setEnabled(isQueryOk);
            openReducedNetButton.setEnabled(isQueryOk && useReduction.isSelected());
            smcTimeEstimationButton.setEnabled(isQueryOk);
        } else {
            saveButton.setEnabled(false);
            saveAndVerifyButton.setEnabled(false);
            saveUppaalXMLButton.setEnabled(false);
            mergeNetComponentsButton.setEnabled(false);
            openReducedNetButton.setEnabled(false);
            smcTimeEstimationButton.setEnabled(false);
        }
    }

    private void setEnabledReductionOptions(){
        if (rawVerificationOptionsEnabled.isSelected()) {
            return;
        }

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
            newProperty.hasNestedPathQuantifiers(),
            lens.isColored(),
            lens.isColored() && !lens.isTimed(),
            lens.isStochastic()
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
            if(queryType.getSelectedIndex() == 2) {
                useStubbornReduction.setEnabled(false);
            } else {
                useStubbornReduction.setEnabled(true);
            }
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
                    if (engine.getNameString().equals(name_verifyTAPN) && lens.isStochastic()) {
                        continue;
                    }

                    options.add(engine.getNameString());
                }
            }
        } else {
            options.add(name_UNTIMED);
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
        if (reductionOption.getSelectedItem() == null) {
            return;
        }

		JRadioButton currentSelected;
		if (heuristicSearch.isSelected()) {
			currentSelected = heuristicSearch;
		} else if(breadthFirstSearch.isSelected()) {
			currentSelected = breadthFirstSearch;
		} else if(depthFirstSearch.isSelected()) {
			currentSelected = depthFirstSearch;
		} else {
			currentSelected = randomSearch;
		}

        if (queryType.getSelectedIndex() == 2) {
            breadthFirstSearch.setEnabled(false);
            heuristicSearch.setEnabled(false);
            randomSearch.setEnabled(false);
        }
        else if (fastestTraceRadioButton.isSelected()) {
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

            if (!useTarjan.isSelected() && someTraceRadioButton.isSelected()) {
                randomSearch.setEnabled(false);
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
        } else if (lens.isTimed() && (newProperty.toString().contains("EG") || newProperty.toString().contains("AF"))) {
            breadthFirstSearch.setEnabled(false);
            if (!(reductionOptionString.equals(name_verifyTAPN) || reductionOptionString.equals(name_DISCRETE))) {
                heuristicSearch.setEnabled(false);
            }
        }

		if (!currentSelected.isEnabled()) {
			if (heuristicSearch.isEnabled()) {
				heuristicSearch.setSelected(true);
			} else {
				depthFirstSearch.setSelected(true);
			}
		}

		if (!lens.isTimed() && !lens.isGame() && isReachabilityQuery()) {
		    heuristicSearch.setText("Random heuristic    ");
        } else {
            heuristicSearch.setText("Heuristic    ");
        }
    }

	private boolean isReachabilityQuery() {
	    return !newProperty.hasNestedPathQuantifiers() && (newProperty instanceof TCTLAGNode || newProperty instanceof TCTLEFNode);
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
        searchBar.setEnabled(false);
        templateBox.setEnabled(false);
        placeTransitionBox.setEnabled(false);
        relationalOperatorBox.setEnabled(false);
        placeMarking.setEnabled(false);
        addPredicateButton.setEnabled(false);
        truePredicateButton.setEnabled(false);
        falsePredicateButton.setEnabled(false);
        deadLockPredicateButton.setEnabled(false);
        traceBox.setEnabled(false);
        traceBoxQuantification.setEnabled(false);
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
        searchBar.setEnabled(false);
        templateBox.setEnabled(false);
        placeTransitionBox.setEnabled(false);
        relationalOperatorBox.setEnabled(false);
        placeMarking.setEnabled(false);
        addPredicateButton.setEnabled(false);
        truePredicateButton.setEnabled(false);
        falsePredicateButton.setEnabled(false);
        if (queryType.getSelectedIndex() == 2) traceBox.setEnabled(false);
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
        searchBar.setEnabled(false);
        templateBox.setEnabled(false);
        placeTransitionBox.setEnabled(false);
        relationalOperatorBox.setEnabled(false);
        placeMarking.setEnabled(false);
        addPredicateButton.setEnabled(false);
        truePredicateButton.setEnabled(false);
        falsePredicateButton.setEnabled(false);
        deadLockPredicateButton.setEnabled(false);
        if (queryType.getSelectedIndex() == 2) traceBox.setEnabled(false);
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
        searchBar.setEnabled(true);
        templateBox.setEnabled(true);
        placeTransitionBox.setEnabled(true);
        relationalOperatorBox.setEnabled(true);
        placeMarking.setEnabled(true);
        truePredicateButton.setEnabled(true);
        falsePredicateButton.setEnabled(true);
        deadLockPredicateButton.setEnabled(true);
        setEnablednessOfAddPredicateButton();
        if (queryType.getSelectedIndex() == 2) traceBox.setEnabled(traceBox.getModel().getSize() > 0);
    }

    private void enableOnlySMCButtons() {
        finallyButton.setEnabled(true);
        globallyButton.setEnabled(true);
        if(lens.isStochastic()) {
            updateSMCButtons();
        }
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
        searchBar.setEnabled(true);
        templateBox.setEnabled(true);
        placeTransitionBox.setEnabled(true);
        relationalOperatorBox.setEnabled(true);
        placeMarking.setEnabled(true);
        truePredicateButton.setEnabled(true);
        falsePredicateButton.setEnabled(true);
        deadLockPredicateButton.setEnabled(true);

        if (queryType.getSelectedIndex() == 1) {
            updateLTLButtons();
        } else if(queryType.getSelectedIndex() == 2) {
            traceBox.setEnabled(traceBox.getModel().getSize() > 0);
            updateHyperLTLButtons();
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
        searchBar.setEnabled(false);
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
        int selectedIndex = queryType.getSelectedIndex();
        if (placeTransitionBox.getSelectedItem() == null ||
            ((selectedIndex == 1 || selectedIndex == 2) && currentSelection.getObject() == newProperty))
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
        if (currentSelection != null) {
            deleteButton.setEnabled(currentSelection != null);
        } 
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
        // trace for HyperLTL
        String selectedTrace = "";
        boolean isHyperLTL = false;
        if (queryType.getSelectedIndex() == 2) {
            isHyperLTL = true;
            selectedTrace = traceBox.getSelectedItem().toString();
        }

        if (currentSelection != null && (currentSelection.getObject() instanceof TCTLAtomicPropositionNode ||
            (!lens.isTimed() && currentSelection.getObject() instanceof TCTLTransitionNode))) {

            Object item = templateBox.getSelectedItem();
            String template = item.equals(SHARED) ? "" : item.toString();
            TCTLAbstractStateProperty property;

            if (!lens.isTimed() && transitionIsSelected()) {
                if(isHyperLTL)
                    property = new TCTLTransitionNode(template, (String) placeTransitionBox.getSelectedItem(), selectedTrace);
                else
                    property = new TCTLTransitionNode(template, (String) placeTransitionBox.getSelectedItem());
            } else {
                if(isHyperLTL) {
                    HyperLTLPathScopeNode pathScope = new HyperLTLPathScopeNode(new TCTLPlaceNode(template, (String) placeTransitionBox.getSelectedItem()), selectedTrace);
                    property =  new TCTLAtomicPropositionNode(
                        pathScope,
                        (String) relationalOperatorBox.getSelectedItem(),
                        new TCTLConstNode((Integer) placeMarking.getValue()));
                } else {
                    property =  new TCTLAtomicPropositionNode(
                        new TCTLPlaceNode(template, (String) placeTransitionBox.getSelectedItem()),
                        (String) relationalOperatorBox.getSelectedItem(),
                        new TCTLConstNode((Integer) placeMarking.getValue()));
                }
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

    private void checkTraceBoxSelection() {
        String selectedTrace = traceBox.getSelectedItem().toString();
        if (!getQuery().getTraceList().contains(selectedTrace)) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "Cannot select a trace that is not declared in ",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Used in HyperLTL to update queries with the 'A' or 'E' quantifier
    private void updateQueryOnQuantificationChange() {
        // trace for HyperLTL
        String selectedTrace = "";
        if (queryType.getSelectedIndex() == 2 && traceBoxQuantification.getSelectedItem() != null) {
            selectedTrace = traceBoxQuantification.getSelectedItem().toString();
        }

        if (currentSelection != null && queryType.getSelectedIndex() == 2 &&
                ((currentSelection.getObject() instanceof LTLANode) || currentSelection.getObject() instanceof LTLENode)) {
            TCTLAbstractPathProperty property;
            TCTLAbstractPathProperty currentProp = (TCTLAbstractPathProperty) currentSelection.getObject();

            if (currentSelection.getObject() instanceof LTLANode) {
                property = new LTLANode(((LTLANode)currentProp).getProperty(), selectedTrace);
            } else {
                property = new LTLENode(((LTLENode)currentProp).getProperty(), selectedTrace);
            }

            if (!property.equals(currentSelection.getObject())) {
                UndoableEdit edit = new QueryConstructionEdit(currentProp, property);
                newProperty = newProperty.replace(currentProp,	property);
                updateSelection(property);
                updateTraceBox(property);
                undoSupport.postEdit(edit);
            }
            queryChanged();
        }

    }

    private void updateTraceBox(TCTLAbstractPathProperty node) {
        if(node instanceof LTLANode) {
            traceBoxQuantification.setSelectedItem(((LTLANode)node).getTrace());
        } else {
            traceBoxQuantification.setSelectedItem(((LTLENode)node).getTrace());
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Initialization of the dialogue
    // /////////////////////////////////////////////////////////////////////

    private void init(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
        initQueryNamePanel();
        initQueryPanel();
        initUppaalOptionsPanel();
        initVerificationPanel();
        initOverApproximationPanel();
        initSmcSettingsPanel();
        initRawVerificationOptionsPanel();
        initButtonPanel(option, queryToCreateFrom == null);

        if (lens.isStochastic()) {
            setSMCSettings(SMCSettings.Default());
        }

        if (queryToCreateFrom != null) {
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

        makeShortcuts();

        if (lens.isGame() && !lens.isTimed()) {
            useReduction.setSelected(false);
            useReduction.setEnabled(false);
            useSiphonTrap.setSelected(false);
            useSiphonTrap.setEnabled(false);
        }

        if (queryToCreateFrom != null) {
            setupRawVerificationOptionsFromQuery(queryToCreateFrom);
        } else {
            setupRawVerificationOptions();
        }
    }

    private void setupFromQuery(TAPNQuery queryToCreateFrom) {
        queryName.setText(queryToCreateFrom.getName());

        if (queryToCreateFrom.getOldCapacity() == null) {
            numberOfExtraTokensInNet.setValue(queryToCreateFrom.getCapacity());
        } else {
            numberOfExtraTokensInNet.setValue(queryToCreateFrom.getOldCapacity());
        }

        if (lens.isTimed()) {
            setupApproximationOptionsFromQuery(queryToCreateFrom);
            setupQuantificationFromQuery(queryToCreateFrom);
        } else if (lens.isColored()) {
            setupUnfoldingOptionsFromQuery(queryToCreateFrom);
        }

        if (queryToCreateFrom.getCategory() == TAPNQuery.QueryCategory.SMC) {
            setSMCSettings(queryToCreateFrom.getSmcSettings());
            smcParallel.setSelected(queryToCreateFrom.isParallel());
            smcVerificationType.setSelectedIndex(queryToCreateFrom.getVerificationType().ordinal());
            smcNumberOfTraces.setValue(queryToCreateFrom.getNumberOfTraces());
            smcTraceType.setSelectedItem(queryToCreateFrom.getSmcTraceType());
            smcGranularityField.setText(String.valueOf(queryToCreateFrom.getGranularity()));
            smcGranularityField.setEnabled(!queryToCreateFrom.isMaxGranularity());
            smcMaxGranularityCheckbox.setSelected(queryToCreateFrom.isMaxGranularity());
        }

        setupQueryCategoryFromQuery(queryToCreateFrom);
        setupSearchOptionsFromQuery(queryToCreateFrom);
        setupReductionOptionsFromQuery(queryToCreateFrom);
        setupTraceOptionsFromQuery(queryToCreateFrom);
        setupTarOptionsFromQuery(queryToCreateFrom);
        setupTarjanOptionsFromQuery(queryToCreateFrom);

        if (queryToCreateFrom.getCategory() == TAPNQuery.QueryCategory.HyperLTL) {
            setupTraceListFromQuery(queryToCreateFrom);
        }

    }

    private void setupRawVerificationOptionsFromQuery(TAPNQuery queryToCreateFrom) {
        rawVerificationOptionsTextArea.setText(queryToCreateFrom.getRawVerificationPrompt());
        setupRawVerificationOptions(queryToCreateFrom.getRawVerification());

        if (rawVerificationOptionsEnabled.isSelected() && !advancedView) {
            toggleAdvancedSimpleView(true);
        }
    }

    private void setupRawVerificationOptions() {
        setupRawVerificationOptions(false);
    }

    private void setupRawVerificationOptions(boolean isSelected) {
        rawVerificationOptionsEnabled.setSelected(isSelected);

        addItemListeners(searchOptionsPanel);
        addItemListeners(unfoldingOptionsPanel);
        addItemListeners(traceOptionsPanel);
        addItemListeners(reductionOptionsPanel);

        numberOfExtraTokensInNet.addChangeListener(e -> updateRawVerificationOptions());
        reductionOption.addActionListener(e -> updateRawVerificationOptions());
        smcVerificationType.addActionListener(e -> {
            if (!updatingSmcSettings) updateRawVerificationOptions();
        });
        smcNumberOfTraces.addChangeListener(e -> {
            if (!updatingSmcSettings) updateRawVerificationOptions();
        });
        smcParallel.addActionListener(e -> {
            if (!updatingSmcSettings) updateRawVerificationOptions();
        });

        final JTextField smcNumTracesTextField = ((JSpinner.DefaultEditor) smcNumberOfTraces.getEditor()).getTextField();

        // Fix from https://stackoverflow.com/a/6276603 to update uppon typing
        // to ensure raw options are updated correctly
        smcNumTracesTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = smcNumTracesTextField.getText().replace(",", "");
                int oldCaretPos = smcNumTracesTextField.getCaretPosition();
                try {
                    Integer newValue = Integer.valueOf(text);
                    smcNumberOfTraces.setValue(newValue);
                    smcNumTracesTextField.setCaretPosition(oldCaretPos);
                } catch(NumberFormatException ex) {
                    // Not a number in text field -> do nothing
                }
            }
        });

        smcTraceType.addActionListener(e -> {
            if (!updatingSmcSettings) updateRawVerificationOptions();
        });
        
        if (reductionOption.getSelectedItem() != null) {
            updateRawVerificationOptions();
        }
    }

    private void addItemListeners(JPanel panel) {
        if (panel != null) {
            for (Component component : panel.getComponents()) {
                if (component instanceof JRadioButton || component instanceof JCheckBox) {
                    AbstractButton button = (AbstractButton) component;
                    button.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            updateRawVerificationOptions();
                        }
                    });
                }
            }
        }
    }

    private void setupTraceListFromQuery(TAPNQuery queryToCreateFrom) {
        // First remove all elements (removes the default trace that was added)
        traceModel.removeAllElements();

        Set<String> traces = new HashSet<>(queryToCreateFrom.getTraceList());
        
        // Extracts trace names from the field
        Pattern pattern = Pattern.compile("\\s([a-zA-Z]\\w*)\\.");
        Matcher matcher = pattern.matcher(queryToCreateFrom.getProperty().toString());
        while (matcher.find()) traces.add(matcher.group(1));

        for (String trace : traces) {
            traceModel.addElement(trace);
        }

        traceList.setModel(traceModel);
        updateTraceBox();
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

        if (queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST) {
            reduction = name_BROADCAST;
        } else if (queryToCreateFrom.getReductionOption() == ReductionOption.DEGREE2BROADCAST) {
            reduction = name_BROADCASTDEG2;
        } else if(queryToCreateFrom.getReductionOption() == ReductionOption.VerifyDTAPN){
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

        if (lens.isTimed()) {
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
        skeletonAnalysis.setSelected(queryToCreateFrom.useOverApproximation());
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
        useColoredReduction.setSelected(queryToCreateFrom.useColoredReduction());
    }

    private void setupUnfoldingOptionsFromQuery(TAPNQuery queryToCreateFrom){
        usePartitioning.setSelected(queryToCreateFrom.usePartitioning());
        useColorFixpoint.setSelected(queryToCreateFrom.useColorFixpoint());
        useSymmetricvars.setSelected(queryToCreateFrom.useSymmetricVars());
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
        } else if (queryToCreateFrom.getProperty() instanceof TCTLAFNode) {
            forAllDiamond.setSelected(true);
        } else if (queryToCreateFrom.getProperty() instanceof TCTLAGNode) {
            forAllBox.setSelected(true);
        }
    }

    private void setupQueryCategoryFromQuery(TAPNQuery queryToCreateFrom) {
        if (!lens.isTimed()) {
            TAPNQuery.QueryCategory category = queryToCreateFrom.getCategory();
            if (category.equals(TAPNQuery.QueryCategory.CTL)) {
                queryType.setSelectedIndex(0);
            } else if (category.equals(TAPNQuery.QueryCategory.LTL)) {
                queryType.setSelectedIndex(1);
            } else if (category.equals(TAPNQuery.QueryCategory.HyperLTL)) {
                queryType.setSelectedIndex(2);
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

        queryType = new JComboBox(new String[]{"CTL/Reachability", "LTL", "HyperLTL"});
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
                return "<html>" +
                    "<b>Boundedness Options</b><br/>" +
                    "The query dialog allows you to specify the extra number of tokens that TAPAAL is allowed to use during the verification. " +
                    "Because TAPAAL models can produce additional tokens by firing transitions (e.g. a transition that has a single input place " +
                    "and two output places) you may need to use additional tokens compared to those that are already in the net. By " +
                    "specifying an extra number of tokens you can ask TAPAAL to check if your net is bounded for this number of extra tokens (i.e. " +
                    "whether there is no reachable marking in the net that would exceed the predefined number of tokens. " +
                    "<br/><br/>" +
                    "<b>Search Strategy Options</b><br/>" +
                    "A search strategy determines how the chosen verification engine performs the search. The possible search strategies are: " +
                    "<ul>" +
                    "<li>Heuristic Search<br/> If available, the search is guided according to the query so that the most likely places where the query is satisfied are visited first. If discrete inclusion optimization is not enabled or the heuristic search is not available, this strategy performs a breadth first search. " +
                    "If discrete inclusion is enabled, the search attempts to maximize the number of tokens in places where the engine checks for discrete inclusion.</li>" +
                    "<li>Breadth First Search<br/>Explores markings in a breadth first manner.</li>" +
                    "<li>Depth First Search<br/>Explores markings in a depth first manner.</li>" +
                    "<li>Random Search<br/>Performs a random exploration of the state space.</li>" +
                    "</ul>" +
                    "<br/>" +
                    "<b>Verification Options</b><br/>" +
                    "TAPAAL supports verification via its own included engines verifytapn and verifydtapn or via a translation to networks of timed automata and then using the tool UPPAAL (requires a separate installation). If you work with an untimed net, we recommend that you use the CTL query creation dialog and use the untimed verifypn engine." +
                    "The TAPAAL engine verifytapn supports also the discrete inclusion optimization. " +
                    "On some models this technique gives a considerable speedup. " +
                    "The user selected set of places that are considered for the discrete inclusion can further fine-tune the performance of the engine. Try to include places where you expect to see many tokens during the execution. " +
                    "The discrete verification engine verifydtapn performs a point-wise exploration of the state space but can be used only for models that do not contain strict intervals as in this situation it is guaranteed to give the same answers as the continuous time engine verifytapn. This discrete engine has options to handle delays in semi-symbolic way (time darts) recommended for models with larger constants and it has a memory optimization option feature (PTrie) that preserves lots of memory at the expense of a slightly longer verification time." +
                    "The different UPPAAL verification methods perform reductions to networks of timed automata. The broadcast reductions supports " +
                    "all query types, while standard and optimized standard support only EF and AG queries but can be sometimes faster." +
                    "<br/>" +
                    "<b>Approximation Options</b><br/>" +
                    "TAPAAL allows to approximate the time intervals on edges by dividing them by the given approximation constant and either enlarging the resulting intervals (over-approximation) or shrinking them (under-approximation). The larger the constant is, the faster is the verification but the more often the user can get an inconclusive answer." +
                    "<br/>" +
                    "<b>SMC Options</b><br/>" +
                    "Statistical model-checking simulates random runs in order to verify how often a property is satisfied. It explores random runs that do not exceed the given time and step bound. " +
                    "There are three types of SMC queries :" +
                    "<ul>" +
                    "<li>The quantitative probability estimation performs a Monte-Carlo algorithm to produce an estimation of the probability of an event happening. The number of runs to execute is defined by the desired confidence and precision, which are the confidence that the real probability is in an interval of ± the precision around the estimation.</li>" +
                    "<li>The qualitative probability testing performs a SPRT test, to produce an estimation whether the probability of an event happening is greater than a real constant C. According to the result of each run, a ratio is updated and a result is decided once it reaches a bound. The false-positives parameter is the probability of estimating the test to be true when it isn't, the false-negatives parameter is the opposite, and the indifference region is the zone C ± width, which bounds are used as thresholds.</li>" +
                    "<li>The simulate mode generates an arbitrary number of random traces, that can then be explored in the simulator. The traces could be restricted to : any trace, only traces satisfying the property, or only traces violating the property. This mode must be used carefully, because in some cases it may not terminate : if asking for 5 traces satisfying the property, but no runs ever satisfy the property, then the algorithm will run forever.</li>" +
                    "<ul/>" +
                    "<br/>" +
                    "</html>";
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

        boolean isSmc = lens.isStochastic();

        Point location = guiDialog.getLocation();

        searchOptionsPanel.setVisible(advancedView && !isSmc);
        if(lens.isColored() && !lens.isTimed()){
            unfoldingOptionsPanel.setVisible(advancedView);
        }

        reductionOptionsPanel.setVisible(advancedView && !isSmc);
        if (lens.isTimed()) {
            saveUppaalXMLButton.setVisible(advancedView && !isSmc);
            // Disabled approximation options for colored models, because they are not supported yet (will generate error)
            overApproximationOptionsPanel.setVisible(advancedView && !isSmc);
        } else if (!lens.isGame()){
            openReducedNetButton.setVisible(advancedView);
        }
        mergeNetComponentsButton.setVisible(advancedView);

        showRawVerificationOptions(advancedView);

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

    private boolean isHyperLTL(TCTLAbstractProperty property) {
        if (property instanceof LTLENode) {
            return !((LTLENode) property).getTrace().equals("");
        } else if(property instanceof LTLANode) {
            return !((LTLANode) property).getTrace().equals("");
        }
        return false;
    }

    private void toggleDialogType() {
        if (queryType.getSelectedIndex() == 2 && (wasCTLType || wasLTLType)) {
            if (!isHyperLTL(newProperty) && !(newProperty instanceof TCTLPathPlaceHolder) || !isValidLTL() && !queryField.getText().trim().equals((new TCTLPathPlaceHolder()).toString())) {
                if (showWarningMessage() == JOptionPane.YES_OPTION) {
                    newProperty = new TCTLPathPlaceHolder();
                    deleteProperty();
                } else {
                    int changeTo = wasCTLType ? 0 : 1;
                    queryType.setSelectedIndex(changeTo);
                    return;
                }
            }

            showLTLButtons(true);
            showHyperLTL(true);
            updateSiphonTrap(true);
            queryChanged();

            wasCTLType = false;
            wasLTLType = false;
            wasHyperLTLType = true;
        } else if (queryType.getSelectedIndex() == 1 && (wasCTLType || wasHyperLTLType)) {
            String ltlType = checkLTLType();
            boolean isA = ltlType.equals("A");
            if (isHyperLTL(newProperty) || convertPropertyType(false, newProperty, true, isA) == null &&
                !(newProperty instanceof TCTLStatePlaceHolder)) {
                if (showWarningMessage() == JOptionPane.YES_OPTION) {
                    deleteProperty();
                } else {
                    int changeTo = wasCTLType ? 0 : 2;
                    queryType.setSelectedIndex(changeTo);
                    return;
                }
            } else if (isA) {
                addAllPathsToProperty(newProperty, null);
            } else if (ltlType.equals("E")) {
                addExistsPathsToProperty(newProperty, null);
            }

            // Check again after conversion
            if (!isValidLTL() && !queryField.getText().trim().equals((new TCTLPathPlaceHolder()).toString())) {
                if (showWarningMessage() == JOptionPane.YES_OPTION) {
                    deleteProperty();
                } else {
                    int changeTo = wasCTLType ? 0 : 2;
                    queryType.setSelectedIndex(changeTo);
                    return;
                }
            }

            showLTLButtons(true);
            updateSiphonTrap(true);
            showHyperLTL(false);
            queryChanged();
            wasHyperLTLType = false;
            wasCTLType = false;
            wasLTLType = true;
        } else if (queryType.getSelectedIndex() == 0 && !wasCTLType) {
            if (isHyperLTL(newProperty) || convertPropertyType(true, newProperty, true, newProperty instanceof LTLANode) == null &&
                !(newProperty instanceof TCTLStatePlaceHolder)) {
                if (showWarningMessage() == JOptionPane.YES_OPTION) {
                    deleteProperty();
                    newProperty = removeExistsAllPathsFromProperty(newProperty);
                } else {
                    int changeTo = wasLTLType ? 1 : 2;
                    queryType.setSelectedIndex(changeTo);
                    return;
                }
            }

            showLTLButtons(false);
            showHyperLTL(false);
            updateSiphonTrap(false);
            
            wasCTLType = true;
            wasLTLType = false;
            wasHyperLTLType = false;
        }

        if (undoManager != null) undoManager.discardAllEdits();
        if (undoButton != null) undoButton.setEnabled(false);
        if (redoButton != null) redoButton.setEnabled(false);

        setEnabledOptionsAccordingToCurrentReduction();
        updateRawVerificationOptions();
    }

    private void toggleSmc() {
        if(lens.isStochastic()) {
            showSMCButtons(true);
            reductionOption.setSelectedItem(name_DISCRETE);
            useGCD.setSelected(false);
            reductionOption.setEnabled(false);
            traceOptionsPanel.setVisible(false);
            boundednessCheckPanel.setVisible(false);
            smcSettingsPanel.setVisible(true);
            toggleAdvancedSimpleView(false);
            queryChanged();
        } else {
            showSMCButtons(false);
            reductionOption.setEnabled(true);
            traceOptionsPanel.setVisible(true);
            boundednessCheckPanel.setVisible(true);
            smcSettingsPanel.setVisible(false);
            toggleAdvancedSimpleView(false);
            queryChanged();
        }

        if (undoManager != null) undoManager.discardAllEdits();
        if (undoButton != null) undoButton.setEnabled(false);
        if (redoButton != null) redoButton.setEnabled(false);
    }

    private boolean isValidLTL() {
        String queryText = queryField.getText().trim();
        return queryText.startsWith("A") || queryText.startsWith("E");
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
        property = removeConverter(property);

        if (toCTL) {
            TCTLAbstractStateProperty firstChild = getChild(toCTL, property, 1, isA);
            if (firstChild == null) return null;
            
            if (property instanceof LTLGNode) {
                replacement = isA? new TCTLAGNode(firstChild) : new TCTLEGNode(firstChild);
            } else if (property instanceof LTLFNode) {
                replacement = isA ? new TCTLAFNode(firstChild) : new TCTLEFNode(firstChild);
            } else if (property instanceof LTLXNode) {
                replacement = isA ? new TCTLAXNode(firstChild) : new TCTLEXNode(firstChild);
            } else if (property instanceof LTLUNode) {
                TCTLAbstractStateProperty secondChild = getChild(toCTL, property, 2, isA);
                if (secondChild == null) return null;
                replacement = isA ? new TCTLAUNode(firstChild, secondChild): new TCTLEUNode(firstChild, secondChild);
            }
        } else {
            TCTLAbstractStateProperty firstChild = getChild(toCTL, property, 1, isA);
            if (firstChild == null) return null;
            
            if (property instanceof TCTLAGNode || property instanceof TCTLEGNode) {
                replacement = new LTLGNode(firstChild);
            } else if (property instanceof TCTLAFNode || property instanceof TCTLEFNode) {
                replacement = new LTLFNode(firstChild);
            } else if (property instanceof TCTLAXNode || property instanceof TCTLEXNode) {
                replacement = new LTLXNode(firstChild);
            } else if (property instanceof TCTLAUNode || property instanceof TCTLEUNode) {
                TCTLAbstractStateProperty secondChild = getChild(toCTL, property, 2, isA);
                if (secondChild == null) return null;
                replacement = new LTLUNode(firstChild, secondChild);
            }
        }

        if (replacement == null) {
            if (property instanceof TCTLStatePlaceHolder || property instanceof TCTLPathPlaceHolder) {
                return property;
            } else if (property instanceof TCTLNotNode) {
                TCTLAbstractStateProperty firstChild = getChild(toCTL, property, 1, isA);
                if (firstChild == null) return null;
                return new TCTLNotNode(firstChild);
            } else if (property instanceof TCTLAndListNode) {
                return convertListNode((TCTLAndListNode) property, toCTL, isA, true);
            } else if (property instanceof TCTLOrListNode) {
                return convertListNode((TCTLOrListNode) property, toCTL, isA, false);
            } else {
                replacement = property;
            }
        }

        return replacement;
    }

    private TCTLAbstractProperty convertListNode(Object listNode, boolean toCTL, boolean isA, boolean isAndNode) {
        StringPosition[] children;
        if (isAndNode) {
            children = ((TCTLAndListNode)listNode).getChildren();
        } else {
            children = ((TCTLOrListNode)listNode).getChildren();
        }
        
        List<TCTLAbstractStateProperty> convertedChildren = new ArrayList<>();
        for (StringPosition childPos : children) {
            TCTLAbstractProperty child = childPos.getObject();
            TCTLAbstractProperty convertedChild = convertPropertyType(toCTL, child, false, isA);
            if (convertedChild == null) return null;

            if (convertedChild instanceof TCTLAbstractStateProperty) {
                convertedChildren.add((TCTLAbstractStateProperty)convertedChild);
            } else if (convertedChild instanceof TCTLAbstractPathProperty) {
                convertedChildren.add(ConvertToStateProperty((TCTLAbstractPathProperty)convertedChild));
            }
        }
        
        if (convertedChildren.isEmpty()) return null;

        return isAndNode ? 
            new TCTLAndListNode(convertedChildren) : 
            new TCTLOrListNode(convertedChildren);
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

    private int showWarningMessage() {
        String message = "The query will be deleted, because it is incompatible with the selected query type.\n" +
            "Are you sure you want to change query category?";
        String title = "Incompatible query";

        return JOptionPane.showConfirmDialog(
            TAPAALGUI.getApp(),
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    }

    private TCTLAbstractPathProperty getParentForHyperLTLAllPath(TCTLAbstractProperty property) {
        if(property instanceof TCTLStatePlaceHolder) {
            return getParentForHyperLTLAllPath(((TCTLStatePlaceHolder) property).getParent());
        } else if(property instanceof TCTLPathToStateConverter) {
            return getParentForHyperLTLAllPath(((TCTLPathToStateConverter) property).getParent());
        } else if(property instanceof TCTLPathPlaceHolder) {
            return getParentForHyperLTLAllPath(((TCTLPathPlaceHolder) property).getParent());
        } else if(!((((TCTLAbstractPathProperty) property).getParent()) instanceof LTLANode) && !(property instanceof LTLANode)) {
            return getParentForHyperLTLAllPath(((TCTLAbstractPathProperty) property).getParent());
        }

        if(property instanceof LTLANode) return (TCTLAbstractPathProperty) property;

        return (TCTLAbstractPathProperty) ((TCTLAbstractPathProperty) property).getParent();
    }

    private void addAllPathsHyperLTL(TCTLAbstractProperty oldProperty, TCTLAbstractProperty selection, String trace) {
        if(!(selection instanceof TCTLStatePlaceHolder) && !(selection instanceof TCTLPathPlaceHolder)) {
            if(selection instanceof TCTLPathToStateConverter) {
                TCTLAbstractProperty child = ((TCTLPathToStateConverter) selection).getProperty();
                addAllPathsHyperLTL(oldProperty, child, trace);
            } else{
                addAllPathsHyperLTL(oldProperty, ((LTLANode) selection).getProperty(), trace);
            }

        } else {
            LTLANode parent = null;
            TCTLAbstractProperty child = null;

            parent = (LTLANode) getParentForHyperLTLAllPath(selection);

            TCTLPathPlaceHolder placeHolder = new TCTLPathPlaceHolder();
            child = new LTLANode(ConvertToStateProperty(placeHolder), trace);

            if(parent != null) {
                parent.setProperty(ConvertToStateProperty((TCTLAbstractPathProperty)child));
            }
        }
    }

    private void addAllPathsToProperty(TCTLAbstractProperty oldProperty, TCTLAbstractProperty selection) {
        TCTLAbstractProperty property = null;
        int selectedIndex = queryType.getSelectedIndex();
        boolean isHyperLTL = selectedIndex == 2;
        String trace = isHyperLTL ? traceBoxQuantification.getSelectedItem().toString() : "";
        if (oldProperty instanceof LTLANode) {
            if(!(selectedIndex == 2)) {
                property = oldProperty;
            } else {
                if(currentSelection.getObject().toString().equals("<*>") && isHyperLTL){
                    // We copy the objects, otherwise it bugs out the undo-manager as it uses the references to the objects
                    oldProperty = oldProperty.copy();
                    selection = selection.copy();
                    addAllPathsHyperLTL(oldProperty, selection, trace);

                    newProperty = selection;
                    updateSelection(selection);

                    return;
                } else {
                    property = isHyperLTL ? new LTLANode(ConvertToStateProperty((TCTLAbstractPathProperty) selection), trace) : new LTLANode();
                }

            }
        } else if (oldProperty instanceof TCTLPathPlaceHolder) {
            property = isHyperLTL ? new LTLANode(trace) : new LTLANode();
        } else if (oldProperty instanceof TCTLAbstractPathProperty) {
            property = isHyperLTL ? new LTLANode(ConvertToStateProperty((TCTLAbstractPathProperty) oldProperty), trace) : new LTLANode(ConvertToStateProperty((TCTLAbstractPathProperty) oldProperty));
        } else if (oldProperty instanceof TCTLNotNode) {
            property = isHyperLTL ? new LTLANode((TCTLNotNode) oldProperty, trace) : new LTLANode((TCTLNotNode) oldProperty);
            property = ConvertToStateProperty((TCTLAbstractPathProperty) property);
        } else if (oldProperty instanceof TCTLAbstractStateProperty && (selection == null || selection instanceof LTLANode)) {
            property = isHyperLTL ? new LTLANode((TCTLAbstractStateProperty) oldProperty, trace) : new LTLANode((TCTLAbstractStateProperty) oldProperty);
            if (!(newProperty instanceof TCTLAbstractPathProperty)) newProperty = ConvertToPathProperty((TCTLAbstractStateProperty) newProperty);
        }

        if (property != null && selection != null) {
            UndoableEdit edit = new QueryConstructionEdit(selection, property);
            if(!(selectedIndex == 2)) {
                newProperty = newProperty.replace(newProperty, property);
            } else {
                newProperty = property;
            }

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
        int selectedIndex = queryType.getSelectedIndex();
        boolean isHyperLTL = selectedIndex == 2;
        String trace = isHyperLTL ? traceBoxQuantification.getSelectedItem().toString() : "";

        if (oldProperty instanceof LTLENode) {
            if(!(selectedIndex == 2)) {
                property = oldProperty;
            } else {
                if(currentSelection.getObject().toString().equals("<*>") && isHyperLTL){
                    // We copy the objects, otherwise it bugs out the undo-manager as it uses the references to the objects
                    oldProperty = oldProperty.copy();
                    selection = selection.copy();
                    addExistsPathsHyperLTL(oldProperty, selection, trace);

                    newProperty = selection;
                    updateSelection(selection);

                    return;

                } else {
                    property = isHyperLTL ? new LTLENode(ConvertToStateProperty((TCTLAbstractPathProperty) oldProperty), trace) : new LTLENode();
                }
            }
        } else if (oldProperty instanceof TCTLPathPlaceHolder) {
            property = isHyperLTL ? new LTLENode(trace) : new LTLENode();
        } else if (oldProperty instanceof TCTLAbstractPathProperty) {
            property = isHyperLTL ? new LTLENode(ConvertToStateProperty((TCTLAbstractPathProperty) oldProperty), trace) : new LTLENode(ConvertToStateProperty((TCTLAbstractPathProperty) oldProperty));
        } else if (oldProperty instanceof TCTLNotNode) {
            property = isHyperLTL ? new LTLENode((TCTLNotNode) oldProperty, trace) : new LTLENode((TCTLNotNode) oldProperty);
            property = ConvertToStateProperty((TCTLAbstractPathProperty) property);
        } else if (oldProperty instanceof TCTLAbstractStateProperty && (selection == null || selection instanceof LTLENode)) {
            property = isHyperLTL ? new LTLENode((TCTLAbstractStateProperty) oldProperty, trace) : new LTLENode((TCTLAbstractStateProperty) oldProperty);
            if (!(newProperty instanceof TCTLAbstractPathProperty)) newProperty = ConvertToPathProperty((TCTLAbstractStateProperty) newProperty);
        }

        if (property != null && selection != null) {
            UndoableEdit edit = new QueryConstructionEdit(selection, property);
            if(!(selectedIndex == 2)) {
                newProperty = newProperty.replace(newProperty, property);
            } else {
                newProperty = property;
            }
            updateSelection(property);
            undoSupport.postEdit(edit);
            queryChanged();
        } else if (property != null) {
            newProperty = newProperty.replace(newProperty, property);
            updateSelection(property);
            queryChanged();
        }
    }

    private void addExistsPathsHyperLTL(TCTLAbstractProperty oldProperty, TCTLAbstractProperty selection, String trace) {
        if (!(selection instanceof TCTLStatePlaceHolder) && !(selection instanceof TCTLPathPlaceHolder)) {
            if(selection instanceof TCTLPathToStateConverter) {
                TCTLAbstractProperty child = ((TCTLPathToStateConverter) selection).getProperty();
                addExistsPathsHyperLTL(oldProperty, child, trace);
            } else {
                addExistsPathsHyperLTL(oldProperty, ((LTLENode) selection).getProperty(), trace);
            }
        } else {
            LTLENode parent = null;
            TCTLAbstractProperty child = null;

            parent = (LTLENode) getParentForHyperLTLExistsPath(selection);


            TCTLPathPlaceHolder placeHolder = new TCTLPathPlaceHolder();
            child = new LTLENode(ConvertToStateProperty(placeHolder), trace);

            if(parent != null) {
                parent.setProperty(ConvertToStateProperty((TCTLAbstractPathProperty)child));
            }

        }
    }

    private TCTLAbstractPathProperty getParentForHyperLTLExistsPath(TCTLAbstractProperty property) {
        if(property instanceof TCTLStatePlaceHolder) {
            return getParentForHyperLTLExistsPath(((TCTLStatePlaceHolder) property).getParent());
        } else if(property instanceof TCTLPathToStateConverter) {
            return getParentForHyperLTLExistsPath(((TCTLPathToStateConverter) property).getParent());
        } else if(property instanceof TCTLPathPlaceHolder) {
            return getParentForHyperLTLExistsPath(((TCTLPathPlaceHolder) property).getParent());
        } else if(!((((TCTLAbstractPathProperty) property).getParent()) instanceof LTLENode) && !(property instanceof LTLENode)) {
            return getParentForHyperLTLExistsPath(((TCTLAbstractPathProperty) property).getParent());
        }

        if(property instanceof LTLENode) return (TCTLAbstractPathProperty) property;

        return (TCTLAbstractPathProperty) ((TCTLAbstractPathProperty) property).getParent();
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
		kbounded.addActionListener(evt -> {
		    querySaved = true;
		    Verifier.analyzeKBound(tapnNetwork, lens, guiModels, getCapacity(), numberOfExtraTokensInNet, getQuery());
            querySaved = false;
        });
		boundednessCheckPanel.add(kbounded);

        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        uppaalOptionsPanel.add(boundednessCheckPanel, gridBagConstraints);
    }

    private void initSmcSettingsPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0,5,0,5);
        GridBagConstraints subPanelGbc = new GridBagConstraints();
        subPanelGbc.anchor = GridBagConstraints.WEST;
        subPanelGbc.insets = new Insets(0,5,0,5);

        FocusListener updater = new FocusListener() {
            public void focusGained(FocusEvent focusEvent) { }
            public void focusLost(FocusEvent focusEvent) {
                updateSMCSettings();
            }
        };

        smcSettingsPanel = new JPanel();
        smcSettingsPanel.setLayout(new GridBagLayout());
        smcSettingsPanel.setVisible(false);
        smcSettingsPanel.setBorder(BorderFactory.createTitledBorder("SMC Options"));
        gbc.gridy = 0;
        gbc.gridx = 0;

        JPanel smcEngineOptions = new JPanel();
        smcEngineOptions.setLayout(new GridBagLayout());
        smcEngineOptions.setBorder(BorderFactory.createTitledBorder("SMC engine options"));
        subPanelGbc.gridy = 0;
        subPanelGbc.gridx = 0;
        smcVerificationTypeLabel = new JLabel("Verification type : ");
        smcEngineOptions.add(smcVerificationTypeLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        subPanelGbc.fill = GridBagConstraints.HORIZONTAL;
        subPanelGbc.gridwidth = 2;
        smcVerificationType = new JComboBox<>(new String[]{ "Quantitative", "Qualitative", "Simulate" });
        smcVerificationType.setToolTipText(TOOL_TIP_ANALYSIS_TYPE);
        smcEngineOptions.add(smcVerificationType, subPanelGbc);
        subPanelGbc.fill = GridBagConstraints.NONE;
        subPanelGbc.gridwidth = 1;
        subPanelGbc.gridy = 1;
        subPanelGbc.gridx = 0;
        JLabel timeBoundLabel = new JLabel("Time bound : ");
        timeBoundLabel.setToolTipText(TOOL_TIP_TIME_BOUND);
        smcEngineOptions.add(timeBoundLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        subPanelGbc.fill = GridBagConstraints.HORIZONTAL;
        smcTimeBoundValue = new JTextField(7);
        DocumentFilters.applyIntegerFilter(smcTimeBoundValue);
        smcTimeBoundValue.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                int endIdx = smcTimeBoundValue.getText().length();
                smcTimeBoundValue.setSelectionStart(endIdx);
                smcTimeBoundValue.setSelectionEnd(endIdx);
            }
        });
        smcTimeBoundValue.setToolTipText(TOOL_TIP_TIME_BOUND);
        smcEngineOptions.add(smcTimeBoundValue, subPanelGbc);
        smcTimeBoundValue.addFocusListener(updater);
        subPanelGbc.fill = GridBagConstraints.NONE;
        subPanelGbc.gridx = 2;
        smcTimeBoundInfinite = new JCheckBox(Character.toString('∞'));
        smcTimeBoundInfinite.addActionListener(evt -> {
            if (!updatingSmcSettings) updateSMCSettings();
        });
        smcEngineOptions.add(smcTimeBoundInfinite, subPanelGbc);

        subPanelGbc.gridy = 2;
        subPanelGbc.gridx = 0;
        JLabel stepBoundLabel = new JLabel("Step bound : ");
        stepBoundLabel.setToolTipText(TOOL_TIP_STEP_BOUND);
        smcEngineOptions.add(stepBoundLabel, subPanelGbc);
    
        subPanelGbc.gridx = 1;
        subPanelGbc.fill = GridBagConstraints.HORIZONTAL;
        smcStepBoundValue = new JTextField(7);
        smcStepBoundValue.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                int endIdx = smcStepBoundValue.getText().length();
                smcStepBoundValue.setSelectionStart(endIdx);
                smcStepBoundValue.setSelectionEnd(endIdx);
            }
        });
        DocumentFilters.applyIntegerFilter(smcStepBoundValue);
        smcStepBoundValue.setToolTipText(TOOL_TIP_STEP_BOUND);
        smcEngineOptions.add(smcStepBoundValue, subPanelGbc);
        smcStepBoundValue.addFocusListener(updater);
        subPanelGbc.fill = GridBagConstraints.NONE;
        subPanelGbc.gridx = 2;
        smcStepBoundInfinite = new JCheckBox(Character.toString('∞'));
        smcStepBoundInfinite.addActionListener(evt -> {
            if (!updatingSmcSettings) updateSMCSettings();
        });
        smcEngineOptions.add(smcStepBoundInfinite, subPanelGbc);

        subPanelGbc.gridy = 3;
        subPanelGbc.gridx = 0;
        smcNumericPrecisionLabel = new JLabel("Numeric precision : ");
        smcNumericPrecisionLabel.setToolTipText(TOOL_TIP_NUMERIC_PRECISION);
        smcEngineOptions.add(smcNumericPrecisionLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        smcNumericPrecision = new CustomJSpinner(1, 1, 18);
        smcNumericPrecision.addChangeListener(e -> {
            if (!updatingSmcSettings) updateRawVerificationOptions();
        });

        smcNumericPrecision.setToolTipText(TOOL_TIP_NUMERIC_PRECISION);
        smcNumericPrecision.addFocusListener(updater);
        smcEngineOptions.add(smcNumericPrecision, subPanelGbc);

        subPanelGbc.gridy = 4;
        subPanelGbc.gridx = 0;
        smcParallelLabel = new JLabel("Use all available cores : ");
        smcEngineOptions.add(smcParallelLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        smcParallel = new JCheckBox();
        smcParallel.setSelected(true);
        smcEngineOptions.add(smcParallel, subPanelGbc);

        smcSettingsPanel.add(smcEngineOptions, gbc);
        gbc.gridx = 1;

        subPanelGbc.gridx = 0;
        subPanelGbc.gridy = 0;
        subPanelGbc.gridwidth = 1;
        quantitativePanel = new JPanel();
        quantitativePanel.setLayout(new GridBagLayout());
        quantitativePanel.setBorder(BorderFactory.createTitledBorder("Quantitative estimation options"));
        subPanelGbc.gridy = 0;
        subPanelGbc.gridx = 0;
        quantitativePanel.add(new JLabel("Confidence : "), subPanelGbc);
        subPanelGbc.gridx = 1;
        smcConfidence = new JTextField(7);
        smcConfidence.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                int endIdx = smcConfidence.getText().length();
                smcConfidence.setSelectionStart(endIdx);
                smcConfidence.setSelectionEnd(endIdx);
            }
        });
        DocumentFilters.applyDoubleFilter(smcConfidence);
        smcConfidence.addFocusListener(updater);
        smcConfidence.setToolTipText(TOOL_TIP_CONFIDENCE);
        quantitativePanel.add(smcConfidence, subPanelGbc);
        subPanelGbc.gridx = 2;
        smcConfidenceSlider = new QuerySlider(95, 0.80, 0.99);
        smcConfidenceSlider.setToolTipText("Value: 0.95");
        smcConfidenceSlider.addChangeListener(e -> {
            if (updatingSmcSettings) return;
            int value = smcConfidenceSlider.getValue();
            double desiredMin = smcConfidenceSlider.getDesiredMin();
            double desiredMax = smcConfidenceSlider.getDesiredMax();
            double proportion = (double) value / smcConfidenceSlider.getMaximum();
            double interpretedValue = desiredMin + proportion * (desiredMax - desiredMin);
            double roundedValue = Math.round(interpretedValue * 100.0) / 100.0;
            smcConfidence.setText(roundedValue + "");
            smcConfidenceSlider.setRealValue(roundedValue);
            smcConfidenceSlider.setToolTipText(String.format("Value: %.2f", roundedValue));
            smcMustUpdateTime = true;
        });

        quantitativePanel.add(smcConfidenceSlider, subPanelGbc);
        subPanelGbc.gridy = 1;
        subPanelGbc.gridx = 0;
        quantitativePanel.add(new JLabel("Precision : "), subPanelGbc);
        subPanelGbc.gridx = 1;
        smcEstimationIntervalWidth = new JTextField(7);
        smcEstimationIntervalWidth.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                int endIdx = smcEstimationIntervalWidth.getText().length();
                smcEstimationIntervalWidth.setSelectionStart(endIdx);
                smcEstimationIntervalWidth.setSelectionEnd(endIdx);
            }
        });
        DocumentFilters.applyDoubleFilter(smcEstimationIntervalWidth);
        smcEstimationIntervalWidth.addFocusListener(updater);
        smcEstimationIntervalWidth.setToolTipText(TOOL_TIP_INTERVAL_WIDTH);
        quantitativePanel.add(smcEstimationIntervalWidth, subPanelGbc);
        subPanelGbc.gridx = 2;
        smcPrecisionSlider = new QuerySlider(0, 0.5, 0.0001);
        smcPrecisionSlider.setToolTipText("Value: 0.5000");
        smcPrecisionSlider.addChangeListener(e -> {
            if (updatingSmcSettings) return;
            int value = smcPrecisionSlider.getValue();
            double desiredMin = smcPrecisionSlider.getDesiredMin();
            double desiredMax = smcPrecisionSlider.getDesiredMax();
            double logMin = Math.log(desiredMin);
            double logMax = Math.log(desiredMax);
            double proportion = (double) value / smcPrecisionSlider.getMaximum();
            double logValue = logMin + proportion * (logMax - logMin);
            double interpretedValue = Math.exp(logValue);
            smcPrecisionSlider.setRealValue(interpretedValue);
            double roundedValue = Math.round(interpretedValue * 10000.0) / 10000.0;
            DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#.####", decimalFormatSymbols);
            String formattedValue = df.format(roundedValue);
            smcEstimationIntervalWidth.setText(formattedValue);
            smcPrecisionSlider.setToolTipText(String.format("Value: %s", formattedValue));
            smcMustUpdateTime = true;
        });

        quantitativePanel.add(smcPrecisionSlider, subPanelGbc);
        subPanelGbc.gridy = 2;
        subPanelGbc.gridx = 0;
        JLabel verifTimeLabel = new JLabel("Estimated verification time (seconds) : ");
        verifTimeLabel.setToolTipText(TOOL_TIP_VERIFICATION_TIME);
        quantitativePanel.add(verifTimeLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        smcTimeExpected = new JTextField(7);
        smcTimeExpected.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                int endIdx = smcTimeExpected.getText().length();
                smcTimeExpected.setSelectionStart(endIdx);
                smcTimeExpected.setSelectionEnd(endIdx);
            }
        });
        DocumentFilters.applyDoubleFilter(smcTimeExpected);
        smcTimeExpected.setToolTipText(TOOL_TIP_VERIFICATION_TIME);
        quantitativePanel.add(smcTimeExpected, subPanelGbc);

        smcEstimatedTimeSlider = new QuerySlider(0, 1, 100, 99);
        smcEstimatedTimeSlider.setToolTipText("Value: 1.0");
        smcEstimatedTimeSlider.addChangeListener(e -> {
            if (updatingSmcSettings) return;
            int value = smcEstimatedTimeSlider.getValue();
            double desiredMin = smcEstimatedTimeSlider.getDesiredMin();
            double desiredMax = smcEstimatedTimeSlider.getDesiredMax();
            double proportion = (double) value / smcEstimatedTimeSlider.getMaximum();
            double interpretedValue = desiredMin + proportion * (desiredMax - desiredMin);
            double roundedValue = Math.round(interpretedValue);
            smcTimeExpected.setText(roundedValue + "");
            smcEstimatedTimeSlider.setRealValue(roundedValue);
            smcEstimatedTimeSlider.setToolTipText(String.format("Value: %.1f", roundedValue));
            smcMustUpdateTime = false;
        });

        subPanelGbc.gridx = 2;
        quantitativePanel.add(smcEstimatedTimeSlider, subPanelGbc);

        subPanelGbc.gridy = 3;
        subPanelGbc.gridx = 0;
        subPanelGbc.gridwidth = 2;
        subPanelGbc.fill = GridBagConstraints.HORIZONTAL;
        smcTimeEstimationButton = new JButton(UPDATE_VERIFICATION_TIME_BTN_TEXT);
        smcTimeEstimationButton.setPreferredSize(new Dimension(378, 25));
        smcTimeEstimationButton.addActionListener(evt -> {
            runBenchmark();
        });
        quantitativePanel.add(smcTimeEstimationButton, subPanelGbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        smcSettingsPanel.add(quantitativePanel, gbc);

        subPanelGbc.gridwidth = 1;
        subPanelGbc.anchor = GridBagConstraints.WEST;
        qualitativePanel = new JPanel();
        qualitativePanel.setLayout(new GridBagLayout());
        qualitativePanel.setBorder(BorderFactory.createTitledBorder("Qualitative estimation options"));
        qualitativePanel.setVisible(false);
        subPanelGbc.gridy = 0;
        subPanelGbc.gridx = 0;
        qualitativePanel.add(new JLabel("False positives : "), subPanelGbc);
        subPanelGbc.gridx = 1;
        smcFalsePositives = new JTextField(7);
        DocumentFilters.applyDoubleFilter(smcFalsePositives);
        smcFalsePositives.addFocusListener(updater);
        smcFalsePositives.setToolTipText(TOOL_TIP_FALSE_POSITIVES);
        qualitativePanel.add(smcFalsePositives, subPanelGbc);
        subPanelGbc.gridx = 2;
        smcFalsePositivesSlider = new QuerySlider(0, 0.001, 0.5);
        smcFalsePositivesSlider.setToolTipText("Value: 0.001");
        smcFalsePositivesSlider.addChangeListener(e -> {
            if (!updatingSmcSettings) smcFalsePositivesSlider.updateValue(smcFalsePositives, 3);
        });
        qualitativePanel.add(smcFalsePositivesSlider, subPanelGbc);
        subPanelGbc.gridy = 1;
        subPanelGbc.gridx = 0;
        qualitativePanel.add(new JLabel("False negatives : "), subPanelGbc);
        subPanelGbc.gridx = 1;
        smcFalseNegatives = new JTextField(7);
        DocumentFilters.applyDoubleFilter(smcFalseNegatives);
        smcFalseNegatives.addFocusListener(updater);
        smcFalseNegatives.setToolTipText(TOOL_TIP_FALSE_NEGATIVES);
        qualitativePanel.add(smcFalseNegatives, subPanelGbc);
        subPanelGbc.gridx = 2;
        smcFalseNegativesSlider = new QuerySlider(0, 0.001, 0.5);
        smcFalseNegativesSlider.setToolTipText("Value: 0.001");
        smcFalseNegativesSlider.addChangeListener(e -> {
            if (!updatingSmcSettings) smcFalseNegativesSlider.updateValue(smcFalseNegatives, 3);
        });
        qualitativePanel.add(smcFalseNegativesSlider, subPanelGbc);
        subPanelGbc.gridy = 2;
        subPanelGbc.gridx = 0;
        qualitativePanel.add(new JLabel("Indifference region width : "), subPanelGbc);
        subPanelGbc.gridx = 1;
        smcIndifference = new JTextField(7);
        DocumentFilters.applyDoubleFilter(smcIndifference);
        smcIndifference.addFocusListener(updater);
        smcIndifference.setToolTipText(TOOL_TIP_INDIFFERENCE);
        qualitativePanel.add(smcIndifference, subPanelGbc);
        subPanelGbc.gridx = 2;
        smcIndifferenceSlider = new QuerySlider(100, 0.001, 0.5);
        smcIndifferenceSlider.setToolTipText("Value: 0.500");
        smcIndifferenceSlider.addChangeListener(e -> {
            if (!updatingSmcSettings) smcIndifferenceSlider.updateValue(smcIndifference, 3);
        });
        qualitativePanel.add(smcIndifferenceSlider, subPanelGbc);
        subPanelGbc.gridy = 3;
        subPanelGbc.gridx = 0;
        JLabel testLabel = new JLabel("Property hold with probability >= ");
        testLabel.setToolTipText(TOOL_TIP_QUALITATIVE_TEST);
        qualitativePanel.add(testLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        smcComparisonFloat = new JTextField(7);
        DocumentFilters.applyDoubleFilter(smcComparisonFloat);
        smcComparisonFloat.setToolTipText(TOOL_TIP_QUALITATIVE_TEST);
        smcComparisonFloat.addFocusListener(updater);
        qualitativePanel.add(smcComparisonFloat, subPanelGbc);
        subPanelGbc.gridx = 2;
        smcComparisonFloatSlider = new QuerySlider(50, 0.01, 0.99, 98);
        smcComparisonFloatSlider.setToolTipText("Value: 0.50");
        smcComparisonFloatSlider.addChangeListener(e -> {
            if (!updatingSmcSettings) smcComparisonFloatSlider.updateValue(smcComparisonFloat, 2);
        });
        qualitativePanel.add(smcComparisonFloatSlider, subPanelGbc);

        smcSettingsPanel.add(qualitativePanel, gbc);
    
        smcTracePanel = new JPanel();
        smcTracePanel.setLayout(new GridBagLayout());
        smcTracePanel.setBorder(BorderFactory.createTitledBorder("Trace options"));
        smcTracePanel.setVisible(false);
        subPanelGbc.gridy = 0;
        subPanelGbc.gridx = 0;
        JLabel numberOfTracesLabel = new JLabel("Number of traces : ");
        numberOfTracesLabel.setToolTipText(TOOL_TIP_N_TRACES);
        smcTracePanel.add(numberOfTracesLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        smcNumberOfTraces = new CustomJSpinner(1, 1, Integer.MAX_VALUE);
        smcNumberOfTraces.addFocusListener(updater);
        smcNumberOfTraces.setToolTipText(TOOL_TIP_N_TRACES);
        smcTracePanel.add(smcNumberOfTraces, subPanelGbc);
        subPanelGbc.gridy = 1;
        smcTracePanel.add(Box.createVerticalStrut(5), subPanelGbc);
        JLabel traceTypeLabel = new JLabel("Trace type : ");
        traceTypeLabel.setToolTipText(TOOL_TIP_TRACE_TYPE);
        subPanelGbc.gridx = 0;
        subPanelGbc.gridy = 2;
        smcTracePanel.add(traceTypeLabel, subPanelGbc);
        subPanelGbc.gridx = 1;
        smcTraceType = new JComboBox<>(new SMCTraceType[]{ new SMCTraceType("Any"), 
                                                           new SMCTraceType("Satisfied"), 
                                                           new SMCTraceType("Not satisfied") });
        smcTraceType.setToolTipText(TOOL_TIP_TRACE_TYPE);
        smcTracePanel.add(smcTraceType, subPanelGbc);
  
        smcSettingsPanel.add(smcTracePanel, gbc);

        smcVerificationType.addActionListener(evt -> {
            boolean quantitative = smcVerificationType.getSelectedIndex() == 0;
            boolean qualitative = smcVerificationType.getSelectedIndex() == 1;
            boolean simulate = smcVerificationType.getSelectedIndex() == 2;

            quantitativePanel.setVisible(quantitative);
            qualitativePanel.setVisible(qualitative);
            smcTracePanel.setVisible(simulate);

            guiDialog.pack();
        });

        JLabel granualityLabel = new JLabel("Granularity : ");
        smcGranularityField = new JTextField(7);
        DocumentFilters.applyIntegerFilter(smcGranularityField);
        smcGranularityField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                updateRawVerificationOptions();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        granualityLabel.setToolTipText(TOOL_TIP_GRANULARITY);
        smcGranularityField.setToolTipText(TOOL_TIP_GRANULARITY);

        smcMaxGranularityCheckbox = new JCheckBox(Character.toString('∞'));
        smcMaxGranularityCheckbox.addActionListener(e -> {
            smcGranularityField.setEnabled(!smcMaxGranularityCheckbox.isSelected());
            updateRawVerificationOptions();
        });

        JButton smcObservationsButton = new JButton("Edit observations");
        smcObservationsButton.addActionListener(evt -> {
            ObservationListDialog dialog = new ObservationListDialog(tapnNetwork, smcObservations);
            dialog.setLocationRelativeTo(guiDialog);
            dialog.setVisible(true);
        });

        JPanel smcObservationsPanel = new JPanel();
        smcObservationsPanel.setLayout(new GridBagLayout());
        smcObservationsPanel.setBorder(BorderFactory.createTitledBorder("SMC Observations"));

        subPanelGbc.gridx = 0;
        subPanelGbc.gridy = 0;
        subPanelGbc.anchor = GridBagConstraints.WEST;
        subPanelGbc.weightx = 0;
        smcObservationsPanel.add(smcObservationsButton, subPanelGbc);

        subPanelGbc.gridx = 1;
        smcObservationsPanel.add(granualityLabel, subPanelGbc);

        subPanelGbc.gridx = 2;
        smcObservationsPanel.add(smcGranularityField, subPanelGbc);

        subPanelGbc.gridx = 3;
        smcObservationsPanel.add(smcMaxGranularityCheckbox, subPanelGbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        smcSettingsPanel.add(smcObservationsPanel, gbc);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5,10,5,10);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1;
        add(smcSettingsPanel, gridBagConstraints);

        Dimension quantitativeSize = quantitativePanel.getPreferredSize();
        Dimension qualitativeSize = qualitativePanel.getPreferredSize();
        Dimension smcTraceSize = smcTracePanel.getPreferredSize();

        int maxWidth = Math.max(quantitativeSize.width, Math.max(qualitativeSize.width, smcTraceSize.width));
        int maxHeight = Math.max(quantitativeSize.height, Math.max(qualitativeSize.height, smcTraceSize.height));
        Dimension largestSize = new Dimension(maxWidth, maxHeight);
        
        quantitativePanel.setPreferredSize(largestSize);
        qualitativePanel.setPreferredSize(largestSize);
        smcTracePanel.setPreferredSize(largestSize);

        setupEstimationListeners();
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
        addTraceButton = new JButton("Edit traces");

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
        addTraceButton.setToolTipText(TOOL_TIP_ADDTRACEBUTTON);

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
        quantificationButtonGroup.add(addTraceButton);

        // Place buttons in GUI
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

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
        gbc.gridx = 1;
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

        // Traces button at the bottom of the panel
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        addTraceButton.setPreferredSize(new Dimension(78, 27));
        quantificationPanel.add(addTraceButton, gbc);

        // Add quantification panel to query panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        queryPanel.add(quantificationPanel, gbc);

        if (lens.isTimed()|| lens.isGame()) {
            addTimedQuantificationListeners();
            if(lens.isStochastic()) {
                addSmcQuantificationListerners();
            }
            showLTLButtons(false);
            showHyperLTLButtons(false);
        } else {
            addUntimedQuantificationListeners();
            showLTLButtons(false);
            showHyperLTLButtons(false);
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

    private void addSmcQuantificationListerners() {
        finallyButton.addActionListener(e -> {
            LTLFNode property = new LTLFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            addPropertyToQuery(property);
        });

        globallyButton.addActionListener(e -> {
            LTLGNode property = new LTLGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            addPropertyToQuery(property);
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

        existsNext.addActionListener(e -> {
            TCTLAbstractPathProperty property;
            if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                property = new TCTLEXNode((TCTLAbstractStateProperty) currentSelection.getObject());
            } else {
                property = new TCTLEXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
        });

        existsUntil.addActionListener(e -> {
            TCTLAbstractPathProperty property;
            if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                property = new TCTLEUNode((TCTLAbstractStateProperty) currentSelection.getObject(),
                    new TCTLStatePlaceHolder());
            } else {
                property = new TCTLEUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                    getSpecificChildOfProperty(2, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
        });

        globallyButton.addActionListener(e -> {
            LTLGNode property;
            if (currentSelection.getObject() instanceof LTLANode || currentSelection.getObject() instanceof LTLENode) {
                property = new LTLGNode();
            } else {
                property = new LTLGNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
            unselectButtons();
        });

        finallyButton.addActionListener(e -> {
            LTLFNode property;
            if (currentSelection.getObject() instanceof LTLANode || currentSelection.getObject() instanceof LTLENode) {
                property = new LTLFNode();
            } else {
                property = new LTLFNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
            unselectButtons();
        });

        forAllNext.addActionListener(e -> {
            TCTLAbstractPathProperty property;
            if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                property = new TCTLAXNode((TCTLAbstractStateProperty) currentSelection.getObject());
            } else {
                property = new TCTLAXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
        });

        forAllUntil.addActionListener(e -> {
            TCTLAbstractPathProperty property;
            if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                property = new TCTLAUNode((TCTLAbstractStateProperty) currentSelection.getObject(),
                    new TCTLStatePlaceHolder());
            } else {
                property = new TCTLAUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                    getSpecificChildOfProperty(2, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
        });

        nextButton.addActionListener(e -> {
            TCTLAbstractPathProperty property;
            if (currentSelection.getObject() instanceof LTLANode || currentSelection.getObject() instanceof LTLENode) {
                property = new LTLXNode();
            } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                property = new LTLXNode((TCTLAbstractStateProperty) currentSelection.getObject());
            } else {
                property = new LTLXNode(getSpecificChildOfProperty(1, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
        });

        untilButton.addActionListener(e -> {
            TCTLAbstractPathProperty property;
            if (currentSelection.getObject() instanceof LTLANode || currentSelection.getObject() instanceof LTLENode) {
                property = new LTLUNode();
            } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                property = new LTLUNode((TCTLAbstractStateProperty) currentSelection.getObject(),
                    new TCTLStatePlaceHolder());
            } else {
                property = new LTLUNode(getSpecificChildOfProperty(1, currentSelection.getObject()),
                    getSpecificChildOfProperty(2, currentSelection.getObject()));
            }
            addPropertyToQuery(property);
        });

        aButton.addActionListener(e -> {
            TCTLAbstractProperty oldProperty = newProperty;
            if (!(queryType.getSelectedIndex() == 2)) {
                newProperty = removeExistsAllPathsFromProperty(newProperty);
                addAllPathsToProperty(newProperty, null);
            } else {
                // Check if there already exists an all-path with the current trace
                String selectedTrace = traceBoxQuantification.getSelectedItem().toString();
                if(oldProperty.toString().contains("A " + selectedTrace)) {
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "An all-path with trace \"" + selectedTrace + "\" already exists. Please chose a different trace.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    isAllPath = true;
                    isExistsPath = false;
                } else {
                    addAllPathsToProperty(newProperty, oldProperty);
                    isAllPath = true;
                    isExistsPath = false;
                }
            }

            UndoableEdit edit = new QueryConstructionEdit(oldProperty, newProperty);
            undoSupport.postEdit(edit);

            queryChanged();
        });

        eButton.addActionListener(e -> {
            TCTLAbstractProperty oldProperty = newProperty;

            if (queryType.getSelectedIndex() != 2) {
                newProperty = removeExistsAllPathsFromProperty(newProperty);
                addExistsPathsToProperty(newProperty, null);
            } else if (traceBoxQuantification.getSelectedItem() != null) {
                // Check if there already exists an exists-path with the current trace
                String selectedTrace = traceBoxQuantification.getSelectedItem().toString();
                if (oldProperty.toString().contains("E " + selectedTrace)) {
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "An exists-path with trace \"" + selectedTrace + "\" already exists. Please chose a different trace.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    isAllPath = false;
                    isExistsPath = true;
                } else {
                    addExistsPathsToProperty(newProperty, oldProperty);
                    isAllPath = false;
                    isExistsPath = true;
                }
            }


            UndoableEdit edit = new QueryConstructionEdit(oldProperty, newProperty);
            undoSupport.postEdit(edit);

            queryChanged();

        });

        addTraceButton.addActionListener(e -> initTraceBoxDialogComponents());
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

    private void showHyperLTL(boolean isVisble) {
        traceRow.setVisible(isVisble);
        addTraceButton.setVisible(isVisble);
        traceBoxQuantification.setVisible(isVisble);
        guiDialog.pack();
    }

    private void showHyperLTLButtons(boolean isVisble) {
        addTraceButton.setVisible(isVisble);
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

    private void showSMCButtons(boolean isVisible) {
        showCTLButtons(!isVisible);
        globallyButton.setVisible(isVisible);
        finallyButton.setVisible(isVisible);
        deadLockPredicateButton.setVisible(isVisible);
    }

    private void updateSiphonTrap(boolean isCTL) {
        useSiphonTrap.setEnabled(isCTL);
    }

    private void addPropertyToQuery(TCTLAbstractPathProperty property) {
        TCTLAbstractProperty selection = currentSelection.getObject();
        if (selection instanceof TCTLAbstractStateProperty) {
            addPropertyToQuery(ConvertToStateProperty(property));
            return;
        }

        if (selection instanceof LTLANode) {
            if (queryType.getSelectedIndex() == 2 && property instanceof LTLANode) {
                addAllPathsToProperty(newProperty, selection);
                return;
            }
            newProperty = newProperty.replace(selection, property);
            if (property instanceof LTLANode) addAllPathsToProperty(newProperty, selection);
            return;
        } else if (selection instanceof LTLENode) {
            if (queryType.getSelectedIndex() == 2 && property instanceof LTLENode) {
                addExistsPathsToProperty(newProperty, selection);
                return;
            }

            newProperty = newProperty.replace(selection, property);
            if (property instanceof LTLENode)
                addExistsPathsToProperty(newProperty, selection);
            else {
                UndoableEdit edit = new QueryConstructionEdit(selection, property);
                updateSelection(property);
                undoSupport.postEdit(edit);
                queryChanged();
            }

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
        conjunctionButton.addActionListener(evt -> {
            TCTLAndListNode andListNode = null;
            if (currentSelection.getObject() instanceof TCTLAndListNode) {
                andListNode = new TCTLAndListNode((TCTLAndListNode) currentSelection.getObject());
                andListNode.setSimpleProperty(true);
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
                    UndoableEdit edit = new QueryConstructionEdit(parentNode, andListNode);
                    newProperty = newProperty.replace(parentNode, andListNode);
                    updateSelection(andListNode);
                    undoSupport.postEdit(edit);
                    queryChanged();
                } else {
                    TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
                    andListNode = new TCTLAndListNode(getStateProperty(currentSelection.getObject()), ph);

                    addPropertyToQuery(andListNode);
                }
            } else if (!lens.isTimed()) {
                checkUntimedAndNode();
            }
        });

        disjunctionButton.addActionListener(e -> {
            TCTLOrListNode orListNode;

            if (currentSelection.getObject() instanceof TCTLOrListNode) {
                orListNode = new TCTLOrListNode((TCTLOrListNode) currentSelection.getObject());
                orListNode.setSimpleProperty(true);
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
                    UndoableEdit edit = new QueryConstructionEdit(parentNode, orListNode);
                    newProperty = newProperty.replace(parentNode, orListNode);
                    updateSelection(orListNode);
                    undoSupport.postEdit(edit);
                    queryChanged();
                } else {
                    TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
                    orListNode = new TCTLOrListNode(getStateProperty(currentSelection.getObject()), ph);
                    addPropertyToQuery(orListNode);
                }
            } else if (!lens.isTimed()) {
                checkUntimedOrNode();
            }
        });

        negationButton.addActionListener(e -> {
            if (lens.isTimed()) {
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

            andListNode.setSimpleProperty(true);

            TCTLAbstractPathProperty property = new TCTLStateToPathConverter(andListNode);
            addPropertyToQuery(property);
        } else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
            TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();

            TCTLAbstractProperty oldProperty = removeExistsAllPathsFromProperty(currentSelection.getObject());

            andListNode = new TCTLAndListNode(getStateProperty(
                new TCTLPathToStateConverter((TCTLAbstractPathProperty) oldProperty)), ph);
            
            if (previousProp instanceof TCTLAndListNode) {
                andListNode.setSimpleProperty(true);
            }

            previousProp = andListNode;
            
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

            orListNode.setSimpleProperty(true);

            TCTLAbstractPathProperty property = new TCTLStateToPathConverter(orListNode);
            addPropertyToQuery(property);
        } else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
            TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
            TCTLAbstractProperty oldProperty = removeExistsAllPathsFromProperty(currentSelection.getObject());

            orListNode = new TCTLOrListNode(getStateProperty(
                new TCTLPathToStateConverter((TCTLAbstractPathProperty) oldProperty)), ph);
            
            if (previousProp instanceof TCTLOrListNode) {
                orListNode.setSimpleProperty(true);
            }

            previousProp = orListNode;
              
            TCTLAbstractPathProperty property = new TCTLStateToPathConverter(orListNode);
            addPropertyToQuery(property);
        }
    }

    private void updateTraceBox() {
        // Updates the trace box drop down menu
        List<String> traceList = getUsedTraces(newProperty);
        Vector<Object> traceBoxVector = new Vector<>();
        Vector<Object> traceBoxQuantificationVector = new Vector<>();
        for (int i = 0; i < traceModel.getSize(); i++) {
            if (traceList.contains(traceModel.get(i).toString())) {
                traceBoxVector.add(traceModel.get(i));
            } else {
                traceBoxQuantificationVector.add(traceModel.get(i));
            }
        }

        traceBox.setModel(new DefaultComboBoxModel<>(traceBoxVector));
        traceBoxQuantification.setModel(new DefaultComboBoxModel<>(traceBoxQuantificationVector));
        updateHyperLTLButtons();
    }

    private ArrayList<String> getUsedTraces(TCTLAbstractProperty current) {
        ArrayList<String> usedTraces = new ArrayList<>();
        if (current instanceof LTLANode) {
            usedTraces.add(((LTLANode) current).getTrace());
        } else if (current instanceof LTLENode) {
            usedTraces.add(((LTLENode) current).getTrace());
        }
        for (StringPosition child : current.getChildren()) {
            usedTraces.addAll(getUsedTraces(child.getObject()));
        }
        return usedTraces;
    }

    private void initTracePanels() {
        traceList = new JList();
        traceModel = new DefaultListModel();
        traceModel.addElement("T1");
        traceModel.addElement("T2");

        for(int i = 0; i < traceList.getModel().getSize(); i++) {
            traceModel.addElement(traceList.getModel().getElementAt(i));
        }

        traceList.setModel(traceModel);

        // Init temp trace list
        // If the user cancels, we dont want to save these new trace names
        tempTraceModel = new DefaultListModel();
        tempTraceList = new JList();

        // Init trace field
        traceNameTextField = new JTextField();
        traceNameTextField.setText("");

        // Drop down menu for traces
        Vector<Object> tracesVector = new Vector<>();
        for(int i = 0; i < traceModel.getSize(); i++) {
            tracesVector.add(traceModel.get(i));
        }

        traceBox = new JComboBox<>(new DefaultComboBoxModel<>());
        traceBoxQuantification = new JComboBox<>(new DefaultComboBoxModel<>(tracesVector));

        traceBox.addActionListener(e -> {
            if (updateTraceBox) updateQueryOnAtomicPropositionChange();
        });
        traceBoxQuantification.addActionListener(e -> {
            if (checkTraceBoxQuantification() && updateTraceBoxQuantification) updateQueryOnQuantificationChange();
        });

        traceBox.setEnabled(false);
        traceBox.setToolTipText(TOOL_TIP_TRACEBOX);
        traceBoxQuantification.setToolTipText(TOOL_TIP_TRACEBOX_QUANTIFICATION);

        // Add to the quantification panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(2,2,4,2);
        ((JLabel)traceBoxQuantification.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        traceBoxQuantification.setPreferredSize(new Dimension(76,27));
        quantificationPanel.add(traceBoxQuantification, gbc);

        Dimension dim = new Dimension(292, 27);
        traceBox.setMaximumSize(dim);
        traceBox.setMinimumSize(dim);
        traceBox.setPreferredSize(dim);
        traceBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(1, 0, 1, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        traceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        traceRow.add(traceBox);
        gbc.gridy = 1;
        predicatePanel.add(traceRow, gbc);
        traceRow.setVisible(false);
    }

    private boolean checkTraceBoxQuantification() {
        if (currentSelection == null || currentSelection.getObject() != newProperty || traceBoxQuantification.getSelectedItem() == null) return false;

        String traceName = traceBoxQuantification.getSelectedItem().toString();
        for (int i = 0; i < traceBox.getModel().getSize(); i++) {
            if (traceName.equals(traceBox.getModel().getElementAt(i).toString())) {
                return true;
            }
        }
        return false;
    }

    private void initTraceBoxDialogComponents() {
        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());

        final JPanel traceBoxPanel = new JPanel();
        traceBoxPanel.setLayout(new GridBagLayout());
        traceBoxPanel.setBorder(BorderFactory.createTitledBorder("Traces"));

        JButton moveUpButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("Images/Up.png"))));
        JButton moveDownButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("Images/Down.png"))));

        JPanel firstRow = new JPanel();
        firstRow.setLayout(new GridBagLayout());

        JLabel traceNameLabel = new JLabel("Trace name: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        firstRow.add(traceNameLabel, gbc);

        traceTextField = new JTextField();
        GridBagConstraints gbcNTF = new GridBagConstraints();
        gbcNTF.gridx = 1;
        gbcNTF.gridy = 0;
        gbcNTF.gridwidth = 1;
        gbcNTF.weightx = 1.0;
        gbcNTF.anchor = GridBagConstraints.WEST;
        gbcNTF.fill = GridBagConstraints.HORIZONTAL;
        gbcNTF.insets = new Insets(4, 4, 2, 4);
        traceTextField.requestFocusInWindow();
        firstRow.add(traceTextField, gbcNTF);

        traceTextField.addActionListener(e -> addTracePanelButton.doClick());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        traceBoxPanel.add(firstRow, gbc);

        JPanel secondRow = new JPanel();
        secondRow.setLayout(new GridBagLayout());


        addTracePanelButton = new JButton("Add trace");
        addTracePanelButton.addActionListener(e -> {
            String traceName = traceTextField.getText();
            addNewTrace(traceName, true);
        });

        Dimension buttonSize = new Dimension(100, 30);
        addTracePanelButton.setMaximumSize(buttonSize);
        addTracePanelButton.setMinimumSize(buttonSize);
        addTracePanelButton.setPreferredSize(buttonSize);
        addTracePanelButton.setMnemonic(KeyEvent.VK_A);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        secondRow.add(addTracePanelButton, gbc);

        traceRemoveButton = new JButton("Remove");
        traceRemoveButton.setEnabled(false);
        traceRemoveButton.addActionListener(actionEvent -> removeTraces());
        traceRemoveButton.setPreferredSize(buttonSize);
        traceRemoveButton.setMinimumSize(buttonSize);
        traceRemoveButton.setMaximumSize(buttonSize);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        secondRow.add(traceRemoveButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        traceBoxPanel.add(secondRow, gbc);

        JPanel thirdRow = new JPanel();
        thirdRow.setLayout(new GridBagLayout());


        traceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                JList source = (JList) e.getSource();
                if (source.getSelectedIndex() == -1) {
                    traceRemoveButton.setEnabled(false);
                    moveUpButton.setEnabled(false);
                    moveDownButton.setEnabled(false);
                } else {
                    traceRemoveButton.setEnabled(true);
                    if (source.getSelectedIndex() > 0) {
                        moveUpButton.setEnabled(true);
                    } else {
                        moveUpButton.setEnabled(false);
                    }

                    if (source.getSelectedIndex() < source.getModel().getSize() - 1) {
                        moveDownButton.setEnabled(true);
                    } else {
                        moveDownButton.setEnabled(false);
                    }
                }
            }
        });

        JScrollPane traceListScrollPane = new JScrollPane(traceList);
        traceListScrollPane.setViewportView(traceList);
        traceListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        traceListScrollPane.setVisible(true);
        traceListScrollPane.setBorder(new LineBorder(Color.GRAY));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        thirdRow.add(traceListScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 3, 3, 3);

        thirdRow.add(moveUpButton, gbc);
        moveUpButton.addActionListener(e -> {
            int index = traceList.getSelectedIndex();
            if (index > 0) {
                traceList.setSelectedIndex(index - 1);
                swapTraces(traceModel, index, index - 1);

            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(3, 3, 3, 3);

        thirdRow.add(moveDownButton, gbc);
        moveDownButton.addActionListener(e -> {
            int index = traceList.getSelectedIndex();
            if (index < traceModel.getSize() - 1) {
                traceList.setSelectedIndex(index + 1);
                swapTraces(traceModel, index, index + 1);

            }
        });

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        traceBoxPanel.add(thirdRow, gbc);

        traceList.clearSelection();
        moveUpButton.setEnabled(false);
        moveDownButton.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 8, 0, 8);
        container.add(traceBoxPanel, gbc);

        JPanel buttonPanel = createTraceButtonPanel();
        gbc.insets = new Insets(0, 8, 5, 8);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        container.add(buttonPanel, gbc);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(container);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        traceDialog = new EscapableDialog(TAPAALGUI.getApp(),
            "Traces", true);
        traceDialog.add(scrollPane, BorderLayout.CENTER);
        traceDialog.getRootPane().setDefaultButton(okButton);
        traceDialog.setResizable(true);
        traceDialog.pack();
        //size of range of integers panel
        traceDialog.setMinimumSize(new Dimension(447, 231));
        traceDialog.setLocationRelativeTo(null);

        UpdateTempTraceList();

        traceDialog.setVisible(true);
        updateTraceBox();
    }

    private void UpdateTempTraceList() {
        tempTraceModel.removeAllElements();

        for (int i = 0; i < traceList.getModel().getSize(); i++) {
            tempTraceModel.addElement(traceList.getModel().getElementAt(i));
        }
        tempTraceList.setModel(tempTraceModel);
    }

    private void addNewTrace(String traceName, boolean isFromTraceDialogBox) {
        if (traceName == null || traceName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(), "You have to enter a name for the trace",
                "Error", JOptionPane.ERROR_MESSAGE);
        } else if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", traceName)) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "Acceptable names for traces are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
                "Error", JOptionPane.ERROR_MESSAGE);
        } else if (traceName.equals("all") || traceName.equals("All") || traceName.equals("dot") || traceName.equals(".all") || traceName.equals(".All")) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "The trace cannot be named \"" + traceName + "\", as the name is reserved",
                "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            boolean nameIsInUse = traceNameTextField.getText().equals(traceName);
            for (int i = 0; i < traceModel.getSize(); i++) {
                String n = traceModel.getElementAt(i).toString();

                if (n.equals(traceName)) {
                    nameIsInUse = true;
                    break;
                }
            }

            if (nameIsInUse && isFromTraceDialogBox) {
                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "A trace with the name \"" + traceName + "\" already exists. Please chose another name.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!nameIsInUse) {
                traceModel.addElement(traceName);
                traceList.setModel(traceModel);

                if(isFromTraceDialogBox) traceTextField.setText("");
            }
        }

        if(isFromTraceDialogBox) traceTextField.requestFocusInWindow();
    }

    private JPanel createTraceButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        okButton = new JButton("OK");
        okButton.setMaximumSize(new Dimension(100, 25));
        okButton.setMinimumSize(new Dimension(100, 25));
        okButton.setPreferredSize(new Dimension(100, 25));

        okButton.setMnemonic(KeyEvent.VK_O);
        GridBagConstraints gbcOk = new GridBagConstraints();
        gbcOk.gridx = 1;
        gbcOk.gridy = 0;
        gbcOk.anchor = GridBagConstraints.WEST;
        gbcOk.insets = new Insets(5, 5, 5, 5);

        okButton.addActionListener(actionEvent -> exitTraceDialog());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(100, 25));
        cancelButton.setMinimumSize(new Dimension(100, 25));
        cancelButton.setPreferredSize(new Dimension(100, 25));
        cancelButton.setMnemonic(KeyEvent.VK_C);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;

        cancelButton.addActionListener(e -> {
            cancelTraceChanges();
            exitTraceDialog();
        });

        buttonPanel.add(cancelButton, gbc);
        buttonPanel.add(okButton, gbcOk);

        return buttonPanel;
    }

    private void cancelTraceChanges() {
        int tempListSize = tempTraceList.getModel().getSize();

        traceModel.removeAllElements();
        for(int i = 0; i < tempListSize; i++) {
            traceModel.addElement(tempTraceList.getModel().getElementAt(i));
        }
        traceList.setModel(traceModel);

    }

    private void exitTraceDialog() {
        updateTraceBox();
        if (traceBox.getSelectedItem() != null) {
            traceBox.setSelectedIndex(traceBox.getItemCount() - 1);
        }
        updateTraceBoxQuantification = false;
        traceBoxQuantification.setSelectedIndex(traceBoxQuantification.getItemCount() - 1);
        updateTraceBoxQuantification = true;
        traceDialog.setVisible(false);
    }

    private void swapTraces(DefaultListModel model, int selectedIndex, int newIndex){
        var temp = model.get(newIndex);
        model.set(newIndex, model.get(selectedIndex));
        model.set(selectedIndex, temp);
    }

    private void removeTraces() {
        if (newProperty.toString().contains(traceList.getSelectedValue().toString())) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "The selected trace is used in the query.\n"
                    + "It is not possible to remove this trace.",
                "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            traceModel.remove(traceList.getSelectedIndex());
            traceList.setModel(traceModel);
        }
    }

    private void initPredicationConstructionPanel() {
        predicatePanel = new JPanel(new GridBagLayout());
        predicatePanel.setBorder(BorderFactory.createTitledBorder("Predicates"));

        initTracePanels();

        placeTransitionBox = new JComboBox();
        Dimension d = new Dimension(125, 27);
        placeTransitionBox.setMaximumSize(d);
        placeTransitionBox.setPreferredSize(d);

        Vector<Object> items = new Vector<>(tapnNetwork.activeTemplates().size()+1);
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
                        if (!lens.isTimed()) {
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
                    if (lens.isTimed()) {
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
                if (!lens.isTimed()) setEnablednessOfOperatorAndMarkingBoxes();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Tuple<?, String>> searchableItems = new ArrayList<>();
        for (TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
            for (TimedPlace place : tapn.places()) {
                if (!place.isShared()) {
                    searchableItems.add(new Tuple<>(place, tapn.toString()));
                }
            }

            if (!lens.isTimed()) {
                for (TimedTransition transition : tapn.transitions()) {
                    if (!transition.isShared()) {
                        searchableItems.add(new Tuple<>(transition, tapn.toString()));
                    }
                }
            }
        }

        for (TimedPlace place : tapnNetwork.sharedPlaces()) {
            searchableItems.add(new Tuple<>(place, SHARED.toString()));
        }

        for (SharedTransition transition : tapnNetwork.sharedTransitions()) {
            searchableItems.add(new Tuple<>(transition, SHARED.toString()));
        }

        searcher = new Searcher<>(searchableItems, obj -> {
            Object element = obj.value1();            
            String name = element.toString();
            if (name.contains(".")) {
                name = name.split("\\.")[1];
            }

            return name;
        });

        searchBar = new SearchBar();
        searchBar.useSharedPrefix(false);
        searchBar.setMaxVisibleItems(4);
        searchBar.setOnSearchTextChanged(query -> {
            if (query == null || query.trim().isEmpty()) {
                searchBar.hideResults();
                return;
            }

            var matches = searcher.findAllMatches(query);
            searchBar.showResults(matches);
        });

        searchBar.setOnResultSelected(result -> {
            if (result == null) return;

            searchBar.clear();

            boolean isShared = false;
            if (result.value1() instanceof TimedPlace) {
                isShared = ((TimedPlace)result.value1()).isShared();
            } else if (result.value1() instanceof TimedTransition) {
                isShared = ((TimedTransition)result.value1()).isShared();
            }

            if (isShared) {
                templateBox.setSelectedItem(SHARED);
                placeTransitionBox.setSelectedItem(result.value1().toString());
            } else {
                templateBox.setSelectedItem(tapnNetwork.getTAPNByName(result.value2()));
                placeTransitionBox.setSelectedItem(result.value1().toString().split("\\.")[1]);
            }
        });

        Dimension dim = new Dimension(235, 27);
        searchBar.setPreferredSize(dim);
        searchBar.setMaximumSize(dim);
        searchBar.setMinimumSize(dim);

        predicatePanel.add(searchBar, gbc);

        gbc.gridy += 2;
        gbc.gridwidth -= 2;

        JPanel templateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        predicatePanel.add(templateRow, gbc);
        templateBox.setPreferredSize(new Dimension(292, 27));
        templateBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        templateRow.add(templateBox);

        JPanel placeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        ++gbc.gridy;
        predicatePanel.add(placeRow, gbc);
        placeTransitionBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        placeRow.add(placeTransitionBox);

        String[] relationalSymbols = { "=", "!=", "<=", "<", ">=", ">" };
        relationalOperatorBox = new JComboBox(new DefaultComboBoxModel(relationalSymbols));
        relationalOperatorBox.setPreferredSize(new Dimension(80, 27));
        relationalOperatorBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        placeRow.add(relationalOperatorBox);

        placeMarking = new CustomJSpinner(0);
        placeMarking.setPreferredSize(new Dimension(80, 27));
        placeRow.add(placeMarking);

        transitionIsEnabledLabel = new JLabel(" is enabled");
        transitionIsEnabledLabel.setPreferredSize(new Dimension(165, 27));
        if (!lens.isTimed()) placeRow.add(transitionIsEnabledLabel);

        JPanel addPredicateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        ++gbc.gridy;
        addPredicateRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        predicatePanel.add(addPredicateRow, gbc);
        addPredicateButton = new JButton("Add predicate to the query");
        addPredicateButton.setPreferredSize(new Dimension(292, 27));
        addPredicateRow.add(addPredicateButton);

        truePredicateButton = new JButton("True");
        truePredicateButton.setPreferredSize(new Dimension(103, 27));

        falsePredicateButton = new JButton("False");
        falsePredicateButton.setPreferredSize(new Dimension(103, 27));

        deadLockPredicateButton = new JButton("Deadlock");
        deadLockPredicateButton.setPreferredSize(new Dimension(103, 27));

        JSeparator verticalSeparator = new JSeparator(JSeparator.VERTICAL);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(5, 5, 5, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        predicatePanel.add(verticalSeparator, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        predicatePanel.add(truePredicateButton, gbc);

        ++gbc.gridy;
        predicatePanel.add(falsePredicateButton, gbc);
        ++gbc.gridy;
        predicatePanel.add(deadLockPredicateButton, gbc);

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
        addPredicateButton.addActionListener(e -> {
            String template = templateBox.getSelectedItem().toString();
            if (template.equals(SHARED)) template = "";

            if ((!lens.isTimed()) && transitionIsSelected()) {
                if (queryType.getSelectedIndex() == 2) {
                    String trace = traceBox.getSelectedItem().toString();
                    addPropertyToQuery(new TCTLTransitionNode(template, (String) placeTransitionBox.getSelectedItem(), trace));
                } else {
                    addPropertyToQuery(new TCTLTransitionNode(template, (String) placeTransitionBox.getSelectedItem()));
                }
            } else {
                if (queryType.getSelectedIndex() == 2) {
                    TCTLAtomicPropositionNode property =
                        new TCTLAtomicPropositionNode (
                            new HyperLTLPathScopeNode (
                                new TCTLPlaceNode(template, (String) placeTransitionBox.getSelectedItem()),
                                traceBox.getSelectedItem().toString()
                            ),
                            (String) relationalOperatorBox.getSelectedItem(),
                            new TCTLConstNode((Integer) placeMarking.getValue())
                        );
                    addPropertyToQuery(property);
                } else {
                    TCTLAtomicPropositionNode property =
                        new TCTLAtomicPropositionNode (
                            new TCTLPlaceNode(template, (String) placeTransitionBox.getSelectedItem()),
                            (String) relationalOperatorBox.getSelectedItem(),
                            new TCTLConstNode((Integer) placeMarking.getValue())
                        );
                    addPropertyToQuery(property);
                }
            }
        });

        truePredicateButton.addActionListener(e -> {
            TCTLTrueNode trueNode = new TCTLTrueNode();
            addPropertyToQuery(trueNode);
        });

        falsePredicateButton.addActionListener(e -> {
            TCTLFalseNode falseNode = new TCTLFalseNode();
            addPropertyToQuery(falseNode);
        });

        deadLockPredicateButton.addActionListener(e -> {
            TCTLDeadlockNode deadLockNode = new TCTLDeadlockNode();
            addPropertyToQuery(deadLockNode);
        });

        placeTransitionBox.addActionListener(e -> {
            if (userChangedAtomicPropSelection) {
                updateQueryOnAtomicPropositionChange();
            }
            if (!lens.isTimed()) {
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

		resetButton.addActionListener(e -> {
            if (queryField.isEditable()) { // in edit mode, this button is now the parse query button.
                // User has potentially altered the query, so try to parse it
                TCTLAbstractProperty newQuery = null;

                try {
                    if (queryField.getText().trim().equals("<*>")) {
                        int choice = JOptionPane.showConfirmDialog(
                            TAPAALGUI.getApp(),
                            "It is not possible to parse an empty query.\nThe specified query has not been saved. Do you want to edit it again?",
                            "Error Parsing Query",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE);
                        if (choice == JOptionPane.NO_OPTION)
                            returnFromManualEdit(null);
                        else
                            return;
                    } else if(lens.isStochastic()) {
                        newQuery = TAPAALSMCQueryParser.parse(queryField.getText());
                    } else if (lens.isTimed()) {
                        newQuery = TAPAALQueryParser.parse(queryField.getText());
                    } else if (queryType.getSelectedIndex() == 0) {
                        newQuery = TAPAALCTLQueryParser.parse(queryField.getText());
                    } else if (queryType.getSelectedIndex() == 1) {
                        newQuery = TAPAALLTLQueryParser.parse(queryField.getText());
                    } else if (queryType.getSelectedIndex() == 2) {
                        newQuery = TAPAALHyperLTLQueryParser.parse(queryField.getText());
                    } else {
                        throw new Exception();
                    }
                } catch (Throwable ex) {
                    String message = ex.getMessage() == null ? "TAPAAL encountered an error while trying to parse the specified query\n" :
                        "TAPAAL encountered the following error while trying to parse the specified query:\n\n"+ex.getMessage();
                    int choice = JOptionPane.showConfirmDialog(
                            TAPAALGUI.getApp(),
                            message + "\nWe recommend using the query construction buttons unless you are an experienced user.\n\n The specified query has not been saved. Do you want to edit it again?",
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
                    // FOR HYPERLTL
                    // If there are new traces present in the manually parsed query, they need to be added to the tracebox
                    // We also have to check we don't do anything illegal, i.e., E T1 (E T1 (...))
                    if (queryType.getSelectedIndex() == 2)
                        checkTraceNamesForManuallyParsedQuery(newQuery);
                    else
                        checkPlacesAndTransitionsForManuallyParsedQuery(newQuery);
                } else {
                    returnFromManualEdit(null);
                }

                setVerificationOptionsEnabled(!rawVerificationOptionsEnabled.isSelected());
            } else { // we are not in edit mode so the button should reset
                // the query
                TCTLPathPlaceHolder ph = new TCTLPathPlaceHolder();
                UndoableEdit edit = new QueryConstructionEdit(newProperty, ph);
                newProperty = ph;
                resetQuantifierSelectionButtons();
                updateSelection(newProperty);
                undoSupport.postEdit(edit);
            }
        });

		undoButton.addActionListener(e -> {
            UndoableEdit edit = undoManager.GetNextEditToUndo();

            if (edit instanceof QueryConstructionEdit) {
                TCTLAbstractProperty original = ((QueryConstructionEdit) edit)
                        .getOriginal();
                undoManager.undo();
                refreshUndoRedo();
                updateSelection(original);
                queryChanged();
            }
        });

		redoButton.addActionListener(e -> {
            UndoableEdit edit = undoManager.GetNextEditToRedo();
            if (edit instanceof QueryConstructionEdit) {
                TCTLAbstractProperty replacement = ((QueryConstructionEdit) edit)
                        .getReplacement();
                undoManager.redo();
                refreshUndoRedo();
                updateSelection(replacement);
                queryChanged();
            }
        });

		editQueryButton.addActionListener(arg0 -> {
            if (queryField.isEditable()) { // we are in edit mode so the user pressed cancel
                returnFromManualEdit(null);
            } else { // user wants to edit query manually
                changeToEditMode();
            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        queryPanel.add(editingButtonPanel, gbc);
    }

    private void checkTraceNamesForManuallyParsedQuery(TCTLAbstractProperty newQuery) {
        HyperLTLTraceNameVisitor traceNameVisitor = new HyperLTLTraceNameVisitor();
        HyperLTLTraceNameVisitor.Context traceContext = traceNameVisitor.getTraceContext(newQuery);

        if (!traceContext.getResult()) {
            StringBuilder message = new StringBuilder("The parsed query does not conform with the syntax supported for Hyper-LTL in TAPAAL.\n\n");

            ArrayList<String> traceList = getUsedTraces(newQuery);
            for (String traceName : traceContext.getTraceNames()) {
                if (!traceList.contains(traceName)) {
                    message.append("The specified query contains a trace that is not in either E or A quantification nodes.\n")
                        .append("You may only use traces that are present in E or A, i.e.:\n")
                        .append("E T1 (T1...)) is legal, whereas \n")
                        .append("E T1 (T2...)) is illegal.\n\n");
                    break;
                }
            }

            if (message.toString().endsWith("TAPAAL.\n\n")) {
                message.append("The specified query has duplicate traces in either the E or A quantification nodes.\n")
                    .append("You may only use different traces for each E or A, i.e.:\n")
                    .append("E T1 (E T2 (...)) is legal, whereas \n")
                    .append("E T1 (E T1 (...)) is illegal.\n\n");
            }

            message.append("The specified query has not been saved. Do you want to edit it again?");
            int choice = JOptionPane.showConfirmDialog(
                TAPAALGUI.getApp(), message.toString(),
                "Error Parsing Query",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);
            if (choice == JOptionPane.NO_OPTION) {
                returnFromManualEdit(null);
            }
        } else {
            ArrayList<String> tracesFromParsedQuery = traceNameVisitor.getTraceContext(newQuery).getTraceNames();
            ArrayList<String> tracesFromTraceBox = new ArrayList<>();

            for (int i = 0; i < traceBox.getModel().getSize(); i++) {
                tracesFromTraceBox.add(traceBox.getItemAt(i).toString());
            }

            for(int i = 0; i < tracesFromParsedQuery.size(); i++) {
                if(!tracesFromTraceBox.contains(tracesFromParsedQuery.get(i))) {
                    addNewTrace(tracesFromParsedQuery.get(i), false);
                    updateTraceBox();
                }
            }
            checkPlacesAndTransitionsForManuallyParsedQuery(newQuery);
        }
    }

    private void checkPlacesAndTransitionsForManuallyParsedQuery(TCTLAbstractProperty newQuery) {
        VerifyPlaceNamesVisitor.Context placeContext = getPlaceContext(newQuery);
        VerifyTransitionNamesVisitor.Context transitionContext = getTransitionContext(newQuery);

        boolean isResultFalse = false;
        if (lens.isGame()) {
            isResultFalse = newQuery.hasNestedPathQuantifiers() || newQuery instanceof TCTLNotNode;
        }
        if (lens.isTimed()) {
            isResultFalse = isResultFalse || !placeContext.getResult();
        } else {
            isResultFalse = isResultFalse || !transitionContext.getResult() || !placeContext.getResult();
        }

        if (isResultFalse) {
            StringBuilder message = new StringBuilder();

            if (lens.isGame()) {
                message.append("The parsed query does not conform with the syntax supported for games in TAPAAL.\n");
            } else {
                message.append("The following places")
                    .append(lens.isTimed() ? "" : " or transitions")
                    .append(" were used in the query, but are not present in your model:\n\n");

                for (String placeName : placeContext.getIncorrectPlaceNames()) {
                    message.append(placeName).append('\n');
                }

                for (String transitionName : transitionContext.getIncorrectTransitionNames()) {
                    message.append(transitionName).append('\n');
                }
            }

            message.append("\nThe specified query has not been saved. Do you want to edit it again?");
            int choice = JOptionPane.showConfirmDialog(
                TAPAALGUI.getApp(), message.toString(),
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
    }

    private VerifyPlaceNamesVisitor.Context getPlaceContext(TCTLAbstractProperty newQuery) {
        // check correct place names are used in atomic propositions
        ArrayList<Tuple<String,String>> templatePlaceNames = new ArrayList<Tuple<String,String>>();
        for(TimedArcPetriNet tapn : tapnNetwork.activeTemplates()) {
            for(TimedPlace p : tapn.places()) {
                if (lens.isTimed() || !p.isShared()) {
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
                if (lens.isTimed() || !t.isShared()) {
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

    private void initVerificationPanel() {
        verificationPanel = new JPanel(new GridBagLayout());

        initReductionOptionsPanel();
        if(lens.isColored() && !lens.isTimed()){
            initUnfoldingOptionsPanel();
        }
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5,10,5,10);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(verificationPanel, gridBagConstraints);
    }

    private void initSearchOptionsPanel() {
        searchOptionsPanel = new JPanel(new GridBagLayout());
        searchOptionsPanel.setVisible(false);

		searchOptionsPanel.setBorder(BorderFactory.createTitledBorder("Search Strategy Options"));
		searchRadioButtonGroup = new ButtonGroup();
		breadthFirstSearch = new JRadioButton("Breadth first    ");
		depthFirstSearch = new JRadioButton("Depth first    ");
		randomSearch = new JRadioButton("Random    ");
        heuristicSearch = new JRadioButton("Heuristic    ");

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
        gridBagConstraints.weightx = 1;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        uppaalOptionsPanel.add(searchOptionsPanel, gridBagConstraints);
    }

    private void initUnfoldingOptionsPanel() {
        unfoldingOptionsPanel = new JPanel(new GridBagLayout());
        unfoldingOptionsPanel.setVisible(false);

        unfoldingOptionsPanel.setBorder(BorderFactory.createTitledBorder("Unfolding Options"));
        usePartitioning = new JCheckBox("Use partitioning of the colored net");
        useColorFixpoint = new JCheckBox("Use color fixpoint analysis");
        useSymmetricvars = new JCheckBox("Use reduction of symmetric variables");

        usePartitioning.setToolTipText(TOOL_TIP_PARTITIONING);
        useColorFixpoint.setToolTipText(TOOL_TIP_COLOR_FIXPOINT);
        useSymmetricvars.setToolTipText(TOOL_TIP_SYMMETRIC_VARIABLES);

        usePartitioning.setSelected(true);
        useColorFixpoint.setSelected(true);
        useSymmetricvars.setSelected(true);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        unfoldingOptionsPanel.add(usePartitioning, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        unfoldingOptionsPanel.add(useColorFixpoint, gridBagConstraints);
        gridBagConstraints.gridy = 2;
        unfoldingOptionsPanel.add(useSymmetricvars, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);

        verificationPanel.add(unfoldingOptionsPanel, gridBagConstraints);

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

        if (lens.isTimed()) {
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
        gridBagConstraints.fill = GridBagConstraints.BOTH;

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
        noApproximationEnable.addActionListener(e -> updateRawVerificationOptions());

        overApproximationEnable = new JRadioButton("Over-approximation");
        overApproximationEnable.setVisible(true);
        overApproximationEnable.setToolTipText(TOOL_TIP_APPROXIMATION_METHOD_OVER);
        overApproximationEnable.addActionListener(e -> updateRawVerificationOptions());

        underApproximationEnable = new JRadioButton("Under-approximation");
        underApproximationEnable.setVisible(true);
        underApproximationEnable.setToolTipText(TOOL_TIP_APPROXIMATION_METHOD_UNDER);
        underApproximationEnable.addActionListener(e -> updateRawVerificationOptions());

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

        reductionOption = new JComboBox<String>();
        reductionOption.setToolTipText(TOOL_TIP_REDUCTION_OPTION);

        reductionOption.addActionListener(e -> setEnabledOptionsAccordingToCurrentReduction());

        reductionOption.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    showRawVerificationOptions(advancedView);
                    guiDialog.pack();
                }
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

        useReduction = new JCheckBox("Use net reductions");
        useColoredReduction = new JCheckBox("Use colored net reductions");
        useSiphonTrap = new JCheckBox("Use siphon-trap analysis");
        useQueryReduction = new JCheckBox("Use query reduction");
        useStubbornReduction = new JCheckBox("Use stubborn reduction");
        symmetryReduction = new JCheckBox("Use symmetry reduction");
        discreteInclusion = new JCheckBox("Use discrete inclusion");
        selectInclusionPlacesButton = new JButton("Select Inclusion Places");
        useTimeDarts = new JCheckBox("Use Time Darts");
        useGCD = new JCheckBox("Use GCD");
        usePTrie = new JCheckBox("Use PTrie");
        skeletonAnalysis = new JCheckBox("Preprocess using skeleton analysis");
        useTraceRefinement = new JCheckBox("Use trace abstraction refinement");
        useTarjan = new JCheckBox("Use Tarjan");

        useReduction.setSelected(true);
        useColoredReduction.setSelected(true);
        useSiphonTrap.setSelected(false);
        useQueryReduction.setSelected(true);
        useStubbornReduction.setSelected(true);
        symmetryReduction.setSelected(true);
        discreteInclusion.setVisible(true);
        selectInclusionPlacesButton.setEnabled(false);
        useTimeDarts.setSelected(false);
        useGCD.setSelected(true);
        usePTrie.setSelected(true);
        skeletonAnalysis.setSelected(true);
        useTraceRefinement.setSelected(false);
        useTarjan.setSelected(true);

        useReduction.setToolTipText(TOOL_TIP_USE_STRUCTURALREDUCTION);
        useColoredReduction.setToolTipText(TOOL_TIP_USE_COLORED_STRUCTURALREDUCTION);
        useSiphonTrap.setToolTipText(TOOL_TIP_USE_SIPHONTRAP);
        useQueryReduction.setToolTipText(TOOL_TIP_USE_QUERY_REDUCTION);
        useStubbornReduction.setToolTipText(TOOL_TIP_STUBBORN_REDUCTION);
        symmetryReduction.setToolTipText(TOOL_TIP_SYMMETRY_REDUCTION);
        discreteInclusion.setToolTipText(TOOL_TIP_DISCRETE_INCLUSION);
        selectInclusionPlacesButton.setToolTipText(TOOL_TIP_SELECT_INCLUSION_PLACES);
        useTimeDarts.setToolTipText(TOOL_TIP_TIME_DARTS);
        useGCD.setToolTipText(TOOL_TIP_GCD);
        usePTrie.setToolTipText(TOOL_TIP_PTRIE);
        skeletonAnalysis.setToolTipText(TOOL_TIP_OVERAPPROX);
        useTraceRefinement.setToolTipText(TOOL_TIP_USE_TRACE_REFINEMENT);
        useTarjan.setToolTipText(TOOL_TIP_USE_TARJAN);

        useTarjan.addActionListener(e -> updateSearchStrategies());

        if (lens.isTimed()) {
            initTimedReductionOptions();
        } else {
            useReduction.addActionListener(actionEvent ->
                openReducedNetButton.setEnabled(useReduction.isSelected() && getQueryComment().length() > 0 && !newProperty.containsPlaceHolder()));
            initUntimedReductionOptions();
        }

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;

        verificationPanel.add(reductionOptionsPanel, gbc);
    }

    private void initTimedReductionOptions() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0,5,0,5);
        reductionOptionsPanel.add(symmetryReduction, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        reductionOptionsPanel.add(discreteInclusion, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        reductionOptionsPanel.add(useStubbornReduction, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        reductionOptionsPanel.add(skeletonAnalysis, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        reductionOptionsPanel.add(useTimeDarts, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        reductionOptionsPanel.add(useGCD, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        reductionOptionsPanel.add(selectInclusionPlacesButton, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        reductionOptionsPanel.add(usePTrie, gbc);

        discreteInclusion.addActionListener(e -> selectInclusionPlacesButton.setEnabled(discreteInclusion.isSelected()));
        selectInclusionPlacesButton.addActionListener(e -> inclusionPlaces = ChooseInclusionPlacesDialog.showInclusionPlacesDialog(tapnNetwork, inclusionPlaces));

        useTimeDarts.addActionListener(e -> setEnabledOptionsAccordingToCurrentReduction());
    }

    private void initUntimedReductionOptions() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0,5,0,5);
        reductionOptionsPanel.add(useReduction, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;

        if (lens.isColored()) {
            reductionOptionsPanel.add(useColoredReduction, gbc);
            gbc.gridx = 0;
            gbc.gridy = 3;
        }

        reductionOptionsPanel.add(useQueryReduction, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        reductionOptionsPanel.add(useStubbornReduction, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        reductionOptionsPanel.add(useTraceRefinement, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        reductionOptionsPanel.add(useSiphonTrap, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        reductionOptionsPanel.add(useTarjan, gbc);
    }

    private void initRawVerificationOptionsPanel() {
        rawVerificationOptionsPanel = new JPanel(new GridBagLayout());
        rawVerificationOptionsPanel.setVisible(false);
        rawVerificationOptionsPanel.setBorder(BorderFactory.createTitledBorder("Verification Options"));

        rawVerificationOptionsEnabled = new JCheckBox("Use");
        rawVerificationOptionsEnabled.setToolTipText(TOOL_TIP_RAW_VERIFICATION_ENABLED_CHECKBOX);

        rawVerificationOptionsTextArea = new JTextArea();
        
        rawVerificationOptionsTextArea.setEnabled(false);
        rawVerificationOptionsTextArea.setToolTipText(TOOL_TIP_RAW_VERIFICATION_TEXT_FIELD);
        rawVerificationOptionsTextArea.setLineWrap(true);
        rawVerificationOptionsTextArea.setWrapStyleWord(true);
        rawVerificationOptionsTextArea.setRows(4);

        JScrollPane rawVerificationOptionsScrollPane = new JScrollPane(rawVerificationOptionsTextArea);

        rawVerificationOptionsHelpButton = new JButton("Help on options");
        rawVerificationOptionsHelpButton.setEnabled(false);
        rawVerificationOptionsHelpButton.setToolTipText(TOOL_TIP_RAW_VERIFICATION_HELP_BUTTON);
        rawVerificationOptionsHelpButton.addActionListener(e -> showEngineHelp());
        rawVerificationOptionsEnabled.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setVerificationOptionsEnabled(!rawVerificationOptionsEnabled.isSelected());
                rawVerificationOptionsTextArea.setEnabled(rawVerificationOptionsEnabled.isSelected());
                rawVerificationOptionsHelpButton.setEnabled(rawVerificationOptionsEnabled.isSelected());
                updateRawVerificationOptions();
            }
        });

        GridBagConstraints checkBoxGbc = new GridBagConstraints();
        checkBoxGbc.gridx = 0;
        checkBoxGbc.gridy = 0;

        GridBagConstraints textAreaGbc = new GridBagConstraints();
        textAreaGbc.gridx = 1;
        textAreaGbc.gridy = 0;
        textAreaGbc.weightx = 1;
        textAreaGbc.gridheight = 2;
        textAreaGbc.fill = GridBagConstraints.HORIZONTAL;
        textAreaGbc.insets = new Insets(0, 10, 0, 10);

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 1;
        buttonGbc.insets = new Insets(0, 5, 0, 0);

        rawVerificationOptionsPanel.add(rawVerificationOptionsEnabled, checkBoxGbc);
        rawVerificationOptionsPanel.add(rawVerificationOptionsScrollPane, textAreaGbc);
        rawVerificationOptionsPanel.add(rawVerificationOptionsHelpButton, buttonGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        add(rawVerificationOptionsPanel, gbc);
    }

    private void showEngineHelp() {
        querySaved = true;
        ModelChecker model = Verifier.getModelChecker(getQuery());
        querySaved = false;
        
        JTextArea engineHelp = new JTextArea();
        engineHelp.setEditable(false);
        
        if (model instanceof VerifyPN) {
            engineHelp.setText(((VerifyPN) model).getHelpOptions());
        } else if (model instanceof VerifyDTAPN) {
            engineHelp.setText(((VerifyDTAPN) model).getHelpOptions());
        } else if (model instanceof VerifyTAPN) {
            engineHelp.setText(((VerifyTAPN) model).getHelpOptions());
        }

        JScrollPane engineHelpScrollPane = new JScrollPane(engineHelp);
        engineHelpScrollPane.setPreferredSize(new Dimension(800, 600));

        if (!engineHelp.getText().isEmpty()) {
            Window ownerWindow = SwingUtilities.windowForComponent(QueryDialog.this);
            JDialog engineHelpDialog = new JDialog(ownerWindow, "Engine Options", ModalityType.MODELESS);
            engineHelpDialog.add(engineHelpScrollPane);
            engineHelpDialog.pack();
            engineHelpDialog.setResizable(true);
            engineHelpDialog.setLocationByPlatform(true);
            engineHelpDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(QueryDialog.this, "Error getting options for this engine.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setVerificationOptionsEnabled(boolean isEnabled) {
        Enabler.setAllEnabled(reductionOptionsPanel, isEnabled);

        if (unfoldingOptionsPanel != null) {
            Enabler.setAllEnabled(unfoldingOptionsPanel, isEnabled);
        }

        Enabler.setAllEnabled(traceOptionsPanel, isEnabled);
        Enabler.setAllEnabled(boundednessCheckPanel, isEnabled);
        Enabler.setAllEnabled(searchOptionsPanel, isEnabled);
        Enabler.setAllEnabled(smcTracePanel, isEnabled);

        smcVerificationTypeLabel.setEnabled(isEnabled);
        smcVerificationType.setEnabled(isEnabled);
        smcParallelLabel.setEnabled(isEnabled);
        smcParallel.setEnabled(isEnabled);

        smcGranularityField.setEnabled(isEnabled);
        smcMaxGranularityCheckbox.setEnabled(isEnabled);
        smcNumericPrecision.setEnabled(isEnabled);

        setEnabledOptionsAccordingToCurrentReduction();
    }

    protected void setEnabledOptionsAccordingToCurrentReduction() {
        refreshQueryEditingButtons();

        if (rawVerificationOptionsEnabled.isSelected()) {
            return;
        }

        refreshTraceOptions();

        if (lens.isTimed()) {
            refreshSymmetryReduction();
            refreshStubbornReduction();
            refreshDiscreteOptions();
            refreshDiscreteInclusion();
            refreshOverApproximationOption();
        } else if (!lens.isTimed()) {
            refreshTraceRefinement();
            refreshTarjan();
            refreshColoredReduction();
            if (queryType.getSelectedIndex() == 2) {
                traceBoxQuantification.setEnabled(traceBoxQuantification.getModel().getSize() > 0);
            }
        }

        if (!lens.isColored()) {
            useColoredReduction.setSelected(false);
            useColoredReduction.setEnabled(false);
        }
        
        boolean isCTL = queryType.getSelectedIndex() == 0;
        updateSiphonTrap(isCTL);
    
        updateSearchStrategies();
		refreshExportButtonText();

        guiDialog.pack();
	}

    private void showRawVerificationOptions(boolean advancedView) {
        querySaved = true;
        TAPNQuery query = getQuery();
        querySaved = false;

        if (query.getReductionOption() != ReductionOption.VerifyTAPN && 
            query.getReductionOption() != ReductionOption.VerifyDTAPN && 
            query.getReductionOption() != ReductionOption.VerifyPN) {

            rawVerificationOptionsPanel.setVisible(false);
        } else {
            rawVerificationOptionsPanel.setVisible(advancedView);
        }

        guiDialog.pack();
    }

    private void updateRawVerificationOptions() {
        querySaved = true;
        TAPNQuery query = getQuery();
        querySaved = false;

        query = Verifier.convertQuery(query, lens);

        Verifier.createTempFile();

        boolean isColored = (lens != null && lens.isColored() || tapnNetwork.isColored());
        VerifyTAPNOptions verifytapnOptions = Verifier.getVerificationOptions(query, isColored);

        ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(QueryDialog.this.tapnNetwork);
        verifytapnOptions.setTokensInModel(transformedModel.value1().getNumberOfTokensInNet());

        String rawVerificationOptions = verifytapnOptions.toString();

        if (verifytapnOptions.enabledOverApproximation() || verifytapnOptions.enabledUnderApproximation()) {
            if (verifytapnOptions.kBoundPresentInRawVerification() && verifytapnOptions.tracePresentInRawVerification()) {
                JOptionPane.showMessageDialog(QueryDialog.this, "Because over/under-approximation is active, the specified k-bound and trace in the custom verification options will be overwritten.", "Warning", JOptionPane.WARNING_MESSAGE);
            } else if (verifytapnOptions.kBoundPresentInRawVerification()) {
                JOptionPane.showMessageDialog(QueryDialog.this,
                "Because over/under-approximation is active, the specified k-bound in the custom verification options will be overwritten.", "Warning",
                            JOptionPane.WARNING_MESSAGE);
            } else if (verifytapnOptions.tracePresentInRawVerification()) {
                JOptionPane.showMessageDialog(QueryDialog.this,
                "Because over/under-approximation is active, the specified trace in the custom verification options will be overwritten.", "Warning",
                            JOptionPane.WARNING_MESSAGE);
            }

            rawVerificationOptions = rawVerificationOptions.replaceAll("(--k-bound|-k|--trace|-t) +\\d*", ""); 
        }
        rawVerificationOptionsTextArea.setText(rawVerificationOptions.trim());
    }

	private void refreshTraceRefinement() {
	    ReductionOption reduction = getReductionOption();

        if (queryType.getSelectedIndex() == 0 && !lens.isGame() &&
            reduction != null && reduction.equals(ReductionOption.VerifyPN) &&
            (newProperty.toString().startsWith("AG") || newProperty.toString().startsWith("EF")) &&
            !hasInhibitorArcs && !newProperty.hasNestedPathQuantifiers()) {
	        useTraceRefinement.setEnabled(true);
        } else {
            useTraceRefinement.setEnabled(false);
        }
    }

    private void refreshTarjan() {
        int selectedIndex = queryType.getSelectedIndex();
        switch (selectedIndex) {
            case 1:
                useTarjan.setVisible(true);
                useTarjan.setEnabled(true);
                break;
            case 2:
                useTarjan.setVisible(true);
                useTarjan.setEnabled(false);
                useTarjan.setSelected(false);
                break;
            default:
                useTarjan.setVisible(false);
                useTarjan.setEnabled(false);
                break;
        }
    }

    private void refreshColoredReduction() {
	    useColoredReduction.setEnabled(someTraceRadioButton.isSelected());
	    if (someTraceRadioButton.isSelected() || lens.isGame()) {
	        useColoredReduction.setEnabled(false);
	        useColoredReduction.setSelected(false);
        } else {
	        useColoredReduction.setEnabled(true);
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
            saveUppaalXMLButton.setText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyDTAPN ? EXPORT_VERIFYTAPN_BTN_TEXT : reduction == ReductionOption.VerifyPN ? EXPORT_VERIFYPN_BTN_TEXT : EXPORT_UPPAAL_BTN_TEXT);
            saveUppaalXMLButton.setToolTipText(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyDTAPN ? TOOL_TIP_SAVE_TAPAAL_BUTTON : reduction == ReductionOption.VerifyPN ? TOOL_TIP_SAVE_PN_BUTTON : TOOL_TIP_SAVE_UPPAAL_BUTTON);
            saveUppaalXMLButton.setEnabled(true);
        }
    }

	private void refreshQueryEditingButtons() {
        boolean isSmc = lens.isStochastic();
		if (currentSelection != null) {
            if (lens.isGame()) {
                if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
                    forAllBox.setSelected(false);
                    enableOnlyForAll();
                } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    enableOnlyStateButtons();
                }
            } else if (lens.isTimed() && !isSmc) {
                if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
                    enableOnlyPathButtons();
                } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    enableOnlyStateButtons();
                }
                updateQueryButtonsAccordingToSelection();
            } else if(isSmc) {
                if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
                    enableOnlySMCButtons();
                } else if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
                    enableOnlyStateButtons();
                    finallyButton.setEnabled(false);
                    globallyButton.setEnabled(false);
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
        if (rawVerificationOptionsEnabled.isSelected()) {
            return;
        }

        if (queryHasDeadlock() || newProperty.toString().contains("EG") || newProperty.toString().contains("AF")){
            skeletonAnalysis.setSelected(false);
            skeletonAnalysis.setEnabled(false);
        } else {
            if(!skeletonAnalysis.isEnabled()){
                skeletonAnalysis.setSelected(true);
            }
            skeletonAnalysis.setEnabled(true);
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
        }
    }

    private void refreshStubbornReduction(){
        if(queryType.getSelectedIndex() == 2) {
            useStubbornReduction.setSelected(false);
            useStubbornReduction.setEnabled(false);
        } else if(useTimeDarts.isSelected()) {
            useStubbornReduction.setSelected(false);
            useStubbornReduction.setEnabled(false);
        } else {
            useStubbornReduction.setEnabled(true);
        }
    }

    private void updateHyperLTLButtons() {
        boolean enable = traceBoxQuantification.getModel().getSize() > 0;
        showHyperLTL(true);

        if (currentSelection == null || currentSelection.getObject() == newProperty) {
            String ltlType = checkLTLType();
            disableAllLTLButtons();
            if (ltlType.equals("placeholder")) {
                aButton.setEnabled(enable);
                eButton.setEnabled(enable);
            } else if (ltlType.equals("A")) {
                aButton.setEnabled(enable);
            } else {
                eButton.setEnabled(enable);
            }
        } else {
            if (currentSelection.getObject() instanceof LTLANode) {
                aButton.setEnabled(enable);
            } else if (currentSelection.getObject() instanceof LTLENode) {
                eButton.setEnabled(enable);
            } else if (containsOnlyPathProperties(newProperty)) {
                aButton.setEnabled(enable && newProperty instanceof LTLANode);
                eButton.setEnabled(enable && newProperty instanceof LTLENode);
                globallyButton.setEnabled(true);
                finallyButton.setEnabled(true);
                nextButton.setEnabled(true);
                untilButton.setEnabled(true);
            } else {
                aButton.setEnabled(false);
                eButton.setEnabled(false);
                traceBoxQuantification.setEnabled(false);
                globallyButton.setEnabled(true);
                finallyButton.setEnabled(true);
                nextButton.setEnabled(true);
                untilButton.setEnabled(true);
            }
        }

        if (currentSelection == null || (currentSelection != null && (currentSelection.getObject() instanceof LTLENode || currentSelection.getObject() instanceof LTLANode || currentSelection.getObject() instanceof TCTLPathPlaceHolder))) {
            conjunctionButton.setEnabled(false);
            disjunctionButton.setEnabled(false);
        } else {
            conjunctionButton.setEnabled(true);
            disjunctionButton.setEnabled(true);
        }
    }

    private boolean containsOnlyPathProperties(TCTLAbstractProperty property) {
        if (property instanceof LTLANode) {
            return containsOnlyPathProperties(((LTLANode) property).getProperty());
        } else if (property instanceof LTLENode) {
            return containsOnlyPathProperties(((LTLENode) property).getProperty());
        } else if (property instanceof TCTLPathToStateConverter) {
            return containsOnlyPathProperties(((TCTLPathToStateConverter) property).getProperty());
        }
        return property instanceof TCTLPathPlaceHolder || property instanceof TCTLStatePlaceHolder;
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

    private void updateSMCButtons() {
        if (currentSelection != null && currentSelection.getObject() == newProperty) {
            String ltlType = checkLTLType();
            if (ltlType.equals("placeholder")) {
                finallyButton.setEnabled(true);
                globallyButton.setEnabled(true);
            }
        } else {
            finallyButton.setEnabled(false);
            globallyButton.setEnabled(false);
        }
    }


    private void queryChanged(){
        setEnabledReductionOptions();
        if (lens.isTimed()) refreshOverApproximationOption();
        int selectedIndex = queryType.getSelectedIndex();
        boolean isSmc = lens.isStochastic();
        if (selectedIndex == 1) {
            updateLTLButtons();
        } else if (selectedIndex == 2) {
            updateHyperLTLButtons();
            updateTraceBox();
        } else if (isSmc) {
            updateSMCButtons();
        }
    }

    private void cancelAndExit() {
        cancelTraceChanges();
    
        // Ensure all query edits are undone
        while (undoManager.canUndo()) {
            UndoableEdit edit = undoManager.GetNextEditToUndo();
            if (edit instanceof QueryConstructionEdit) {
                TCTLAbstractProperty original = ((QueryConstructionEdit) edit)
                        .getOriginal();
                undoManager.undo();
                refreshUndoRedo();
                updateSelection(original);
                queryChanged();
            } else {
                undoManager.undo();
            }
        }

        exit();
    }

    private void initButtonPanel(QueryDialogueOption option, boolean isNewQuery) {
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
                        tab.setNetChanged(true);
                        exit();
                        TAPNQuery query = getQuery();
                        if (isNewQuery) {
                            var queryPane = tab.getQueryPane();
                            queryPane.getUndoManager().addNewEdit(new AddQueryCommand(query, tab));
                            queryPane.addQuery(query);
                        }
                    }
                }
            });
            saveAndVerifyButton.addActionListener(evt -> {
                updateRawVerificationOptions();
                if (checkIfSomeReductionOption()) {
                    if (lens.isStochastic() && !tapnNetwork.isNonStrict()) {
                        JOptionPane.showMessageDialog(QueryDialog.this, "The model has strict intervals and can therefore not be verified", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    querySaved = true;
                    // Now if a query is saved and verified, the net is marked as modified
                    tab.setNetChanged(true);
                    exit();
                    TAPNQuery query = getQuery();
                    if (isNewQuery) {
                        var queryPane = tab.getQueryPane();
                        queryPane.getUndoManager().addNewEdit(new AddQueryCommand(query, tab));
                        queryPane.addQuery(query);
                    }
                    
                    if (query.getReductionOption() == ReductionOption.VerifyTAPN || query.getReductionOption() == ReductionOption.VerifyDTAPN || query.getReductionOption() == ReductionOption.VerifyPN) {
                        Verifier.runVerifyTAPNVerification(tapnNetwork, query, null, guiModels,false, lens);
                    } else {
                        Verifier.runUppaalVerification(tapnNetwork, query);
                    }
                }});
            cancelButton.addActionListener(evt -> cancelAndExit());

            saveUppaalXMLButton.addActionListener(e -> {
                querySaved = true;

                String xmlFile = null, queryFile = null;
                ReductionOption reduction = getReductionOption();
                try {
                    FileBrowser browser = FileBrowser.constructor(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyDTAPN || reduction == ReductionOption.VerifyPN ? "Verifytapn XML" : "Uppaal XML",	"xml", xmlFile);
                    xmlFile = browser.saveFile();
                    if (xmlFile != null) {
                        String[] a = xmlFile.split(".xml");
                        queryFile = a[0] + ".q";
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                        "There were errors performing the requested action:\n"
                            + e, "Error",
                        JOptionPane.ERROR_MESSAGE);
                }

                if (xmlFile != null && queryFile != null) {
                    ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
                    Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(QueryDialog.this.tapnNetwork);

                    if (overApproximationEnable.isSelected()) {
                        OverApproximation overaprx = new OverApproximation();
                        overaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator());
                    }
                    else if (underApproximationEnable.isSelected()) {
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
                    if (lens.isColored() && lens.isTimed()) {
                        exportTACPN(transformedModel, xmlFile);
                    } else if (lens.isColored()) {
                        VerifyCPNExporter exporter = new VerifyCPNExporter();
                        exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile), tapnQuery, lens, transformedModel.value2(), composer.getGuiModel());

                    } else if(reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyDTAPN) {
                        VerifyTAPNExporter exporter = new VerifyTAPNExporter();
                        exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile), tapnQuery, lens, transformedModel.value2(), composer.getGuiModel());

                    } else if(reduction == ReductionOption.VerifyPN){
                        VerifyPNExporter exporter = new VerifyPNExporter();
                        exporter.export(transformedModel.value1(), clonedQuery, new File(xmlFile), new File(queryFile), tapnQuery, lens, transformedModel.value2(), composer.getGuiModel());

                    } else {
                        UppaalExporter exporter = new UppaalExporter();
                        try {
                            exporter.export(transformedModel.value1(), clonedQuery, tapnQuery.getReductionOption(), new File(xmlFile), new File(queryFile), tapnQuery.useSymmetry());
                        } catch (Exception exportException) {
                            StringBuilder s = new StringBuilder();
                            if (exportException instanceof UnsupportedModelException)
                                s.append(UNSUPPORTED_MODEL_TEXT + "\n\n");
                            else if (exportException instanceof UnsupportedQueryException)
                                s.append(UNSUPPORTED_QUERY_TEXT + "\n\n");
                            if (reduction == ReductionOption.VerifyTAPN || reduction == ReductionOption.VerifyDTAPN || reduction == ReductionOption.VerifyPN)
                                s.append(NO_VERIFYTAPN_XML_FILE_SAVED);
                            else
                                s.append(NO_UPPAAL_XML_FILE_SAVED);

                            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), s.toString());
                        }
                    }
                }
            });

            mergeNetComponentsButton.addActionListener(e -> {
                TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, lens, true, true);
                Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(tapnNetwork);

                ArrayList<Template> templates = new ArrayList<Template>(1);
                querySaved = true;	//Setting this to true will make sure that new values will be used.
                if (overApproximationEnable.isSelected()) {
                    OverApproximation overaprx = new OverApproximation();
                    overaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator());
                }
                else if (underApproximationEnable.isSelected()) {
                    UnderApproximation underaprx = new UnderApproximation();
                    underaprx.modifyTAPN(transformedModel.value1(), getQuery().approximationDenominator(), composer.getGuiModel());
                }
                templates.add(new Template(transformedModel.value1(), composer.getGuiModel(), new Zoomer()));

                TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();

                network.add(transformedModel.value1());
                network.setColorTypes(tapnNetwork.colorTypes());
                network.setConstants(tapnNetwork.constants());
                network.setVariables(tapnNetwork.variables());

                NetWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<TAPNQuery>(0), new ArrayList<Constant>(0), lens);

                try {
                    ByteArrayOutputStream outputStream = tapnWriter.savePNML();
                    String composedName = "composed-" + tab.getTabTitle();
                    composedName = composedName.replace(".tapn", "");
                    TAPAALGUI.openNewTabFromStream(new ByteArrayInputStream(outputStream.toByteArray()), composedName);
                    exit();
                } catch (Exception e1) {
                    System.console().printf(e1.getMessage());
                }
            });
            openReducedNetButton.addActionListener(e -> {
                if (checkIfSomeReductionOption()) {
                    querySaved = true;
                    // Now if a query is saved and verified, the net is marked as modified
                    tab.setNetChanged(true);

                    TAPNQuery query = getQuery();
                    if (query.getReductionOption() != ReductionOption.VerifyPN) {
                        JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                            "The selected verification engine does not support application of reduction rules",
                            "Reduction rules unsupported", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    exit();
                   
                    Verifier.runVerifyTAPNVerification(tapnNetwork, query,null, guiModels, true, null);

                    File reducedNetFile = new File(Verifier.getReducedNetFilePath());

                    if (reducedNetFile.exists() && reducedNetFile.isFile() && reducedNetFile.canRead()) {
                        try {
                            PetriNetTab reducedNetTab = PetriNetTab.createNewTabFromPNMLFile(reducedNetFile);
                            //Ensure that a net was created by the query reduction
                            if (reducedNetTab.currentTemplate().guiModel().getPlaces().length > 0
                                    || reducedNetTab.currentTemplate().guiModel().getTransitions().length > 0) {
                                reducedNetTab.setInitialName("reduced-" + tab.getTabTitle());
                                TAPNQuery convertedQuery = query.convertPropertyForReducedNet(reducedNetTab.currentTemplate().toString());
                                reducedNetTab.addQuery(convertedQuery);
                                TAPAALGUI.openNewTabFromStream(reducedNetTab);
                            }
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                                e1.getMessage(),
                                "Error loading reduced net file",
                                JOptionPane.ERROR_MESSAGE);
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
            cancelButton.addActionListener(evt -> cancelAndExit());
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
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 10);
        add(buttonPanel, gridBagConstraints);

    }

    private void exportTACPN(Tuple<TimedArcPetriNet, NameMapping> transformedModel, String xmlFile) {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        ArrayList<Template> templates = new ArrayList<>(1);
        ArrayList<TAPNQuery> queries = new ArrayList<>(1);

        network.add(transformedModel.value1());
        for (ColorType ct : QueryDialog.this.tapnNetwork.colorTypes()) {
            network.add(ct);
        }
        for (Variable variable: QueryDialog.this.tapnNetwork.variables()) {
            network.add(variable);
        }
        for (int i = 0; i < templateBox.getItemCount(); i++) {
            TimedArcPetriNet tapn = (TimedArcPetriNet) templateBox.getItemAt(i);
            templates.add(new Template(tapn, guiModels.get(tapn), new Zoomer()));
        }
        // guimodel not working
        //templates.add(new Template(transformedModel.value1(), null, new Zoomer()));
        TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(QueryDialog.this.tapnNetwork, templates, queries, QueryDialog.this.tapnNetwork.constants(), lens);
        try {
            writerTACPN.savePNML(new File(xmlFile));
        } catch (IOException | ParserConfigurationException | TransformerException exception) {
            exception.printStackTrace();
        }
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
        private final TCTLAbstractProperty original;
        private final TCTLAbstractProperty replacement;

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
            public void actionPerformed(ActionEvent e) { undoButton.doClick(); }
        });
        am.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { redoButton.doClick(); }
        });
        InputMap im = this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke('Z', shortcutkey), "undo");
        im.put(KeyStroke.getKeyStroke('Y', shortcutkey), "redo");
    }

    private void setupEstimationListeners() {
        DocumentListener needUpdateTime = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                update();
            }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                update();
            }
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                update();
            }
            public void update() {
                if(!smcEstimationIntervalWidth.hasFocus() && !smcConfidence.hasFocus()) return;
                smcMustUpdateTime = true;
                smcTimeExpected.setText("");
                smcTimeEstimationButton.setText(UPDATE_VERIFICATION_TIME_BTN_TEXT);
                try {
                    Float.parseFloat(smcEstimationIntervalWidth.getText());
                    boolean isQueryOk = getQueryComment().length() > 0 && !newProperty.containsPlaceHolder();
                    smcTimeEstimationButton.setEnabled(!queryField.isEditable() && isQueryOk);
                } catch(NumberFormatException e) {
                    smcTimeEstimationButton.setEnabled(false);
                }
            }
        };
        smcEstimationIntervalWidth.getDocument().addDocumentListener(needUpdateTime);
        smcConfidence.getDocument().addDocumentListener(needUpdateTime);
        smcTimeExpected.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                update();
            }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                update();
            }
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                update();
            }
            public void update() {
                if(!smcTimeExpected.hasFocus()) return;
                smcMustUpdateTime = false;
                smcEstimationIntervalWidth.setText("");
                smcTimeEstimationButton.setText(UPDATE_PRECISION_BTN_TEXT);
                try {
                    Double.parseDouble(smcTimeExpected.getText());
                    boolean isQueryOk = getQueryComment().length() > 0 && !newProperty.containsPlaceHolder();
                    smcTimeEstimationButton.setEnabled(!queryField.isEditable() && isQueryOk);
                } catch(NumberFormatException e) {
                    smcTimeEstimationButton.setEnabled(false);
                }
            }
        });
    }

    private void runBenchmark() {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat precisionFormat = new DecimalFormat("#.#####", decimalFormatSymbols);
        DecimalFormat timeFormat = new DecimalFormat("#.##", decimalFormatSymbols);
        if(doingBenchmark) {
            if(benchmarkThread == null) return;
            benchmarkThread.cancel(true);
            doingBenchmark = false;
            updateFieldsOnBenchmark();
            smcTimeEstimationButton.setText(smcMustUpdateTime ? UPDATE_VERIFICATION_TIME_BTN_TEXT : UPDATE_PRECISION_BTN_TEXT);
            return;
        }
        doingBenchmark = true;
        updateFieldsOnBenchmark();
        smcTimeEstimationButton.setText("Interrupt estimation");
        boolean saved = querySaved;
        querySaved = true;
        TAPNQuery query = getQuery();
        querySaved = saved;
        SMCSettings settings = query.getSmcSettings();
        query.setBenchmarkMode(true);
        query.setBenchmarkRuns(48);
        double timeWanted = 10;
        try {
            timeWanted = Double.parseDouble(smcTimeExpected.getText());
        } catch(NumberFormatException ignored) { }
        smcTimeExpected.setText("");
        smcEstimationIntervalWidth.setText("");
        double finalTimeWanted = timeWanted;
        VerificationCallback callback1 = result1 -> {
            query.setBenchmarkRuns(128);
            SMCStats stats1 = (SMCStats) result1.stats();
            float runsDone1 = stats1.getExecutedRuns();
            float time1 = stats1.getVerificationTime();
            VerificationCallback callback2 = result2 -> {
                doingBenchmark = false;
                updateFieldsOnBenchmark();
                SMCStats stats2 = (SMCStats) result2.stats();
                float runsDone2 = stats2.getExecutedRuns();
                float time2 = stats2.getVerificationTime();
                float coeff = (time2 - time1) / (runsDone2 - runsDone1);
                float stat_err = time1 - coeff * runsDone1;
                if(smcMustUpdateTime) {
                    double runsNeeded = (double) settings.chernoffHoeffdingBound();
                    double estimation = coeff * runsNeeded + stat_err;
                    if(estimation < 0) estimation = 0.01;
                    smcTimeExpected.setText(timeFormat.format(estimation));
                    smcEstimationIntervalWidth.setText(precisionFormat.format(smcSettings.estimationIntervalWidth));
                    smcTimeEstimationButton.setText(UPDATE_VERIFICATION_TIME_BTN_TEXT);

                    double desiredMin = smcEstimatedTimeSlider.getDesiredMin();
                    double desiredMax = smcEstimatedTimeSlider.getDesiredMax();
                    double proportion = (estimation - desiredMin) / (desiredMax - desiredMin);
                    int initialValue = (int) (proportion * smcEstimatedTimeSlider.getMaximum());
                    updatingSmcSettings = true;
                    smcEstimatedTimeSlider.setValue(
                        Math.max(smcEstimatedTimeSlider.getMinimum(), 
                                Math.min(initialValue, smcEstimatedTimeSlider.getMaximum())));
                    smcEstimatedTimeSlider.setToolTipText(String.format("Value: %.1f", estimation));
                    updatingSmcSettings = false;
                } else {
                    int runsNeeded = (int) Math.ceil( (finalTimeWanted - stat_err) / coeff );
                    float precision = settings.precisionFromRuns(runsNeeded);
                    smcEstimationIntervalWidth.setText(precisionFormat.format(precision));
                    smcTimeExpected.setText(String.valueOf(finalTimeWanted));
                    smcTimeEstimationButton.setText(UPDATE_PRECISION_BTN_TEXT);
                    updateSMCSettings();
                    setSMCSettings(smcSettings);

                    double desiredMin = smcEstimatedTimeSlider.getDesiredMin();
                    double desiredMax = smcEstimatedTimeSlider.getDesiredMax();
                    double proportion = (finalTimeWanted - desiredMin) / (desiredMax - desiredMin);
                    int initialValue = (int) (proportion * smcEstimatedTimeSlider.getMaximum());
                    updatingSmcSettings = true;
                    smcEstimatedTimeSlider.setValue(
                        Math.max(smcEstimatedTimeSlider.getMinimum(), 
                                Math.min(initialValue, smcEstimatedTimeSlider.getMaximum())));
                    smcEstimatedTimeSlider.setToolTipText(String.format("Value: %.1f", finalTimeWanted));
                    updatingSmcSettings = false;
                }
            };
            
            benchmarkThread = Verifier.runVerifyTAPNSilent(tapnNetwork, query, callback2, guiModels, false, lens);
        };
       
        benchmarkThread = Verifier.runVerifyTAPNSilent(tapnNetwork, query, callback1, guiModels,false, lens);
    }

    private void updateFieldsOnBenchmark() {
        smcConfidence.setEnabled(!doingBenchmark);
        smcEstimationIntervalWidth.setEnabled(!doingBenchmark);
        smcTimeExpected.setEnabled(!doingBenchmark);
        smcParallel.setEnabled(!doingBenchmark);
        smcVerificationType.setEnabled(!doingBenchmark);
        smcStepBoundValue.setEnabled(!doingBenchmark && !smcStepBoundInfinite.isSelected());
        smcStepBoundInfinite.setEnabled(!doingBenchmark && !smcTimeBoundInfinite.isSelected());
        smcTimeBoundValue.setEnabled(!doingBenchmark && !smcTimeBoundInfinite.isSelected());
        smcTimeBoundInfinite.setEnabled(!doingBenchmark && !smcStepBoundInfinite.isSelected());
        smcNumericPrecision.setEnabled(!doingBenchmark);
    }

}
