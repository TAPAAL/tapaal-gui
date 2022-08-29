package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.tapn.TimedTransition;

public class ToggleTransitionUncontrollableCommand extends Command {
    private final TimedTransition transition;
    private final boolean oldValue;
    private final PetriNetTab tab;

    public ToggleTransitionUncontrollableCommand(TimedTransition transition, PetriNetTab tab){
        this.transition = transition;
        oldValue = transition.isUncontrollable();
        this.tab = tab;
    }

    @Override
    public void redo() {
        transition.setUncontrollable(!oldValue);
        tab.repaint();
    }

    @Override
    public void undo() {
        transition.setUncontrollable(oldValue);
        tab.repaint();
    }
}
