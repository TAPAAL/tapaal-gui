package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import dk.aau.cs.model.tapn.LocalTimedMarking;

import java.util.List;

public class ColoredTransitionStep implements PetriNetStep {
    private final TimedTransition transition;
    private final List<String> bindings;

    private LocalTimedMarking postMarking;

    public ColoredTransitionStep(TimedTransition transition, List<String> bindings) {
        Require.that(transition != null, "transition cannot be null");
        this.transition = transition;
        this.bindings = bindings;
    }

    public TimedTransition transition() {
		return transition;
	}

    public List<String> bindings() {
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
