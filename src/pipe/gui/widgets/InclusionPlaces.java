package pipe.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.model.tapn.TimedPlace;

public class InclusionPlaces {
	public enum InclusionPlacesOption { AllPlaces, UserSpecified };
	
	private InclusionPlacesOption inclusionOption;
	private List<TimedPlace> inclusionPlaces;
	
	public InclusionPlaces() {
		this(InclusionPlacesOption.AllPlaces, new ArrayList<TimedPlace>());
	}
	
	public InclusionPlaces(InclusionPlacesOption inclusionOption, List<TimedPlace> inclusionPlaces) {
		this.inclusionOption = inclusionOption;
		this.inclusionPlaces = inclusionPlaces;
	}
	
	public List<TimedPlace> inclusionPlaces() {
		return inclusionPlaces;
	}
	
	public InclusionPlacesOption inclusionOption() {
		return inclusionOption;
	}

	public void removePlace(TimedPlace place) {
		inclusionPlaces.remove(place);
	}
	
	/**
	 * Perform a deep copy of the object and return it
	 * 
	 * @return
	 */
	public InclusionPlaces copy() {
		InclusionPlaces copy = new InclusionPlaces();
		copy.inclusionOption = (this.inclusionOption == InclusionPlacesOption.AllPlaces) ? InclusionPlacesOption.AllPlaces : InclusionPlacesOption.UserSpecified;
		for (TimedPlace place : this.inclusionPlaces) {
			copy.inclusionPlaces.add(place.copy());
		}
		
		return copy;
	}
}
