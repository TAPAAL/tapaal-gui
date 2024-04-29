package net.tapaal.gui.petrinet.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.swinghelpers.SwingHelper;
import pipe.gui.TAPAALGUI;
import pipe.gui.GuiFrame;
import pipe.gui.swingcomponents.EscapableDialog;

public class NewTAPNPanel extends EscapableDialog {

	private final JRootPane rootPane;
	private final GuiFrame frame;
	private JTextField nameTextBox;
	private JRadioButton timedNet;
    private JRadioButton untimedNet;
	private JRadioButton gameNet;
    private JRadioButton nonGameNet;
	private JRadioButton coloredNet;
    private JRadioButton nonColorNet;
    private JRadioButton stochasticNet;
	private static int newNameCounter = 1;
    static NewTAPNPanel newTAPNPanel;
    private final static String COLORED_GAMES_NOT_SUPPORTED = "There exists no verification engine for colored games, we only allow modelling.\n\n Do you wish to continue?";

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
        String defaultName = String.format("New Petri net %1$d", newNameCounter);
        newTAPNPanel.setName(defaultName);
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

		okButton.addActionListener(e -> createNewTAPNBasedOnSelection(nameTextBox.getText(), timedNet.isSelected(), gameNet.isSelected(), coloredNet.isSelected(), stochasticNet.isSelected()));

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

	protected void createNewTAPNBasedOnSelection(String name, boolean isTimed, boolean isGame, boolean isColored, boolean isStochastic) {
		if (!name.endsWith(".tapn")) {
			name = name + ".tapn";
		}

		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
					"You must provide a name for the net.", "Error",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		try {
			PetriNetTab tab = PetriNetTab.createNewEmptyTab(name, isTimed, isGame, isColored, isStochastic);
			TAPAALGUI.openNewTabFromStream(tab);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							TAPAALGUI.getApp(),
							"Something went wrong while creating a new model. Please try again.",
							"Error", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return;
		}
		newNameCounter++;
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

		String defaultName = String.format("New Petri net %1$d", newNameCounter);
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
        initColorOptions(selectionPanel);
        initStochasticOptions(selectionPanel);

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

        untimedNet = new JRadioButton("No");
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

        nonGameNet = new JRadioButton("No");
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
        gbc.anchor = GridBagConstraints.WEST;
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

        nonColorNet = new JRadioButton("No");
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
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        selectionPanel.add(isColorPanel, gbc);
    }

    private void initStochasticOptions(JPanel selectionPanel) {
        JPanel isStochasticPanel = new JPanel(new GridBagLayout());
        ButtonGroup isStochasticRadioButtonGroup = new ButtonGroup();

        JLabel colorText = new JLabel("Use stochastic semantics :");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isStochasticPanel.add(colorText, gbc);

        JRadioButton nonStochasticNet = new JRadioButton("No");
        nonStochasticNet.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isStochasticPanel.add(nonStochasticNet, gbc);
        isStochasticRadioButtonGroup.add(nonStochasticNet);

        stochasticNet = new JRadioButton("Yes");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        isStochasticPanel.add(stochasticNet, gbc);
        isStochasticRadioButtonGroup.add(stochasticNet);
        stochasticNet.setEnabled(false);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        selectionPanel.add(isStochasticPanel, gbc);

        var refreshOthers = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                gameNet.setEnabled(!stochasticNet.isSelected());
                coloredNet.setEnabled(!stochasticNet.isSelected());
                untimedNet.setEnabled(!stochasticNet.isSelected());
            }
        };
        var refreshStochastic = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                stochasticNet.setEnabled(
                    timedNet.isSelected() && nonGameNet.isSelected() && nonColorNet.isSelected()
                );
            }
        };
        stochasticNet.addActionListener(refreshOthers);
        nonStochasticNet.addActionListener(refreshOthers);
        timedNet.addActionListener(refreshStochastic);
        untimedNet.addActionListener(refreshStochastic);
        gameNet.addActionListener(refreshStochastic);
        nonGameNet.addActionListener(refreshStochastic);
        coloredNet.addActionListener(refreshStochastic);
        nonColorNet.addActionListener(refreshStochastic);
    }

    public void setName(String name){
        nameTextBox.setText(name);
    }
}
