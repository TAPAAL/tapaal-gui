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
import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Tuple;

public class WorkflowDialog extends JDialog{

	private static final long serialVersionUID = 5613743579411748200L;
	
	static WorkflowDialog dialog;

	private JPanel panel;
	
	private JComboBox inplace;
	private JComboBox outplace;
	
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
		
		Tuple<String[], String[]> place_options = getInOutPlaces();
		
		inplace = new JComboBox(place_options.value1());
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 0, 0, 5);
		panel.add(inplace, gbc);
		
		int i = 0;
		int selectedIndex = 0;
		for(String p : place_options.value1()){
			if(p.toLowerCase().equals("in") || p.toLowerCase().endsWith(".in")){
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
		for(String p : place_options.value2()){
			if(p.toLowerCase().equals("out") || p.toLowerCase().endsWith(".out")){
				selectedIndex = i;
				break;
			}
			i++;
		}
		
		if(selectedIndex > 0){
			outplace.setSelectedIndex(selectedIndex);
		}
		
		/* Check if workflow net */
		JButton checkIfWorkflow = new JButton("Check if net is TAWFN");
		checkIfWorkflow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO check if TAWFN
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(checkIfWorkflow, gbc);
	}
	
	private Tuple<String[], String[]> getInOutPlaces(){
		ArrayList<String> in_place_options = new ArrayList<String>();
		ArrayList<String> out_place_options = new ArrayList<String>();
		
		boolean useTemplatePrefix = CreateGui.getCurrentTab().network().activeTemplates().size() > 1;
		
		for(TimedArcPetriNet temp : CreateGui.getCurrentTab().network().activeTemplates()){
			String prefix = useTemplatePrefix? temp.name() + "." : "";
			for(TimedPlace p : temp.places()){
				in_place_options.add(prefix + p.name());
				out_place_options.add(prefix + p.name());
			}
		}
		
		String[] in_place_array = new String[in_place_options.size()];
		String[] out_place_array = new String[in_place_options.size()];
		
		int i = 0;
		for(String s : in_place_options)	in_place_array[i++] = s;
		i = 0;
		for(String s : out_place_options)	out_place_array[i++] = s;

		return new Tuple<String[], String[]>(in_place_array, out_place_array);
	}
}
