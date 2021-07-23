package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.TabContent;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

import java.util.Map;

public class ChangeAllNamesVisibilityCommand extends Command {
    private final Map<PetriNetObject, Boolean> places;
    private final Map<PetriNetObject, Boolean> transitions;
    private final boolean isVisible;
	private final TabContent tabContent;

	public ChangeAllNamesVisibilityCommand(TabContent tabContent, Map<PetriNetObject, Boolean> places, Map<PetriNetObject, Boolean> transitions, boolean isVisible) {
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
