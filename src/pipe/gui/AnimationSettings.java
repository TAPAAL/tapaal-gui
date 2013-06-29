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
import sun.security.jca.GetInstance.Instance;

public class AnimationSettings extends JPanel {

	
	private static AnimationSettings instance;
	
	public static AnimationSettings getInstance() {
		if(instance == null){
			instance = new AnimationSettings();
		}
		return instance;
	}
	
	private AnimationSettings(){
		super(new BorderLayout());
		BlueTransitionControl blue = BlueTransitionControl.getInstance();
		SimulationControl simControl = SimulationControl.getInstance();
		
		add(blue, BorderLayout.NORTH);
		add(simControl, BorderLayout.SOUTH);
	}
	
	private static JDialog dialog;
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
		contentPane.add(getInstance(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(3, 3, 0, 3);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add(closeDialogButton, gbc);
		
		dialog = new EscapableDialog(CreateGui.getApp(), "Simulation controls", true);
		dialog.setContentPane(contentPane);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(CreateGui.getApp());
		dialog.setVisible(true);
	}

}
