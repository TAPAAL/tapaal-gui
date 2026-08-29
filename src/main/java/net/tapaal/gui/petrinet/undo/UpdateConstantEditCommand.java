package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class UpdateConstantEditCommand implements Command {

	private final Constant newConstant;
	private final ConstantStore store;
	private final Constant oldConstant;
	private final TimedArcPetriNetNetwork model;
	private final Runnable onChange;

	public UpdateConstantEditCommand(Constant oldConstant, Constant newConstant,
                                     ConstantStore store, TimedArcPetriNetNetwork model) {
		this(oldConstant, newConstant, store, model, () -> {});
	}

	public UpdateConstantEditCommand(Constant oldConstant, Constant newConstant,
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
		model.updateGuardsAndWeightsWithNewConstant(oldConstant.name(), newConstant);
		onChange.run();
	}

	@Override
	public void undo() {
		store.replace(newConstant, oldConstant);
		model.updateGuardsAndWeightsWithNewConstant(newConstant.name(), oldConstant);
		onChange.run();

	}

}
