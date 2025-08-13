package net.tapaal.gui.petrinet.dialog;

import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.TAPAALGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ColoredSimulationDialog extends JDialog {
    private final static String TOOL_TIP_PARTITIONING = "Partitions the colors into logically equivalent groups before unfolding";
    private final static String TOOL_TIP_COLOR_FIXPOINT = "Explores the possible colored markings and only unfolds for those";
    private final static String TOOL_TIP_SYMMETRIC_VARIABLES = "Finds variables with equivalent behavior and treats them as the same variable";

    private JCheckBox usePartition;
    private JCheckBox useColorFixpoint;
    private JCheckBox useSymmetricvars;

    private static boolean cancelled;
    private static boolean explicitSimulationMode;

    private static PetriNetTab currentTab;

    public static ColoredSimulationDialog coloredSimDialog;

    private ColoredSimulationDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        initComponents();
    }

    public static void showSimulationDialog(PetriNetTab tab, boolean explicit) {
        int unfoldAnswer = 0;
        if (explicit) {
            String[] options = {"Cancel", "Unfold", "Explicit"};
            unfoldAnswer = JOptionPane.showOptionDialog(TAPAALGUI.getApp(), "Simulate the net explicitly or unfolded", "Simulation Mode",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
        } else {
            String[] options = {"Cancel", "Unfold"};
            unfoldAnswer = JOptionPane.showOptionDialog(TAPAALGUI.getApp(), "The net will need to be unfolded before entering simulation mode", "Unfolding Required", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        }

        if (unfoldAnswer == 2) {
            explicitSimulationMode = true;
        } else if (unfoldAnswer == 1) {
            showUnfoldDialog(tab);
        } else {
            cancelled = true;
        }
    }

    public static void showUnfoldDialog(PetriNetTab tab) {
        currentTab = tab;

        if (tab.getLens().isTimed()) {
            currentTab.createNewAndUnfoldColor(false, false, false);
            return;
        }

        if (coloredSimDialog == null) {
            coloredSimDialog = new ColoredSimulationDialog(TAPAALGUI.getApp(), "Unfold", true);
            coloredSimDialog.pack();
            coloredSimDialog.setPreferredSize(coloredSimDialog.getSize());
            coloredSimDialog.setMinimumSize(new Dimension(coloredSimDialog.getWidth(), coloredSimDialog.getHeight()));
            coloredSimDialog.setLocationRelativeTo(TAPAALGUI.getApp());
            coloredSimDialog.setResizable(false);
        }

        coloredSimDialog.setEnabled(true);
        coloredSimDialog.setVisible(true);
    }

    public static boolean wasCancelled() {
        return cancelled;
    }

    public static boolean explicitSimulationMode() {
        return explicitSimulationMode;
    }

    private void initComponents() {
        setLayout(new FlowLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());

        JPanel checkboxPanel = createCheckboxPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 5, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(checkboxPanel,gbc);

        JPanel buttonPanel = createButtonPanel();
        gbc.insets = new Insets(0, 8, 5, 8);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(buttonPanel,gbc);

        setContentPane(mainPanel);
    }

    private JPanel createCheckboxPanel() {
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridBagLayout());

        usePartition = new JCheckBox("Partition");
        usePartition.setToolTipText(TOOL_TIP_PARTITIONING);
        usePartition.setSelected(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 8, 0, 8);
        checkboxPanel.add(usePartition, gbc);

        useColorFixpoint = new JCheckBox("Color Fixpoint");
        useColorFixpoint.setToolTipText(TOOL_TIP_COLOR_FIXPOINT);
        useColorFixpoint.setSelected(true);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 8, 0, 8);
        checkboxPanel.add(useColorFixpoint, gbc);

        useSymmetricvars = new JCheckBox("Symmetric Variables");
        useSymmetricvars.setToolTipText(TOOL_TIP_SYMMETRIC_VARIABLES);
        useSymmetricvars.setSelected(true);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 8, 0, 8);
        checkboxPanel.add(useSymmetricvars, gbc);
        return checkboxPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        JButton okButton = new JButton("OK");
        okButton.setMaximumSize(new Dimension(100, 25));
        okButton.setMinimumSize(new Dimension(100, 25));
        okButton.setPreferredSize(new Dimension(100, 25));

        okButton.setMnemonic(KeyEvent.VK_O);
        GridBagConstraints gbcOk = new GridBagConstraints();
        gbcOk.gridx = 1;
        gbcOk.gridy = 0;
        gbcOk.anchor = GridBagConstraints.WEST;
        gbcOk.insets = new Insets(5, 5, 5, 5);

        okButton.addActionListener(actionEvent -> onOK());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(100, 25));
        cancelButton.setMinimumSize(new Dimension(100, 25));
        cancelButton.setPreferredSize(new Dimension(100, 25));
        cancelButton.setMnemonic(KeyEvent.VK_C);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;

        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(cancelButton,gbc);
        buttonPanel.add(okButton,gbcOk);

        return buttonPanel;
    }


    private void exit() {
        coloredSimDialog.setVisible(false);
    }

    private void onCancel(){
        cancelled = true;
        exit();
    }

    private void onOK() {
            exit();
            currentTab.createNewAndUnfoldColor(usePartition.isSelected(), useColorFixpoint.isSelected(), useSymmetricvars.isSelected());
    }

    public static void resetFlags() {
        cancelled = false;
        explicitSimulationMode = false;
    }
}
