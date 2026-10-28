package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;

public class AddConstantEditCommand implements Command {
	private final Constant constant;
	private final ConstantStore store;
	private final Runnable onChange;

	public AddConstantEditCommand(Constant constant, ConstantStore store) {
		this(constant, store, () -> {});
	}

	public AddConstantEditCommand(Constant constant, ConstantStore store, Runnable onChange) {
		this.constant = constant;
		this.store = store;
		this.onChange = onChange == null ? () -> {} : onChange;
	}

	@Override
	public void redo() {
		store.add(constant);
		onChange.run();
	}

	@Override
	public void undo() {
		store.remove(constant);
		onChange.run();
	}
}
