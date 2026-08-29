package net.tapaal.gui.petrinet.undo;

import pipe.gui.TAPAALGUI;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class UpdateConstantEditCommand implements Command {

	private final Constant newConstant;
	private final ConstantStore store;
	private final Constant oldConstant;
	private final TimedArcPetriNetNetwork model;

	public UpdateConstantEditCommand(Constant oldConstant, Constant newConstant,
                                     ConstantStore store, TimedArcPetriNetNetwork model) {
		this.oldConstant = oldConstant;
		this.newConstant = newConstant;
		this.store = store;
		this.model = model;
	}

	@Override
	public void redo() {
		store.replace(oldConstant, newConstant);
		model.updateGuardsAndWeightsWithNewConstant(oldConstant.name(), newConstant);
		TAPAALGUI.getCurrentTab().updateConstantsList();
	}

	@Override
	public void undo() {
		store.replace(newConstant, oldConstant);
		model.updateGuardsAndWeightsWithNewConstant(newConstant.name(), oldConstant);
		TAPAALGUI.getCurrentTab().updateConstantsList();

	}

}
