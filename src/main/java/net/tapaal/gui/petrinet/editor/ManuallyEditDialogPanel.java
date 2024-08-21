package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.editor.ConstantsPane.ColorTypesListModel;
import net.tapaal.gui.petrinet.editor.ConstantsPane.VariablesListModel;
import net.tapaal.gui.petrinet.undo.manualEdit.EditConstantsCommand;
import net.tapaal.gui.petrinet.undo.manualEdit.NetworkState;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.undo.UndoManager;
import pipe.gui.swingcomponents.EscapableDialog;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import net.tapaal.gui.petrinet.undo.Command;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.CPN.ConstantsParser.ConstantsParser;
import dk.aau.cs.model.CPN.ConstantsParser.ParseException;

import javax.swing.JOptionPane;

public class ManuallyEditDialogPanel extends EscapableDialog {
    private static final int GAP = 10;
    private static final int TEXT_TO_BORDER_MARGIN = 10;
    private static final int FONT_SIZE = 14;

    private final ColorTypesListModel colorTypesListModel;
    private final VariablesListModel variablesListModel;
    private final ConstantsListModel constantsListModel;
    private final TimedArcPetriNetNetwork network;
    private final UndoManager undoManager;

    private JTextArea constantsArea;

    public ManuallyEditDialogPanel(ColorTypesListModel colorTypesListModel,
                                   VariablesListModel variablesListModel,
                                   ConstantsListModel constantsListModel,
                                   TimedArcPetriNetNetwork network,
                                   UndoManager undoManager) {
        super(TAPAALGUI.getApp(), "Manually Edit", false);
        this.colorTypesListModel = colorTypesListModel;
        this.variablesListModel = variablesListModel;
        this.constantsListModel = constantsListModel;
        this.network = network;
        this.undoManager = undoManager;

        init();
    }

    public void showDialog() {
        String cleanHtml = "</?(html|b)>";
        String angleBracketOpen = "&lt;";
        String angleBracketClose = "&gt; ";
        
        for (int i = 0; i < colorTypesListModel.getSize(); ++i) {
            ColorType ct = (ColorType)colorTypesListModel.getElementAt(i);

            if (ct.getName().equals("dot")) continue;
            String colorTypeStr = "type ";
            if (ct.isIntegerRange() && !ct.getColorList().isEmpty()) {
                colorTypeStr += ct.getName() + " is [" + ct.getLowerBound() + ", " + ct.getUpperBound() + "]";
            } else {
                colorTypeStr += ct;
            }

            colorTypeStr += ";\n";

            colorTypeStr = colorTypeStr.replaceAll(cleanHtml, "");
            colorTypeStr = colorTypeStr.replace(angleBracketOpen, "<");
            colorTypeStr = colorTypeStr.replace(angleBracketClose, ">");

            constantsArea.append(colorTypeStr);
        }
        
        Map<String, List<String>> variableTypeToName = new LinkedHashMap<>();
        for (int i = 0; i < variablesListModel.getSize(); ++i) {
            Variable v = (Variable)variablesListModel.getElementAt(i);
            String ctName = v.getColorType().getName();
            String varName = v.getName();

            variableTypeToName.putIfAbsent(ctName, new ArrayList<String>());
            variableTypeToName.get(ctName).add(varName);
        }

        for (Map.Entry<String, List<String>> entry : variableTypeToName.entrySet()) {
            String ctName = entry.getKey();
            List<String> varNames = entry.getValue();

            String varStr = "var ";
            for (String varName : varNames) {
                varStr += varName + ", ";
            }

            varStr = varStr.substring(0, varStr.length() - 2);
            varStr += " in " + ctName + ";\n";
            varStr.replaceAll(cleanHtml, "");

            constantsArea.append(varStr);
        }

        for (int i = 0; i < constantsListModel.getSize(); ++i) {
            constantsArea.append("const " + constantsListModel.getElementAt(i) + ";\n");
        }

        setVisible(true);
    }

    private void init() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel constantsLabel = new JLabel("Color Types, Variables, and Constants");
        constantsLabel.setAlignmentX(CENTER_ALIGNMENT);

        constantsArea = new JTextArea();
        constantsArea.setLineWrap(true);
        constantsArea.setWrapStyleWord(true);
        constantsArea.setMargin(new Insets(TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN));
        constantsArea.setFont(new Font(constantsArea.getFont().getName(), Font.PLAIN, FONT_SIZE));
        JScrollPane constantsScrollPane = new JScrollPane(constantsArea);

        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel helpPanel = new JPanel();
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener(e -> showHelpDialog(network.isColored()));

        helpPanel.add(helpButton);

        JPanel actionPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(e -> dispose());
    
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());

        actionPanel.add(cancelButton);
        actionPanel.add(saveButton);

        buttonPanel.add(helpPanel, BorderLayout.WEST);
        buttonPanel.add(actionPanel, BorderLayout.EAST);

        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, helpButton.getPreferredSize().height));

        panel.add(constantsLabel);
        panel.add(constantsScrollPane);;
        panel.add(Box.createVerticalStrut(GAP));
        panel.add(buttonPanel);

        add(panel);

        setSize(500, 550);
        setLocationRelativeTo(null);
    }

    private void save() {
        try {
            NetworkState oldState = new NetworkState(network);
            boolean resultOk = ConstantsParser.parse(constantsArea.getText(), network);
            if (resultOk) {
                Command command = new EditConstantsCommand(oldState, network, colorTypesListModel, variablesListModel);
                command.redo();
                undoManager.addNewEdit(command);
                dispose();
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), e.getMessage(), "Error during parsing", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHelpDialog(boolean isColored) {
        EscapableDialog helpDialog = new EscapableDialog(this, "Help", true);

        JTextArea helpTextArea = new JTextArea();

        helpTextArea.setEditable(false);

        String helpText = "Syntax for defining " + (isColored ? "color types, variables, and " : "") + "constants:\n" +
                          "const {ID} = {INTEGER};\n" +
                          "e.g. const a = 5; is a valid constant\n\n" +
                          (isColored ? "Syntax for defining color types:\n" +
                            "type {ID} is [{COLOR}, ..., {COLOR}];\n" +
                            "type ID is [{LOWER_BOUND}, {UPPER_BOUND}];\n" +
                            "type ID is <{COLOR}, ..., {COLOR}>;\n" +
                            "e.g. type Colors is [red, green, blue]; is a valid color type\n\n" : "") +
                          (isColored ? "Syntax for defining variables:\n" +
                            "var {ID} in {COLORTYPE};\n" +
                            "e.g. var c in Colors;\n\n" : "") + "All statements must end with a semicolon.";

        helpTextArea.setText(helpText);

        helpTextArea.setLineWrap(true);
        helpTextArea.setWrapStyleWord(true);
        helpTextArea.setMargin(new Insets(TEXT_TO_BORDER_MARGIN, 
                                          TEXT_TO_BORDER_MARGIN, 
                                          TEXT_TO_BORDER_MARGIN, 
                                          TEXT_TO_BORDER_MARGIN));
        helpTextArea.setFont(new Font(constantsArea.getFont().getName(), Font.PLAIN, FONT_SIZE));

        helpDialog.add(new JScrollPane(helpTextArea));
        int height = isColored ? 400 : 210;
        helpDialog.setSize(550, height);
        helpDialog.setLocationRelativeTo(this);

        helpDialog.setVisible(true);
    }
}
