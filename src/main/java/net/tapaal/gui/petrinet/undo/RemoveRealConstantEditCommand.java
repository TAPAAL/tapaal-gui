package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.ConstantStore;

public class RemoveRealConstantEditCommand implements Command {

	private final RealConstant constant;
	private final ConstantStore store;
	private final Runnable onChange;

	public RemoveRealConstantEditCommand(RealConstant constant, ConstantStore store) {
		this(constant, store, () -> {});
	}

	public RemoveRealConstantEditCommand(RealConstant constant, ConstantStore store, Runnable onChange) {
		this.constant = constant;
		this.store = store;
		this.onChange = onChange == null ? () -> {} : onChange;
	}

	@Override
	public void redo() {
		store.remove(constant);
		onChange.run();
	}

	@Override
	public void undo() {
		store.add(constant);
		onChange.run();
	}
}
