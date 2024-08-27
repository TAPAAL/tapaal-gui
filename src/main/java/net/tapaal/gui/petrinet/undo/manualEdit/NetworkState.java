package net.tapaal.gui.petrinet.undo.manualEdit;

import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import java.util.stream.Collectors;

import java.util.List;

public class NetworkState {
    private final List<Constant> constants;
    private final List<ColorType> colorTypes;
    private final List<Variable> variables;

    public NetworkState(TimedArcPetriNetNetwork network) {
        this.constants = network.constants().stream().collect(Collectors.toList());
        this.colorTypes = network.colorTypes().stream().collect(Collectors.toList());
        this.variables = network.variables().stream().collect(Collectors.toList());
    }

    public NetworkState(NetworkState state) {
        this.constants = state.getConstants();
        this.colorTypes = state.getColorTypes();
        this.variables = state.getVariables();
    }

    public List<Constant> getConstants() {
        return constants;
    }

    public List<ColorType> getColorTypes() {
        return colorTypes;
    }

    public List<Variable> getVariables() {
        return variables;
    }
}
