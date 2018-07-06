package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

/*
 * LeftConstantsPane.java
 *
 * Created on 08-10-2009, 13:51:42
 */

/**
 * 
 * @author Morten Jacobsen
 */
public class ConstantsDialogPanel extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6734583459331431789L;
	private JRootPane rootPane;
	private TimedArcPetriNetNetwork model;
	private int lowerBound;
	private int upperBound;
	private int initialValue = 0;
	private EscapableDialog dialog;

	JPanel nameTextFieldPane;
	JTextField nameTextField;
	Dimension size;
	JLabel nameLabel;  
	JPanel valueSpinnerPane;
	JLabel valueLabel; 	
	CustomJSpinner valueSpinner;
	JPanel container;
	JPanel buttonContainer;
	JButton okButton;
	JButton cancelButton;

	private String oldName;

	public ConstantsDialogPanel() throws IOException {
		initComponents();		
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model) throws IOException {
		initComponents();
		rootPane = pane;
		this.model = model;		
		oldName = "";
		nameTextField.setText(oldName);
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model,
			Constant constant) throws IOException {		
		rootPane = pane;
		this.model = model;	

		initialValue = constant.value();		
		initComponents();
		
		oldName = constant.name();
		lowerBound = constant.lowerBound();
		upperBound = constant.upperBound();		 
		nameTextField.setText(oldName);
	}

	public void showDialog() {
		dialog = new EscapableDialog(CreateGui.getApp(),
				"Edit Constant", true);
		dialog.add(container);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void initComponents() throws IOException {						
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		size = new Dimension(330, 25);
		nameTextField = new javax.swing.JTextField();	
		nameTextField.setPreferredSize(size);
		nameTextField.addAncestorListener(new RequestFocusListener());
		nameTextField.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				okButton.requestFocusInWindow();
				okButton.doClick();
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 2, 4);
		container.add(nameTextField,gbc);

		nameLabel = new JLabel(); 
		nameLabel.setText("Name: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(4, 4, 2, 4);
		gbc.anchor = GridBagConstraints.WEST;
		container.add(nameLabel,gbc);

		valueLabel = new javax.swing.JLabel(); 
		valueLabel.setText("Value: ");
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 4, 2, 4);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(valueLabel,gbc);		
				
		buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridBagLayout());

		okButton = new JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.setMnemonic(KeyEvent.VK_O);
		gbc = new GridBagConstraints();		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.insets = new java.awt.Insets(5, 5, 5, 5);
		buttonContainer.add(okButton,gbc);
		
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.setMnemonic(KeyEvent.VK_C);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		buttonContainer.add(cancelButton,gbc);		
		
		//initialize valueSpinner		
		valueSpinner = new CustomJSpinner(initialValue, okButton);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 4, 2, 4);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		container.add(valueSpinner,gbc);
		
		//add action listeners for buttons
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		//add button container
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		container.add(buttonContainer,gbc);
	}
	
	private void exit() {
		dialog.setVisible(false);
	}

	private void onOK() {
		if (((JSpinner.NumberEditor)valueSpinner.getEditor()).getTextField().getText().equals("")){
			JOptionPane.showMessageDialog(
					CreateGui.getApp(),
					"The specified value is invalid for the current net.\n"
					+ "Updating the constant to the specified value invalidates the guard\n"
					+ "on one or more arcs, or it sets the weight of an arc to 0.",
					"Constant value invalid for current net",
					JOptionPane.ERROR_MESSAGE);
			valueSpinner.requestFocusInWindow();
			return;
		}
		String newName = nameTextField.getText();

		if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)) {
			System.err
			.println("Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z]*");
			JOptionPane
			.showMessageDialog(
					CreateGui.getApp(),
					"Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
					"Error", JOptionPane.ERROR_MESSAGE);
			nameTextField.requestFocusInWindow();
			return;
		}

		if (newName.trim().isEmpty()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"You must specify a name.", "Missing name",
					JOptionPane.ERROR_MESSAGE);
			nameTextField.requestFocusInWindow();
			return;				
		} else {				
			int val = (Integer) valueSpinner.getValue();
			if (!oldName.equals("")) {
				if (!oldName.equalsIgnoreCase(newName)
						&& model.isConstantNameUsed(newName)) {
					JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"There is already another constant with the same name.\n\n"
							+ "Choose a different name for the constant.",
							"Error", JOptionPane.ERROR_MESSAGE);
					nameTextField.requestFocusInWindow();
					return;
				}
				//Kyrke - This is messy, but a quck fix for bug #815487			
				//Check that the value is within the allowed bounds
				if (!( lowerBound <= val && val <= upperBound )){
					JOptionPane.showMessageDialog(
							CreateGui.getApp(),
							"The specified value is invalid for the current net.\n"
							+ "Updating the constant to the specified value invalidates the guard\n"
							+ "on one or more arcs, or it sets the weight of an arc to 0.",
							"Constant value invalid for current net",
							JOptionPane.ERROR_MESSAGE);
					valueSpinner.requestFocusInWindow();
					return;
				}
				Command edit = model.updateConstant(oldName, new Constant(
						newName, val));
				if (edit == null) {
					JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"The specified value is invalid for the current net.\n"
							+ "Updating the constant to the specified value invalidates the guard\n"
							+ "on one or more arcs, or it sets the weight of an arc to 0.",
							"Constant value invalid for current net",
							JOptionPane.ERROR_MESSAGE);
					valueSpinner.requestFocusInWindow();
					return;
				} else {
					CreateGui.getCurrentTab().drawingSurface().getUndoManager()
					.addNewEdit(edit);
					CreateGui.getCurrentTab().drawingSurface().repaintAll();
					exit();
				}
			} else {
				Command edit = model.addConstant(newName, val);
				
				if (edit==null) {
					JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"A constant with the specified name already exists.",
							"Constant exists",
							JOptionPane.ERROR_MESSAGE);
					nameTextField.requestFocusInWindow();
					return;
				} else
					CreateGui.getDrawingSurface().getUndoManager().addNewEdit(edit);
				exit();
			}
			model.buildConstraints();
		}		
	}
}

