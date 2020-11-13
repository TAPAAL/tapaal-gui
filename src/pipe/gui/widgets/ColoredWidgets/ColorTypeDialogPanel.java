package pipe.gui.widgets.ColoredWidgets;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import org.jdesktop.swingx.JXComboBox;
import pipe.gui.CreateGui;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.ConstantsPane;
import pipe.gui.widgets.EscapableDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ColorTypeDialogPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String toolTipColorComboBox = "Switch between the different defined color types";
    private static final String finiteEnumeration= "Finite Enumeration";
    private static final String cyclicEnumeration = "Cyclic Enumeration";
    private static final String rangeOfIntegers = "Range of Integers";
    private static final String productColor = "Product Color";
    private boolean rangeOfIntegersPanelEnabled = false;
    private TimedArcPetriNetNetwork network;
    private EscapableDialog dialog;
    private List<ColorType> colorTypes;
    private ColorType oldColorType;
    private final HashMap<String, ColorType> colortypes = new HashMap<String, ColorType>();
    private String oldName;
    private ConstantsPane.ColorTypesListModel colorTypesListModel;
    private UndoManager undoManager;

    private JLabel nameLabel;
    private JTextField nameTextField;
    private JComboBox colorTypeComboBox;
    private JLabel colorTypeLabel;
    private JList allColorTypes;
    private JLabel productLabel;
    private DefaultListModel productModel;
    private JButton productAddButton;
    private JButton productRemoveButton;
    private JList productColorTypeList;
    private JComboBox productTypeComboBox;

    private JLabel lowerBoundLabel;
    private JLabel upperBoundLabel;
    private JTextField lowerBoundTextField;
    private JTextField upperBoundTextField;
    private JLabel enumNameLabel;
    private JTextField enumTextField;
    private JButton enumAddButton;
    private JScrollPane enumListScroller;
    private JPanel cyclicAndFiniteEnumerationPanel;
    private JList enumList;
    private JPanel container;
    private JPanel rangeOfIntegersPanel;
    private JPanel productTypePanel;
    private DefaultListModel cyclicModel;
    private JButton cyclicRemoveButton;
    private JButton okButton;
    private JScrollPane scrollPane;
    private static final String removeColorInColorTypeMessage = "You have removed a color in this color type. " +
        "\nAll tokens of this color will be removed and all usages of this color in Arc expressions and guards will also be removed,\n " +
        "while maintaing as much of the expression as possible.\n" +
        "If the arc expressions are empty they will be converted to 1'{topColor}.\n" +
        "If the guards are empty they will be removed.\n\n" +
        "Do you wish to continue?";
    private static final String editProductTypeMessage = "Editing product types will result in removing all tokens in all places with this colortype\n" +
        "All arc expressions from such a place will also be reset to 1'{topColor}.\n" +
        "Do you wish to continue?";

    public ColorTypeDialogPanel(JRootPane pane, ConstantsPane.ColorTypesListModel colorTypesListModel,
                                TimedArcPetriNetNetwork network, UndoManager undoManager) {
        oldName = "";
        this.network = network;
        this.colorTypesListModel = colorTypesListModel;
        this.undoManager = undoManager;
        initComponents();
        nameTextField.setText(oldName);

    }

    public ColorTypeDialogPanel(JRootPane pane, ConstantsPane.ColorTypesListModel colorTypesListModel,
                                TimedArcPetriNetNetwork network, ColorType colortype, UndoManager undoManager) {
        this.oldColorType = colortype;
        oldName = colortype.getName();
        this.undoManager = undoManager;
        this.network = network;
        this.colorTypesListModel = colorTypesListModel;
        initComponents();
        initValues();
        colorTypeComboBox.setVisible(false);
        colorTypeLabel.setVisible(false);
    }

    public void showDialog() {
        dialog = new EscapableDialog(CreateGui.getApp(),
                "Edit color type", true);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setResizable(true);
        dialog.pack();
        //size of range of integers panel
        dialog.setMinimumSize(new Dimension(447,231));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void initValues() {
        nameTextField.setText(oldName);
        if (!(oldColorType instanceof ProductType)) { // colortype is always either ProductType or Cyclic
            colorTypeComboBox.setSelectedIndex(0);
            for (dk.aau.cs.model.CPN.Color element : oldColorType) {
                cyclicModel.addElement(element);
            }
            enumList.setModel(cyclicModel);
        }
        else {
            colorTypeComboBox.setSelectedIndex(2);
            for (ColorType type : ((ProductType) oldColorType).getColorTypes()) {
                productModel.addElement(type);
            }
            productColorTypeList.setModel(productModel);
        }
    }

    private void initComponents()  {
        container = new JPanel();
        container.setLayout(new GridBagLayout());

        JPanel nameAndTypePanel = createNameAndTypePanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 0, 8);
        container.add(nameAndTypePanel, gbc);

        cyclicAndFiniteEnumerationPanel = createCyclicAndFiniteEnumerationPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 8, 0, 8);
        container.add(cyclicAndFiniteEnumerationPanel, gbc);

        rangeOfIntegersPanel = createRangeOfIntegersPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 0, 8);
        container.add(rangeOfIntegersPanel, gbc);

        productTypePanel = createProductTypePanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 8, 0, 8);
        container.add(productTypePanel, gbc);

        JPanel buttonPanel = createButtonPanel();
        gbc.insets = new Insets(0, 8, 5, 8);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        container.add(buttonPanel,gbc);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(container);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rangeOfIntegersPanel.setVisible(false);
        productTypePanel.setVisible(false);
    }

    private JPanel createNameAndTypePanel() {
        JPanel nameAndTypePanel = new JPanel();
        nameAndTypePanel.setLayout(new GridBagLayout());
        nameAndTypePanel.setBorder(BorderFactory.createTitledBorder("Name and Color Type"));

        nameLabel = new JLabel();
        nameLabel.setText("Name: ");
        GridBagConstraints gbcNL = new GridBagConstraints();
        gbcNL.gridx = 0;
        gbcNL.gridy = 0;
        gbcNL.gridwidth = 1;
        gbcNL.insets = new Insets(4, 4, 2, 4);
        gbcNL.anchor = GridBagConstraints.WEST;
        nameAndTypePanel.add(nameLabel, gbcNL);

        nameTextField = new JTextField();
        Dimension size = new Dimension(330, 30);
        nameTextField.setPreferredSize(size);
        nameTextField.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent ancestorEvent) {
                final AncestorListener al= this;
                JComponent component = ancestorEvent.getComponent();
                component.requestFocusInWindow();
                component.removeAncestorListener( al );
            }

            @Override
            public void ancestorRemoved(AncestorEvent ancestorEvent) {

            }

            @Override
            public void ancestorMoved(AncestorEvent ancestorEvent) {

            }
        });
        nameTextField.addActionListener(e -> {
            okButton.requestFocusInWindow();
            okButton.doClick();
        });
        GridBagConstraints gbcNTF = new GridBagConstraints();
        gbcNTF.gridx = 1;
        gbcNTF.gridy = 0;
        gbcNTF.gridwidth = 1;
        gbcNTF.weightx = 1.0;
        gbcNTF.anchor = GridBagConstraints.WEST;
        gbcNTF.fill = GridBagConstraints.HORIZONTAL;
        gbcNTF.insets = new Insets(4, 4, 2, 4);
        nameTextField.requestFocusInWindow();
        nameAndTypePanel.add(nameTextField, gbcNTF);

        colorTypeLabel = new JLabel();
        colorTypeLabel.setText("Color type: ");
        GridBagConstraints gbcCTL = new GridBagConstraints();
        gbcCTL.insets = new Insets(2, 4, 2, 4);
        gbcCTL.gridx = 0;
        gbcCTL.gridy = 1;
        gbcCTL.gridwidth = 1;
        gbcCTL.anchor = GridBagConstraints.WEST;
        nameAndTypePanel.add(colorTypeLabel,gbcCTL);

        colorTypeComboBox = new JXComboBox(new String[]{cyclicEnumeration, rangeOfIntegers, productColor});
        colorTypeComboBox.setToolTipText(toolTipColorComboBox);

        colorTypeComboBox.addActionListener(e -> {
            JComboBox source = (JXComboBox)e.getSource();
            final String selectedString = source.getSelectedItem().toString();

            if (selectedString.equals(finiteEnumeration) || selectedString.equals(cyclicEnumeration)) {
                rangeOfIntegersPanel.setVisible(false);
                productTypePanel.setVisible(false);
                cyclicAndFiniteEnumerationPanel.setVisible(true);
                rangeOfIntegersPanelEnabled = false;
            }
            else if (selectedString.equals(rangeOfIntegers)) {
                cyclicAndFiniteEnumerationPanel.setVisible(false);
                productTypePanel.setVisible(false);
                rangeOfIntegersPanel.setVisible(true);
                rangeOfIntegersPanelEnabled = true;
            }
            else if (selectedString.equals(productColor)) {
                cyclicAndFiniteEnumerationPanel.setVisible(false);
                productTypePanel.setVisible(true);
                rangeOfIntegersPanel.setVisible(false);
                rangeOfIntegersPanelEnabled = false;
            }
            if(dialog != null){
                dialog.pack();
            }
        });

        GridBagConstraints gbcCTCB = new GridBagConstraints();
        gbcCTCB.insets = new Insets(2, 4, 2, 4);
        gbcCTCB.gridx = 1;
        gbcCTCB.gridy = 1;
        gbcCTCB.gridwidth = 1;
        gbcCTCB.weightx = 1.0;
        gbcCTCB.anchor = GridBagConstraints.EAST;
        gbcCTCB.fill = GridBagConstraints.HORIZONTAL;
        nameAndTypePanel.add(colorTypeComboBox, gbcCTCB);

        return nameAndTypePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        okButton = new JButton("OK");
        okButton.setMaximumSize(new Dimension(100, 25));
        okButton.setMinimumSize(new Dimension(100, 25));
        okButton.setPreferredSize(new Dimension(100, 25));

        okButton.setMnemonic(KeyEvent.VK_O);
        GridBagConstraints gbcOk = new GridBagConstraints();
        gbcOk.gridx = 1;
        gbcOk.gridy = 0;
        gbcOk.anchor = GridBagConstraints.WEST;
        gbcOk.insets = new Insets(5, 5, 5, 5);

        okButton.addActionListener(actionEvent -> onOK());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(100, 25));
        cancelButton.setMinimumSize(new Dimension(100, 25));
        cancelButton.setPreferredSize(new Dimension(100, 25));
        cancelButton.setMnemonic(KeyEvent.VK_C);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;

        cancelButton.addActionListener(e -> exit());

        buttonPanel.add(cancelButton,gbc);
        buttonPanel.add(okButton,gbcOk);

        return buttonPanel;
    }

    private JPanel createRangeOfIntegersPanel() {
        JPanel rangeOfIntegers = new JPanel();
        rangeOfIntegers.setLayout(new GridBagLayout());
        rangeOfIntegers.setBorder(BorderFactory.createTitledBorder("Range Of Integers"));

        lowerBoundLabel = new JLabel("Lower Bound: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        rangeOfIntegers.add(lowerBoundLabel, gbc);

        lowerBoundTextField = new  JTextField();

        Dimension size = new Dimension(55, 30);
        lowerBoundTextField.setPreferredSize(size);
        lowerBoundTextField.setMaximumSize(size);
        lowerBoundTextField.setMaximumSize(size);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 2, 4);
        rangeOfIntegers.add(lowerBoundTextField, gbc);

        upperBoundLabel = new JLabel("Upper Bound: ");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        rangeOfIntegers.add(upperBoundLabel, gbc);

        upperBoundTextField = new  JTextField();
        upperBoundTextField.setPreferredSize(size);
        upperBoundTextField.setMaximumSize(size);
        upperBoundTextField.setMaximumSize(size);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 2, 4);
        //lowerBoundTextField.requestFocusInWindow();
        rangeOfIntegers.add(upperBoundTextField, gbc);

        return rangeOfIntegers;
    }

    private JPanel createCyclicAndFiniteEnumerationPanel() {
        final JPanel cyclicAndFiniteEnumeration = new JPanel();
        cyclicAndFiniteEnumeration.setLayout(new GridBagLayout());
        cyclicAndFiniteEnumeration.setBorder(BorderFactory.createTitledBorder("Cyclic and Finite Enumeration"));
        cyclicAndFiniteEnumeration.setSize(550,300);

        JPanel firstRow = new JPanel();
        firstRow.setLayout(new GridBagLayout());

        enumNameLabel = new JLabel("Name: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        firstRow.add(enumNameLabel, gbc);

        enumTextField = new  JTextField();
        Dimension size = new Dimension(300, 30);
        enumTextField.setMaximumSize(size);
        enumTextField.setMinimumSize(size);
        enumTextField.setPreferredSize(size);
        GridBagConstraints gbcNTF = new GridBagConstraints();
        gbcNTF.gridx = 1;
        gbcNTF.gridy = 0;
        gbcNTF.gridwidth = 1;
        gbcNTF.weightx = 1.0;
        gbcNTF.anchor = GridBagConstraints.WEST;
        gbcNTF.fill = GridBagConstraints.HORIZONTAL;
        gbcNTF.insets = new Insets(4, 4, 2, 4);
        nameTextField.requestFocusInWindow();
        firstRow.add(enumTextField, gbcNTF);

        enumTextField.addActionListener(e -> enumAddButton.doClick());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cyclicAndFiniteEnumeration.add(firstRow, gbc);

        JPanel secondRow = new JPanel();
        secondRow.setLayout(new GridBagLayout());

        enumAddButton = new JButton("Add");
        enumAddButton.addActionListener(e -> {
            String enumerationName = enumTextField.getText();
            if(enumerationName == null || enumerationName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        CreateGui.getApp(), "You have to enter a name for the color",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if(!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", enumerationName)) {
                JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
                    "Acceptable names for enumerations are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if(enumerationName.equals("all") || enumerationName.equals("All") || enumerationName.equals("dot")){
                JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
                    "The color cannot be named \"" + enumerationName + "\", as the name is reserved",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (cyclicModel.contains(enumTextField.getText())) {
                JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
                    "A color with the name \"" + enumerationName + "\" already exists",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }else {
                cyclicModel.addElement(enumTextField.getText());
                enumList.setModel(cyclicModel);
                enumTextField.setText("");
                cyclicRemoveButton.setEnabled(true);
            }
        });

        Dimension buttonSize = new Dimension(100, 30);
        enumAddButton.setMaximumSize(buttonSize);
        enumAddButton.setMinimumSize(buttonSize);
        enumAddButton.setPreferredSize(buttonSize);
        enumAddButton.setMnemonic(KeyEvent.VK_A);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        secondRow.add(enumAddButton, gbc);

        cyclicRemoveButton = new JButton("Remove");
        cyclicRemoveButton.setEnabled(false);
        cyclicRemoveButton.addActionListener(actionEvent -> {
            cyclicModel.removeElementAt(enumList.getSelectedIndex());
            enumList.setModel(cyclicModel);
        });
        cyclicRemoveButton.setPreferredSize(buttonSize);
        cyclicRemoveButton.setMinimumSize(buttonSize);
        cyclicRemoveButton.setMaximumSize(buttonSize);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        secondRow.add(cyclicRemoveButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        cyclicAndFiniteEnumeration.add(secondRow, gbc);

        JPanel thirdRow = new JPanel();
        thirdRow.setLayout(new GridBagLayout());

        cyclicModel = new DefaultListModel();
        enumList = new JList();

        enumList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                JList source = (JList) e.getSource();
                if (source.getSelectedIndex() == -1) {
                    cyclicRemoveButton.setEnabled(false);
                } else{
                    cyclicRemoveButton.setEnabled(true);
                }
            }
        });

        JScrollPane cyclicListScrollPane = new JScrollPane(enumList);
        cyclicListScrollPane.setViewportView(enumList);
        cyclicListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        Dimension listSize = new Dimension(450, 150);
        cyclicListScrollPane.setPreferredSize(listSize);
        cyclicListScrollPane.setMinimumSize(listSize);
        cyclicListScrollPane.setMaximumSize(listSize);
        cyclicListScrollPane.setVisible(true);
        cyclicListScrollPane.setBorder(new LineBorder(Color.GRAY));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        thirdRow.add(cyclicListScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cyclicAndFiniteEnumeration.add(thirdRow, gbc);

        return cyclicAndFiniteEnumeration;
    }

    private JPanel createProductTypePanel() {
        JPanel productTypePanel = new JPanel();
        productTypePanel.setLayout(new GridBagLayout());
        productTypePanel.setBorder(BorderFactory.createTitledBorder("Product Type/ Domain"));
        GridBagConstraints gbc = new GridBagConstraints();

        productLabel = new JLabel("Color types: ");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        productTypePanel.add(productLabel, gbc);

        productModel = new DefaultListModel();

        allColorTypes = new JList();


        colorTypes = new ArrayList<>();
        colorTypes = network.colorTypes();

        productTypeComboBox = new JComboBox();

        for (ColorType element : colorTypes) {
            if(!(element instanceof ProductType)){
                productTypeComboBox.addItem(element);
            }
        }
/*        if(oldColorType != null && oldColorType instanceof ProductType){
            productTypeComboBox.removeItem(oldColorType);
        }*/
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        productTypeComboBox.setBackground(Color.white);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        productTypePanel.add(productTypeComboBox,gbc);

        JPanel productButtonPanel = createProductButtonsPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        productTypePanel.add(productButtonPanel, gbc);

        productColorTypeList = new JList();
        productColorTypeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                JList source = (JList) e.getSource();
                if (source.getSelectedIndex() == -1) {
                    productRemoveButton.setEnabled(false);
                } else{
                    productRemoveButton.setEnabled(true);
                }
            }
        });
        JScrollPane productColorsListScrollPane = new JScrollPane(productColorTypeList);
        productColorsListScrollPane.setViewportView(productColorTypeList);
        productColorsListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3,3,3,3);
        productTypePanel.add(productColorsListScrollPane, gbc);

        return productTypePanel;
    }

    private JPanel createProductButtonsPanel() {
        JPanel productButtonPanel = new JPanel();
        productButtonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        productAddButton = new JButton("Add");
        productAddButton.setMnemonic(KeyEvent.VK_A);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;
        productButtonPanel.add(productAddButton, gbc);

        productAddButton.addActionListener(e -> {
            productModel.addElement(productTypeComboBox.getSelectedItem());
            productColorTypeList.setModel(productModel);
            productRemoveButton.setEnabled(true);
        });

        productRemoveButton = new JButton("Remove");
        productRemoveButton.setEnabled(false);
        productRemoveButton.setMnemonic(KeyEvent.VK_R);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;
        productButtonPanel.add(productRemoveButton, gbc);

        productRemoveButton.addActionListener(e -> {

            productModel.removeElementAt(productColorTypeList.getSelectedIndex());
            productColorTypeList.setModel(productModel);
            if(productModel.size() == 0) {
                productRemoveButton.setEnabled(false);
            }
        });
        return productButtonPanel;
    }

    private void exit() {
        dialog.setVisible(false);
    }

    private void onOK() {
            String name = nameTextField.getText();
            String lowerbound = lowerBoundTextField.getText();
            String upperbound = upperBoundTextField.getText();
            String enumerationName = enumTextField.getText();


            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        CreateGui.getApp(), "You have to enter a name for the color",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", name)) {
                JOptionPane.showMessageDialog(
                                CreateGui.getApp(),
                                "Acceptable names for color types are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (network.isNameUsedForColorType(name) && oldColorType == null) {
                JOptionPane.showMessageDialog(
                                CreateGui.getApp(),
                                "There is already another color type with the same name.\n\n"
                                        + "Choose a different name for the color type.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }else if (!oldName.equals("") && !oldName.equalsIgnoreCase(name) && network.isNameUsedForColorType(name)) {
                JOptionPane.showMessageDialog(
                                CreateGui.getApp(),
                                "There is already another color type with the same name.\n\n"
                                        + "Choose a different name for the color type.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }else if (!Pattern.matches("[0-9]+", lowerbound) && rangeOfIntegersPanelEnabled ) {
                JOptionPane.showMessageDialog(
                                CreateGui.getApp(),
                                "Lower bound must be a number",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (!Pattern.matches("[0-9]+", upperbound) && rangeOfIntegersPanelEnabled ) {
                JOptionPane.showMessageDialog(
                                CreateGui.getApp(),
                                "Upper bound must be a number",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else if (rangeOfIntegersPanelEnabled && (Integer.parseInt(lowerBoundTextField.getText()) > Integer.parseInt(upperBoundTextField.getText()))) {
                JOptionPane.showMessageDialog(
                        CreateGui.getApp(),
                        "Lower bound must be smaller than Upper bound",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else if((lowerbound.trim().isEmpty() || upperbound.trim().isEmpty()) && rangeOfIntegersPanelEnabled) {
                JOptionPane.showMessageDialog(
                        CreateGui.getApp(),
                        "You must specify both a lower and upper bound",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            else {
                String selectedColorType = colorTypeComboBox.getSelectedItem().toString();
                ColorType newColorType = new ColorType(name);

                 if(selectedColorType.equals(finiteEnumeration) || selectedColorType.equals(cyclicEnumeration)) {
                    for(int i = 0; i < enumList.getModel().getSize();i++) {
                        newColorType.addColor(enumList.getModel().getElementAt(i).toString());
                    }
                    if (newColorType.size() <= 0) {
                        JOptionPane.showMessageDialog(
                            CreateGui.getApp(),
                            "You must specify at least one enumeration name",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (oldColorType != null) {
                        if(oldColorType.equals(newColorType)){
                            exit();
                            return;
                        }
                        boolean showDialog = false;
                        for(dk.aau.cs.model.CPN.Color c : oldColorType.getColors()){
                            if(!newColorType.getColors().contains(c)){
                                showDialog = true;
                            }
                        }
                        if(showDialog){
                            int cont = JOptionPane.showConfirmDialog(this,  removeColorInColorTypeMessage, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if(cont == JOptionPane.OK_OPTION){
                                undoManager.newEdit();
                                network.updateColorType(oldColorType.getName(), newColorType, colorTypesListModel, undoManager);
                            }
                        } else{
                            network.updateColorType(oldColorType.getName(), newColorType, colorTypesListModel, undoManager);
                            colorTypesListModel.updateName();
                        }
                    } else {
                        colorTypesListModel.addElement(newColorType);
                    }

                }
                else if(selectedColorType.equals(rangeOfIntegers)) {
                    int lowerboundNumber = Integer.parseInt(lowerbound);
                    int upperboundNumber = Integer.parseInt(upperbound);
                    for(int i = lowerboundNumber; i < upperboundNumber + 1; i++) {
                        newColorType.addColor(String.valueOf(i));
                    }

                    if (oldColorType != null) {
                        if(oldColorType.equals(newColorType)){
                            exit();
                            return;
                        }
                        boolean showDialog = false;
                        for(dk.aau.cs.model.CPN.Color c : oldColorType.getColors()){
                            if(!newColorType.getColors().contains(c)){
                                showDialog = true;
                            }
                        }
                        if(showDialog) {
                            int cont = JOptionPane.showConfirmDialog(this, "<html>" + removeColorInColorTypeMessage, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (cont == JOptionPane.OK_OPTION) {
                                undoManager.newEdit();
                                network.updateColorType(oldColorType.getName(), newColorType, colorTypesListModel, undoManager);
                            }
                        } else{
                            network.updateColorType(oldColorType.getName(), newColorType, colorTypesListModel, undoManager);
                            colorTypesListModel.updateName();
                        }
                    }
                    else {
                        colorTypesListModel.addElement(newColorType);
                    }
                }
                else if(selectedColorType.equals(productColor)) {
                    ProductType productType = new ProductType(name);
                    int size = productColorTypeList.getModel().getSize();
                    for(int i = 0; i < size; i++) {
                        String colorTypeName = productColorTypeList.getModel().getElementAt(i).toString();
                        int size2 = productTypeComboBox.getItemCount();
                        for (int j = 0; j < size2; j++) {
                            if (colorTypeName.equals(productTypeComboBox.getItemAt(j).toString())) {
                                productType.addType(colorTypes.get(j));
                            }
                        }
                    }
                    if (oldColorType != null) {
                        if(oldColorType.equals(productType)){
                            exit();
                            return;
                        }
                        boolean showDialog = false;
                        for(ColorType ct : ((ProductType)oldColorType).getColorTypes()){
                            if(!productType.getColorTypes().contains(ct)){
                                showDialog = true;
                            }
                        }
                        if(!showDialog && ((ProductType)oldColorType).getColorTypes().size() != productType.getColorTypes().size()){
                            showDialog = true;
                        }
                        if(showDialog) {
                            int cont = JOptionPane.showConfirmDialog(this, "<html>" + editProductTypeMessage, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (cont == JOptionPane.OK_OPTION) {

                                undoManager.newEdit();
                                network.updateColorType(oldColorType.getName(), productType, colorTypesListModel, undoManager);
                            }
                        } else{
                            network.updateColorType(oldColorType.getName(), productType, colorTypesListModel, undoManager);
                            colorTypesListModel.updateName();
                        }
                    } else {
                        colorTypesListModel.addElement(productType);
                    }
                }
            }
            exit();
    }


}
