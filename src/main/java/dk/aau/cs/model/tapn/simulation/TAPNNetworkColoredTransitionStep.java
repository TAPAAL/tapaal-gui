package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedTransition;

import java.util.List;

import dk.aau.cs.model.tapn.LocalTimedMarking;

public class TAPNNetworkColoredTransitionStep extends TAPNNetworkTraceStep  {
    private final TimedTransition transition;
    private final List<String> bindings;
    private final NetworkMarking marking;

    public TAPNNetworkColoredTransitionStep(TimedTransition transition, List<String> bindings, LocalTimedMarking marking) {
        this.transition = transition;
        this.bindings = bindings;
        NetworkMarking networkMarking = marking.getNetworkMarking().clone();
        networkMarking.updateMarking(marking);
        this.marking = networkMarking;
    }

    public TimedTransition getTransition() {
        return transition;
    }

    public List<String> getBindings() {
        return bindings;
    }

    public NetworkMarking getMarking() {
        return marking;
    }

    @Override
    public NetworkMarking performStepFrom(NetworkMarking marking) {
        throw new UnsupportedOperationException("This method is not implemented for TAPNNeworkColoredTransitionStep");
    }

    @Override
    public String toString() {
        String transitionName = transition.isShared() ? transition.sharedTransition().name() : transition.name();
        return isLoopStep() ? formatAsLoopStep(transitionName) : transitionName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        TAPNNetworkColoredTransitionStep otherStep = (TAPNNetworkColoredTransitionStep)other;
        if (!transition.equals(otherStep.transition)) return false;
        return marking.equals(otherStep.marking);
    }
}
