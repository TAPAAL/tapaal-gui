package pipe.gui.ColoredComponents;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.components.ColorComboBoxRenderer;
import dk.aau.cs.gui.undo.Colored.SetTransitionExpressionCommand;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.GuardExpressionParser.GuardExpressionParser;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.widgets.TAPNTransitionEditor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;

public class ColoredTransitionGuardPanel  extends JPanel {
    JPanel coloredTransitionPanel;
    JLabel guardExpressionLabel;

    //Edit expression buttons
    private JPanel editPanel;
    private ButtonGroup editButtonsGroup;
    private JButton resetExprButton;
    private JButton deleteExprSelectionButton;
    private JButton editExprButton;
    private JButton undoButton;
    private JButton redoButton;

    //Variable interaction elements
    private JPanel comparisonPanel;
    private JLabel variableLabel;

    //Logic buttons
    private JPanel logicPanel;
    private ButtonGroup logicButtonGroup;
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
    JComboBox<ColorType> colorTypeCombobox;
    JLabel colorTypeLabel;
    ColorComboboxPanel colorCombobox;
    JLabel colorLabel;
    JButton addColorButton;

    private JPanel exprPanel;
    private JTextPane exprField;
    private JScrollPane exprScrollPane;

    private Context context;
    private TimedTransitionComponent transition;
    private List<Variable> variables;

    private GuardExpression newProperty;
    TAPNTransitionEditor parent;
    ExpressionConstructionUndoManager undoManager;
    UndoableEditSupport undoSupport;

    private ExprStringPosition currentSelection = null;
    public ColoredTransitionGuardPanel(TimedTransitionComponent transition, Context context, TAPNTransitionEditor parent){
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder("Guard Expression"));
        this.transition = transition;
        this.context = context;
        this.parent = parent;
        initExprField();
        initLogicPanel();
        initComparisonPanel();
        initExprEditPanel();
        initColorExpressionPanel();
        initExpr();
        updateSelection();
        updateColorType();

        //TODO: implement these
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        undoManager = new ColoredTransitionGuardPanel.ExpressionConstructionUndoManager();
        undoSupport = new UndoableEditSupport();
        undoSupport.addUndoableEditListener(new ColoredTransitionGuardPanel.UndoAdapter());
        refreshUndoRedo();
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
                predExpr = new PredecessorExpression((ColorExpression) currentSelection.getObject());
                UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), predExpr);
                newProperty = newProperty.replace(currentSelection.getObject(), predExpr);
                updateSelection(predExpr);
                undoSupport.postEdit(edit);
            }
        });

        succButton.addActionListener(actionEvent -> {
            SuccessorExpression succExpr;
            if (currentSelection.getObject() instanceof  ColorExpression) {
                succExpr = new SuccessorExpression((ColorExpression) currentSelection.getObject());
                UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), succExpr);
                newProperty = newProperty.replace(currentSelection.getObject(), succExpr);
                updateSelection(succExpr);
                undoSupport.postEdit(edit);
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

        colorTypeLabel = new JLabel("Color Type: ");
        colorTypeCombobox = new JComboBox();
        //colorTypeCombobox.setPreferredSize(new Dimension(200,25));
        colorTypeCombobox.setRenderer(new ColorComboBoxRenderer(colorTypeCombobox));


        for (ColorType element : context.network().colorTypes()) {
            colorTypeCombobox.addItem(element);
        }

        if (colorTypeCombobox.getItemCount() != 0) {
            colorTypeCombobox.setSelectedIndex(0);
        }

        colorLabel = new JLabel("Color: ");
        colorCombobox = new ColorComboboxPanel((ColorType)colorTypeCombobox.getSelectedItem(), "",false,context) {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {

            }
        };
        colorCombobox.removeScrollPaneBorder();
        addColorButton = new JButton("Add Color");
        Dimension addDim = new Dimension(120, 30);
        addColorButton.setPreferredSize(addDim);
        addColorButton.setMinimumSize(addDim);
        addColorButton.setMaximumSize(addDim);

        addColorButton.addActionListener(actionEvent -> {
            Expression newExpression;
            if (colorCombobox.getColorTypeComboBoxesArray().length > 1) {
                Vector<ColorExpression> tempVec = new Vector();
                for (int i = 0; i < colorCombobox.getColorTypeComboBoxesArray().length; i++) {
                    ColorExpression expr;
                    Object selectedElement = colorCombobox.getColorTypeComboBoxesArray()[i].getSelectedItem();
                    if ( selectedElement instanceof String) {
                        expr = new AllExpression(((ProductType)colorCombobox.getColorType()).getColorTypes().get(i));
                    }else if(selectedElement instanceof Variable){
                        expr = new VariableExpression((Variable)selectedElement);
                    } else {
                        expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) selectedElement);
                    }
                    tempVec.add(expr);
                }
                newExpression = new TupleExpression(tempVec);
            } else {
                ColorExpression expr;
                Object selectedElement = colorCombobox.getColorTypeComboBoxesArray()[0].getSelectedItem();
                if (selectedElement instanceof String) {
                    expr = new AllExpression(colorCombobox.getColorType());
                } else if(selectedElement instanceof Variable){
                    expr = new VariableExpression((Variable)selectedElement);
                }
                else {
                    expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) selectedElement);
                }
                newExpression = expr;
            }
            if (currentSelection.getObject() instanceof ColorExpression) {
                UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), newExpression);
                newProperty = newProperty.replace(currentSelection.getObject(), newExpression);
                updateSelection(newExpression);
                undoSupport.postEdit(edit);

            }
        });

        colorTypeCombobox.addActionListener(actionEvent -> updateColorType());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3,3,3,3);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        colorExpressionPanel.add(colorTypeCombobox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,3,3,3);
        colorExpressionPanel.add(colorCombobox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 3,0 ,0);
        colorExpressionPanel.add(addColorButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0,0);
        colorExpressionPanel.add(colorExpressionButtons, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(colorExpressionPanel, gbc);
    }

    private void initLogicPanel() {
        logicPanel = new JPanel(new GridBagLayout());
        logicPanel.setBorder(BorderFactory.createTitledBorder("Logic"));
        Dimension d = new Dimension(100, 150);
        logicPanel.setPreferredSize(d);
        logicPanel.setMinimumSize(d);

        logicButtonGroup = new ButtonGroup();
        andButton = new JButton("AND");
        orButton = new JButton("OR");
        notButton = new JButton("NOT");

        //TODO: set tooltip for all three buttons

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
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(logicPanel, gbc);

        andButton.addActionListener(actionEvent -> {
            AndExpression andExpr = null;
            if (currentSelection.getObject() instanceof AndExpression) {
                andExpr = new AndExpression((AndExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
            }
            else if (currentSelection.getObject() instanceof OrExpression) {
                andExpr = new AndExpression(((OrExpression) currentSelection.getObject()).getLeftExpression(), ((OrExpression) currentSelection.getObject()).getRightExpression());
                //andExpr = new AndExpression((OrExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());

            }
            else if (currentSelection.getObject() instanceof NotExpression) {
                andExpr = new AndExpression((NotExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
            }
            else if (currentSelection.getObject() instanceof GuardExpression) {
                PlaceHolderGuardExpression ph = new PlaceHolderGuardExpression();
                andExpr = new AndExpression((GuardExpression)currentSelection.getObject(), ph);
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), andExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), andExpr);
            updateSelection(andExpr);
            undoSupport.postEdit(edit);

        });

        orButton.addActionListener(actionEvent -> {
            OrExpression orExpr = null;
            if (currentSelection.getObject() instanceof AndExpression) {
                orExpr = new OrExpression(((AndExpression) currentSelection.getObject()).getLeftExpression(), ((AndExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof OrExpression) {
                orExpr = new OrExpression((AndExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
            }
            else if (currentSelection.getObject() instanceof NotExpression) {
                orExpr = new OrExpression((AndExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
            }
            else if (currentSelection.getObject() instanceof GuardExpression) {
                PlaceHolderGuardExpression ph = new PlaceHolderGuardExpression();
                orExpr = new OrExpression((GuardExpression) currentSelection.getObject(), ph);
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), orExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), orExpr);
            updateSelection(orExpr);
            undoSupport.postEdit(edit);
        });

        notButton.addActionListener(actionEvent -> {
            NotExpression notExpr = null;
            if (currentSelection.getObject() instanceof NotExpression) {
                notExpr = new NotExpression((NotExpression)currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof AndExpression) {
                notExpr = new NotExpression((AndExpression)currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof OrExpression) {
                notExpr = new NotExpression((OrExpression) currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof EqualityExpression ||
                currentSelection.getObject() instanceof InequalityExpression ||
                currentSelection.getObject() instanceof GreaterThanExpression ||
                currentSelection.getObject() instanceof GreaterThanEqExpression ||
                currentSelection.getObject() instanceof LessThanExpression ||
                currentSelection.getObject() instanceof LessThanEqExpression) {
                notExpr = new NotExpression((GuardExpression)currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof GuardExpression) {
                PlaceHolderGuardExpression ph = new PlaceHolderGuardExpression();
                notExpr = new NotExpression((GuardExpression)ph);
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), notExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), notExpr);
            updateSelection(notExpr);
            undoSupport.postEdit(edit);
        });
    }

    private void initComparisonPanel() {
        comparisonPanel = new JPanel(new GridBagLayout());
        comparisonPanel.setBorder(BorderFactory.createTitledBorder("Comparison"));

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
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(equalityButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(inequalityButton, gbc);



        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(greaterThanEqButton, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(greaterThanButton, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(lessThanEqButton, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(lessThanButton, gbc);

        JSeparator seperator = new JSeparator(SwingConstants.HORIZONTAL);
        seperator.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        comparisonPanel.add(seperator, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(comparisonPanel, gbc);


        equalityButton.addActionListener(actionEvent -> {
            EqualityExpression eqExpr = null;
            if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                eqExpr = new EqualityExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                eqExpr = new EqualityExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                eqExpr = new EqualityExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof InequalityExpression) {
                eqExpr = new EqualityExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                eqExpr = new EqualityExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanExpression) {
                eqExpr = new EqualityExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                eqExpr = new EqualityExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), eqExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
            updateSelection(eqExpr);
            undoSupport.postEdit(edit);
        });

        greaterThanEqButton.addActionListener(actionEvent -> {
            GreaterThanEqExpression greaterEqExpr = null;
            if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                greaterEqExpr = new GreaterThanEqExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                greaterEqExpr = new GreaterThanEqExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
            }

            else if (currentSelection.getObject() instanceof InequalityExpression) {
                greaterEqExpr = new GreaterThanEqExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof EqualityExpression) {
                greaterEqExpr = new GreaterThanEqExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanExpression) {
                greaterEqExpr = new GreaterThanEqExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                greaterEqExpr = new GreaterThanEqExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), greaterEqExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), greaterEqExpr);
            updateSelection(greaterEqExpr);
            undoSupport.postEdit(edit);
        });

        greaterThanButton.addActionListener(actionEvent -> {
            GreaterThanExpression greaterExpr = null;
            if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                greaterExpr = new GreaterThanExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                greaterExpr = new GreaterThanExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                greaterExpr = new GreaterThanExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof EqualityExpression) {
                greaterExpr = new GreaterThanExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof InequalityExpression) {
                greaterExpr = new GreaterThanExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                greaterExpr = new GreaterThanExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanExpression) {
                greaterExpr = new GreaterThanExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), greaterExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
            updateSelection(greaterExpr);
            undoSupport.postEdit(edit);
        });

        inequalityButton.addActionListener(actionEvent -> {
            InequalityExpression iqExpr = null;
            if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                iqExpr = new InequalityExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                iqExpr = new InequalityExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                iqExpr = new InequalityExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof EqualityExpression) {
                iqExpr = new InequalityExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof InequalityExpression) {
                iqExpr = new InequalityExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanExpression) {
                iqExpr = new InequalityExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                iqExpr = new InequalityExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), iqExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
            updateSelection(iqExpr);
            undoSupport.postEdit(edit);
        });
        lessThanButton.addActionListener(actionEvent -> {
            LessThanExpression lessThanExpr = null;
            if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                lessThanExpr = new LessThanExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                lessThanExpr = new LessThanExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                lessThanExpr = new LessThanExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof EqualityExpression) {
                lessThanExpr = new LessThanExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof InequalityExpression) {
                lessThanExpr = new LessThanExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanExpression) {
                lessThanExpr = new LessThanExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                lessThanExpr = new LessThanExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), lessThanExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
            updateSelection(lessThanExpr);
            undoSupport.postEdit(edit);
        });

        lessThanEqButton.addActionListener(actionEvent -> {
            LessThanEqExpression lessThanEqExpr = null;
            if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                lessThanEqExpr = new LessThanEqExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                lessThanEqExpr = new LessThanEqExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                lessThanEqExpr = new LessThanEqExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof InequalityExpression) {
                lessThanEqExpr = new LessThanEqExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof EqualityExpression) {
                lessThanEqExpr = new LessThanEqExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                lessThanEqExpr = new LessThanEqExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
            }
            else if (currentSelection.getObject() instanceof LessThanExpression) {
                lessThanEqExpr = new LessThanEqExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
            }
            UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), lessThanEqExpr);
            newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
            updateSelection(lessThanEqExpr);
            undoSupport.postEdit(edit);
        });

    }

    private void initExprEditPanel() {
        editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
        //editPanel.setPreferredSize(new Dimension(260, 190));

        editButtonsGroup = new ButtonGroup();
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
                        CreateGui.getApp(),
                        "TAPAAL encountered an error trying to parse the specified query with the following error: \n\n" + ex.getMessage()+ ".\n\nWe recommend using the query construction buttons unless you are an experienced user.\n\n The specified query has not been saved. Do you want to edit it again?",
                        "Error Parsing Query",
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
                //queryChanged();
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
                //queryChanged();
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

        //TODO: Actionlisteners

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
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




        exprScrollPane = new JScrollPane(exprField);
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

        exprField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }
        });

        exprField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!exprField.isEditable()) {
                    //TODO: see line 1232 in CTLQueryDialog for impl example.
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

    private void initExpr() {
        TimedTransition t = transition.underlyingTransition();
        if (t.getGuard() != null) {
            newProperty = new PlaceHolderGuardExpression();
            updateSelection(newProperty);
            parseExpression(t.getGuard());
        }
        else {
            newProperty = new PlaceHolderGuardExpression();
            exprField.setText(newProperty.toString());
        }
    }

    private void addPropertyToExpr(Expression property) {

        //TODO: add undo functionality - line 1447 in CTLQueryDialog
        newProperty =  newProperty.replace(currentSelection.getObject(), property);

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
            succButton.setEnabled(false);
            predButton.setEnabled(false);
            colorCombobox.setEnabled(false);
            colorTypeCombobox.setEnabled(false);
            addColorButton.setEnabled(false);
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
            if(!(currentSelection.getObject() instanceof PlaceHolderExpression)){
                succButton.setEnabled(true);
                predButton.setEnabled(true);
            }
            colorCombobox.setEnabled(true);
            colorTypeCombobox.setEnabled(true);
            addColorButton.setEnabled(true);;
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
            colorTypeCombobox.setEnabled(false);
            addColorButton.setEnabled(false);

        }
        if(newProperty.containsPlaceHolder() && !(newProperty instanceof PlaceHolderExpression)){
            parent.enableOKButton(false);
        } else{
            parent.enableOKButton(true);
        }
    }

    private void updateSelection(Expression newSelection) {
        exprField.setText(newProperty.toString());

        ExprStringPosition position;
        if (!(newSelection instanceof  ColorExpression) && newProperty.containsPlaceHolder()) {
            Expression ge = newProperty.findFirstPlaceHolder();
            position = newProperty.indexOf(ge);
        }
        else {
            position = newProperty.indexOf(newSelection);
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        updateEnabledButtons();
    }

    private void updateSelection() {
        int index = exprField.getCaretPosition();
        ExprStringPosition position = newProperty.objectAt(index);

        if (position == null) {
            return;
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;
        Logger.log(currentSelection.getObject());
        updateEnabledButtons();

        //TODO: updateexprButtonsAccordingToSelection; line 573
    }

    private void deleteSelection() {
        if (currentSelection != null) {
            Expression replacement = null;
            if (currentSelection.getObject() instanceof GuardExpression) {
                replacement = getSpecificChildOfProperty(1, currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof ColorExpression) {
                replacement = new PlaceHolderColorExpression();
            }
            if (replacement != null) {
                UndoableEdit edit = new ExpressionConstructionEdit(currentSelection.getObject(), replacement);
                newProperty = newProperty.replace(currentSelection.getObject(), replacement);
                updateSelection(replacement);
                undoSupport.postEdit(edit);
            }
        }
    }

    private GuardExpression getSpecificChildOfProperty(int number, Expression property) {
        ExprStringPosition[] children = property.getChildren();
        int count = 0;
        for (int i = 0; i < children.length; i++) {
            Expression child = children[i].getObject();
            if (child instanceof GuardExpression) {
                count++;
            }
            if (count == number) {
                return (GuardExpression) child;
            }
        }

        return new PlaceHolderGuardExpression();
    }

    private void returnFromManualEdit(GuardExpression newExpr) {
        setExprFieldEditable(false);
        deleteExprSelectionButton.setEnabled(true);
        if (newExpr != null)
            newProperty = newExpr;

        updateSelection(newProperty);
        resetExprButton.setText("Reset Query");
        editExprButton.setText("Edit Query");

        /*resetExprButton.setToolTipText(TOOL_TIP_RESETBUTTON);
        editExprButton.setToolTipText(TOOL_TIP_EDITQUERYBUTTON);*/
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
        addColorButton.setEnabled(false);
        resetExprButton.setText("Parse Query");
        editExprButton.setText("Cancel");
        clearSelection();
        exprField.setCaretPosition(exprField.getText().length());
    }

    private void clearSelection() {
        exprField.select(0, 0);
        currentSelection = null;

    }

    private void setExprFieldEditable(boolean isEditable) {
        exprField.setEditable(isEditable);
        exprField.setFocusable(false);
        exprField.setFocusable(true);
        exprField.requestFocus(true);
    }

    public void onOK(pipe.gui.undo.UndoManager undoManager) {
        if (newProperty instanceof PlaceHolderGuardExpression) {
            Command cmd = new SetTransitionExpressionCommand(transition, transition.getGuardExpression(), null);
            cmd.redo();
            undoManager.addEdit(cmd);
        } else {
            Command cmd = new SetTransitionExpressionCommand(transition, transition.getGuardExpression(), newProperty);
            cmd.redo();
            undoManager.addEdit(cmd);
        }
    }

    private void parseExpression(GuardExpression expression) {
        if (expression instanceof AndExpression) {
            AndExpression andExpr = new AndExpression(((AndExpression) expression).getLeftExpression(), ((AndExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), andExpr);
            updateSelection(andExpr);
        }
        else if (expression instanceof OrExpression) {
            OrExpression orExpr = new OrExpression(((OrExpression) expression).getLeftExpression(), ((OrExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), orExpr);
            updateSelection(orExpr);
        }

        else if (expression instanceof NotExpression) {
            NotExpression notExpr = new NotExpression(((NotExpression) expression).getExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), notExpr);
            updateSelection(notExpr);
        }
        else if (expression instanceof EqualityExpression) {
            EqualityExpression eqExpr = new EqualityExpression(((EqualityExpression) expression).getLeftExpression(), ((EqualityExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
            updateSelection(eqExpr);
        }
        else if (expression instanceof InequalityExpression) {
            InequalityExpression iqExpr = new InequalityExpression(((InequalityExpression) expression).getLeftExpression(), ((InequalityExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
            updateSelection(iqExpr);
        }
        else if (expression instanceof GreaterThanEqExpression) {
            GreaterThanEqExpression gEQExpr = new GreaterThanEqExpression(((GreaterThanEqExpression) expression).getLeftExpression(), ((GreaterThanEqExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), gEQExpr);
            updateSelection(gEQExpr);
        }
        else if (expression instanceof GreaterThanExpression) {
            GreaterThanExpression greaterExpr = new GreaterThanExpression(((GreaterThanExpression) expression).getLeftExpression(), ((GreaterThanExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
            updateSelection(greaterExpr);
        }
        else if (expression instanceof LessThanEqExpression) {
            LessThanEqExpression lesseQExpr = new LessThanEqExpression(((LessThanEqExpression) expression).getLeftExpression(), ((LessThanEqExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), lesseQExpr);
            updateSelection(lesseQExpr);
        }
        else if (expression instanceof LessThanExpression) {
            LessThanExpression lessExpr = new LessThanExpression(((LessThanExpression) expression).getLeftExpression(), ((LessThanExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), lessExpr);
            updateSelection(lessExpr);
        }

    }
    private void updateColorType() {
        ColorType ct;
        ct = colorTypeCombobox.getItemAt(colorTypeCombobox.getSelectedIndex());
        if (ct != null) {
            colorCombobox.updateColorType(ct, context);
        }
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
        private Expression original;
        private Expression replacement;

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
            newProperty = newProperty.replace(replacement, original);
        }

        @Override
        public void redo() throws CannotRedoException {
            newProperty = newProperty.replace(original, replacement);
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

}


