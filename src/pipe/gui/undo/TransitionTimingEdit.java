/*
 * TransitionTimingEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author corveau
 */
public class TransitionTimingEdit extends Command {

	Transition transition;

	/** Creates a new instance of placeRateEdit */
	public TransitionTimingEdit(Transition _transition) {
		transition = _transition;
	}

	/** */
	@Override
	public void undo() {
		transition.setTimed(!transition.isTimed());
	}

	/** */
	@Override
	public void redo() {
		transition.setTimed(!transition.isTimed());
	}

}
