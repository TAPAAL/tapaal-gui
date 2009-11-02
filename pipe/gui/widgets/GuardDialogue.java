package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pipe.dataLayer.Constant;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TimedArc;
import pipe.gui.CreateGui;

public class GuardDialogue extends JPanel /*implements ActionListener, PropertyChangeListener*/
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4582651236913407101L;
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

	private JCheckBox leftUseConstant;
	private JComboBox leftConstantsComboBox;
	private JCheckBox rightUseConstant;
	private JComboBox rightConstantsComboBox;

	public GuardDialogue (JRootPane rootPane, final PetriNetObject objectToBeEdited){
		myRootPane = rootPane;
		setLayout(new GridBagLayout());
		
		guardEditPanel = new JPanel(new GridBagLayout());
		guardEditPanel.setBorder(BorderFactory.createTitledBorder("Edit Guard"));
		
		buttonPanel = new JPanel(new GridBagLayout());

		String[] left = {"[","("};
		leftDelimiter = new JComboBox();
		Dimension dims = new Dimension(55,25);
		leftDelimiter.setPreferredSize(dims);
		leftDelimiter.setMinimumSize(dims);
		leftDelimiter.setMaximumSize(dims);
		leftDelimiter.setModel(new DefaultComboBoxModel(left));
		
		String[] right = {"]",")"};
		rightDelimiter = new JComboBox();
		rightDelimiter.setPreferredSize(dims);
		rightDelimiter.setMinimumSize(dims);
		rightDelimiter.setMaximumSize(dims);
		rightDelimiter.setModel(new DefaultComboBoxModel(right));
		

		inf = new JCheckBox("inf", true);
		
		firstIntervalNumber = new JSpinner();
		secondIntervalNumber = new JSpinner();
		
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		
		label = new JLabel("Time Interval:");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		guardEditPanel.add(label, gridBagConstraints);
		
		
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(leftDelimiter, gridBagConstraints);
		
		Dimension intervalBoxDims = new Dimension(90,25);
		firstIntervalNumber.setMaximumSize(intervalBoxDims);
		firstIntervalNumber.setMinimumSize(intervalBoxDims);
		firstIntervalNumber.setPreferredSize(intervalBoxDims);
		firstIntervalNumber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				firstSpinnerStateChanged(evt);
			}
		});
		
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add( firstIntervalNumber, gridBagConstraints);
		
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add( new JLabel(" , "), gridBagConstraints);

		secondIntervalNumber.setMaximumSize(intervalBoxDims);
		secondIntervalNumber.setMinimumSize(intervalBoxDims);
		secondIntervalNumber.setPreferredSize(intervalBoxDims);
		secondIntervalNumber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				secondSpinnerStateChanged(evt);
			}
		});
		
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add( secondIntervalNumber, gridBagConstraints);
		
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(rightDelimiter, gridBagConstraints);
		
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(inf, gridBagConstraints);
		
		inf.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent evt){
						if (inf.isSelected() ){
							secondIntervalNumber.setEnabled(false);
							rightDelimiter.setEnabled(false);
						}else {
							secondIntervalNumber.setEnabled(true);
							rightDelimiter.setEnabled(true);
						}
						setDelimiterModels();
					}

				});

		Set<String> constants = CreateGui.getModel().getConstantNames();
		boolean enableConstantsCheckBoxes = !constants.isEmpty();
		leftUseConstant = new JCheckBox("Use Constant");
		leftUseConstant.setEnabled(enableConstantsCheckBoxes);
		leftUseConstant.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				updateLeftComponents();
				updateRightConstantComboBox();
				setDelimiterModels();
			}
		});
				
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(leftUseConstant, gridBagConstraints);
		
		
		leftConstantsComboBox = new JComboBox(constants.toArray());
		leftConstantsComboBox.setVisible(false);
		leftConstantsComboBox.setMaximumSize(intervalBoxDims);
		leftConstantsComboBox.setMinimumSize(intervalBoxDims);
		leftConstantsComboBox.setPreferredSize(intervalBoxDims);
		leftConstantsComboBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					updateRightConstantComboBox();
					setDelimiterModels();
				}
			}
		});
		
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(leftConstantsComboBox, gridBagConstraints);
		
		rightUseConstant = new JCheckBox("Use Constant");
		rightUseConstant.setEnabled(enableConstantsCheckBoxes);
		rightUseConstant.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				updateRightComponents();
				updateRightConstantComboBox();
				setDelimiterModels();
			}
		});
				
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(rightUseConstant, gridBagConstraints);
		
		
		rightConstantsComboBox = new JComboBox(constants.toArray());
		rightConstantsComboBox.setVisible(false);
		rightConstantsComboBox.setMaximumSize(intervalBoxDims);
		rightConstantsComboBox.setMinimumSize(intervalBoxDims);
		rightConstantsComboBox.setPreferredSize(intervalBoxDims);
		gridBagConstraints = new GridBagConstraints();
		rightConstantsComboBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					setDelimiterModels();
				}
			}
		});
		
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(rightConstantsComboBox, gridBagConstraints);
		
		
		okButton.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						TimedArc arc = (TimedArc) objectToBeEdited;
						String guard = composeGuard(arc.getGuard());
						CreateGui.getView().getUndoManager().addNewEdit( arc.setGuard(guard) );
					
						CreateGui.getModel().buildConstraints();
											
						exit();
					}
				

					private String composeGuard(String oldGuard) {
						boolean useConstantLeft = leftUseConstant.isSelected();
						boolean useConstantRight = rightUseConstant.isSelected();
						
						String leftDelim = leftDelimiter.getSelectedItem().toString();
						String rightDelim = rightDelimiter.getSelectedItem().toString();
						String leftInterval = null;
						String rightInterval = null;
						
						if(useConstantLeft)
							leftInterval = leftConstantsComboBox.getSelectedItem().toString();
						else
							leftInterval = firstIntervalNumber.getValue().toString();
						
						if(useConstantRight)
							rightInterval = rightConstantsComboBox.getSelectedItem().toString();
						else if ( inf.isSelected())
							rightInterval = "inf";
						else
							rightInterval = secondIntervalNumber.getValue().toString();
						
						if (TimedArc.validateTimeInterval(leftDelim, leftInterval, rightInterval, rightDelim)){
							//XXX send error messsage
							return leftDelim + leftInterval + "," + rightInterval + rightDelim;
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
		
		setInitialState((TimedArc)objectToBeEdited);	
	}

	

	private void setInitialState(TimedArc arc) {
		String timeInterval = arc.getGuard();
			
		String[] partedTimeInterval = timeInterval.split(",");
		String firstNumber = partedTimeInterval[0].substring(1, partedTimeInterval[0].length() );
		String secondNumber = partedTimeInterval[1].substring(0, partedTimeInterval[1].length()-1);
		int first = 0, 
			second = 0;
		boolean firstIsNumber = true, 
				secondIsNumber = true;
		
		try{
			first = Integer.parseInt(firstNumber);
		}
		catch(NumberFormatException e){
			firstIsNumber = false;
		}
		
		try{
			second = Integer.parseInt(secondNumber);
		}
		catch(NumberFormatException e){
			secondIsNumber = false;
		}
		
		SpinnerNumberModel spinnerModelForFirstNumber = new SpinnerNumberModel(
				first, 0,	Integer.MAX_VALUE, 1);
		
		SpinnerNumberModel spinnerModelForSecondNumber = null;
		boolean isInf = secondNumber.equals("inf");
		if (isInf){
			inf.setSelected(true);
			secondIntervalNumber.setEnabled(false);
			rightDelimiter.setEnabled(false);
			spinnerModelForSecondNumber = new SpinnerNumberModel(
					0, 0,	Integer.MAX_VALUE, 1);
		}else{
			inf.setSelected(false);
			spinnerModelForSecondNumber = new SpinnerNumberModel(
					second, 0,	Integer.MAX_VALUE, 1);
		}
		firstIntervalNumber.setModel(spinnerModelForFirstNumber);
		secondIntervalNumber.setModel(spinnerModelForSecondNumber);
		
		if(!firstIsNumber){
			leftUseConstant.setSelected(true);
			leftConstantsComboBox.setSelectedItem(firstNumber);
			updateLeftComponents();
		}
		
		if(!secondIsNumber && !isInf){
			rightUseConstant.setSelected(true);
			rightConstantsComboBox.setSelectedItem(secondNumber);
			updateRightComponents();
		}
		
		boolean canUseConstants = rightUseConstant.isEnabled();
		if(canUseConstants){
			updateRightConstantComboBox();
		}
		
		setDelimiterModels();
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
	}
	
	private void updateLeftComponents() {
		boolean value = leftUseConstant.isSelected();
		firstIntervalNumber.setVisible(!value);
		leftConstantsComboBox.setVisible(value);	
		setDelimiterModels();
	}
	
	private void updateRightComponents() {
		boolean value = rightUseConstant.isSelected();
		inf.setVisible(!value);
		if(value)
			rightDelimiter.setEnabled(true);
		else
			rightDelimiter.setEnabled(!inf.isSelected());
		secondIntervalNumber.setVisible(!value);
		rightConstantsComboBox.setVisible(value);	
		
		((JDialog)myRootPane.getParent()).pack();
		setDelimiterModels();
	}

	public void exit(){
		myRootPane.getParent().setVisible(false);
	}

	private void setDelimiterModels(){
		int firstValue = getFirstValue();
		int secondValue = getSecondValue();
		
		DefaultComboBoxModel modelRightIncludedOnly = new DefaultComboBoxModel(new String[]{"]"});
		DefaultComboBoxModel modelLeftIncludedOnly = new DefaultComboBoxModel(new String[]{"["});
		DefaultComboBoxModel modelRightBoth = new DefaultComboBoxModel(new String[]{"]", ")"});
		DefaultComboBoxModel modelLeftBoth = new DefaultComboBoxModel(new String[]{"[", "("});
		DefaultComboBoxModel modelRightExcludedOnly = new DefaultComboBoxModel(new String[]{")"});
		
		if(firstValue > secondValue){
			secondIntervalNumber.setValue(firstValue);
			secondValue = firstValue;
		}
		
		String leftOldDelim = leftDelimiter.getSelectedItem().toString();
		String rightOldDelim = rightDelimiter.getSelectedItem().toString();
		
				
		if(firstValue==secondValue){
			rightDelimiter.setModel(modelRightIncludedOnly);
			leftDelimiter.setModel(modelLeftIncludedOnly);
		}else {
			leftDelimiter.setModel(modelLeftBoth);
		
			if(inf.isSelected() && !rightUseConstant.isSelected())
				rightDelimiter.setModel(modelRightExcludedOnly);
			else
				rightDelimiter.setModel(modelRightBoth);
		}
		
		leftDelimiter.setSelectedItem(leftOldDelim);
		if(rightUseConstant.isSelected())
			rightDelimiter.setSelectedItem("]");
		else
			rightDelimiter.setSelectedItem(rightOldDelim);
	}

	private int getSecondValue() {
		int secondValue;
		if(rightUseConstant.isSelected()){
			secondValue = CreateGui.getModel().getConstantValue(
					rightConstantsComboBox.getSelectedItem().toString());
		}
		else if(inf.isSelected()){
			secondValue = Integer.MAX_VALUE;
		}else{
			secondValue = Integer.parseInt(String.valueOf(secondIntervalNumber.getValue()));
		}
		return secondValue;
	}

	private int getFirstValue() {
		int firstValue;
		if(leftUseConstant.isSelected()){
			firstValue = CreateGui.getModel().getConstantValue(
					leftConstantsComboBox.getSelectedItem().toString());
		}
		else{
			firstValue = Integer.parseInt(String.valueOf(firstIntervalNumber.getValue()));
		}
		return firstValue;
	}
	
	private void updateRightConstantComboBox() {
		int value = getFirstValue();
		
		String oldRight = rightConstantsComboBox.getSelectedItem() != null ? 
				rightConstantsComboBox.getSelectedItem().toString() : null;
		rightConstantsComboBox.removeAllItems();
		Collection<Constant> constants = CreateGui.getModel().getConstants();
		for(Constant c : constants){
			if(c.getValue() >= value){
				rightConstantsComboBox.addItem(c.getName());
			}
		}
		
		//if(rightConstantsComboBox.getItemCount() == 0){
		//	rightUseConstant.setEnabled(false);
		//}
		
		if(oldRight != null)
			rightConstantsComboBox.setSelectedItem(oldRight);	
	}
	
	private void firstSpinnerStateChanged(ChangeEvent evt) {
		int firstValue = getFirstValue();
		int secondValue = getSecondValue();
		if(rightUseConstant.isSelected() && firstValue > secondValue){
			rightUseConstant.setSelected(false);
			updateRightComponents();
		}
		if(firstValue > CreateGui.getModel().getLargestConstantValue())
			rightUseConstant.setEnabled(false);
		else{
			rightUseConstant.setEnabled(true);
			updateRightConstantComboBox();
		}
		setDelimiterModels();
	}
	private void secondSpinnerStateChanged(ChangeEvent evt) {
		setDelimiterModels();
	}
}