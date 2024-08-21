package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.editor.ConstantsPane.ColorTypesListModel;
import net.tapaal.gui.petrinet.editor.ConstantsPane.VariablesListModel;
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

import dk.aau.cs.model.CPN.ColorType;
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
        
        for (int i = 0; i < colorTypesListModel.getSize(); ++i) {
            if (((ColorType)colorTypesListModel.getElementAt(i)).getName().equals("dot")) continue;
            constantsArea.append((colorTypesListModel.getElementAt(i) + ";\n").replaceAll(cleanHtml, ""));
        }
        
        for (int i = 0; i < variablesListModel.getSize(); ++i) {
            constantsArea.append((variablesListModel.getElementAt(i) + ";\n").replaceAll(cleanHtml, ""));
        }

        for (int i = 0; i < constantsListModel.getSize(); ++i) {
            constantsArea.append(constantsListModel.getElementAt(i) + ";\n");
        }

        setVisible(true);
    }

    private void init() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel constantsLabel = new JLabel("Constants");
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
            ConstantsParser.parse(constantsArea.getText(), network);
            undoManager.newEdit();
            dispose();
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), e.getMessage(), "Error parsing constants", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHelpDialog(boolean isColored) {
        EscapableDialog helpDialog = new EscapableDialog(this, "Help", true);

        JTextArea helpTextArea = new JTextArea();

        helpTextArea.setEditable(false);

        String helpText = "Syntax for defining constants:\n" +
                          "<ID> = <INTEGER>;\n" +
                          "e.g. a = 5; is a valid constant\n\n" +
                          (isColored ? "Syntax for defining color types:\n" +
                            "<ID> is [<COLOR>, ..., <COLOR>]\n" +
                            "e.g. color is [red, green, blue]; is a valid color type\n\n" : "") +
                          (isColored ? "Syntax for defining variables:\n" +
                            "<ID> in <COLORTYPE>\n" +
                            "e.g. c in Color\n\n" : "") + "All statements must end with a semicolon.";

        helpTextArea.setText(helpText);

        helpTextArea.setLineWrap(true);
        helpTextArea.setWrapStyleWord(true);
        helpTextArea.setMargin(new Insets(TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN));
        helpTextArea.setFont(new Font(constantsArea.getFont().getName(), Font.PLAIN, FONT_SIZE));

        helpDialog.add(new JScrollPane(helpTextArea));
        int height = isColored ? 350 : 210;
        helpDialog.setSize(450, height);
        helpDialog.setLocationRelativeTo(this);

        helpDialog.setVisible(true);
    }
}
