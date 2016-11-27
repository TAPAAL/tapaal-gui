package dk.aau.cs.gui.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import pipe.dataLayer.Template;
import pipe.gui.AnimationSettings;
import pipe.gui.DelayEnabledTransitionControl;
import pipe.gui.CreateGui;
import pipe.gui.SimulationControl;
import pipe.gui.graphicElements.Transition;

public class TransitionFireingComponent extends JPanel {
	private static final long serialVersionUID = -1208007964368671066L;

	private EnabledTransitionsList enabledTransitionsList;
	private JButton fireButton;
	private JButton settingsButton;

	public TransitionFireingComponent(boolean showDelayEnabledTransitions) {
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

		settingsButton = new JButton("Settings");
		settingsButton.setPreferredSize(new Dimension(0, settingsButton.getPreferredSize().height)); //Make the two buttons equal in size
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnimationSettings.showAnimationSettings();
			}
		});

		fireButton = new JButton("Delay & Fire");
		fireButton.setPreferredSize(new Dimension(0, fireButton.getPreferredSize().height)); //Make the two buttons equal in size
		fireButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(SimulationControl.getInstance().randomSimulation() && CreateGui.getApp().isShowingDelayEnabledTransitions()){
					SimulationControl.startSimulation();
				} else {
					fireSelectedTransition();
				}
			}
		});
		fireButton.addKeyListener(new KeyAdapter() {			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					if(SimulationControl.getInstance().randomSimulation()){
						SimulationControl.startSimulation();
					} else {
						fireSelectedTransition();
					}
				}
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
		this.add(settingsButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		this.add(fireButton, gbc);

		showDelayEnabledTransitions(showDelayEnabledTransitions);
	}

	public static final String FIRE_BUTTON_DEACTIVATED_TOOL_TIP = "No transitions are enabled";
	public static final String FIRE_BUTTON_ENABLED_TOOL_TIP = "Press to fire the selected transition";
	public static final String SIMULATE_DEACTIVATED_TOOL_TIP = "Not able to simulate from this marking, no transitions are enabled";
	public static final String SIMULATE_ACTIVATED_TOOL_TIP = "Do a random simulation of the net";

	public void updateFireButton(){
		//If the simulation is running deactivate the firebutton.
		if(SimulationControl.getInstance().isRunning()){
			fireButton.setEnabled(false);
			return;
		}
		
		//Make sure the firebutton is enabled
		fireButton.setEnabled(true);
		
		//If random simulation is enabled
		if(CreateGui.getApp().isShowingDelayEnabledTransitions() && SimulationControl.getInstance().randomSimulation()){
			fireButton.setText("Simulate");
			
			if(enabledTransitionsList.getNumberOfTransitions() == 0){
				fireButton.setEnabled(false);
				fireButton.setToolTipText(SIMULATE_DEACTIVATED_TOOL_TIP);
			} else {
				fireButton.setEnabled(true);
				fireButton.setToolTipText(SIMULATE_ACTIVATED_TOOL_TIP);
			}
		} else { //If random simulation is not enabled.
			fireButton.setText(CreateGui.getApp().isShowingDelayEnabledTransitions() ? "Delay & Fire" : "Fire");

			if(enabledTransitionsList.getNumberOfTransitions() == 0){
				fireButton.setEnabled(false);
				fireButton.setToolTipText(FIRE_BUTTON_DEACTIVATED_TOOL_TIP);
			} else {
				fireButton.setEnabled(true);
				fireButton.setToolTipText(FIRE_BUTTON_ENABLED_TOOL_TIP);
			}
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

	public DelayEnabledTransitionControl getDelayEnabledTransitionControl() {
		return DelayEnabledTransitionControl.getInstance();
	}

	public void showDelayEnabledTransitions(boolean enable) {
		settingsButton.setVisible(enable);
		updateFireButton();
	}
	
	public void fireSelectedTransition(){
		enabledTransitionsList.fireSelectedTransition();
	}
}
