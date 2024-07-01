package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.verification.SMCDistribution;
import pipe.gui.petrinet.PetriNetTab;

public class ChangeTransitionDistributionCommand extends Command {
    private final TimedTransition transition;
    private final SMCDistribution oldValue;
    private final SMCDistribution newValue;
    private final PetriNetTab tab;

    public ChangeTransitionDistributionCommand(TimedTransition transition, PetriNetTab tab, SMCDistribution newDistribution){
        this.transition = transition;
        oldValue = transition.getDistribution();
        newValue = newDistribution;
        this.tab = tab;
    }

    @Override
    public void redo() {
        transition.setDistribution(newValue);
        tab.repaint();
    }

    @Override
    public void undo() {
        transition.setDistribution(oldValue);
        tab.repaint();
    }

}
