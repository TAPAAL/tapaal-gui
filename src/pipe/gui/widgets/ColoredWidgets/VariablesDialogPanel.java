package pipe.gui.widgets.ColoredWidgets;

import dk.aau.cs.gui.undo.Colored.AddVariableCommand;
import dk.aau.cs.gui.undo.Colored.UpdateVariableCommand;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.CreateGui;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.ConstantsPane;
import pipe.gui.widgets.EscapableDialog;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VariablesDialogPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JRootPane rootPane;
    private TimedArcPetriNetNetwork network;
    private EscapableDialog dialog;
    private Variable variable;
    private List<ColorType> colorTypes;
    private ConstantsPane.VariablesListModel listModel;

    private String oldName;

    JTextField nameTextField;
    Dimension size;
    JLabel nameLabel;

    JComboBox<ColorType> colorTypeComboBox;
    JLabel colorTypeLabel;

    JPanel container;
    JPanel buttonContainer;
    JButton okButton;
    JButton cancelButton;
    private JScrollPane scrollPane;
    private UndoManager undoManager;

    public VariablesDialogPanel() throws IOException {
        initComponents();
    }

    public VariablesDialogPanel(JRootPane pane, ConstantsPane.VariablesListModel listModel, TimedArcPetriNetNetwork network, UndoManager undoManager) throws IOException {
        rootPane = pane;
        oldName = "";
        this.network = network;
        this.listModel = listModel;
        initComponents();
        nameTextField.setText(oldName);
        this.undoManager = undoManager;


    }

    public VariablesDialogPanel(JRootPane pane, ConstantsPane.VariablesListModel listModel, TimedArcPetriNetNetwork network, Variable variable,UndoManager undoManager) throws IOException {
        rootPane = pane;
        this.variable = variable;
        // set combobox value to the already chosen ColorType
        oldName = variable.getName();
        this.network = network;
        this.listModel = listModel;
        initComponents();
        nameTextField.setText(oldName);
        this.undoManager = undoManager;
    }

    public void showDialog() {
        String panelHeader = variable != null? "Edit Variable" : "Create Variable";

        dialog = new EscapableDialog(CreateGui.getApp(),
                panelHeader, true);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void initComponents() throws IOException {
        container = new JPanel();
        buttonContainer = new JPanel();
        container.setLayout(new GridBagLayout());
        size = new Dimension(330, 30);

        createCancelButton();
        createColorTypeLabel();
        createcolorTypesComboBox();
        createNameLabel();
        createNameTextField();
        createOKButton();

        okButton.addActionListener(e -> onOK());

        cancelButton.addActionListener(e -> exit());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 5, 8);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        container.add(buttonContainer,gbc);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(container);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private void createNameTextField() {
        nameTextField = new JTextField();
        nameTextField.setPreferredSize(size);
        nameTextField.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent ancestorEvent) {
                final AncestorListener al= this;
                SwingUtilities.invokeLater(() -> {
                    JComponent component = ancestorEvent.getComponent();
                    component.requestFocusInWindow();
                    component.removeAncestorListener( al );
                });
            }

            @Override
            public void ancestorRemoved(AncestorEvent ancestorEvent) {

            }

            @Override
            public void ancestorMoved(AncestorEvent ancestorEvent) {

            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 2, 4);
        container.add(nameTextField,gbc);
    }

    private void createNameLabel() {
        nameLabel = new JLabel();
        nameLabel.setText("Name: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        container.add(nameLabel,gbc);
    }

    private void createColorTypeLabel() {
        colorTypeLabel = new JLabel();
        colorTypeLabel.setText("Color type: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        container.add(colorTypeLabel,gbc);
    }


    private void createcolorTypesComboBox() {
        colorTypes = new ArrayList<ColorType>();
        colorTypes = network.colorTypes();

        colorTypeComboBox = new JComboBox();

        int variableIndex = 0;
        for (ColorType element : colorTypes) {
            if(!element.isProductColorType()){
                colorTypeComboBox.addItem(element);
                if (variable != null) {
                    if (element.getName().equals(variable.getColorType().getName())) {
                        variableIndex = colorTypeComboBox.getItemCount() - 1;
                    }
                }
            }
        }
        colorTypeComboBox.setSelectedIndex(variableIndex);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        container.add(colorTypeComboBox,gbc);
    }

    private void createOKButton() {
        okButton = new JButton();
        okButton.setText("OK");
        okButton.setMaximumSize(new Dimension(100, 25));
        okButton.setMinimumSize(new Dimension(100, 25));
        okButton.setPreferredSize(new Dimension(100, 25));
        okButton.setMnemonic(KeyEvent.VK_O);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        buttonContainer.add(okButton,gbc);
    }

    private void createCancelButton() {
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(100, 25));
        cancelButton.setMinimumSize(new Dimension(100, 25));
        cancelButton.setPreferredSize(new Dimension(100, 25));
        cancelButton.setMnemonic(KeyEvent.VK_C);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.EAST;
        buttonContainer.add(cancelButton,gbc);
    }

    private void exit() {
        dialog.setVisible(false);
    }

    private void onOK() {
        if (colorTypeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
                    "You have to choose a color type for the variable. If none are present you have to create one first under variables.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            colorTypeComboBox.requestFocusInWindow();
            return;
        }
        String newName = nameTextField.getText();
        if (newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    CreateGui.getApp(),
                    "You have to enter a name in the textfield in order to create a new variable",
                    "Error", JOptionPane.ERROR_MESSAGE);
            colorTypeComboBox.requestFocusInWindow();
            return;
        }
        if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)) {
            JOptionPane
                    .showMessageDialog(
                            CreateGui.getApp(),
                            "Acceptable names for variables are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
                            "Error", JOptionPane.ERROR_MESSAGE);
            nameTextField.requestFocusInWindow();
            return;
        }
        if (network.isNameUsedForVariable(newName)) {
            JOptionPane
                    .showMessageDialog(
                            CreateGui.getApp(),
                            "There is already another variable with the same name.\n\n"
                                    + "Choose a different name for the variable.",
                            "Error", JOptionPane.ERROR_MESSAGE);
            nameTextField.requestFocusInWindow();
            return;
        }
        if (!oldName.equals("") && !oldName.equalsIgnoreCase(newName) && network.isNameUsedForVariable(newName)) {
            JOptionPane
                    .showMessageDialog(
                            CreateGui.getApp(),
                            "There is already another variable with the same name.\n\n"
                                    + "Choose a different name for the variable.",
                            "Error", JOptionPane.ERROR_MESSAGE);
            nameTextField.requestFocusInWindow();
            return;
        }
        Command cmd;
        if (!oldName.equals("")) {
            cmd = new UpdateVariableCommand(variable, nameTextField.getText(), colorTypes.get(colorTypeComboBox.getSelectedIndex()), listModel);
        }
        else {
            cmd = new AddVariableCommand(new Variable(nameTextField.getText(), "Var" + nameTextField.getText(), (ColorType) colorTypeComboBox.getSelectedItem()),
                network, listModel, network.variables().size());
            //listModel.addElement(new Variable(nameTextField.getText(),"Var" + nameTextField.getText(), (ColorType) colorTypeComboBox.getSelectedItem()));
        }
        undoManager.addNewEdit(cmd);
        cmd.redo();
        exit();

    }

}
