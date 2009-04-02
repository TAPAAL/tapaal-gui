package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TimedPlace;
import pipe.gui.CreateGui;

public class GuardDialogue extends JPanel /*implements ActionListener, PropertyChangeListener*/
{
	private JRootPane myRootPane;
	private JPanel guardEditPanel;
	private JPanel buttonPanel;
	
	private JButton okButton;
	private JButton cancelButton;
	
	private JLabel label;
	private JSpinner firstIntervalNumber;
	private JSpinner secondIntervalNumber;

	private JCheckBox inf;
	
	private JComboBox leftDelimiter;
	private JComboBox rightDelimiter;

	
	public GuardDialogue (JRootPane rootPane, final PetriNetObject objectToBeEdited){
		myRootPane = rootPane;
		setLayout(new GridBagLayout());
		
		guardEditPanel = new JPanel(new GridBagLayout());
		guardEditPanel.setBorder(BorderFactory.createTitledBorder("Edit Guard"));
		
		buttonPanel = new JPanel(new GridBagLayout());

		String[] left = {"[","("};
		leftDelimiter = new JComboBox();
		leftDelimiter.setModel(new DefaultComboBoxModel(left));
		
		String[] right = {"]",")"};
		rightDelimiter = new JComboBox();
		rightDelimiter.setModel(new DefaultComboBoxModel(right));
		
		String timeInterval = ( (TimedArc) objectToBeEdited).getGuard();
		if ( timeInterval.contains("[") ){
			leftDelimiter.setSelectedItem("[");
		} else {
			leftDelimiter.setSelectedItem("(");
		}
		if ( timeInterval.contains("]") ){
			rightDelimiter.setSelectedItem("]");
		}else {
			rightDelimiter.setSelectedItem(")");
		}

		inf = new JCheckBox("inf", true);
		
		firstIntervalNumber = new JSpinner();
		secondIntervalNumber = new JSpinner();
		
		String[] partedTimeInterval = timeInterval.split(",");
		String firstNumber = partedTimeInterval[0].substring(1, partedTimeInterval[0].length() );
		String secondNumber = partedTimeInterval[1].substring(0, partedTimeInterval[1].length()-1);
		
		SpinnerNumberModel spinnerModelForFirstNumber = new SpinnerNumberModel(
				Integer.parseInt( firstNumber ), 0,	Integer.MAX_VALUE, 1);
		
		SpinnerNumberModel spinnerModelForSecondNumber = new SpinnerNumberModel(
				Integer.MAX_VALUE, 0,	Integer.MAX_VALUE, 1);
		if (secondNumber.equals("inf")){
			inf.setSelected(true);
			secondIntervalNumber.setEnabled(false);
			rightDelimiter.setEnabled(false);
			spinnerModelForSecondNumber = new SpinnerNumberModel(
					0, 0,	Integer.MAX_VALUE, 1);
		}else{
			inf.setSelected(false);
			spinnerModelForSecondNumber = new SpinnerNumberModel(
					Integer.parseInt( secondNumber ), 0,	Integer.MAX_VALUE, 1);
		}
		
		firstIntervalNumber.setModel(spinnerModelForFirstNumber);
		secondIntervalNumber.setModel(spinnerModelForSecondNumber);		

		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		
		label = new JLabel("Time Interval:");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		guardEditPanel.add(label, gridBagConstraints);
		
		
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(leftDelimiter, gridBagConstraints);
		
		firstIntervalNumber.setMaximumSize(new Dimension(50,30));
		firstIntervalNumber.setMinimumSize(new Dimension(50,30));
		firstIntervalNumber.setPreferredSize(new Dimension(50,30));
		firstIntervalNumber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				firstSpinnerStateChanged(evt);
			}
		});
		
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add( firstIntervalNumber, gridBagConstraints);
		
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add( new JLabel(" , "), gridBagConstraints);

		secondIntervalNumber.setMaximumSize(new Dimension(50,30));
		secondIntervalNumber.setMinimumSize(new Dimension(50,30));
		secondIntervalNumber.setPreferredSize(new Dimension(50,30));
		secondIntervalNumber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				secondSpinnerStateChanged(evt);
			}
		});
		
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add( secondIntervalNumber, gridBagConstraints);
		
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(rightDelimiter, gridBagConstraints);
		
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(inf, gridBagConstraints);
		
		inf.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent evt){
						setDelimiterModels();
						if ( ((JCheckBox)inf).isSelected() ){
							secondIntervalNumber.setEnabled(false);
							rightDelimiter.setSelectedItem(")");
							rightDelimiter.setEnabled(false);
						}else {
							secondIntervalNumber.setEnabled(true);
							rightDelimiter.setSelectedItem("]");
							rightDelimiter.setEnabled(true);
						}
					}

				});

		okButton.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						CreateGui.getView().getUndoManager().addNewEdit( ((TimedArc) objectToBeEdited).setGuard(composeGuard(((TimedArc) objectToBeEdited).getGuard())) );
						exit();
					}

					private String composeGuard(String oldGuard) {
						String guard = "";
						if ( ((JCheckBox)inf).getSelectedObjects() != null){
							guard = guard + leftDelimiter.getSelectedItem() + firstIntervalNumber.getValue() + ",inf)";	
						}else {
							guard = guard + leftDelimiter.getSelectedItem() + firstIntervalNumber.getValue() + ","
							+ secondIntervalNumber.getValue() + rightDelimiter.getSelectedItem();
						}
						if (TimedArc.validateTimeInterval(guard)){
							//XXX send error messsage
							return guard;
						}else {
							return oldGuard;
						}
					}
				}
		);
		cancelButton.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						exit();
					}
				}
		);

		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		add(guardEditPanel, gridBagConstraints);
		
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		buttonPanel.add(okButton,gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		buttonPanel.add(cancelButton,gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		add(buttonPanel, gridBagConstraints);
		
		myRootPane.setDefaultButton(okButton);
	}
	
	public void exit(){
		myRootPane.getParent().setVisible(false);
	}

	private void setDelimiterModels(){
		if (((Integer) firstIntervalNumber.getValue()) >= ((Integer) secondIntervalNumber.getValue())){
			secondIntervalNumber.setValue(firstIntervalNumber.getValue());
			if (inf.isSelected()){
				if (rightDelimiter.getModel().getSize()==1){
					rightDelimiter.setModel(new DefaultComboBoxModel(new String[]{"]",")"}));
				}
				if (leftDelimiter.getModel().getSize()==1){
					leftDelimiter.setModel(new DefaultComboBoxModel(new String[]{"[","("}));
					leftDelimiter.setSelectedItem("[");
				}
				rightDelimiter.setSelectedItem(")");
			}else{
				rightDelimiter.setModel(new DefaultComboBoxModel(new String[]{"]"}));
				leftDelimiter.setModel(new DefaultComboBoxModel(new String[]{"["}));
				rightDelimiter.setSelectedItem("]");
				leftDelimiter.setSelectedItem("[");
			}
		}else{
			if (inf.isSelected()){
				if (rightDelimiter.getModel().getSize()==1){
					rightDelimiter.setModel(new DefaultComboBoxModel(new String[]{"]",")"}));
				}
				if (leftDelimiter.getModel().getSize()==1){
					leftDelimiter.setModel(new DefaultComboBoxModel(new String[]{"[","("}));
					leftDelimiter.setSelectedItem("[");
				}
				rightDelimiter.setSelectedItem(")");
			}else {
				if (rightDelimiter.getModel().getSize()==1){
					rightDelimiter.setModel(new DefaultComboBoxModel(new String[]{"]",")"}));
					rightDelimiter.setSelectedItem("]");
				}
				if (leftDelimiter.getModel().getSize()==1){
					leftDelimiter.setModel(new DefaultComboBoxModel(new String[]{"[","("}));
					leftDelimiter.setSelectedItem("[");
				} 
			}
		}
	}
	
	private void firstSpinnerStateChanged(ChangeEvent evt) {
		setDelimiterModels();
	}
	private void secondSpinnerStateChanged(ChangeEvent evt) {
		setDelimiterModels();
	}
}