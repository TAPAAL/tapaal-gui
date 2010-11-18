package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.TransportArc;

public class TransportArcGroupEdit extends Command {
	private TransportArc myArc;
	int oldGroup;
	int newGroup;
	
	public TransportArcGroupEdit(TransportArc arc, int oldGroup, int newGroup) {
		this.myArc = arc;
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
