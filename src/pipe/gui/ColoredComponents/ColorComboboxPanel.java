package pipe.gui.ColoredComponents;

import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.components.ColorComboBoxRenderer;
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
    boolean showAllElement;
    Context context;

    public ColorComboboxPanel(ColorType colorType) {
        this(colorType,false, null);
    }
    public ColorComboboxPanel(ColorType colorType, boolean showAllElement, Context context){
        this.colorType = colorType;
        this.showAllElement = showAllElement;
        this.setLayout(new BorderLayout());
        this.context = context;
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
        }
        else if (colorType != null){
            colorTypeComboBoxesArray[0].setSelectedItem(color);
        }
    }
    //This assumes no nested producttypes
    public void updateSelection(ColorExpression expr){
        if(expr instanceof TupleExpression){
            int i = 0;
            for(ColorExpression c : ((TupleExpression)expr).getColors()){
                setIndex(c, i);
                i++;
            }
        } else{
            setIndex(expr, 0);
        }
    }

    private void setIndex(ColorExpression expr, int index){
        if(expr instanceof AllExpression){
            //.all is always last so we just select the last item
            colorTypeComboBoxesArray[index].setSelectedIndex(colorTypeComboBoxesArray[index].getItemCount()-1);
        } else if(expr instanceof VariableExpression){
            colorTypeComboBoxesArray[index].setSelectedItem(((VariableExpression)expr).getVariable());
        }else{
            colorTypeComboBoxesArray[index].setSelectedItem(((UserOperatorExpression)expr).getUserOperator());
        }
    }
    public void updateColorType(ColorType ct){
        updateColorType(ct, null);
    }
    public void updateColorType(ColorType ct, Context context){
        removeOldComboBoxes();
        colorType = ct;
        if (colorType instanceof ProductType) {
            colorTypeComboBoxesArray = new JComboBox[((ProductType) colorType).getColorTypes().size()];

            for (int i = 0; i < colorTypeComboBoxesArray.length; i++) {
                colorTypeComboBoxesArray[i] = new JComboBox();
                var currentComboBox = colorTypeComboBoxesArray[i];
                currentComboBox.setRenderer(new ColorComboBoxRenderer(currentComboBox));
                currentComboBox.setPreferredSize(new Dimension(200,25));
                if (context != null) {
                    boolean variableFound = false;
                    for (Variable variable : context.network().variables()) {
                        if (variable.getColorType().getName().equals(((ProductType) colorType).getColorTypes().get(i).getName())) {
                            variableFound = true;
                            currentComboBox.addItem(variable);
                        }
                    }
                    if(variableFound){
                        currentComboBox.addItem(new JSeparator());
                    }
                }
                for (Color element : ((ProductType) colorType).getColorTypes().get(i)) {
                    currentComboBox.addItem(element);
                }

                currentComboBox.addActionListener(e -> {
                    if(currentComboBox.getSelectedItem() instanceof JSeparator){
                        currentComboBox.setSelectedIndex(currentComboBox.getSelectedIndex() +1);
                    }
                    changedColor(colorTypeComboBoxesArray);
                });
                ((JLabel)currentComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(0 , 0,5,0);
                gbc.fill =GridBagConstraints.HORIZONTAL;
                comboBoxPanel.add(currentComboBox, gbc);
                if(showAllElement){
                    currentComboBox.addItem(new JSeparator());
                    currentComboBox.addItem("<html><b>.all</b>");
                }
            }
        }
        else if (colorType != null){
            colorTypeComboBoxesArray = new JComboBox[1];
            var currentComboBox = new JComboBox();
            colorTypeComboBoxesArray[0] = currentComboBox;
            currentComboBox.setRenderer(new ColorComboBoxRenderer(currentComboBox));
            currentComboBox.setPreferredSize(new Dimension(200,25));
            if(context != null){
                boolean variableFound = false;
                for (Variable element : context.network().variables()) {
                    if (element.getColorType().getName().equals(ct.getName())) {
                        currentComboBox.addItem(element);
                        variableFound = true;
                    }
                }
                if(variableFound){
                    currentComboBox.addItem(new JSeparator());
                }
            }
            for (Color element : colorType) {
                if(currentComboBox.getSelectedItem() instanceof JSeparator){
                    currentComboBox.setSelectedIndex(currentComboBox.getSelectedIndex() +1);
                }
                currentComboBox.addItem(element);
            }

            currentComboBox.addActionListener(e -> {
                if(currentComboBox.getSelectedItem() instanceof JSeparator){
                    currentComboBox.setSelectedIndex(currentComboBox.getSelectedIndex() +1);
                }
                changedColor(colorTypeComboBoxesArray);
            });
            ((JLabel)currentComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill =GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0 , 0,5,0);
            comboBoxPanel.add(currentComboBox, gbc);
            if(showAllElement){
                currentComboBox.addItem(new JSeparator());
                currentComboBox.addItem("<html><b>.all</b>");
            }
        }
        revalidate();
    }
    private void removeOldComboBoxes(){
        comboBoxPanel.removeAll();
    }

    @Override
    public void setEnabled(boolean enabled){
        colorcomboBoxPanel.setEnabled(enabled);
        for(var combobox : colorTypeComboBoxesArray){
            combobox.setEnabled(enabled);
        }
    }
}
