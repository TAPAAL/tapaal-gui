package dk.aau.cs.gui.components;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.ChangeAllNamesVisibilityCommand;
import dk.aau.cs.gui.undo.Command;
import pipe.gui.graphicElements.PetriNetObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class NameVisibilityPanel extends JPanel {
    private static final String DIALOG_TITLE = "Change Name Visibility";

    private static JDialog dialog;
    private JRadioButton showNames;
    private JRadioButton hideNames;
    private JRadioButton placeOption;
    private JRadioButton transitionOption;
    private JRadioButton bothOption;
    private JRadioButton selectedComponent;
    private JRadioButton allComponents;

    ButtonGroup visibilityRadioButtonGroup;
    ButtonGroup objectRadioButtonGroup;
    ButtonGroup componentRadioButtonGroup;

    JPanel visibilityPanel;
    JPanel placeTransitionPanel;
    JPanel componentPanel;

    private final TabContent tab;

    public NameVisibilityPanel(TabContent tab) {
        super(new GridBagLayout());

        this.tab = tab;

        init();
    }

    public void showNameVisibilityPanel() {
        JOptionPane optionPane = new JOptionPane(this, JOptionPane.INFORMATION_MESSAGE);

        dialog = optionPane.createDialog(DIALOG_TITLE);
        dialog.pack();
        dialog.setVisible(true);

        Object selectedValue = optionPane.getValue();

        if (selectedValue != null) ChangeNameVisibilityBasedOnSelection();
    }

    public void showNameVisibilityPanel(boolean isVisible, boolean isPlace, boolean isTransition, boolean isSelectedComponent) {
        JOptionPane optionPane = new JOptionPane(this, JOptionPane.INFORMATION_MESSAGE);

        showNames.setSelected(isVisible);
        hideNames.setSelected(!isVisible);
        placeOption.setSelected(isPlace && !isTransition);
        transitionOption.setSelected(isTransition && !isPlace);
        bothOption.setSelected(isPlace && isTransition);
        selectedComponent.setSelected(isSelectedComponent);
        allComponents.setSelected(!isSelectedComponent);

        dialog = optionPane.createDialog(DIALOG_TITLE);
        dialog.pack();
        dialog.setVisible(true);

        Object selectedValue = optionPane.getValue();

        if (selectedValue != null) ChangeNameVisibilityBasedOnSelection();
    }

    private void init() {
        visibilityPanel = initVisibilityOptions();
        placeTransitionPanel = initPlaceTransitionOptions();
        componentPanel = initComponentOptions();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);

        this.add(visibilityPanel, gbc);
        gbc.gridy = 1;
        this.add(placeTransitionPanel, gbc);
        gbc.gridy = 2;
        this.add(componentPanel, gbc);

        this.setVisible(true);
    }

    private JPanel initVisibilityOptions() {
        JPanel panel = new JPanel(new GridBagLayout());
        visibilityRadioButtonGroup = new ButtonGroup();

        JLabel text = new JLabel("Choose visibility:       ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(text, gbc);

        showNames = new JRadioButton("Show");
        showNames.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(showNames, gbc);
        visibilityRadioButtonGroup.add(showNames);

        hideNames = new JRadioButton("Hide");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hideNames, gbc);
        visibilityRadioButtonGroup.add(hideNames);

        return panel;
    }

    private JPanel initPlaceTransitionOptions() {
        JPanel panel = new JPanel(new GridBagLayout());
        objectRadioButtonGroup = new ButtonGroup();

        JLabel text = new JLabel("Choose group:           ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(text, gbc);

        placeOption = new JRadioButton("Places");
        placeOption.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(placeOption, gbc);
        objectRadioButtonGroup.add(placeOption);

        transitionOption = new JRadioButton("Transitions");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(transitionOption, gbc);
        objectRadioButtonGroup.add(transitionOption);

        bothOption = new JRadioButton("Both");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(bothOption, gbc);
        objectRadioButtonGroup.add(bothOption);

        return panel;
    }

    private JPanel initComponentOptions() {
        JPanel panel = new JPanel(new GridBagLayout());
        componentRadioButtonGroup = new ButtonGroup();

        JLabel text = new JLabel("Choose components: ");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(text, gbc);

        selectedComponent = new JRadioButton("Selected component");
        selectedComponent.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(selectedComponent, gbc);
        componentRadioButtonGroup.add(selectedComponent);

        allComponents = new JRadioButton("All components");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(allComponents, gbc);
        componentRadioButtonGroup.add(allComponents);

        return panel;
    }

    protected void ChangeNameVisibilityBasedOnSelection() {
        Map<PetriNetObject, Boolean> places = new HashMap<>();
        Map<PetriNetObject, Boolean> transitions = new HashMap<>();

        if (placeOption.isSelected() || bothOption.isSelected()) {
           places = tab.showNames(showNames.isSelected(), true, selectedComponent.isSelected());
        }
        if (transitionOption.isSelected() || bothOption.isSelected()) {
            transitions = tab.showNames(showNames.isSelected(), false, selectedComponent.isSelected());
        }

        Command changeVisibilityCommand = new ChangeAllNamesVisibilityCommand(tab, places, transitions, showNames.isSelected());
        tab.getUndoManager().addNewEdit(changeVisibilityCommand);
    }

    public boolean isShowNamesOption() {
        return showNames.isSelected();
    }

    public boolean isSelectedComponentOption() {
        return selectedComponent.isSelected();
    }

    public boolean isPlaceOption() {
        return placeOption.isSelected() || bothOption.isSelected();
    }

    public boolean isTransitionOption() {
        return transitionOption.isSelected() || bothOption.isSelected();
    }
}
