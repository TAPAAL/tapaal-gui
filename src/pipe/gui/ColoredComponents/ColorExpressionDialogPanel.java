package pipe.gui.ColoredComponents;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.Context;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class ColorExpressionDialogPanel extends JPanel {

    public boolean clickedOK;

    JRootPane rootPane;
    Context context;
    ExprStringPosition currentSelection;
    ColorExpression expr;
    JPanel exprPanel;
    JTextPane exprField;
    JScrollPane exprScrollPane;

    JPanel colortypePanel;
    JComboBox<ColorType> colorTypeCombobox;
    JLabel colorTypeLabel;
    JComboBox<Variable> variableCombobox;
    JLabel variableLabel;
    JButton addVariableButton;
    JComboBox<dk.aau.cs.model.CPN.Color> colorCombobox;
    JLabel colorLabel;
    JButton addColorButton;

    JPanel colorExpressionButtons;
    JButton predButton;
    JButton succButton;
    JButton addPlaceHolderButton;

    JPanel editPanel;
    JButton deleteExprSelectionButton;
    JButton resetExprButton;
    JButton undoButton;
    JButton redoButton;
    JButton editExprButton;

    JPanel exitPanel;
    JButton cancelButton;
    JButton OKButton;
    ButtonGroup exitButtons;

    private boolean transport;
    private boolean isArc;
    private ColorType arcColorType;


    public ColorExpressionDialogPanel(Context context, ColorExpression expr, boolean transport, ColorType ct) {
        this.rootPane = rootPane;
        this.context = context;
        this.expr = expr;
        this.transport = transport;
        arcColorType = ct;
        this.isArc = true;
        initComponents();
    }

    public ColorExpressionDialogPanel(JRootPane rootPane, Context context, ColorExpression expr, boolean transport, ColorType ct) {
        this.rootPane = rootPane;
        this.context = context;
        this.expr = expr;
        this.transport = transport;
        arcColorType = ct;
        this.isArc = true;
        initComponents();
    }

    public ColorExpressionDialogPanel(JRootPane rootPane, Context context, ColorExpression expr, boolean transport) {
        this.rootPane = rootPane;
        this.context = context;
        this.expr = expr;
        this.transport = transport;
        this.isArc = false;
        arcColorType = null;
        initComponents();
    }

    public ColorExpressionDialogPanel(JRootPane rootPane, Context context) {
        this.rootPane = rootPane;
        this.context = context;
        this.transport = false;
        this.isArc = false;
        initComponents();
    }

    public void initComponents() {
        initPanels();
        updateColorType();
        updateSelection();
        //TODO: implement these
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        editExprButton.setEnabled(false);
    }

    public void initPanels() {
        setLayout(new BorderLayout());
        exprPanel = new JPanel(new GridBagLayout());
        exprPanel.setBorder(BorderFactory.createTitledBorder("Color Expression"));

        initExprField();
        initColorTypePanel();
        initButtonsPanel();
        initExprEditPanel();
        initExitButtons();

        add(exprPanel, BorderLayout.CENTER);
    }

    public ColorExpression getColorExpression() {
        return expr;
    }

    private ColorExpression getSpecificChildOfProperty(int number, Expression property) {
        ExprStringPosition[] children = property.getChildren();
        int count = 0;
        for (int i = 0; i < children.length; i++) {
            Expression child = children[i].getObject();
            if (child instanceof ColorExpression) {
                count++;
            }
            if (count == number) {
                return (ColorExpression) child;
            }
        }
        return new PlaceHolderColorExpression();
    }

    private void deleteSelection() {
        if (currentSelection != null) {
            ColorExpression replacement = null;
            if (currentSelection.getObject() instanceof ColorExpression) {
                replacement = getSpecificChildOfProperty(1, currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof ColorExpression) {
                replacement = new PlaceHolderColorExpression();
            }
            if (replacement != null) {
                expr = expr.replace(currentSelection.getObject(), replacement);
                updateSelection(replacement);
            }
        }
    }

    public void updateSelection() {
        int index = exprField.getCaretPosition();

        //todo fix null-pointer exception
        ExprStringPosition position = expr.objectAt(index);

        if (position == null) {
            return;
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;


        toggleEnabledButtons();

        Logger.log(currentSelection.getObject());
    }

    public void updateSelection(ColorExpression newSelection) {
        exprField.setText(expr.toString());

        ExprStringPosition position;
        if (expr.containsPlaceHolder()) {
            ColorExpression ce = (ColorExpression) expr.findFirstPlaceHolder();
            position = expr.indexOf(ce);
        }
        else {
            position = expr.indexOf(newSelection);
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;
        if (currentSelection != null) {
            toggleEnabledButtons();
        }
        else {

        }
    }

    private void toggleEnabledButtons() {
        if (currentSelection == null){
            toggleaddPlaceholderButton(false);
            toggleColorExprButtons(false);
            deleteExprSelectionButton.setEnabled(false);
            resetExprButton.setEnabled(false);
        }
        else if (currentSelection.getObject() instanceof TupleExpression) {
            toggleaddPlaceholderButton(true);
            toggleColorExprButtons(false);
            deleteExprSelectionButton.setEnabled(true);
            resetExprButton.setEnabled(true);
        }
        else if (currentSelection.getObject() instanceof PlaceHolderColorExpression) {
            if (expr instanceof PlaceHolderColorExpression) {
                toggleaddPlaceholderButton(true);
                toggleColorExprButtons(true);
                deleteExprSelectionButton.setEnabled(true);
                resetExprButton.setEnabled(true);
            } else {
                toggleaddPlaceholderButton(false);
                toggleColorExprButtons(true);
                deleteExprSelectionButton.setEnabled(true);
                resetExprButton.setEnabled(true);
            }
        }
        else if (currentSelection.getObject() instanceof ColorExpression) {
            toggleaddPlaceholderButton(false);
            toggleColorExprButtons(true);
            deleteExprSelectionButton.setEnabled(true);
            resetExprButton.setEnabled(true);
        }
        if(variableCombobox.getItemCount() < 1){
            variableCombobox.setEnabled(false);
            addVariableButton.setEnabled(false);
        }
    }

    private void toggleaddPlaceholderButton(boolean enable) {
        addPlaceHolderButton.setEnabled(enable);
    }

    private void toggleColorExprButtons(boolean enable) {
        addVariableButton.setEnabled(enable);
        addColorButton.setEnabled(enable);
        succButton.setEnabled(enable);
        predButton.setEnabled(enable);

    }

    private void updateColorType() {
        colorCombobox.removeAllItems();
        variableCombobox.removeAllItems();
        ColorType ct;
        if(!isArc){
            ct = colorTypeCombobox.getItemAt(colorTypeCombobox.getSelectedIndex());
        } else{
            ct = arcColorType;
        }
        if (ct != null) {
            for (Variable element : context.network().variables()) {
                if (element.getColorType().getName().equals(ct.getName())) {
                    variableCombobox.addItem(element);
                }
            }
            if (ct instanceof ProductType) {
                for (ColorType element: ((ProductType) ct).getColorTypes()) {
                    for (dk.aau.cs.model.CPN.Color color : element) {
                        colorCombobox.addItem(color);
                    }
                }
            }
            else {
                for (dk.aau.cs.model.CPN.Color element : ct) {
                    colorCombobox.addItem(element);
                }
            }
        }

    }

    private void onExit() {
        rootPane.getParent().setVisible(false);
    }

    private void onOK() {

        onExit();
    }

    public void initExitButtons() {
        exitPanel = new JPanel(new GridBagLayout());

        cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(100, 25));
        cancelButton.setMinimumSize(new Dimension(100, 25));
        cancelButton.setPreferredSize(new Dimension(100, 25));

        OKButton = new JButton("OK");
        OKButton.setMaximumSize(new Dimension(100, 25));
        OKButton.setMinimumSize(new Dimension(100, 25));
        OKButton.setPreferredSize(new Dimension(100, 25));

        exitButtons = new ButtonGroup();

        exitButtons.add(cancelButton);
        exitButtons.add(OKButton);

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clickedOK = true;
                onOK();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clickedOK = false;
                onExit();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(15, 5,5 ,0 );
        exitPanel.add(cancelButton, gbc);
        if(!transport){
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            exitPanel.add(OKButton, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.VERTICAL;
            exprPanel.add(exitPanel, gbc);
        }

    }

    public void initButtonsPanel() {
        colorExpressionButtons = new JPanel(new GridBagLayout());
        colorExpressionButtons.setBorder(BorderFactory.createTitledBorder("Misc"));
        colorExpressionButtons.setPreferredSize(new Dimension(300 ,158 ));

        ButtonGroup expressionButtonsGroup = new ButtonGroup();
        predButton = new JButton("Add Pred");
        succButton = new JButton("Add Succ");
        addPlaceHolderButton = new JButton("Add placeholder");

        predButton.setPreferredSize(new Dimension(130 , 27));
        predButton.setMinimumSize(new Dimension(130 , 27));
        predButton.setMaximumSize(new Dimension(130 , 27));
        succButton.setPreferredSize(new Dimension(130 , 27));
        succButton.setMinimumSize(new Dimension(130 , 27));
        succButton.setMaximumSize(new Dimension(130 , 27));
        addPlaceHolderButton.setPreferredSize(new Dimension(260 , 27));
        addPlaceHolderButton.setMinimumSize(new Dimension(260 , 27));
        addPlaceHolderButton.setMaximumSize(new Dimension(260 , 27));


        expressionButtonsGroup.add(predButton);
        expressionButtonsGroup.add(succButton);
        expressionButtonsGroup.add(addPlaceHolderButton);

        addPlaceHolderButton.addActionListener(actionEvent -> {
            if (!(currentSelection.getObject() instanceof TupleExpression) || !(expr instanceof TupleExpression)) {
                ColorExpression currentObject = (ColorExpression) currentSelection.getObject();
                Vector<ColorExpression> colorExprVec = new Vector();
                colorExprVec.add(currentObject);
                TupleExpression tupleExpr = new TupleExpression(colorExprVec);
                tupleExpr.addColorExpression(new PlaceHolderColorExpression());
                expr = expr.replace(expr, tupleExpr);
                updateSelection(tupleExpr);
            } else {
                TupleExpression tupleExpr = (TupleExpression) expr;
                tupleExpr.addColorExpression(new PlaceHolderColorExpression());
                expr = expr.replace(expr, tupleExpr);
                updateSelection(tupleExpr);
            }
        });

        predButton.addActionListener(actionEvent -> {
            PredecessorExpression predExpr;
            if (currentSelection.getObject() instanceof ColorExpression) {
                predExpr = new PredecessorExpression((ColorExpression) currentSelection.getObject());
                expr = expr.replace(currentSelection.getObject(), predExpr);
                updateSelection(predExpr);
            }
        });

        succButton.addActionListener(actionEvent -> {
            SuccessorExpression succExpr;
            if (currentSelection.getObject() instanceof  ColorExpression) {
                succExpr = new SuccessorExpression((ColorExpression) currentSelection.getObject());
                expr = expr.replace(currentSelection.getObject(), succExpr);
                updateSelection(succExpr);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        colorExpressionButtons.add(predButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0 , 0);
        colorExpressionButtons.add(succButton, gbc);

        gbc.insets = new Insets(0, 0, 5 , 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        colorExpressionButtons.add(addPlaceHolderButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        exprPanel.add(colorExpressionButtons, gbc);
    }

    private void initExprEditPanel() {
        editPanel = new JPanel(new GridBagLayout());
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


        deleteExprSelectionButton.addActionListener(actionEvent -> deleteSelection());

        resetExprButton.addActionListener(actionEvent -> {
            PlaceHolderColorExpression phColorExpr = new PlaceHolderColorExpression();
            expr = expr.replace(expr, phColorExpr);
            updateSelection(phColorExpr);
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
        gbc.fill = GridBagConstraints.VERTICAL;
        exprPanel.add(editPanel, gbc);
    }

    public void initColorTypePanel() {
        colortypePanel = new JPanel(new GridBagLayout());
        colortypePanel.setBorder(BorderFactory.createTitledBorder("Variables and Colors"));

        colorTypeLabel = new JLabel("Color Type: ");
        colorTypeCombobox = new JComboBox();

        for (ColorType element : context.network().colorTypes()) {
            colorTypeCombobox.addItem(element);
        }
        if (colorTypeCombobox.getItemCount() != 0) {
            colorTypeCombobox.setSelectedIndex(0);
        }
        JLabel colorTypeLabel = new JLabel();
        if(isArc){
            colorTypeLabel.setText("<html>Colortype is: " + arcColorType.toString());
        }

        variableLabel = new JLabel("Variable: ");
        variableCombobox = new JComboBox();
        addVariableButton = new JButton("Add Variable");
        Dimension addDim = new Dimension(120, 27);

        addVariableButton.setPreferredSize(addDim);
        addVariableButton.setMinimumSize(addDim);
        addVariableButton.setMaximumSize(addDim);

        colorLabel = new JLabel("Color: ");
        colorCombobox = new JComboBox();
        addColorButton = new JButton("Add Color");

        addColorButton.setPreferredSize(addDim);
        addColorButton.setMinimumSize(addDim);
        addColorButton.setMaximumSize(addDim);

        addVariableButton.addActionListener(actionEvent -> {
            VariableExpression varExpr;
            Variable var = variableCombobox.getItemAt(variableCombobox.getSelectedIndex());
            if (currentSelection.getObject() instanceof TupleExpression) {
                TupleExpression expr = (TupleExpression)currentSelection.getObject();
                if (expr.containsPlaceHolder()) {
                    //expr.findFirstPlaceHolder().
                }
            }
            else if (currentSelection.getObject() instanceof PlaceHolderColorExpression) {
                varExpr = new VariableExpression(var);
                expr = expr.replace(currentSelection.getObject(), varExpr);
                updateSelection(varExpr);
            }

            else if (currentSelection.getObject() instanceof VariableExpression) {
                varExpr = new VariableExpression(var);
                expr = expr.replace(currentSelection.getObject(), varExpr);
                updateSelection(varExpr);
            }
            else if (currentSelection.getObject() instanceof ColorExpression) {
                varExpr = new VariableExpression(var);
                expr = expr.replace(currentSelection.getObject(), varExpr);
                updateSelection(varExpr);
            }

        });

        addColorButton.addActionListener(actionEvent -> {
            dk.aau.cs.model.CPN.Color color = colorCombobox.getItemAt(colorCombobox.getSelectedIndex());
            UserOperatorExpression colorExpr;

            if (currentSelection.getObject() instanceof ColorExpression) {
                colorExpr = new UserOperatorExpression(color);
                expr = expr.replace(currentSelection.getObject(), colorExpr);
                updateSelection(colorExpr);
            }
        });

        colorTypeCombobox.addActionListener(actionEvent -> updateColorType());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3,3,3,3);
        if(!isArc){
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            colortypePanel.add(colorTypeCombobox, gbc);
        } else{
            colortypePanel.add(colorTypeLabel, gbc);
        }

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,3,3,3);
        colortypePanel.add(variableCombobox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3,3,3,3);
        gbc.anchor = GridBagConstraints.WEST;
        colortypePanel.add(addVariableButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,3,3,3);
        colortypePanel.add(colorCombobox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3,3 ,3);
        colortypePanel.add(addColorButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        exprPanel.add(colortypePanel, gbc);
    }

    public void initExprField() {
        exprField = new JTextPane();

        StyledDocument doc = exprField.getStyledDocument();

        MutableAttributeSet standard = new SimpleAttributeSet();
        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(standard, 12);
        doc.setParagraphAttributes(0 , 0, standard, true);

        exprField.setBackground(Color.white);

        exprField.setEditable(false);
        exprField.setToolTipText("Tooltip missing");

        if (expr != null) { //TODO:: Call expression parser in the future. Just loading the string does not make the expression interactable
            exprField.setText(expr.toString());
        }

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
        gbc.gridwidth = 4;
        exprPanel.add(exprScrollPane, gbc);
    }

    public void hideExprField(){
        exprScrollPane.setVisible(false);
        exprPanel.setBorder(null);
        editPanel.setVisible(false);
        exitPanel.setVisible(false);
    }
}
