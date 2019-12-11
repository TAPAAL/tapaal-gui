/*
 * AnnotationTextEdit.java
 */

package pipe.gui.undo;

import pipe.gui.graphicElements.AnnotationNote;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author corveau
 */
public final class AnnotationTextEdit extends Command {

	AnnotationNote annotationNote;
	String oldText;
	String newText;

	/** Creates a new instance of placeRateEdit */
	public AnnotationTextEdit(AnnotationNote _annotationNote, String _oldText,
			String _newText) {
		annotationNote = _annotationNote;
		oldText = _oldText;
		newText = _newText;
	}

	/** */
	@Override
	public void undo() {
		annotationNote.setText(oldText);
	}

	/** */
	@Override
	public void redo() {
		annotationNote.setText(newText);
		annotationNote.updateBounds();
		annotationNote.repaint();
	}

	@Override
	public String toString() {
		return super.toString() + " "
				+ annotationNote.getClass().getSimpleName() + "oldText: "
				+ oldText + "newText: " + newText;
	}

}
