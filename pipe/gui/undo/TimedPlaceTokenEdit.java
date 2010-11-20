package pipe.gui.undo;

import java.math.BigDecimal;
import java.util.ArrayList;

import dk.aau.cs.gui.undo.Command;

import pipe.dataLayer.TimedPlaceComponent;


public class TimedPlaceTokenEdit extends Command {
	private ArrayList<BigDecimal> oldValue;
	private ArrayList<BigDecimal> newValue;
	private TimedPlaceComponent place;
	
	public TimedPlaceTokenEdit(	TimedPlaceComponent place, 
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
