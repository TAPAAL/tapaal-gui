package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
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
	private boolean autoEdited;
	//private String currentTextValueOfSpinner;
	//private boolean spinnerTextIsEmptyString;

	/** Creates new form LeftConstantsPane */

	public ConstantsDialogPanel() {
		initComponents();
		
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model) {
		initComponents();

		rootPane = pane;
		this.model = model;

		oldName = "";
		//currentTextValueOfSpinner = "0";
		//spinnerTextIsEmptyString = false;
		
		// Set up initial values
		//SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		//valueSpinner.setModel(spinnerModel);
		//((JSpinner.NumberEditor) valueSpinner.getEditor()).getTextField().setText("");
		nameTextField.setText(oldName);
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model,
			Constant constant) {
		this(pane, model);

		oldName = constant.name();
		lowerBound = constant.lowerBound();
		upperBound = constant.upperBound();

		//SpinnerNumberModel spinnerModel = new SpinnerNumberModel(constant
			//	.value(), 0, constant.upperBound(), 1);
		//valueSpinner.setModel(spinnerModel);
		//((JSpinner.NumberEditor) valueSpinner.getEditor()).getTextField().setText("");
		//((JSpinner.NumberEditor) valueSpinner.getEditor()).set
		 
		nameTextField.setText(oldName);
		//currentTextValueOfSpinner = "0";
		//spinnerTextIsEmptyString = false;
	}
	
	public void showDialog() {
		autoEdited = false;
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
				showDialog();
				return;
			}

			if (newName.trim().isEmpty()) {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"You must specify a name.", "Missing name",
						JOptionPane.ERROR_MESSAGE);
				showDialog();
				return;				
			} else {
				 
				//check for empty string in value field
				//if show error message; showDialog(); and return;
				if (autoEdited){
					JOptionPane.showMessageDialog(CreateGui.getApp(),
							"You must specify a value.", "Missing value",
							JOptionPane.ERROR_MESSAGE);
					showDialog();
					return;
				}
				//System.out.println("test");
				//System.out.println(valueSpinner.getValue());
				//if you are not carefull you get a class cast exception. Apparantly the spinner returns type long!
				//if valuespinner.getvalue is of not of typew int:
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
						showDialog();
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
						showDialog();
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
						showDialog();
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
						showDialog();
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
//    class spinnerDocumentFilter extends DocumentFilter {
//
//		@Override
//		public void insertString(FilterBypass fb, int offset, String string,
//				AttributeSet attr) throws BadLocationException {
//			// TODO Auto-generated method stub
//			super.insertString(fb, offset, string, attr);
//			System.out.println("insert, string: "+ string+", offset: "+offset+
//					", fb.doc: "+fb.getDocument().getText(0,fb.getDocument().getLength()));
//		}
//
//		@Override
//		public void remove(FilterBypass fb, int offset, int length)
//				throws BadLocationException {
//			// TODO Auto-generated method stub
//			super.remove(fb, offset, length);
//			System.out.println("remove, offset: "+offset+"length: "+length);
//		}
//
//		@Override
//		public void replace(FilterBypass fb, int offset, int length,
//				String text, AttributeSet attrs) throws BadLocationException {
//			// TODO Auto-generated method stub
//			super.replace(fb, offset, length, text, attrs);
//			System.out.println("replace, string: "+text+", offset: "+offset+", length"+length);
//		}
//    	
//    }
	 private JSpinner makeDigitsOnlySpinnerUsingDocumentFilter() {
         //JSpinner spinner = new JSpinner(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
         //JSpinner spinner = new JSpinner(new SpinnerNumberModel());
		 JSpinner spinner = new JSpinner();
		 //spinner.
         JSpinner.NumberEditor jsEditor =
             (JSpinner.NumberEditor)spinner.getEditor();

         JFormattedTextField textField = jsEditor.getTextField();
         final DocumentFilter digitOnlyFilter = new DocumentFilter() {
             @Override
             public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            	 //this method is only called by programatic editing of the textbox! 
//                 if (stringIsNumber(fb.getDocument().getText(0, fb.getDocument().getLength()))) {
                 //    super.insertString(fb, offset, string, attr);
//                 }
//                 System.out.println("inserting..");
//                 System.out.println("fb: "+fb.getDocument().getText(0, fb.getDocument().getLength()));
//                 System.out.println("offset: "+offset);
//                 System.out.println("string: "+string);
             }

             @Override
             public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {                 
            	 System.out.println("fb: "+fb.getDocument().getText(0, fb.getDocument().getLength()));
            	 //System.out.println(stringIsNumber(fb.getDocument().getText(0, fb.getDocument().getLength())));
            	 String old = fb.getDocument().getText(0, fb.getDocument().getLength());
            	 //make new string
//            	 System.out.println("offset: "+offset);
//                 System.out.println("string: "+text);
//                 System.out.println("length: "+length);
            	 //text.getChars(0, text.length(), old.toCharArray(), offset);
            	 StringBuffer newString = new StringBuffer(old);
            	 newString.replace(offset, length+offset, "");
            	 
            	 System.out.println("newlycomposed: "+newString);
            	 if (stringIsNumber(newString.toString())) {
                	 super.remove(fb, offset, length);
                 }
                
//                 System.out.println("removing..");
//                 System.out.println("fb: "+fb.getDocument().getText(0, fb.getDocument().getLength()));
//                 System.out.println("offset: "+offset);
//                 System.out.println("length: "+length);
             }

             @Override
             public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            	 System.out.println("fb: "+fb.getDocument().getText(0, fb.getDocument().getLength()));
            	 //System.out.println(stringIsNumber(fb.getDocument().getText(0, fb.getDocument().getLength())));
            	 String old = fb.getDocument().getText(0, fb.getDocument().getLength());
            	 //make new string
//            	 System.out.println("offset: "+offset);
//                 System.out.println("string: "+text);
//                 System.out.println("length: "+length);
            	 //text.getChars(0, text.length(), old.toCharArray(), offset);
            	 StringBuffer newString = new StringBuffer(old);
            	 newString.replace(offset, length+offset, text);
            	 
            	 System.out.println("newlycomposed: "+newString);
            	 if (stringIsNumber(newString.toString())) {                	
                     super.replace(fb, offset, length, text, attrs);
                 }
                     
                     //System.out.println("replacing..");
                 //System.out.println("fb: "+fb.getDocument().getText(0, fb.getDocument().getLength()));
//                 System.out.println("offset: "+offset);
//                 System.out.println("string: "+text);
//                 System.out.println("length: "+length);
             }

             private boolean stringIsNumber(String text) {
                 //for (int i = 0; i<text.length(); i++) {
                  //   if (!Character.isDigit(text.charAt(i))) {
                   //      return false;
                    // }
                 //}
                 if (Pattern.matches("^([1-9]([0-9])*)?|0$",text))
                	 return true;
                 return false;
             }
         };

         NumberFormat format = NumberFormat.getIntegerInstance();
         format.setGroupingUsed(false);
         textField.setFormatterFactory(new DefaultFormatterFactory(
        		 new InternationalFormatter(format){             
        			 private static final long serialVersionUID = 1L;
        			 @Override
        			 protected DocumentFilter getDocumentFilter() {
        				 return digitOnlyFilter;
        			 }
        		 }));
         textField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
//				if (!valueSpinner.isShowing()) {
				if (!valueSpinner.isDisplayable()) {
					System.out.println("autoedit..");
					autoEdited = true;
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				System.out.println("something was inserted..");
//				if (!valueSpinner.isShowing()) {
				if (!valueSpinner.isDisplayable()) {
					System.out.println("autoedit..");
					autoEdited = true;
				}
//				try {
//					System.out.println("doc: "+e.getDocument().getText(0, e.getDocument().getLength()));
//				} catch (BadLocationException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				System.out.println("changhed called");
			}
		});
         spinner.addChangeListener(new javax.swing.event.ChangeListener() {
        	    public void stateChanged(javax.swing.event.ChangeEvent e) {
        	    	//if currentvalue is -1, make it 0:
        	    	if (((JSpinner)e.getSource()).getValue() instanceof Long) {
        	    		System.out.println("was long");
        	    		if (((Long)((JSpinner)e.getSource()).getValue()) == -1l) {
        	    			System.out.println("was minus 1");
        	    			((JSpinner)e.getSource()).setValue(new Integer(0));
        	    		}
        	    			
        	    	}
        	    }
         	});
         return spinner;
     }
	
	private void initComponents() {
		valueSpinner =  makeDigitsOnlySpinnerUsingDocumentFilter();
//		valueSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
//		valueSpinner.setEditor(new JSpinner.NumberEditor(valueSpinner));
//		((NumberFormatter)(((JSpinner.DefaultEditor)valueSpinner.getEditor()).getTextField().getFormatter())).setAllowsInvalid(false);
		
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		size = new Dimension(250, 25);
		
		nameTextField = new javax.swing.JTextField();	
		nameTextField.setPreferredSize(size);
		nameTextField.addAncestorListener(new RequestFocusListener());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		container.add(nameTextField,gbc);
		
		nameLabel = new JLabel(); 
		nameLabel.setText("Name: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(nameLabel,gbc);

		valueLabel = new javax.swing.JLabel(); 
		valueLabel.setText("Value: ");
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 0, 2, 0);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(valueLabel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 0, 2, 0);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		container.add(valueSpinner,gbc);
		
		
		//((JTextField)((JSpinner.DefaultEditor)valueSpinner.getEditor()).getTextField()).setEditable(false);
		//action listeners:
		//up and down button
		//enter text to value box?
		//JTextField txt = new JTextField(); 
		//valueSpinner.setEditor(txt);
		//((AbstractDocument)txt.getDocument()).setDocumentFilter(new spinnerDocumentFilter());
		//((AbstractDocument)nameTextField.getDocument()).setDocumentFilter(new spinnerDocumentFilter());
		//((AbstractDocument)((JTextField)((JSpinner.DefaultEditor)valueSpinner.getEditor()).getTextField()).getDocument()).setDocumentFilter(new spinnerDocumentFilter());
		//((JTextField)((JSpinner.DefaultEditor)valueSpinner.getEditor()).getTextField()).getDocument().addDocumentListener(new DocumentListener() {
			  //public void changedUpdate(DocumentEvent e) {
			    //warn();
				//  System.out.println("changed..");				  
			  //}
			  //public void removeUpdate(DocumentEvent e) {
//			    //warn();
//				  //System.out.println("removeupdated..");
//				  try {
////					 System.out.println("value is: "+
////					e.getDocument().getText(0, e.getDocument().getLength())+".");
//					 if (!Pattern.matches("([1-9]([0-9])*)|([0])", e.getDocument().getText(0, e.getDocument().getLength()))
//							 && !e.getDocument().getText(0, e.getDocument().getLength()).isEmpty()) {
//						  //accepted = false;
//						((JSpinner.NumberEditor) valueSpinner.getEditor()).getTextField().setText(currentTextValueOfSpinner);
//					  } else {
//						currentTextValueOfSpinner = e.getDocument().getText(0, e.getDocument().getLength());
//					  }
//				} catch (BadLocationException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
			  //}
			  //public void insertUpdate(DocumentEvent e) {
//			    //warn();
//				  //System.out.println("insertUpdated..");
//				  //System.out.println(valueSpinner.isShowing());
//				  try {
//					  //System.out.println("value is: "+
//					//e.getDocument().getText(0, e.getDocument().getLength())+".");
//					  //boolean accepted = true;
//					  if (e.getDocument().getText(0, e.getDocument().getLength()) == "0") {
//						  if (!valueSpinner.isShowing())
//							  if (currentTextValueOfSpinner != "0")
//								  return;
//					  }
//					  if (!Pattern.matches("([1-9]([0-9])*)|([0])", e.getDocument().getText(0, e.getDocument().getLength()))) {
//						  //accepted = false;
//						((JSpinner.NumberEditor) valueSpinner.getEditor()).getTextField().setText(currentTextValueOfSpinner);
//					  } else {
//						currentTextValueOfSpinner = e.getDocument().getText(0, e.getDocument().getLength());
//					  }
//					  //System.out.println("accepted: "+accepted);
//				} catch (BadLocationException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
			  //}

//			  public void warn() {
//			     if (Integer.parseInt(textField.getText())<=0){
//			       JOptionPane.showMessageDialog(null,
//			          "Error: Please enter number bigger than 0", "Error Massage",
//			          JOptionPane.ERROR_MESSAGE);
//			     }
//			  }
			//});
	}
}

