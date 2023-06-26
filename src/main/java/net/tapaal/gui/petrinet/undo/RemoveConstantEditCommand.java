package net.tapaal.gui.petrinet.undo;

import pipe.gui.TAPAALGUI;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;

public class RemoveConstantEditCommand extends Command {

	private final Constant constant;
	private final ConstantStore store;

	public RemoveConstantEditCommand(Constant constant, ConstantStore store) {
		this.constant = constant;
		this.store = store;
	}

	@Override
	public void redo() {
		store.remove(constant);
		TAPAALGUI.getCurrentTab().updateConstantsList();
	}

	@Override
	public void undo() {
		store.add(constant);
		TAPAALGUI.getCurrentTab().updateConstantsList();
	}

}
