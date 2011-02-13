package pipe.dataLayer;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.util.Require;

public class Template {
	private TimedArcPetriNet net;
	private DataLayer guiModel;

	public Template(TimedArcPetriNet net, DataLayer guiModel) {
		this.net = net;
		this.guiModel = guiModel;
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

	public void setGuiModel(DataLayer guiModel) {
		Require.that(guiModel != null, "GuiModel cannot be null");
		this.guiModel = guiModel;
	}

	public Template copy() {
		TimedArcPetriNet tapn = net.copy();
		tapn.setName(tapn.getName() + "Copy");
		return new Template(tapn, guiModel.copy(tapn));
	}
}
