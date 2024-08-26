package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;

import java.util.Map;

public class ChangeAllNamesVisibilityCommand implements Command {
    private final Map<PetriNetObject, Boolean> places;
    private final Map<PetriNetObject, Boolean> transitions;
    private final boolean isVisible;
	private final PetriNetTab tabContent;

	public ChangeAllNamesVisibilityCommand(PetriNetTab tabContent, Map<PetriNetObject, Boolean> places, Map<PetriNetObject, Boolean> transitions, boolean isVisible) {
		this.tabContent = tabContent;
		this.places = places;
		this.transitions= transitions;
		this.isVisible = isVisible;
	}

	@Override
	public void redo() {
	    if (places != null) {
            for (PetriNetObject place : places.keySet()) {
                if (place instanceof TimedPlaceComponent) {
                    TimedPlaceComponent component = (TimedPlaceComponent) place;
                    component.setAttributesVisible(isVisible);
                    component.update(true);
                    tabContent.repaint();
                }
            }
        }
        if (transitions != null) {
            for (PetriNetObject transition : transitions.keySet()) {
                if (transition instanceof TimedTransitionComponent) {
                    TimedTransitionComponent component = (TimedTransitionComponent) transition;
                    component.setAttributesVisible(isVisible);
                    component.update(true);
                    tabContent.repaint();
                }
            }
        }
	}

	@Override
	public void undo() {
        if (places != null) {
            for (PetriNetObject place : places.keySet()) {
                if (place instanceof TimedPlaceComponent) {
                    TimedPlaceComponent component = (TimedPlaceComponent) place;
                    component.setAttributesVisible(places.get(component));
                    component.update(true);
                    tabContent.repaint();
                }
            }
        }
        if (transitions != null) {
            for (PetriNetObject transition : transitions.keySet()) {
                if (transition instanceof TimedTransitionComponent) {
                    TimedTransitionComponent component = (TimedTransitionComponent) transition;
                    component.setAttributesVisible(transitions.get(component));
                    component.update(true);
                    tabContent.repaint();
                }
            }
        }
	}
}
