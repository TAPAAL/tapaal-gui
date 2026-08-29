package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.Probability;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;

public class ChangeProbabilityWeightCommand implements Command {
    private final TimedTransitionComponent transitionComponent;
    private final TimedTransition transition;
    private final Probability oldValue;
    private final Probability newValue;
    
    public ChangeProbabilityWeightCommand(TimedTransitionComponent transitionComponent, TimedTransition transition, Probability newWeight) {
        this.transitionComponent = transitionComponent;
        this.transition = transition;
        this.oldValue = transition.getWeight();
        this.newValue = newWeight;
    }

    @Override
    public void redo() {
        transition.setWeight(newValue);
        transitionComponent.update(true);
    }

    @Override
    public void undo() {
        transition.setWeight(oldValue);
        transitionComponent.update(true);
    }
}
