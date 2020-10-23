package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.widgets.ConstantsPane;

public class UpdateColorTypeCommand extends Command {
    private final TimedArcPetriNetNetwork network;
    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final Integer index;

    public UpdateColorTypeCommand(TimedArcPetriNetNetwork network, ColorType oldColorType, ColorType newColorType, Integer index) {
        this.network = network;
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.index = index;
    }

    @Override
    public void undo() {
        network.colorTypes().set(index, oldColorType);
    }

    @Override
    public void redo() {
        network.colorTypes().set(index, newColorType);
    }
}
