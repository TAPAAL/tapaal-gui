package dk.aau.cs.translations;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;

public class Pairing {
	public enum ArcType {
		NORMAL, TARC
	}

	private TAPNArc input;
	private Arc output;
	private String interval;
	private ArcType arcType;

	public Pairing(TAPNArc input, String interval, Arc output, ArcType arcType) {
		this.input = input;
		this.interval = interval;
		this.output = output;
		this.arcType = arcType;
	}

	public TAPNPlace getInput() {
		return (TAPNPlace) input.getSource();
	}

	public TAPNPlace getOutput() {
		return (TAPNPlace) output.getTarget();
	}

	public ArcType getArcType() {
		return arcType;
	}

	public String getInterval() {
		return interval;
	}

	public TAPNArc getInputArc() {
		return input;
	}

	public Arc getOutputArc() {
		return output;
	}
}
