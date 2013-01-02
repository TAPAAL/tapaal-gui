package dk.aau.cs.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import pipe.dataLayer.Template;
import pipe.gui.BlueTransitionControl;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Transition;

public class TransitionFireingComponent extends JPanel {
	private static final long serialVersionUID = -1208007964368671066L;

	private EnabledTransitionsList enabledTransitionsList;
	private JButton fireButton;
	private JButton settingsButton;
	
	public TransitionFireingComponent() {
		super(new GridBagLayout());
		
		enabledTransitionsList = new EnabledTransitionsList();
		
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Enabled Transitions"),
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		this
		.setToolTipText("List of currently enabled transitions (double click a transition to fire it)");
		enabledTransitionsList.setPreferredSize(new Dimension(
				enabledTransitionsList.getPreferredSize().width,
				enabledTransitionsList.getMinimumSize().height));
		
		fireButton = new JButton("Fire");
		fireButton.setPreferredSize(new Dimension(0, fireButton.getPreferredSize().height)); //Make the two buttons equal in size
		fireButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enabledTransitionsList.fireSelectedTransition();
			}
		});
		
		settingsButton = new JButton("Settings");
		settingsButton.setPreferredSize(new Dimension(0, settingsButton.getPreferredSize().height)); //Make the two buttons equal in size
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BlueTransitionControl.showBlueTransitionDialog();
			}
		});
		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		this.add(enabledTransitionsList, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		this.add(fireButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		this.add(settingsButton, gbc);
	}
	
	public static final String FIRE_BUTTON_DEACTIVATED_TOOL_TIP = "No transitions are enabled";
	public static final String FIRE_BUTTON_ENABLED_TOOL_TIP = "Press to fire the selected transition";
	public void updateFireButton(){
		if(enabledTransitionsList.getNumberOfTransitions() == 0){
			fireButton.setEnabled(false);
			fireButton.setToolTipText(FIRE_BUTTON_DEACTIVATED_TOOL_TIP);
		} else {
			fireButton.setEnabled(true);
			fireButton.setToolTipText(FIRE_BUTTON_ENABLED_TOOL_TIP);
		}
	}
	
	public void addTransition(Template template, Transition transition){
		enabledTransitionsList.addTransition(template, transition);
	}
	
	public void startReInit(){
		enabledTransitionsList.startReInit();
	}
	
	public void reInitDone(){
		updateFireButton();
		enabledTransitionsList.reInitDone();
	}
	
	public BlueTransitionControl getBlueTransitionControl() {
		return BlueTransitionControl.getInstance();
	}

	public void showBlueTransitions(boolean enable) {
		settingsButton.setVisible(enable);
	}
}
