package pipe.gui.widgets;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TAPNTransition;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArc;
import pipe.exception.InvariantViolatedAnimationException;
import pipe.gui.AnimationHistory;
import pipe.gui.Pipe;

public class AnimationSelectmodeDialog extends JPanel{

	private static final long serialVersionUID = 7852107237344005547L;

	TAPNTransition firedtransition = null;

	public ArrayList<JComboBox> presetPanels = new ArrayList<JComboBox>();

	private JPanel namePanel;

	private JComboBox selectTokenBox;

	private JButton okButton;


	public AnimationSelectmodeDialog(Transition t){

		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		
		
		firedtransition = (TAPNTransition)t; // XXX - unsafe cast (ok by contract)

		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Select tokens to Fire in Transition " + t.getName()));

		
		add(namePanel, c);


		
		//Start adding the stuff
		JPanel presetPanelContainer;
		presetPanelContainer = new JPanel(new FlowLayout());
		
		 
		c.gridx = 0;
		c.gridy = 1;

		add(presetPanelContainer, c);

		
		Arc test = null;

		for (Object o : t.getPreset()){

			JPanel presetPanel;
			presetPanel = new JPanel(new FlowLayout());
			
			
			Arc a = (Arc)o;
	
			//For each place in the preset create a box for selecting tokens
			
			presetPanel.setBorder(BorderFactory.createTitledBorder("Place " + a.getSource().getName()));
			presetPanel.add(new JLabel("Select token from Place " + a.getSource().getName()));

			ArrayList<String> eligableToken = null;

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
			df.setMinimumFractionDigits(1);
			if (a instanceof TransportArc){
				eligableToken= new ArrayList<String>();
				

			   TimedPlace p = (TimedPlace)a.getSource();
			   
			   ArrayList<BigDecimal> tokensOfPlace = p.getTokens();					
			   
			   TimedPlace targetPlace = (TimedPlace)((TransportArc)a).getconnectedTo().getTarget();

				for (int i=0; i< tokensOfPlace.size(); i++){
					if ( ((TimedArc)a).satisfiesGuard(tokensOfPlace.get(i)) && targetPlace.satisfiesInvariant(tokensOfPlace.get(i))) {
						eligableToken.add(df.format(tokensOfPlace.get(i)));
					}
				}	

			}else if (a instanceof TimedArc){
				eligableToken = new ArrayList<String>();
				//int indexOfOldestEligebleToken = 0;

				TimedPlace p = (TimedPlace)a.getSource();

				ArrayList<BigDecimal> tokensOfPlace = p.getTokens();						   
				for (int i=0; i< tokensOfPlace.size(); i++){
					if ( ((TimedArc)a).satisfiesGuard(tokensOfPlace.get(i))){
						eligableToken.add(df.format(tokensOfPlace.get(i)));
					}
				}						   
			}
			
			
			ArrayList<BigDecimal> tokens = ((TimedPlace)a.getSource()).getTokens();

			selectTokenBox = new JComboBox(eligableToken.toArray());

			selectTokenBox.setSelectedIndex(0);

			presetPanel.add(selectTokenBox);
			presetPanels.add(selectTokenBox);
			presetPanelContainer.add(presetPanel);


		}

		c.gridx = 0;
		c.gridy = 2;
		//OK
		okButton = new javax.swing.JButton();
		
		okButton.setText("OK");
	      okButton.setMaximumSize(new java.awt.Dimension(75, 25));
	      okButton.setMinimumSize(new java.awt.Dimension(75, 25));
	      okButton.setPreferredSize(new java.awt.Dimension(75, 25));
	      okButton.addActionListener(new java.awt.event.ActionListener() {
	         public void actionPerformed(java.awt.event.ActionEvent evt) {
	            
	        	 exit(); 
	        	 
	         }
	      });
		
	      
	      add(okButton,c);
		


	}
	   private void exit(){
			this.getRootPane().getParent().setVisible(false); 
		}

}
