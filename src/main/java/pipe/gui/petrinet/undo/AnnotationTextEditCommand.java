/*
 * AnnotationTextEdit.java
 */

package pipe.gui.petrinet.undo;

import pipe.gui.petrinet.graphicElements.AnnotationNote;
import net.tapaal.gui.petrinet.undo.Command;

/**
 * 
 * @author corveau
 */
public final class AnnotationTextEditCommand implements Command {

	final AnnotationNote annotationNote;
	final String oldText;
	final String newText;

	/** Creates a new instance of placeRateEdit */
	public AnnotationTextEditCommand(AnnotationNote _annotationNote, String _oldText,
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
