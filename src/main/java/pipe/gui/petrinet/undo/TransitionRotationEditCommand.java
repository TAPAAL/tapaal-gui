/*
 * transitionPriorityEdit.java
 */
package pipe.gui.petrinet.undo;

import pipe.gui.petrinet.graphicElements.Transition;
import net.tapaal.gui.petrinet.undo.Command;

/**
 * 
 * @author corveau
 */
public class TransitionRotationEditCommand implements Command {

	final Transition transition;
	final Integer angle;

	/** Creates a new instance of placePriorityEdit */
	public TransitionRotationEditCommand(Transition _transition, Integer _angle) {
		transition = _transition;
		angle = _angle;
	}

	/** */
	@Override
	public void undo() {
		transition.rotate(-angle);
	}

	/** */
	@Override
	public void redo() {
		transition.rotate(angle);
	}

}
