package pipe.gui.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

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

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewTAPNBasedOnSelection(nameTextBox.getText(), NetType.TAPN);
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(3, 3, 3, 3);
		buttonPanel.add(okButton, gbc);

		rootPane.setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3, 3, 3, 3);
		buttonPanel.add(cancelButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(buttonPanel, gbc);
	}

	protected void exit() {
		rootPane.getParent().setVisible(false);
	}

	protected void createNewTAPNBasedOnSelection(String name, NetType type) {
		if (!name.endsWith(".xml")) {
			name = name + ".xml";
		}

		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"You must provide a name for the net.", "Error",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		try {
			frame.createNewTab(name, type);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"Something went wrong while creating a new model. Please try again.",
							"Error", JOptionPane.INFORMATION_MESSAGE);
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
		nameTextBox = new JTextField(defaultName, 12);
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
