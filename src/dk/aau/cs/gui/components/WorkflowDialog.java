package dk.aau.cs.gui.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import pipe.gui.*;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.undo.DeleteTimedTransitionCommand;
import pipe.gui.undo.UndoManager;
import pipe.dataLayer.*;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.TCTLWORKFLOWSOUNDNESSNode;
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Tuple;

public class WorkflowDialog extends JDialog{

	private static final long serialVersionUID = 5613743579411748200L;
	
	static WorkflowDialog dialog;

	private JPanel panel;
	
	private JComboBox inplace;
	private JComboBox outplace;
	
	private JButton checkIfWorkflow;
	
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
		
		/* In place */
		JLabel inplaceLabel = new JLabel("Select one place as IN-place:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(inplaceLabel, gbc);
		
		Tuple<TimedPlace[], TimedPlace[]> place_options = getInOutPlaces();
		
		inplace = new JComboBox(place_options.value1());
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 0, 0, 5);
		panel.add(inplace, gbc);
		
		int i = 0;
		int selectedIndex = 0;
		for(TimedPlace p : place_options.value1()){
			if(p.name().toLowerCase().equals("in")){
				selectedIndex = i;
				break;
			}
			i++;
		}
		
		if(selectedIndex > 0){
			inplace.setSelectedIndex(selectedIndex);
		}
		
		/* Out place */
		JLabel outplaceLabel = new JLabel("Select one place as OUT-place:");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel.add(outplaceLabel, gbc);
		
		outplace = new JComboBox(place_options.value2());
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 0, 5);
		panel.add(outplace, gbc);
		
		i = 0;
		selectedIndex = 0;
		for(TimedPlace p : place_options.value2()){
			if(p.name().toLowerCase().equals("out")){
				selectedIndex = i;
				break;
			}
			i++;
		}
		
		if(selectedIndex > 0){
			outplace.setSelectedIndex(selectedIndex);
		}
		
		/* Check if workflow net */
		checkIfWorkflow = new JButton("Check if net is TAWFN");
		checkIfWorkflow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final TimedPlace in = (TimedPlace) inplace.getSelectedItem();
				final TimedPlace out = (TimedPlace) outplace.getSelectedItem();
				
				inplace.setEnabled(false);
				outplace.setEnabled(false);
				
				if(checkIfTAWFN()){				
					GridBagConstraints gbc = new GridBagConstraints();
					JLabel isWorkflowLabel = new JLabel("This is a TAWFN!");
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
				}else{
					inplace.setEnabled(true);
					outplace.setEnabled(true);
					
					JOptionPane.showMessageDialog(CreateGui.getApp(), "The net is not a TAWFN with the selected IN and OUT places.", "Net is not TAWFN", JOptionPane.ERROR_MESSAGE);
				}
			}
		});		
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(checkIfWorkflow, gbc);
	}
	
	private Tuple<TimedPlace[], TimedPlace[]> getInOutPlaces(){
		ArrayList<TimedPlace> in_place_options = new ArrayList<TimedPlace>();
		ArrayList<TimedPlace> out_place_options = new ArrayList<TimedPlace>();
		
		boolean useTemplatePrefix = CreateGui.getCurrentTab().network().activeTemplates().size() > 1;
		
		for(TimedArcPetriNet temp : CreateGui.getCurrentTab().network().activeTemplates()){
			String prefix = useTemplatePrefix? temp.name() + "." : "";
			for(TimedPlace p : temp.places()){
				in_place_options.add(p);
				out_place_options.add(p);
				p.setInPlace(false);
				p.setOutPlace(false);
			}
		}
		
		TimedPlace[] in_place_array = new TimedPlace[in_place_options.size()];
		TimedPlace[] out_place_array = new TimedPlace[in_place_options.size()];
		
		int i = 0;
		for(TimedPlace s : in_place_options)	in_place_array[i++] = s;
		i = 0;
		for(TimedPlace s : out_place_options)	out_place_array[i++] = s;

		return new Tuple<TimedPlace[], TimedPlace[]>(in_place_array, out_place_array);
	}
	
	private boolean checkIfTAWFN(){
		List<TimedArcPetriNet> tapns=CreateGui.getCurrentTab().network().activeTemplates();
		List<TimedPlace> outplaces=new ArrayList<TimedPlace>();	
		List<TimedPlace> inplaces=new ArrayList<TimedPlace>();

		for(TimedArcPetriNet tapn: tapns){ 

			if(!tapn.inputArcs().iterator().hasNext() && !tapn.outputArcs().iterator().hasNext()
					&& !tapn.inhibitorArcs().iterator().hasNext() && !tapn.transportArcs().iterator().hasNext()){ // A net without arcs
				return false;
			}
			inplaces.addAll(tapn.places());
			outplaces.addAll(tapn.places());
			for(TimedOutputArc toa: tapn.outputArcs()){
				inplaces.remove(toa.destination());
			}
			for(TimedInputArc tia: tapn.inputArcs()){
				outplaces.remove(tia.source());
			}
			for(TransportArc trpa: tapn.transportArcs()){
				inplaces.remove(trpa.destination());
				outplaces.remove(trpa.source());
			}
			for(TimedInhibitorArc ia: tapn.inhibitorArcs()){
				outplaces.remove(ia.source());
			}

		}

		if(outplaces.size()!=1 || inplaces.size()!=1){ // there are zero or more than one input places
			return false;
		}
		
		return true;
	}
	
	private void checkTAWFNSoundness(TimedPlace in, TimedPlace out){
		TAPNQuery q = new TAPNQuery("Workflow soundness checking", 10, new TCTLWORKFLOWSOUNDNESSNode(new TCTLTrueNode()), TraceOption.NONE, SearchOption.HEURISTIC, ReductionOption.VerifyTAPNdiscreteVerification, true, false, false, null, ExtrapolationOption.AUTOMATIC);
		in.setInPlace(true);
		out.setOutPlace(true);
		Verifier.runVerifyTAPNVerification(CreateGui.getCurrentTab().network(), q);
	}
	
	private TimedPlace stringToPlace(String place){
		Iterator<Template> i = CreateGui.getCurrentTab().activeTemplates().iterator();
		TimedArcPetriNet net = null;
		while(i.hasNext()){
			net = i.next().model();
			if(!place.contains(".") || place.startsWith(net.name() + ".")){
				break;
			}
		}
			
		TimedPlace p = net.getPlaceByName(place.contains(".")? place.substring(place.indexOf(".")+1):place);
		return p;
	}
}
