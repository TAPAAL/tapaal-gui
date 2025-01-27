package net.tapaal.gui.petrinet.dialog;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

import dk.aau.cs.model.SMC.TokenMgrError;
import dk.aau.cs.model.SMC.ParseException;
import dk.aau.cs.model.SMC.ObservationParser;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.verification.observations.Observation;
import dk.aau.cs.verification.observations.expressions.ObsExprPosition;
import dk.aau.cs.verification.observations.expressions.ObsAdd;
import dk.aau.cs.verification.observations.expressions.ObsConstant;
import dk.aau.cs.verification.observations.expressions.ObsExpression;
import dk.aau.cs.verification.observations.expressions.ObsMultiply;
import dk.aau.cs.verification.observations.expressions.ObsOperator;
import dk.aau.cs.verification.observations.expressions.ObsPlace;
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
import java.util.List;
import java.util.regex.Pattern;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

public class ObservationDialog extends EscapableDialog {
    private static final String SHARED = "Shared";
    private static final Pattern namePattern = Pattern.compile("\\w+(?: \\w+)*");
    
    private final DefaultListModel<Observation> observationModel;
    private final Observation observation;

    private final TimedArcPetriNetNetwork tapnNetwork;
    private final JTextPane expressionField = new JTextPane();
    private final UndoManager undoManager = new UndoManager();
    
    private JPanel placesPanel;
    private JPanel constantsPanel;
    private JPanel operationsPanel;
    private JButton saveButton;
    private JButton undoButton;
    private JButton redoButton;
    private JButton resetExpression;
    private JButton editExpression;

    private ObsExpression currentExpr;
    private ObsExpression selectedExpr;

    private boolean isNewObservation;
    private boolean isEditing;

    public ObservationDialog(TimedArcPetriNetNetwork tapnNetwork, DefaultListModel<Observation> observationModel, Observation observation) {
        super(TAPAALGUI.getApp(), observation.getName(), true);
        this.tapnNetwork = tapnNetwork;
        this.observationModel = observationModel;
        this.observation = observation;
        this.currentExpr = observation.getExpression();

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
                if (isEditing) return;

                int pos = expressionField.viewToModel2D(e.getPoint());
                ObsExprPosition exprPos = currentExpr.getObjectPosition(pos - 1);
                expressionField.select(exprPos.getStart(), exprPos.getEnd());
                selectedExpr = exprPos.getObject();

                Enabler.setAllEnabled(placesPanel, selectedExpr.isLeaf());
                Enabler.setAllEnabled(constantsPanel, selectedExpr.isLeaf());
            }
        });

        JScrollPane expressionScrollPane = new JScrollPane(expressionField);
        expressionScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension d = new Dimension(900, 80);
        expressionScrollPane.setPreferredSize(d);
        expressionScrollPane.setMinimumSize(d);

        operationsPanel = new JPanel();
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
        tapnNetwork.activeTemplates().forEach(template -> {
            List<TimedPlace> places = template.places();
            long sharedPlaces = places.stream().filter(TimedPlace::isShared).count();
            if (sharedPlaces != places.size() && !template.name().equals(SHARED)) {
                templateComboBox.addItem(template);
            }
        });

        if (tapnNetwork.sharedPlaces().size() > 0) {
            templateComboBox.addItem(SHARED);
        }

        JComboBox<TimedPlace> placeComboBox = new JComboBox<>();
        templateComboBox.addActionListener(e -> {
            placeComboBox.removeAllItems();
            System.out.println(templateComboBox.getSelectedItem());
            if (templateComboBox.getSelectedItem().equals(SHARED)) {
                tapnNetwork.sharedPlaces().forEach(place -> placeComboBox.addItem(place));
            } else {
                TimedArcPetriNet template = (TimedArcPetriNet)templateComboBox.getSelectedItem();
                template.places().forEach(place -> {
                    if (!place.isShared()) {
                        placeComboBox.addItem(place);
                    }
                });
            }
        });

        // Initialize the placeComboBox with the first template
        if (templateComboBox.getItemCount() > 0) {
            templateComboBox.setSelectedIndex(0);
        }

        JButton addPlaceButton = new JButton("Add place");
        addPlaceButton.addActionListener(e -> {
            Object template = templateComboBox.getSelectedItem();
            TimedPlace place = (TimedPlace)placeComboBox.getSelectedItem();
            ObsExpression placeExpr = new ObsPlace(template, place);
            updateExpression(placeExpr);
            Enabler.setAllEnabled(placesPanel, false);
            Enabler.setAllEnabled(constantsPanel, false);
        });

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

        CustomJSpinner constantSpinner = new CustomJSpinner(1, 1, Integer.MAX_VALUE);
        JButton addConstantButton = new JButton("Add constant");
        addConstantButton.addActionListener(e -> {
            int value = (int)constantSpinner.getValue();
            ObsExpression constantExpr = new ObsConstant(value);
            updateExpression(constantExpr);
            Enabler.setAllEnabled(placesPanel, false);
            Enabler.setAllEnabled(constantsPanel, false);
        });

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

        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);

        undoButton.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });

        redoButton.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });

        JButton deleteSelection = new JButton("Delete Selection");
        deleteSelection.setEnabled(false);
        resetExpression = new JButton("Reset Expression");

        resetExpression.addActionListener(e -> {
            if (isEditing) {
                try {
                    ObsExpression parsedExpr = ObservationParser.parse(expressionField.getText(), tapnNetwork);
                    toggleManualEditing();  
                    currentExpr = parsedExpr;
                    expressionField.setText(currentExpr.toString());
                    undoManager.addEdit(new ExpressionEdit(new ObsPlaceHolder(), currentExpr.deepCopy()));
                    refreshUndoRedoButtons();
                    saveButton.setEnabled(!includesPlaceHolder());
                } catch (ParseException | TokenMgrError ex) {
                    JOptionPane.showMessageDialog(TAPAALGUI.getApp(), ex.getMessage(), "Error during parsing", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                ObsExpression oldExpr = currentExpr.deepCopy();
                currentExpr = new ObsPlaceHolder();
                expressionField.setText(currentExpr.toString());

                if (!oldExpr.isPlaceHolder()) {
                    undoManager.addEdit(new ExpressionEdit(oldExpr, currentExpr.deepCopy()));
                    refreshUndoRedoButtons();
                }
            }
        });

        editExpression = new JButton("Edit Expression");
        editExpression.addActionListener(e -> toggleManualEditing());

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
        saveButton.setEnabled(!includesPlaceHolder());

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> {
            nameField.setText(nameField.getText().trim());
            if (!namePattern.matcher(nameField.getText()).matches()) {
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "\"The specified name is invalid.\n" +
                                        "Acceptable names are defined by the regular expression:\n" +
                                        namePattern, "Error", JOptionPane.ERROR_MESSAGE);                        
                nameField.requestFocusInWindow();
                return;
            }

            boolean nameExists = false;
            for (int i = 0; i < observationModel.getSize(); i++) {
                Observation obs = observationModel.getElementAt(i);
                if (obs.getName().equals(nameField.getText()) && 
                    !obs.equals(observation)) {
                    nameExists = true;
                    break;
                }
            }

            if (nameExists) {
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "An observation with the name \"" + observation.getName() + "\" already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                observation.setName(nameField.getText());
                observation.setExpression(currentExpr);

                if (isNewObservation) {
                    observationModel.addElement(observation);
                } else {
                    observationModel.setElementAt(observation, observationModel.indexOf(observation));
                }
    
                dispose();
            }
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

    private void toggleManualEditing() {
        isEditing = !isEditing;

        Enabler.setAllEnabled(operationsPanel, !isEditing);
        Enabler.setAllEnabled(placesPanel, !isEditing);
        Enabler.setAllEnabled(constantsPanel, !isEditing);

        if (isEditing) {
            saveButton.setEnabled(false);
        } else {
            saveButton.setEnabled(!includesPlaceHolder());
        }

        expressionField.setEditable(isEditing);
    
        if (isEditing) {
            resetExpression.setText("Parse Expression");
            editExpression.setText("Cancel");
        } else {
            resetExpression.setText("Reset Expression");
            editExpression.setText("Edit Expression");
        }
    }

    private void updateExpression(ObsExpression newExpr) {
        String selectedText = expressionField.getSelectedText();
        String fullText = expressionField.getText();
        ObsExpression oldExpr = currentExpr.deepCopy();
        if (selectedText != null && 
            !selectedText.equals(fullText) && 
            currentExpr.isOperator()) {
            ((ObsOperator)currentExpr).replace(selectedExpr, newExpr);
        } else if (currentExpr.isOperator() && selectedText == null) {
            ((ObsOperator)currentExpr).insertLeftMost(newExpr);
        } else {
            currentExpr = newExpr;
        }

        if (!currentExpr.toString().equals(oldExpr.toString())) {   
            expressionField.setText(currentExpr.toString());
            undoManager.addEdit(new ExpressionEdit(oldExpr, currentExpr.deepCopy()));
            refreshUndoRedoButtons();
            saveButton.setEnabled(!includesPlaceHolder());
        }
    }

    private void refreshUndoRedoButtons() {
        undoButton.setEnabled(undoManager.canUndo());
        redoButton.setEnabled(undoManager.canRedo());
    }

    private boolean includesPlaceHolder() {
        return currentExpr.toString().contains(new ObsPlaceHolder().toString());
    }

    private class ExpressionEdit extends AbstractUndoableEdit {
        private final ObsExpression oldExpr;
        private final ObsExpression newExpr;

        public ExpressionEdit(ObsExpression oldExpr, ObsExpression newExpr) {
            this.oldExpr = oldExpr;
            this.newExpr = newExpr;
        }

        @Override
        public void undo() {
            super.undo();
            currentExpr = oldExpr;
            expressionField.setText(currentExpr.toString());
            refreshUndoRedoButtons();
        }

        @Override
        public void redo() {
            super.redo();
            currentExpr = newExpr;
            expressionField.setText(currentExpr.toString());
            refreshUndoRedoButtons();
        }
    }
}
