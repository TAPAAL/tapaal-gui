package net.tapaal.gui.petrinet.editor;

import net.tapaal.gui.petrinet.Context;
import net.tapaal.gui.petrinet.undo.Colored.SetTransitionExpressionCommand;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.GuardExpressionParser.GuardExpressionParser;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;
import kotlin.Pair;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.editor.TAPNTransitionEditor;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ColoredTransitionGuardPanel  extends JPanel {

    private JButton resetExprButton;
    private JButton deleteExprSelectionButton;
    private JButton editExprButton;
    private JButton undoButton;
    private JButton redoButton;

    JComboBox<ColorType> colorTypeCombobox;
    JPanel comparisonButtonsPanel;
    private JButton andButton;
    private JButton orButton;
    private JButton notButton;
    private JButton equalityButton;
    private JButton greaterThanEqButton;
    private JButton greaterThanButton;
    private JButton inequalityButton;
    private JButton lessThanEqButton;
    private JButton lessThanButton;

    //colorexpression elements
    JPanel colorExpressionPanel;
    JPanel colorExpressionButtons;
    JButton predButton;
    JButton succButton;
    JLabel colorTypeLabel;
    ColorComboboxPanel colorCombobox;
    JLabel colorLabel;

    private JTextPane exprField;

    private final Context context;
    private final TimedTransitionComponent transition;

    private GuardExpression newProperty;
    final TAPNTransitionEditor parent;
    final ExpressionConstructionUndoManager undoManager;
    final UndoableEditSupport undoSupport;

    private boolean doColorTypeUndo = true;
    private boolean updatingColorSelection = false;

    private ExprStringPosition currentSelection = null;
    public ColoredTransitionGuardPanel(TimedTransitionComponent transition, Context context, TAPNTransitionEditor parent){
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder("Guard Expression"));
        this.transition = transition;
        this.context = context;
        this.parent = parent;
        initExprField();
        initComparisonPanel();
        initLogicPanel();
        initColorExpressionPanel();
        initExprEditPanel();

        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        undoManager = new ColoredTransitionGuardPanel.ExpressionConstructionUndoManager();
        undoSupport = new UndoableEditSupport();
        undoSupport.addUndoableEditListener(new ColoredTransitionGuardPanel.UndoAdapter());
        refreshUndoRedo();
        makeShortcuts();
    }

    private void initColorExpressionPanel(){
        colorExpressionPanel = new JPanel(new GridBagLayout());
        colorExpressionPanel.setBorder(BorderFactory.createTitledBorder("Variables and Colors"));
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
                Expression oldExpr = getOldExpression(newProperty);
                predExpr = new PredecessorExpression((ColorExpression) oldExpr);
                replaceAndAddToUndo(oldExpr, predExpr);
            }
        });

        succButton.addActionListener(actionEvent -> {
            SuccessorExpression succExpr;
            if (currentSelection.getObject() instanceof  ColorExpression) {
                Expression oldExpr = getOldExpression(newProperty);
                succExpr = new SuccessorExpression((ColorExpression) oldExpr);
                replaceAndAddToUndo(oldExpr, succExpr);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,5,0,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        colorExpressionButtons.add(predButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0 , 0);
        colorExpressionButtons.add(succButton, gbc);

        colorLabel = new JLabel("Color: ");
        colorCombobox = new ColorComboboxPanel((ColorType)colorTypeCombobox.getSelectedItem(),false) {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                if (!updatingColorSelection) addColor();
            }
        };
        colorCombobox.removeScrollPaneBorder();
        colorCombobox.setEnabled(false);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,3,3,3);
        colorExpressionPanel.add(colorCombobox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0,0);
        colorExpressionPanel.add(colorExpressionButtons, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(colorExpressionPanel, gbc);
    }

    private Expression getOldExpression(Expression parent) {
        for (ExprStringPosition child : parent.getChildren()) {
            if (child.getObject() == currentSelection.getObject()) {
                return (parent instanceof GuardExpression) ? child.getObject() : parent;
            } else {
                Expression possibleExpr = getOldExpression(child.getObject());
                if (possibleExpr != null) return possibleExpr;
            }
        }
        return currentSelection.getObject();
    }

    private void initLogicPanel() {
        //Logic buttons
        JPanel logicPanel = new JPanel(new GridBagLayout());
        logicPanel.setBorder(BorderFactory.createTitledBorder("Logic"));
        Dimension d = new Dimension(100, 150);
        logicPanel.setPreferredSize(d);
        logicPanel.setMinimumSize(d);

        ButtonGroup logicButtonGroup = new ButtonGroup();
        andButton = new JButton("AND");
        orButton = new JButton("OR");
        notButton = new JButton("NOT");

        logicButtonGroup.add(andButton);
        logicButtonGroup.add(orButton);
        logicButtonGroup.add(notButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 5);
        logicPanel.add(andButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 5);
        logicPanel.add(orButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 5, 5);
        logicPanel.add(notButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(logicPanel, gbc);

        andButton.addActionListener(actionEvent -> {
            AndExpression andExpr = null;
            if (currentSelection.getObject() instanceof OrExpression) {
                andExpr = new AndExpression(((OrExpression) currentSelection.getObject()).getLeftExpression(), ((OrExpression) currentSelection.getObject()).getRightExpression());
            } else if (currentSelection.getObject() instanceof GuardExpression) {
                andExpr = new AndExpression((GuardExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
            }
            replaceAndAddToUndo(currentSelection.getObject(), andExpr);
        });

        orButton.addActionListener(actionEvent -> {
            OrExpression orExpr = null;
            if (currentSelection.getObject() instanceof AndExpression) {
                orExpr = new OrExpression(((AndExpression) currentSelection.getObject()).getLeftExpression(), ((AndExpression) currentSelection.getObject()).getRightExpression());
            } else if (currentSelection.getObject() instanceof GuardExpression) {
                orExpr = new OrExpression((GuardExpression) currentSelection.getObject(), new PlaceHolderGuardExpression());
            }
            replaceAndAddToUndo(currentSelection.getObject(), orExpr);
        });

        notButton.addActionListener(actionEvent -> {
            NotExpression notExpr = new NotExpression((GuardExpression)currentSelection.getObject());
            replaceAndAddToUndo(currentSelection.getObject(), notExpr);
        });
    }

    private void initComparisonPanel() {
        //Variable interaction elements
        JPanel comparisonPanel = new JPanel(new GridBagLayout());
        comparisonPanel.setBorder(BorderFactory.createTitledBorder("Comparison"));

        colorTypeLabel = new JLabel("Color Type: ");
        colorTypeCombobox = new JComboBox();
        colorTypeCombobox.setPreferredSize(new Dimension(300,25));
        colorTypeCombobox.setRenderer(new ColorComboBoxRenderer(colorTypeCombobox));

        addColorTypesToCombobox(context.network().colorTypes());
        if (colorTypeCombobox.getItemCount() != 0) {
            colorTypeCombobox.setSelectedIndex(0);
        }
        colorTypeCombobox.addActionListener(actionEvent -> updateColorType());

        ButtonGroup comparisonButtons = new ButtonGroup();
        equalityButton = new JButton("=");
        greaterThanEqButton = new JButton(">=");
        greaterThanButton = new JButton(">");
        inequalityButton = new JButton("!=");
        lessThanEqButton = new JButton("<=");
        lessThanButton = new JButton("<");

        comparisonButtons.add(equalityButton);
        comparisonButtons.add(greaterThanEqButton);
        comparisonButtons.add(greaterThanButton);
        comparisonButtons.add(inequalityButton);
        comparisonButtons.add(lessThanEqButton);
        comparisonButtons.add(lessThanButton);

        Dimension comparisonButtonsSize = new Dimension(75, 30);

        equalityButton.setPreferredSize(comparisonButtonsSize);
        greaterThanEqButton.setPreferredSize(comparisonButtonsSize);
        greaterThanButton.setPreferredSize(comparisonButtonsSize);
        inequalityButton.setPreferredSize(comparisonButtonsSize);
        lessThanEqButton.setPreferredSize(comparisonButtonsSize);
        lessThanButton.setPreferredSize(comparisonButtonsSize);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(colorTypeCombobox, gbc);

        comparisonButtonsPanel = new JPanel(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonButtonsPanel.add(equalityButton, gbc);

        gbc.gridx = 1;
        comparisonButtonsPanel.add(inequalityButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        comparisonButtonsPanel.add(greaterThanButton, gbc);

        gbc.gridx = 1;
        comparisonButtonsPanel.add(greaterThanEqButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        comparisonButtonsPanel.add(lessThanButton, gbc);

        gbc.gridx = 1;
        comparisonButtonsPanel.add(lessThanEqButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        comparisonPanel.add(comparisonButtonsPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(comparisonPanel, gbc);

        equalityButton.addActionListener(actionEvent -> {
            var pair = getLeftRightExpression(currentSelection.getObject());
            ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
            replaceAndAddToUndo(currentSelection.getObject(), new EqualityExpression(pair.component1(),pair.component2(), type));

        });

        greaterThanEqButton.addActionListener(actionEvent -> {
            var pair = getLeftRightExpression(currentSelection.getObject());
            ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
            replaceAndAddToUndo(currentSelection.getObject(), new GreaterThanEqExpression(pair.component1(),pair.component2(), type));

        });

        greaterThanButton.addActionListener(actionEvent -> {
            var pair = getLeftRightExpression(currentSelection.getObject());
            ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
            replaceAndAddToUndo(currentSelection.getObject(), new GreaterThanExpression(pair.component1(),pair.component2(), type));

        });

        inequalityButton.addActionListener(actionEvent -> {
            var pair = getLeftRightExpression(currentSelection.getObject());
            ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
            replaceAndAddToUndo(currentSelection.getObject(), new InequalityExpression(pair.component1(),pair.component2(), type));

        });
        lessThanButton.addActionListener(actionEvent -> {
            var pair = getLeftRightExpression(currentSelection.getObject());
            ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
            replaceAndAddToUndo(currentSelection.getObject(), new LessThanExpression(pair.component1(),pair.component2(), type));
        });

        lessThanEqButton.addActionListener(actionEvent -> {
            var pair = getLeftRightExpression(currentSelection.getObject());
            ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
            replaceAndAddToUndo(currentSelection.getObject(), new LessThanEqExpression(pair.component1(),pair.component2(), type));
        });

    }

    private Vector<ColorExpression> createPlaceholderVectors(int size) {
        Vector<ColorExpression> colorExpressions = new Vector<>();
        ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
        for (int i = 0; i < size; i++) {
            colorExpressions.add(new PlaceHolderColorExpression(type));
        }
        return colorExpressions;
    }

    private Pair<ColorExpression, ColorExpression> getLeftRightExpression(Expression currentSelection) {
        if (currentSelection instanceof PlaceHolderGuardExpression) {
            if (colorTypeCombobox.getSelectedItem() instanceof ProductType) {
                int size = ((ProductType) colorTypeCombobox.getSelectedItem()).size();
                Vector<ColorExpression> tempVec1 = createPlaceholderVectors(size);
                Vector<ColorExpression> tempVec2 = createPlaceholderVectors(size);
                return new Pair<>(new TupleExpression(tempVec1, (ProductType) colorTypeCombobox.getSelectedItem()),
                                  new TupleExpression(tempVec2, (ProductType) colorTypeCombobox.getSelectedItem()));
            }
            ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
            return new Pair<>(new PlaceHolderColorExpression(type), new PlaceHolderColorExpression(type));
        } else {
            return new Pair<>(((LeftRightGuardExpression) currentSelection).getLeftExpression(),
                ((LeftRightGuardExpression) currentSelection).getRightExpression());
        }
    }

    private void initExprEditPanel() {
        //Edit expression buttons
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
        //editPanel.setPreferredSize(new Dimension(260, 190));

        ButtonGroup editButtonsGroup = new ButtonGroup();
        deleteExprSelectionButton = new JButton("Delete Selection");
        resetExprButton = new JButton("Reset Expression");
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        editExprButton = new JButton("Edit Expression");
        editExprButton.setEnabled(true);

        editButtonsGroup.add(deleteExprSelectionButton);
        editButtonsGroup.add(resetExprButton);
        editButtonsGroup.add(undoButton);
        editButtonsGroup.add(redoButton);
        editButtonsGroup.add(editExprButton);

        deleteExprSelectionButton.addActionListener(actionEvent -> deleteSelection());

        editExprButton.addActionListener(actionEvent -> {
            if (exprField.isEditable()) {
                returnFromManualEdit(null);
            } else {
                changeToEditMode();
            }
        });

        resetExprButton.addActionListener(actionEvent -> {
            if (exprField.isEditable()) {
                GuardExpression newExpression = null;
                try {
                    newExpression = GuardExpressionParser.parse(exprField.getText(),context.network());
                } catch (Throwable ex) {
                    int choice = JOptionPane.showConfirmDialog(
                        TAPAALGUI.getApp(),
                        "TAPAAL encountered an error trying to parse the specified Expression with the following error: \n\n" + ex.getMessage()+ ".\n\nWe recommend using the expression construction buttons unless you are an experienced user.\n\n The specified expression has not been saved. Do you want to edit it again?",
                        "Error Parsing Expression",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                    System.out.println(ex.getMessage());
                    if (choice == JOptionPane.NO_OPTION)
                        returnFromManualEdit(null);
                    else
                        return;
                }
                if(newExpression != null){
                    UndoableEdit edit = new ExpressionConstructionEdit(newProperty, newExpression);
                    returnFromManualEdit(newExpression);
                    undoSupport.postEdit(edit);
                }else{
                    returnFromManualEdit(null);
                }
            } else {
                PlaceHolderGuardExpression phGuard = new PlaceHolderGuardExpression();
                UndoableEdit edit = new ExpressionConstructionEdit(newProperty, phGuard);
                newProperty = newProperty.replace(newProperty, phGuard);
                updateSelection(phGuard);
                undoSupport.postEdit(edit);
            }
        });

        undoButton.addActionListener(e -> {
            UndoableEdit edit = undoManager.GetNextEditToUndo();

            if (edit instanceof ColoredTransitionGuardPanel.ExpressionConstructionEdit) {
                Expression original = ((ColoredTransitionGuardPanel.ExpressionConstructionEdit) edit)
                    .getOriginal();
                undoManager.undo();
                refreshUndoRedo();
                updateSelection(original);
            }
        });

        redoButton.addActionListener(e -> {
            UndoableEdit edit = undoManager.GetNextEditToRedo();
            if (edit instanceof ColoredTransitionGuardPanel.ExpressionConstructionEdit) {
                Expression replacement = ((ColoredTransitionGuardPanel.ExpressionConstructionEdit) edit)
                    .getReplacement();
                undoManager.redo();
                refreshUndoRedo();
                updateSelection(replacement);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,0,5,5);
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

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.BOTH;
        add(editPanel, gbc);
    }

    private void initExprField() {
        exprField = new JTextPane();

        StyledDocument doc = exprField.getStyledDocument();

        //Set alignment to be centered for all paragraphs
        MutableAttributeSet standard = new SimpleAttributeSet();
        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(standard, 14);
        doc.setParagraphAttributes(0,0, standard,true);

        exprField.setBackground(java.awt.Color.white);
        exprField.setEditable(false);
        exprField.setToolTipText("Tooltip missing");

        JScrollPane exprScrollPane = new JScrollPane(exprField);
        exprScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension d = new Dimension(100, 80);
        exprScrollPane.setPreferredSize(d);
        //exprScrollPane.setMinimumSize(d);

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
        add(exprScrollPane, gbc);
    }

    public void initExpr(GuardExpression guard) {
        newProperty = new PlaceHolderGuardExpression();
        //new Exception().printStackTrace();
        if (guard != null) {
            newProperty = guard.copy();
        }
        ColorType ct = newProperty.getColorType();
        doColorTypeUndo = false;
        if (ct != null)
            colorTypeCombobox.setSelectedItem(ct);
        else
            colorTypeCombobox.setSelectedIndex(0);
        doColorTypeUndo = true;
        updateSelection(newProperty);
        colorTypeCombobox.setEnabled(newProperty instanceof PlaceHolderGuardExpression);
    }

    private void addColor() {
        Expression newExpression;
        ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
        if (type instanceof ProductType) {
            Object selectedElement = colorCombobox.getColorTypeComboBoxesArray()[0].getSelectedItem();
            if (selectedElement instanceof String) {
                newExpression = new AllExpression(((ProductType)colorCombobox.getColorType()).getColorTypes().get(0));
            } else if (selectedElement instanceof Variable) {
                newExpression = new VariableExpression((Variable)selectedElement, type);
            } else if (selectedElement instanceof PlaceHolderColorExpression) {
                newExpression = new PlaceHolderColorExpression(type);
            } else {
                newExpression = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) selectedElement, type);
            }
        } else {
            ColorExpression expr;
            ColorExpression oldExpression = ((ColorExpression)currentSelection.getObject());
            Object selectedElement = colorCombobox.getColorTypeComboBoxesArray()[0].getSelectedItem();
            if (selectedElement instanceof String) {
                expr = new AllExpression(colorCombobox.getColorType());
            } else if(selectedElement instanceof Variable) {
                expr = new VariableExpression((Variable) selectedElement);
            } else if (selectedElement instanceof PlaceHolderColorExpression) {
                expr = new PlaceHolderColorExpression(type);
            } else {
                expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) selectedElement);
            }
            if (oldExpression.getParent() instanceof TupleExpression) {
                expr.setParent(oldExpression.getParent());
                expr.setIndex(oldExpression.getIndex());
            }
            newExpression = expr;
        }
        if (currentSelection.getObject() instanceof ColorExpression) {
            replaceAndAddToUndo(currentSelection.getObject(), newExpression);
        }
    }

    private void updateEnabledButtons() {
        if(currentSelection == null){
            andButton.setEnabled(false);
            orButton.setEnabled(false);
            notButton.setEnabled(false);
            equalityButton.setEnabled(false);
            inequalityButton.setEnabled(false);
            greaterThanButton.setEnabled(false);
            greaterThanEqButton.setEnabled(false);
            lessThanEqButton.setEnabled(false);
            lessThanButton.setEnabled(false);
            colorTypeCombobox.setEnabled(false);
            succButton.setEnabled(false);
            predButton.setEnabled(false);
            colorCombobox.setEnabled(false);
        }
        else if (currentSelection.getObject() instanceof ColorExpression) {
            andButton.setEnabled(false);
            orButton.setEnabled(false);
            notButton.setEnabled(false);
            equalityButton.setEnabled(false);
            inequalityButton.setEnabled(false);
            greaterThanButton.setEnabled(false);
            greaterThanEqButton.setEnabled(false);
            lessThanEqButton.setEnabled(false);
            lessThanButton.setEnabled(false);
            colorTypeCombobox.setEnabled(false);
            succButton.setEnabled(true);
            predButton.setEnabled(true);
            colorCombobox.setEnabled(true);
        }
        else if (currentSelection.getObject() instanceof GuardExpression) {
            andButton.setEnabled(true);
            orButton.setEnabled(true);
            notButton.setEnabled(true);
            equalityButton.setEnabled(true);
            inequalityButton.setEnabled(true);
            greaterThanButton.setEnabled(true);
            greaterThanEqButton.setEnabled(true);
            lessThanEqButton.setEnabled(true);
            lessThanButton.setEnabled(true);
            succButton.setEnabled(false);
            predButton.setEnabled(false);
            colorCombobox.setEnabled(false);
            colorTypeCombobox.setEnabled(
                currentSelection.getObject() instanceof LeftRightGuardExpression ||
                currentSelection.getObject() instanceof PlaceHolderGuardExpression
            );
        }
        if (colorTypeCombobox.getItemAt(colorTypeCombobox.getSelectedIndex()) instanceof ProductType) {
            greaterThanButton.setEnabled(false);
            greaterThanEqButton.setEnabled(false);
            lessThanEqButton.setEnabled(false);
            lessThanButton.setEnabled(false);
            checkSelectionComparison();
        }
        parent.enableOKButton(!newProperty.containsPlaceHolder() || newProperty instanceof PlaceHolderExpression);
    }

    private void checkSelectionComparison() {
        if (currentSelection != null && (currentSelection.getObject() instanceof GreaterThanEqExpression || currentSelection.getObject() instanceof GreaterThanExpression ||
            currentSelection.getObject() instanceof LessThanEqExpression || currentSelection.getObject() instanceof LessThanExpression)) {
            deleteSelection();
        }
    }

    private void updateSelection(Expression newSelection) {
        exprField.setText(newProperty.toString());

        ExprStringPosition position;
        if (newProperty.containsPlaceHolder()) {
            Expression ge = newProperty.findFirstPlaceHolder();
            position = newProperty.indexOf(ge);
        } else {
            position = newProperty.indexOf(newSelection);
        }
        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        updateEnabledButtons();
        updateColorTypeSelection();
        updateColorSelection();
    }

    private void updateSelection() {
        int index = exprField.getCaretPosition();
        ExprStringPosition position = newProperty.objectAt(index);

        if (position == null) {
            return;
        }
        if (position.getObject() instanceof TupleExpression) {
            position = newProperty.objectAt(index-1);
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        updateEnabledButtons();
        updateColorTypeSelection();
        updateColorSelection();
    }

    private void updateColorTypeSelection() {
        doColorTypeUndo = false;
        if (currentSelection.getObject() instanceof ColorExpression) {
            colorTypeCombobox.setSelectedItem(((ColorExpression)currentSelection.getObject()).getColorType());
        } else if (currentSelection.getObject() instanceof LeftRightGuardExpression) {
            colorTypeCombobox.setSelectedItem(((GuardExpression)currentSelection.getObject()).getColorType());
        }
        doColorTypeUndo = true;
    }

    private void updateColorSelection() {
        updatingColorSelection = true;
        if (currentSelection.getObject() instanceof ColorExpression) {
            ColorType ct = colorTypeCombobox.getItemAt(colorTypeCombobox.getSelectedIndex());
            if (ct instanceof ProductType && !(currentSelection.getObject() instanceof TupleExpression)) {
                int currentIndex = ((ColorExpression) currentSelection.getObject()).getIndex();
                ColorType newColorType;
                if (currentIndex == -1)
                    newColorType = ((ProductType) ct).getConstituents().firstElement();
                else
                    newColorType = ((ProductType) ct).getConstituents().get(((ColorExpression) currentSelection.getObject()).getIndex());
                colorCombobox.updateColorType(newColorType, context, true, true);
            }
            ColorExpression exprToCheck = ((ColorExpression) currentSelection.getObject()).getBottomColorExpression();
            colorCombobox.updateSelection(exprToCheck);
        }
        updatingColorSelection = false;
    }

    private void addColorTypesToCombobox(List<ColorType> types) {
        colorTypeCombobox.removeAllItems();
        for (ColorType type : types) {
            colorTypeCombobox.addItem(type);
        }
    }

    private void deleteSelection() {
        if (currentSelection != null) {
            Expression replacement = null;
            if (currentSelection.getObject() instanceof GuardExpression) {
                replacement = new PlaceHolderGuardExpression();
            }
            else if (currentSelection.getObject() instanceof ColorExpression) {
                ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
                replacement = new PlaceHolderColorExpression(type);
            }
            if (replacement != null) {
                UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), replacement);
                newProperty = newProperty.replace(currentSelection.getObject(), replacement);
                updateSelection(replacement);
                undoSupport.postEdit(edit);
            }
        }
    }

    private void returnFromManualEdit(GuardExpression newExpr) {
        setExprFieldEditable(false);
        deleteExprSelectionButton.setEnabled(true);
        if (newExpr != null) {
            newProperty = newExpr;
        }

        updateSelection(newProperty);
        resetExprButton.setText("Reset Expression");
        editExprButton.setText("Edit Expression");

        updateEnabledButtons();
    }

    private void changeToEditMode() {
        setExprFieldEditable(true);
        deleteExprSelectionButton.setEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        andButton.setEnabled(false);
        orButton.setEnabled(false);
        notButton.setEnabled(false);
        equalityButton.setEnabled(false);
        inequalityButton.setEnabled(false);
        greaterThanButton.setEnabled(false);
        greaterThanEqButton.setEnabled(false);
        lessThanEqButton.setEnabled(false);
        lessThanButton.setEnabled(false);
        succButton.setEnabled(false);
        predButton.setEnabled(false);
        colorCombobox.setEnabled(false);
        colorTypeCombobox.setEnabled(false);
        resetExprButton.setText("Parse Expression");
        editExprButton.setText("Cancel");
        clearSelection();
        exprField.setCaretPosition(exprField.getText().length());
    }

    private void clearSelection() {
        exprField.select(0, 0);
        currentSelection = null;
    }

    public GuardExpression getExpression(){
        return newProperty;
    }

    private void setExprFieldEditable(boolean isEditable) {
        exprField.setEditable(isEditable);
        exprField.setFocusable(false);
        exprField.setFocusable(true);
        exprField.requestFocus(true);
    }

    public void onOK(pipe.gui.petrinet.undo.UndoManager undoManager) {
        Command cmd;
        if (newProperty instanceof PlaceHolderGuardExpression) {
            cmd = new SetTransitionExpressionCommand(transition, transition.getGuardExpression(), null);
        } else {
            cmd = new SetTransitionExpressionCommand(transition, transition.getGuardExpression(), newProperty);
        }
        cmd.redo();
        undoManager.addEdit(cmd);
    }

    private void replaceAndAddToUndo(Expression currentSelection, Expression newExpression) {
        if (currentSelection != null && newExpression != null) {
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection, newExpression);
            newProperty = newProperty.replace(currentSelection, newExpression);
            updateSelection(newExpression);
            undoSupport.postEdit(edit);
        }
    }

    private void updateColorType() {
        ColorType ct = colorTypeCombobox.getItemAt(colorTypeCombobox.getSelectedIndex());
        if (ct != null) {
            colorCombobox.updateColorType(ct, context, true, true);
        }
        updateEnabledButtons();
        if (doColorTypeUndo && !(currentSelection.getObject() instanceof PlaceHolderGuardExpression)) updateExpression();
    }

    private void updateExpression() {
        ColorType ct = colorTypeCombobox.getItemAt(colorTypeCombobox.getSelectedIndex());
        if (ct == getColorType(currentSelection.getObject())) return;

        Expression oldProperty = currentSelection.getObject();
        if (doColorTypeUndo) {
            replaceAndAddToUndo(oldProperty, getTypeReplacement(ct));
        } else {
            newProperty = newProperty.replace(oldProperty, updateChildren(newProperty, ct, currentSelection.getObject(), currentSelection.getObject().getChildren()));
            updateSelection(newProperty);
        }
    }

    private Expression getTypeReplacement(ColorType ct) {
        Expression replacement = newProperty.copy();
        replacement = findCurrentProperty(replacement, replacement);
        if (replacement != null) {
            return updateChildren(replacement, ct, replacement, replacement.getChildren());
        }
        return null;
    }

    private Expression findCurrentProperty(Expression original, Expression replacement) {
        if (original == replacement && replacement instanceof LeftRightGuardExpression) {
            return replacement;
        }

        for (ExprStringPosition exprStr : replacement.getChildren()) {
            if (exprStr.getObject() instanceof LeftRightGuardExpression) {
                if (original.indexOf(exprStr.getObject()).getStart() == currentSelection.getStart() &&
                    original.indexOf(exprStr.getObject()).getEnd() == currentSelection.getEnd()) {
                    return exprStr.getObject().copy();
                }
            } else {
                Expression expr = findCurrentProperty(original, exprStr.getObject());
                if (expr != null) return expr;
            }
        }
        return null;
    }

    private Expression updateChildren(Expression replaceProperty, ColorType ct, Expression parent, ExprStringPosition[] children) {
        for (ExprStringPosition child : children) {
            if (child.getObject() instanceof ColorExpression) {
                Expression expr;
                if (ct instanceof ProductType) {
                    expr = new TupleExpression(createPlaceholderVectors(ct.size()), ct);
                } else {
                    ColorType type = (ColorType) colorTypeCombobox.getSelectedItem();
                    expr = new PlaceHolderColorExpression(type);
                }
                replaceProperty = replaceProperty.replace(child.getObject(), expr);
            } else if (parent instanceof GuardExpression && child.getObject() instanceof LeftRightGuardExpression) {
                replaceProperty = updateChildren(replaceProperty, ct, child.getObject(), child.getObject().getChildren());
            }
        }
        return replaceProperty;
    }

    private ColorType getColorType(Expression expr) {
        if (expr instanceof ColorExpression) {
            return ((ColorExpression) expr).getColorType();
        } else if (expr instanceof GuardExpression) {
            return ((GuardExpression) expr).getColorType();
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////////
    // Undo support stuff
    // /////////////////////////////////////////////////////////////////////
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
        private final ColorType originalColorType;
        private final Expression replacement;
        private final ColorType replacementColorType;

        public Expression getOriginal() {
            return original;
        }

        public Expression getReplacement() {
            return replacement;
        }

        public ExpressionConstructionEdit(Expression original,
                                          Expression replacement) {
            this.original = original;
            this.originalColorType = getColorType(original);
            this.replacement = replacement;
            this.replacementColorType = colorTypeCombobox.getItemAt(colorTypeCombobox.getSelectedIndex());
        }

        @Override
        public void undo() throws CannotUndoException {
            newProperty = newProperty.replace(replacement, original);
            if (originalColorType != null) {
                doColorTypeUndo = false;
                colorTypeCombobox.setSelectedItem(originalColorType);
                doColorTypeUndo = true;
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            newProperty = newProperty.replace(original, replacement);
            if (replacementColorType != null) {
                doColorTypeUndo = false;
                colorTypeCombobox.setSelectedItem(replacementColorType);
                doColorTypeUndo = true;
            }
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
            public void actionPerformed(ActionEvent e) {  redoButton.doClick(); }
        });
        InputMap im = this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke('Z', shortcutkey), "undo");
        im.put(KeyStroke.getKeyStroke('Y', shortcutkey), "redo");
    }

}


