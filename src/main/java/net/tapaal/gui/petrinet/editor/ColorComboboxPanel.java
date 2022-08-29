package net.tapaal.gui.petrinet.editor;

import net.tapaal.gui.petrinet.Context;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Expressions.*;
import javax.swing.*;
import java.awt.*;

public abstract class ColorComboboxPanel extends JPanel {
    private ColorType colorType;
    private JPanel colorcomboBoxPanel;
    private JComboBox[] colorTypeComboBoxesArray;
    JScrollPane colorTypesScrollPane;
    JPanel comboBoxPanel;
    final boolean showAllElement;

    public ColorComboboxPanel(ColorType colorType) {
        this(colorType,false);
    }
    public ColorComboboxPanel(ColorType colorType, boolean showAllElement){
        this.colorType = colorType;
        this.showAllElement = showAllElement;
        this.setLayout(new BorderLayout());
        initPanel();
    }

    public ColorType getColorType() {
        return colorType;
    }

    public JComboBox[] getColorTypeComboBoxesArray() {
        return colorTypeComboBoxesArray;
    }

    private void initPanel() {
        colorcomboBoxPanel = new JPanel();
        colorcomboBoxPanel.setLayout(new GridBagLayout());

        //This panel contains all comboboxes, there can be more than one with ProductTypes
        comboBoxPanel = new JPanel(new GridBagLayout());
        //In case it is a really large product type we have a scrollPane
        colorTypesScrollPane = new JScrollPane(comboBoxPanel);
        colorTypesScrollPane.setViewportView(comboBoxPanel);
        colorTypesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        colorTypesScrollPane.setBorder(BorderFactory.createTitledBorder("Color Type elements"));
        updateColorType(colorType);

        add(colorTypesScrollPane, BorderLayout.CENTER);
    }

    public void removeScrollPaneBorder(){
        colorTypesScrollPane.setBorder(null);
    }

    public abstract void changedColor(JComboBox[] comboBoxes);
    public void updateSelection(Color color){
        if (colorType instanceof ProductType) {
            int i = 0;
            for (Color element : color.getTuple()) {
                colorTypeComboBoxesArray[i].setSelectedItem(element);
                i++;
            }
        } else if (colorType != null){
            colorTypeComboBoxesArray[0].setSelectedItem(color);
        }
    }

    //This assumes no nested producttypes
    public void updateSelection(ColorExpression expr){
        if (expr instanceof TupleExpression) {
            int i = 0;
            for (ColorExpression c : ((TupleExpression)expr).getColors()) {
                setIndex(c, i);
                i++;
            }
        } else {
            setIndex(expr, 0);
        }
    }

    private void setIndex(ColorExpression expr, int index){
        if(expr instanceof AllExpression){
            //.all is always last so we just select the last item
<<<<<<< HEAD
            colorTypeComboBoxesArray[index].setSelectedIndex(colorTypeComboBoxesArray[index].getItemCount());
=======
            colorTypeComboBoxesArray[index].setSelectedIndex(colorTypeComboBoxesArray[index].getItemCount() - 1);
>>>>>>> origin/cpn
        } else if(expr instanceof VariableExpression){
            colorTypeComboBoxesArray[index].setSelectedItem(((VariableExpression)expr).getVariable());
        } else if(expr instanceof UserOperatorExpression){
            colorTypeComboBoxesArray[index].setSelectedItem(((UserOperatorExpression)expr).getUserOperator());
        } else if(expr instanceof TupleExpression){
            colorTypeComboBoxesArray[index].setSelectedItem(((TupleExpression)expr).getColors().get(0));
<<<<<<< HEAD
=======
        } else if (expr instanceof PredecessorExpression) {
            setIndex(((PredecessorExpression)expr).getPredecessorExpression(), index);
        } else if (expr instanceof SuccessorExpression) {
            setIndex(((SuccessorExpression)expr).getSuccessorExpression(), index);
>>>>>>> origin/cpn
        } else {
            colorTypeComboBoxesArray[index].setSelectedItem(expr);
        }
    }

    public void updateColorType(ColorType ct){
<<<<<<< HEAD
        updateColorType(ct, null);
    }
    public void updateColorType(ColorType ct, Context context){
        updateColorType(ct, context, false);
    }
    public void updateColorType(ColorType ct, Context context, boolean includePlaceHolder) {
=======
        updateColorType(ct, null, false, false);
    }
    public void updateColorType(ColorType ct, Context context, boolean includePlaceHolder, boolean transitionDialog) {
>>>>>>> origin/cpn
        removeOldComboBoxes();
        colorType = ct;

        if (colorType instanceof ProductType) {
<<<<<<< HEAD
            int numberOfComBoboxes = includePlaceHolder ? 1 : ((ProductType) colorType).getColorTypes().size();
=======
            int numberOfComBoboxes =  transitionDialog ? 1 : ((ProductType) colorType).getColorTypes().size();
>>>>>>> origin/cpn
            colorTypeComboBoxesArray = new JComboBox[numberOfComBoboxes];

            for (int i = 0; i < colorTypeComboBoxesArray.length; i++) {
                JComboBox currentComboBox = createColoredCombobox(context, ((ProductType) colorType).getColorTypes().get(i), includePlaceHolder);
                colorTypeComboBoxesArray[i] = currentComboBox;
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.weightx = 1.0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0 , 0,5,0);
                comboBoxPanel.add(currentComboBox, gbc);
            }
        } else if (colorType != null) {
            colorTypeComboBoxesArray = new JComboBox[1];
            JComboBox currentComboBox = createColoredCombobox(context, ct, includePlaceHolder);
            colorTypeComboBoxesArray[0] = currentComboBox;

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 5, 0);
            comboBoxPanel.add(currentComboBox, gbc);
        }
        revalidate();
    }

    private JComboBox createColoredCombobox(Context context, ColorType ct, boolean includePlaceHolder) {
        JComboBox combobox = new JComboBox<>();
        combobox.setRenderer(new ColorComboBoxRenderer(combobox));
        combobox.setPreferredSize(new Dimension(200,25));

        if (includePlaceHolder) {
            combobox.addItem(new PlaceHolderColorExpression());
            combobox.addItem(new JSeparator());
        }
        if (context != null) {
            boolean variableFound = false;
            for (Variable variable : context.network().variables()) {
                if (variable.getColorType().getName().equals(ct.getName())) {
                    combobox.addItem(variable);
                    variableFound = true;
                }
            }
            if (variableFound) {
                combobox.addItem(new JSeparator());
            }
        }
        for (Color element : ct) {
            if (combobox.getSelectedItem() instanceof JSeparator) {
                combobox.setSelectedIndex(combobox.getSelectedIndex() + 1);
            }
            combobox.addItem(element);
        }
        if (showAllElement) {
            combobox.addItem(new JSeparator());
            combobox.addItem("<html><b>.all</b>");
        }
        combobox.addActionListener(e -> {
            if (combobox.getSelectedItem() instanceof JSeparator) {
                combobox.setSelectedIndex(combobox.getSelectedIndex() + 1);
            }
            changedColor(colorTypeComboBoxesArray);
        });
        ((JLabel)combobox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        return combobox;
    }

    private void removeOldComboBoxes(){
        comboBoxPanel.removeAll();
    }

    @Override
    public void setEnabled(boolean enabled){
        colorcomboBoxPanel.setEnabled(enabled);
<<<<<<< HEAD
        for (var combobox : colorTypeComboBoxesArray) {
            combobox.setEnabled(enabled);
=======
        if (colorTypeComboBoxesArray != null) {
            for (var combobox : colorTypeComboBoxesArray) {
                combobox.setEnabled(enabled);
            }
>>>>>>> origin/cpn
        }
    }
}
