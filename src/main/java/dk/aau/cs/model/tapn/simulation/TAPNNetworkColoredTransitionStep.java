package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Tuple;

import java.util.List;
import java.util.Map;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.LocalTimedMarking;

public class TAPNNetworkColoredTransitionStep extends TAPNNetworkTraceStep  {
    private final TimedTransition transition;
    private final Map<Variable, Color> bindings;
    private final NetworkMarking marking;

    public TAPNNetworkColoredTransitionStep(TimedTransition transition, Map<Variable, Color> bindings, LocalTimedMarking marking, Map<TimedPlace, List<TimedToken>> sharedPlacesToTokensMap) {
        this.transition = transition;
        this.bindings = bindings;
        NetworkMarking networkMarking = marking.getNetworkMarking().clone();
        networkMarking.updateMarking(marking, sharedPlacesToTokensMap);
        this.marking = networkMarking;
    }

    public TAPNNetworkColoredTransitionStep(TimedTransition transition, Tuple<Variable, Color> binding, NetworkMarking marking) {
        this.transition = transition;
        this.bindings = Map.of(binding.value1(), binding.value2());
        this.marking = marking;
    }

    public TimedTransition getTransition() {
        return transition;
    }

    public Map<Variable, Color> getBindings() {
        return bindings;
    }

    public NetworkMarking getMarking() {
        return marking;
    }

    @Override
    public boolean isColoredTransitionStep() {
        return true;
    }

    @Override
    public NetworkMarking performStepFrom(NetworkMarking marking) {
        throw new UnsupportedOperationException("This method is not implemented for TAPNNeworkColoredTransitionStep");
    }

    @Override
    public String toString() {
        String transitionName = transition.isShared() ? transition.sharedTransition().name() : transition.name();
        String result = isLoopStep() ? formatAsLoopStep(transitionName) : transitionName;

        StringBuilder sb = new StringBuilder();
        if (!bindings.isEmpty()) {
            sb.append(" [");
            int i = 0;
            for (Map.Entry<Variable, Color> entry : bindings.entrySet()) {
                sb.append(entry.getKey().getId()).append("->").append(entry.getValue().getName());
                if (i < bindings.size() - 1) {
                    sb.append(",");
                }
                
                ++i;
            }
    
            sb.append("]");
        } 

        result += sb.toString();

        return result;
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
