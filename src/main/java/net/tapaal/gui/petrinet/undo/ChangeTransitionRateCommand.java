package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.petrinet.PetriNetTab;

public class ChangeTransitionRateCommand extends Command {
    private final TimedTransition transition;
    private final float oldValue;
    private final float newValue;
    private final PetriNetTab tab;

    public ChangeTransitionRateCommand(TimedTransition transition, PetriNetTab tab, float newRate){
        this.transition = transition;
        oldValue = transition.getRate();
        newValue = newRate;
        this.tab = tab;
    }

    @Override
    public void redo() {
        transition.setRate(newValue);
        tab.repaint();
    }

    @Override
    public void undo() {
        transition.setRate(oldValue);
        tab.repaint();
    }

}
