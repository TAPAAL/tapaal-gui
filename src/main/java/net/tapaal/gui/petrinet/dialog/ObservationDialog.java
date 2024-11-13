package net.tapaal.gui.petrinet.dialog;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.observations.Observation;
import dk.aau.cs.verification.observations.expressions.ObsExprPosition;
import dk.aau.cs.verification.observations.expressions.ObsAdd;
import dk.aau.cs.verification.observations.expressions.ObsExpression;
import dk.aau.cs.verification.observations.expressions.ObsMultiply;
import dk.aau.cs.verification.observations.expressions.ObsOperator;
import dk.aau.cs.verification.observations.expressions.ObsPlaceHolder;
import dk.aau.cs.verification.observations.expressions.ObsSubtract;
import net.tapaal.helpers.Enabler;
import net.tapaal.swinghelpers.CustomJSpinner;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

public class ObservationDialog extends EscapableDialog {
    private static final String SHARED = "Shared";

    private final DefaultListModel<Observation> observationModel;
    private final Observation observation;

    private JPanel placesPanel;
    private JPanel constantsPanel;

    private ObsExpression currentExpr;
    private ObsExpression selectedExpr;

    private boolean isNewObservation;
    private boolean isEditing;

    private JButton saveButton;

    private final TimedArcPetriNetNetwork tapnNetwork;
    private final JTextPane expressionField = new JTextPane();

    public ObservationDialog(TimedArcPetriNetNetwork tapnNetwork, DefaultListModel<Observation> observationModel, Observation observation) {
        super(TAPAALGUI.getApp(), observation.getName(), true);
        this.tapnNetwork = tapnNetwork;
        this.observationModel = observationModel;
        this.observation = observation;
        this.currentExpr = observation.getExpression().copy();

        init();
    }

    public ObservationDialog(TimedArcPetriNetNetwork tapnNetwork, DefaultListModel<Observation> observationModel) {
        this(tapnNetwork, observationModel, new Observation("New Observation"));
        isNewObservation = true;
    }

    private void init() {
        setSize(1200, 425);
        setResizable(false);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new GridBagLayout());

        JLabel nameLabel = new JLabel("Observation name:");
        JTextField nameField = new JTextField(observation.getName(), 25);

        GridBagConstraints nameGbc = new GridBagConstraints();
        nameGbc.gridx = 0;
        nameGbc.gridy = 0;
        nameGbc.anchor = GridBagConstraints.WEST;
        nameGbc.insets = new Insets(0, 0, 0, 5);
        namePanel.add(nameLabel, nameGbc);

        ++nameGbc.gridx;
        nameGbc.insets = new Insets(0, 0, 0, 0);
        namePanel.add(nameField, nameGbc);

        add(namePanel, gbc);
    
        StyledDocument doc = expressionField.getStyledDocument();
        MutableAttributeSet standard = new SimpleAttributeSet();
        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(standard, 14);
        doc.setParagraphAttributes(0, 0, standard, true);
        expressionField.setText(currentExpr.toString());
        expressionField.setEditable(false);

        expressionField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int pos = expressionField.viewToModel2D(e.getPoint());
                ObsExprPosition exprPos = currentExpr.getObjectPosition(pos - 1);
                expressionField.select(exprPos.getStart(), exprPos.getEnd());
                selectedExpr = exprPos.getObject();
            }
        });

        JScrollPane expressionScrollPane = new JScrollPane(expressionField);
        expressionScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension d = new Dimension(900, 80);
        expressionScrollPane.setPreferredSize(d);
        expressionScrollPane.setMinimumSize(d);

        JPanel operationsPanel = new JPanel();
        operationsPanel.setLayout(new GridBagLayout());
        operationsPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
        JButton plusButton = new JButton("+");
        JButton minusButton = new JButton("-");
        JButton multiplyButton = new JButton("*");
        JButton divideButton = new JButton("/");
        divideButton.setVisible(false); // Division is not supported yet

        plusButton.addActionListener(e -> updateExpression(new ObsAdd()));
        minusButton.addActionListener(e -> updateExpression(new ObsSubtract()));
        multiplyButton.addActionListener(e -> updateExpression(new ObsMultiply()));

        GridBagConstraints operationsGbc = new GridBagConstraints();
        operationsGbc.gridx = 0;
        operationsGbc.gridy = 0;
        operationsGbc.weightx = 1;
        operationsGbc.fill = GridBagConstraints.HORIZONTAL;
        operationsGbc.insets = new Insets(0, 10, 0, 10);
        operationsPanel.add(plusButton, operationsGbc);
        ++operationsGbc.gridy;
        operationsGbc.insets = new Insets(5, 10, 0, 10);
        operationsPanel.add(minusButton, operationsGbc);
        ++operationsGbc.gridy;
        operationsPanel.add(multiplyButton, operationsGbc);
        ++operationsGbc.gridy;
        operationsPanel.add(divideButton, operationsGbc);

        placesPanel = new JPanel();
        placesPanel.setLayout(new GridBagLayout());
        placesPanel.setBorder(BorderFactory.createTitledBorder("Places"));

        JComboBox<Object> templateComboBox = new JComboBox<>();
        tapnNetwork.activeTemplates().forEach(templateComboBox::addItem);
        if (tapnNetwork.sharedPlaces().size() > 0) {
            templateComboBox.addItem(SHARED);
        }

        JComboBox<String> placeComboBox = new JComboBox<>();
        templateComboBox.addActionListener(e -> {
            placeComboBox.removeAllItems();
            if (templateComboBox.getSelectedItem().equals(SHARED)) {
                tapnNetwork.sharedPlaces().forEach(place -> placeComboBox.addItem(place.name()));
            } else {
                TimedArcPetriNet template = (TimedArcPetriNet) templateComboBox.getSelectedItem();
                template.places().forEach(place -> placeComboBox.addItem(place.name()));
            }
        });

        // Initialize the placeComboBox with the first template
        if (templateComboBox.getItemCount() > 0) {
            templateComboBox.setSelectedIndex(0);
        }

        JButton addPlaceButton = new JButton("Add place");

        GridBagConstraints placesGbc = new GridBagConstraints();
        placesGbc.gridx = 0;
        placesGbc.gridy = 0;
        placesGbc.weightx = 1;
        placesGbc.fill = GridBagConstraints.HORIZONTAL;
        placesGbc.insets = new Insets(0, 10, 0, 10);
        placesPanel.add(templateComboBox, placesGbc);
        ++placesGbc.gridy;
        placesGbc.insets = new Insets(5, 10, 0, 10);
        placesPanel.add(placeComboBox, placesGbc);
        ++placesGbc.gridy;
        placesPanel.add(addPlaceButton, placesGbc);

        Enabler.setAllEnabled(placesPanel, false);

        constantsPanel = new JPanel();
        constantsPanel.setLayout(new GridBagLayout());
        constantsPanel.setBorder(BorderFactory.createTitledBorder("Constants"));

        CustomJSpinner constantSpinner = new CustomJSpinner(0, 0, Integer.MAX_VALUE);
        JButton addConstantButton = new JButton("Add constant");

        GridBagConstraints constantsGbc = new GridBagConstraints();
        constantsGbc.gridx = 0;
        constantsGbc.gridy = 0;
        constantsGbc.weightx = 1;
        constantsGbc.fill = GridBagConstraints.HORIZONTAL;
        constantsGbc.insets = new Insets(0, 10, 0, 10);
        constantsPanel.add(constantSpinner, constantsGbc);
        ++constantsGbc.gridy;
        constantsGbc.insets = new Insets(5, 10, 0, 10);
        constantsPanel.add(addConstantButton, constantsGbc);

        Enabler.setAllEnabled(constantsPanel, false);

        JPanel editingPanel = new JPanel();
        editingPanel.setLayout(new GridBagLayout());
        editingPanel.setBorder(BorderFactory.createTitledBorder("Editing"));

        JButton undoButton = new JButton("Undo");
        undoButton.setEnabled(false);

        JButton redoButton = new JButton("Redo");
        redoButton.setEnabled(false);

        JButton deleteSelection = new JButton("Delete Selection");
        deleteSelection.setEnabled(false);
        JButton resetExpression = new JButton("Reset Expression");

        resetExpression.addActionListener(e -> {
            currentExpr = new ObsPlaceHolder();
            expressionField.setText(currentExpr.toString());
        });

        JButton editExpression = new JButton("Edit Expression");

        editExpression.addActionListener(e -> {
            isEditing = !isEditing;

            Enabler.setAllEnabled(operationsPanel, !isEditing);
            Enabler.setAllEnabled(placesPanel, !isEditing);
            Enabler.setAllEnabled(constantsPanel, !isEditing);
            saveButton.setEnabled(!isEditing);
            expressionField.setEditable(isEditing);
        
            if (isEditing) {
                resetExpression.setText("Parse Expression");
                editExpression.setText("Cancel");
            } else {
                resetExpression.setText("Reset Expression");
                editExpression.setText("Edit Expression");
            }
        });

        GridBagConstraints editingGbc = new GridBagConstraints();
        editingGbc.gridx = 0;
        editingGbc.gridy = 0;
        editingGbc.weightx = 1;
        editingGbc.fill = GridBagConstraints.HORIZONTAL;
        editingGbc.insets = new Insets(0, 10, 0, 5);
        editingPanel.add(undoButton, editingGbc);
        ++editingGbc.gridx;
        editingGbc.insets = new Insets(0, 0, 0, 10);
        editingPanel.add(redoButton, editingGbc);
        editingGbc.gridx = 0;
        ++editingGbc.gridy;
        editingGbc.gridwidth = 2;
        editingGbc.insets = new Insets(5, 10, 0, 10);
        editingPanel.add(deleteSelection, editingGbc);
        ++editingGbc.gridy;
        editingPanel.add(resetExpression, editingGbc);
        ++editingGbc.gridy;
        editingPanel.add(editExpression, editingGbc);

        JPanel expressionPanel = new JPanel();
        expressionPanel.setLayout(new GridBagLayout());
        expressionPanel.setBorder(BorderFactory.createTitledBorder("Observation Expression"));

        GridBagConstraints expressionGbc = new GridBagConstraints();
        expressionGbc.gridx = 0;
        expressionGbc.gridy = 0;
        expressionGbc.weightx = 1;
        expressionGbc.weighty = 1;
        expressionGbc.fill = GridBagConstraints.BOTH;
        expressionGbc.insets = new Insets(0, 30, 0, 30);
        expressionGbc.gridwidth = 4;
        expressionPanel.add(expressionScrollPane, expressionGbc);
        expressionGbc.gridwidth = 1;
        ++expressionGbc.gridy;
        expressionGbc.insets = new Insets(0, 30, 0, 0);
        expressionPanel.add(operationsPanel, expressionGbc);
        ++expressionGbc.gridx;
        expressionGbc.insets = new Insets(0, 0, 0, 0);
        expressionPanel.add(placesPanel, expressionGbc);
        ++expressionGbc.gridx;
        expressionPanel.add(constantsPanel, expressionGbc);
        ++expressionGbc.gridx;
        expressionGbc.insets = new Insets(0, 0, 0, 30);
        expressionPanel.add(editingPanel, expressionGbc);

        ++gbc.gridy;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(expressionPanel, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        JButton cancelButton = new JButton("Cancel");
        saveButton = new JButton("Save");

        cancelButton.addActionListener(e -> {
            dispose();
        });

        saveButton.addActionListener(e -> {
            observation.setName(nameField.getText());

            if (isNewObservation) {
                observationModel.addElement(observation);
            } else {
                observationModel.setElementAt(observation, observationModel.indexOf(observation));
            }

            dispose();
        });

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.weightx = 1;
        buttonGbc.anchor = GridBagConstraints.EAST;
        buttonGbc.insets = new Insets(0, 0, 0, 5);
        buttonPanel.add(cancelButton, buttonGbc);
        buttonGbc.insets = new Insets(0, 0, 0, 0);
        ++buttonGbc.gridx;
        buttonPanel.add(saveButton, buttonGbc);

        ++gbc.gridy;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(buttonPanel, gbc);

        pack();
    }

    private void updateExpression(ObsExpression newExpr) {
        String selectedText = expressionField.getSelectedText();
        String fullText = expressionField.getText();
        if (selectedText != null && 
            !selectedText.equals(fullText) && 
            currentExpr.isOperator()) {
            ((ObsOperator)currentExpr).replace(selectedExpr, newExpr);
        } else if (currentExpr.isOperator() && selectedText == null) {
            ((ObsOperator)currentExpr).insertLeftMost(newExpr);
        } else {
            currentExpr = newExpr;
        }

        expressionField.setText(currentExpr.toString());
    }
}
