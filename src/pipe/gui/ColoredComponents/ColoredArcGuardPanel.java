package pipe.gui.ColoredComponents;

import dk.aau.cs.model.CPN.ColorType;
import pipe.gui.ColorComboboxPanel;
import pipe.gui.CreateGui;
import pipe.gui.widgets.EscapableDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class ColoredArcGuardPanel extends JPanel {
    public ColoredArcGuardPanel(){
        this.setLayout(new GridBagLayout());
        initPanels();
    }

    private void initPanels() {
        initArcExpressionPanel();
        initColoredTimedGuard();
        initWeightPanel();
        /*if(input) {
            if (transportArc) {
                transportWeightPanel = new JPanel(new GridBagLayout());
                int current = transportWeight;
                int min = 1;
                int max = 9999;
                int step = 1;
                SpinnerNumberModel numberModel = new SpinnerNumberModel(current, min, max, step);
                colorExpressionWeightSpinner = new JSpinner(numberModel);
                weightLabel = new JLabel("Weight:");
                ColorExpressionDialogPanel inputPanel = new ColorExpressionDialogPanel(getRootPane(), context, transportInputExpr, true);
                exprPanel.add(inputPanel, "input");
            } else {
                ArcExpressionPanel inputPanel = new ArcExpressionPanel(objectToBeEdited, context, true);
                exprPanel.add(inputPanel, "input");
            }
        }else {
            ArcExpressionPanel inputPanel = new ArcExpressionPanel(objectToBeEdited, context, true);
            exprPanel.add(inputPanel, "output");
        }
        if (transportArc) {
            ColorExpressionDialogPanel outputPanel = new ColorExpressionDialogPanel(getRootPane(), context, transportOutputExpr, true);
            exprPanel.add(outputPanel, "output");
        }
        if (input)
            initColoredTimedGuard();

        */
    }

    private void initWeightPanel(){
        JPanel transportWeightPanel = new JPanel(new GridBagLayout());
        //int current = transportWeight;
        int min = 1;
        int max = 9999;
        int step = 1;
        SpinnerNumberModel numberModel = new SpinnerNumberModel(1, min, max, step);
        JSpinner colorExpressionWeightSpinner = new JSpinner(numberModel);
        JLabel weightLabel = new JLabel("Weight:");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10 ,5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        transportWeightPanel.add(weightLabel, gbc);

        gbc.gridx = 1;
        transportWeightPanel.add(colorExpressionWeightSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(transportWeightPanel, gbc);
    }

    private void initColoredTimedGuard() {
        ColorType ct;
        /*if (!transportArc) {
            if(objectToBeEditedInput.underlyingTimedInputArc().source() instanceof SharedColoredPlace) {
                ct = ((SharedColoredPlace)objectToBeEditedInput.underlyingTimedInputArc().source()).getColorType();
                colorIntervalPanel = new ColorIntervalAndInvariantEditPanel(context, ct, true, objectToBeEditedInput);
            }else {
                ct = ((LocalColoredPlace) objectToBeEditedInput.underlyingTimedInputArc().source()).getColorType();
                colorIntervalPanel = new ColorIntervalAndInvariantEditPanel(context, ct, true, objectToBeEditedInput);
            }
        }
        else {
            if(objectToBeEditedTransport.underlyingTransportArc().source() instanceof  SharedColoredPlace) {
                ct = (((SharedColoredPlace) objectToBeEditedTransport.underlyingTransportArc().source()).getColorType());
                colorIntervalPanel = new ColorIntervalAndInvariantEditPanel(context, ct, true, objectToBeEditedInput);
            }else {
                ct = ((LocalColoredPlace) objectToBeEditedTransport.underlyingTransportArc().source()).getColorType();
                colorIntervalPanel = new ColorIntervalAndInvariantEditPanel(context, ct, true, objectToBeEditedTransport);
            }
        }*/

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(initColorConstraintPanel(), gbc);
    }

    private JPanel initColorConstraintPanel() {
        arcColorInvariantPanel = new JPanel(new GridBagLayout());
        ColorComboboxPanel colorComboboxPanel = new ColorComboboxPanel(colorType, "colors");

        JButton addTimeConstraintButton = new JButton("Add");
        JButton removeTimeConstraintButton = new JButton("Remove");
        JButton editTimeConstraintButton = new JButton("Edit");

        Dimension buttonSize = new Dimension(80, 27);

        addTimeConstraintButton.setPreferredSize(buttonSize);
        removeTimeConstraintButton.setPreferredSize(buttonSize);
        editTimeConstraintButton.setPreferredSize(buttonSize);

        ListModel timeConstraintListModel = new DefaultListModel();
        JList timeConstraintList = new JList(timeConstraintListModel);
        JScrollPane timeConstraintScrollPane = new JScrollPane(timeConstraintList);
        timeConstraintScrollPane.setViewportView(timeConstraintList);
        timeConstraintScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        timeConstraintScrollPane.setBorder(BorderFactory.createTitledBorder("Time interval for colors"));
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Color");
        tableModel.addColumn("Time interval");
        //   ((DefaultTableModel) tableModel).addRow(test);

        JTable table = new JTable(tableModel);
        table.setPreferredSize(new Dimension(300, 150));
        timeConstraintScrollPane.setPreferredSize(new Dimension(300, 150));
        //timeConstraintList.addMouseListener(createDoubleClickMouseAdapter());



        /*addTimeConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JComboBox[] comboBoxes = colorComboboxPanel.getColorTypeComboBoxesArray();
                AbstractTimeConstraint timeConstraint;

                if (!(colorType instanceof ProductType)) {
                    if (isInterval)
                        timeConstraint = ColoredTimeInterval.ZERO_INF_DYN_COLOR((Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex()));
                    else
                        timeConstraint = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR((Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex()));
                } else {
                    Vector<Color> colors = new Vector<Color>();
                    for (int i = 0; i < comboBoxes.length; i++) {
                        colors.add((Color) comboBoxes[i].getItemAt(comboBoxes[i].getSelectedIndex()));
                    }
                    Color color = new Color(colorType, 0, colors);
                    if (isInterval)
                        timeConstraint = ColoredTimeInterval.ZERO_INF_DYN_COLOR(color);
                    else
                        timeConstraint = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR(color);
                }
                boolean alreadyExists = false;
                for (int i = 0; i < timeConstraintListModel.size(); i++) {
                    if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i)))
                        alreadyExists = true;
                }
                if (alreadyExists) {
                    JOptionPane.showMessageDialog(null, "A time constraint for this color is already active and can be found in the list.");
                } else
                    timeConstraintListModel.addElement(timeConstraint);

            }

        });*/

        /*removeTimeConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int index = timeConstraintList.getSelectedIndex();
                Object star = timeConstraintListModel.elementAt(index);
                if(star instanceof ColoredTimeInterval) {
                    if(((ColoredTimeInterval) star).getColor().equals(Color.STAR_COLOR)) {
                        JOptionPane.showMessageDialog(null, "Star interval cannot be removed");
                    }else {
                        timeConstraintListModel.removeElementAt(timeConstraintList.getSelectedIndex());
                    }
                }else if(star instanceof ColoredTimeInvariant) {
                    if(((ColoredTimeInvariant) star).getColor().equals(Color.STAR_COLOR)) {
                        JOptionPane.showMessageDialog(null, "Star invariant cannot be removed");
                    }else{
                        timeConstraintListModel.removeElementAt(timeConstraintList.getSelectedIndex());
                    }
                }
            }
        });*/

        /*editTimeConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isInterval) {
                    EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Time Interval", true);
                    Container contentPane = guiDialog.getContentPane();

                    // 1 Set layout
                    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
                    ColoredTimeIntervalDialogPanel ctiPanel = new ColoredTimeIntervalDialogPanel(guiDialog.getRootPane(), context,(ColoredTimeInterval) timeConstraintListModel.getElementAt(timeConstraintList.getSelectedIndex()));
                    contentPane.add(ctiPanel);

                    guiDialog.setResizable(false);

                    // Make window fit contents' preferred size
                    guiDialog.pack();

                    // Move window to the middle of the screen
                    guiDialog.setLocationRelativeTo(null);
                    guiDialog.setVisible(true);

                    if (ctiPanel.isEditConfirmed())
                        timeConstraintListModel.set(timeConstraintList.getSelectedIndex(), ctiPanel.getNewTimeInterval());

                } else {
                    EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Time Invariant", true);
                    Container contentPane = guiDialog.getContentPane();

                    // 1 Set layout
                    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
                    ColoredTimeInvariantDialogPanel ctiPanel = new ColoredTimeInvariantDialogPanel(guiDialog.getRootPane(), context,(ColoredTimeInvariant) timeConstraintListModel.getElementAt(timeConstraintList.getSelectedIndex()), placeComp);
                    contentPane.add(ctiPanel);

                    guiDialog.setResizable(false);

                    // Make window fit contents' preferred size
                    guiDialog.pack();

                    // Move window to the middle of the screen
                    guiDialog.setLocationRelativeTo(null);
                    guiDialog.setVisible(true);

                    if (ctiPanel.didEdit)
                        timeConstraintListModel.set(timeConstraintList.getSelectedIndex(), ctiPanel.getNewTimeInvariant());
                }
            }
        });*/


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 3;
        arcColorInvariantPanel.add(colorComboboxPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3,3);
        buttonPanel.add(addTimeConstraintButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(removeTimeConstraintButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(editTimeConstraintButton, gbc);

        Dimension dim;
        dim = new Dimension(375, 200);
        timeConstraintScrollPane.setPreferredSize(dim);
        timeConstraintScrollPane.setMaximumSize(dim);
        timeConstraintScrollPane.setMinimumSize(dim);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        arcColorInvariantPanel.add(buttonPanel,gbc);


        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridheight = 2;
        arcColorInvariantPanel.add(timeConstraintScrollPane, gbc);

        return arcColorInvariantPanel;
    }

    private void initArcExpressionPanel(){
        exprPanel = new JPanel(new GridBagLayout());
        exprPanel.setBorder(BorderFactory.createTitledBorder("Arc Expressions"));
        initExprField();
        initNumberExpressionsPanel();
        initArithmeticPanel();
        initEditPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(exprPanel, gbc);
    }

    private void initEditPanel() {
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
        editPanel.setPreferredSize(new Dimension(260, 190));

        ButtonGroup editButtonsGroup = new ButtonGroup();
        JButton deleteExprSelectionButton = new JButton("Delete Selection");
        JButton resetExprButton = new JButton("Reset Expression");
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton editExprButton = new JButton("Edit Expression");
        editExprButton.setEnabled(true);

        //TODO: add tooltips to buttons

        editButtonsGroup.add(deleteExprSelectionButton);
        editButtonsGroup.add(resetExprButton);
        editButtonsGroup.add(undoButton);
        editButtonsGroup.add(redoButton);
        editButtonsGroup.add(editExprButton);

        /*deleteExprSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                deleteSelection();
            }
        });

        resetExprButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PlaceHolderArcExpression pHExpr = new PlaceHolderArcExpression();
                arcExpression = arcExpression.replace(arcExpression, pHExpr);
                updateSelection(pHExpr);
            }
        });*/

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
        gbc.fill = GridBagConstraints.VERTICAL;

        exprPanel.add(editPanel, gbc);
    }

    private void initArithmeticPanel() {
        JPanel arithmeticPanel = new JPanel(new GridBagLayout());
        arithmeticPanel.setBorder(BorderFactory.createTitledBorder("Arithmetic Expressions"));

        JButton additionButton = new JButton("Addition");
        JButton addAdditionPlaceHolderButton = new JButton("Add Placeholder");
        JButton subtractionButton = new JButton("Subtraction");
        JButton scalarButton = new JButton("Scalar");

        final Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelScalar = new SpinnerNumberModel(current, min, max, step);

        JSpinner scalarJSpinner = new JSpinner(numberModelScalar);

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

        addAdditionPlaceHolderButton.setPreferredSize(new Dimension(150, 30));

        /*additionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AddExpression addExpr;
                if (currentSelection.getObject() instanceof ArcExpression) {
                    Vector<ArcExpression> vExpr = new Vector();
                    vExpr.add((ArcExpression) currentSelection.getObject());
                    vExpr.add(new PlaceHolderArcExpression());
                    addExpr = new AddExpression(vExpr);
                    arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
                    updateSelection(addExpr);
                }
            }
        });

        subtractionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SubtractExpression subExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderArcExpression) {
                    subExpr = new SubtractExpression((PlaceHolderArcExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                }
                else if (currentSelection.getObject() instanceof SubtractExpression) {
                    subExpr = new SubtractExpression((SubtractExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                }
                else if (currentSelection.getObject() instanceof ScalarProductExpression) {
                    subExpr = new SubtractExpression((ScalarProductExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                } else if (currentSelection.getObject() instanceof NumberOfExpression || currentSelection.getObject() instanceof AddExpression) {
                    subExpr = new SubtractExpression((ArcExpression) currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                }
            }
        });

        scalarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ScalarProductExpression scalarExpr = null;
                Integer value = (Integer)scalarJSpinner.getValue();
                if (currentSelection.getObject() instanceof ArcExpression) {
                    scalarExpr = new ScalarProductExpression(value, (ArcExpression) currentSelection.getObject());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), scalarExpr);
                    updateSelection(scalarExpr);
                }
            }
        });

        addAdditionPlaceHolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (currentSelection.getObject() instanceof AddExpression) {
                    AddExpression addExpr = (AddExpression) currentSelection.getObject();
                    Vector<ArcExpression> vecExpr =  addExpr.getAddExpression();
                    vecExpr.add(new PlaceHolderArcExpression());
                    addExpr = new AddExpression(vecExpr);
                    arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
                    updateSelection(addExpr);
                }
            }
        });*/

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0,0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        arithmeticPanel.add(additionButton, gbc);

        gbc.gridy = 1;
        arithmeticPanel.add(addAdditionPlaceHolderButton, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        arithmeticPanel.add(separator,gbc);


        gbc.gridy = 3;
        arithmeticPanel.add(subtractionButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        arithmeticPanel.add(scalarJSpinner, gbc);

        gbc.gridx = 1;
        arithmeticPanel.add(scalarButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;

        exprPanel.add(arithmeticPanel,gbc);

    }

    private void initNumberExpressionsPanel() {
        JPanel numberExprPanel = new JPanel(new GridBagLayout());
        numberExprPanel.setBorder(BorderFactory.createTitledBorder("Numerical Expressions"));

        Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelNumber = new SpinnerNumberModel(current, min, max, step);
        SpinnerNumberModel numberModelAll = new SpinnerNumberModel(current, min, max, step);

        JSpinner numberExpressionJSpinner = new JSpinner(numberModelNumber);
        JButton numberExpressionButton = new JButton("Number Expr");



        JComboBox allExpressionComboBox = new JComboBox();
        JSpinner allExpressionJSpinner = new JSpinner(numberModelAll);
        JButton allExpressionButton = new JButton("All Expression");

        JButton addColorExpressionButton = new JButton("Edit Color Expr");

        /*for (ColorType element : context.activeModel().parentNetwork().colorTypes()) {
            allExpressionComboBox.addItem(element);
        }*/

        numberExpressionJSpinner.setPreferredSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMinimumSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMaximumSize(new Dimension(50, 27));

        allExpressionJSpinner.setPreferredSize(new Dimension(50, 27));
        allExpressionJSpinner.setMinimumSize(new Dimension(50, 27));
        allExpressionJSpinner.setMaximumSize(new Dimension(50, 27));

        allExpressionComboBox.setPreferredSize(new Dimension(150, 27));
        allExpressionComboBox.setMinimumSize(new Dimension(150, 27));
        allExpressionComboBox.setMaximumSize(new Dimension(150, 27));

        numberExpressionButton.setPreferredSize(new Dimension(125, 27));
        numberExpressionButton.setMinimumSize(new Dimension(125, 27));
        numberExpressionButton.setMaximumSize(new Dimension(125, 27));

        allExpressionButton.setPreferredSize(new Dimension(125, 27));
        allExpressionButton.setMinimumSize(new Dimension(125, 27));
        allExpressionButton.setMaximumSize(new Dimension(125, 27));

        addColorExpressionButton.setMaximumSize(new Dimension(125, 27));
        addColorExpressionButton.setMinimumSize(new Dimension(125, 27));
        addColorExpressionButton.setPreferredSize(new Dimension(125, 27));


       /* allExpressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addAllExpression();
            }
        });

        numberExpressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addNumberExpression();
            }
        });

        addColorExpressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (currentSelection.getObject() instanceof ColorExpression) {
                    ColorExpression colorExpr = (ColorExpression)currentSelection.getObject();

                    EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Color Expression", true);
                    Container contentPane = guiDialog.getContentPane();

                    ColorExpressionDialogPanel cep = new ColorExpressionDialogPanel(guiDialog.getRootPane(), context, colorExpr, false);
                    contentPane.add(cep);

                    guiDialog.setResizable(true);
                    guiDialog.pack();
                    guiDialog.setLocationRelativeTo(null);
                    guiDialog.setVisible(true);

                    if (cep.clickedOK == true) {
                        ColorExpression expr = cep.getColorExpression();
                        arcExpression = arcExpression.replace(currentSelection.getObject(), expr);
                        updateSelection(expr);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(CreateGui.getApp(), "You have to select a colored placeholder location <+> or a color expression already added in the expression to add a color expression.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });*/

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0,5 ,5 );
        numberExprPanel.add(allExpressionJSpinner, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.WEST;
        numberExprPanel.add(allExpressionComboBox, gbc);

        gbc.gridx = 2;
        numberExprPanel.add(allExpressionButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0,5 ,5 );
        numberExprPanel.add(numberExpressionJSpinner, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(numberExpressionButton, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(separator,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(addColorExpressionButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.VERTICAL;

        exprPanel.add(numberExprPanel, gbc);
    }

    private void initExprField () {
        JTextPane exprField = new JTextPane();

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
        Dimension d = new Dimension(880, 80);
        exprScrollPane.setPreferredSize(d);
        exprScrollPane.setMinimumSize(d);

        /*exprField.addMouseListener(new MouseAdapter() {
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
        });*/

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 4;
        exprPanel.add(exprScrollPane, gbc);
    }

    public void hideColorInvariantPanel(){
        arcColorInvariantPanel.setVisible(false);
    }

    private ColorType colorType;
    private JPanel exprPanel;
    JPanel arcColorInvariantPanel;
}
