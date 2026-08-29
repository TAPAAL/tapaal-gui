package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.LocalTimedMarking;

import java.util.Map;

public class ColoredTransitionStep implements PetriNetStep {
    private final TimedTransition transition;
    private final Map<Variable, Color> bindings;

    private LocalTimedMarking postMarking;

    public ColoredTransitionStep(TimedTransition transition, Map<Variable, Color> bindings) {
        Require.that(transition != null, "transition cannot be null");
        this.transition = transition;
        this.bindings = bindings;
    }

    public TimedTransition transition() {
		return transition;
	}

    public Map<Variable, Color> bindings() {
        return bindings;
    }

    public LocalTimedMarking getPostMarking() {
        return postMarking;
    }

    public void setPostMarking(LocalTimedMarking postMarking) {
        this.postMarking = postMarking;
    }

    @Override
	public String toString() {
		return transition.toString();
	}
}
