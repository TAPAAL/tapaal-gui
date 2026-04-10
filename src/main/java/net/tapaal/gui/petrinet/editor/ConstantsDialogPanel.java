package net.tapaal.gui.petrinet.editor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.tapaal.swinghelpers.CustomJSpinner;
import net.tapaal.swinghelpers.RequestFocusListener;
import net.tapaal.swinghelpers.SwingHelper;
import pipe.gui.TAPAALGUI;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.swingcomponents.EscapableDialog;

public class ConstantsDialogPanel extends JPanel {

    private final TimedArcPetriNetNetwork model;
    private int lowerBound;
    private int upperBound;
    private EscapableDialog dialog;

    JTextField nameTextField;
    JLabel nameLabel;
    JLabel checkBoxLabel;
    JCheckBox globalCheckBox;
    JLabel valueLabel;
    
    DefaultListModel<Integer> listModel;
    JList<Integer> valueList;
    JScrollPane listScrollPane;
    CustomJSpinner newValueSpinner;
    CustomJSpinner singleValueSpinner;
    JButton addButton;
    JButton removeButton;
    
    JPanel container;
    JPanel buttonContainer;
    JButton okButton;
    JButton cancelButton;

    private final String oldName;

    public ConstantsDialogPanel(TimedArcPetriNetNetwork model, Constant constant) {
        this.model = model;
        listModel = new DefaultListModel<>();

        if (constant != null) {
            for (int val : constant.values()) {
                listModel.addElement(val);
            }
            oldName = constant.name();
            lowerBound = constant.lowerBound();
            upperBound = constant.upperBound();
        } else {
            oldName = "";
        }
        
        initComponents();
        nameTextField.setText(oldName);
        if (constant != null && constant.values().size() > 1) {
            globalCheckBox.setSelected(true);
            for (ActionListener listener : globalCheckBox.getActionListeners()) {
                listener.actionPerformed(new ActionEvent(globalCheckBox, ActionEvent.ACTION_PERFORMED, ""));
            }
        }
    }

    public void showDialog() {
        dialog = new EscapableDialog(TAPAALGUI.getApp(), "Edit Constant", true);
        dialog.add(container);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void initComponents() {
        container = new JPanel();
        container.setLayout(new GridBagLayout());
        
        nameTextField = new JTextField();
        SwingHelper.setPreferredWidth(nameTextField, 330);
        nameTextField.addAncestorListener(new RequestFocusListener());
        nameTextField.addActionListener(e -> {
            okButton.requestFocusInWindow();
            okButton.doClick();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 2, 4);
        container.add(nameTextField, gbc);

        nameLabel = new JLabel(); 
        nameLabel.setText("Name: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        container.add(nameLabel, gbc);

        checkBoxLabel = new JLabel("Multiple values:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        container.add(checkBoxLabel, gbc);

        globalCheckBox = new JCheckBox();
        globalCheckBox.setSelected(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 4, 2, 4);
        container.add(globalCheckBox, gbc);

        valueLabel = new javax.swing.JLabel(); 
        valueLabel.setText("Values: ");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        container.add(valueLabel, gbc);      
        
        JPanel listPanel = new JPanel(new GridBagLayout());
        
        valueList = new JList<>(listModel);
        valueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScrollPane = new JScrollPane(valueList);
        listScrollPane.setPreferredSize(new Dimension(200, 100));
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        listPanel.add(listScrollPane, gbc);
        
        newValueSpinner = new CustomJSpinner(0, okButton);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 4, 4, 0);
        listPanel.add(newValueSpinner, gbc);
        
        addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            if (!((JSpinner.NumberEditor)newValueSpinner.getEditor()).getTextField().getText().isEmpty()) {
                listModel.addElement((Integer) newValueSpinner.getValue());
                int newIndex = listModel.getSize() - 1;
                valueList.setSelectedIndex(newIndex);
                valueList.ensureIndexIsVisible(newIndex);
                removeButton.setEnabled(true);
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 4, 4, 0);
        listPanel.add(addButton, gbc);
        
        removeButton = new JButton("Remove");
        removeButton.setEnabled(!listModel.isEmpty() && valueList.getSelectedIndex() != -1);
        
        valueList.addListSelectionListener(e -> {
            removeButton.setEnabled(!listModel.isEmpty() && valueList.getSelectedIndex() != -1);
        });

        removeButton.addActionListener(e -> {
            int selectedIndex = valueList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
                if (listModel.getSize() > 0) {
                    int nextIndex = Math.min(selectedIndex, listModel.getSize() - 1);
                    valueList.setSelectedIndex(nextIndex);
                    valueList.ensureIndexIsVisible(nextIndex);
                } else {
                    removeButton.setEnabled(false);
                }
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 4, 0, 0);
        listPanel.add(removeButton, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        container.add(listPanel, gbc);
        
        singleValueSpinner = new CustomJSpinner(listModel.isEmpty() ? 0 : listModel.get(0), okButton);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(2, 4, 2, 4);
        container.add(singleValueSpinner, gbc);

        globalCheckBox.addActionListener(e -> {
            boolean multiple = globalCheckBox.isSelected();
            listPanel.setVisible(multiple);
            singleValueSpinner.setVisible(!multiple);
            valueLabel.setText(multiple ? "Values: " : "Value: ");
            valueLabel.setVerticalAlignment(multiple ? JLabel.TOP : JLabel.CENTER);
            if (dialog != null) {
                dialog.pack();
            }
        });

        boolean initialMultiple = false;
        globalCheckBox.setSelected(initialMultiple);
        listPanel.setVisible(initialMultiple);
        singleValueSpinner.setVisible(!initialMultiple);
        valueLabel.setText(initialMultiple ? "Values: " : "Value: ");
        valueLabel.setVerticalAlignment(initialMultiple ? JLabel.TOP : JLabel.CENTER);

        buttonContainer = new JPanel();
        buttonContainer.setLayout(new GridBagLayout());

        okButton = new JButton();
        okButton.setText("OK");
        okButton.setMaximumSize(new Dimension(100, 25));
        okButton.setMinimumSize(new Dimension(100, 25));
        okButton.setPreferredSize(new Dimension(100, 25));
        okButton.setMnemonic(KeyEvent.VK_O);
        gbc = new GridBagConstraints();     
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        buttonContainer.add(okButton, gbc);
        
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(100, 25));
        cancelButton.setMinimumSize(new Dimension(100, 25));
        cancelButton.setPreferredSize(new Dimension(100, 25));
        cancelButton.setMnemonic(KeyEvent.VK_C);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;
        buttonContainer.add(cancelButton, gbc);      
        
        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> exit());
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 5, 8);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        container.add(buttonContainer, gbc);
    }
    
    private void exit() {
        dialog.setVisible(false);
    }

    private void onOK() {
        if (globalCheckBox.isSelected() && listModel.isEmpty()){
            JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "The value list cannot be empty.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            newValueSpinner.requestFocusInWindow();
            return;
        }
        
        String newName = nameTextField.getText();

        if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)) {
            System.err.println("Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*");
            JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
                    "Error", JOptionPane.ERROR_MESSAGE);
            nameTextField.requestFocusInWindow();
            return;
        }

        if (model.isNameUsedForColorType(newName) || model.isNameUsedForVariable(newName) || model.isNameUsedForColor(newName, null)) {
            JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "There is already another Color, Color Type or Variable with the same name.\n\n"
                        + "Choose a different name for the constant.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            nameTextField.requestFocusInWindow();
            return;
        }

        if (newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
                    "You must specify a name.", "Missing name",
                    JOptionPane.ERROR_MESSAGE);
            nameTextField.requestFocusInWindow();
            return;             
        } 
        
        Set<Integer> vals = new LinkedHashSet<>();
        if (globalCheckBox.isSelected()) {
            for (int i = 0; i < listModel.size(); ++i) {
                vals.add(listModel.get(i));
            }
        } else {
            vals.add((Integer) singleValueSpinner.getValue());
        }

        if (!oldName.equals("")) {
            if (!oldName.equals(newName) && model.isNameUsedForConstant(newName)) {
                JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "There is already another constant with the same name.\n\n"
                        + "Choose a different name for the constant.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                nameTextField.requestFocusInWindow();
                return;
            }

            for (int val : vals) {
                if (!(lowerBound <= val && val <= upperBound)){
                    JOptionPane.showMessageDialog(
                            TAPAALGUI.getApp(),
                            "One or more specified values are invalid for the current net.\n"
                            + "Updating the constant invalidates the guard\n"
                            + "on one or more arcs, or it sets the weight of an arc to 0.",
                            "Constant value invalid for current net",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Command edit = model.updateConstant(oldName, new Constant(newName, vals));
            if (edit == null) {
                JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "One or more specified values are invalid for the current net.\n"
                        + "Updating the constant invalidates the guard\n"
                        + "on one or more arcs, or it sets the weight of an arc to 0.",
                        "Constant value invalid for current net",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(edit);
                TAPAALGUI.getCurrentTab().drawingSurface().repaintAll();
                exit();
            }
        } else {
            Command edit = model.addConstant(newName, vals);
            
            if (edit == null) {
                JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "A constant with the specified name already exists.",
                        "Constant exists",
                        JOptionPane.ERROR_MESSAGE);
                nameTextField.requestFocusInWindow();
                return;
            } else {
                TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(edit);
            }
            exit();
        }
        model.buildConstraints();
    }
}