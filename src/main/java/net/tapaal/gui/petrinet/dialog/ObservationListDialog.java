package net.tapaal.gui.petrinet.dialog;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.observations.Observation;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridBagConstraints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationListDialog extends EscapableDialog {
    private final TimedArcPetriNetNetwork tapnNetwork;

    private final List<Observation> observations;

    public ObservationListDialog(TimedArcPetriNetNetwork tapnNetwork, List<Observation> observations) {
        super(TAPAALGUI.getApp(), "Observations", true);
        this.tapnNetwork = tapnNetwork;
        this.observations = observations;
        init();
    }

    private static class EllipsisListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            int availableWidth = list.getParent() instanceof JViewport ? ((JViewport)list.getParent()).getWidth() - 10 : list.getWidth();
            if (availableWidth <= 0) return this;
            
            String text = getText();
            FontMetrics fm = getFontMetrics(getFont());
            int textWidth = fm.stringWidth(text);
            
            if (textWidth > availableWidth) {
                int maxChars = text.length();
                String dots = "...";
                int dotsWidth = fm.stringWidth(dots);
                
                while (maxChars > 0) {
                    String truncated = text.substring(0, maxChars);
                    if (fm.stringWidth(truncated) + dotsWidth <= availableWidth) {
                        setText(truncated + dots);
                        setToolTipText(text);
                        break;
                    }

                    --maxChars;
                }
            } else {
                setToolTipText(null);
            }
            
            return this;
        }
    }    

    private static class CheckboxListRenderer extends JPanel implements ListCellRenderer<Observation> {
        private final JCheckBox checkBox;
        private final JLabel label;
        private final EllipsisListCellRenderer ellipsisRenderer;

        public CheckboxListRenderer() {
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            label = new JLabel();
            ellipsisRenderer = new EllipsisListCellRenderer();
            add(checkBox, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Observation> list, Observation value, int index, boolean isSelected, boolean cellHasFocus) {       
            ellipsisRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            boolean checked = ((CheckboxListModel)list.getModel()).isChecked(value);
            checkBox.setSelected(checked);

            setToolTipText("Select the observation to be monitored during SMC execution");

            label.setText(value.toString());
            label.setToolTipText(ellipsisRenderer.getToolTipText());
            label.setEnabled(checked);
            
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            checkBox.setBackground(getBackground());
            label.setBackground(getBackground());
            
            return this;
        }
    }

    private static class CheckboxListModel extends DefaultListModel<Observation> {
        private final Map<Observation, Boolean> checkMap = new HashMap<>();
        
        public boolean isChecked(Observation observation) {
            return checkMap.getOrDefault(observation, observation.isEnabled());
        }
        
        public void toggleChecked(Observation observation) {
            boolean currentState = isChecked(observation);
            boolean newState = !currentState;
            checkMap.put(observation, newState);
            observation.setEnabled(newState);
            int index = indexOf(observation);
            if (index >= 0) {
                fireContentsChanged(this, index, index);
            }
        }
        
        @Override
        public void addElement(Observation observation) {
            super.addElement(observation);
            checkMap.put(observation, observation.isEnabled());
        }
        
        @Override
        public boolean removeElement(Object observation) {
            boolean removed = super.removeElement(observation);
            checkMap.remove(observation);
            return removed;
        }
    }

    private void init() {
        setSize(500, 350);
        setResizable(false);

        setLayout(new GridBagLayout());
       
        JPanel observationPanel = new JPanel();
        observationPanel.setLayout(new GridBagLayout());
        
        CheckboxListModel listModel = new CheckboxListModel();
        for (Observation observation : observations) {
            listModel.addElement(observation);
        }

        listModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
                    observations.add(i, listModel.getElementAt(i));
                }
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
                    observations.remove(i);
                }
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                for (int i = e.getIndex0(); i <= e.getIndex1(); ++i) {
                    observations.set(i, listModel.getElementAt(i));
                }
            }
        });

        JList<Observation> observationList = new JList<>(listModel);
        observationList.setCellRenderer(new CheckboxListRenderer());

        JScrollPane observationScrollPane = new JScrollPane(observationList);
        observationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        observationScrollPane.setPreferredSize(new Dimension(500, observationScrollPane.getPreferredSize().height));

        JButton editButton = new JButton("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(e -> {
            showEditObservationDialog(observationList.getSelectedIndex(), listModel);
        });

        observationList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = observationList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle bounds = observationList.getCellBounds(index, index);
                    if (bounds != null && e.getX() < bounds.x + 20) {
                        Observation observation = listModel.getElementAt(index);
                        listModel.toggleChecked(observation);
                        observationList.repaint();
                    } else if (e.getClickCount() == 2) {
                        showEditObservationDialog(observationList.getSelectedIndex(), listModel);
                    }
                }
            }
        });

        JButton removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> {
            for (Observation observation : observationList.getSelectedValuesList()) {
                listModel.removeElement(observation);
            }
        });

        JButton newButton = new JButton("New");
        newButton.addActionListener(e -> {
            ObservationDialog observationDialog = new ObservationDialog(tapnNetwork, listModel);
            observationDialog.setLocationRelativeTo(this);
            observationDialog.setVisible(true);
        });

        observationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean elemIsSelected = observationList.getSelectedIndex() != -1;
                editButton.setEnabled(elemIsSelected);
                removeButton.setEnabled(elemIsSelected);
            }
        });

        GridBagConstraints observationGbc = new GridBagConstraints();
        observationGbc.gridx = 0;
        observationGbc.gridy = 0;
        observationGbc.weightx = 1;
        observationGbc.weighty = 1;
        observationGbc.fill = GridBagConstraints.BOTH;
        observationGbc.gridwidth = 3;
        observationPanel.add(observationScrollPane, observationGbc);
        ++observationGbc.gridy;
        observationGbc.gridwidth = 1;
        observationGbc.fill = GridBagConstraints.HORIZONTAL;
        observationPanel.add(editButton, observationGbc);
        ++observationGbc.gridx;
        observationPanel.add(removeButton, observationGbc);
        ++observationGbc.gridx;
        observationPanel.add(newButton, observationGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        add(observationPanel, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        JButton okButton = new JButton("OK");

        okButton.addActionListener(e -> {
            dispose();
        });

        GridBagConstraints buttonGbc = new GridBagConstraints();

        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(okButton, buttonGbc);

        ++gbc.gridy;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(buttonPanel, gbc);

        pack();
    }

    private void showEditObservationDialog(int selectedIndex, DefaultListModel<Observation> listModel) {
        if (selectedIndex != -1) {
            ObservationDialog observationDialog = new ObservationDialog(tapnNetwork, listModel, listModel.get(selectedIndex));
            observationDialog.setLocationRelativeTo(this);
            observationDialog.setVisible(true);
        }
    }
}
