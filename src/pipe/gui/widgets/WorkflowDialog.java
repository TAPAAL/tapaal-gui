package pipe.gui.widgets;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.TraceConverter;
import dk.aau.cs.verification.VerificationResult;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pipe.dataLayer.*;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.WorkflowMode;
import pipe.gui.*;
import pipe.gui.GuiFrame.GUIMode;

public class WorkflowDialog extends JDialog {

	private static String getHelpMessage(){ 
		// There is automatic word wrapping in the control that displays the text, so you don't need line breaks in paragraphs.
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>");
		buffer.append("<b>Workflow Net</b><br/>");
		buffer.append("A workflow net must contain exactly one <em>input</em> place with no incoming arcs,\n");
                buffer.append("exactly one <em>output</em> place with no outgoing arcs and every other place has\n");
                buffer.append("at least one incoming and one outgoing arc. Every transition must have\n");
                buffer.append("at least one incoming arc (inhibitor arcs do not count). Strict time intervals are not allowed.<br/><br/>");
                buffer.append("Workflow nets that contain inhibitor arcs, age invariants or urgent transitions\n");
                buffer.append("are called <em>extended</em> workflow nets. Nets without these features are\n");
                buffer.append("called <em>monotonic</em>.\n");
		buffer.append("<br/><br/>");
		buffer.append("<b>Initial Marking, Final Markings</b><br/>");
		buffer.append("The <em>initial marking</em> has just one token of age 0 in the input place.\n");
                buffer.append("A <em>final marking</em> has one token in the output place and no other tokens\n");
                buffer.append("anywhere else in the workflow net.");
		buffer.append("<br/><br/>");
		buffer.append("<b>Soundness of Workflow Net</b><br/>");
                buffer.append("A workflow net is <em>sound</em> if we can reach a final marking from\n");
                buffer.append("any marking reachable from the initial marking and all markings that place\n");
                buffer.append("a token to the output place are final markings. For sound nets\n");
                buffer.append("we can compute the <em>minimum time duration</em> to move a token\n");
                buffer.append("from the input place to the output place.\n");
		buffer.append("<br/><br/>");
		buffer.append("<b>Strong Soundness of Workflow Net</b><br/>");
                buffer.append("A workflow net is <em>strongly sound</em> if it is sound and any computation\n");
                buffer.append("of the net is time-bounded (time cannot diverge for ever).\n");
                buffer.append("For strongly sound nets we can compute the <em>maximum time duration</em>\n");
                buffer.append("to move a token from the input place to the output place.\n");
		buffer.append("<br/><br/>");
		buffer.append("<b>Extra Number of Tokens</b><br/>");
                buffer.append("The user can set up the extra number of tokens in order to limit\n");
                buffer.append("the state-space during the verification. This is necessary for extended\n");
                buffer.append("workflow nets as soundness is otherwise undecidable. For monotonic nets\n");
                buffer.append("the tool provides conclusive answers also for unbounded nets but increasing\n");
                buffer.append("the number of extra tokens can speedup the verification and help with trace generation\n");
		buffer.append("<br/><br/>");
		buffer.append("<b>Typical Reasons for Breaking Soundness</b><br/>");
                buffer.append("<ul><li>A reachable deadlock marking (no available transition firing after any delay).</li>\n");
                buffer.append("<li>A reachable marking from which there is no computation to any final marking.</li>\n");
                buffer.append("<li>The output place can be marked with a token while there are some other tokens left in the net.</li></ul>\n");
		buffer.append("<br/><br/>");
		buffer.append("<b>Typical Reasons for Breaking Strong Soundness</b><br/>");
                buffer.append("<ul><li>The net is not sound.</li>\n");
                buffer.append("<li>There is a reachable marking where we can delay for ever (time-divergent marking).</li>\n");
                buffer.append("<li>There is an infinite execution where the accumulated time diverges to infinity.</li></ul>\n");
		buffer.append("</html>");
		return buffer.toString(); 
	}
	
	private static final String TOOLTIP_SOUNDNESS = "Soundness (option for proper termination).";
	private static final String TOOLTIP_MIN = "Calculate the minimum duration of the workflow.";
	private static final String TOOLTIP_STRONGSOUNDNESS = "Strong soundness (proper termination within a bounded time).";
	private static final String TOOLTIP_MAX = "Calculate the maximum duration of the workflow.";
	
	private static final String DISCRETE_SEMANTICS_WARNING = "<html>Because the workflow contains age invariants and/or urgent transitions,<br /> this result is only valid for the discrete semantics (integer delays).</html>";
	
	private static final String LABEL_TYPE_OF_WORKFLOW = "Type of the workflow:";
	private static final String LABEL_INPUT_PLACE = "Input place:";
	private static final String LABEL_OUTPUT_PLACE = "Output place:";
	private static final String LABEL_INHIBITOR_ARCS = "Inhibitor arcs:";
	private static final String LABEL_URGENT_TRANSITIONS = "Urgent transitions:";
	private static final String LABEL_INVARIANTS = "Age invariants:";

	private static final String LABEL_RESULT_SOUND = "Soundness:";
	private static final String LABEL_RESULT_MIN = "Minimum duration:";
	private static final String LABEL_RESULT_STRONG_SOUND = "Strong soundness:";
	private static final String LABEL_RESULT_MAX = "Maximum duration:";
	
	private static final String RESULT_STRING_SATISFIED = "Satisfied";
	private static final String RESULT_STRING_NOT_SATISFIED = "Not satisfied";
	private static final String RESULT_STRING_INCONCLUSIVE = "Inconclusive";
	private static final String RESULT_NOT_DEFINED = "Undefined";
	
	private static final String ERROR_INCREASE_BOUND = "Try to increase the number of extra tokens.";

	/* Soundness */
	
	private static final String RESULT_ERROR_NONFINAL_REACHED = "A non-final marking with a token in the output place is reachable.";
	private static final String RESULT_ERROR_NO_TRACE_TO_FINAL = "A marking is reachable from which a final marking cannot be reached.";
	private static final String LABEL_UNUSED_TRANSITIONS = "The workflow contains the following redundant transitions that are never enabled:";
	
	/* Strong Soundness */
	
	private static final String RESULT_ERROR_CYCLE = "The workflow has an infinite time-divergent execution.";
	private static final String RESULT_ERROR_TIME = "A time divergent marking is reachable.";
	/* Syntax */
	
	private static final String ERROR_MULTIPLE_IN = "Multiple input places found";
	private static final String ERROR_MULTIPLE_OUT = "Multiple output places found";
	
	private static final long serialVersionUID = 5613743579411748200L;

	private JPanel panel;

	private JCheckBox soundness = null;
	private JCheckBox strongSoundness = null;
	private JCheckBox min = null;
	private JCheckBox max = null;

	private JLabel soundnessResult;
	private JLabel strongSoundnessResult;
	private JLabel soundnessResultExplanation;
	private JLabel strongSoundnessResultExplanation;
	private JButton soundnessResultTraceButton;
	private TAPNNetworkTrace soundnessResultTrace = null;
	private JLabel minResult;
	private JButton minResultTraceButton;
	private TAPNNetworkTrace minResultTrace = null;
	private JLabel maxResult;
	private TAPNNetworkTrace maxResultTrace = null;
	private JButton maxResultTraceButton;
	private TAPNNetworkTrace strongSoundnessResultTrace = null;
	private JButton strongSoundnessResultTraceButton;
	private JLabel soundnessVerificationStats;
	private JLabel strongSoundnessVerificationStats;
	private JLabel unusedTransitionsLabel;
	private JLabel unusedTransitions;

	private JLabel soundnessResultLabel;
	private JLabel minResultLabel;
	private JLabel strongSoundnessResultLabel;
	private JLabel maxResultLabel;

	private JPanel resultPanel = null;

	private CustomJSpinner numberOfExtraTokensInNet = null;

	private ArrayList<String> errorMsgs = new ArrayList<String>();
	private int errors = 0;
	private LinkedList<Runnable> verificationQueue = new LinkedList<Runnable>();
	private ArrayList<SharedPlace> unusedSharedPlaces = new ArrayList<SharedPlace>();

	private TimedPlace in;
	private TimedPlace out;
	private TimedArcPetriNet out_template;

	private boolean isSound = false;
	private boolean isConclusive = true;
	private long m;
	private int B;
	private Constant c = null;
	private TimedPlace done = null;
	
	private static int numErrorsShown = 5;
	private static int maxStringLength = LABEL_UNUSED_TRANSITIONS.length();

	private TimedArcPetriNetNetwork model = null;

	private boolean isInTraceMode = false;

	private enum TAWFNTypes {
		ETAWFN, MTAWFN, NOTTAWFN
	}

	TAWFNTypes netType;

	public static void showDialog() {
		if(CreateGui.getCurrentTab().getWorkflowDialog() == null){
			CreateGui.getCurrentTab().setWorkflowDialog(new WorkflowDialog(CreateGui.getApp(), "Workflow Analysis", true));
		}else if(!CreateGui.getCurrentTab().getWorkflowDialog().isInTraceMode){
			WorkflowDialog oldDialog = CreateGui.getCurrentTab().getWorkflowDialog();
			CreateGui.getCurrentTab().setWorkflowDialog(new WorkflowDialog(CreateGui.getApp(), "Workflow Analysis", true));
			WorkflowDialog newDialog = CreateGui.getCurrentTab().getWorkflowDialog();
			newDialog.soundness.setSelected(oldDialog.soundness.isSelected());
			newDialog.min.setSelected(oldDialog.min.isSelected());
			newDialog.strongSoundness.setSelected(oldDialog.strongSoundness.isSelected());
			newDialog.max.setSelected(oldDialog.max.isSelected());
			newDialog.max.setEnabled(oldDialog.max.isEnabled());
		}

		CreateGui.getCurrentTab().getWorkflowDialog().isInTraceMode = false;
		CreateGui.getCurrentTab().getWorkflowDialog().setVisible(true);
	}

	public boolean restoreWindow(){
		return isInTraceMode;
	}

	private void switchToTrace(TAPNNetworkTrace trace){
		isInTraceMode = true;
		setVisible(false);
		CreateGui.getApp().setGUIMode(GUIMode.animation);
		CreateGui.getAnimator().SetTrace(trace);
	}

	private WorkflowDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);

		/* Copy model */

		model = CreateGui.getCurrentTab().network().copy();
		
		// Fix - remove unused shared places
		unusedSharedPlaces.clear();
		for(SharedPlace p : model.sharedPlaces()){
			if(p.getComponentsUsingThisPlace().isEmpty()){
				unusedSharedPlaces.add(p);
			}
		}
		for(SharedPlace p : unusedSharedPlaces){
			model.remove(p);
		}

		/* Make dialog */

		initComponents();
		setContentPane(panel);

		pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = (screenSize.width - getWidth()) / 2;
		int y = (int) ((screenSize.height - getHeight()) / 2 * 0.5);
		setLocation(x, y);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

	private void initComponents() {
		panel = new JPanel(new GridBagLayout());

		/* Check if workflow net */
		netType = getWorkflowType();

		JPanel informationPanel = new JPanel();
		informationPanel.setBorder(BorderFactory
				.createTitledBorder("About the Workflow"));
		informationPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		panel.add(informationPanel, gbc);

		gbc.gridwidth = 1;
		JLabel workflowType = new JLabel(LABEL_TYPE_OF_WORKFLOW, SwingConstants.RIGHT);
		informationPanel.add(workflowType, gbc);

		gbc.gridx = 1;

		JLabel workflowTypeLabel = new JLabel("");
		informationPanel.add(workflowTypeLabel, gbc);

		JLabel workflowTypeError = new JLabel("");
		workflowTypeError.setVisible(false);
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		informationPanel.add(workflowTypeError, gbc);
		gbc.gridwidth = 1;

		switch (netType) {
		case MTAWFN:
			workflowTypeLabel.setText("Monotonic workflow net");
			break;
		case ETAWFN:
			workflowTypeLabel.setText("Extended workflow net");
			break;
		case NOTTAWFN:
			workflowType.setVisible(false);
			workflowTypeLabel.setVisible(false);
			StringBuilder sb = new StringBuilder();
			String sep = "<br>";
			for (String e : errorMsgs)
				sb.append(sep).append("- ").append(e);
			workflowTypeError
			.setText("<html>This net is not a workflow net for the following reason"+(errorMsgs.size() > 1?"s":"")+":"
					+ sb.toString() + "</html>");
			workflowTypeError.setVisible(true);
			break;
		}

		/* Initialize component to store settings */
		if (soundness == null)
			soundness = new JCheckBox("Check soundness.");

		if (min == null){
			min = new JCheckBox("Calculate minimum duration.");
			min.setSelected(true);
			min.setToolTipText(TOOLTIP_MIN);
		}

		if (strongSoundness == null)
			strongSoundness = new JCheckBox("Check strong soundness.");

		if (max == null)
			max = new JCheckBox("Calculate maximum duration.");


		if (netType != TAWFNTypes.NOTTAWFN) {
			gbc.gridy = 1;
			gbc.gridx = 0;
			informationPanel.add(new JLabel(LABEL_INPUT_PLACE, SwingConstants.RIGHT), gbc);

			JLabel inPlaceLabel = new JLabel(in.toString());
			gbc.gridx = 1;
			informationPanel.add(inPlaceLabel, gbc);

			gbc.gridy = 2;
			gbc.gridx = 0;
			informationPanel.add(new JLabel(LABEL_OUTPUT_PLACE, SwingConstants.RIGHT), gbc);
			JLabel outPlaceLabel = new JLabel(out.toString());
			gbc.gridx = 1;
			informationPanel.add(outPlaceLabel, gbc);

			gbc.gridy = 0;
			gbc.gridx = 3;
			informationPanel.add(new JLabel(LABEL_INHIBITOR_ARCS, SwingConstants.RIGHT), gbc);
			gbc.gridx = 4;
			informationPanel.add(new JLabel(model.hasInhibitorArcs()? "Yes":"No"), gbc);

			gbc.gridy = 1;
			gbc.gridx = 3;
			informationPanel.add(new JLabel(LABEL_URGENT_TRANSITIONS, SwingConstants.RIGHT), gbc);
			gbc.gridx = 4;
			informationPanel.add(new JLabel(model.hasUrgentTransitions()? "Yes":"No"), gbc);

			gbc.gridy = 2;
			gbc.gridx = 3;
			informationPanel.add(new JLabel(LABEL_INVARIANTS, SwingConstants.RIGHT), gbc);
			gbc.gridx = 4;
			informationPanel.add(new JLabel(model.hasInvariants()? "Yes":"No"), gbc);


			initValidationPanel();
		}

		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		JButton help_button = new JButton("Help");
		help_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(CreateGui.getAppGui(), getMessageComponent(), "Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		panel.add(help_button, gbc);

		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		JButton close_button = new JButton("Close");
		close_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		panel.add(close_button, gbc);
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

	private void initValidationPanel() {

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);

		JPanel soundnessPanel = new JPanel();
		soundnessPanel.setBorder(BorderFactory.createTitledBorder("Workflow Properties"));
		soundnessPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		panel.add(soundnessPanel, gbc);

		gbc.gridwidth = 1;

		soundness.setToolTipText(TOOLTIP_SOUNDNESS);
		soundness.setSelected(true);
		soundness.setEnabled(false);
		gbc.gridx = 0;
		gbc.gridy = 1;
		soundnessPanel.add(soundness, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		soundnessPanel.add(min, gbc);

		strongSoundness.setToolTipText(TOOLTIP_STRONGSOUNDNESS);
		strongSoundness.setSelected(true);
		gbc.gridx = 0;
		gbc.gridy = 2;
		soundnessPanel.add(strongSoundness, gbc);

		max.setToolTipText(TOOLTIP_MAX);
		max.setSelected(true);
		gbc.gridx = 1;
		gbc.gridy = 2;
		soundnessPanel.add(max, gbc);

		strongSoundness.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				max.setEnabled(strongSoundness.isSelected());
				if (!strongSoundness.isSelected()) {
					max.setSelected(false);
				}else{
					max.setSelected(true);
				}
			}
		});

		// Initialize correct state
		max.setEnabled(strongSoundness.isSelected());
		if (!strongSoundness.isSelected()) {
			max.setSelected(false);
		}

		/* Result panel */

		if(resultPanel == null){
			resultPanel = new JPanel();
			resultPanel.setBorder(BorderFactory
					.createTitledBorder("Analysis Results"));
			resultPanel.setLayout(new GridBagLayout());
			resultPanel.setVisible(false);
		}
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		panel.add(resultPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 5, 0, 5);

		/* Initialize results panel on first invokation */
		if(soundnessResultLabel == null){
			soundnessResultLabel = new JLabel(LABEL_RESULT_SOUND);
			soundnessResultLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			resultPanel.add(soundnessResultLabel, gbc);
			soundnessResultLabel.setVisible(false);

			soundnessResult = new JLabel();
			gbc.gridx = 1;
			resultPanel.add(soundnessResult, gbc);
			soundnessResult.setVisible(false);

			gbc.gridx = 2;
			soundnessVerificationStats = new JLabel();
			soundnessVerificationStats.setVisible(false);
			resultPanel.add(soundnessVerificationStats, gbc);

			soundnessResultExplanation = new JLabel();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(0, 5, 0, 5);
			soundnessResultExplanation.setVisible(false);
			soundnessResultExplanation.setEnabled(false);
			resultPanel.add(soundnessResultExplanation, gbc);

			gbc.gridwidth = 1;

			soundnessResultTraceButton = new JButton("Show trace");
			gbc.gridx = 2;
			soundnessResultTraceButton.setVisible(false);
			soundnessResultTraceButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					switchToTrace(soundnessResultTrace);
				}
			});
			resultPanel.add(soundnessResultTraceButton, gbc);

			// Min 
			gbc.gridy = 2;
			gbc.gridx = 0;
			gbc.insets = new Insets(10, 5, 5, 5);
			minResultLabel = new JLabel(LABEL_RESULT_MIN);
			minResultLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			resultPanel.add(minResultLabel, gbc);
			minResultLabel.setVisible(false);

			minResult = new JLabel();
			gbc.gridx = 1;
			resultPanel.add(minResult, gbc);
			minResult.setVisible(false);

			minResultTraceButton = new JButton("Show trace");
			gbc.gridx = 2;
			minResultTraceButton.setVisible(false);
			minResultTraceButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					switchToTrace(minResultTrace);
				}
			});
			resultPanel.add(minResultTraceButton, gbc);

			// Strong soundness

			gbc.gridwidth = 1;
			gbc.gridy = 3;
			gbc.gridx = 0;
			gbc.insets = new Insets(5, 5, 0, 5);
			strongSoundnessResultLabel = new JLabel(LABEL_RESULT_STRONG_SOUND);
			strongSoundnessResultLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			resultPanel.add(strongSoundnessResultLabel, gbc);
			strongSoundnessResultLabel.setVisible(false);

			strongSoundnessResult = new JLabel();
			gbc.gridx = 1;
			resultPanel.add(strongSoundnessResult, gbc);
			strongSoundnessResult.setVisible(false);

			strongSoundnessVerificationStats = new JLabel();
			gbc.gridx = 2;
			strongSoundnessVerificationStats.setVisible(false);
			resultPanel.add(strongSoundnessVerificationStats, gbc);

			strongSoundnessResultExplanation = new JLabel();
			gbc.gridx = 0;
			gbc.gridy = 4;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(0, 5, 0, 5);
			strongSoundnessResultExplanation.setVisible(false);
			strongSoundnessResultExplanation.setEnabled(false);
			resultPanel.add(strongSoundnessResultExplanation, gbc);

			gbc.gridwidth = 1;

			strongSoundnessResultTraceButton = new JButton("Show trace");
			gbc.gridx = 2;
			strongSoundnessResultTraceButton.setVisible(false);
			strongSoundnessResultTraceButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					switchToTrace(strongSoundnessResultTrace);
				}
			});
			resultPanel.add(strongSoundnessResultTraceButton, gbc);

			// Max 

			gbc.gridy = 5;
			gbc.gridx = 0;
			gbc.insets = new Insets(10, 5, 5, 5);
			maxResultLabel = new JLabel(LABEL_RESULT_MAX);
			maxResultLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			resultPanel.add(maxResultLabel, gbc);
			maxResultLabel.setVisible(false);

			maxResult = new JLabel();
			gbc.gridx = 1;
			resultPanel.add(maxResult, gbc);
			maxResult.setVisible(false);

			maxResultTraceButton = new JButton("Show trace");
			gbc.gridx = 2;
			maxResultTraceButton.setVisible(false);
			maxResultTraceButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					switchToTrace(maxResultTrace);
				}
			});
			resultPanel.add(maxResultTraceButton, gbc);
			
			unusedTransitionsLabel = new JLabel();
			gbc.gridy = 6;
			gbc.gridx = 0;
			gbc.gridwidth = 3;
			gbc.insets = new Insets(10, 5, 0, 5);
			unusedTransitionsLabel.setText(LABEL_UNUSED_TRANSITIONS);
			unusedTransitionsLabel.setVisible(false);
			resultPanel.add(unusedTransitionsLabel, gbc);
			
			unusedTransitions = new JLabel();
			gbc.gridy = 7;
			gbc.insets = new Insets(0, 5, 5, 5);
			unusedTransitions.setVisible(false);
			unusedTransitions.setEnabled(false);
			resultPanel.add(unusedTransitions, gbc);
		}

		gbc.insets = new Insets(5, 5, 5, 5);

		/* K-bound panel */

		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		panel.add(new JLabel(" Number of extra tokens:  "), gbc);

		if (numberOfExtraTokensInNet == null)
			numberOfExtraTokensInNet = new CustomJSpinner(model.getDefaultBound(), 0,
					100000);	// Allow at most 100.000 extra tokens.
		else
			numberOfExtraTokensInNet.setValue(model.getDefaultBound());
		
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(55, 30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(55, 30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(55, 30));
		gbc.gridx = 1;
		gbc.gridy = 6;
		panel.add(numberOfExtraTokensInNet, gbc);

		numberOfExtraTokensInNet.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				model.setDefaultBound((Integer) numberOfExtraTokensInNet.getValue());
				CreateGui.getCurrentTab().network().setDefaultBound((Integer) numberOfExtraTokensInNet.getValue());
				CreateGui.getDrawingSurface().setNetChanged(true);
			}
		});

		gbc.gridwidth = 1;

		JButton checkBound = new JButton("Check boundedness");
		gbc.gridx = 2;
		panel.add(checkBound, gbc);

		checkBound.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				checkBound();
			}
		});

		JButton checkIfSound = new JButton("Analyse the workflow");
		getRootPane().setDefaultButton(checkIfSound);
		checkIfSound.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				checkProperties();
			}
		});
		gbc.gridx = 2;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(checkIfSound, gbc);
	}

	private TAWFNTypes getWorkflowType() {
		List<TimedArcPetriNet> tapns = model.activeTemplates();
		ArrayList<TimedPlace> sharedInPlaces = new ArrayList<TimedPlace>();
		ArrayList<TimedPlace> sharedOutPlaces = new ArrayList<TimedPlace>();
		ArrayList<TimedPlace> sharedAcceptedPlaces = new ArrayList<TimedPlace>();
		ArrayList<TimedPlace> sharedOrphanedPlaces = new ArrayList<TimedPlace>();
		ArrayList<SharedTransition> sharedTransitions = new ArrayList<SharedTransition>();
		in = null;
		out = null;
		ArrayList<TimedPlace> inCandidates = new ArrayList<TimedPlace>();
		ArrayList<TimedPlace> outCandidates = new ArrayList<TimedPlace>();
		errorMsgs = new ArrayList<String>();
		errors = 0;
		
		if(!model.isNonStrict()){
			errorMsgs.add("The net contains strict intervals.");
		}
	
		boolean isin;
		boolean isout;
		boolean isMonotonic = true;
		int numberOfTokensInNet = 0;
		ArrayList<TimedPlace> countedSharedPlaces = new ArrayList<TimedPlace>();

		for (TimedArcPetriNet tapn : tapns) {
			for (TimedPlace p : tapn.places()) {
				isin = true;
				isout = true;

				p.invariant().asIterval();
				if (isMonotonic
						&& !p.invariant().asIterval()
						.equals(TimeInterval.ZERO_INF)) {
					isMonotonic = false;
				}

				// Test for arcs going in to place
				for (TimedOutputArc arc : tapn.outputArcs()) {
					if (arc.destination().equals(p)) {
						isin = false;
						break;
					}
				}

				// Test for arcs going out from place
				for (TimedInputArc arc : tapn.inputArcs()) {
					if (arc.source().equals(p)) {
						isout = false;
						break;
					}
				}

				// Transport arcs
				for (TransportArc arc : tapn.transportArcs()) {
					if (arc.destination().equals(p)) {
						isin = false;
					}
					if (arc.source().equals(p)) {
						isout = false;
					}
					if (!isin && !isout)
						break;
				}
				
				
				if (p.isShared()) {
					if(isin && isout){
						sharedOrphanedPlaces.add(p);
					}else{
						if (isin) {
							sharedInPlaces.add(p);
						}else if (isout) {
							sharedOutPlaces.add(p);
						}else if(!isin && !isout){
							sharedAcceptedPlaces.add(p);
						}
						
						sharedOrphanedPlaces.remove(p);
					}

				} else if (isin && isout) {
					if(errorMsgs.size() > 5)	errors++;
					else
						errorMsgs.add("Place " + p
								+ " has no incoming or outgoing arcs.");
				} else if (isin) {
					inCandidates.add(p);
				} else if (isout) {
					outCandidates.add(p);
				}

				if (p.isShared() && !countedSharedPlaces.contains(p)) {
					numberOfTokensInNet += p.numberOfTokens();
					countedSharedPlaces.add(p);
				} else if (!p.isShared()) {
					numberOfTokensInNet += p.numberOfTokens();
				}
			}

			for (TimedTransition t : tapn.transitions()) {
				if (t.isShared()) {
					sharedTransitions.add(t.sharedTransition());
				} else if (t.getInputArcs().isEmpty()
						&& t.getTransportArcsGoingThrough().isEmpty()) {
					if(errorMsgs.size() > 5)	errors++;
					else
						errorMsgs.add("Transition " + t.name()
								+ " has no incoming arcs.");
				}

				if (isMonotonic
						&& (t.isUrgent() || !t.getInhibitorArcs().isEmpty())) {
					isMonotonic = false;
				}
			}
		}

		outer: while (sharedTransitions.size() > 0) {
			SharedTransition st = sharedTransitions.get(0);
			for (TimedTransition t : st.transitions()) {
				if (!t.getTransportArcsGoingThrough().isEmpty()
						|| !t.getInputArcs().isEmpty()) {
					while (sharedTransitions.remove(st)) {
					}
					continue outer;
				}
			}
			if(errorMsgs.size() > 5)	errors++;
			else
				errorMsgs.add("Transition " + st.name() + " has no incoming arcs.");
			while (sharedTransitions.remove(st)) {
			}
		}

		for(TimedPlace p : sharedAcceptedPlaces){
			while (sharedInPlaces.remove(p)) {
			}
			while (sharedOutPlaces.remove(p)) {
			}
			while (sharedOrphanedPlaces.remove(p)) {
			}
		}


		while (sharedInPlaces.size() != 0) {
			TimedPlace p = sharedInPlaces.get(0);
			while (sharedInPlaces.remove(p)) {
			}
			if (!sharedOutPlaces.remove(p)) {
				inCandidates.add(p);
			}
			while (sharedOutPlaces.remove(p)) {
			}
			while (sharedOrphanedPlaces.remove(p)) {
			}
		}

		while (sharedOutPlaces.size() > 0) {
			TimedPlace p = sharedOutPlaces.get(0);
			outCandidates.add(p);
			while (sharedOutPlaces.remove(p)) {
			}
			while (sharedOrphanedPlaces.remove(p)) {
			}
		}
		
		if(inCandidates.isEmpty()){
			if(errorMsgs.size() > 5)	errors++;
			else
				errorMsgs.add("No input place found.");
		}else if(inCandidates.size() > 1){
			if(errorMsgs.size() > 5)	errors++;
			else{
				StringBuilder errorString = new StringBuilder(ERROR_MULTIPLE_IN + " (");
				int lineLength = errorString.length();
				for(TimedPlace p : inCandidates){
					if(lineLength + p.toString().length() > maxStringLength){
						errorString.append("<br>");
						lineLength = 0;
					}
					errorString.append(p + ", ");
					lineLength += p.toString().length() + 2;
				}
				errorString.delete(errorString.length()-2, errorString.length());
				errorString.append(").");
				errorMsgs.add(errorString.toString());
			}
		}else{
			in = inCandidates.get(0);
		}
		
		if(outCandidates.isEmpty()){
			if(errorMsgs.size() > 5)	errors++;
			else
				errorMsgs.add("No output place found.");
		}else if(outCandidates.size() > 1){
			if(errorMsgs.size() > 5)	errors++;
			else{
				StringBuilder errorString = new StringBuilder(ERROR_MULTIPLE_OUT + " (");
				int lineLength = errorString.length();
				for(TimedPlace p : outCandidates){
					if(lineLength + p.toString().length() > maxStringLength){
						errorString.append("<br>");
						lineLength = 0;
					}
					errorString.append(p + ", ");
					lineLength += p.toString().length() + 2;
				}
				errorString.delete(errorString.length()-2, errorString.length());
				errorString.append(").");
				errorMsgs.add(errorString.toString());
			}
		}else{
			out = outCandidates.get(0);
		}


		if (numberOfTokensInNet > 1 || (in != null && in.tokens().size() != 1)) {
			if(errorMsgs.size() > 5)	errors++;
			else
				errorMsgs
				.add("The current marking is not a valid initial marking.");
		}
		
		for(TimedPlace p : sharedOrphanedPlaces){
			errorMsgs.add("The shared place \"" + p.name() + "\" has no incoming or outgoing arcs.");
		}

		if (!errorMsgs.isEmpty()) {
			if(errors > 0)	errorMsgs.add("and "+errors+" other problems.");
			return TAWFNTypes.NOTTAWFN;
		}

		int i = 0;
		outer: for(TimedArcPetriNet t : model.activeTemplates()){
			for(TimedPlace p : t.places()){
				if(p.equals(out)){
					break outer;
				}
			}
			i++;
		}

		out_template = model.activeTemplates().get(i);

		return isMonotonic ? TAWFNTypes.MTAWFN : TAWFNTypes.ETAWFN;
	}

	private void checkProperties() {
		// Clear old results
		soundnessResult.setText("");
		soundnessResult.setVisible(false);
		soundnessResultLabel.setVisible(false);
		soundnessResultTraceButton.setVisible(false);
		minResult.setText("");
		minResult.setVisible(false);
		minResultLabel.setVisible(false);
		minResultTraceButton.setVisible(false);
		strongSoundnessResult.setText("");
		strongSoundnessResult.setVisible(false);
		strongSoundnessResultLabel.setVisible(false);
		strongSoundnessResultTraceButton.setVisible(false);
		maxResult.setText("");
		maxResult.setVisible(false);
		maxResultLabel.setVisible(false);
		maxResultTraceButton.setVisible(false);
		soundnessResultExplanation.setVisible(false);
		soundnessVerificationStats.setVisible(false);
		strongSoundnessResultExplanation.setVisible(false);
		strongSoundnessVerificationStats.setVisible(false);
		unusedTransitionsLabel.setVisible(false);
		unusedTransitions.setVisible(false);
		unusedTransitions.setText("");
		resultPanel.setVisible(true);

		pack();

		verificationQueue.clear();
		isSound = false;
		isConclusive = true;

		verificationQueue.add(getSoundnessRunnable());

		if (strongSoundness.isSelected()) {
			verificationQueue.add(getStrongSoundnessRunnable());
		}

		// Run steps
		while (!verificationQueue.isEmpty()) {
			verificationQueue.get(0).run();
			verificationQueue.remove(0);
		}
	}

	private Runnable getStrongSoundnessRunnable() {
		return new Runnable() {

			@Override
			public void run() {

				// Check preliminary conditions
				if(!isSound){
					setStrongSoundnessResult(false,isConclusive?"The workflow is not sound.":"The soundness check was inconclusive.", isConclusive);
					return;
				}

				// Compute B
				B = 0;
				for (TimedArcPetriNet t : model
						.activeTemplates()) {
					for (TimedPlace p : t.places()) {
						if (p.invariant().upperBound().equals(Bound.Infinity)) {
							continue;
						}
						B = Math.max(B, p.invariant().upperBound().value());
					}
				}
                
				long c  = m*B+1;

				/* Call engine */

				final TAPNQuery q;
                            q = new TAPNQuery(
                                    "Workflow strong soundness checking",
                                    numberOfExtraTokensInNet == null ? 0
                                            : (Integer) numberOfExtraTokensInNet.getValue(),
                                    new TCTLEGNode(new TCTLTrueNode()), TraceOption.SOME,
                                    SearchOption.DEFAULT,
                                    ReductionOption.VerifyTAPNdiscreteVerification, true, true,
                                    false, true, false, null, ExtrapolationOption.AUTOMATIC, WorkflowMode.WORKFLOW_STRONG_SOUNDNESS, c);
				Verifier.runVerifyTAPNVerification(model, q, new VerificationCallback() {

					@Override
					public void run() {
					}

					@Override
					public void run(VerificationResult<TAPNNetworkTrace> result) {
						if(result.isQuerySatisfied()){
							
							switch(((TimedTAPNNetworkTrace) result.getTrace()).getTraceType()){
							case EG_LOOP:
								setStrongSoundnessResult(false, RESULT_ERROR_CYCLE);
								break;
							case EG_DELAY_FOREVER:
								setStrongSoundnessResult(false, RESULT_ERROR_TIME);
								break;
							default:
								assert(false);
								break;
							}

							strongSoundnessResultTrace = mapTraceToRealModel(result.getTrace());
							strongSoundnessResultTraceButton.setVisible(true);

							if(max.isSelected()){
								maxResult.setText(RESULT_NOT_DEFINED);
								maxResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
								maxResult.setVisible(true);
							}
						}else{
							setStrongSoundnessResult(true, null);
							if(max.isSelected()){
								setMaxResult(model, result.stats().maximumExecutionTime());
								maxResultTrace = mapTraceToRealModel(result.getTrace());
								maxResultTraceButton.setVisible(true);
							}
						}


						strongSoundnessVerificationStats.setText(result
								.getVerificationTimeString().replace("Estimated verification time", "Est. time")
								+ ", memory: "
								+ MemoryMonitor.getPeakMemory());
						strongSoundnessVerificationStats.setVisible(true);

						pack();
					}
				});
			}
		};
	}

	private void setMaxResult(TimedArcPetriNetNetwork model, int value){
		maxResult.setText(value + " time units.");
		maxResult.setForeground(Pipe.QUERY_SATISFIED_COLOR);
		maxResult.setVisible(true);
		maxResultLabel.setVisible(true);
		pack();
	}

	private void setStrongSoundnessResult(boolean satisfied, String explanation) {
		setStrongSoundnessResult(satisfied, explanation, true);
	}

	private void setStrongSoundnessResult(boolean satisfied, String explanation, boolean conclusive) {
		if (satisfied) {
			strongSoundnessResult.setText(RESULT_STRING_SATISFIED);
			strongSoundnessResult.setForeground(Pipe.QUERY_SATISFIED_COLOR);
		} else {
			if(conclusive){
				strongSoundnessResult.setText(RESULT_STRING_NOT_SATISFIED);
				strongSoundnessResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
			}else{
				strongSoundnessResult.setText(RESULT_STRING_INCONCLUSIVE);
				strongSoundnessResult.setForeground(Pipe.QUERY_INCONCLUSIVE_COLOR);
			}
			if(max.isSelected()){
				maxResult.setText(RESULT_NOT_DEFINED);
				maxResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
				maxResultLabel.setVisible(true);
				maxResult.setVisible(true);
			}
		}

		if (explanation != null) {
			strongSoundnessResultExplanation.setText(explanation);
			strongSoundnessResultExplanation.setVisible(true);
		}
		strongSoundnessResultLabel.setVisible(true);
		strongSoundnessResult.setVisible(true);

		pack();
	}

	private Runnable getSoundnessRunnable() {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				final TAPNQuery q = new TAPNQuery(
						"Workflow soundness checking",
						numberOfExtraTokensInNet == null ? 0
								: (Integer) numberOfExtraTokensInNet.getValue(),
								new TCTLEFNode(new TCTLTrueNode()), TraceOption.SOME,
								SearchOption.DEFAULT,
								ReductionOption.VerifyTAPNdiscreteVerification, true, true,
								false, true, false, null, ExtrapolationOption.AUTOMATIC,
								WorkflowMode.WORKFLOW_SOUNDNESS);
				Verifier.runVerifyTAPNVerification(model, q, new VerificationCallback() {

					@Override
					public void run() {
					}

					@Override
					public void run(VerificationResult<TAPNNetworkTrace> result) {
						if (result.isQuerySatisfied()) {
							soundnessResult
							.setText(RESULT_STRING_SATISFIED);
							soundnessResult
							.setForeground(Pipe.QUERY_SATISFIED_COLOR);
							if(model.hasUrgentTransitions() || model.hasInvariants()){
								soundnessResultExplanation.setText(DISCRETE_SEMANTICS_WARNING);
								soundnessResultExplanation.setVisible(true);
							}
							isSound = true;
							
							// Detect unused transitions
							boolean hasUnusedTransitions = false;
							StringBuilder sb = new StringBuilder();
							int lineLength = 0;
							sb.append("<html>");
							for(Tuple<String, Integer> stat : result.getTransitionStatistics()){
								if(stat.value2() == 0){
									if(!hasUnusedTransitions){
										hasUnusedTransitions = true;
									}else{
										sb.append(", ");
										lineLength += 2;
										if(lineLength > maxStringLength - stat.value1().length()){
											sb.append("<br />");
											lineLength = 0;
										}
									}
									sb.append(stat.value1());
									lineLength += stat.value1().length();
								}
							}
							sb.append("</html>");
							if(hasUnusedTransitions){
								unusedTransitionsLabel.setVisible(true);
								unusedTransitions.setText(sb.toString());
								unusedTransitions.setVisible(true);
							}
						} else if (netType == TAWFNTypes.ETAWFN && !result.isBounded()) {
							soundnessResult
							.setText(RESULT_STRING_INCONCLUSIVE);
							soundnessResult
							.setForeground(Pipe.QUERY_INCONCLUSIVE_COLOR);
							isConclusive = false;
							soundnessResultExplanation.setText(ERROR_INCREASE_BOUND);
							soundnessResultExplanation.setVisible(true);
						} else {
							soundnessResult
							.setText(RESULT_STRING_NOT_SATISFIED);
							soundnessResult
							.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
							soundnessResultTrace = mapTraceToRealModel(result.getTrace());
							soundnessResultTraceButton.setVisible(true);
							soundnessResultExplanation.setText(determineSoundnessError(result.getTrace()));
							soundnessResultExplanation.setVisible(true);

							NetworkMarking coveredMarking = result.getCoveredMarking(model);
							if(coveredMarking != null){
								completeSoundnessTrace(result, coveredMarking);
							}
						}
						soundnessResult.setVisible(true);
						soundnessResultLabel.setVisible(true);

						if (min.isSelected()) {
							if(result.isQuerySatisfied()){
								minResult.setText(result.stats()
										.minimumExecutionTime()
										+ " time units.");
								minResult.setForeground(Pipe.QUERY_SATISFIED_COLOR);
								minResultTrace = mapTraceToRealModel(result.getTrace());
								minResultTraceButton.setVisible(true);
							}else{
								minResult.setText(RESULT_NOT_DEFINED);
								minResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
							}
							minResultLabel.setVisible(true);
							minResult.setVisible(true);
						}

						soundnessVerificationStats.setText(result
								.getVerificationTimeString().replace("Estimated verification time", "Est. time")
								+ ", memory: "
								+ MemoryMonitor.getPeakMemory());
						soundnessVerificationStats.setVisible(true);

						m = result.stats().exploredStates();

						pack();
					}

					private void completeSoundnessTrace(final VerificationResult<TAPNNetworkTrace> soundnessResult, final NetworkMarking coveredMarking) {
						final String explanationText = soundnessResultExplanation.getText();
						soundnessResultExplanation.setText(explanationText + " Computing trace.");
						soundnessResultTraceButton.setVisible(false);

						final NetworkMarking oldMarking = model.marking();
						model.setMarking(coveredMarking);

						final TAPNQuery q = new TAPNQuery(
								"Workflow computing trace",
								numberOfExtraTokensInNet == null ? 0
										: (Integer) numberOfExtraTokensInNet.getValue(),
										new TCTLEFNode(new TCTLAtomicPropositionNode(new TCTLPlaceNode(out.isShared()?"":out_template.name(), out.name()), ">=",new TCTLConstNode(1))), TraceOption.SOME,
										SearchOption.DEFAULT,
										ReductionOption.VerifyTAPNdiscreteVerification, true, true,
										true, true, null, ExtrapolationOption.AUTOMATIC);
						Verifier.runVerifyTAPNVerification(model, q, new VerificationCallback() {

							@Override
							public void run() {
							}

							@Override
							public void run(VerificationResult<TAPNNetworkTrace> result) {
								model.setMarking(oldMarking);
								soundnessResultExplanation.setText(explanationText);
								if(result.isQuerySatisfied()){
									appendTrace(mapTraceToRealModel(result.getTrace()));
									soundnessResultExplanation.setText(RESULT_ERROR_NONFINAL_REACHED);
									soundnessResultTraceButton.setVisible(true);
								}else{
									if(result.isBounded()){
										// Compute trace to covered marking
										soundnessResultTrace = mapTraceToRealModel(soundnessResult.getSecondaryTrace());
										soundnessResultTraceButton.setVisible(true);
									}else{
										soundnessResultExplanation.setText("<html>The monotonic workflow net has a reachable marking covering another one.<br/>Try to increase the number of extra tokens so that a trace can be generated.</html>");
									}
								}
							}

							private void appendTrace(TAPNNetworkTrace trace) {
								for(TAPNNetworkTraceStep step : trace){
									((TimedTAPNNetworkTrace) soundnessResultTrace).add(step);
								}
							}
						});

					}

					private String determineSoundnessError(
							TAPNNetworkTrace trace) {

						Iterator<TAPNNetworkTraceStep> iter = trace.iterator();
						NetworkMarking final_marking = model.marking().clone(); 
						while(iter.hasNext()) final_marking = iter.next().performStepFrom(final_marking);

						int out_size = final_marking.getTokensFor(out).size();
						if(out_size > 0 && final_marking.size() != 1){
							return RESULT_ERROR_NONFINAL_REACHED;
						}

						// Detect if any transition is dEnabled from last marking (not deadlock)
						String output = "A deadlock marking is reachable.";
						NetworkMarking oldMarking = model.marking();
						model.setMarking(final_marking);
						outer: for( TimedArcPetriNet temp : model.activeTemplates()){
							Iterator<TimedTransition> transitionIterator = temp.transitions().iterator();
							while (transitionIterator.hasNext()) {
								TimedTransition tempTransition = transitionIterator.next();
								if (tempTransition.isDEnabled()){
									output = RESULT_ERROR_NO_TRACE_TO_FINAL;
									break outer;
								}
							}
						}
						model.setMarking(oldMarking);

						return output;
					}
				});
			}
		};
		return r;
	}

	private void checkBound() {
		Verifier.analyzeKBound(model,
				(Integer) numberOfExtraTokensInNet.getValue(),
				numberOfExtraTokensInNet);
	}

	private TAPNNetworkTrace mapTraceToRealModel(TAPNNetworkTrace tapnNetworkTrace){
		TraceConverter converter = new TraceConverter(tapnNetworkTrace, CreateGui.getCurrentTab().network());
		return converter.convert();
	}
}
