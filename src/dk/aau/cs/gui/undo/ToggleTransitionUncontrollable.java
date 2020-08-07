package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.TimedTransition;

public class ToggleTransitionUncontrollable extends Command {
    private final TimedTransition transition;
    private final boolean oldValue;

    public ToggleTransitionUncontrollable(TimedTransition transition){
        this.transition = transition;
        oldValue = transition.isUncontrollable();
    }

    @Override
    public void redo() {
        transition.setUncontrollable(!oldValue);
    }

    @Override
    public void undo() {
        transition.setUncontrollable(oldValue);
    }
}
