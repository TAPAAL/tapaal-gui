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

import pipe.gui.undo.UndoManager;
import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.TCTL.visitors.RenameSharedTransitionVisitor;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.gui.undo.AddSharedTransitionCommand;
import dk.aau.cs.gui.undo.RenameSharedTransitionCommand;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.util.RequireException;

public class SharedTransitionNamePanel extends JPanel {
	private static final long serialVersionUID = -8099814326394422263L;

	private final JRootPane rootPane;
	private final SharedTransitionsListModel listModel;
	private JTextField nameField;
	private SharedTransition transitionToEdit;

	private final UndoManager undoManager;
	private final NameGenerator nameGenerator;
        private final Context context;
	
	JButton okButton;

	public SharedTransitionNamePanel(JRootPane rootPane, SharedTransitionsListModel sharedTransitionsListModel, UndoManager undoManager, NameGenerator nameGenerator, Context context) {
		this(rootPane, sharedTransitionsListModel, undoManager, nameGenerator, context, null);
	}
	
	public SharedTransitionNamePanel(JRootPane rootPane, SharedTransitionsListModel sharedTransitionsListModel, UndoManager undoManager, NameGenerator nameGenerator, Context context, SharedTransition transitionToEdit) {
		this.rootPane = rootPane;
		listModel = sharedTransitionsListModel;
		this.undoManager = undoManager;
		this.nameGenerator = nameGenerator;
		this.transitionToEdit = transitionToEdit;
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
		
		JLabel label = new JLabel("Enter a shared transition name:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 4, 2, 4);
		namePanel.add(label, gbc);
		
		String initialText = (transitionToEdit == null) ? "" : transitionToEdit.name();
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
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "You must specify a name.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return;
				}else{
					boolean success = true;
					if(transitionToEdit == null){
						success = addNewSharedTransition(name);
					}else if(!name.equals(transitionToEdit.name())){
						success = updateExistingTransition(name);
					}
					
					if(success){
						nameGenerator.updateIndicesForAllModels(name);
						exit();
					}
				}
			}

			private boolean updateExistingTransition(String name) {
				
				String oldName = transitionToEdit.name();
				
				if(transitionToEdit.network().isNameUsed(name) && !oldName.equalsIgnoreCase(name)) {
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "The specified name is already used by a place or transition in one of the components.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
				
				
				try{
					transitionToEdit.setName(name);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "The specified name is invalid.\nAcceptable names are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
                                
                                for(TAPNQuery query : context.queries()){
					query.getProperty().accept(new RenameSharedTransitionVisitor(oldName, name), null);
				}
				
				listModel.updatedName();
				undoManager.addNewEdit(new RenameSharedTransitionCommand(transitionToEdit, context.tabContent(), oldName, name, listModel));
				return true;
			}
			private boolean addNewSharedTransition(String name) {
				SharedTransition transition = null;
				
				try{
					transition = new SharedTransition(name);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "The specified name is invalid.\nAcceptable names are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
				
				try{
					listModel.addElement(transition);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "A transition or place with the specified name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
					nameField.requestFocusInWindow();
					return false;
				}
				
				undoManager.addNewEdit(new AddSharedTransitionCommand(listModel, transition));
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
