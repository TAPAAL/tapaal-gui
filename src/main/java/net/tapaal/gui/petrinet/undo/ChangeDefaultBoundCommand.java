package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class ChangeDefaultBoundCommand implements Command {
    private final TimedArcPetriNetNetwork network;
    private final int oldBound;
    private final int newBound;

    public ChangeDefaultBoundCommand(TimedArcPetriNetNetwork network, int oldBound, int newBound) {
        this.network = network;
        this.oldBound = oldBound;
        this.newBound = newBound;
    }

    @Override
    public void undo() {
        network.setDefaultBound(oldBound);
    }

    @Override
    public void redo() {
        network.setDefaultBound(newBound);
    }
}
