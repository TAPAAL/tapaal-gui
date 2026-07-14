package net.tapaal.gui.petrinet.undo;

import pipe.gui.TAPAALGUI;
import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.ConstantStore;

public class AddRealConstantEditCommand implements Command {
	private final RealConstant constant;
	private final ConstantStore store;

	public AddRealConstantEditCommand(RealConstant constant, ConstantStore store) {
		this.constant = constant;
		this.store = store;
	}

	@Override
	public void redo() {
		store.add(constant);
		TAPAALGUI.getCurrentTab().updateConstantsList();
	}

	@Override
	public void undo() {
		store.remove(constant);
		TAPAALGUI.getCurrentTab().updateConstantsList();
	}
}
