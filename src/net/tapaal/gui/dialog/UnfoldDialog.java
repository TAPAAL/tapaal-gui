package net.tapaal.gui.dialog;

import dk.aau.cs.gui.TabContent;
import pipe.gui.TAPAALGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class UnfoldDialog extends JDialog {
    private final static String TOOL_TIP_PARTITIONING = "Partitions the colors into logically equivalent groups before unfolding";
    private final static String TOOL_TIP_COLOR_FIXPOINT = "Explores the possible colored markings and only unfolds for those";
    private final static String TOOL_TIP_SYMMETRIC_VARIABLES = "Finds variables with equivalent behavior and treats them as the same variable";

    private JCheckBox usePartition;
    private JCheckBox useColorFixpoint;
    private JCheckBox useSymmetricvars;

    private static boolean cancelled = false;

    private static TabContent currentTab;

    public static UnfoldDialog unfoldDialog;

    private UnfoldDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        initComponents();
    }

    public static void showSimulationDialog(TabContent tab){
        int unfoldAnswer = JOptionPane.showConfirmDialog(null, "The net will need to be unfolded before entering simulation mode", "Unfolding Required", JOptionPane.OK_CANCEL_OPTION);
        if(unfoldAnswer == 0){
            showDialog(tab);
        } else {
            cancelled = true;
        }
    }

    public static void showDialog(TabContent tab) {
        currentTab = tab;

        if(tab.getLens().isTimed()){
            currentTab.createNewAndUnfoldColor(false, false, false);
            return;
        }

        if(unfoldDialog == null){
            unfoldDialog = new UnfoldDialog(TAPAALGUI.getApp(), "Unfold", true);
            unfoldDialog.pack();
            unfoldDialog.setPreferredSize(unfoldDialog.getSize());
            unfoldDialog.setMinimumSize(new Dimension(unfoldDialog.getWidth(), unfoldDialog.getHeight()));
            unfoldDialog.setLocationRelativeTo(null);
            unfoldDialog.setResizable(false);
        }
        unfoldDialog.setEnabled(true);
        unfoldDialog.setVisible(true);

    }

    public static boolean wasCancelled(){
        return cancelled;
    }

    private void initComponents()  {
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
        unfoldDialog.setVisible(false);
    }

    private void onCancel(){
        cancelled = true;
        exit();
    }

    private void onOK() {
            exit();
            currentTab.createNewAndUnfoldColor(usePartition.isSelected(), useColorFixpoint.isSelected(), useSymmetricvars.isSelected());
    }


}
