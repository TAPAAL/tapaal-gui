package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.Probability;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.petrinet.PetriNetTab;

public class ChangeProbabilityWeightCommand implements Command {
    private final TimedTransition transition;
    private final PetriNetTab tab;
    private final Probability oldValue;
    private final Probability newValue;
    

    public ChangeProbabilityWeightCommand(TimedTransition transition, PetriNetTab tab, Probability newWeight) {
        this.transition = transition;
        this.tab = tab;
        this.oldValue = transition.getWeight();
        this.newValue = newWeight;
    }

    @Override
    public void redo() {
        transition.setWeight(newValue);
        tab.repaint();
    }

    @Override
    public void undo() {
        transition.setWeight(oldValue);
        tab.repaint();
    }
}
