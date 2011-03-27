package dk.aau.cs.model.tapn;

import dk.aau.cs.TCTL.TCTLAbstractProperty;

public class TAPNQuery {
	private TCTLAbstractProperty property;
	private int totalTokens = 0;

	public TCTLAbstractProperty getProperty() {
		return property;
	}

	public TAPNQuery(TCTLAbstractProperty inputProperty, int totalTokens) {
		this.property = inputProperty;
		this.totalTokens = totalTokens;
	}

	@Override
	public String toString() {
		return property.toString();
	}

	public int getTotalTokens() {
		return totalTokens;
	}
}
