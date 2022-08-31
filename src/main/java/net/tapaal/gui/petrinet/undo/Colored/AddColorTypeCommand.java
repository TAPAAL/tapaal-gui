package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.editor.ConstantsPane;

public class AddColorTypeCommand extends Command {
    private final ColorType colorType;
    private final TimedArcPetriNetNetwork network;
    private final ConstantsPane.ColorTypesListModel colorTypesListModel;
    private final int index;

    public AddColorTypeCommand(ColorType colorType, TimedArcPetriNetNetwork network, ConstantsPane.ColorTypesListModel colorTypesListModel, int index) {
        this.colorType = colorType;
        this.network = network;
        this.colorTypesListModel = colorTypesListModel;
        this.index = index;
    }
    @Override
    public void undo() {
        new RemoveColorTypeFromNetworkCommand(colorType, network, colorTypesListModel, index).redo();
    }

    @Override
    public void redo() {
        new RemoveColorTypeFromNetworkCommand(colorType, network, colorTypesListModel, index).undo();
    }
}

