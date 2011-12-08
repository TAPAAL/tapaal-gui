package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.NumberFormatter;
import javax.swing.text.DocumentFilter.FilterBypass;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.io.ResourceManager;
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
	JSpinner valueSpinner;
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
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private JSpinner makeDigitsOnlySpinnerUsingDocumentFilter(Integer value) {
		JSpinner spinner = new JSpinner();
		JSpinner.NumberEditor jsEditor =
			(JSpinner.NumberEditor)spinner.getEditor();

		JFormattedTextField textField = jsEditor.getTextField();
		final DocumentFilter digitOnlyFilter = new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				//this method is only called by programatic editing of the textbox! 
			}

			@Override
			public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {                 
				String old = fb.getDocument().getText(0, fb.getDocument().getLength());
				StringBuffer newString = new StringBuffer(old);
				newString.replace(offset, length+offset, "");
				if (stringIsNumber(newString.toString())) {
					super.remove(fb, offset, length);
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				String old = fb.getDocument().getText(0, fb.getDocument().getLength());
				StringBuffer newString = new StringBuffer(old);
				newString.replace(offset, length+offset, text);            	 			
				if (stringIsNumber(newString.toString())) {                	
					super.replace(fb, offset, length, text, attrs);
				}
			}

			private boolean stringIsNumber(String text) {
				if (Pattern.matches("^([1-9]([0-9])*)?|0$",text))
					return true;
				return false;
			}
		};

		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);
		textField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				okButton.requestFocusInWindow();
				okButton.doClick();			}
		});
		textField.setFormatterFactory(new DefaultFormatterFactory(
				new InternationalFormatter(format){             
					private static final long serialVersionUID = 1L;
					@Override
					protected DocumentFilter getDocumentFilter() {
						return digitOnlyFilter;
					}
				}));
		spinner.setValue(value);
		spinner.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				//if currentvalue is -1, make it 0:
					if (((JSpinner)e.getSource()).getValue() instanceof Long) {
						if (((Long)((JSpinner)e.getSource()).getValue()) == -1l) {
							((JSpinner)e.getSource()).setValue(new Integer(0));
						}        	    			
					}
			}
		});
		return spinner;
	}

	private void initComponents() throws IOException {		
		valueSpinner =  makeDigitsOnlySpinnerUsingDocumentFilter(initialValue);
		valueSpinner.setPreferredSize(new Dimension(100, 25));
		valueSpinner.setMaximumSize(new Dimension(100, 25));
		valueSpinner.setMinimumSize(new Dimension(100, 25));
		
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		size = new Dimension(330, 25);

		nameTextField = new javax.swing.JTextField();	
		nameTextField.setPreferredSize(size);
		nameTextField.addAncestorListener(new RequestFocusListener());
		nameTextField.addActionListener(new ActionListener() {
			
			@Override
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

		gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 4, 2, 4);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		container.add(valueSpinner,gbc);
				
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
					+ "on one or more arcs.",
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
			//if you are not carefull you get a class cast exception. Apparantly the spinner returns type long or int,
			//depending on whether the value is 0 or different from 0.
			int val;				
			if (valueSpinner.getValue() instanceof Long) {
				val = (int)((Long) valueSpinner.getValue()).longValue();
			}
			else {
				val = (Integer) valueSpinner.getValue();
			}
			if (!oldName.equals("")) {
				if (!oldName.equals(newName)
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
							+ "on one or more arcs.",
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
							+ "on one or more arcs.",
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
				if (edit == null) {
					JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"A constant with the specified name already exists.",
							"Constant exists",
							JOptionPane.ERROR_MESSAGE);
					nameTextField.requestFocusInWindow();
					return;
				} else
					CreateGui.getView().getUndoManager().addNewEdit(edit);
				exit();
			}
			model.buildConstraints();
		}		
	}
}

