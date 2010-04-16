package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.IntOrConstant;
import pipe.gui.undo.UndoManager;

public class OutputValueEditorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4253194892721505373L;

	private JRootPane rootPane;
	private ColoredOutputArc arc;
	private UndoManager undoManager;
	private DataLayer model;
	
	private JPanel outputValuePanel;
	private JRadioButton normalRadioButton;
	private JRadioButton constantRadioButton;
	private JSpinner integerValueSpinner;
	private JComboBox constantDropDown;
	
	private JPanel buttonPanel;
	private JButton okButton;
	private JButton cancelButton;
	
	public OutputValueEditorPanel(JRootPane rootPane, ColoredOutputArc arc, DataLayer model, UndoManager undoManager){
		this.rootPane = rootPane;
		this.arc = arc;
		this.undoManager = undoManager;
		this.model = model;
		
		initComponents();
	}
	
	public void initComponents(){
		setLayout(new GridBagLayout());
		initOutputValuePanel();	
		initButtonPanel();
		setInitialState();
	}

	private void setInitialState() {
		IntOrConstant value = arc.getOutputValue();
		
		if(value.isUsingConstant()){
			constantRadioButton.setSelected(true);
			enableConstantControls();
			
			constantDropDown.setSelectedItem(value.getConstantName());
		}else{
			normalRadioButton.setSelected(true);
			enableNormalControls();
			
			integerValueSpinner.setValue(value.getIntegerValue());
		}
	}

	private void initButtonPanel() {
		buttonPanel = new JPanel(new GridBagLayout());
		
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				IntOrConstant output = new IntOrConstant();
				if(normalRadioButton.isSelected()){
					output.setOutputValue((Integer)integerValueSpinner.getValue());
				}else{
					output.setConstantName((String)constantDropDown.getSelectedItem());
				}
				
				undoManager.addEdit(arc.setOutputValue(output));
				model.buildConstraints();
				
				exit();			
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3,3,3,3);
		buttonPanel.add(okButton, gbc);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(3,3,3,3);
		buttonPanel.add(cancelButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(buttonPanel, gbc);		
	}

	private void initOutputValuePanel() {
		outputValuePanel = new JPanel(new GridBagLayout());
		outputValuePanel.setBorder(BorderFactory.createTitledBorder("Output Value"));
				
		normalRadioButton = new JRadioButton("Normal:");
		normalRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				enableNormalControls();
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3,3,3,3);
		outputValuePanel.add(normalRadioButton, gbc);
		
		constantRadioButton = new JRadioButton("Constant:");
		constantRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				enableConstantControls();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(3,3,3,3);
		outputValuePanel.add(constantRadioButton, gbc);
		
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(normalRadioButton);
		btnGroup.add(constantRadioButton);
		
		integerValueSpinner = new JSpinner(new SpinnerNumberModel(0,0,Integer.MAX_VALUE, 1));
		Dimension dims = new Dimension(90,25);
		integerValueSpinner.setMaximumSize(dims);
		integerValueSpinner.setMinimumSize(dims);
		integerValueSpinner.setPreferredSize(dims);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		outputValuePanel.add(integerValueSpinner, gbc);
		
		Set<String> constants = model.getConstantNames();
		constantDropDown = new JComboBox(constants.toArray());
		dims = new Dimension(150,25);
		constantDropDown.setMaximumSize(dims);
		constantDropDown.setMinimumSize(dims);
		constantDropDown.setPreferredSize(dims);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		outputValuePanel.add(constantDropDown, gbc);
		
		if(constants.isEmpty()){
			constantRadioButton.setEnabled(false);
			enableNormalControls();
		}
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(outputValuePanel, gbc);
	}
	
	private void enableNormalControls() {
		integerValueSpinner.setEnabled(true);
		constantDropDown.setEnabled(false);
	}

	private void enableConstantControls() {
		constantDropDown.setEnabled(true);
		integerValueSpinner.setEnabled(false);
		
	}
	
	public void exit(){
		rootPane.getParent().setVisible(false);
	}
}
