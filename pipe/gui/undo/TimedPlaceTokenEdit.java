package pipe.gui.undo;

import java.math.BigDecimal;
import java.util.ArrayList;

import pipe.dataLayer.TimedPlace;


public class TimedPlaceTokenEdit extends UndoableEdit {
	private ArrayList<BigDecimal> oldValue;
	private ArrayList<BigDecimal> newValue;
	private TimedPlace place;
	
	public TimedPlaceTokenEdit(	TimedPlace place, 
								ArrayList<BigDecimal> oldAgeOfTokens, 
								ArrayList<BigDecimal> arrayList){
		this.place = place;
		this.oldValue = oldAgeOfTokens;
		this.newValue = arrayList;
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
