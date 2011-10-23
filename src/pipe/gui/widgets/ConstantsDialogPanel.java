package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

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
	
	JPanel nameTextFieldPane;
	JTextField nameTextField;
	Dimension size;
	JLabel nameLabel;  
	JPanel valueSpinnerPane;
	JLabel valueLabel; 	
	JSpinner valueSpinner;
	JPanel container;
	
	private String oldName;

	/** Creates new form LeftConstantsPane */

	public ConstantsDialogPanel() {
		initComponents();
		
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model) {
		initComponents();

		rootPane = pane;
		this.model = model;

		oldName = "";

		// Set up initial values
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		valueSpinner.setModel(spinnerModel);
		setupValueEditor();

		nameTextField.setText(oldName);
	}

	private void setupValueEditor() {
		valueSpinner.setEditor(new JSpinner.NumberEditor(valueSpinner));
		
		// Disable nonnumeric keys in value spinner
		JFormattedTextField txt = ((JSpinner.NumberEditor) valueSpinner.getEditor()).getTextField();
		((NumberFormatter) txt.getFormatter()).setAllowsInvalid(false);
		
		//txt.setMaximumSize(new Dimension(60,25));//why does none of these 3 work??
		//valueSpinner.getEditor().setSize(new Dimension(60,25));
		//valueSpinner.getEditor().setMaximumSize(new Dimension(60,25));
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model,
			Constant constant) {
		this(pane, model);

		oldName = constant.name();
		lowerBound = constant.lowerBound();
		upperBound = constant.upperBound();

		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(constant
				.value(), 0, constant.upperBound(), 1);
		valueSpinner.setModel(spinnerModel);
		setupValueEditor();
		nameTextField.setText(oldName);
	}
	
	public void showDialog() {
		Integer constantWasConfirmed = new Integer(2); //cancel = 2, ok = 0, close window = -1		
		constantWasConfirmed = JOptionPane.showConfirmDialog(
				null, container, "Edit Constant",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if (constantWasConfirmed == 2 )
			return;
		else if (constantWasConfirmed == -1 )
			return;
		else if (constantWasConfirmed == 0)
		{			
			String newName = nameTextField.getText();
			
			if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)) {
				System.err
				.println("Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z]*");
				JOptionPane
				.showMessageDialog(
						CreateGui.getApp(),
						"Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (newName.trim().isEmpty()) {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"You must specify a name.", "Missing name",
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				int val = (Integer) valueSpinner.getValue();

				if (!oldName.equals("")) {
					if (!oldName.equals(newName)
							&& model.isConstantNameUsed(newName)) {
						JOptionPane
						.showMessageDialog(
								CreateGui.getApp(),
								"There is already another constant with the same name.\n\n"
								+ "Choose a different name for the constant.",
								"Error", JOptionPane.ERROR_MESSAGE);
						//nameTextField.setText(oldName);
						return;
					}
					//Kyrke - This is messy, but a quck fix for bug #815487			
					//Check that the value is within the allowed bounds
					if (!( lowerBound <= val && val <= upperBound )){
						JOptionPane.showMessageDialog(
								CreateGui.getApp(),
								"The specified value is invalid for the current net.\n"
								+ "Updating the constant to the specified value invalidates the guard\n"
								+ "on one or more arcs.",
								"Constant value invalid for current net",
								JOptionPane.ERROR_MESSAGE);
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
								+ "on one or more arcs.",
								"Constant value invalid for current net",
								JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						CreateGui.getCurrentTab().drawingSurface().getUndoManager()
						.addNewEdit(edit);
						CreateGui.getCurrentTab().drawingSurface().repaintAll();
					}
				} else {
					Command edit = model.addConstant(newName, val);
					if (edit == null) {
						JOptionPane
						.showMessageDialog(
								CreateGui.getApp(),
								"A constant with the specified name already exists.",
								"Constant exists",
								JOptionPane.ERROR_MESSAGE);
						return;
					} else
						CreateGui.getView().getUndoManager().addNewEdit(edit);
				}
				model.buildConstraints();
			}
		}
	}
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	
	private void initComponents() {
		nameTextFieldPane = new JPanel();
		nameTextField = new javax.swing.JTextField();
		size = new Dimension(200, 25);
		nameTextField.setPreferredSize(size);
		nameTextField.setMinimumSize(size);
		nameLabel = new JLabel(); 
		nameLabel.setText("Name:");
		nameTextFieldPane.add(nameLabel);
		nameTextFieldPane.add(nameTextField);
		//create value spinner component for choosing value 
		valueSpinnerPane = new JPanel();
		valueLabel = new javax.swing.JLabel(); 
		valueLabel.setText("Value:");
		valueSpinner = new javax.swing.JSpinner();
		valueSpinner.setMaximumSize(new Dimension(60,25));
		valueSpinnerPane.add(valueLabel);
		valueSpinnerPane.add(valueSpinner);
		//add components to containing pane
		container = new JPanel();
		container.add(nameTextFieldPane);
		container.add(valueSpinnerPane);
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setAlignmentX(LEFT_ALIGNMENT);		
	}
}
