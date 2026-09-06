package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class RemoveColorTypeFromNetworkCommand implements Command {

    private final ColorType colorType;
    private final TimedArcPetriNetNetwork network;
    private final Runnable onChange;
    private final int index;

    public RemoveColorTypeFromNetworkCommand(ColorType colorType, TimedArcPetriNetNetwork network, int index, Runnable onChange) {
        this.colorType = colorType;
        this.network = network;
        this.index = index;
        this.onChange = onChange == null ? () -> {} : onChange;
    }

    @Override
    public void undo() {
        network.colorTypes().add(index, colorType);
        onChange.run();
    }

    @Override
    public void redo() {
        network.colorTypes().remove(colorType);
        onChange.run();
    }
}
