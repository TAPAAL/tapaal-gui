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
    private JTextArea variablesArea;
    private JTextArea colorTypesArea;

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

        for (int i = 0; i < constantsListModel.getSize(); ++i) {
            constantsArea.append(constantsListModel.getElementAt(i) + ";\n");
        }

        for (int i = 0; i < variablesListModel.getSize(); ++i) {
            variablesArea.append((variablesListModel.getElementAt(i) + ";\n").replaceAll(cleanHtml, ""));
        }

        for (int i = 0; i < colorTypesListModel.getSize(); ++i) {
            colorTypesArea.append((colorTypesListModel.getElementAt(i) + ";\n").replaceAll(cleanHtml, ""));
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

        JLabel variablesLabel = new JLabel("Variables");
        variablesLabel.setAlignmentX(CENTER_ALIGNMENT);

        variablesArea = new JTextArea();
        variablesArea.setLineWrap(true);
        variablesArea.setWrapStyleWord(true);
        variablesArea.setMargin(new Insets(TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN));
        variablesArea.setFont(new Font(variablesArea.getFont().getName(), Font.PLAIN, FONT_SIZE));
        JScrollPane variablesScrollPane = new JScrollPane(variablesArea);

        JLabel colorTypesLabel = new JLabel("Color Types");
        colorTypesLabel.setAlignmentX(CENTER_ALIGNMENT);

        colorTypesArea = new JTextArea();
        colorTypesArea.setLineWrap(true);
        colorTypesArea.setWrapStyleWord(true);
        colorTypesArea.setMargin(new Insets(TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN, 
                                            TEXT_TO_BORDER_MARGIN));
        colorTypesArea.setFont(new Font(colorTypesArea.getFont().getName(), Font.PLAIN, FONT_SIZE));
        JScrollPane colorTypesScrollPane = new JScrollPane(colorTypesArea);

        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel helpPanel = new JPanel();
        JButton helpButton = new JButton("Help");

        helpPanel.add(helpButton);

        JPanel actionPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(e -> dispose());
    
        JButton saveButton = new JButton("Save");

        actionPanel.add(cancelButton);
        actionPanel.add(saveButton);

        buttonPanel.add(helpPanel, BorderLayout.WEST);
        buttonPanel.add(actionPanel, BorderLayout.EAST);

        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, helpButton.getPreferredSize().height));

        panel.add(constantsLabel);
        panel.add(constantsScrollPane);
        panel.add(Box.createVerticalStrut(GAP));
        panel.add(variablesLabel);
        panel.add(variablesScrollPane);
        panel.add(Box.createVerticalStrut(GAP));
        panel.add(colorTypesLabel);
        panel.add(colorTypesScrollPane);
        panel.add(Box.createVerticalStrut(GAP));
        panel.add(buttonPanel);

        add(panel);

        setSize(600, 800);
        setLocationRelativeTo(null);
    }
}
