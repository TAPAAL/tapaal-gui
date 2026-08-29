package net.tapaal.gui.petrinet.editor;

import javax.swing.AbstractListModel;

import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class RealConstantsListModel extends AbstractListModel {

	private TimedArcPetriNetNetwork network;

	public RealConstantsListModel(TimedArcPetriNetNetwork network) {
		this.network = network;
	}

	public void setNetwork(TimedArcPetriNetNetwork newNetwork) {
		network = newNetwork;
		updateAll();
	}

	public RealConstant getElementAt(int index) {
		return network.getRealConstant(index);
	}

	public int getSize() {
		return network.realConstants().size();
	}

	public void updateAll() {
		fireContentsChanged(this, 0, Integer.MAX_VALUE);
	}
}
