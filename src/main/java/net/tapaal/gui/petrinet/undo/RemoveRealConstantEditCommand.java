package net.tapaal.gui.petrinet.undo;

import pipe.gui.TAPAALGUI;
import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.ConstantStore;

public class RemoveRealConstantEditCommand implements Command {

	private final RealConstant constant;
	private final ConstantStore store;

	public RemoveRealConstantEditCommand(RealConstant constant, ConstantStore store) {
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
