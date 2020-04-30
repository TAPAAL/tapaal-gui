package pipe.gui.undo;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class UpdateConstantEdit extends Command {

	private final Constant newConstant;
	private final ConstantStore store;
	private final Constant oldConstant;
	private final TimedArcPetriNetNetwork model;

	public UpdateConstantEdit(Constant oldConstant, Constant newConstant,
			ConstantStore store, TimedArcPetriNetNetwork model) {
		this.oldConstant = oldConstant;
		this.newConstant = newConstant;
		this.store = store;
		this.model = model;
	}

	@Override
	public void redo() {
		store.remove(oldConstant);
		store.add(newConstant);
		model.updateGuardsAndWeightsWithNewConstant(oldConstant.name(), newConstant);
		CreateGui.getCurrentTab().updateConstantsList();
	}

	@Override
	public void undo() {
		store.remove(newConstant);
		store.add(oldConstant);
		model.updateGuardsAndWeightsWithNewConstant(newConstant.name(), oldConstant);
		CreateGui.getCurrentTab().updateConstantsList();

	}

}
