package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class AddColorTypeCommand implements Command {
    private final ColorType colorType;
    private final TimedArcPetriNetNetwork network;
    private final Runnable onChange;
    private final int index;

    public AddColorTypeCommand(ColorType colorType, TimedArcPetriNetNetwork network, int index, Runnable onChange) {
        this.colorType = colorType;
        this.network = network;
        this.index = index;
        this.onChange = onChange == null ? () -> {} : onChange;
    }
    @Override
    public void undo() {
        new RemoveColorTypeFromNetworkCommand(colorType, network, index, onChange).redo();
    }

    @Override
    public void redo() {
        new RemoveColorTypeFromNetworkCommand(colorType, network, index, onChange).undo();
    }
}
