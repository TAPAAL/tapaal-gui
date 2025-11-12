package net.tapaal.gui.petrinet.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.LinkedHashMap;
import java.util.HashMap;

import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Variable;
import pipe.gui.petrinet.Searcher;

public class ColoredBindingSelectionDialog {
    public static Map<Variable, Color> showDialog(TimedTransition transition, List<Map<Variable, Color>> validBindings) {
        String title = "Select binding for transition: " + transition.name();
        EscapableDialog dialog = new EscapableDialog(
            TAPAALGUI.getApp(),
            title,
            true
        );

        JPanel panel = new JPanel(new BorderLayout());

        List<Variable> variables = new ArrayList<>();
        if (!validBindings.isEmpty()) {
            variables.addAll(validBindings.get(0).keySet());
            variables.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));
        }

        JPanel searchPanel = new JPanel(new GridBagLayout());
        Map<Variable, JTextField> searchFields = new LinkedHashMap<>();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        for (Variable var : variables) {
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel label = new JLabel(var.getName() + ":");
            label.setHorizontalAlignment(JLabel.RIGHT);
            searchPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JTextField field = new JTextField(16);
            searchFields.put(var, field);
            searchPanel.add(field, gbc);

            gbc.gridy++;
        }
        JScrollPane searchScroll = new JScrollPane(searchPanel);
        searchScroll.setPreferredSize(new Dimension(350, Math.min(200, variables.size() * 28)));
        panel.add(searchScroll, BorderLayout.NORTH);

        DefaultListModel<Map<Variable, Color>> listModel = new DefaultListModel<>();
        for (var binding : validBindings) {
            listModel.addElement(binding);
        }

        JList<Map<Variable, Color>> bindingList = new JList<>(listModel);
        bindingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bindingList.setSelectedIndex(0);

        bindingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                @SuppressWarnings("unchecked")
                Map<Variable, Color> binding = (Map<Variable, Color>)value;
                StringBuilder sb = new StringBuilder();
                binding.entrySet().stream()
                    .sorted((e1, e2) -> e1.getKey().getName().compareTo(e2.getKey().getName()))
                    .forEach(entry -> {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(entry.getKey().getName()).append(" = ").append(entry.getValue().getName());
                    });
                setText(sb.toString());
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(bindingList);
        panel.add(scrollPane, BorderLayout.CENTER);

        Map<Variable, Searcher<Map<Variable, Color>>> searchers = new HashMap<>();
        for (Variable var : variables) {
            searchers.put(var, new Searcher<>(validBindings, binding -> {
                Color c = binding.get(var);
                return c == null ? "" : c.getName();
            }));
        }

        Runnable filterList = () -> {
            listModel.clear();
            outer: for (var binding : validBindings) {
                for (var entry : searchFields.entrySet()) {
                    String filter = entry.getValue().getText().trim().toLowerCase();
                    if (!filter.isEmpty()) {
                        Color c = binding.get(entry.getKey());
                        if (c == null || !c.getName().toLowerCase().contains(filter)) {
                            continue outer;
                        }
                    }
                }

                listModel.addElement(binding);
            }

            if (!listModel.isEmpty()) {
                bindingList.setSelectedIndex(0);
            }
        };
        
        for (JTextField field : searchFields.values()) {
            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { filterList.run(); }
                public void removeUpdate(DocumentEvent e) { filterList.run(); }
                public void changedUpdate(DocumentEvent e) { filterList.run(); }
            });
        }

        JPanel buttonPanel = new JPanel();

        final AtomicReference<Map<Variable, Color>> selectedBindingRef = new AtomicReference<>(null);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            dialog.setVisible(false);
        });

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            int selectedIndex = bindingList.getSelectedIndex();
            if (selectedIndex != -1) {
                selectedBindingRef.set(bindingList.getModel().getElementAt(selectedIndex));
                dialog.setVisible(false);
            }
        });

        dialog.getRootPane().setDefaultButton(selectButton);

        buttonPanel.add(cancelButton);
        buttonPanel.add(selectButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();

        FontMetrics fm = dialog.getFontMetrics(dialog.getFont());
        int titleWidth = fm.stringWidth(title);
        int decorationsWidth = 225;
        int requiredWidth = titleWidth + decorationsWidth;
        if (dialog.getWidth() < requiredWidth) {
            dialog.setSize(requiredWidth, dialog.getHeight());
        }
        
        var point = MouseInfo.getPointerInfo().getLocation();
        int yOffset = 30;
        dialog.setLocation(point.x - dialog.getWidth() / 2, point.y + yOffset);
        
        SwingUtilities.invokeLater(() -> bindingList.requestFocusInWindow());

        dialog.setVisible(true);

        return selectedBindingRef.get();
    }
}