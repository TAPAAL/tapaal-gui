package net.tapaal.gui.petrinet.undo;

import java.util.List;

import net.tapaal.gui.petrinet.editor.ConstantsPane;
import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class SortRealConstantsCommand implements Command {

	final TimedArcPetriNetNetwork network;
	final ConstantsPane constantsPane;
	List<RealConstant> oldOrder;

	public SortRealConstantsCommand(TimedArcPetriNetNetwork network, ConstantsPane constantsPane) {
		this.network = network;
		this.constantsPane = constantsPane;
	}

	@Override
	public void undo() {
		network.undoSortRealConstants(oldOrder);
		constantsPane.showConstants();
	}

	@Override
	public void redo() {
		oldOrder = network.sortRealConstants();
		constantsPane.showConstants();
	}
}
