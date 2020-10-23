package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class RemoveColorTypeFromNetworkCommand extends Command {

    private final ColorType colorType;
    private final TimedArcPetriNetNetwork network;

    public RemoveColorTypeFromNetworkCommand(ColorType colorType, TimedArcPetriNetNetwork network) {
        this.colorType = colorType;
        this.network = network;
    }

    @Override
    public void undo() {

        if(!network.colorTypes().contains(colorType)) {
            network.colorTypes().add(colorType);
        }

    }

    @Override
    public void redo() {

        network.colorTypes().remove(colorType);
    }
}
