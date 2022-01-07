package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.editor.ConstantsPane;

public class RemoveColorTypeFromNetworkCommand extends Command {

    private final ColorType colorType;
    private final TimedArcPetriNetNetwork network;
    private final ConstantsPane.ColorTypesListModel colorTypesListModel;
    private final int index;

    public RemoveColorTypeFromNetworkCommand(ColorType colorType, TimedArcPetriNetNetwork network, ConstantsPane.ColorTypesListModel colorTypesListModel, int index) {
        this.colorType = colorType;
        this.network = network;
        this.colorTypesListModel = colorTypesListModel;
        this.index = index;
    }

    @Override
    public void undo() {
        network.colorTypes().add(index, colorType);
        colorTypesListModel.updateName();
    }

    @Override
    public void redo() {
        network.colorTypes().remove(colorType);
        colorTypesListModel.updateName();
    }
}
