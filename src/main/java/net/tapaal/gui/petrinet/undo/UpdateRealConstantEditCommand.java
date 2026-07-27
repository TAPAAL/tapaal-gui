package net.tapaal.gui.petrinet.undo;

import pipe.gui.TAPAALGUI;
import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class UpdateRealConstantEditCommand implements Command {

	private final RealConstant newConstant;
	private final ConstantStore store;
	private final RealConstant oldConstant;
	private final TimedArcPetriNetNetwork model;

	public UpdateRealConstantEditCommand(RealConstant oldConstant, RealConstant newConstant,
										 ConstantStore store, TimedArcPetriNetNetwork model) {
		this.oldConstant = oldConstant;
		this.newConstant = newConstant;
		this.store = store;
		this.model = model;
	}

	@Override
	public void redo() {
		store.replace(oldConstant, newConstant);
		model.updateDistributionsWithNewConstant(oldConstant.name(), newConstant);
		TAPAALGUI.getCurrentTab().updateConstantsList();
	}

	@Override
	public void undo() {
		store.replace(newConstant, oldConstant);
		model.updateDistributionsWithNewConstant(newConstant.name(), oldConstant);
		TAPAALGUI.getCurrentTab().updateConstantsList();
	}
}
