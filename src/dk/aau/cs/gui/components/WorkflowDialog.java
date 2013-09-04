package dk.aau.cs.gui.components;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pipe.gui.*;
import pipe.dataLayer.*;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.ModelType;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.QueryType;

public class WorkflowDialog extends JDialog{

	private static final long serialVersionUID = 5613743579411748200L;
	
	static WorkflowDialog dialog;

	private JPanel panel;
	
	private JButton checkIfWorkflow;
	private TimedPlace in;
	private TimedPlace out;
	
	public static void showDialog(){
		dialog = new WorkflowDialog(CreateGui.getApp(), "Workflow Analysis", true);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(true);
		dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
		checkIfWorkflow = new JButton("Analyze net");
		checkIfWorkflow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				checkIfWorkflow.setEnabled(false);
				try{
					checkIfTAWFN();
					GridBagConstraints gbc = new GridBagConstraints();
					JLabel isWorkflowLabel = new JLabel("The net is a TAWFN!");
					gbc.gridx = 0;
					gbc.gridy = 2;
					gbc.gridwidth = 2;
					gbc.insets = new Insets(5, 0, 5, 5);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					panel.add(isWorkflowLabel, gbc);
					
					/* Check if workflow net is sound */
					JButton checkIfSound = new JButton("Check if TAWFN is sound");
					checkIfSound.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							checkTAWFNSoundness(in, out);
						}
					});
					gbc.gridx = 0;
					gbc.gridy = 3;
					gbc.gridwidth = 2;
					gbc.insets = new Insets(0, 0, 5, 0);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					panel.add(checkIfSound, gbc);
					
					panel.remove(checkIfWorkflow);
					dialog.pack();
				}catch(Exception ee){
					JOptionPane.showMessageDialog(CreateGui.getApp(), ee.getMessage(), "Net is not TAWFN", JOptionPane.ERROR_MESSAGE);
				}
			}
		});		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(checkIfWorkflow, gbc);
	}
	
	private boolean checkIfTAWFN() throws Exception{
		List<TimedArcPetriNet> tapns = CreateGui.getCurrentTab().network().activeTemplates();
		ArrayList<TimedPlace> sharedInPlaces = new ArrayList<TimedPlace>();	
		ArrayList<TimedPlace> sharedOutPlaces = new ArrayList<TimedPlace>();
		in = null;
		out = null;
		
		boolean isin;
		boolean isout;
		
		for(TimedArcPetriNet tapn: tapns){ 
			
			/*if(!tapn.inputArcs().iterator().hasNext() && !tapn.outputArcs().iterator().hasNext()
					&& !tapn.inhibitorArcs().iterator().hasNext() && !tapn.transportArcs().iterator().hasNext()){ // A net without arcs
				return false;
			}*/
			
			
			
			for(TimedPlace p : tapn.places()){
				isin = true;
				isout = true;
				
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
				
				// TODO inhibitor arcs in transport places?
				
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
					throw new Exception("Model contains place with no in- or out-going arcs.");
				}else if(isin){
					if(in == null){
						in = p;
					}else{
						throw new Exception("Multiple in-places found.");
					}
				}else if(isout){
					if(out == null){
						out = p;
					}else{
						throw new Exception("Multiple out-places found.");
					}
				}
			}
		}
		
		while(sharedInPlaces.size()!=0){
			TimedPlace p = sharedInPlaces.get(0);
			while(sharedInPlaces.remove(p)){}
			if(!sharedOutPlaces.remove(p)){
				if(in == null){
					in = p;
				}else{
					throw new Exception("Multiple in-places found.");
				}
			}
			while(sharedOutPlaces.remove(p)){}
		}
		
		if(in == null){
			throw new Exception("No in-place found.");
		}
		
		while(sharedOutPlaces.size() > 0){
			if(out == null){
				TimedPlace p = sharedOutPlaces.get(0);
				out = p;
				while(sharedOutPlaces.remove(p)){}
			}else{
				throw new Exception("Multiple out-places found.");
			}
		}
		
		if(out == null){
			throw new Exception("No in-place found.");
		}
		
		return true;
	}
	
	private void checkTAWFNSoundness(TimedPlace in, TimedPlace out){
		TAPNQuery q = new TAPNQuery("Workflow soundness checking", 10, new TCTLEFNode(new TCTLTrueNode()), TraceOption.NONE, SearchOption.HEURISTIC, ReductionOption.VerifyTAPNdiscreteVerification, true, false, false, null, ExtrapolationOption.AUTOMATIC, ModelType.TAWFN);
		Verifier.runVerifyTAPNVerification(CreateGui.getCurrentTab().network(), q);
	}
}