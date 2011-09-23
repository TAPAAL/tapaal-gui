package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		setLayout(new BorderLayout());
		
		JPanel namePanel = createNamePanel();
		JPanel buttonPanel = createButtonPanel();
		
		add(namePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	private JPanel createNamePanel() {
		JPanel namePanel = new JPanel(new GridBagLayout());
		
		JLabel label = new JLabel("Please enter a name:");
		GridBagConstraints gbc = new GridBagConstraints();
		namePanel.add(label, gbc);
		
		String initialText = (placeToEdit == null) ? "" : placeToEdit.name();
		nameField = new JTextField(initialText);
		nameField.setMinimumSize(new Dimension(100,27));
		nameField.setPreferredSize(new Dimension(150, 27));
		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		namePanel.add(nameField, gbc);
		return namePanel;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		
		JButton okButton = new JButton("OK");
		rootPane.setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				String name = nameField.getText();
				if(name == null || name.isEmpty()){
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "You must specify a name.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}else{
					boolean success = false;
					if(placeToEdit == null){
						success = addNewSharedPlace(name);
					}else{
						success = updateExistingPlace(name);
					}
					
					if(success){
						context.nameGenerator().updateIndicesForAllModels(name);
						exit();
					}
				}
			}

			private boolean updateExistingPlace(String name) {
				if(placeToEdit.network().isNameUsed(name)) {
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "The specified name is already used by a place or transition in one of the components.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				String oldName = placeToEdit.name();
				try{
					placeToEdit.setName(name);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "The specified name is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
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
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "The specified name is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				try{
					listModel.addElement(place);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedPlaceNamePanel.this, "A transition or place with the specified name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				context.undoManager().addNewEdit(new AddSharedPlaceCommand(listModel, place));
				return true;
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		return buttonPanel;
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

}

