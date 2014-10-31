package pipe.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import pipe.dataLayer.NetType;
import pipe.gui.GuiFrame.AnimateAction;
import pipe.gui.Pipe.ElementType;
import pipe.gui.action.GuiAction;
import pipe.gui.widgets.DecimalOnlyDocumentFilter;
import dk.aau.cs.gui.components.NonsearchableJComboBox;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
        private JSlider delaySlider;
        private int delayScale = 10;
	private String PRECISION_ERROR_MESSAGE = "The precision is limited to 5 decimal places, the number will be truncated.";
	private String PRECISION_ERROR_DIALOG_TITLE = "Precision of Time Delay Exceeded"; 

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

	public AnimationController(NetType netType) {
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
		c.gridy = 2;
		add(animationToolBar, c);

		if (!netType.equals(NetType.UNTIMED)) {
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
			        ToolTipManager.sharedInstance().setEnabled(true);
			    }
			    
			    @Override
			    public void mouseExited(MouseEvent e) {
			        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
			        ToolTipManager.sharedInstance().setInitialDelay(defaultInitalDelay);
			        ToolTipManager.sharedInstance().setReshowDelay(defaultReshowDelay);
			        ToolTipManager.sharedInstance().setEnabled(CreateGui.getApp().isShowingToolTips());
			    }
			});

			TimeDelayField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						addTimeDelayToHistory();
						TimeDelayField.getFocusCycleRootAncestor().requestFocus();	// Remove focus
					}
				}

				public void keyReleased(KeyEvent e) {
					CreateGui.getAnimator().reportBlockingPlaces();
				}

				public void keyTyped(KeyEvent e) {

				}

			});
			
			// Disable shortcuts when focused
			TimeDelayField.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent arg0) {
					CreateGui.getApp().setStepShotcutEnabled(true);
				}
				public void focusGained(FocusEvent arg0) {
					CreateGui.getApp().setStepShotcutEnabled(false);
				}
			});

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
			df.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);

			TimeDelayField.setText(df.format(1f));
			TimeDelayField.setColumns(6);
                        
			timedelayPanel.add(TimeDelayField);
			timedelayPanel.add(okButton);
			//CreateGui.getAnimator().reportBlockingPlaces();
			
			// c.fill = GridBagConstraints.HORIZONTAL;
			// c.weightx = 0.5;
			// c.gridx = 0;
			// c.gridy = 3;
			// add(timedelayPanel, c);
			animationToolBar.add(timedelayPanel);
                        
                        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        JButton decrese = new JButton("-");
                        decrese.setPreferredSize(new Dimension(20, 30));
                        decrese.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                setDelayModeScale(delayScale/2);
                                delaySlider.setValue(delaySlider.getValue()*2);
                            }
                        });
                        sliderPanel.add(decrese);
                        
                        delaySlider = new JSlider(0, 100);
                        delaySlider.setSnapToTicks(false);
                        delaySlider.setMajorTickSpacing(10);
                        delaySlider.setMinorTickSpacing(1);
                        delaySlider.setPaintLabels(true);
                        delaySlider.setPaintTicks(true);
                        delaySlider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                TimeDelayField.setText(Double.toString(delaySlider.getValue()*((double) delayScale)/100));
                            }
                        });
                        
                        setDelayModeScale(8);
                        
                        sliderPanel.add(delaySlider);
                        JButton increse = new JButton("+");
                        increse.setPreferredSize(new Dimension(20, 30));
                        increse.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                setDelayModeScale(delayScale*2);
                                delaySlider.setValue(delaySlider.getValue()/2);
                            }
                        });
                        sliderPanel.add(increse);
                        
                        c.fill = GridBagConstraints.HORIZONTAL;
                        c.weightx = 0.5;
                	c.gridx = 0;
                        c.gridy = 1;
                        add(sliderPanel, c);
		}
                
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Simulation Control"), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));
		this.setPreferredSize(new Dimension(275, 180));
		this.setMinimumSize(new Dimension(275, 180));
		
		initializeDocumentFilterForDelayInput();
	}
        
        private void setDelayModeScale(int scale){
            if (scale == 0) return;
            delayScale = scale;
            Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
            labels.put(0, new JLabel("0"));
            labels.put(100, new JLabel(Integer.toString(delayScale)));
            delaySlider.setLabelTable(labels);
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
			//e.printStackTrace();
		}

		setAnimationButtonsEnabled();
	}
	
	public BigDecimal getCurrentDelay() throws NumberFormatException, ParseException{
		String oldText = TimeDelayField.getText();
		char localDecimalseparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

		if (Pattern.matches("^(([1-9]([0-9])*)?|0)(" + Pattern.quote(Character.toString(localDecimalseparator)) + "([0-9]){6,})?$",  oldText)) {
			if (oldText.indexOf('.') != -1) {
				TimeDelayField.setText(oldText.substring(0,oldText.indexOf('.')+6));	
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						PRECISION_ERROR_MESSAGE, PRECISION_ERROR_DIALOG_TITLE,
						JOptionPane.INFORMATION_MESSAGE);				
			}
		}
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
	
	private void initializeDocumentFilterForDelayInput() {
		javax.swing.text.Document doc = TimeDelayField.getDocument();
		((AbstractDocument)doc).setDocumentFilter(new DecimalOnlyDocumentFilter(5));
	}

	JTextField TimeDelayField = new JTextField();
	JComboBox firermodebox = null;
	private final String[] FIRINGMODES = { "Random", "Oldest", "Youngest", "Manual" };
}
