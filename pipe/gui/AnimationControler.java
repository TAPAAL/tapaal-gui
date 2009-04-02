package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

import dk.aau.cs.petrinet.Place;

import jpowergraph.PIPEInitialState;

import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TimedPlace;
import pipe.exception.InvariantViolatedAnimationException;
import pipe.gui.GuiFrame.AnimateAction;
import pipe.gui.GuiFrame.ToggleButton;
import pipe.gui.action.GuiAction;

/**
 * Implementes af class handling drawing of animation functions
 * 
 * Copyright 2009
 * Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * Based on code from GuiFrame
 * 
 * Licensed under the Open Software License version 3.0
 */

public class AnimationControler extends JPanel {
	
	private javax.swing.JButton okButton;
	
	class ToggleButton extends JToggleButton implements PropertyChangeListener {

		public ToggleButton(Action a) {
			super(a);
			if (a.getValue(Action.SMALL_ICON) != null) {
				// toggle buttons like to have images *and* text, nasty
				setText(null);
			}
			a.addPropertyChangeListener(this);
		}


		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "selected") {
				Boolean b = (Boolean)evt.getNewValue();
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

	   public AnimationControler(String text) throws 
	           javax.swing.text.BadLocationException {
	  
		   
		startAction = new AnimateAction("Simulation mode", 
				Pipe.START, "Toggle Simulation Mode", "Ctrl A", true);
		   
		   
	      stepbackwardAction =
	              new AnimateAction("Back", Pipe.STEPBACKWARD,
	              "Step backward a firing", "typed 4" );
	      stepforwardAction  =
	              new AnimateAction("Forward", Pipe.STEPFORWARD,
	              "Step forward a firing", "typed 6" );
	      
	      stepbackwardAction.setEnabled(false);
		   stepforwardAction.setEnabled(false);
	      
	      //timeAction = new AnimateAction("Time", Pipe.TIMEPASS, "Let Time pass", "_");
	      
	      randomAction =
	              new AnimateAction("Random", Pipe.RANDOM,
	              "Randomly fire a transition", "typed 5");
	      randomAnimateAction =
	              new AnimateAction("Simulate", Pipe.ANIMATE,
	              "Randomly fire a number of transitions", "typed 7",true);      
		   
		   
		   setLayout(new GridBagLayout());
		   GridBagConstraints c = new GridBagConstraints();
		    

		   
	        //Use the default FlowLayout.
	        //Create everything.

		   JLabel label = new JLabel("Simulator");

		   c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 0;
			add(label, c);
		   
		      
		      
		      JPanel firemode = new JPanel(new FlowLayout(FlowLayout.LEFT));
		      
		    String[] firermodes = { "Random", "Oldest", "Youngest", "Manual" };

		    // TODO - set current fire mode
		    
		    label = new JLabel("Token selection method: ");

		    firermodebox = new JComboBox(firermodes);
		    
		    int o=0;
		    boolean found=false;

		    for (String s : firermodes){
		    	if (CreateGui.getAnimator().getFiringmode().getName().equals(s)){
		    		found=true;
		    		break;
		    	}
		    	o++;
		    }

		    if (found){
		    	firermodebox.setSelectedIndex(o);
		    } else {
		    	firermodebox.setSelectedIndex(0);
		    }

		    firermodebox.addActionListener(new java.awt.event.ActionListener() {
		         public void actionPerformed(java.awt.event.ActionEvent evt) {
		        	 
		        	 CreateGui.getAnimator().setFiringmode(firermodebox.getSelectedItem().toString());
		         }
		    });
		    firemode.add(label);
		    firemode.add(firermodebox);
		    
		    
		    
		      c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 1;
		      add(firemode, c);
			

		   JToolBar animationToolBar = new JToolBar();
		   animationToolBar.setFloatable(false);
		   addButton(animationToolBar, stepbackwardAction);
		   addButton(animationToolBar, stepforwardAction);

		   //addButton(animationToolBar, timeAction);

		   addButton(animationToolBar, randomAction);
		   addButton(animationToolBar, randomAnimateAction);

		   
		   animationToolBar.setVisible(true);

		   c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 2;
			add(animationToolBar, c);
		   
		   
			JPanel timedelayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			
			okButton = new javax.swing.JButton();
			
			okButton.setText("Time delay");
		      //okButton.setMaximumSize(new java.awt.Dimension(75, 25));
		      okButton.setMinimumSize(new java.awt.Dimension(75, 25));
		      //okButton.setPreferredSize(new java.awt.Dimension(75, 25));
		      okButton.addActionListener(new java.awt.event.ActionListener() {
		         public void actionPerformed(java.awt.event.ActionEvent evt) {
		            //okButtonHandler(evt);
		        	 AnimationHistory animBox = CreateGui.getAnimationHistory();
		        	 animBox.clearStepsForward();
		        	 try {
						CreateGui.getAnimator().letTimePass(Float.parseFloat((TimeDelayField.getValue().toString())));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvariantViolatedAnimationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	 stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
		             stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
		        	 
		        	 
		         }
		      });
			
		      TimeDelayField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		      TimeDelayField.setValue(new Float("1.00000"));
		      TimeDelayField.setColumns(4);
		      
		      timedelayPanel.add(TimeDelayField);
		      timedelayPanel.add(okButton);
		      
//		      c.fill = GridBagConstraints.HORIZONTAL;
//				c.weightx = 0.5;
//				c.gridx = 0;
//				c.gridy = 3;
//		      add(timedelayPanel, c);
				animationToolBar.add(timedelayPanel);		      
	   }
	   
	   
	   class AnimateAction extends GuiAction {

		      private int typeID;
		      private AnimationHistory animBox;


		      AnimateAction(String name, int typeID, String tooltip, String keystroke){
		         super(name, tooltip, keystroke);
		         this.typeID = typeID;
		      }

		      AnimateAction(String name, int typeID, String tooltip, String keystroke,
		              boolean toggleable){
		         super(name, tooltip, keystroke, toggleable);
		         this.typeID = typeID;
		      }


		      public AnimateAction(String name, int typeID, String tooltip,
					KeyStroke keyStroke) {
			         super(name, tooltip, keyStroke);
			         this.typeID = typeID;
			}

			public void actionPerformed(ActionEvent ae){
		         
		         animBox = CreateGui.getAnimationHistory();

		         switch(typeID){
		            case Pipe.TIMEPASS:
		            	animBox.clearStepsForward();
		            	try {
							CreateGui.getAnimator().letTimePass(1f);
						} catch (InvariantViolatedAnimationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            	
						setAnimationButtonsEnabled();
		            	
		            	break;
		            	
		            case Pipe.RANDOM:
		               animBox.clearStepsForward();
		               CreateGui.getAnimator().doRandomFiring();
		               
		               setAnimationButtonsEnabled();
		               break;

		            case Pipe.STEPFORWARD:
		               animBox.stepForward();
		               CreateGui.getAnimator().stepForward();
		               setAnimationButtonsEnabled();

//This is totally overruled by the actions in pipe/gui/GuiFram.java in fact it is unclear if this code is ever active after initialization
		               
//		               for (pipe.dataLayer.Place p : CreateGui.getModel().getPlaces() ){
//		            	   if (((TimedPlace)p).isAgeOfTokensShown()){
//		            		   //((TimedPlace)p).showAgeOfTokens(false);
//		            		   ((TimedPlace)p).showAgeOfTokens(true);
//		            	   }
//		               }
		               break;

		            case Pipe.STEPBACKWARD:
		               animBox.stepBackwards();
		               CreateGui.getAnimator().stepBack();
		               setAnimationButtonsEnabled();
		               break;

		            case Pipe.ANIMATE:
		               Animator a = CreateGui.getAnimator();
		               if (a.getNumberSequences() > 0) {
		                  a.setNumberSequences(0); // stop animation
		                  setSelected(false);
		               } else {
		                  stepbackwardAction.setEnabled(false);
		                  stepforwardAction.setEnabled(false);
		                  randomAction.setEnabled(false);
		                  setSelected(true);
		                  animBox.clearStepsForward();
		                  CreateGui.getAnimator().startRandomFiring();
		               }
		               break;

		            default:
		               break;
		         }
		      }

	   }

	   private void setEnabledStepbackwardAction(boolean b) {
		   stepbackwardAction.setEnabled(b);

	   }
	   private void setEnabledStepforwardAction(boolean b) {
		   stepforwardAction.setEnabled(b);

	   }
	   
	   public void setAnimationButtonsEnabled(){
		   AnimationHistory animationHistory = CreateGui.getAnimationHistory();

		   setEnabledStepforwardAction(animationHistory.isStepForwardAllowed());
		   setEnabledStepbackwardAction(animationHistory.isStepBackAllowed());

		   CreateGui.appGui.setEnabledStepForwardAction(animationHistory.isStepForwardAllowed());
		   CreateGui.appGui.setEnabledStepBackwardAction(animationHistory.isStepBackAllowed());
		   
//		   setEnabledStepforwardAction(false);
//		   setEnabledStepbackwardAction(false);
	   }
	   
	   JFormattedTextField TimeDelayField = new JFormattedTextField();
	   JComboBox firermodebox = null;
}
