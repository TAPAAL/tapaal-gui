package pipe.gui.undo;

import java.util.ArrayList;

import pipe.dataLayer.TimedPlace;


public class TimedPlaceTokenEdit extends UndoableEdit {
	private ArrayList<Float> oldValue;
	private ArrayList<Float> newValue;
	private TimedPlace place;
	
	public TimedPlaceTokenEdit(	TimedPlace place, 
								ArrayList<Float> oldValue, 
								ArrayList<Float> newValue){
		this.place = place;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public void redo() {
		place.setAgeOfTokens(newValue);

	}

	@Override
	public void undo() {
		place.setAgeOfTokens(oldValue);

	}

}
