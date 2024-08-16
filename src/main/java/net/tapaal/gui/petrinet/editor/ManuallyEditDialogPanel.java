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
import java.awt.BorderLayout;
import java.awt.Dimension;

public class ManuallyEditDialogPanel extends EscapableDialog {
    private final ColorTypesListModel colorTypesListModel;
    private final VariablesListModel variablesListModel;
    private final ConstantsListModel constantsListModel;
    private final TimedArcPetriNetNetwork network;
    private final UndoManager undoManager;
    private static final int GAP = 10;

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
        setVisible(true);
    }

    private void init() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel constantsLabel = new JLabel("Constants");
        constantsLabel.setAlignmentX(CENTER_ALIGNMENT);

        JTextArea constantsArea = new JTextArea();
        constantsArea.setLineWrap(true);
        constantsArea.setWrapStyleWord(true);
        
        JLabel variablesLabel = new JLabel("Variables");
        variablesLabel.setAlignmentX(CENTER_ALIGNMENT);

        JTextArea variablesArea = new JTextArea();
        variablesArea.setLineWrap(true);
        variablesArea.setWrapStyleWord(true);

        JLabel colorTypesLabel = new JLabel("Color Types");
        colorTypesLabel.setAlignmentX(CENTER_ALIGNMENT);

        JTextArea colorTypesArea = new JTextArea();
        colorTypesArea.setLineWrap(true);
        colorTypesArea.setWrapStyleWord(true);

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
        panel.add(constantsArea);
        panel.add(Box.createVerticalStrut(GAP));
        panel.add(variablesLabel);
        panel.add(variablesArea);
        panel.add(Box.createVerticalStrut(GAP));
        panel.add(colorTypesLabel);
        panel.add(colorTypesArea);
        panel.add(Box.createVerticalStrut(GAP));
        panel.add(buttonPanel);

        add(panel);

        setSize(600, 800);
        setLocationRelativeTo(null);
    }
}
