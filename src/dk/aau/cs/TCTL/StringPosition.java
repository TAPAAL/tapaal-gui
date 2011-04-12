package dk.aau.cs.TCTL;

public class StringPosition {

	private int startIndex;
	private int endIndex;
	private TCTLAbstractProperty object;

	public StringPosition(int startIndex, int endIndex,
			TCTLAbstractProperty object) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.object = object;
	}

	public int getStart() {
		return startIndex;
	}

	public int getEnd() {
		return endIndex;
	}

	public void incrementEnd(int offset) {
		endIndex = Math.max(startIndex, endIndex + offset);
	}

	public StringPosition addOffset(int offset) {
		startIndex += offset;
		endIndex += offset;
		return this;
	}

	public TCTLAbstractProperty getObject() {
		return object;
	}
}
