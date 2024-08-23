package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.FiringMode;

public class ChangeFiringModeCommand implements Command {
    private final TimedTransition transition;
    private final FiringMode oldFiringMode;
    private final FiringMode newFiringMode;

    public ChangeFiringModeCommand(TimedTransition transition, FiringMode newFiringMode) {
        this.transition = transition;
        this.oldFiringMode = transition.getFiringMode();
        this.newFiringMode = newFiringMode;
    }

    @Override
    public void redo() {
        transition.setFiringMode(newFiringMode);
    }

    @Override
    public void undo() {
        transition.setFiringMode(oldFiringMode);
    }
}
