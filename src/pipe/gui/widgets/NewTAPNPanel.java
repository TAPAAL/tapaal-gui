package pipe.gui.widgets;

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

import dk.aau.cs.gui.TabContent;
import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.GuiFrame;

public class NewTAPNPanel extends JPanel {
	private static final long serialVersionUID = -4598172555484557945L;
	private JRootPane rootPane;
	private GuiFrame frame;
	private JTextField nameTextBox;

	public NewTAPNPanel(JRootPane rootPane, GuiFrame frame) {
		this.rootPane = rootPane;
		this.frame = frame;

		initComponents();
	}

	private void initComponents() {
		this.setLayout(new GridBagLayout());

		initNamePanel();
		initButtonPanel();
	}

	private void initButtonPanel() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());

		JButton okButton = new JButton("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.setMnemonic(KeyEvent.VK_O);
		GridBagConstraints gbc = new GridBagConstraints();		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.insets = new java.awt.Insets(5, 5, 5, 5);
		buttonPanel.add(okButton,gbc);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.setMnemonic(KeyEvent.VK_C);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		buttonPanel.add(cancelButton,gbc);		

		okButton.addActionListener(e -> createNewTAPNBasedOnSelection(nameTextBox.getText(), NetType.TAPN));

		rootPane.setDefaultButton(okButton);
		
		cancelButton.addActionListener(e -> exit());

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.anchor = GridBagConstraints.EAST;
		add(buttonPanel, gbc);
	}

	protected void exit() {
		rootPane.getParent().setVisible(false);
	}

	protected void createNewTAPNBasedOnSelection(String name, NetType type) {
		if (!name.endsWith(".tapn")) {
			name = name + ".tapn";
		}

		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"You must provide a name for the net.", "Error",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		try {
			TabContent tab = frame.createNewEmptyTab(name, type);
			frame.attachTabToGuiFrame(tab);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"Something went wrong while creating a new model. Please try again.",
							"Error", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return;
		}

		frame.incrementNameCounter();
		exit();
	}

	private void initNamePanel() {
		JPanel namePanel = new JPanel(new GridBagLayout());
		
		JLabel nameLabel = new JLabel("Name:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(3, 3, 3, 3);
		namePanel.add(nameLabel, gbc);

		String defaultName = String.format("New Petri net %1$d", frame
				.getNameCounter());
		nameTextBox = new JTextField(defaultName);
		Dimension size = new Dimension(330, 25);			
		nameTextBox.setPreferredSize(size);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 3, 3, 3);
		namePanel.add(nameTextBox, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		add(namePanel, gbc);
	}
}
