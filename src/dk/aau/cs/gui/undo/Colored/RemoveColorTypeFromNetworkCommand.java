package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.widgets.ConstantsPane;

public class RemoveColorTypeFromNetworkCommand extends Command {

    private final ColorType colorType;
    private final TimedArcPetriNetNetwork network;
    private final ConstantsPane.ColorTypesListModel colorTypesListModel;

    public RemoveColorTypeFromNetworkCommand(ColorType colorType, TimedArcPetriNetNetwork network, ConstantsPane.ColorTypesListModel colorTypesListModel) {
        this.colorType = colorType;
        this.network = network;
        this.colorTypesListModel = colorTypesListModel;
    }

    @Override
    public void undo() {
        if(!network.colorTypes().contains(colorType)) {
            network.colorTypes().add(colorType);
            colorTypesListModel.updateName();
        }
    }

    @Override
    public void redo() {
        network.colorTypes().remove(colorType);
        colorTypesListModel.updateName();
    }
}
