package pipe.dataLayer;

import pipe.gui.Zoomer;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class Template {
	private final TimedArcPetriNet net;
	private final DataLayer guiModel;
	private final Zoomer zoomer;
	private boolean hasPositionalInfo = false;
	
	public Template(TimedArcPetriNet net, DataLayer guiModel, Zoomer zoomer) {
		this.net = net;
		this.guiModel = guiModel;
		this.zoomer = zoomer;
	}

	@Override
	public String toString() {
		return net.toString();
	}

	public DataLayer guiModel() {
		return guiModel;
	}

	public TimedArcPetriNet model() {
		return net;
	}
	
	public boolean isActive() {
		return net.isActive();
	}
	
	public void setActive(boolean isActive) {
		net.setActive(isActive);
	}

	public Template copy() {
		TimedArcPetriNet tapn = net.copy();
		tapn.setName(tapn.name() + "Copy");
		return new Template(tapn, guiModel.copy(tapn), new Zoomer(zoomer.getPercent()));
	}

	public Zoomer zoomer() {
		return zoomer;
	}
	
	public boolean getHasPositionalInfo() {
		return hasPositionalInfo;
	}
	
	public void setHasPositionalInfo(boolean positionalInfo) {
		hasPositionalInfo = positionalInfo;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Template){
			Template other = (Template)obj;
			if(this.net.equals(other.net)){
				return true;
			}
		}
		return false;
	}
}
