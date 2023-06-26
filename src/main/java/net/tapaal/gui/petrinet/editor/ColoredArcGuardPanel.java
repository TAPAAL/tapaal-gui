package net.tapaal.gui.petrinet.editor;

import net.tapaal.gui.petrinet.Context;
import net.tapaal.gui.petrinet.undo.Colored.SetArcExpressionCommand;
import net.tapaal.gui.petrinet.undo.Colored.SetColoredArcIntervalsCommand;
import net.tapaal.gui.petrinet.undo.Colored.SetTransportArcExpressionsCommand;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ArcExpressionParser.ArcExpressionParser;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TransportArc;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.tapn.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public abstract class ColoredArcGuardPanel extends JPanel {
    final PetriNetObject objectToBeEdited;
    boolean isTransportArc = false;
    boolean isInputTransportArc = false;
    boolean isInputArc = false;
    boolean isInhibitorArc = false;
    private boolean updatingSelection = false;
    final Context context;
    final ColoredArcGuardPanel.ExpressionConstructionUndoManager undoManager;
    final UndoableEditSupport undoSupport;

    public ColoredArcGuardPanel(PetriNetObject objectToBeEdited, Context context){
        this.objectToBeEdited = objectToBeEdited;
        if(objectToBeEdited instanceof TimedTransportArcComponent){
            isTransportArc = true;
            if(((TimedTransportArcComponent)objectToBeEdited).getSource() instanceof Place){
                isInputTransportArc = true;
            }
            //setTransportExpression();
        }
        else if(objectToBeEdited instanceof TimedInhibitorArcComponent){
            isInhibitorArc = true;
        }
        if(((Arc)objectToBeEdited).getSource() instanceof Place){
            isInputArc = true;
            this.colorType = ((TimedPlaceComponent) ((Arc)objectToBeEdited).getSource()).underlyingPlace().getColorType();
        } else if(isTransportArc){
            //it is the outgoing arc so we take the output places type
            this.colorType = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().destination().getColorType();
        } else{
            this.colorType = ((TimedOutputArcComponent)objectToBeEdited).underlyingArc().destination().getColorType();
        }
        this.context = context;
        selectedColorType = this.colorType;
        this.setLayout(new GridBagLayout());
        initPanels();
        initExpr();
        setTimeConstraints();
        //updateSelection();
        hideIrrelevantInformation();

        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        undoManager = new ColoredArcGuardPanel.ExpressionConstructionUndoManager();
        undoSupport = new UndoableEditSupport();
        undoSupport.addUndoableEditListener(new ColoredArcGuardPanel.UndoAdapter());
        refreshUndoRedo();
        makeShortcuts();
    }

    public void hideIrrelevantInformation(){
        if(!objectToBeEdited.isTimed() && isInputArc && nonDefaultArcColorIntervalPanel != null){
            nonDefaultArcColorIntervalPanel.setVisible(false);
        }
        if(isTransportArc){
            additionButton.setEnabled(false);
            subtractionButton.setEnabled(false);
            scalarButton.setEnabled(false);
            scalarJSpinner.setEnabled(false);

        }
    }

    private void initPanels() {
        initRegularArcExpressionPanel();
        if(isInputArc && !isInhibitorArc){
            initNonDefaultColorIntervalPanel();
        }
    }

    private void initNonDefaultColorIntervalPanel() {
        nonDefaultArcColorIntervalPanel = new JPanel(new GridBagLayout());
        JPanel colorIntervalEditPanel = new JPanel(new GridBagLayout());
        nonDefaultArcColorIntervalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Color specific time intervals"));

        colorIntervalComboboxPanel = new ColorComboboxPanel(colorType) {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                ColoredTimeInterval timeConstraint;
                if (!(colorType instanceof ProductType)) {
                    timeConstraint = ColoredTimeInterval.ZERO_INF_DYN_COLOR((dk.aau.cs.model.CPN.Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex()));
                } else {
                    Vector<dk.aau.cs.model.CPN.Color> colors = new Vector<>();
                    for (JComboBox comboBox : comboBoxes) {
                        colors.add((dk.aau.cs.model.CPN.Color) comboBox.getItemAt(comboBox.getSelectedIndex()));
                    }
                    dk.aau.cs.model.CPN.Color color = new dk.aau.cs.model.CPN.Color(colorType, 0, colors);
                    timeConstraint = ColoredTimeInterval.ZERO_INF_DYN_COLOR(color);
                }
                boolean alreadyExists = false;
                for (int i = 0; i < timeConstraintListModel.size(); i++) {
                    if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i))){
                        intervalEditorPanel.setTimeInterval((ColoredTimeInterval) timeConstraintListModel.get(i));
                        addTimeConstraintButton.setText("Modify");
                        alreadyExists = true;
                    }
                }
                if(!alreadyExists){
                    intervalEditorPanel.setTimeInterval(timeConstraint);
                    addTimeConstraintButton.setText("Add");
                }
            }
        };
        colorIntervalComboboxPanel.removeScrollPaneBorder();

        addTimeConstraintButton = new JButton("Add");
        removeTimeConstraintButton = new JButton("Remove");

        Dimension buttonSize = new Dimension(80, 27);

        addTimeConstraintButton.setPreferredSize(buttonSize);
        removeTimeConstraintButton.setPreferredSize(buttonSize);

        timeConstraintListModel = new DefaultListModel();
        timeConstraintList = new JList(timeConstraintListModel);
        timeConstraintList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        timeConstraintListModel.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent arg0) {
            }

            public void intervalAdded(ListDataEvent arg0) {
                timeConstraintList.setSelectedIndex(arg0.getIndex0());
                timeConstraintList.ensureIndexIsVisible(arg0.getIndex0());
            }

            public void intervalRemoved(ListDataEvent arg0) {
                int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
                timeConstraintList.setSelectedIndex(index);
                timeConstraintList.ensureIndexIsVisible(index);
            }

        });
        timeConstraintList.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                JList source = (JList) listSelectionEvent.getSource();
                if(source.getSelectedIndex() >= 0 && source.getModel().getSize() > 0){
                    ColoredTimeInterval cti = (ColoredTimeInterval) source.getModel().getElementAt(source.getSelectedIndex());
                    intervalEditorPanel.setTimeInterval(cti);
                    colorIntervalComboboxPanel.updateSelection(cti.getColor());
                    addTimeConstraintButton.setText("Modify");
                }
                removeTimeConstraintButton.setEnabled(!timeConstraintList.isSelectionEmpty());
            }
        });
        JScrollPane timeConstraintScrollPane = new JScrollPane(timeConstraintList);
        timeConstraintScrollPane.setViewportView(timeConstraintList);
        timeConstraintScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        timeConstraintScrollPane.setBorder(BorderFactory.createTitledBorder("Time interval for colors"));


        addTimeConstraintButton.addActionListener(actionEvent -> {
            ColoredTimeInterval timeConstraint = intervalEditorPanel.getInterval();
            boolean alreadyExists = false;

            for (int i = 0; i < timeConstraintListModel.size(); i++) {
                if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i))){
                    alreadyExists = true;
                    timeConstraintListModel.setElementAt(timeConstraint, i);
                }
            }
            if (!alreadyExists){
                timeConstraintListModel.addElement(timeConstraint);
            }

        });

        removeTimeConstraintButton.addActionListener(actionEvent -> {
            for (Object o : timeConstraintList.getSelectedValuesList()) {
                timeConstraintListModel.removeElement(o);
            }
            if (timeConstraintListModel.isEmpty()) {
                addTimeConstraintButton.setText("Add");
            } else {
                timeConstraintList.setSelectedIndex(0);
            }
        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        colorIntervalEditPanel.add(colorIntervalComboboxPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3,3);
        buttonPanel.add(addTimeConstraintButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(removeTimeConstraintButton, gbc);

        ColoredTimeInterval cti = null;
        if(isTransportArc){
            if(((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getColorTimeIntervals().isEmpty()) {
                cti = ColoredTimeInterval.ZERO_INF_DYN_COLOR(((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().source().getColorType().getFirstColor());
            } else{
                cti = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getColorTimeIntervals().get(0);
            }
        } else if(isInputArc && !isInhibitorArc){
            if(((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().getColorTimeIntervals().isEmpty()) {
                cti = ColoredTimeInterval.ZERO_INF_DYN_COLOR(((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().source().getColorType().getFirstColor());
            } else{
                cti = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().getColorTimeIntervals().get(0);
            }
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        intervalEditorPanel = new ColoredTimeIntervalDialogPanel(cti, context);
        colorIntervalEditPanel.add(intervalEditorPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        colorIntervalEditPanel.add(buttonPanel,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        nonDefaultArcColorIntervalPanel.add(colorIntervalEditPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth =3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        nonDefaultArcColorIntervalPanel.add(timeConstraintScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(nonDefaultArcColorIntervalPanel, gbc);
    }

    private void initRegularArcExpressionPanel(){
        regularArcExprPanel = new JPanel(new GridBagLayout());
        initExprField();
        initNumberExpressionsPanel();
        initArithmeticPanel();
        initEditPanel();
        initColorExpressionButtonsPanel();

        regularArcExprPanel.setBorder(BorderFactory.createTitledBorder("Arc Expression"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        //Comment in this if we want to resize the height as well
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(regularArcExprPanel, gbc);
    }

    private void initEditPanel() {
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
        editPanel.setPreferredSize(new Dimension(260, 190));

        ButtonGroup editButtonsGroup = new ButtonGroup();
        deleteExprSelectionButton = new JButton("Delete Selection");
        resetExprButton = new JButton("Reset Expression");
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        editExprButton = new JButton("Edit Expression");
        editExprButton.setEnabled(true);

        //TODO: add tooltips to buttons

        editButtonsGroup.add(deleteExprSelectionButton);
        editButtonsGroup.add(resetExprButton);
        editButtonsGroup.add(undoButton);
        editButtonsGroup.add(redoButton);
        editButtonsGroup.add(editExprButton);
        editExprButton.addActionListener(actionEvent -> {
            if (exprField.isEditable()) {
                returnFromManualEdit(null);
            } else {
                changeToEditMode();
            }
        });
        deleteExprSelectionButton.addActionListener(actionEvent -> deleteSelection());

        resetExprButton.addActionListener(actionEvent -> {
            if (exprField.isEditable()) {
                ArcExpression newExpression = null;
                try {
                    if(!isTransportArc){
                        newExpression = ArcExpressionParser.parse(exprField.getText(), colorType,context.network());
                    } else{
                        newExpression = ArcExpressionParser.parseNumberOfExpression(exprField.getText(), colorType,context.network());
                    }
                } catch (Throwable ex) {
                    int choice = JOptionPane.showConfirmDialog(
                        TAPAALGUI.getApp(),
                        "TAPAAL encountered an error trying to parse the specified expression with the following error: \n\n" + ex.getMessage() + ".\n\nWe recommend using the expression construction buttons unless you are an experienced user.\n\n The specified expression has not been saved. Do you want to edit it again?",
                        "Error Parsing Expression",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                    System.out.println(ex.getMessage());
                    if (choice == JOptionPane.NO_OPTION)
                        returnFromManualEdit(null);
                    else
                        return;
                }
                if (newExpression != null) {
                    UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(arcExpression, newExpression);
                    returnFromManualEdit(newExpression);
                    undoSupport.postEdit(edit);
                } else {
                    returnFromManualEdit(null);
                }
            }else{
                PlaceHolderArcExpression pHExpr = new PlaceHolderArcExpression();
                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(arcExpression, pHExpr);
                arcExpression = arcExpression.replace(arcExpression, pHExpr);
                updateSelection(pHExpr);
                undoSupport.postEdit(edit);
            }
        });
        undoButton.addActionListener(e -> {
            UndoableEdit edit = undoManager.GetNextEditToUndo();

            if (edit instanceof ColoredArcGuardPanel.ExpressionConstructionEdit) {
                Expression original = ((ColoredArcGuardPanel.ExpressionConstructionEdit) edit)
                    .getOriginal();
                undoManager.undo();
                refreshUndoRedo();
                updateSelection(original);
            }
        });

        redoButton.addActionListener(e -> {
            UndoableEdit edit = undoManager.GetNextEditToRedo();
            if (edit instanceof ColoredArcGuardPanel.ExpressionConstructionEdit) {
                Expression replacement = ((ColoredArcGuardPanel.ExpressionConstructionEdit) edit)
                    .getReplacement();
                undoManager.redo();
                refreshUndoRedo();
                updateSelection(replacement);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        editPanel.add(undoButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0 , 0);
        editPanel.add(redoButton, gbc);

        gbc.insets = new Insets(0, 0, 5 , 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(deleteExprSelectionButton, gbc);

        gbc.gridy = 2;
        editPanel.add(resetExprButton, gbc);

        gbc.gridy = 3;
        editPanel.add(editExprButton, gbc);

        //TODO: Actionlisteners

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        regularArcExprPanel.add(editPanel, gbc);
    }

    private void initArithmeticPanel() {
        JPanel arithmeticPanel = new JPanel(new GridBagLayout());
        arithmeticPanel.setBorder(BorderFactory.createTitledBorder("Arithmetic Operators"));

        additionButton = new JButton("Addition");
        subtractionButton = new JButton("Subtraction");
        scalarButton = new JButton("Scalar");

        final Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelScalar = new SpinnerNumberModel(current, min, max, step);

        scalarJSpinner = new JSpinner(numberModelScalar);

        scalarJSpinner.setPreferredSize(new Dimension(50, 27));
        scalarJSpinner.setPreferredSize(new Dimension(50, 27));
        scalarJSpinner.setPreferredSize(new Dimension(50, 27));

        additionButton.setPreferredSize(new Dimension(110, 30));
        additionButton.setMinimumSize(new Dimension(110, 30));
        additionButton.setMaximumSize(new Dimension(110, 30));

        subtractionButton.setPreferredSize(new Dimension(110, 30));
        subtractionButton.setMinimumSize(new Dimension(110, 30));
        subtractionButton.setMaximumSize(new Dimension(110, 30));

        scalarButton.setPreferredSize(new Dimension(110, 30));
        scalarButton.setMinimumSize(new Dimension(110, 30));
        scalarButton.setMaximumSize(new Dimension(110, 30));


        additionButton.addActionListener(actionEvent -> {
            AddExpression addExpr;
            if (currentSelection.getObject() instanceof ArcExpression) {
                Vector<ArcExpression> exprArc = new Vector<>();
                ArcExpression newExpr = new NumberOfExpression(1, getPlaceholderVec());

                exprArc.add((ArcExpression) currentSelection.getObject());
                exprArc.add(newExpr);
                addExpr = new AddExpression(exprArc);

                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(currentSelection.getObject(), addExpr);
                arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
                updateSelection(newExpr);
                undoSupport.postEdit(edit);
            }
        });

        subtractionButton.addActionListener(actionEvent -> {
            SubtractExpression subExpr;
            if (currentSelection.getObject() instanceof ArcExpression) {
                ArcExpression rightExpr = new NumberOfExpression(1, getPlaceholderVec());
                subExpr = new SubtractExpression((ArcExpression) currentSelection.getObject(), rightExpr);

                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(currentSelection.getObject(), subExpr);
                arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                updateSelection(subExpr);
                undoSupport.postEdit(edit);
            }
        });

        scalarButton.addActionListener(actionEvent -> {
            ScalarProductExpression scalarExpr;
            Integer value = (Integer)scalarJSpinner.getValue();
            if (currentSelection.getObject() instanceof ArcExpression) {
                scalarExpr = new ScalarProductExpression(value, (ArcExpression) currentSelection.getObject());
                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(currentSelection.getObject(), scalarExpr);
                arcExpression = arcExpression.replace(currentSelection.getObject(), scalarExpr);
                updateSelection(scalarExpr);
                undoSupport.postEdit(edit);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0,0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        arithmeticPanel.add(additionButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        arithmeticPanel.add(subtractionButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        arithmeticPanel.add(scalarJSpinner, gbc);

        gbc.gridx = 1;
        arithmeticPanel.add(scalarButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;

        regularArcExprPanel.add(arithmeticPanel,gbc);

    }

    private Vector<ColorExpression> getPlaceholderVec() {
        Vector<ColorExpression> vec = new Vector<>();
        ColorExpression c = getColorExpression().get(0);
        if (c instanceof TupleExpression) {
            int size = ((TupleExpression) c).getColors().size();
            vec.add(createPlaceholderTuple(size));
        } else {
            vec.add(new PlaceHolderColorExpression());
        }
        return vec;
    }

    private TupleExpression createPlaceholderTuple(int size) {
        Vector<ColorExpression> placeholders = new Vector<>();
        for (int i = 0; i < size; i++) {
            placeholders.add(new PlaceHolderColorExpression());
        }
        return new TupleExpression(placeholders);
    }

    private void initNumberExpressionsPanel() {
        numberExprPanel = new JPanel(new GridBagLayout());
        numberExprPanel.setBorder(BorderFactory.createTitledBorder("Numerical Expressions"));

        colorExpressionComboBoxPanel = new ColorComboboxPanel(colorType, !isTransportArc) {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                if (!updatingSelection) addNumberExpression((Integer)numberExpressionJSpinner.getValue(), getColorExpression());
            }
        };
        colorExpressionComboBoxPanel.removeScrollPaneBorder();
        Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelNumber = new SpinnerNumberModel(current, min, max, step);

        numberExpressionJSpinner = new JSpinner(numberModelNumber);

        numberExpressionJSpinner.setPreferredSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMinimumSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMaximumSize(new Dimension(50, 27));
        numberExpressionJSpinner.addChangeListener(ChangeEvent ->
            addNumberExpression((Integer)numberExpressionJSpinner.getValue(), getColorOfSelection()));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0,5 ,5 );
        numberExprPanel.add(numberExpressionJSpinner, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(colorExpressionComboBoxPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.BOTH;

        regularArcExprPanel.add(numberExprPanel, gbc);
    }

    private void initExprField () {
        exprField = new JTextPane();

        StyledDocument doc = exprField.getStyledDocument();

        //Set alignment to be centered for all paragraphs
        MutableAttributeSet standard = new SimpleAttributeSet();
        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(standard, 14);
        doc.setParagraphAttributes(0, 0, standard, true);

        exprField.setBackground(java.awt.Color.white);

        exprField.setEditable(false);
        exprField.setToolTipText("Tooltip missing");

        JScrollPane exprScrollPane = new JScrollPane(exprField);
        exprScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension d = new Dimension(100, 80);
        exprScrollPane.setPreferredSize(d);

        exprField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!exprField.isEditable()) {
                    updateSelection();
                }
            }
        });

        exprField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!exprField.isEditable()) {
                    if (e.getKeyChar() == KeyEvent.VK_DELETE
                        || e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                        deleteSelection();
                    }else if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT){
                        e.consume();
                        int position = exprField.getSelectionEnd();
                        if(e.getKeyCode() == KeyEvent.VK_LEFT){
                            position = exprField.getSelectionStart();
                        }
                        changeToEditMode();
                        exprField.setCaretPosition(position);
                    }
                } else {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                        resetExprButton.doClick(); // we are in manual edit mode, so the reset button is now the Parse Expr button
                        e.consume();
                    }
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 4;
        regularArcExprPanel.add(exprScrollPane, gbc);
    }

    private Vector<ColorExpression> getColorExpression() {
        Vector<ColorExpression> exprVec = new Vector<>();
        TupleExpression tupleExpression;
        if (selectedColorType instanceof ProductType) {
            Vector<ColorExpression> tempVec = new Vector<>();
            for (int i = 0; i < colorExpressionComboBoxPanel.getColorTypeComboBoxesArray().length; i++) {
                ColorExpression expr;
                Object selectedElement = colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[i].getSelectedItem();
                if (selectedElement instanceof PlaceHolderColorExpression) {
                    expr = new PlaceHolderColorExpression();
                } else if (selectedElement instanceof String) {
                    expr = new AllExpression(((ProductType) colorExpressionComboBoxPanel.getColorType()).getColorTypes().get(i));
                } else if (selectedElement instanceof Variable) {
                    expr = new VariableExpression((Variable) selectedElement);
                } else {
                    expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) selectedElement);
                }
                tempVec.add(expr);
            }
            tupleExpression = new TupleExpression(tempVec);
            exprVec.add(tupleExpression);
        } else {
            ColorExpression expr;
            Object selectedElement = colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[0].getSelectedItem();
            if (selectedElement instanceof PlaceHolderColorExpression) {
                expr = new PlaceHolderColorExpression();
            } else if (selectedElement instanceof String) {
                expr = new AllExpression(colorExpressionComboBoxPanel.getColorType());
            } else if (selectedElement instanceof Variable) {
                Vector<ColorExpression> currentColor = getColorOfSelection();
                Set<Variable> currentVariables = getVariablesOfSelection();
                if (currentColor.size() > 0 && (currentVariables.contains(selectedElement))) {
                    expr = currentColor.get(0);
                } else {
                    expr = new VariableExpression((Variable) selectedElement);
                }
            } else {
                Vector<ColorExpression> currentColor = getColorOfSelection();
                if (currentColor.size() > 0 && currentColor.get(0).toString().contains(selectedElement.toString()) && !(currentColor.get(0) instanceof AllExpression)) {
                    expr = currentColor.get(0);
                } else {
                    expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) selectedElement);
                }
            }
            exprVec.add(expr);
        }
        return exprVec;
    }

    private void addNumberExpression(int value, Vector<ColorExpression> exprVec) {
        Expression current = currentSelection.getObject();
        if (current instanceof ArcExpression && exprVec.size() > 0) {
            NumberOfExpression numbExpr = new NumberOfExpression(value, exprVec);
            if (!numbExpr.toString().equals(current.toString()) && undoSupport != null) {
                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(current, numbExpr);
                arcExpression = arcExpression.replace(current, numbExpr);
                updateSelection(numbExpr);
                undoSupport.postEdit(edit);
            }
        } else if (exprVec.size() > 0){
            //TODO: add implementation for when the vector has more than one element, if that can ever happen
            ColorExpression colorExpr = exprVec.firstElement();
            if (!colorExpr.toString().equals(current.toString())) {
                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(current, colorExpr);
                arcExpression = arcExpression.replace(current, colorExpr);
                updateSelection(colorExpr);
                undoSupport.postEdit(edit);
            }
        }
    }

    private Vector<ColorExpression> getColorOfSelection() {
        Vector<ColorExpression> colorExpression = new Vector<>();
        Expression current = currentSelection.getObject();
        if (current instanceof NumberOfExpression) {
            return ((NumberOfExpression) current).getColor();
        } else if (current instanceof SuccessorExpression) {
            colorExpression.add((SuccessorExpression) current);
        } else if (current instanceof PredecessorExpression) {
            colorExpression.add((PredecessorExpression) current);
        }
        return colorExpression;
    }

    private Set<Variable> getVariablesOfSelection() {
        Set<Variable> variables = new HashSet<>();
        currentSelection.getObject().getVariables(variables);
        return variables;
    }

    private void deleteSelection() {
        if (currentSelection != null) {
            Expression replacement = null;
            if (currentSelection.getObject() instanceof ArcExpression) {
                replacement = new PlaceHolderArcExpression();
            }
            if (replacement != null) {
                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(currentSelection.getObject(), replacement);
                arcExpression = arcExpression.replace(currentSelection.getObject(), replacement);
                updateSelection(replacement);
                undoSupport.postEdit(edit);
            }
        }
    }

    private ArcExpression getSpecificChildOfProperty(int number, Expression property) {
        ExprStringPosition[] children = property.getChildren();
        int count = 0;
        for (ExprStringPosition exprStringPosition : children) {
            Expression child = exprStringPosition.getObject();
            if (child instanceof ArcExpression) {
                count++;
            }
            if (count == number) {
                return (ArcExpression) child;
            }
        }

        return new PlaceHolderArcExpression();
    }

    public void initColorExpressionButtonsPanel() {
        colorExpressionButtons = new JPanel(new GridBagLayout());

        ButtonGroup expressionButtonsGroup = new ButtonGroup();
        predButton = new JButton("Add Pred");
        succButton = new JButton("Add Succ");

        predButton.setPreferredSize(new Dimension(130 , 27));
        predButton.setMinimumSize(new Dimension(130 , 27));
        predButton.setMaximumSize(new Dimension(130 , 27));
        succButton.setPreferredSize(new Dimension(130 , 27));
        succButton.setMinimumSize(new Dimension(130 , 27));
        succButton.setMaximumSize(new Dimension(130 , 27));

        expressionButtonsGroup.add(predButton);
        expressionButtonsGroup.add(succButton);

        predButton.addActionListener(actionEvent -> {
            PredecessorExpression predExpr;
            if (currentSelection.getObject() instanceof ColorExpression) {
                predExpr = new PredecessorExpression((ColorExpression) currentSelection.getObject());
                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(currentSelection.getObject(), predExpr);
                arcExpression = arcExpression.replace(currentSelection.getObject(), predExpr);
                updateSelection(predExpr);
                undoSupport.postEdit(edit);
            }
        });

        succButton.addActionListener(actionEvent -> {
            SuccessorExpression succExpr;
            if (currentSelection.getObject() instanceof  ColorExpression) {
                succExpr = new SuccessorExpression((ColorExpression) currentSelection.getObject());
                UndoableEdit edit = new ColoredArcGuardPanel.ExpressionConstructionEdit(currentSelection.getObject(), succExpr);
                arcExpression = arcExpression.replace(currentSelection.getObject(), succExpr);
                updateSelection(succExpr);
                undoSupport.postEdit(edit);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        colorExpressionButtons.add(predButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        colorExpressionButtons.add(succButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;
        numberExprPanel.add(colorExpressionButtons, gbc);

    }
    private void initExpr() {
        ArcExpression expression;
        arcExpression = new PlaceHolderArcExpression();
        if(!isTransportArc) {
            if (isInputArc && !isInhibitorArc) {
                expression = ((TimedInputArcComponent) objectToBeEdited).underlyingTimedInputArc().getArcExpression();
            } else if (isInhibitorArc) {
                expression = ((TimedInhibitorArcComponent) objectToBeEdited).underlyingTimedInhibitorArc().getArcExpression();
            } else {
                expression = (((TimedOutputArcComponent) objectToBeEdited).underlyingArc()).getExpression();
            }
        } else{
            TransportArc transportArc = ((TimedTransportArcComponent) objectToBeEdited).underlyingTransportArc();
            if (isInputArc) {
                expression = transportArc.getInputExpression();
            } else{
                expression = transportArc.getOutputExpression();
            }
        }

        if(expression != null){
            arcExpression = expression.deepCopy();
        }
        exprField.setText(arcExpression.toString());
        updateSelection(arcExpression);
    }

    private void updateSelection() {
        int index = exprField.getCaretPosition();
        ExprStringPosition position = arcExpression.objectAt(index);

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        updateSelectedColorType();
        updateNumberExpressionsPanel(currentSelection.getObject());

        toggleEnabledButtons();
    }

    private void updateSelection(Expression newSelection) {
        exprField.setText(arcExpression.toString());

        ExprStringPosition position = arcExpression.indexOf(newSelection);
        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        updateSelectedColorType();
        updateNumberExpressionsPanel(currentSelection.getObject());

        toggleEnabledButtons();

    }

    private Expression findParent (Expression child, Expression parent) {
        for (ExprStringPosition childCheck : parent.getChildren()) {
            if (child == childCheck.getObject()) {
                return parent;
            } else if (!(childCheck.getObject() instanceof PlaceHolderExpression)) {
                Expression foundParent = findParent(child, childCheck.getObject());
                if (foundParent != child) return foundParent;
            }
        }
        return child;
    }

    private void updateSelectedColorType(){
        ColorType newSelectedColorType = getCurrentSelectionColorType();
        if (newSelectedColorType != null) {
            selectedColorType = newSelectedColorType;
        } else {
            selectedColorType = colorType;
        }
    }

    private void updateNumberExpressionsPanel(Expression current) {
        colorExpressionComboBoxPanel.updateColorType(selectedColorType, context, true, false);
        numberExpressionJSpinner.setVisible(!(current instanceof ColorExpression));
        updatingSelection = true;
        if (current instanceof NumberOfExpression) {
            var numberOfExpression = ((NumberOfExpression) current);
            numberExpressionJSpinner.setValue(numberOfExpression.getNumber());
            var colorExpression = numberOfExpression.getNumberOfExpression().get(0).getBottomColorExpression();
            colorExpressionComboBoxPanel.updateSelection(colorExpression);
        } else if (current instanceof ColorExpression) {
            colorExpressionComboBoxPanel.updateSelection(((ColorExpression) current).getBottomColorExpression());
        } else if (current instanceof ArcExpression && !(current instanceof PlaceHolderArcExpression)) {
            updateNumberExpressionsPanel(getSpecificChildOfProperty(1, current));
        }
        updatingSelection = false;
    }

    private void toggleEnabledButtons() {
        if(currentSelection == null){
            allExpressionButton.setEnabled(false);
            numberExpressionJSpinner.setEnabled(false);
            colorExpressionComboBoxPanel.setEnabled(false);
            additionButton.setEnabled(false);
            subtractionButton.setEnabled(false);
            scalarButton.setEnabled(false);
            succButton.setEnabled(false);
            predButton.setEnabled(false);
        }
        if (currentSelection.getObject() instanceof ColorExpression) {
            numberExpressionJSpinner.setEnabled(true);
            colorExpressionComboBoxPanel.setEnabled(true);
            additionButton.setEnabled(false);
            subtractionButton.setEnabled(false);
            scalarButton.setEnabled(false);
            succButton.setEnabled(true);
            predButton.setEnabled(true);
        }
        else if (currentSelection.getObject() instanceof ArcExpression) {
            numberExpressionJSpinner.setEnabled(true);
            colorExpressionComboBoxPanel.setEnabled(true);
            if(!isTransportArc){
                additionButton.setEnabled(true);
                subtractionButton.setEnabled(true);
                scalarButton.setEnabled(true);
            }
            succButton.setEnabled(false);
            predButton.setEnabled(false);
        }
        if(arcExpression.containsPlaceHolder()){
            disableOkButton();
        } else{
            enableOkButton();
        }

    }
    public void onOkColored(pipe.gui.petrinet.undo.UndoManager undoManager) {
        if(isTransportArc){
            TransportArc transportArc = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc();
            if(isInputArc){
                ArcExpression inputExpression = arcExpression;
                //Output and input should have same value
                ArcExpression outputExpression = transportArc.getOutputExpression().deepCopy();
                ((NumberOfExpression)outputExpression).setNumber((((NumberOfExpression)arcExpression).getNumber()));

                Command expressionsCommand = new SetTransportArcExpressionsCommand((TimedTransportArcComponent)objectToBeEdited, transportArc.getInputExpression(),
                    inputExpression, transportArc.getOutputExpression(), outputExpression);
                expressionsCommand.redo();
                undoManager.addEdit(expressionsCommand);
                Command cmd = new SetColoredArcIntervalsCommand((TimedTransportArcComponent) objectToBeEdited, ((TimedTransportArcComponent) objectToBeEdited).getCtiList(), getctiList());
                cmd.redo();
                undoManager.addEdit(cmd);
            } else{
                ArcExpression outputExpression = arcExpression;
                //Output and input should have same value
                ArcExpression inputExpression = transportArc.getInputExpression().deepCopy();
                ((NumberOfExpression)inputExpression).setNumber((((NumberOfExpression)arcExpression).getNumber()));

                Command expressionsCommand = new SetTransportArcExpressionsCommand((TimedTransportArcComponent)objectToBeEdited, transportArc.getInputExpression(),
                    inputExpression, transportArc.getOutputExpression(), outputExpression);
                expressionsCommand.redo();
                undoManager.addEdit(expressionsCommand);
            }
        }
        else if (!isInhibitorArc && isInputArc) {
            TimedInputArc inputArc = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc();
            Command cmd = new SetArcExpressionCommand((TimedInputArcComponent)objectToBeEdited, inputArc.getArcExpression(), arcExpression);
            cmd.redo();
            undoManager.addEdit(cmd);
            cmd = new SetColoredArcIntervalsCommand((TimedInputArcComponent)objectToBeEdited, inputArc.getColorTimeIntervals(), getctiList());
            cmd.redo();
            undoManager.addEdit(cmd);
        } else if(isInhibitorArc){
            TimedInhibitorArc inhibitorArc = ((TimedInhibitorArcComponent)objectToBeEdited).underlyingTimedInhibitorArc();
            Command cmd = new SetArcExpressionCommand((TimedInhibitorArcComponent)objectToBeEdited, inhibitorArc.getArcExpression(), arcExpression);
            cmd.redo();
            undoManager.addEdit(cmd);
        } else {
            TimedOutputArc outputArc = ((TimedOutputArcComponent)objectToBeEdited).underlyingArc();
            Command cmd = new SetArcExpressionCommand((TimedOutputArcComponent)objectToBeEdited, outputArc.getExpression(), arcExpression);
            cmd.redo();
            undoManager.addEdit(cmd);
        }
    }

    private void setTimeConstraints() {
        List<ColoredTimeInterval> timeIntervals;

        if (isInputTransportArc){
            timeIntervals = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getColorTimeIntervals();
        }
        else if(isInputArc && !isInhibitorArc){
            timeIntervals = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().getColorTimeIntervals();
        } else{
            return;
        }
        for (ColoredTimeInterval timeInterval : timeIntervals) {
            timeConstraintListModel.addElement(timeInterval);
        }
    }

    public DefaultListModel getTimeConstraintModel() {return timeConstraintListModel;}
    private List<ColoredTimeInterval> getctiList() {
        List<ColoredTimeInterval> ctiList = new ArrayList<>();
        for (int i = 0; i < getTimeConstraintModel().size(); i++) {
            ctiList.add((ColoredTimeInterval) getTimeConstraintModel().get(i));
        }
        return ctiList;
    }

    private ColorType getCurrentSelectionColorType() {
        if (currentSelection.getObject() instanceof ColorExpression) {
            ColorExpression current = (ColorExpression) currentSelection.getObject();
            ColorType ct = current.getColorType();

            if (ct != null) return ct;

            if (current instanceof TupleExpression) {
                return ((TupleExpression) currentSelection.getObject()).getColorType(context.network().colorTypes());
            } else if (current.getParent() instanceof TupleExpression) {
                ExprStringPosition[] children = current.getParent().getChildren();
                for (int i = 0; i < children.length; i++) {
                    if (((ColorExpression) children[i].getObject()).getBottomColorExpression().equals(current.getBottomColorExpression())) {
                        return colorType.getProductColorTypes().get(i);
                    }
                }
            }
        }
        return null;
    }

    private void returnFromManualEdit(ArcExpression newExpr) {
        setExprFieldEditable(false);
        deleteExprSelectionButton.setEnabled(true);
        if (newExpr != null) {
            arcExpression = newExpr;
        }

        updateSelection(arcExpression);
        resetExprButton.setText("Reset Expression");
        editExprButton.setText("Edit Expression");

        toggleEnabledButtons();
    }

    private void changeToEditMode() {
        setExprFieldEditable(true);
        deleteExprSelectionButton.setEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        //allExpressionButton.setEnabled(false);
        additionButton.setEnabled(false);
        numberExpressionJSpinner.setEnabled(false);
        colorExpressionComboBoxPanel.setEnabled(false);
        subtractionButton.setEnabled(false);
        scalarButton.setEnabled(false);
        succButton.setEnabled(false);
        predButton.setEnabled(false);
        succButton.setEnabled(false);
        predButton.setEnabled(false);
        resetExprButton.setText("Parse Expression");
        editExprButton.setText("Cancel");
        clearSelection();
        exprField.setCaretPosition(exprField.getText().length());
    }
    private void setExprFieldEditable(boolean isEditable) {
        exprField.setEditable(isEditable);
        exprField.setFocusable(false);
        exprField.setFocusable(true);
        exprField.requestFocus(true);
    }
    private void clearSelection() {
        exprField.select(0, 0);
        currentSelection = null;

    }

    public abstract void disableOkButton();
    public abstract void enableOkButton();

    private final ColorType colorType;
    private ColorType selectedColorType;
    private JPanel regularArcExprPanel;
    JPanel nonDefaultArcColorIntervalPanel;
    DefaultListModel timeConstraintListModel;
    JList timeConstraintList;
    private ExprStringPosition currentSelection = null;
    JSpinner numberExpressionJSpinner;
    private ArcExpression arcExpression;
    private JTextPane exprField;
    JButton allExpressionButton;
    JButton addTimeConstraintButton;
    JButton removeTimeConstraintButton;
    JButton editTimeConstraintButton;
    JButton deleteExprSelectionButton;
    JButton resetExprButton;
    JButton undoButton;
    JButton redoButton;
    JButton editExprButton;
    JButton additionButton;
    JButton subtractionButton;
    JButton scalarButton;
    JSpinner scalarJSpinner;
    ColorComboboxPanel colorIntervalComboboxPanel;
    ColoredTimeIntervalDialogPanel intervalEditorPanel;

    JPanel colorExpressionButtons;
    JButton predButton;
    JButton succButton;
    ColorComboboxPanel colorExpressionComboBoxPanel;
    JPanel numberExprPanel;

    private void refreshUndoRedo() {
        undoButton.setEnabled(undoManager.canUndo());
        redoButton.setEnabled(undoManager.canRedo());
    }

    private class UndoAdapter implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent arg0) {
            UndoableEdit edit = arg0.getEdit();
            undoManager.addEdit(edit);
            refreshUndoRedo();
        }
    }

    private static class ExpressionConstructionUndoManager extends UndoManager {
        public UndoableEdit GetNextEditToUndo() {
            return editToBeUndone();
        }

        public UndoableEdit GetNextEditToRedo() {
            return editToBeRedone();
        }
    }

    public class ExpressionConstructionEdit extends AbstractUndoableEdit {
        private final Expression original;
        private final Expression replacement;

        public Expression getOriginal() {
            return original;
        }

        public Expression getReplacement() {
            return replacement;
        }

        public ExpressionConstructionEdit(Expression original,
                                          Expression replacement) {
            this.original = original;
            this.replacement = replacement;
        }

        @Override
        public void undo() throws CannotUndoException {
            arcExpression = arcExpression.replace(replacement, original);
        }

        @Override
        public void redo() throws CannotRedoException {
            arcExpression = arcExpression.replace(original, replacement);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public boolean canRedo() {
            return true;
        }
    }

    private void makeShortcuts(){
        int shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        ActionMap am = this.getActionMap();
        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { undoButton.doClick(); }
        });
        am.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { redoButton.doClick(); }
        });
        InputMap im = this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke('Z', shortcutkey), "undo");
        im.put(KeyStroke.getKeyStroke('Y', shortcutkey), "redo");
    }
}
