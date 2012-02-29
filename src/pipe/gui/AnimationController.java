package pipe.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

import pipe.dataLayer.NetType;
import pipe.gui.GuiFrame.AnimateAction;
import pipe.gui.Pipe.ElementType;
import pipe.gui.action.GuiAction;
import dk.aau.cs.gui.components.NonsearchableJComboBox;
import dk.aau.cs.model.tapn.simulation.FiringMode;

/**
 * Implementes af class handling drawing of animation functions
 * 
 * Copyright 2009 Author Kenneth Yrke Joergensen <kenneth@yrke.dk> Based on code
 * from GuiFrame
 * 
 * Licensed under the Open Software License version 3.0
 */

public class AnimationController extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7037756165634426275L;
	private javax.swing.JButton okButton;

	class ToggleButton extends JToggleButton implements PropertyChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6594894202788511816L;

		public ToggleButton(Action a) {
			super(a);
			if (a.getValue(Action.SMALL_ICON) != null) {
				// toggle buttons like to have images *and* text, nasty
				setText(null);
			}
			a.addPropertyChangeListener(this);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("selected")) {
				Boolean b = (Boolean) evt.getNewValue();
				if (b != null) {
					setSelected(b.booleanValue());
				}
			}
		}

	}

	private void addButton(JToolBar toolBar, GuiAction action) {

		if (action.getValue("selected") != null) {
			toolBar.add(new ToggleButton(action));
		} else {
			toolBar.add(action);
		}
	}

	AnimateAction startAction, stepforwardAction, stepbackwardAction,
			randomAction, randomAnimateAction, timeAction;

	public AnimationController() {
		startAction = CreateGui.appGui.startAction;

		stepbackwardAction = CreateGui.appGui.stepbackwardAction;
		stepforwardAction = CreateGui.appGui.stepforwardAction;

		stepbackwardAction.setEnabled(false);
		stepforwardAction.setEnabled(false);

		// timeAction = new AnimateAction("Time", Pipe.TIMEPASS,
		// "Let Time pass", "_");

		//randomAction = new AnimateAction("Random", ElementType.RANDOM,
		//		"Randomly fire a transition", "typed 5");
		//randomAnimateAction = new AnimateAction("Simulate", ElementType.ANIMATE,
		//		"Randomly fire a number of transitions", "typed 7", true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// Use the default FlowLayout.
		// Create everything.

		firermodebox = new NonsearchableJComboBox(FIRINGMODES);
		updateFiringModeComboBox();

		firermodebox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				CreateGui.getAnimator().setFiringmode(
						(String) firermodebox.getSelectedItem());
			}
		});

		JToolBar animationToolBar = new JToolBar();
		animationToolBar.setFloatable(false);
		animationToolBar.setBorder(new EmptyBorder(0, 0, 0, 0));
		addButton(animationToolBar, stepbackwardAction);
		addButton(animationToolBar, stepforwardAction);

		animationToolBar.setVisible(true);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		add(animationToolBar, c);

		if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
			JPanel firemode = new JPanel(new FlowLayout(FlowLayout.LEFT));

			JLabel label = new JLabel("Token selection: ");

			firemode.add(label);
			firemode.add(firermodebox);

			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 0;
			add(firemode, c);

			JPanel timedelayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

			okButton = new javax.swing.JButton();

			okButton.setText("Time delay");
			// okButton.setMaximumSize(new java.awt.Dimension(75, 25));
			okButton.setMinimumSize(new java.awt.Dimension(75, 25));
			// okButton.setPreferredSize(new java.awt.Dimension(75, 25));
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					// okButtonHandler(evt);
					addTimeDelayToHistory();
				}
			});
			
			//"Hack" to make sure the toolTip for this button is showed as long as possible
			okButton.addMouseListener(new MouseAdapter() {
			    final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
			    final int defaultInitalDelay = ToolTipManager.sharedInstance().getInitialDelay();
			    final int defaultReshowDelay = ToolTipManager.sharedInstance().getReshowDelay();
			    final int dismissDelayMinutes = Integer.MAX_VALUE;
			    @Override
			    public void mouseEntered(MouseEvent e) {
			        ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
			        ToolTipManager.sharedInstance().setInitialDelay(0);
			        ToolTipManager.sharedInstance().setReshowDelay(0);
			    }
			    
			    @Override
			    public void mouseExited(MouseEvent e) {
			        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
			        ToolTipManager.sharedInstance().setInitialDelay(defaultInitalDelay);
			        ToolTipManager.sharedInstance().setReshowDelay(defaultReshowDelay);
			    }
			});

			TimeDelayField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						addTimeDelayToHistory();
					}
				}

				public void keyReleased(KeyEvent e) {
					CreateGui.getAnimator().reportBlockingPlaces();
				}

				public void keyTyped(KeyEvent e) {

				}

			});

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
			df.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);

			TimeDelayField.setText(df.format(1f));
			TimeDelayField.setColumns(5);

			timedelayPanel.add(TimeDelayField);
			timedelayPanel.add(okButton);
			//CreateGui.getAnimator().reportBlockingPlaces();
			
			// c.fill = GridBagConstraints.HORIZONTAL;
			// c.weightx = 0.5;
			// c.gridx = 0;
			// c.gridy = 3;
			// add(timedelayPanel, c);
			animationToolBar.add(timedelayPanel);
		}

		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Simulation Control"), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));
		this.setPreferredSize(new Dimension(275, 100));
		this.setMinimumSize(new Dimension(275, 100));
	}

	public void updateFiringModeComboBox() {
		FiringMode currentFiringMode = CreateGui.getAnimator().getFiringmode();
		if (currentFiringMode == null) {
			firermodebox.setSelectedItem("Manual");
		} else {
			firermodebox.setSelectedItem(currentFiringMode.toString());
		}

	}
	
	public javax.swing.JButton getOkButton(){
		return okButton;
	}

	private void addTimeDelayToHistory() {
		AnimationHistoryComponent animBox = CreateGui.getAnimationHistory();
		animBox.clearStepsForward();
		try {

			// Hack to allow usage of localised numbes
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
			df.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
			df.applyLocalizedPattern("#.#");

			DecimalFormat parser = new DecimalFormat();
			parser.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
			parser.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);

			Number parseTime = parser.parse(TimeDelayField.getText()); // Parse
																		// the
																		// number
																		// localised
			// Try parse

			BigDecimal timeDelayToSet = new BigDecimal(parseTime.toString(),
					new MathContext(Pipe.AGE_PRECISION));

			// BigDecimal timeDelayToSet = new
			// BigDecimal(TimeDelayField.getText(), new
			// MathContext(Pipe.AGE_PRECISION));
			if (timeDelayToSet.compareTo(new BigDecimal(0L)) <= 0) {
				// Nothing to do, illegal value
				System.err.println("Illegal value");
			} else {
				CreateGui.getAnimator().letTimePass(timeDelayToSet);
			}
		} catch (NumberFormatException e) {
			// Do nothing, invalud number
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setAnimationButtonsEnabled();
	}
	
	public BigDecimal getCurrentDelay() throws NumberFormatException, ParseException{
		// Hack to allow usage of localised numbes
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.applyLocalizedPattern("#.#");

		DecimalFormat parser = new DecimalFormat();
		parser.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		parser.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);

		Number parseTime = parser.parse(TimeDelayField.getText()); // Parse
																	// the
																	// number
																	// localised
		// Try parse

		BigDecimal timeDelayToSet = new BigDecimal(parseTime.toString(),
				new MathContext(Pipe.AGE_PRECISION));

		// BigDecimal timeDelayToSet = new
		// BigDecimal(TimeDelayField.getText(), new
		// MathContext(Pipe.AGE_PRECISION));
		return timeDelayToSet;
	}

	private void setEnabledStepbackwardAction(boolean b) {
		stepbackwardAction.setEnabled(b);

	}

	private void setEnabledStepforwardAction(boolean b) {
		stepforwardAction.setEnabled(b);

	}

	public void setAnimationButtonsEnabled() {
		AnimationHistoryComponent animationHistory = CreateGui.getAnimationHistory();

		setEnabledStepforwardAction(animationHistory.isStepForwardAllowed());
		setEnabledStepbackwardAction(animationHistory.isStepBackAllowed());

		CreateGui.appGui.setEnabledStepForwardAction(animationHistory.isStepForwardAllowed());
		CreateGui.appGui.setEnabledStepBackwardAction(animationHistory.isStepBackAllowed());
	}

	JTextField TimeDelayField = new JTextField();
	JComboBox firermodebox = null;
	private final String[] FIRINGMODES = { "Random", "Oldest", "Youngest", "Manual" };
}
