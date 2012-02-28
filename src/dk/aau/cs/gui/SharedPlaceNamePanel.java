package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.TCTL.visitors.RenameSharedPlaceVisitor;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.gui.undo.AddSharedPlaceCommand;
import dk.aau.cs.gui.undo.RenameSharedPlaceCommand;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.util.RequireException;

public class SharedPlaceNamePanel extends JPanel {
	private static final long serialVersionUID = -8099814326394422263L;

	private final JRootPane rootPane;
	private final SharedPlacesListModel listModel;
	private JTextField nameField;
	private SharedPlace placeToEdit;
	private final Context context;
	
	JButton okButton;

	public SharedPlaceNamePanel(JRootPane rootPane, SharedPlacesListModel sharedPlacesListModel, Context context) {
		this(rootPane, sharedPlacesListModel, context, null);	
	}
	
	public SharedPlaceNamePanel(JRootPane rootPane, SharedPlacesListModel sharedPlacesListModel, Context context, SharedPlace placeToEdit) {
		this.rootPane = rootPane;
		listModel = sharedPlacesListModel;
		this.placeToEdit = placeToEdit;
		this.context = context;
		initComponents();		
	}
	
	public void initComponents(){
		setLayout(new GridBagLayout());
		
		JPanel namePanel = createNamePanel();
		JPanel buttonPanel = createButtonPanel();
		
		GridBagConstraints gbcNamePanel = new GridBagConstraints();
		gbcNamePanel.insets = new Insets(4, 4, 2, 4);
		gbcNamePanel.gridx = 0;
		gbcNamePanel.gridy = 0;
		gbcNamePanel.gridwidth = 1;
		gbcNamePanel.anchor = GridBagConstraints.EAST;			
		add(namePanel, gbcNamePanel);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		add(buttonPanel, gbc);
	}

	private JPanel createNamePanel() {
		JPanel namePanel = new JPanel(new GridBagLayout());
		
		JLabel label = new JLabel("Enter a shared place name:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 4, 2, 4);
		namePanel.add(label, gbc);
		
		String initialText = (placeToEdit == null) ? "" : placeToEdit.name();
		nameField = new JTextField(initialText);
		nameField.setMinimumSize(new Dimension(330, 25));
		nameField.setPreferredSize(new Dimension(330, 25));
		nameField.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				okButton.requestFocusInWindow();
				okButton.doClick();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.insets = new Insets(4, 4, 2, 4);
		namePanel.add(nameField, gbc);
		return namePanel;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		
		okButton = new JButton("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		
		okButton.setMnemonic(KeyEvent.VK_O);
		GridBagConstraints gbcOk = new GridBagConstraints();		
		gbcOk.gridx = 1;
		gbcOk.gridy = 0;
		gbcOk.anchor = java.awt.GridBagConstraints.WEST;
		gbcOk.insets = new java.awt.Insets(5, 5, 5, 5);


	//	rootPane.setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				String name = nameField.getText();
				if(name == null || name.isEmpty()){
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "You must specify a name.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return;
				}else{
					boolean success = false;
					if(placeToEdit == null){
						success = addNewSharedPlace(name);
					}else{
						if (!name.equals(placeToEdit.name())){ //Name is different
							success = updateExistingPlace(name);
						}else {
							success = true;
						}
					}

					if(success){
						context.nameGenerator().updateIndicesForAllModels(name);
						exit();
					}

				}
			}

			private boolean updateExistingPlace(String name) {
				String oldName = placeToEdit.name();
				
				if(placeToEdit.network().isNameUsed(name) && !oldName.equalsIgnoreCase(name)) {
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "The specified name is already used by a place or transition in one of the components.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
				
				
				try{
					placeToEdit.setName(name);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "The specified name is invalid.\nAcceptable names are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]* \n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
				
				for(TAPNQuery query : context.queries()){
					query.getProperty().accept(new RenameSharedPlaceVisitor(oldName, name), null);
				}
				
				listModel.updatedName();
				context.undoManager().addNewEdit(new RenameSharedPlaceCommand(placeToEdit, listModel, context.tabContent(), oldName, name));
				return true;
			}

			private boolean addNewSharedPlace(String name) {
				SharedPlace place = null;
				try{
					place = new SharedPlace(name);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "The specified name is invalid.\nAcceptable names are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]* \n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
				
				try{
					listModel.addElement(place);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "A transition or place with the specified name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
				
				context.undoManager().addNewEdit(new AddSharedPlaceCommand(listModel, place));
				return true;
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		
		cancelButton.setMnemonic(KeyEvent.VK_C);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;		

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		buttonPanel.add(cancelButton,gbc);
		buttonPanel.add(okButton,gbcOk);
		
		return buttonPanel;
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

}

