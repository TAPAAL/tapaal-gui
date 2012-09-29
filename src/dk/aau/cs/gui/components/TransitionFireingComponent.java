package dk.aau.cs.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import pipe.gui.BlueTransitionControl;
import pipe.gui.CreateGui;

public class TransitionFireingComponent extends JPanel {
	private static final long serialVersionUID = -1208007964368671066L;

	protected BlueTransitionControl blueTransitionControl;
	protected EnabledTransitionsList enabledTransitionsList;
	
	public TransitionFireingComponent() {
		super(new GridBagLayout());
		
		blueTransitionControl = new BlueTransitionControl();
		blueTransitionControl.setVisible(CreateGui.getApp().isShowingBlueTransitions());
		enabledTransitionsList = new EnabledTransitionsList();
		
		enabledTransitionsList.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Enabled Transitions"),
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		enabledTransitionsList
		.setToolTipText("List of currently enabled transitions (double click a transition to fire it)");
		enabledTransitionsList.setPreferredSize(new Dimension(
				enabledTransitionsList.getPreferredSize().width,
				enabledTransitionsList.getMinimumSize().height));
		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		this.add(enabledTransitionsList, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		this.add(blueTransitionControl, gbc);
	}
	
	public EnabledTransitionsList getEnabledTransitionList() {
		return enabledTransitionsList;
	}
	
	public BlueTransitionControl getBlueTransitionControl() {
		return blueTransitionControl;
	}

	public void showBlueTransitions(boolean enable) {
		if(blueTransitionControl != null){
			blueTransitionControl.setVisible(enable);
		}
	}
}
