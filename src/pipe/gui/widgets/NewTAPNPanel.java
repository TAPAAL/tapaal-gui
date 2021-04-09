package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

import dk.aau.cs.gui.TabContent;
import net.tapaal.swinghelpers.SwingHelper;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.GuiFrame;

public class NewTAPNPanel extends JPanel {

	private final JRootPane rootPane;
	private final GuiFrame frame;
	private JTextField nameTextBox;
	private JRadioButton timedNet;
	private JRadioButton gameNet;

	public NewTAPNPanel(JRootPane rootPane, GuiFrame frame) {
		this.rootPane = rootPane;
		this.frame = frame;

		initComponents();
	}

	private void initComponents() {
		this.setLayout(new GridBagLayout());

		initNamePanel();
		initSelectionPanel();
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

		okButton.addActionListener(e -> createNewTAPNBasedOnSelection(nameTextBox.getText(), timedNet.isSelected(), gameNet.isSelected()));

		rootPane.setDefaultButton(okButton);
		
		cancelButton.addActionListener(e -> exit());

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 8, 5, 8);
		gbc.anchor = GridBagConstraints.EAST;
		add(buttonPanel, gbc);
	}

	protected void exit() {
		rootPane.getParent().setVisible(false);
	}

	protected void createNewTAPNBasedOnSelection(String name, boolean isTimed, boolean isGame) {
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
			TabContent tab = TabContent.createNewEmptyTab(name, isTimed, isGame);
			CreateGui.openNewTabFromStream(tab);
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
        SwingHelper.setPreferredWidth(nameTextBox,330);
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

	private void initSelectionPanel() {
        JPanel selectionPanel = new JPanel(new GridBagLayout());

        initTimeOptions(selectionPanel);
        initGameOptions(selectionPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(selectionPanel, gbc);
    }

    private void initTimeOptions(JPanel selectionPanel) {
        JPanel isTimedPanel = new JPanel(new GridBagLayout());
        ButtonGroup isTimedRadioButtonGroup = new ButtonGroup();

        JLabel timedText = new JLabel("Use time semantics: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isTimedPanel.add(timedText, gbc);

        JRadioButton untimedNet = new JRadioButton("No");
        untimedNet.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isTimedPanel.add(untimedNet, gbc);
        isTimedRadioButtonGroup.add(untimedNet);

        timedNet = new JRadioButton("Yes");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isTimedPanel.add(timedNet, gbc);
        isTimedRadioButtonGroup.add(timedNet);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        selectionPanel.add(isTimedPanel, gbc);
    }

    private void initGameOptions(JPanel selectionPanel) {
        JPanel isGamePanel = new JPanel(new GridBagLayout());
        ButtonGroup isGameRadioButtonGroup = new ButtonGroup();

        JLabel gameText = new JLabel("Use game semantics:");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isGamePanel.add(gameText, gbc);

        JRadioButton nonGameNet = new JRadioButton("No");
        nonGameNet.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isGamePanel.add(nonGameNet, gbc);
        isGameRadioButtonGroup.add(nonGameNet);

        gameNet = new JRadioButton("Yes");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isGamePanel.add(gameNet, gbc);
        isGameRadioButtonGroup.add(gameNet);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        selectionPanel.add(isGamePanel, gbc);
    }
}
