package pipe.gui.ColoredComponents;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.Context;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.TAPNTransitionEditor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

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
    private JComboBox<Variable> variableComboBox;
    private JLabel variableValueLabel;
    private JComboBox<Color> variableValuesComboBox;
    private JButton editColorExpressionButton;

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


    private JPanel exprPanel;
    private JTextPane exprField;
    private JScrollPane exprScrollPane;

    private Context context;
    private TimedTransitionComponent transition;
    private List<Variable> variables;

    private GuardExpression newProperty;
    TAPNTransitionEditor parent;

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
        initExpr();
        updateSelection();

        //TODO: implement these
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        editExprButton.setEnabled(false);
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
                newProperty = newProperty.replace(currentSelection.getObject(), andExpr);
                updateSelection(andExpr);
            }
            else if (currentSelection.getObject() instanceof OrExpression) {
                andExpr = new AndExpression(((OrExpression) currentSelection.getObject()).getLeftExpression(), ((OrExpression) currentSelection.getObject()).getRightExpression());
                //andExpr = new AndExpression((OrExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
                newProperty = newProperty.replace(currentSelection.getObject(), andExpr);
                updateSelection(andExpr);
            }
            else if (currentSelection.getObject() instanceof NotExpression) {
                andExpr = new AndExpression((NotExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
                newProperty = newProperty.replace(currentSelection.getObject(), andExpr);
                updateSelection(andExpr);
            }
            else if (currentSelection.getObject() instanceof GuardExpression) {
                PlaceHolderGuardExpression ph = new PlaceHolderGuardExpression();
                andExpr = new AndExpression((GuardExpression)currentSelection.getObject(), ph);

                newProperty = newProperty.replace(currentSelection.getObject(), andExpr);
                updateSelection(andExpr);
            }
        });

        orButton.addActionListener(actionEvent -> {
            OrExpression orExpr = null;
            if (currentSelection.getObject() instanceof AndExpression) {
                orExpr = new OrExpression(((AndExpression) currentSelection.getObject()).getLeftExpression(), ((AndExpression) currentSelection.getObject()).getRightExpression());
                newProperty = newProperty.replace(currentSelection.getObject(), orExpr);
                updateSelection(orExpr);
            }
            else if (currentSelection.getObject() instanceof OrExpression) {
                orExpr = new OrExpression((AndExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
                newProperty = newProperty.replace(currentSelection.getObject(), orExpr);
                updateSelection(orExpr);
            }
            else if (currentSelection.getObject() instanceof NotExpression) {
                orExpr = new OrExpression((AndExpression)currentSelection.getObject(), new PlaceHolderGuardExpression());
                newProperty = newProperty.replace(currentSelection.getObject(), orExpr);
                updateSelection(orExpr);
            }
            else if (currentSelection.getObject() instanceof GuardExpression) {
                PlaceHolderGuardExpression ph = new PlaceHolderGuardExpression();
                orExpr = new OrExpression((GuardExpression) currentSelection.getObject(), ph);
                newProperty = newProperty.replace(currentSelection.getObject(), orExpr);
                updateSelection(orExpr);
            }
        });

        notButton.addActionListener(actionEvent -> {
            NotExpression notExpr = null;
            if (currentSelection.getObject() instanceof NotExpression) {
                notExpr = new NotExpression((NotExpression)currentSelection.getObject());
                newProperty = newProperty.replace(currentSelection.getObject(), notExpr);
                updateSelection(notExpr);
            }
            else if (currentSelection.getObject() instanceof AndExpression) {
                notExpr = new NotExpression((AndExpression)currentSelection.getObject());
                newProperty = newProperty.replace(currentSelection.getObject(), notExpr);
                updateSelection(notExpr);
            }
            else if (currentSelection.getObject() instanceof OrExpression) {
                notExpr = new NotExpression((OrExpression) currentSelection.getObject());
                newProperty = newProperty.replace(currentSelection.getObject(), notExpr);
                updateSelection(notExpr);
            }
            else if (currentSelection.getObject() instanceof EqualityExpression ||
                currentSelection.getObject() instanceof InequalityExpression ||
                currentSelection.getObject() instanceof GreaterThanExpression ||
                currentSelection.getObject() instanceof GreaterThanEqExpression ||
                currentSelection.getObject() instanceof LessThanExpression ||
                currentSelection.getObject() instanceof LessThanEqExpression) {
                notExpr = new NotExpression((GuardExpression)currentSelection.getObject());
                newProperty = newProperty.replace(currentSelection.getObject(), notExpr);
                updateSelection(notExpr);
            }
            else if (currentSelection.getObject() instanceof GuardExpression) {
                PlaceHolderGuardExpression ph = new PlaceHolderGuardExpression();
                notExpr = new NotExpression((GuardExpression)ph);
                newProperty = newProperty.replace(currentSelection.getObject(), notExpr);
                updateSelection(notExpr);
            }
        });
    }

    private void initComparisonPanel() {
        comparisonPanel = new JPanel(new GridBagLayout());
        comparisonPanel.setBorder(BorderFactory.createTitledBorder("Comparison"));

        ButtonGroup comparisonButtons = new ButtonGroup();
        equalityButton = new JButton("EQ");
        greaterThanEqButton = new JButton("GEQ");
        greaterThanButton = new JButton("G");
        inequalityButton = new JButton("IQ");
        lessThanEqButton = new JButton("LEQ");
        lessThanButton = new JButton("L");

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
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(inequalityButton, gbc);



        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(greaterThanEqButton, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(greaterThanButton, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        comparisonPanel.add(lessThanEqButton, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
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


        editColorExpressionButton = new JButton("Edit color expression");
        editColorExpressionButton.setPreferredSize(new Dimension(225, 30));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        comparisonPanel.add(editColorExpressionButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(comparisonPanel, gbc);


        editColorExpressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Logger.log(currentSelection.getObject());
                if (currentSelection.getObject() instanceof TupleExpression || currentSelection.getObject() instanceof ColorExpression) {


                    EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Color Expression", true);
                    Container contentPane = guiDialog.getContentPane();

                    ColorExpressionDialogPanel cep = new ColorExpressionDialogPanel(guiDialog.getRootPane(), context, (ColorExpression) currentSelection.getObject(), false);
                    contentPane.add(cep);

                    guiDialog.setResizable(true);
                    guiDialog.pack();
                    guiDialog.setLocationRelativeTo(null);
                    guiDialog.setVisible(true);

                    ColorExpression expr = cep.getColorExpression();

                    if (cep.clickedOK) {
                        if (currentSelection.getObject()instanceof EqualityExpression) {
                            EqualityExpression eqExpr= new EqualityExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), expr);
                            newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                            updateSelection(expr);
                            return;
                        }
                        newProperty = newProperty.replace(currentSelection.getObject(), expr);
                        updateSelection(expr);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(CreateGui.getApp(), "You have to either select a color expression or a colored placeholder <+>.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }



            }
        });

        equalityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                EqualityExpression eqExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                    eqExpr = new EqualityExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                    updateSelection(eqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                    eqExpr = new EqualityExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                    updateSelection(eqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                    eqExpr = new EqualityExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                    updateSelection(eqExpr);
                }
                else if (currentSelection.getObject() instanceof InequalityExpression) {
                    eqExpr = new EqualityExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                    updateSelection(eqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                    eqExpr = new EqualityExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                    updateSelection(eqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanExpression) {
                    eqExpr = new EqualityExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                    updateSelection(eqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                    eqExpr = new EqualityExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), eqExpr);
                    updateSelection(eqExpr);
                }
            }
        });

        greaterThanEqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                GreaterThanEqExpression greaterEqExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                    greaterEqExpr = new GreaterThanEqExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterEqExpr);
                    updateSelection(greaterEqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                    greaterEqExpr = new GreaterThanEqExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterEqExpr);
                    updateSelection(greaterEqExpr);
                }

                else if (currentSelection.getObject() instanceof InequalityExpression) {
                    greaterEqExpr = new GreaterThanEqExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterEqExpr);
                    updateSelection(greaterEqExpr);
                }
                else if (currentSelection.getObject() instanceof EqualityExpression) {
                    greaterEqExpr = new GreaterThanEqExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterEqExpr);
                    updateSelection(greaterEqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanExpression) {
                    greaterEqExpr = new GreaterThanEqExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterEqExpr);
                    updateSelection(greaterEqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                    greaterEqExpr = new GreaterThanEqExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterEqExpr);
                    updateSelection(greaterEqExpr);
                }
            }
        });

        greaterThanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                GreaterThanExpression greaterExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                    greaterExpr = new GreaterThanExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
                    updateSelection(greaterExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                    greaterExpr = new GreaterThanExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
                    updateSelection(greaterExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                    greaterExpr = new GreaterThanExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
                    updateSelection(greaterExpr);
                }
                else if (currentSelection.getObject() instanceof EqualityExpression) {
                    greaterExpr = new GreaterThanExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
                    updateSelection(greaterExpr);
                }
                else if (currentSelection.getObject() instanceof InequalityExpression) {
                    greaterExpr = new GreaterThanExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
                    updateSelection(greaterExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                    greaterExpr = new GreaterThanExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
                    updateSelection(greaterExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanExpression) {
                    greaterExpr = new GreaterThanExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), greaterExpr);
                    updateSelection(greaterExpr);
                }
            }
        });

        inequalityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                InequalityExpression iqExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                    iqExpr = new InequalityExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
                    updateSelection(iqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                    iqExpr = new InequalityExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
                    updateSelection(iqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                    iqExpr = new InequalityExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
                    updateSelection(iqExpr);
                }
                else if (currentSelection.getObject() instanceof EqualityExpression) {
                    iqExpr = new InequalityExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
                    updateSelection(iqExpr);
                }
                else if (currentSelection.getObject() instanceof InequalityExpression) {
                    iqExpr = new InequalityExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
                    updateSelection(iqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanExpression) {
                    iqExpr = new InequalityExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
                    updateSelection(iqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                    iqExpr = new InequalityExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), iqExpr);
                    updateSelection(iqExpr);
                }
            }
        });
        lessThanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LessThanExpression lessThanExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                    lessThanExpr = new LessThanExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
                    updateSelection(lessThanExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                    lessThanExpr = new LessThanExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
                    updateSelection(lessThanExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                    lessThanExpr = new LessThanExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
                    updateSelection(lessThanExpr);
                }
                else if (currentSelection.getObject() instanceof EqualityExpression) {
                    lessThanExpr = new LessThanExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
                    updateSelection(lessThanExpr);
                }
                else if (currentSelection.getObject() instanceof InequalityExpression) {
                    lessThanExpr = new LessThanExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
                    updateSelection(lessThanExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanExpression) {
                    lessThanExpr = new LessThanExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
                    updateSelection(lessThanExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                    lessThanExpr = new LessThanExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanExpr);
                    updateSelection(lessThanExpr);
                }
            }
        });

        lessThanEqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LessThanEqExpression lessThanEqExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderGuardExpression) {
                    lessThanEqExpr = new LessThanEqExpression(new PlaceHolderColorExpression(), new PlaceHolderColorExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
                    updateSelection(lessThanEqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanExpression) {
                    lessThanEqExpr = new LessThanEqExpression(((GreaterThanExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
                    updateSelection(lessThanEqExpr);
                }
                else if (currentSelection.getObject() instanceof GreaterThanEqExpression) {
                    lessThanEqExpr = new LessThanEqExpression(((GreaterThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((GreaterThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
                    updateSelection(lessThanEqExpr);
                }
                else if (currentSelection.getObject() instanceof InequalityExpression) {
                    lessThanEqExpr = new LessThanEqExpression(((InequalityExpression) currentSelection.getObject()).getLeftExpression(), ((InequalityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
                    updateSelection(lessThanEqExpr);
                }
                else if (currentSelection.getObject() instanceof EqualityExpression) {
                    lessThanEqExpr = new LessThanEqExpression(((EqualityExpression) currentSelection.getObject()).getLeftExpression(), ((EqualityExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
                    updateSelection(lessThanEqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanEqExpression) {
                    lessThanEqExpr = new LessThanEqExpression(((LessThanEqExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanEqExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
                    updateSelection(lessThanEqExpr);
                }
                else if (currentSelection.getObject() instanceof LessThanExpression) {
                    lessThanEqExpr = new LessThanEqExpression(((LessThanExpression) currentSelection.getObject()).getLeftExpression(), ((LessThanExpression) currentSelection.getObject()).getRightExpression());
                    newProperty = newProperty.replace(currentSelection.getObject(), lessThanEqExpr);
                    updateSelection(lessThanEqExpr);
                }
            }
        });

    }

    private void initExprEditPanel() {
        editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
        editPanel.setPreferredSize(new Dimension(260, 190));

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

        deleteExprSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                deleteSelection();
            }
        });

        editExprButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (exprField.isEditable()) {
                    returnFromManualEdit(null);
                } else {
                    changeToEditMode();
                }
            }
        });

        resetExprButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PlaceHolderGuardExpression phGuard = new PlaceHolderGuardExpression();
                newProperty = newProperty.replace(newProperty, phGuard);
                updateSelection(phGuard);
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
        Dimension d = new Dimension(880, 80);
        exprScrollPane.setPreferredSize(d);
        exprScrollPane.setMinimumSize(d);

        exprField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!exprField.isEditable()) {
                    updateSelection();
                }
            }
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
                    if(currentSelection.getObject() instanceof ColorExpression){
                        editColorExpressionButton.doClick();
                    }
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
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 3;
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
            editColorExpressionButton.setEnabled(false);
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
            editColorExpressionButton.setEnabled(true);
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
            editColorExpressionButton.setEnabled(false);
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
        if (newProperty.containsPlaceHolder()) {
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
                newProperty = newProperty.replace(currentSelection.getObject(), replacement);
                updateSelection(replacement);
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
    //TODO: implement properly
    private void returnFromManualEdit(GuardExpression newExpr) {
        setExprFieldEditable(false);
        //  if (newExpr)
    }

    private void changeToEditMode() {
        setExprFieldEditable(true);
        resetExprButton.setText("Parse Query");
        editExprButton.setText("Cancel");
        clearSelection();
        //TODO:disable buttons not needed in edit mode
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

    public void onOK() {
        if (newProperty instanceof PlaceHolderGuardExpression) {
            transition.underlyingTransition().setGuard(null);
        } else {
            transition.underlyingTransition().setGuard(newProperty);
        }
        /*if (urgentCheckBox.isSelected()) {
            List<TimedInputArc> oldInputArcs = transition.underlyingTransition().getInputArcs();
            List<TimedInputArc> newInputArcs = new ArrayList<TimedInputArc>();
            List<TransportArc> oldTransportArcs = transition.underlyingTransition().getTransportArcsGoingThrough();
            List<TransportArc> newTransportArcs = new ArrayList<TransportArc>();

            for (TransportArc transportArc : oldTransportArcs) {
                ColoredTransportArc coloredTransportArc = (ColoredTransportArc) transportArc;
                coloredTransportArc.setCtiList(new ArrayList<ColoredTimeInterval>(){{add(ColoredTimeInterval.ZERO_INF_DYN_COLOR(Color.STAR_COLOR));}});
                newTransportArcs.add(coloredTransportArc);
            }

            for (TimedInputArc oldInputArc : oldInputArcs) {
                ColoredInputArc coloredInputArc = (ColoredInputArc) oldInputArc;
                List<ColoredTimeInterval> ctiList = new ArrayList<ColoredTimeInterval>() {{add(ColoredTimeInterval.ZERO_INF_DYN_COLOR(Color.STAR_COLOR));}};
                coloredInputArc.setColorTimeIntervals(ctiList);
                newInputArcs.add(coloredInputArc);
            }

            for (int i = 0; i < newTransportArcs.size(); i++) {
                transition.underlyingTransition().removeTransportArcGoingThrough(oldTransportArcs.get(i));
                transition.underlyingTransition().addTransportArcGoingThrough(newTransportArcs.get(i));
            }

            for (int i = 0; i <  newInputArcs.size(); i++) {
                transition.underlyingTransition().removeFromPreset(oldInputArcs.get(i));
                transition.underlyingTransition().addToPreset(newInputArcs.get(i));
            }
        }*/
        //context.tabContent().network().paintNet();
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
            updateSelection(lesseQExpr);;
        }
        else if (expression instanceof LessThanExpression) {
            LessThanExpression lessExpr = new LessThanExpression(((LessThanExpression) expression).getLeftExpression(), ((LessThanExpression) expression).getRightExpression());
            newProperty = newProperty.replace(currentSelection.getObject(), lessExpr);
            updateSelection(lessExpr);
        }

    }
}
