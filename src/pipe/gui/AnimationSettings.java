package pipe.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import pipe.gui.widgets.EscapableDialog;

public class AnimationSettings{

	private static JDialog dialog;
	private static DelayEnabledTransitionControl delayEnabled;
	private static SimulationControl simControl;
	
	private static JPanel getContent(){
		JPanel content = new JPanel(new BorderLayout());
		 
		delayEnabled = DelayEnabledTransitionControl.getInstance();
		simControl = SimulationControl.getInstance();
		simControl.showCheckbox(true);
		
		simControl.addRandomSimulationActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(simControl.randomSimulation()){
					delayEnabled.randomMode.setSelected(true);
				}
				CreateGui.getCurrentTab().getTransitionFireingComponent().updateFireButton();
			}
		});
		
		content.add(delayEnabled, BorderLayout.NORTH);
		content.add(simControl, BorderLayout.SOUTH);
		return content;
	}
	
	
	public static void showAnimationSettings(){
		JPanel contentPane = new JPanel(new GridBagLayout());
		
		JButton closeDialogButton = new JButton("Close");
		closeDialogButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 3, 0, 3);
		gbc.fill = GridBagConstraints.BOTH;
		contentPane.add(getContent(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(3, 3, 0, 3);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add(closeDialogButton, gbc);
		
		dialog = new EscapableDialog(CreateGui.getApp(), "Settings", true);
		dialog.getRootPane().setDefaultButton(closeDialogButton);
		dialog.setContentPane(contentPane);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(CreateGui.getApp());
		dialog.setVisible(true);
	}

}
