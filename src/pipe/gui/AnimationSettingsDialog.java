package pipe.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import dk.aau.cs.gui.TabContent;
import pipe.gui.widgets.EscapableDialog;

public class AnimationSettingsDialog {

	private static JDialog dialog;
	private static DelayEnabledTransitionControl delayEnabled;
	private static SimulationControl simControl;

	private static JPanel getContent(TabContent.TAPNLens lens){
		JPanel content = new JPanel(new BorderLayout());
		 
		delayEnabled = DelayEnabledTransitionControl.getInstance();
		simControl = SimulationControl.getInstance();
		simControl.showCheckbox(true);
		
		simControl.addRandomSimulationActionListener(e -> {
			if(simControl.randomSimulation()){
				simControl.randomMode.setSelected(true);
			}
			CreateGui.getCurrentTab().getTransitionFiringComponent().updateFireButton();
		});
		
		content.add(delayEnabled, BorderLayout.NORTH);
		content.add(simControl, BorderLayout.SOUTH);
		hideTimedInformation(lens);
		return content;
	}

    private static void hideTimedInformation(TabContent.TAPNLens lens){
	    delayEnabled.setVisible(lens.isTimed());
    }
	
	public static void showAnimationSettings(TabContent.TAPNLens lens){
		JPanel contentPane = new JPanel(new GridBagLayout());
		
		JButton closeDialogButton = new JButton("Close");
		closeDialogButton.addActionListener(o -> dialog.setVisible(false));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 3, 0, 3);
		gbc.fill = GridBagConstraints.BOTH;
		contentPane.add(getContent(lens), gbc);
		
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
