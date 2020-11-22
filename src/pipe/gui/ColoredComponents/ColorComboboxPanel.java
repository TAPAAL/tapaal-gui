package pipe.gui.ColoredComponents;

import dk.aau.cs.gui.components.ColorComboBoxRenderer;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Expressions.AllExpression;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.TupleExpression;
import dk.aau.cs.model.CPN.Expressions.UserOperatorExpression;
import net.tapaal.swinghelpers.GridBagHelper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Iterator;
import java.util.Vector;

public abstract class ColorComboboxPanel extends JPanel {

    private ColorType colorType;
    private String panelName;

    private JPanel colorcomboBoxPanel;
    private JComboBox[] colorTypeComboBoxesArray;
    JScrollPane colorTypesScrollPane;
    JPanel comboBoxPanel;
    private JCheckBox[] allCheckBoxesArray;
    boolean showAllElement;

    public ColorComboboxPanel(ColorType colorType, String panelName) {
        this(colorType,panelName,false);
    }
    public ColorComboboxPanel(ColorType colorType, String panelName, boolean showAllElement){
        this.colorType = colorType;
        this.panelName = panelName;
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

    public JCheckBox[] getAllCheckBoxesArray(){
        return allCheckBoxesArray;
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

    public void updateSelection(ColorExpression expr){
        if(expr instanceof TupleExpression){
            int i = 0;
            for(ColorExpression c : ((TupleExpression)expr).getColors()){
                setIndex(c, i);
            }
        } else{
            setIndex(expr, 0);
        }
    }

    private void setIndex(ColorExpression expr, int index){
        if(expr instanceof AllExpression){
            colorTypeComboBoxesArray[index].setSelectedItem(".all");
        } else{
            colorTypeComboBoxesArray[index].setSelectedItem(((UserOperatorExpression)expr).getUserOperator());
        }
    }

    public void updateColorType(ColorType ct){
        removeOldComboBoxes();
        colorType = ct;
        if (colorType instanceof ProductType) {
            colorTypeComboBoxesArray = new JComboBox[((ProductType) colorType).getColorTypes().size()];
            allCheckBoxesArray = new JCheckBox[((ProductType) colorType).getColorTypes().size()];


            for (int i = 0; i < colorTypeComboBoxesArray.length; i++) {
                colorTypeComboBoxesArray[i] = new JComboBox();
                colorTypeComboBoxesArray[i].setRenderer(new ColorComboBoxRenderer(colorTypeComboBoxesArray[i]));
                for (Color element : ((ProductType) colorType).getColorTypes().get(i)) {
                    colorTypeComboBoxesArray[i].addItem(element);
                }

                colorTypeComboBoxesArray[i].addActionListener(actionEvent -> changedColor(colorTypeComboBoxesArray));
                ((JLabel)colorTypeComboBoxesArray[i].getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(5 , 0,0,0);
                gbc.fill =GridBagConstraints.HORIZONTAL;
                comboBoxPanel.add(colorTypeComboBoxesArray[i], gbc);
                if(showAllElement){
                    colorTypeComboBoxesArray[i].addItem(new JSeparator());
                    colorTypeComboBoxesArray[i].addItem("<html><b>.all</b>");
                }
            }
        }
        else if (colorType != null){
            colorTypeComboBoxesArray = new JComboBox[1];
            allCheckBoxesArray = new JCheckBox[1];
            colorTypeComboBoxesArray[0] = new JComboBox();
            colorTypeComboBoxesArray[0].setRenderer(new ColorComboBoxRenderer(colorTypeComboBoxesArray[0]));

            for (Iterator<Color> iter = colorType.iterator(); iter.hasNext();) {
                Color element = iter.next();
                colorTypeComboBoxesArray[0].addItem(element);
            }

            colorTypeComboBoxesArray[0].addActionListener(actionEvent -> changedColor(colorTypeComboBoxesArray));
            ((JLabel)colorTypeComboBoxesArray[0].getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill =GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5 , 0,0,0);
            comboBoxPanel.add(colorTypeComboBoxesArray[0], gbc);
            if(showAllElement){
                colorTypeComboBoxesArray[0].addItem(new JSeparator());
                colorTypeComboBoxesArray[0].addItem("<html><b>.all</b>");
            }
        }
        addCheckBoxActionListeners();
        revalidate();
    }
    private void removeOldComboBoxes(){
        comboBoxPanel.removeAll();
    }

    private void addCheckBoxActionListeners(){
        for(JCheckBox checkBox : allCheckBoxesArray){
            if(checkBox != null){
                checkBox.addItemListener(e -> {
                    int index = 0;
                    for(JCheckBox item : allCheckBoxesArray){
                        if(item.equals(e.getItem())){
                            if(e.getStateChange() == ItemEvent.SELECTED) {
                                colorTypeComboBoxesArray[index].setEnabled(false);
                            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                                colorTypeComboBoxesArray[index].setEnabled(true);
                            }
                        }
                        index++;
                    }
                });
            }
        }
    }
}
