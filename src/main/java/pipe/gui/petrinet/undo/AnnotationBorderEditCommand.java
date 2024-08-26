/*
 * AnnotationBorderEdit.java
 */

package pipe.gui.petrinet.undo;

import pipe.gui.petrinet.graphicElements.Note;
import net.tapaal.gui.petrinet.undo.Command;

/**
 * 
 * @author corveau
 */
public class AnnotationBorderEditCommand implements Command {

	final Note note;

	/** Creates a new instance of placeRateEdit */
	public AnnotationBorderEditCommand(Note _note) {
		note = _note;
	}

	/** */
	@Override
	public void undo() {
		note.showBorder(!note.isShowingBorder());
	}

	/** */
	@Override
	public void redo() {
		note.showBorder(!note.isShowingBorder());
	}

	@Override
	public String toString() {
		return super.toString() + " " + note.getClass().getSimpleName();
	}

}
