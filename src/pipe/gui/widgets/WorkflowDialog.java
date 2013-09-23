package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import pipe.gui.*;
import pipe.gui.GuiFrame.GUIMode;
import pipe.dataLayer.*;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.ModelType;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.TraceConverter;
import dk.aau.cs.verification.VerificationResult;

public class WorkflowDialog extends JDialog {

	private static final long serialVersionUID = 5613743579411748200L;

	static WorkflowDialog dialog;

	private JPanel panel;

	private static JCheckBox soundness = null;
	private static JCheckBox strongSoundness = null;
	private static JCheckBox min = null;
	private static JCheckBox max = null;

	private static JLabel soundnessResult;
	private static JLabel strongSoundnessResult;
	private static JLabel soundnessResultExplanation;
	private static JLabel strongSoundnessResultExplanation;
	private static JButton soundnessResultTraceButton;
	private static TAPNNetworkTrace soundnessResultTrace = null;
	private static JLabel minResult;
	private static JButton minResultTraceButton;
	private static TAPNNetworkTrace minResultTrace = null;
	private static JLabel maxResult;
	private static JLabel soundnessVerificationStats;
	private static JLabel strongSoundnessVerificationStats;

	private static CustomJSpinner numberOfExtraTokensInNet = null;

	private ArrayList<String> errorMsgs = new ArrayList<String>();
	private ArrayList<Runnable> verificationQueue = new ArrayList<Runnable>();
	private static ArrayList<SharedPlace> unusedSharedPlaces = new ArrayList<SharedPlace>();

	private TimedPlace in;
	private TimedPlace out;

	private static boolean isSound = false;
	private static long m;
	private static int B;
	private static int min_exec;
	private static long strongSoundnessSequenceTimer;
	private static int strongSoundnessPeakMemory;
	private static Constant c = null;
	private static TimedPlace done = null;
	
	private static TimedArcPetriNetNetwork model = null;

	private enum TAWFNTypes {
		ETAWFN, MTAWFN, NOTTAWFN
	}

	TAWFNTypes netType;

	public static void showDialog() {
		
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
		
		dialog = new WorkflowDialog(CreateGui.getApp(), "Workflow Analysis",
				true);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(true);
		dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		dialog.setVisible(true);
	}

	private WorkflowDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);

		initComponents();
		setContentPane(panel);
	}

	private void initComponents() {
		panel = new JPanel(new GridBagLayout());

		/* Check if workflow net */
		netType = checkIfTAWFN();

		JPanel informationPanel = new JPanel();
		informationPanel.setBorder(BorderFactory
				.createTitledBorder("Properties"));
		informationPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		panel.add(informationPanel, gbc);

		gbc.gridwidth = 1;

		JLabel workflowTypeLabel = new JLabel();
		informationPanel.add(workflowTypeLabel, gbc);

		switch (netType) {
		case MTAWFN:
			workflowTypeLabel.setText("This net is a monotonic workflow net..");
			break;
		case ETAWFN:
			workflowTypeLabel.setText("This net is an extended workflow net.");
			break;
		case NOTTAWFN:
			StringBuilder sb = new StringBuilder();
			String sep = "<br>";
			for (String e : errorMsgs)
				sb.append(sep).append("- ").append(e);
			workflowTypeLabel
					.setText("<html>This net is not a workflow net for the following reason(s):"
							+ sb.toString() + "</html>");
			break;
		}

		if (netType != TAWFNTypes.NOTTAWFN) {
			JLabel inPlaceLabel = new JLabel(in.name() + " is the in-place.");
			gbc.gridy = 1;
			informationPanel.add(inPlaceLabel, gbc);

			JLabel outPlaceLabel = new JLabel(out.name() + " is the out-place.");
			gbc.gridy = 2;
			informationPanel.add(outPlaceLabel, gbc);

			initValidationPanel();
		}

		gbc.gridx = netType == TAWFNTypes.NOTTAWFN ? 0 : 1;
		gbc.gridy = 7;
		JButton close_button = new JButton("Close");
		close_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});

		panel.add(close_button, gbc);
	}

	private void initValidationPanel() {

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);

		JPanel soundnessPanel = new JPanel();
		soundnessPanel.setBorder(BorderFactory.createTitledBorder("Soundness"));
		soundnessPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		panel.add(soundnessPanel, gbc);

		gbc.gridwidth = 1;

		if (soundness == null)
			soundness = new JCheckBox("Check soundness.");
		soundness.setSelected(true);
		soundness.setEnabled(false);
		gbc.gridx = 0;
		gbc.gridy = 1;
		soundnessPanel.add(soundness, gbc);

		soundnessResult = new JLabel();
		gbc.gridx = 1;
		soundnessPanel.add(soundnessResult, gbc);

		soundnessResultTraceButton = new JButton("Show trace");
		gbc.gridx = 2;
		soundnessResultTraceButton.setVisible(false);
		soundnessResultTraceButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateGui.getApp().setGUIMode(GUIMode.animation);
				CreateGui.getAnimator().SetTrace(soundnessResultTrace);
				dialog.dispose();
			}
		});
		soundnessPanel.add(soundnessResultTraceButton, gbc);
		
		soundnessResultExplanation = new JLabel();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		soundnessResultExplanation.setVisible(false);
		soundnessResultExplanation.setEnabled(false);
		soundnessPanel.add(soundnessResultExplanation, gbc);

		gbc.gridwidth = 1;

		if (min == null)
			min = new JCheckBox("Calculate minimum duration.");
		gbc.gridx = 0;
		gbc.gridy = 3;
		soundnessPanel.add(min, gbc);

		minResult = new JLabel();
		gbc.gridx = 1;
		soundnessPanel.add(minResult, gbc);
		
		minResultTraceButton = new JButton("Show trace");
		gbc.gridx = 2;
		minResultTraceButton.setVisible(false);
		minResultTraceButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateGui.getApp().setGUIMode(GUIMode.animation);
				CreateGui.getAnimator().SetTrace(minResultTrace);
				dialog.dispose();
			}
		});
		soundnessPanel.add(minResultTraceButton, gbc);

		soundnessVerificationStats = new JLabel();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		soundnessVerificationStats.setVisible(false);
		soundnessPanel.add(soundnessVerificationStats, gbc);

		gbc.gridwidth = 1;

		JPanel strongSoundnessPanel = new JPanel();
		strongSoundnessPanel.setBorder(BorderFactory
				.createTitledBorder("Strong Soundness"));
		strongSoundnessPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		panel.add(strongSoundnessPanel, gbc);

		gbc.gridwidth = 1;

		if (strongSoundness == null)
			strongSoundness = new JCheckBox("Check strong soundness.");
		gbc.gridx = 0;
		gbc.gridy = 1;
		strongSoundnessPanel.add(strongSoundness, gbc);

		strongSoundnessResult = new JLabel();
		gbc.gridx = 1;
		strongSoundnessPanel.add(strongSoundnessResult, gbc);

		strongSoundnessResultExplanation = new JLabel();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		strongSoundnessResultExplanation.setVisible(false);
		strongSoundnessResultExplanation.setEnabled(false);
		strongSoundnessPanel.add(strongSoundnessResultExplanation, gbc);

		gbc.gridwidth = 1;

		if (max == null)
			max = new JCheckBox("Calculate maximum duration.");
		gbc.gridx = 0;
		gbc.gridy = 4;
		strongSoundnessPanel.add(max, gbc);

		maxResult = new JLabel();
		gbc.gridx = 1;
		strongSoundnessPanel.add(maxResult, gbc);

		strongSoundnessVerificationStats = new JLabel();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		strongSoundnessVerificationStats.setVisible(false);
		strongSoundnessPanel.add(strongSoundnessVerificationStats, gbc);

		gbc.gridwidth = 1;

		strongSoundnessPanel.setEnabled(strongSoundness.isSelected());

		strongSoundness.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				max.setEnabled(strongSoundness.isSelected());
				if (!strongSoundness.isSelected()) {
					max.setSelected(false);
				}
			}
		});

		if (netType == TAWFNTypes.ETAWFN) {
			gbc.gridx = 0;
			gbc.gridy = 5;
			panel.add(new JLabel(" Number of extra tokens:  "), gbc);

			if (numberOfExtraTokensInNet == null)
				numberOfExtraTokensInNet = new CustomJSpinner(3, 0,
						Integer.MAX_VALUE);
			numberOfExtraTokensInNet.setMaximumSize(new Dimension(55, 30));
			numberOfExtraTokensInNet.setMinimumSize(new Dimension(55, 30));
			numberOfExtraTokensInNet.setPreferredSize(new Dimension(55, 30));
			gbc.gridx = 0;
			gbc.gridy = 6;
			panel.add(numberOfExtraTokensInNet, gbc);

			JButton checkBound = new JButton("Check bound");
			gbc.gridx = 1;
			panel.add(checkBound, gbc);

			checkBound.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					checkBound();
				}
			});
		}

		JButton checkIfSound = new JButton("Check workflow soundness");
		checkIfSound.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				checkTAWFNSoundness();
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 7;
		panel.add(checkIfSound, gbc);
	}

	private TAWFNTypes checkIfTAWFN() {
		List<TimedArcPetriNet> tapns = model.activeTemplates();
		ArrayList<TimedPlace> sharedInPlaces = new ArrayList<TimedPlace>();
		ArrayList<TimedPlace> sharedOutPlaces = new ArrayList<TimedPlace>();
		ArrayList<SharedTransition> sharedTransitions = new ArrayList<SharedTransition>();
		in = null;
		out = null;
		errorMsgs = new ArrayList<String>();

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
					if (isin) {
						sharedInPlaces.add(p);
					}

					if (isout) {
						sharedOutPlaces.add(p);
					}
				} else if (isin && isout) {
					errorMsgs.add("Place " + p
							+ " has no in- or out-going arcs.");
				} else if (isin) {
					if (in == null) {
						in = p;
					} else {
						errorMsgs.add("Multiple in-places found (" + in
								+ " and " + p + ").");
					}
				} else if (isout) {
					if (out == null) {
						out = p;
					} else {
						errorMsgs.add("Multiple out-places found (" + out
								+ " and " + p + ").");
					}
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
					errorMsgs.add("Transition " + t.name()
							+ " has empty preset.");
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
			errorMsgs.add("Transition " + st.name() + " has empty preset.");
			while (sharedTransitions.remove(st)) {
			}
		}

		while (sharedInPlaces.size() != 0) {
			TimedPlace p = sharedInPlaces.get(0);
			while (sharedInPlaces.remove(p)) {
			}
			if (!sharedOutPlaces.remove(p)) {
				if (in == null) {
					in = p;
				} else {
					errorMsgs.add("Multiple in-places found (" + in + " and "
							+ p + ").");
				}
			}
			while (sharedOutPlaces.remove(p)) {
			}
		}

		if (in == null) {
			errorMsgs.add("No in-place found.");
		}

		while (sharedOutPlaces.size() > 0) {
			TimedPlace p = null;
			if (out == null) {
				p = sharedOutPlaces.get(0);
				out = p;
				while (sharedOutPlaces.remove(p)) {
				}
			} else {
				errorMsgs.add("Multiple out-places found (" + out + " and " + p
						+ ").");
			}
		}

		if (out == null) {
			errorMsgs.add("No in-place found.");
		}

		if (numberOfTokensInNet > 1 || in.tokens().size() != 1) {
			errorMsgs
					.add("The current marking is not a valid initial marking.");
		}

		if (!errorMsgs.isEmpty()) {
			return TAWFNTypes.NOTTAWFN;
		}

		return isMonotonic ? TAWFNTypes.MTAWFN : TAWFNTypes.ETAWFN;
	}

	private void checkTAWFNSoundness() {
		// Clear old results
		soundnessResult.setText("");
		soundnessResultTraceButton.setVisible(false);
		minResult.setText("");
		minResultTraceButton.setVisible(false);
		strongSoundnessResult.setText("");
		maxResult.setText("");
		soundnessResultExplanation.setVisible(false);
		soundnessVerificationStats.setVisible(false);
		strongSoundnessResultExplanation.setVisible(false);
		strongSoundnessVerificationStats.setVisible(false);

		dialog.pack();
		verificationQueue.clear();
		isSound = false;
		min_exec = -1;

		verificationQueue.add(getSoundnessRunnable());

		if (strongSoundness.isSelected()) {
			verificationQueue.add(getInitialStrongSoundnessRunnable());
		}

		// Run steps
		while (!verificationQueue.isEmpty()) {
			verificationQueue.get(0).run();
			verificationQueue.remove(0);
		}
	}

	private void setCValue(int value) {
		c.setValue(value);
	}

	private TimedArcPetriNetNetwork composeStrongSoundnessModel() {
		int i = 0;
		outer: for(TimedArcPetriNet t : model.activeTemplates()){
			for(TimedPlace p : t.places()){
				if(p.equals(out)){
					break outer;
				}
			}
			i++;
		}
		
		TimedArcPetriNetNetwork network = model.copy();
		TimedArcPetriNet out_template = network.activeTemplates().get(i);
		TimedPlace out_hook = null;
		
		for(TimedPlace p : out_template.places()){
			if(p.name().equals(out.name())){
				out_hook = p;
				break;
			}
		}
		
		// Add new components	- TODO prevent name clashing
		c = new Constant("C", (int) m*B+1); 
		network.constants().add(c);
		TimedTransition nok_t = new TimedTransition("NOK", true);
		out_template.add(nok_t);
		TimedTransition tick_t = new TimedTransition("TICK", false);
		out_template.add(tick_t);
		TimedTransition ok_t = new TimedTransition("OK", true);
		out_template.add(ok_t);
		TimedPlace timer_p = new LocalTimedPlace("TIMER", new TimeInvariant(true, new ConstantBound(c)));
		out_template.add(timer_p);
		TimedPlace ready_p = new LocalTimedPlace("READY");
		out_template.add(ready_p);
		done = new LocalTimedPlace("DONE");
		out_template.add(done);
		
		out_template.add(new TimedInputArc(out_hook, nok_t, TimeInterval.ZERO_INF));
		out_template.add(new TimedInputArc(out_hook, ok_t, TimeInterval.ZERO_INF));
		out_template.add(new TimedInputArc(timer_p, tick_t, new TimeInterval(true, new ConstantBound(c), new ConstantBound(c), true)));
		out_template.add(new TimedOutputArc(tick_t, ready_p));
		out_template.add(new TimedInputArc(ready_p, ok_t, TimeInterval.ZERO_INF));
		out_template.add(new TimedOutputArc(ok_t, done));
		
		out_template.addToken(new TimedToken(timer_p));
		
		return network;
	}

	private Runnable getInitialStrongSoundnessRunnable() {
		
		strongSoundnessSequenceTimer = new Date().getTime();
		strongSoundnessPeakMemory = 0;
		
		return new Runnable() {

			@Override
			public void run() {

				// Check preliminary conditions
				if(!isSound){
					setStrongSoundnessResult(false,"Model is not sound.");
					return;
				}
				
				B = Integer.MAX_VALUE;
				for (TimedArcPetriNet t : model
						.activeTemplates()) {
					for (TimedPlace p : t.places()) {
						if (p.invariant().upperBound().equals(Bound.Infinity)) {
							if (!p.equals(out)) {
								setStrongSoundnessResult(false,
										"Place " + p.name()
												+ " has no invariant.");
								return;
							} else {
								continue;
							}
						}
						B = Math.min(B, p.invariant().upperBound().value());
					}
				}
				B++;

				final TAPNQuery q = new TAPNQuery(
						"Workflow strong soundness initial check",
						numberOfExtraTokensInNet == null ? 0
								: (Integer) numberOfExtraTokensInNet.getValue(),
						new TCTLEFNode(new TCTLFalseNode()), TraceOption.NONE,
						SearchOption.HEURISTIC,
						ReductionOption.VerifyTAPNdiscreteVerification, true,
						false, false, null, ExtrapolationOption.AUTOMATIC,
						ModelType.TAPN, strongSoundness.isSelected(), min
								.isSelected(), max.isSelected());
				Verifier.runVerifyTAPNVerification(model, q, new VerificationCallback() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

					}

					@Override
					public void run(VerificationResult<TAPNNetworkTrace> result) {
						m = result.stats().exploredStates();
						
						verificationQueue.add(getStrongSoundnessRunnable());
					}
				});
			}
		};
	}

	private Runnable getStrongSoundnessRunnable() {
		final TimedArcPetriNetNetwork model = composeStrongSoundnessModel();		
		return new Runnable() {
			
			@Override
			public void run() {
				String template = done.isShared()? ((SharedPlace) done).getComponentsUsingThisPlace().get(0):((LocalTimedPlace) done).model().name();
				// TODO get place name correct s.t. it is mapped
				final TAPNQuery q = new TAPNQuery(
						"Workflow strong soundness checking",
						numberOfExtraTokensInNet == null ? 0
								: (Integer) numberOfExtraTokensInNet.getValue(),
						new TCTLEFNode(new TCTLAtomicPropositionNode(template, done.name(), "=", 1)), TraceOption.NONE,
						SearchOption.HEURISTIC,
						ReductionOption.VerifyTAPNdiscreteVerification, true,
						false, false, null, ExtrapolationOption.AUTOMATIC);
				Verifier.runVerifyTAPNVerification(model, q, new VerificationCallback() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

					}

					@Override
					public void run(VerificationResult<TAPNNetworkTrace> result) {
						updatePeakMemory();
						if(result.isQuerySatisfied()){
							setStrongSoundnessResult(false, null);
							
							if(max.isSelected()){
								maxResult.setText("Not defined.");
								maxResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
								maxResult.setVisible(true);
							}
						}else{
							setStrongSoundnessResult(true, null);
							if(max.isSelected()){
								setMaxResult(model, min_exec, (int) (m*B+1));
							}
						}
					}
				});
			}
		};
	}
	
	private Runnable getMaxSearchRunnable(final TimedArcPetriNetNetwork model, final int lower, final int upper){
		final int bound = (int) Math.ceil(lower+((upper-lower)/2));
		setCValue(bound);
		
		return new Runnable() {
			
			@Override
			public void run() {
				String template = done.isShared()? "":((LocalTimedPlace) done).model().name();
				// TODO get place name correct s.t. it is mapped
				final TAPNQuery q = new TAPNQuery(
						"Workflow strong soundness checking",
						numberOfExtraTokensInNet == null ? 0
								: (Integer) numberOfExtraTokensInNet.getValue(),
						new TCTLEFNode(new TCTLAtomicPropositionNode(template, done.name(), "=", 1)), TraceOption.NONE,
						SearchOption.HEURISTIC,
						ReductionOption.VerifyTAPNdiscreteVerification, true,
						false, false, null, ExtrapolationOption.AUTOMATIC);
				Verifier.runVerifyTAPNVerification(model, q, new VerificationCallback() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

					}

					@Override
					public void run(VerificationResult<TAPNNetworkTrace> result) {
						updatePeakMemory();
						if(result.isQuerySatisfied()){
							setMaxResult(model, bound, upper);
						}else{
							setMaxResult(model, lower, bound);
						}
					}
				});
			}
		};
	}
	
	private void updatePeakMemory(){
		strongSoundnessPeakMemory = Math.max(strongSoundnessPeakMemory, MemoryMonitor.getPeakMemoryValue());
	}
	
	private void setMaxResult(TimedArcPetriNetNetwork model, int lower, int upper){
		if(lower == upper-1){
			// Found max!
			maxResult.setText(lower + " time units.");
			maxResult.setForeground(Pipe.QUERY_SATISFIED_COLOR);
			strongSoundnessVerificationStats.setText("Estimated verification time: "+ ((new Date().getTime()-strongSoundnessSequenceTimer) / 1000) + "s, peak memory usage: " + strongSoundnessPeakMemory + "MB");
			strongSoundnessVerificationStats.setVisible(true);
		}else{
			maxResult.setText(lower + " <= max < "+upper);
			maxResult.setForeground(Pipe.QUERY_INCONCLUSIVE_COLOR);
			verificationQueue.add(getMaxSearchRunnable(model, lower, upper));
		}
		dialog.pack();
	}

	private void setStrongSoundnessResult(boolean satisfied, String explanation) {
		if (satisfied) {
			strongSoundnessResult.setText("The property is satisfied.");
			strongSoundnessResult.setForeground(Pipe.QUERY_SATISFIED_COLOR);
		} else {
			strongSoundnessResult.setText("The property is NOT satisfied.");
			strongSoundnessResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
			if(max.isSelected()){
				maxResult.setText("Not available.");
				maxResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
			}
		}

		if (explanation != null) {
			strongSoundnessResultExplanation.setText(explanation);
			strongSoundnessResultExplanation.setVisible(true);
		}
		
		if(!max.isSelected() || !satisfied){
			strongSoundnessVerificationStats.setText("Estimated verification time: "+ ((new Date().getTime()-strongSoundnessSequenceTimer) / 1000) + "s, peak memory usage: " + strongSoundnessPeakMemory + "MB");
			strongSoundnessVerificationStats.setVisible(true);
		}
		dialog.pack();
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
						SearchOption.HEURISTIC,
						ReductionOption.VerifyTAPNdiscreteVerification, true,
						false, false, null, ExtrapolationOption.AUTOMATIC,
						ModelType.TAWFN, strongSoundness.isSelected(), min
								.isSelected(), max.isSelected());
				Verifier.runVerifyTAPNVerification(model, q, new VerificationCallback() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

					}

					@Override
					public void run(VerificationResult<TAPNNetworkTrace> result) {
						if (result.isQuerySatisfied()) {
							soundnessResult
									.setText("The property is satisfied.");
							soundnessResult
									.setForeground(Pipe.QUERY_SATISFIED_COLOR);
							isSound = true;
							min_exec = result.stats().minimumExecutionTime();
						} else if (!result.isBounded()) {
							soundnessResult
									.setText("The search was inconclusive.");
							soundnessResult
									.setForeground(Pipe.QUERY_INCONCLUSIVE_COLOR);
							
							soundnessResultExplanation.setText("Try to increase the number of extra tokens.");
							soundnessResultExplanation.setVisible(true);
						} else {
							soundnessResult
									.setText("The property is NOT satisfied.");
							soundnessResult
									.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
							soundnessResultTrace = mapTraceToRealModel(result.getTrace());
							soundnessResultTraceButton.setVisible(true);
						}

						if (q.findMin) {
							if(result.isQuerySatisfied()){
								minResult.setText(result.stats()
											.minimumExecutionTime()
											+ " time units.");
								minResult.setForeground(Pipe.QUERY_SATISFIED_COLOR);
								minResultTrace = mapTraceToRealModel(result.getTrace());
								minResultTraceButton.setVisible(true);
							}else{
								minResult.setText("Not available.");
								minResult.setForeground(Pipe.QUERY_NOT_SATISFIED_COLOR);
							}
						}

						soundnessVerificationStats.setText(result
								.getVerificationTimeString()
								+ ", peak memory usage: "
								+ MemoryMonitor.getPeakMemory());
						soundnessVerificationStats.setVisible(true);

						dialog.pack();
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