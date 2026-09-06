package net.tapaal.gui.petrinet.undo.manualEdit;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.editor.ConstantsPane.ColorTypesListModel;
import net.tapaal.gui.petrinet.editor.ConstantsPane.VariablesListModel;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.undo.Command;

public class EditConstantsCommand implements Command {
    private final NetworkState oldState;
    private final NetworkState newState;
    private final TimedArcPetriNetNetwork network;
    private final ColorTypesListModel colorTypesListModel;
    private final VariablesListModel variablesListModel;
    private final PetriNetTab tab;

    public EditConstantsCommand(NetworkState oldState, TimedArcPetriNetNetwork network, ColorTypesListModel colorTypesListModel, VariablesListModel variablesListModel, PetriNetTab tab) {
        this.oldState = oldState;
        this.newState = new NetworkState(network);
        this.network = network;
        this.colorTypesListModel = colorTypesListModel;
        this.variablesListModel = variablesListModel;
        this.tab = tab;
    }

    @Override
    public void undo() {
        network.getConstantStore().clear();
        network.setConstants(oldState.getConstants());
        network.setRealConstants(oldState.getRealConstants());
        network.setColorTypes(oldState.getColorTypes());
        network.setVariables(oldState.getVariables());
        tab.updateConstantsList();
        colorTypesListModel.updateName();
        variablesListModel.updateName();
    }

    @Override
    public void redo() {
        network.getConstantStore().clear();
        network.setConstants(newState.getConstants());
        network.setRealConstants(newState.getRealConstants());
        network.setColorTypes(newState.getColorTypes());
        network.setVariables(newState.getVariables());
        tab.updateConstantsList();
        colorTypesListModel.updateName();
        variablesListModel.updateName();
    }
}
