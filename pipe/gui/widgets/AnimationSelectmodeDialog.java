package pipe.gui.widgets;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TimedInhibitorArcComponent;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArcComponent;
import pipe.dataLayer.colors.ColoredInputArc;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;

public class AnimationSelectmodeDialog extends JPanel {

	private static final long serialVersionUID = 7852107237344005547L;

	TimedTransitionComponent firedtransition = null;

	public ArrayList<JComboBox> presetPanels = new ArrayList<JComboBox>();

	private JPanel namePanel;

	private JButton okButton;
	private boolean cancelled = true;

	public boolean cancelled() {
		return cancelled;
	}

	public AnimationSelectmodeDialog(Transition t) {

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		firedtransition = (TimedTransitionComponent) t; // XXX - unsafe cast (ok
		// by contract)

		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Select tokens to Fire in Transition "
				+ t.getName()));

		add(namePanel, c);

		// Start adding the stuff
		JPanel presetPanelContainer;
		presetPanelContainer = new JPanel(new FlowLayout());

		c.gridx = 0;
		c.gridy = 1;

		add(presetPanelContainer, c);

		for (Arc a : t.getPreset()) {
			if (!(a instanceof TimedInhibitorArcComponent)) {
				JPanel presetPanel = createDropDownForArc(a);
				presetPanelContainer.add(presetPanel);
			}

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

	private JPanel createDropDownForArc(Arc a) {
		JPanel presetPanel;
		presetPanel = new JPanel(new FlowLayout());

		// For each place in the preset create a box for selecting tokens

		presetPanel.setBorder(BorderFactory.createTitledBorder("Place "
				+ a.getSource().getName()));
		presetPanel.add(new JLabel("Select token from Place "
				+ a.getSource().getName()));

		ArrayList<String> eligableToken = null;

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.setMinimumFractionDigits(1);
		if (a instanceof TransportArcComponent) {
			eligableToken = new ArrayList<String>();
			TimedPlaceComponent p = (TimedPlaceComponent) a.getSource();

			ArrayList<BigDecimal> tokensOfPlace = p.getTokens();

			TimedPlaceComponent targetPlace = (TimedPlaceComponent) ((TransportArcComponent) a)
			.getConnectedTo().getTarget();

			for (int i = 0; i < tokensOfPlace.size(); i++) {
				if (((TimedInputArcComponent) a)
						.satisfiesGuard(tokensOfPlace.get(i))
						&& targetPlace.satisfiesInvariant(tokensOfPlace
								.get(i))) {
					eligableToken.add(df.format(tokensOfPlace.get(i)));
				}
			}

		} else if (a instanceof TimedInputArcComponent) {
			eligableToken = new ArrayList<String>();
			// int indexOfOldestEligebleToken = 0;

			TimedPlaceComponent p = (TimedPlaceComponent) a.getSource();

			ArrayList<BigDecimal> tokensOfPlace = p.getTokens();
			for (int i = 0; i < tokensOfPlace.size(); i++) {
				if (((TimedInputArcComponent) a)
						.satisfiesGuard(tokensOfPlace.get(i))) {
					eligableToken.add(df.format(tokensOfPlace.get(i)));
				}
			}
		}

		JComboBox selectTokenBox = new JComboBox(eligableToken.toArray());
		selectTokenBox.setSelectedIndex(0);

		presetPanel.add(selectTokenBox);
		presetPanels.add(selectTokenBox);
		return presetPanel;
	}

	private void exit() {
		this.getRootPane().getParent().setVisible(false);
	}

}
