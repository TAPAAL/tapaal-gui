package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.widgets.ConstantsPane;

import javax.swing.*;

public class UpdateColorTypePanelCommand extends Command {
    private ConstantsPane.ColorTypesListModel colorTypesListModel;
    private JList<?> list;

    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton editBtn;
    private JButton removeBtn;

    public UpdateColorTypePanelCommand(ConstantsPane.ColorTypesListModel colorTypesListModel, JList<?> list, JButton moveUpButton,
                                       JButton moveDownButton, JButton editBtn, JButton removeBtn) {
        this.colorTypesListModel = colorTypesListModel;
        this.list = list;
        this.moveUpButton = moveUpButton;
        this.moveDownButton = moveDownButton;
        this.editBtn = editBtn;
        this.removeBtn = removeBtn;
    }


    @Override
    public void undo() {
        colorTypesListModel.updateName();

        int numElements = list.getModel().getSize();
        if (numElements > 1) {
            moveDownButton.setEnabled(true);
            moveUpButton.setEnabled(true);
        }
        if (numElements > 0) {
            removeBtn.setEnabled(true);
            editBtn.setEnabled(true);
        }
    }

    @Override
    public void redo() {
        colorTypesListModel.updateName();

        int numElements = list.getModel().getSize();
        if (numElements <= 1) {
            moveDownButton.setEnabled(false);
            moveUpButton.setEnabled(false);
        }
        if (numElements <= 0) {
            removeBtn.setEnabled(false);
            editBtn.setEnabled(false);
        }
    }
}
