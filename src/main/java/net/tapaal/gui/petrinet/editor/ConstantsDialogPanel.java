package net.tapaal.gui.petrinet.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import net.tapaal.swinghelpers.CustomJSpinner;
import net.tapaal.swinghelpers.RequestFocusListener;
import net.tapaal.swinghelpers.SwingHelper;
import pipe.gui.TAPAALGUI;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.swingcomponents.EscapableDialog;

public class ConstantsDialogPanel extends javax.swing.JPanel {

    private final TimedArcPetriNetNetwork model;
	private int lowerBound;
	private int upperBound;
	private int initialValue = 0;
	private EscapableDialog dialog;

    JTextField nameTextField;
    JLabel nameLabel;
    JLabel valueLabel;
	CustomJSpinner valueSpinner;
	JPanel container;
	JPanel buttonContainer;
	JButton okButton;
	JButton cancelButton;

	private final String oldName;

    public ConstantsDialogPanel(TimedArcPetriNetNetwork model, Constant constant) {
        this.model = model;

        if (constant != null) {
            initialValue = constant.value();
            oldName = constant.name();
            lowerBound = constant.lowerBound();
            upperBound = constant.upperBound();
        } else {
            oldName = "";
        }
		initComponents();

		nameTextField.setText(oldName);
	}

	public void showDialog() {
		dialog = new EscapableDialog(TAPAALGUI.getApp(), "Edit Constant", true);
		dialog.add(container);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void initComponents() {
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		nameTextField = new javax.swing.JTextField();
        SwingHelper.setPreferredWidth(nameTextField,330);
		nameTextField.addAncestorListener(new RequestFocusListener());
		nameTextField.addActionListener(e -> {
			okButton.requestFocusInWindow();
			okButton.doClick();
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
		okButton.addActionListener(e -> onOK());
		
		cancelButton.addActionListener(e -> exit());
		
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
					TAPAALGUI.getApp(),
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
			JOptionPane.showMessageDialog(
					TAPAALGUI.getApp(),
					"Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
					"Error", JOptionPane.ERROR_MESSAGE);
			nameTextField.requestFocusInWindow();
			return;
		}

        if (model.isNameUsedForColorType(newName) || model.isNameUsedForVariable(newName) || model.isNameUsedForColor(newName, null)) {
            JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "There is already another Color, Color Type or Variable with the same name.\n\n"
                        + "Choose a different name for the constant.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            nameTextField.requestFocusInWindow();
            return;
        }

		if (newName.trim().isEmpty()) {
			JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
					"You must specify a name.", "Missing name",
					JOptionPane.ERROR_MESSAGE);
			nameTextField.requestFocusInWindow();
			return;				
		} else {				
			int val = (Integer) valueSpinner.getValue();
			if (!oldName.equals("")) {

				if (!oldName.equals(newName) && model.isNameUsedForConstant(newName)) {
					JOptionPane.showMessageDialog(
							TAPAALGUI.getApp(),
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
							TAPAALGUI.getApp(),
							"The specified value is invalid for the current net.\n"
							+ "Updating the constant to the specified value invalidates the guard\n"
							+ "on one or more arcs, or it sets the weight of an arc to 0.",
							"Constant value invalid for current net",
							JOptionPane.ERROR_MESSAGE);
					valueSpinner.requestFocusInWindow();
					return;
				}
				Command edit = model.updateConstant(oldName, new Constant(newName, val));
				if (edit == null) {
					JOptionPane
					.showMessageDialog(
							TAPAALGUI.getApp(),
							"The specified value is invalid for the current net.\n"
							+ "Updating the constant to the specified value invalidates the guard\n"
							+ "on one or more arcs, or it sets the weight of an arc to 0.",
							"Constant value invalid for current net",
							JOptionPane.ERROR_MESSAGE);
					valueSpinner.requestFocusInWindow();
					return;
				} else {
					TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(edit);
					TAPAALGUI.getCurrentTab().drawingSurface().repaintAll();
					exit();
				}
			} else {
				Command edit = model.addConstant(newName, val);
				
				if (edit==null) {
					JOptionPane.showMessageDialog(
							TAPAALGUI.getApp(),
							"A constant with the specified name already exists.",
							"Constant exists",
							JOptionPane.ERROR_MESSAGE);
					nameTextField.requestFocusInWindow();
					return;
				} else {
                    TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(edit);
                }
				exit();
			}
			model.buildConstraints();
		}		
	}
}

