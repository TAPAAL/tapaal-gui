package pipe.gui.widgets;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;

public class AnimationSelectmodeDialog extends JPanel {

	private static final long serialVersionUID = 7852107237344005547L;

	TimedTransition transition = null;

	public ArrayList<JComboBox> presetPanels = new ArrayList<JComboBox>();

	private JPanel namePanel;

	private JButton okButton;
	private boolean cancelled = true;

	public boolean cancelled() {
		return cancelled;
	}

	public AnimationSelectmodeDialog(TimedTransition transition) {

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		this.transition = transition;

		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Select tokens to Fire in Transition " + transition.name()));

		add(namePanel, c);

		// Start adding the stuff
		JPanel presetPanelContainer;
		presetPanelContainer = new JPanel(new FlowLayout());

		c.gridx = 0;
		c.gridy = 1;

		add(presetPanelContainer, c);

		for (TimedInputArc arc : transition.getInputArcs()) {
			JPanel tokenPanel = createDropDownForArc(arc.source().name(), arc.getElligibleTokens());
			presetPanelContainer.add(tokenPanel);
		}
		
		for (TransportArc arc : transition.getTransportArcsGoingThrough()) {
			JPanel tokenPanel = createDropDownForArc(arc.source().name(), arc.getElligibleTokens());
			presetPanelContainer.add(tokenPanel);
		}

		c.gridx = 0;
		c.gridy = 2;
		// OK
		okButton = new javax.swing.JButton();

		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(75, 25));
		okButton.setMinimumSize(new java.awt.Dimension(75, 25));
		okButton.setPreferredSize(new java.awt.Dimension(75, 25));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelled = false;
				exit();
			}
		});

		add(okButton, c);
	}

	private JPanel createDropDownForArc(String placeName, List<TimedToken> elligibleTokens) {
		JPanel presetPanel = new JPanel(new FlowLayout());

		// For each place in the preset create a box for selecting tokens

		presetPanel.setBorder(BorderFactory.createTitledBorder("Place " + placeName));
		presetPanel.add(new JLabel("Select token from Place " + placeName));
		
		JComboBox selectTokenBox = new JComboBox(elligibleTokens.toArray());
		selectTokenBox.setSelectedIndex(0);

		presetPanel.add(selectTokenBox);
		presetPanels.add(selectTokenBox);
		return presetPanel;
	}

	private void exit() {
		this.getRootPane().getParent().setVisible(false);
	}

	public List<TimedToken> getTokens() {
		if(cancelled) return null;
		
		List<TimedToken> tokens = new ArrayList<TimedToken>();
		for(JComboBox box : presetPanels){
			tokens.add((TimedToken)box.getSelectedItem());
		}
		
		return tokens;
	}

}
