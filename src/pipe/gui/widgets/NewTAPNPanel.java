package pipe.gui.widgets;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import dk.aau.cs.gui.BatchProcessingDialog;
import dk.aau.cs.gui.TabContent;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.GuiFrame;

public class NewTAPNPanel extends JDialog {

	private final JRootPane rootPane;
	private final GuiFrame frame;
	private JTextField nameTextBox;
	private JRadioButton timedNet;
	private JRadioButton gameNet;
	private JRadioButton coloredNet;
    static NewTAPNPanel newTAPNPanel;

    /* ListOfQueries is used throughout the class to check if
    BatchProcessing was called from QueryPane
    (should maybe be boolean)
    */
    public static void showNewTapnPanel(GuiFrame frame){
        if(newTAPNPanel == null){
            newTAPNPanel = new NewTAPNPanel(frame, "New Net", true);
            newTAPNPanel.pack();
            newTAPNPanel.setPreferredSize(newTAPNPanel.getSize());
            //Set the minimum size to 150 less than the preferred, to be consistent with the minimum size of the result panel
            newTAPNPanel.setMinimumSize(new Dimension(newTAPNPanel.getWidth(), newTAPNPanel.getHeight()-150));
            newTAPNPanel.setLocationRelativeTo(null);
            newTAPNPanel.setResizable(true);
        }
        newTAPNPanel.setVisible(true);
    }

    private NewTAPNPanel(GuiFrame frame, String title, boolean modal) {
        super(frame, title, modal);
        this.frame = frame;
        this.rootPane = this.getRootPane();
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

		okButton.addActionListener(e -> createNewTAPNBasedOnSelection(nameTextBox.getText(), timedNet.isSelected(), gameNet.isSelected(), coloredNet.isSelected()));

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

	protected void createNewTAPNBasedOnSelection(String name, boolean isTimed, boolean isGame, boolean isColored) {
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
			TabContent tab = TabContent.createNewEmptyTab(name, isTimed, isGame, isColored);
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

		String defaultName = String.format("New Petri net %1$d", this.frame
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

	private void initSelectionPanel() {
        JPanel selectionPanel = new JPanel(new GridBagLayout());

        initTimeOptions(selectionPanel);
        initGameOptions(selectionPanel);
        initColorOptions(selectionPanel);

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
    private void initColorOptions(JPanel selectionPanel) {
        JPanel isColorPanel = new JPanel(new GridBagLayout());
        ButtonGroup isColorRadioButtonGroup = new ButtonGroup();

        JLabel colorText = new JLabel("Use color semantics:");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isColorPanel.add(colorText, gbc);

        JRadioButton nonColorNet = new JRadioButton("No");
        nonColorNet.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isColorPanel.add(nonColorNet, gbc);
        isColorRadioButtonGroup.add(nonColorNet);

        coloredNet = new JRadioButton("Yes");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isColorPanel.add(coloredNet, gbc);
        isColorRadioButtonGroup.add(coloredNet);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        selectionPanel.add(isColorPanel, gbc);
    }
}
