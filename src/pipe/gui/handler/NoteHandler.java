package pipe.gui.handler;

import java.awt.event.MouseEvent;

import pipe.gui.graphicElements.Note;

public class NoteHandler extends PetriNetObjectHandler {

	public NoteHandler(Note note) {
		super(note);
		enablePopup = true;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if ((e.getComponent() == myObject) || !e.getComponent().isEnabled()) {
			super.mousePressed(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if ((e.getComponent() == myObject) || !e.getComponent().isEnabled()) {
			super.mouseDragged(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if ((e.getComponent() == myObject) || !e.getComponent().isEnabled()) {
			super.mouseReleased(e);
		}
	}

}
