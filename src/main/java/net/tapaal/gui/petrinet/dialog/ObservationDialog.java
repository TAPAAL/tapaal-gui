package net.tapaal.gui.petrinet.dialog;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

public class ObservationDialog extends EscapableDialog {
    private static final String SHARED = "Shared";
    private static final String ANY_COLOR = "Any";
    private static final Pattern namePattern = Pattern.compile("\\w+(?: \\w+)*");
    
    private final DefaultListModel<Observation> observationModel;
    private final Observation observation;

    private final TimedArcPetriNetNetwork tapnNetwork;
    private final JTextPane expressionField = new JTextPane();
    private final UndoManager undoManager = new UndoManager();

    private JComboBox<Object> templateComboBox;
    private JComboBox<TimedPlace> placeComboBox;
    private JComboBox<Object> colorComboBox;
    
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
    private ObsExpression previousExpr;

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
                updateSelected();
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

        templateComboBox = new JComboBox<>();
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

        colorComboBox = new JComboBox<>();
        colorComboBox.setVisible(tapnNetwork.isColored());
        colorComboBox.setToolTipText("Choose a color for the selected place.");
        colorComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                var label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(label.getFont().deriveFont(ANY_COLOR.equals(value) ? Font.ITALIC : Font.PLAIN));
                return label;
            }
        });
        colorComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                var comp = colorComboBox.getUI().getAccessibleChild(colorComboBox, 0);
                if (comp instanceof JPopupMenu popup) {
                    for (var element : popup.getComponents()) {
                        if (element instanceof JScrollPane scrollPane) {
                            if (scrollPane.getHorizontalScrollBar() == null) {
                                scrollPane.setHorizontalScrollBar(new JScrollBar(JScrollBar.HORIZONTAL));
                            }
                            
                            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            break;
                        }
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        placeComboBox = new JComboBox<>();
        templateComboBox.addActionListener(e -> {
            placeComboBox.removeAllItems();
            Object selected = templateComboBox.getSelectedItem();
            if (selected != null) {
                if (selected.equals(SHARED)) {
                    tapnNetwork.sharedPlaces().forEach(place -> placeComboBox.addItem(place));
                } else if (selected instanceof TimedArcPetriNet) {
                    TimedArcPetriNet template = (TimedArcPetriNet)selected;
                    template.places().forEach(place -> {
                        if (!place.isShared()) {
                            placeComboBox.addItem(place);
                        }
                    });
                }
            }
            refreshColorComboBox();
        });

        placeComboBox.addActionListener(e -> refreshColorComboBox());

        // Initialize the placeComboBox with the first template
        if (templateComboBox.getItemCount() > 0) {
            templateComboBox.setSelectedIndex(0);
        }
        refreshColorComboBox();

        JButton addPlaceButton = new JButton("Add place");
        addPlaceButton.addActionListener(e -> {
            Object template = templateComboBox.getSelectedItem();
            TimedPlace place = (TimedPlace)placeComboBox.getSelectedItem();
            if (template != null && place != null) {
                ObsExpression placeExpr = new ObsPlace(template, place, selectedColor());
                updateExpression(placeExpr);
            }
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
        if (tapnNetwork.isColored()) {
            ++placesGbc.gridy;
            placesPanel.add(colorComboBox, placesGbc);
        }
        ++placesGbc.gridy;
        placesPanel.add(addPlaceButton, placesGbc);

        Enabler.setAllEnabled(placesPanel, false);

        constantsPanel = new JPanel();
        constantsPanel.setLayout(new GridBagLayout());
        constantsPanel.setBorder(BorderFactory.createTitledBorder("Constants"));

        JTextField constantTextField = new JTextField("1.0");
        JButton addConstantButton = new JButton("Add constant");
        addConstantButton.addActionListener(e -> {
            try {
                float value = Float.parseFloat(constantTextField.getText());
                ObsExpression constantExpr = new ObsConstant(value);
                updateExpression(constantExpr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ObservationDialog.this, "The constant value must be a valid real number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        GridBagConstraints constantsGbc = new GridBagConstraints();
        constantsGbc.gridx = 0;
        constantsGbc.gridy = 0;
        constantsGbc.weightx = 1;
        constantsGbc.fill = GridBagConstraints.HORIZONTAL;
        constantsGbc.insets = new Insets(0, 10, 0, 10);
        constantsPanel.add(constantTextField, constantsGbc);
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

            refreshSaveButton();
        });

        redoButton.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }

            refreshSaveButton();
        });

        JButton deleteSelection = new JButton("Delete Selection");
        deleteSelection.setEnabled(false);
        deleteSelection.addActionListener(e -> {
            String fullText = expressionField.getText();
            String selectedText = expressionField.getSelectedText();
            if (currentExpr.isOperator() && !fullText.equals(selectedText)) {
                ((ObsOperator)currentExpr).replace(selectedExpr, new ObsPlaceHolder());
            } else {
                currentExpr = new ObsPlaceHolder();
            }

            expressionField.setText(currentExpr.toString());
            refreshSaveButton();
        });

        expressionField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                deleteSelection.setEnabled(expressionField.getSelectedText() != null);
            }
        });

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
                    refreshSaveButton();
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

                refreshSaveButton();
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
        refreshSaveButton();

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

        updateSelected();
    }

    private void toggleManualEditing() {
        isEditing = !isEditing;

        Enabler.setAllEnabled(operationsPanel, !isEditing);
        Enabler.setAllEnabled(placesPanel, !isEditing);
        Enabler.setAllEnabled(constantsPanel, !isEditing);

        if (isEditing) {
            saveButton.setEnabled(false);
            previousExpr = currentExpr.deepCopy();
            resetExpression.setText("Parse Expression");
            editExpression.setText("Cancel");
        } else {
            refreshSaveButton();
            currentExpr = previousExpr;
            expressionField.setText(currentExpr.toString());
            resetExpression.setText("Reset Expression");
            editExpression.setText("Edit Expression");
        }

        expressionField.setEditable(isEditing);
    }

    private void updateSelected() {
        if (isEditing) return;

        expressionField.requestFocusInWindow();
    
        int pos = expressionField.getCaretPosition();
        ObsExprPosition exprPos = currentExpr.getObjectPosition(pos - 1);
        expressionField.select(exprPos.getStart(), exprPos.getEnd());
        selectedExpr = exprPos.getObject();
        if (selectedExpr.isPlace() && selectedExpr instanceof ObsPlace) {
            ObsPlace obsPlace = (ObsPlace)selectedExpr;
            Object selectedTemplate = obsPlace.getTemplate();
            TimedPlace selectedPlace = obsPlace.getPlace();
            String selectedColorName = obsPlace.getColor();

            if (selectedTemplate != null) {
                if (selectedTemplate.toString().equals(SHARED) || selectedTemplate.equals(SHARED)) {
                    templateComboBox.setSelectedItem(SHARED);
                } else if (selectedTemplate instanceof TimedArcPetriNet) {
                    templateComboBox.setSelectedItem(selectedTemplate);
                } else {
                    TimedArcPetriNet net = tapnNetwork.getTAPNByName(selectedTemplate.toString());
                    if (net != null) {
                        templateComboBox.setSelectedItem(net);
                    }
                }
            }

            if (selectedPlace != null) {
                placeComboBox.setSelectedItem(selectedPlace);
            }

            selectColor(selectedColorName);
        }
    
        Enabler.setAllEnabled(placesPanel, selectedExpr.isLeaf());
        Enabler.setAllEnabled(constantsPanel, selectedExpr.isLeaf());
    }

    private void refreshColorComboBox() {
        if (colorComboBox == null) return;
        var previousSelection = colorComboBox.getSelectedItem();
        TimedPlace place = (TimedPlace)placeComboBox.getSelectedItem();

        var colors = new Vector<>();
        colors.add(ANY_COLOR);
        if (tapnNetwork.isColored() && place != null && place.getColorType() != null) {
            colors.addAll(place.getColorType().getColors());
        }
        colorComboBox.setModel(new DefaultComboBoxModel<>(colors));
        if (colors.contains(previousSelection)) {
            colorComboBox.setSelectedItem(previousSelection);
        }
    }

    private String selectedColor() {
        if (!tapnNetwork.isColored() || colorComboBox == null) return null;
        var color = colorComboBox.getSelectedItem();
        return color == null || ANY_COLOR.equals(color) ? null : color.toString();
    }

    private void selectColor(String colorName) {
        if (colorComboBox == null) return;
        colorComboBox.setSelectedItem(ANY_COLOR);
        if (colorName == null) return;
        for (int i = 1; i < colorComboBox.getItemCount(); ++i) {
            var color = colorComboBox.getItemAt(i);
            if (colorName.equals(color.toString())) {
                colorComboBox.setSelectedItem(color);
                return;
            }
        }
    }

    private void updateExpression(ObsExpression newExpr) {
        String fullText = expressionField.getText();
        String selectedText = expressionField.getSelectedText();
        ObsExpression oldExpr = currentExpr.deepCopy();

        if (selectedText != null && currentExpr.isOperator() && !fullText.equals(selectedText)) {
            if (newExpr.isOperator()) {
                ((ObsOperator)newExpr).setLeft(selectedExpr);
            }
            
            selectedExpr.setParent(newExpr);
            ((ObsOperator)currentExpr).replace(selectedExpr, newExpr);
        } else {
            if (newExpr.isOperator()) {
                ((ObsOperator)newExpr).setLeft(currentExpr);
            }

            currentExpr.setParent(newExpr);
            currentExpr = newExpr;
        }
        
        if (!currentExpr.toString().equals(oldExpr.toString())) {   
            expressionField.setText(currentExpr.toString());
            undoManager.addEdit(new ExpressionEdit(oldExpr, currentExpr.deepCopy()));
            refreshUndoRedoButtons();
            refreshSaveButton();
        }

        ObsExprPosition exprPos = null;
        if (newExpr.isOperator()) {
            ObsOperator newOp = (ObsOperator)newExpr;
            ObsExpression newOpLeft = newOp.getLeft();
            ObsExpression newOpRight = newOp.getRight();

            if (newOpLeft.isOperator()) {
                ObsOperator newOpLeftOp = (ObsOperator)newOpLeft;
                if (newOpLeftOp.getRight().isPlaceHolder()) {
                    exprPos = currentExpr.getObjectPosition(newOpLeftOp.getRight());
                } else {
                    exprPos = currentExpr.getObjectPosition(newOpRight);
                }
            } else if (newOpLeft.isPlaceHolder()) {
                exprPos = currentExpr.getObjectPosition(newOpLeft);
            } else {
                exprPos = currentExpr.getObjectPosition(newOpRight);
            }
        } else {
            ObsExpression parentOp = newExpr.getParent();
            if (parentOp != null) {
                ObsExpression rightExpr = ((ObsOperator)parentOp).getRight();
                if (rightExpr.isPlaceHolder()) {
                    exprPos = currentExpr.getObjectPosition(rightExpr);
                } else {
                    ObsExpression grandParentOp = parentOp.getParent();
                    if (grandParentOp != null) {
                        rightExpr = ((ObsOperator)grandParentOp).getRight();
                        exprPos = currentExpr.getObjectPosition(rightExpr);
                    }
                }
            }
        }

        if (exprPos != null) {
            expressionField.requestFocusInWindow();
            expressionField.select(exprPos.getStart(), exprPos.getEnd());
            updateSelected();
        }
    }

    private void refreshUndoRedoButtons() {
        undoButton.setEnabled(undoManager.canUndo());
        redoButton.setEnabled(undoManager.canRedo());
    }

    private boolean includesPlaceHolder() {
        return currentExpr.toString().contains(new ObsPlaceHolder().toString());
    }

    private void refreshSaveButton() {
        saveButton.setEnabled(!includesPlaceHolder());
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
