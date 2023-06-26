package dk.aau.cs.model.tapn;

public abstract class TAPNElement {
	private TimedArcPetriNet model;

	public TimedArcPetriNet model() {
		return model;
	}

	public void setModel(TimedArcPetriNet model) {
		this.model = model;
	}

	public abstract void delete();
}
