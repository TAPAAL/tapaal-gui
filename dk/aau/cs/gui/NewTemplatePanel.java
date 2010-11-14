package dk.aau.cs.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.GuiFrame;

public class NewTemplatePanel extends JPanel {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4598172555484557945L;
	private TemplateExplorer templates;
	private JRootPane rootPane;
	private JTextField nameTextBox;
	private NetType netType;
	
	public NewTemplatePanel(JRootPane rootpane, TemplateExplorer templates, NetType netType){
		this.templates = templates;
		this.rootPane = rootpane;
		this.netType = netType;
		
		initComponents();
	}
	
	private void initComponents(){
		this.setLayout(new GridBagLayout());
		
		initChoicePanel();
		initButtonPanel();
	}

	private void initButtonPanel() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		
		JButton okButton = new JButton("OK");
		okButton.setMaximumSize(new java.awt.Dimension(75, 25));
		okButton.setMinimumSize(new java.awt.Dimension(75, 25));
		okButton.setPreferredSize(new java.awt.Dimension(75, 25));
		
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				createNewTemplate(nameTextBox.getText());
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3,3,3,3);
		buttonPanel.add(okButton, gbc);

		rootPane.setDefaultButton(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(75, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(75, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(75, 25));
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

	protected void exit() {
		rootPane.getParent().setVisible(false);
	}

	protected void createNewTemplate(String name) {
			
		if(name.isEmpty()){
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"You must provide a name for the template.",
					"Error",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		templates.createNewTemplate(name, netType);
		
		exit();
	}

	private void initChoicePanel() {
		JPanel choicePanel = new JPanel(new GridBagLayout());
		
		JLabel nameLabel = new JLabel("Name:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(3,3,3,3);
		choicePanel.add(nameLabel, gbc);
		
		String defaultName = "New Petri net template";
		nameTextBox = new JTextField(defaultName, 12);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3,3,3,3);
		choicePanel.add(nameTextBox, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		add(choicePanel, gbc);
	}
}
