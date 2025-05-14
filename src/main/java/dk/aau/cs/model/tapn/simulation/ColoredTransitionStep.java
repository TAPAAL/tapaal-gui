package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

import java.util.List;
import java.util.Map;

public class ColoredTransitionStep implements PetriNetStep {
    private final TimedTransition transition;
    private final Map<String, List<String>> bindings;

    public ColoredTransitionStep(TimedTransition transition, Map<String, List<String>> bindings) {
        Require.that(transition != null, "transition cannot be null");
        this.transition = transition;
        this.bindings = bindings;
    }

    public TimedTransition transition() {
		return transition;
	}

    public Map<String, List<String>> bindings() {
        return bindings;
    }

    @Override
	public String toString() {
		return transition.toString();
	}
}
