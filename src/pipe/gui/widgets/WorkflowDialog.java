package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMessages;

import pipe.gui.*;
import pipe.dataLayer.*;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.ModelType;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.VerificationResult;

public class WorkflowDialog extends JDialog{

	private static final long serialVersionUID = 5613743579411748200L;

	static WorkflowDialog dialog;

	private JPanel panel;

	private static JCheckBox soundness = null;
	private static JCheckBox strongSoundness = null;
	private static JCheckBox min = null;
	private static JCheckBox max = null;
	
	private static JLabel soundnessResult;
	private static JLabel strongSoundnessResult;
	private static JLabel minResult;
	private static JLabel maxResult;
	
	private static CustomJSpinner numberOfExtraTokensInNet = null;
	
	private ArrayList<String> errorMsgs = new ArrayList<String>();

	private TimedPlace in;
	private TimedPlace out;

	private enum TAWFNTypes{
		ETAWFN, MTAWFN, NOTTAWFN
	}

	TAWFNTypes netType;

	public static void showDialog(){
		dialog = new WorkflowDialog(CreateGui.getApp(), "Workflow Analysis", true);
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
		informationPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
		informationPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		panel.add(informationPanel, gbc);

		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 0, 0);

		JLabel workflowTypeLabel = new JLabel();
		informationPanel.add(workflowTypeLabel, gbc);

		switch(netType){
		case MTAWFN:
			workflowTypeLabel.setText("This net is a MTAWFN");
			break;
		case ETAWFN:
			workflowTypeLabel.setText("This net is an ETAWFN");
			break;
		case NOTTAWFN:
			StringBuilder sb = new StringBuilder();
			String sep = "<br>";
			for (String e : errorMsgs) sb.append(sep).append("- ").append(e);
			workflowTypeLabel.setText("<html>This net is not a TAWFN for the following reason(s):"+sb.toString()+"</html>");
			break;
		}

		if(netType != TAWFNTypes.NOTTAWFN){
			JLabel inPlaceLabel = new JLabel("In-place: "+in.name());
			gbc.gridy = 1;
			informationPanel.add(inPlaceLabel, gbc);
			
			JLabel outPlaceLabel = new JLabel("Out-place: "+out.name());
			gbc.gridy = 2;
			informationPanel.add(outPlaceLabel, gbc);
			
			initValidationPanel();
		}
		
		gbc.gridx = netType == TAWFNTypes.NOTTAWFN? 0:1;
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

	private void initValidationPanel(){

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		
		JPanel soundnessPanel = new JPanel();
		soundnessPanel.setBorder(BorderFactory.createTitledBorder("Soundness"));
		soundnessPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 1;	
		gbc.gridwidth = 2;
		panel.add(soundnessPanel, gbc);
		
		gbc.gridwidth = 1;

		if(soundness == null)	soundness = new JCheckBox("Check soundness.");
		soundness.setSelected(true);
		soundness.setEnabled(false);
		gbc.gridx = 0;
		gbc.gridy = 1;	
		soundnessPanel.add(soundness, gbc);
		
		soundnessResult = new JLabel();
		gbc.gridx = 1;
		soundnessPanel.add(soundnessResult, gbc);

		if(min == null)	min = new JCheckBox("Calculate minimum duration.");
		gbc.gridx = 0;
		gbc.gridy = 2;	
		soundnessPanel.add(min, gbc);
		
		minResult = new JLabel();
		gbc.gridx = 1;
		soundnessPanel.add(minResult, gbc);
		
		JPanel strongSoundnessPanel = new JPanel();
		strongSoundnessPanel.setBorder(BorderFactory.createTitledBorder("Strong Soundness"));
		strongSoundnessPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 2;	
		gbc.gridwidth = 2;
		panel.add(strongSoundnessPanel, gbc);
		
		gbc.gridwidth = 1;
		
		if(strongSoundness == null)	strongSoundness = new JCheckBox("Check strong soundness.");
		gbc.gridx = 0;
		gbc.gridy = 3;	
		strongSoundnessPanel.add(strongSoundness, gbc);
		
		strongSoundnessResult = new JLabel();
		gbc.gridx = 1;
		strongSoundnessPanel.add(strongSoundnessResult, gbc);

		if(max == null)	max = new JCheckBox("Calculate maximum duration.");
		gbc.gridx = 0;
		gbc.gridy = 4;	
		strongSoundnessPanel.add(max, gbc);
		
		maxResult = new JLabel();
		gbc.gridx = 1;
		panel.add(maxResult, gbc);
		
		strongSoundnessPanel.setEnabled(strongSoundness.isSelected());
		
		strongSoundness.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				max.setEnabled(strongSoundness.isSelected());
				if(!strongSoundness.isSelected()){
					max.setSelected(false);
				}
			}
		});
		
		if(netType == TAWFNTypes.ETAWFN){
			gbc.gridx = 0;
			gbc.gridy = 5;
			panel.add(new JLabel(" Number of extra tokens:  "), gbc);
			
			if(numberOfExtraTokensInNet == null)	numberOfExtraTokensInNet = new CustomJSpinner(3, 0, Integer.MAX_VALUE);	
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
				checkTAWFNSoundness(in, out);
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 7;
		panel.add(checkIfSound, gbc);
	}

	private TAWFNTypes checkIfTAWFN(){
		List<TimedArcPetriNet> tapns = CreateGui.getCurrentTab().network().activeTemplates();
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

		for(TimedArcPetriNet tapn: tapns){ 		
			for(TimedPlace p : tapn.places()){
				isin = true;
				isout = true;

				p.invariant().asIterval();
				if(isMonotonic && !p.invariant().asIterval().equals(TimeInterval.ZERO_INF)){
					isMonotonic = false;
				}

				// Test for arcs going in to place
				for(TimedOutputArc arc: tapn.outputArcs()){
					if(arc.destination().equals(p)){
						isin = false;
						break;
					}
				}

				//Test for arcs going out from place
				for(TimedInputArc arc: tapn.inputArcs()){
					if(arc.source().equals(p)){
						isout = false;
						break;
					}
				}

				// Transport arcs
				for(TransportArc arc: tapn.transportArcs()){
					if(arc.destination().equals(p)){
						isin = false;
					}
					if(arc.source().equals(p)){
						isout = false;
					}
					if(!isin && !isout)	break;
				}



				if(p.isShared()){
					if(isin){
						sharedInPlaces.add(p);
					}

					if(isout){
						sharedOutPlaces.add(p);
					}
				}else if(isin && isout){
					errorMsgs.add("Place " +p+ " has no in- or out-going arcs.");
				}else if(isin){
					if(in == null){
						in = p;
					}else{
						errorMsgs.add("Multiple in-places found ("+in+" and "+p+").");
					}
				}else if(isout){
					if(out == null){
						out = p;
					}else{
						errorMsgs.add("Multiple out-places found ("+out+" and "+ p +").");
					}
				}

				if(p.isShared() && !countedSharedPlaces.contains(p)){
					numberOfTokensInNet += p.numberOfTokens();
					countedSharedPlaces.add(p);
				}else if(!p.isShared()){
					numberOfTokensInNet += p.numberOfTokens();
				}
			}

			for(TimedTransition t : tapn.transitions()){
				if(t.isShared()){
					sharedTransitions.add(t.sharedTransition());
				}else if(t.getInputArcs().isEmpty() && t.getTransportArcsGoingThrough().isEmpty()){
					errorMsgs.add("Transition "+t.name()+" has empty preset.");
				}

				if(isMonotonic && (t.isUrgent() || !t.getInhibitorArcs().isEmpty())){
					isMonotonic = false;
				}
			}
		}

		outer: while(sharedTransitions.size() > 0){
			SharedTransition st = sharedTransitions.get(0);
			for(TimedTransition t : st.transitions()){
				if(!t.getTransportArcsGoingThrough().isEmpty() || !t.getInputArcs().isEmpty()){
					while(sharedTransitions.remove(st)){}
					continue outer;
				}
			}
			errorMsgs.add("Transition "+st.name()+" has empty preset.");
		}
		
		if(!sharedTransitions.isEmpty()){
			errorMsgs.add("Transition "+sharedTransitions.get(0).name()+" has empty preset.");
		}
		
		while(sharedInPlaces.size()!=0){
			TimedPlace p = sharedInPlaces.get(0);
			while(sharedInPlaces.remove(p)){}
			if(!sharedOutPlaces.remove(p)){
				if(in == null){
					in = p;
				}else{
					errorMsgs.add("Multiple in-places found ("+in+" and "+p+").");
				}
			}
			while(sharedOutPlaces.remove(p)){}
		}

		if(in == null){
			errorMsgs.add("No in-place found.");
		}

		while(sharedOutPlaces.size() > 0){
			TimedPlace p = null;
			if(out == null){
				p = sharedOutPlaces.get(0);
				out = p;
				while(sharedOutPlaces.remove(p)){}
			}else{
				errorMsgs.add("Multiple out-places found ("+out+" and "+ p +").");
			}
		}

		if(out == null){
			errorMsgs.add("No in-place found.");
		}

		if(numberOfTokensInNet > 1 || in.tokens().size() != 1){
			errorMsgs.add("The current marking is not a valid initial marking.");
		}

		if(!errorMsgs.isEmpty()){
			return TAWFNTypes.NOTTAWFN;
		}

		return isMonotonic ? TAWFNTypes.MTAWFN : TAWFNTypes.ETAWFN;
	}

	private void checkTAWFNSoundness(TimedPlace in, TimedPlace out){
		final TAPNQuery q = new TAPNQuery("Workflow soundness checking", numberOfExtraTokensInNet == null? 0 : (Integer) numberOfExtraTokensInNet.getValue(), new TCTLEFNode(new TCTLTrueNode()), TraceOption.NONE, SearchOption.HEURISTIC, ReductionOption.VerifyTAPNdiscreteVerification, true, false, false, null, ExtrapolationOption.AUTOMATIC, ModelType.TAWFN, strongSoundness.isSelected(), min.isSelected(), max.isSelected());
		Verifier.runVerifyTAPNVerification(CreateGui.getCurrentTab().network(), q, new VerificationCallback() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void run(VerificationResult<TAPNNetworkTrace> result) {
				if(result.isQuerySatisfied()){
					soundnessResult.setText("True");
					soundnessResult.setForeground(Color.green);
				}else if(!result.isBounded()){
					soundnessResult.setText("Inconclusive");
					soundnessResult.setForeground(Color.yellow);
				}else{
					soundnessResult.setText("False");
					soundnessResult.setForeground(Color.red);
				}

				if(q.findMin && result.isQuerySatisfied()){
					if(result.stats().minimumExecutionTime() >= 0){
						minResult.setText(result.stats().minimumExecutionTime() + "");
						minResult.setForeground(Color.green);
					}else{
						minResult.setText("ERROR!");
						minResult.setForeground(Color.red);
					}
				}
				dialog.pack();
			}
		});
		
	}
	
	private void checkBound(){
		Verifier.analyzeKBound(CreateGui.getCurrentTab().network(), (Integer) numberOfExtraTokensInNet.getValue(), numberOfExtraTokensInNet);
	}
}