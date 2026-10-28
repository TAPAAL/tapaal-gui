package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;

public class RemoveConstantEditCommand implements Command {

	private final Constant constant;
	private final ConstantStore store;
	private final Runnable onChange;

	public RemoveConstantEditCommand(Constant constant, ConstantStore store) {
		this(constant, store, () -> {});
	}

	public RemoveConstantEditCommand(Constant constant, ConstantStore store, Runnable onChange) {
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
