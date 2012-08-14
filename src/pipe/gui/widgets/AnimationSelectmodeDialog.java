package pipe.gui.widgets;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pipe.gui.widgets.ArcTokenSelector.ArcTokenSelectorListener;
import pipe.gui.widgets.ArcTokenSelector.ArcTokenSelectorListenerEvent;

import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;

public class AnimationSelectmodeDialog extends JPanel {

	private static final long serialVersionUID = 7852107237344005547L;

	TimedTransition transition = null;

	public ArrayList<ArcTokenSelector> arcTokenSelectors = new ArrayList<ArcTokenSelector>();

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
		//c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		this.transition = transition;

		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("<html>Select tokens to fire transition <b>" + transition.name() + "</b></html>"));

		add(namePanel, c);

		// Start adding the stuff
		JPanel presetPanelContainer = new JPanel(new FlowLayout());
		c.gridx = 0;
		c.gridy = 1;

		add(presetPanelContainer, c);

		if(transition.isShared()){
			for(TimedTransition trans : transition.sharedTransition().transitions()){
				createDropDownsForTransition(trans, presetPanelContainer);
			}
		}else{
			createDropDownsForTransition(transition, presetPanelContainer);
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

		updateOkButton();
		add(okButton, c);
	}

	private void createDropDownsForTransition(TimedTransition transition, JPanel presetPanelContainer) {
		for (TimedInputArc arc : transition.getInputArcs()) {
			JPanel tokenPanel = createDropDownForArc(arc.source().toString(), arc.getElligibleTokens(), arc.getWeight().value());
			presetPanelContainer.add(tokenPanel);
		}
		
		for (TransportArc arc : transition.getTransportArcsGoingThrough()) {
			JPanel tokenPanel = createDropDownForArc(arc.source().toString(), arc.getElligibleTokens(), arc.getWeight().value());
			presetPanelContainer.add(tokenPanel);
		}
	}

	private JPanel createDropDownForArc(String placeName, List<TimedToken> elligibleTokens, int weight) {
		ArcTokenSelector tokenSelector = new ArcTokenSelector(placeName, elligibleTokens, weight);
		tokenSelector.addArcTokenSelectorListener(new ArcTokenSelectorListener() {

			public void arcTokenSelectorActionPreformed(ArcTokenSelectorListenerEvent e) {
				updateOkButton();
			}
		});
		
		arcTokenSelectors.add(tokenSelector);
		return tokenSelector;
	}
	
	private void updateOkButton(){
		boolean enable = true;
		for(ArcTokenSelector selector : arcTokenSelectors){
			enable = enable && selector.allChosen(); 
		}
		
		okButton.setEnabled(enable);
	}

	private void exit() {
		this.getRootPane().getParent().setVisible(false);
	}

	public List<TimedToken> getTokens() {
		if(cancelled) return null;
		
		List<TimedToken> tokens = new ArrayList<TimedToken>();
		for(ArcTokenSelector selector : arcTokenSelectors){
			tokens.addAll(selector.getSelected());
		}
		
		return tokens;
	}

}
