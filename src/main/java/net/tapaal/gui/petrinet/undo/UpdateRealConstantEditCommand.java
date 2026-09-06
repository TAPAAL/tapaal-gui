package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class UpdateRealConstantEditCommand implements Command {

	private final RealConstant newConstant;
	private final ConstantStore store;
	private final RealConstant oldConstant;
	private final TimedArcPetriNetNetwork model;
	private final Runnable onChange;

	public UpdateRealConstantEditCommand(RealConstant oldConstant, RealConstant newConstant,
										 ConstantStore store, TimedArcPetriNetNetwork model) {
		this(oldConstant, newConstant, store, model, () -> {});
	}

	public UpdateRealConstantEditCommand(RealConstant oldConstant, RealConstant newConstant,
										 ConstantStore store, TimedArcPetriNetNetwork model, Runnable onChange) {
		this.oldConstant = oldConstant;
		this.newConstant = newConstant;
		this.store = store;
		this.model = model;
		this.onChange = onChange == null ? () -> {} : onChange;
	}

	@Override
	public void redo() {
		store.replace(oldConstant, newConstant);
		model.updateDistributionsWithNewConstant(oldConstant.name(), newConstant);
		onChange.run();
	}

	@Override
	public void undo() {
		store.replace(newConstant, oldConstant);
		model.updateDistributionsWithNewConstant(newConstant.name(), oldConstant);
		onChange.run();
	}
}
