package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.tapn.SMCUserDefinedDistribution;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.swinghelpers.GridBagHelper;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;
import pipe.gui.swingcomponents.filebrowser.FileBrowser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

public class ManageCustomDistributionsDialog extends EscapableDialog {
    private final TimedArcPetriNetNetwork network;
    private final DistributionPanel parent;

    private JList<String> distributionList;
    private DefaultListModel<String> listModel;
    private JButton addButton;
    private JButton removeButton;
    private JButton renameButton;
    private JButton editButton;
    private JButton closeButton;

    public ManageCustomDistributionsDialog(TimedArcPetriNetNetwork network, DistributionPanel parent) {
        super(TAPAALGUI.getApp(), "Manage Custom Distributions", true);
        this.network = network;
        this.parent = parent;
        initComponents();
        loadDistributions();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        
        listModel = new DefaultListModel<>();
        distributionList = new JList<>(listModel);
        distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(distributionList);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        renameButton = new JButton("Rename");
        renameButton.setEnabled(false);
        editButton = new JButton("View/Edit");
        editButton.setEnabled(false);
        closeButton = new JButton("Close");

        addButton.addActionListener(e -> addDistribution());
        removeButton.addActionListener(e -> removeDistribution());
        renameButton.addActionListener(e -> renameDistribution());
        editButton.addActionListener(e -> editDistribution());
        closeButton.addActionListener(e -> setVisible(false));
        
        distributionList.addListSelectionListener(e -> {
            boolean selection = !distributionList.isSelectionEmpty();
            removeButton.setEnabled(selection);
            renameButton.setEnabled(selection);
            editButton.setEnabled(selection);
        });

        GridBagConstraints gbc = GridBagHelper.as(0, 0, GridBagHelper.Anchor.WEST, new Insets(5, 5, 5, 5));
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);

        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(addButton, gbc);

        ++gbc.gridx;
        add(removeButton, gbc);

        ++gbc.gridx;
        add(renameButton, gbc);

        ++gbc.gridx;
        add(editButton, gbc);

        ++gbc.gridy;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(closeButton, gbc);

        pack();
        setLocationRelativeTo(parent);
    }

    private void loadDistributions() {
        listModel.clear();
        for (SMCUserDefinedDistribution dist : network.userDefinedDistributions()) {
            listModel.addElement(dist.getName());
        }
    }

    private void addDistribution() {
        String name = JOptionPane.showInputDialog(this, "Enter name for the new distribution:");
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        name = name.trim();

        for (int i = 0; i < listModel.size(); ++i) {
            if (listModel.get(i).equals(name)) {
                JOptionPane.showMessageDialog(this, "A distribution with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        FileBrowser browser = FileBrowser.constructor("Import Distribution", "txt", FileBrowser.userPath);
        File file = browser.openFile();
        if (file != null) {
            try {
               List<Double> values = Files.lines(file.toPath())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
                    
               SMCUserDefinedDistribution newDist = new SMCUserDefinedDistribution(name, values);
               network.add(newDist);
               loadDistributions();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editDistribution() {
        String selected = distributionList.getSelectedValue();
        if (selected == null) return;
        
        SMCUserDefinedDistribution dist = null;
        for (SMCUserDefinedDistribution d : network.userDefinedDistributions()) {
            if (d.getName().equals(selected)) {
                dist = d;
                break;
            }
        }
        
        if (dist != null) {
            StringBuilder sb = new StringBuilder();
            for (Double d : dist.getValues()) {
                sb.append(d).append("\n");
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(true);
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setPreferredSize(new Dimension(300, 400));
            
            int result = JOptionPane.showConfirmDialog(this, scroll, "Edit Values for " + selected, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String text = textArea.getText();
                List<Double> newValues = new ArrayList<>();
                try {
                	String[] lines = text.split("\\n");
                	for (String line : lines) {
                		line = line.trim();
                		if (!line.isEmpty()) {
                			newValues.add(Double.parseDouble(line));
                		}
                	}

                	dist.getValues().clear();
                	dist.getValues().addAll(newValues);
                } catch (NumberFormatException e) {
                	JOptionPane.showMessageDialog(this, "Invalid number format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void removeDistribution() {
        String selected = distributionList.getSelectedValue();
        if (selected != null) {
            network.removeUserDefinedDistribution(selected);
            loadDistributions();
        }
    }

    private void renameDistribution() {
        String selected = distributionList.getSelectedValue();
        if (selected == null) return;

        SMCUserDefinedDistribution dist = null;
        for (SMCUserDefinedDistribution d : network.userDefinedDistributions()) {
            if (d.getName().equals(selected)) {
                dist = d;
                break;
            }
        }

        if (dist == null) return;

        String newName = JOptionPane.showInputDialog(this, "Enter new name for the distribution:", selected);
        if (newName == null) return;
        newName = newName.trim();
        
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newName.equals(selected)) return; 

        for (int i = 0; i < listModel.size(); ++i) {
            if (listModel.get(i).equals(newName)) {
                JOptionPane.showMessageDialog(this, "A distribution with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        network.renameUserDefinedDistribution(selected, newName);
        loadDistributions();
        distributionList.setSelectedValue(newName, true);
    }
}
