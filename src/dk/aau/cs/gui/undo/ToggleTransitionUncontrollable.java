package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.TimedTransition;

public class ToggleTransitionUncontrollable extends Command {
    private final TimedTransition transition;
    private final boolean oldValue;
    private final TabContent tab;

    public ToggleTransitionUncontrollable(TimedTransition transition, TabContent tab){
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
