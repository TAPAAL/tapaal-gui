package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ConstantsPane;

public class UpdateColorTypeCommand extends Command {
    private final TimedArcPetriNetNetwork network;
    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final Integer index;
    private final ConstantsPane.ColorTypesListModel colorTypesListModel;

    public UpdateColorTypeCommand(TimedArcPetriNetNetwork network, ColorType oldColorType, ColorType newColorType, Integer index, ConstantsPane.ColorTypesListModel colorTypesListModel) {
        this.network = network;
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.index = index;
        this.colorTypesListModel = colorTypesListModel;
    }

    @Override
    public void undo() {
        network.colorTypes().set(index, oldColorType);
        colorTypesListModel.updateName();
    }

    @Override
    public void redo() {
        network.colorTypes().set(index, newColorType);
        colorTypesListModel.updateName();
    }
}
