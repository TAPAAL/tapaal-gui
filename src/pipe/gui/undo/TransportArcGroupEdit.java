package pipe.gui.undo;

import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.gui.undo.Command;

public class TransportArcGroupEdit extends Command {
	private TimedTransportArcComponent myArc;
	int oldGroup;
	int newGroup;

	public TransportArcGroupEdit(TimedTransportArcComponent arc, int oldGroup,
			int newGroup) {
		myArc = arc;
		this.oldGroup = oldGroup;
		this.newGroup = newGroup;
	}

	@Override
	public void redo() {
		myArc.setGroupNr(newGroup);
	}

	@Override
	public void undo() {
		myArc.setGroupNr(oldGroup);
	}
}
