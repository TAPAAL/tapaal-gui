package pipe.gui;

import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Color;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

public class ColorComboboxPanel extends JPanel {

    private ColorType colorType;
    private String panelName;

    private JPanel colorcomboBoxPanel;
    private JComboBox[] colorTypeComboBoxesArray;

    public ColorComboboxPanel(ColorType colorType, String panelName) {
        this.colorType = colorType;
        this.panelName = panelName;
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

        Dimension comboSize = new Dimension(230, 30);

        JPanel comboBoxPanel = new JPanel(new GridBagLayout());
        if (colorType instanceof ProductType) {
            colorTypeComboBoxesArray = new JComboBox[((ProductType) colorType).getColorTypes().size()];

            for (int i = 0; i < colorTypeComboBoxesArray.length; i++) {
                colorTypeComboBoxesArray[i] = new JComboBox();
                for (Color element : ((ProductType) colorType).getColorTypes().get(i)) {
                    colorTypeComboBoxesArray[i].addItem(element);
                }

                colorTypeComboBoxesArray[i].setPreferredSize(comboSize);
                colorTypeComboBoxesArray[i].setMinimumSize(comboSize);
                colorTypeComboBoxesArray[i].setMaximumSize(comboSize);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.anchor = GridBagConstraints.SOUTH;
                gbc.insets = new Insets(5 , 0,0,0);
                comboBoxPanel.add(colorTypeComboBoxesArray[i], gbc);
            }
        }
        else if (colorType != null){
            colorTypeComboBoxesArray = new JComboBox[1];
            colorTypeComboBoxesArray[0] = new JComboBox();
            for (Iterator<Color> iter = colorType.iterator(); iter.hasNext();) {
                Color element = iter.next();
                colorTypeComboBoxesArray[0].addItem(element);
            }
            colorTypeComboBoxesArray[0].setPreferredSize(comboSize);
            colorTypeComboBoxesArray[0].setMinimumSize(comboSize);
            colorTypeComboBoxesArray[0].setMaximumSize(comboSize);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;

            gbc.anchor = GridBagConstraints.SOUTH;
            gbc.insets = new Insets(5 , 0,0,0);
            comboBoxPanel.add(colorTypeComboBoxesArray[0], gbc);
        }

        JScrollPane colorTypesScrollPane = new JScrollPane(comboBoxPanel);
        colorTypesScrollPane.setViewportView(comboBoxPanel);
        colorTypesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        colorTypesScrollPane.setBorder(BorderFactory.createTitledBorder("Color Type elements"));

        Dimension scrollPaneSize = new Dimension(260, 150);
        colorTypesScrollPane.setPreferredSize(scrollPaneSize);
        colorTypesScrollPane.setMinimumSize(scrollPaneSize);
        colorTypesScrollPane.setMaximumSize(scrollPaneSize);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.SOUTH;
        colorcomboBoxPanel.add(colorTypesScrollPane, gbc);
        add(colorcomboBoxPanel, gbc);
    }
}
