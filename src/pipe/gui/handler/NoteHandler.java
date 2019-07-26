package pipe.gui.handler;

import java.awt.event.MouseEvent;

import pipe.gui.graphicElements.Note;

public class NoteHandler extends PetriNetObjectHandler {

	public NoteHandler(Note note) {
		super(note);
		enablePopup = true;
	}



}
