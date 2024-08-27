package net.tapaal.gui.petrinet.undo.manualEdit;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.editor.ConstantsPane.ColorTypesListModel;
import net.tapaal.gui.petrinet.editor.ConstantsPane.VariablesListModel;
import net.tapaal.gui.petrinet.undo.Command;
import pipe.gui.TAPAALGUI;

public class EditConstantsCommand implements Command {
    private final NetworkState oldState;
    private final NetworkState newState;
    private final TimedArcPetriNetNetwork network;
    private final ColorTypesListModel colorTypesListModel;
    private final VariablesListModel variablesListModel;

    public EditConstantsCommand(NetworkState oldState, TimedArcPetriNetNetwork network, ColorTypesListModel colorTypesListModel, VariablesListModel variablesListModel) {
        this.oldState = oldState;
        this.newState = new NetworkState(network);
        this.network = network;
        this.colorTypesListModel = colorTypesListModel;
        this.variablesListModel = variablesListModel;
    }

    @Override
    public void undo() {
        network.getConstantStore().clear();
        network.setConstants(oldState.getConstants());
        network.setColorTypes(oldState.getColorTypes());
        network.setVariables(oldState.getVariables());
        TAPAALGUI.getCurrentTab().updateConstantsList();
        colorTypesListModel.updateName();
        variablesListModel.updateName();
    }

    @Override
    public void redo() {
        network.getConstantStore().clear();
        network.setConstants(newState.getConstants());
        network.setColorTypes(newState.getColorTypes());
        network.setVariables(newState.getVariables());
        TAPAALGUI.getCurrentTab().updateConstantsList();
        colorTypesListModel.updateName();
        variablesListModel.updateName();
    }
}
