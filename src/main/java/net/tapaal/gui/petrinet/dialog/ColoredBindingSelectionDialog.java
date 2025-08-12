package net.tapaal.gui.petrinet.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Tuple;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Variable;

public class ColoredBindingSelectionDialog {
    public static Tuple<Variable, Color> showDialog(TimedTransition transition, List<Map<Variable, Color>> validBindings) {
        EscapableDialog dialog = new EscapableDialog(
            TAPAALGUI.getApp(),
            "Select binding for transition: " + transition.name(),
            true
        );

        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<Tuple<Variable, Color>> listModel = new DefaultListModel<>();
        for (Map<Variable, Color> binding : validBindings) {
            for (Map.Entry<Variable, Color> entry : binding.entrySet()) {
                listModel.addElement(new Tuple<>(entry.getKey(), entry.getValue()));
            }
        }

        JList<Tuple<Variable, Color>> bindingList = new JList<>(listModel);
        bindingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bindingList.setSelectedIndex(0);

        bindingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Tuple) {
                    try {
                        Tuple<?, ?> tuple = (Tuple<?, ?>)value;
                        if (tuple.value1() instanceof Variable && tuple.value2() instanceof Color) {
                            Variable variable = (Variable) tuple.value1();
                            Color color = (Color) tuple.value2();
                            setText(variable.getName() + " = " + color.getName());
                        } else {
                            setText(value.toString());
                        }
                    } catch (ClassCastException e) {
                        setText(value.toString());
                    }
                } else {
                    setText(value != null ? value.toString() : "");
                }
                
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(bindingList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        final AtomicReference<Tuple<Variable, Color>> selectedBindingRef = new AtomicReference<>(null);
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

        buttonPanel.add(cancelButton);
        buttonPanel.add(selectButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setVisible(true);

        return selectedBindingRef.get();
    }
}
