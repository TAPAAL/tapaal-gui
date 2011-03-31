package dk.aau.cs.model.tapn;

import dk.aau.cs.TCTL.TCTLAbstractProperty;

public class TAPNQuery {
	private TCTLAbstractProperty property;
	private int extraTokens = 0;

	public TCTLAbstractProperty getProperty() {
		return property;
	}

	public TAPNQuery(TCTLAbstractProperty inputProperty, int extraTokens) {
		this.property = inputProperty;
		this.extraTokens = extraTokens;
	}

	public int getExtraTokens() {
		return extraTokens;
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
