package pipe.gui;

import pipe.gui.graphicElements.PetriNetObject;

/**
 * This is the interface that a component must implement so that it can be
 * copied and pasted.
 * 
 * @author Pere Bonet
 */
public interface CopyPasteable {

	/**
	 * copy()
	 * 
	 * @return a copy of the PetriNetObject
	 */
	PetriNetObject copy();

	/**
	 * paste()
	 * 
	 * @param despX
	 * @param despY
	 * @param toAnotherView
	 * @return a copy of the saved PetriNetObject that can be added to a GuiView
	 *         instance
	 */
	PetriNetObject paste(double despX, double despY,
			boolean toAnotherView);

	/**
	 * isCopyPasteable();
	 * 
	 * @return true if this object can be copied and pasted
	 */
	boolean isCopyPasteable();

}
