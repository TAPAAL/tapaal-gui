package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.SMCDistribution;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;

public class ChangeTransitionDistributionCommand implements Command {
    private final TimedTransitionComponent transitionComponent;
    private final TimedTransition transition;
    private final SMCDistribution oldValue;
    private final SMCDistribution newValue;

    public ChangeTransitionDistributionCommand(TimedTransitionComponent transitionComponent, TimedTransition transition, SMCDistribution newDistribution) {
        this.transitionComponent = transitionComponent;
        this.transition = transition;
        oldValue = transition.getDistribution();
        newValue = newDistribution;
    }

    @Override
    public void redo() {
        transition.setDistribution(newValue);
        transitionComponent.update(true);
    }

    @Override
    public void undo() {
        transition.setDistribution(oldValue);
        transitionComponent.update(true);
    }

}
