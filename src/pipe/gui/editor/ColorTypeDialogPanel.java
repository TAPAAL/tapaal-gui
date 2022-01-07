package pipe.gui.editor;

import dk.aau.cs.gui.components.ColortypeListCellRenderer;
import dk.aau.cs.gui.undo.Colored.AddColorTypeCommand;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import org.jdesktop.swingx.JXComboBox;
import pipe.gui.TAPAALGUI;
import pipe.gui.undo.UndoManager;
import pipe.gui.swingcomponents.EscapableDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ColorTypeDialogPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String toolTipColorComboBox = "Switch between the different defined color types";
    private static final String finiteEnumeration = "Finite Enumeration";
    private static final String cyclicEnumeration = "Cyclic Enumeration";
    private static final String rangeOfIntegers = "Range of Integers";
    private static final String productColor = "Product Color";
    private boolean rangeOfIntegersPanelEnabled = false;
    private final TimedArcPetriNetNetwork network;
    private EscapableDialog dialog;
    private List<ColorType> colorTypes;
    private ColorType oldColorType;
    private final String oldName;
    private final ConstantsPane.ColorTypesListModel colorTypesListModel;
    private final UndoManager undoManager;

    private JTextField nameTextField;
    private JComboBox colorTypeComboBox;
    private JLabel colorTypeLabel;
    private DefaultListModel productModel;
    private JButton productRemoveButton;
    private JList productColorTypeList;
    private JComboBox productTypeComboBox;

    private JTextField lowerBoundTextField;
    private JTextField upperBoundTextField;
    private JTextField enumTextField;
    private JButton enumAddButton;
    private JPanel cyclicAndFiniteEnumerationPanel;
    private JList enumList;
    private JPanel rangeOfIntegersPanel;
    private JPanel productTypePanel;
    private DefaultListModel cyclicModel;
    private JButton cyclicRemoveButton;
    private JButton okButton;

    private JScrollPane scrollPane;

    private static final int MAXIMUM_INTEGER = 10000;

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
        dialog = new EscapableDialog(TAPAALGUI.getApp(),
            "Edit color type", true);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setResizable(true);
        dialog.pack();
        //size of range of integers panel
        dialog.setMinimumSize(new Dimension(447, 231));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void initValues() {
        nameTextField.setText(oldName);
        if (!(oldColorType instanceof ProductType)) { // colortype is always either ProductType or Cyclic
            if (oldColorType.isIntegerRange()) {
                colorTypeComboBox.setSelectedIndex(1);
                for (dk.aau.cs.model.CPN.Color element : oldColorType) {
                    cyclicModel.addElement(element);
                }
                lowerBoundTextField.setText(cyclicModel.get(0).toString());

                upperBoundTextField.setText(cyclicModel.get(cyclicModel.getSize() - 1).toString());
            } else {
                colorTypeComboBox.setSelectedIndex(0);
                for (dk.aau.cs.model.CPN.Color element : oldColorType) {
                    cyclicModel.addElement(element);
                }
            }
            enumList.setModel(cyclicModel);

        } else {
            colorTypeComboBox.setSelectedIndex(2);
            for (ColorType type : ((ProductType) oldColorType).getColorTypes()) {
                productModel.addElement(type);
            }
            productColorTypeList.setModel(productModel);
        }
    }

    private void initComponents() {
        JPanel container = new JPanel();
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
        container.add(buttonPanel, gbc);
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
        nameAndTypePanel.setBorder(BorderFactory.createTitledBorder("Name of Color Type"));

        JLabel nameLabel = new JLabel();
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
                final AncestorListener al = this;
                JComponent component = ancestorEvent.getComponent();
                component.requestFocusInWindow();
                component.removeAncestorListener(al);
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
        nameAndTypePanel.add(colorTypeLabel, gbcCTL);

        colorTypeComboBox = new JXComboBox(new String[]{cyclicEnumeration, rangeOfIntegers, productColor});
        colorTypeComboBox.setToolTipText(toolTipColorComboBox);

        colorTypeComboBox.addActionListener(e -> {
            JComboBox source = (JXComboBox) e.getSource();
            final String selectedString = source.getSelectedItem().toString();

            switch (selectedString) {
                case finiteEnumeration:
                case cyclicEnumeration:
                    rangeOfIntegersPanel.setVisible(false);
                    productTypePanel.setVisible(false);
                    cyclicAndFiniteEnumerationPanel.setVisible(true);
                    rangeOfIntegersPanelEnabled = false;
                    break;
                case rangeOfIntegers:
                    cyclicAndFiniteEnumerationPanel.setVisible(false);
                    productTypePanel.setVisible(false);
                    rangeOfIntegersPanel.setVisible(true);
                    rangeOfIntegersPanelEnabled = true;
                    break;
                case productColor:
                    cyclicAndFiniteEnumerationPanel.setVisible(false);
                    productTypePanel.setVisible(true);
                    rangeOfIntegersPanel.setVisible(false);
                    rangeOfIntegersPanelEnabled = false;
                    break;
            }
            if (dialog != null) {
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

        buttonPanel.add(cancelButton, gbc);
        buttonPanel.add(okButton, gbcOk);

        return buttonPanel;
    }

    private JPanel createRangeOfIntegersPanel() {
        JPanel rangeOfIntegers = new JPanel();
        rangeOfIntegers.setLayout(new GridBagLayout());
        rangeOfIntegers.setBorder(BorderFactory.createTitledBorder("Range Of Integers"));

        JLabel lowerBoundLabel = new JLabel("Lower Bound: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        rangeOfIntegers.add(lowerBoundLabel, gbc);

        lowerBoundTextField = new JTextField();

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

        JLabel upperBoundLabel = new JLabel("Upper Bound: ");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        rangeOfIntegers.add(upperBoundLabel, gbc);

        upperBoundTextField = new JTextField();
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
        //cyclicAndFiniteEnumeration.setSize(550,300);
        JButton moveUpButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png"))));
        JButton moveDownButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png"))));

        JPanel firstRow = new JPanel();
        firstRow.setLayout(new GridBagLayout());

        JLabel enumNameLabel = new JLabel("Name: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        firstRow.add(enumNameLabel, gbc);

        enumTextField = new JTextField();
        //Dimension size = new Dimension(300, 30);
        //enumTextField.setMaximumSize(size);
        //enumTextField.setMinimumSize(size);
        //enumTextField.setPreferredSize(size);
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
            if (enumerationName == null || enumerationName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(), "You have to enter a name for the color",
                    "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", enumerationName)) {
                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "Acceptable names for enumerations are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
                    "Error", JOptionPane.ERROR_MESSAGE);
            } else if (enumerationName.equals("all") || enumerationName.equals("All") || enumerationName.equals("dot") || enumerationName.equals(".all") || enumerationName.equals(".All")) {
                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "The color cannot be named \"" + enumerationName + "\", as the name is reserved",
                    "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                boolean nameIsInUse = network.isNameUsedForVariable(enumerationName) || network.isNameUsedForColor(enumerationName, null) || network.isNameUsedForColorType(enumerationName) || network.isNameUsedForConstant(enumerationName) || nameTextField.getText().equalsIgnoreCase(enumerationName);
                for (int i = 0; i < cyclicModel.getSize(); i++) {
                    String n = cyclicModel.getElementAt(i).toString();

                    if (n.equalsIgnoreCase(enumerationName)) {
                        nameIsInUse = true;
                        break;
                    }
                }

                if (nameIsInUse) {
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "A color, color type, variable or constant with the name \"" + enumerationName + "\" already exists. Please chose an other name.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    cyclicModel.addElement(enumTextField.getText());
                    enumList.setModel(cyclicModel);
                    enumTextField.setText("");
                    cyclicRemoveButton.setEnabled(true);
                }
            }
            enumTextField.requestFocusInWindow();
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
        cyclicRemoveButton.addActionListener(actionEvent -> removeColors());
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
                    moveUpButton.setEnabled(false);
                    moveDownButton.setEnabled(false);
                } else {
                    cyclicRemoveButton.setEnabled(true);
                    if (source.getSelectedIndex() > 0) {
                        moveUpButton.setEnabled(true);
                    } else {
                        moveUpButton.setEnabled(false);
                    }

                    if (source.getSelectedIndex() < source.getModel().getSize() - 1) {
                        moveDownButton.setEnabled(true);
                    } else {
                        moveDownButton.setEnabled(false);
                    }
                }
            }
        });

        JScrollPane cyclicListScrollPane = new JScrollPane(enumList);
        cyclicListScrollPane.setViewportView(enumList);
        cyclicListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        //Dimension listSize = new Dimension(450, 150);
        //cyclicListScrollPane.setPreferredSize(listSize);
        //cyclicListScrollPane.setMinimumSize(listSize);
        //cyclicListScrollPane.setMaximumSize(listSize);
        cyclicListScrollPane.setVisible(true);
        cyclicListScrollPane.setBorder(new LineBorder(Color.GRAY));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        thirdRow.add(cyclicListScrollPane, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 3, 3, 3);

        thirdRow.add(moveUpButton, gbc);
        moveUpButton.addActionListener(e -> {
            int index = enumList.getSelectedIndex();
            if (index > 0) {
                enumList.setSelectedIndex(index - 1);
                swapColors(cyclicModel, index, index - 1);

            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(3, 3, 3, 3);

        thirdRow.add(moveDownButton, gbc);
        moveDownButton.addActionListener(e -> {
            int index = enumList.getSelectedIndex();
            if (index < cyclicModel.getSize() - 1) {
                enumList.setSelectedIndex(index + 1);
                swapColors(cyclicModel, index, index + 1);

            }
        });

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cyclicAndFiniteEnumeration.add(thirdRow, gbc);

        if (enumList.getSelectedIndex() == -1) {
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
        }

        return cyclicAndFiniteEnumeration;
    }

    private void removeColorTypes() {
        ArrayList<String> messages = new ArrayList<>();

        for (Object value : productColorTypeList.getSelectedValuesList()) {
            ArrayList<String> emptyMessages = new ArrayList<>();
            if (oldColorType == null || network.canColorTypeBeRemoved(oldColorType, emptyMessages)) {
                productModel.removeElement(value);
                productColorTypeList.setModel(productModel);
            } else if (!emptyMessages.isEmpty()){
                messages.addAll(emptyMessages);
            }
            if(productModel.size() == 0) {
                productRemoveButton.setEnabled(false);
            }
        }

        if (!messages.isEmpty()) {
            String message = "Colortype cannot have colors removed for the following reasons: \n\n";
            for (String m : messages) {
                if (!message.contains(m)) message += m;
            }
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not remove color from color type", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeColors() {
        ArrayList<String> messages = new ArrayList<>();

        int[] indices = enumList.getSelectedIndices();
        int counter = 0;

        for (Object value : enumList.getSelectedValuesList()) {
            ArrayList<String> emptyMessages = new ArrayList<>();
            dk.aau.cs.model.CPN.Color color;

            if (!(value instanceof dk.aau.cs.model.CPN.Color)) {
                ColorType colorType = new ColorType(nameTextField.getText());
                color = new dk.aau.cs.model.CPN.Color(colorType, indices[counter], value.toString());
            } else {
                color = (dk.aau.cs.model.CPN.Color) value;
            }
            if (oldColorType == null || network.canColorBeRemoved(color, emptyMessages)) {
                cyclicModel.removeElement(value);
                enumList.setModel(cyclicModel);
            } else if (!emptyMessages.isEmpty()){
                messages.addAll(emptyMessages);
            }
            counter++;
        }

        if (!messages.isEmpty()) {
            String message = "Color(s) cannot be removed for the following reasons: \n\n";
            for (String m : messages) {
                if (!message.contains(m)) message += m;
            }
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not remove color from color type", JOptionPane.WARNING_MESSAGE);
        }
    }

    //productTypePanel contains TopPanel and scrollPanePanel
    //TopPanel contains label, combobox and add/remove buttons
    //scrollPanePanel contains scrollPane and up/down buttons
    private JPanel createProductTypePanel() {
        JPanel productTypePanel = new JPanel();
        productTypePanel.setLayout(new GridBagLayout());
        productTypePanel.setBorder(BorderFactory.createTitledBorder("Product Type/ Domain"));
        GridBagConstraints gbc = new GridBagConstraints();
        JButton moveUpButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png"))));
        JButton moveDownButton = new JButton(new ImageIcon(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png"))));

        moveUpButton.setEnabled(false);
        moveDownButton.setEnabled(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        JLabel productLabel = new JLabel("Color types: ");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        topPanel.add(productLabel, gbc);

        productModel = new DefaultListModel();


        colorTypes = new ArrayList<>();
        colorTypes = network.colorTypes();

        productTypeComboBox = new JComboBox();
        productTypeComboBox.setRenderer(new ColortypeListCellRenderer());

        for (ColorType element : colorTypes) {
            if(!(element instanceof ProductType)){
                productTypeComboBox.addItem(element);
            }
        }

        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        productTypeComboBox.setBackground(Color.white);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(productTypeComboBox,gbc);

        JPanel productButtonPanel = createProductButtonsPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        topPanel.add(productButtonPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        productTypePanel.add(topPanel,gbc);

        JPanel scrollPanePanel = new JPanel();
        scrollPanePanel.setLayout(new GridBagLayout());
        productColorTypeList = new JList();
        productColorTypeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                JList source = (JList) e.getSource();
                if(source.getSelectedIndex() == -1){
                    productRemoveButton.setEnabled(false);
                    moveUpButton.setEnabled(false);
                    moveDownButton.setEnabled(false);
                } else{
                    if(source.getSelectedIndex() > 0){
                        moveUpButton.setEnabled(true);
                    } else{
                        moveUpButton.setEnabled(false);
                    }
                    if(source.getSelectedIndex() < source.getModel().getSize()-1){
                        moveDownButton.setEnabled(true);
                    } else{
                        moveDownButton.setEnabled(false);
                    }
                    productRemoveButton.setEnabled(true);
                }
            }
        });
        JScrollPane productColorsListScrollPane = new JScrollPane(productColorTypeList);
        productColorsListScrollPane.setViewportView(productColorTypeList);
        productColorsListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        //gbc.insets = new Insets(0,3,3,3);
        scrollPanePanel.add(productColorsListScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,3,3,3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        scrollPanePanel.add(moveUpButton,gbc);
        moveUpButton.addActionListener(e -> {
            int index = productColorTypeList.getSelectedIndex();
            if (index > 0) {
                productColorTypeList.setSelectedIndex(index-1);
                swapColors(productModel,index, index -1);

            }

        });
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3,3,3,3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        scrollPanePanel.add(moveDownButton, gbc);

        moveDownButton.addActionListener(e -> {
            int index = productColorTypeList.getSelectedIndex();
            if (index < productModel.getSize()-1) {
                productColorTypeList.setSelectedIndex(index+1);
                swapColors(productModel,index, index +1);

            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3,3,3,3);
        productTypePanel.add(scrollPanePanel, gbc);

        return productTypePanel;
    }

    private JPanel createProductButtonsPanel() {
        Dimension buttonSize = new Dimension(100, 30);
        JPanel productButtonPanel = new JPanel();
        productButtonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JButton productAddButton = new JButton("Add");
        productAddButton.setMnemonic(KeyEvent.VK_A);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;
        productAddButton.setPreferredSize(buttonSize);
        productButtonPanel.add(productAddButton, gbc);

        productAddButton.addActionListener(e -> {
            ArrayList<String> messages = new ArrayList<>();
            if(oldColorType == null || network.canColorTypeBeRemoved(oldColorType,messages)) {
                productModel.addElement(productTypeComboBox.getSelectedItem());
                productColorTypeList.setModel(productModel);
                productRemoveButton.setEnabled(true);
            }else{
                String message = "Colortype cannot have colors removed for the following reasons: \n\n";
                message += String.join("", messages);
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not remove color from color type", JOptionPane.WARNING_MESSAGE);
            }
        });

        productRemoveButton = new JButton("Remove");
        productRemoveButton.setEnabled(false);
        productRemoveButton.setMnemonic(KeyEvent.VK_R);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;
        productRemoveButton.setPreferredSize(buttonSize);
        productButtonPanel.add(productRemoveButton, gbc);

        productRemoveButton.addActionListener(e -> removeColorTypes());
        return productButtonPanel;
    }

    private void exit() {
        dialog.setVisible(false);
    }

    private void onOK() {
        String name = nameTextField.getText();
        String lowerbound = lowerBoundTextField.getText();
        String upperbound = upperBoundTextField.getText();

        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(), "You have to enter a name for the color",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", name)) {
            JOptionPane.showMessageDialog(
                            TAPAALGUI.getApp(),
                            "Acceptable names for color types are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
                            "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (network.isNameUsedForColorType(name) && oldColorType == null) {
            JOptionPane.showMessageDialog(
                            TAPAALGUI.getApp(),
                            "There is already another color type with the same name.\n\n"
                                    + "Choose a different name for the color type.",
                            "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (network.isNameUsedForVariable(name)) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "There is already variable with the same name.\n\n"
                    + "Choose a different name for the color type.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (network.isNameUsedForConstant(name)) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "There is already a constant with the same name.\n\n"
                    + "Choose a different name for the color type.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (network.isNameUsedForColor(name, null)) {
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "There is already a color with the same name.\n\n"
                    + "Choose a different name for the color type.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!oldName.equals("") && !oldName.equalsIgnoreCase(name) && network.isNameUsedForColorType(name)) {
            JOptionPane.showMessageDialog(
                            TAPAALGUI.getApp(),
                            "There is already another color type with the same name.\n\n"
                                    + "Choose a different name for the color type.",
                            "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!Pattern.matches("[0-9]+", lowerbound) && rangeOfIntegersPanelEnabled ) {
            JOptionPane.showMessageDialog(
                            TAPAALGUI.getApp(),
                            "Lower bound must be a nonnegative number",
                            "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!Pattern.matches("[0-9]+", upperbound) && rangeOfIntegersPanelEnabled ) {
            JOptionPane.showMessageDialog(
                            TAPAALGUI.getApp(),
                            "Upper bound must be a number",
                            "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try{
            if (rangeOfIntegersPanelEnabled && (Integer.parseInt(lowerBoundTextField.getText()) > Integer.parseInt(upperBoundTextField.getText()))) {
                JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "Lower bound must be smaller or equal than upper bound.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "Input could not be parsed as integers",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(rangeOfIntegersPanelEnabled && (Integer.parseInt(upperBoundTextField.getText()) - Integer.parseInt(lowerBoundTextField.getText())  > MAXIMUM_INTEGER)){
            JOptionPane.showMessageDialog(
                TAPAALGUI.getApp(),
                "We do not allow integer ranges with more than " + MAXIMUM_INTEGER + " elements",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //If upperbound has been made smaller or lowerbound has been made larger
        if(rangeOfIntegersPanelEnabled && oldColorType != null && (Integer.parseInt(upperBoundTextField.getText()) < Integer.parseInt(oldColorType.getColors().lastElement().getColorName()) || Integer.parseInt(lowerBoundTextField.getText()) > Integer.parseInt(oldColorType.getColors().firstElement().getColorName()))){
            //collect removed colors
            List<dk.aau.cs.model.CPN.Color> removedColors = new ArrayList<>();
            ArrayList<String> messages = new ArrayList<>();

            for(dk.aau.cs.model.CPN.Color c : oldColorType.getColors()){
                if(Integer.parseInt(c.getName()) > Integer.parseInt(upperBoundTextField.getText()) || Integer.parseInt(c.getName()) < Integer.parseInt(lowerBoundTextField.getText())){
                    //We need all the messages so we do nothing with the return value
                    network.canColorBeRemoved(c, messages);
                }
            }

            if(!messages.isEmpty()) {
                String message = "Colortype cannot have the following colors removed for the following reasons: \n\n";
                message += String.join("", messages);
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), message, "Could not remove color from color type", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if((lowerbound.trim().isEmpty() || upperbound.trim().isEmpty()) && rangeOfIntegersPanelEnabled) {
            JOptionPane.showMessageDialog(
                    TAPAALGUI.getApp(),
                    "You must specify both a lower and upper bound",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //If everything is false add the colortype
        String selectedColorType = colorTypeComboBox.getSelectedItem().toString();

        //Enum named are not allow to overlap with variable names
        switch (selectedColorType) {
            case finiteEnumeration: //Fall through
            case cyclicEnumeration:

                ArrayList<String> overlaps = new ArrayList<>();
                for (int i = 0; i < enumList.getModel().getSize(); i++) {
                    String e = enumList.getModel().getElementAt(i).toString();
                    if (network.isNameUsedForVariable(e) || network.isNameUsedForColor(e, oldColorType) || network.isNameUsedForColorType(e) || network.isNameUsedForConstant(e) || name.equalsIgnoreCase(e)) {
                        overlaps.add(e);
                    }
                }
                if (overlaps.size() > 0) {
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "Color names must not overlap with variable names or other color names: \n" +
                        "Remove or rename the following: \n" +
                            String.join(", ", overlaps),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //No overlap between

                break;
        }

        ColorType newColorType = new ColorType(name);

        switch (selectedColorType) {
            case finiteEnumeration:
            case cyclicEnumeration:
                for (int i = 0; i < enumList.getModel().getSize(); i++) {
                    newColorType.addColor(enumList.getModel().getElementAt(i).toString());
                }
                if (newColorType.size() <= 0) {
                    JOptionPane.showMessageDialog(
                        TAPAALGUI.getApp(),
                        "You must specify at least one enumeration name",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (oldColorType != null) {
                    if (oldColorType.equals(newColorType)) {
                        exit();
                        return;
                    }
                    boolean showDialog = false;
                    for (dk.aau.cs.model.CPN.Color c : oldColorType.getColors()) {
                        if (!newColorType.contains(c)) {
                            showDialog = true;
                        }
                    }
                    if (showDialog) {
                        network.updateColorType(oldColorType, newColorType, colorTypesListModel, undoManager);
                    } else {
                        undoManager.newEdit();
                        network.renameColorType(oldColorType, newColorType, colorTypesListModel, undoManager);
                        colorTypesListModel.updateName();
                    }
                } else {
                    Command cmd = new AddColorTypeCommand(newColorType,
                        network, colorTypesListModel, network.colorTypes().size());
                    undoManager.addNewEdit(cmd);
                    cmd.redo();
                }

                break;
            case rangeOfIntegers:
                int lowerboundNumber = Integer.parseInt(lowerbound);
                int upperboundNumber = Integer.parseInt(upperbound);
                for (int i = lowerboundNumber; i < upperboundNumber + 1; i++) {
                    newColorType.addColor(String.valueOf(i));
                }

                if (oldColorType != null) {
                    if (oldColorType.equals(newColorType)) {
                        exit();
                        return;
                    }
                    boolean showDialog = false;
                    for (dk.aau.cs.model.CPN.Color c : oldColorType.getColors()) {
                        if (!newColorType.getColors().contains(c)) {
                            showDialog = true;
                        }
                    }
                    if (showDialog) {
                        network.updateColorType(oldColorType, newColorType, colorTypesListModel, undoManager);
                    } else {
                        undoManager.newEdit();
                        network.renameColorType(oldColorType, newColorType, colorTypesListModel, undoManager);
                        colorTypesListModel.updateName();
                    }
                } else {
                    Command cmd = new AddColorTypeCommand(newColorType,
                        network, colorTypesListModel, network.colorTypes().size());
                    undoManager.addNewEdit(cmd);
                    cmd.redo();
                }
                break;
            case productColor:
                ProductType productType = new ProductType(name);
                List<ColorType> availableColorTypes = new ArrayList<>();
                for (ColorType type : colorTypes) {
                    if (!type.getName().equals(name)) {
                        availableColorTypes.add(type);
                    }
                }
                int size = productColorTypeList.getModel().getSize();
                for (int i = 0; i < size; i++) {
                    String colorTypeName = productColorTypeList.getModel().getElementAt(i).toString();
                    int size2 = productTypeComboBox.getItemCount();
                    for (int j = 0; j < size2; j++) {
                        if (colorTypeName.equals(productTypeComboBox.getItemAt(j).toString())) {
                            productType.addType((ColorType) productTypeComboBox.getItemAt(j));
                        }
                    }
                }
                if (oldColorType != null) {
                    if (oldColorType.equals(productType)) {
                        exit();
                        return;
                    }
                    boolean showDialog = false;
                    for (ColorType ct : ((ProductType) oldColorType).getColorTypes()) {
                        if (!productType.getColorTypes().contains(ct)) {
                            showDialog = true;
                            break;
                        }
                    }
                    if (!showDialog && ((ProductType) oldColorType).getColorTypes().size() != productType.getColorTypes().size()) {
                        showDialog = true;
                    }
                    if (showDialog) {
                        network.updateColorType(oldColorType, productType, colorTypesListModel, undoManager);

                    } else {
                        undoManager.newEdit();
                        network.renameColorType(oldColorType, productType, colorTypesListModel, undoManager);
                        colorTypesListModel.updateName();
                    }
                } else {
                    Command cmd = new AddColorTypeCommand(productType,
                        network, colorTypesListModel, network.colorTypes().size());
                    undoManager.addNewEdit(cmd);
                    cmd.redo();
                }
                break;
        }

        exit();
    }

    public void swapColors(DefaultListModel model, int selectedIndex, int newIndex){
        var temp = model.get(newIndex);
        model.set(newIndex, model.get(selectedIndex));
        model.set(selectedIndex, temp);
    }
}
