package pipe.dataLayer.colors;

import java.util.ArrayList;
import java.util.List;

import pipe.dataLayer.Place;
import pipe.dataLayer.TimedPlace;
import pipe.gui.undo.PlaceColorInvariantEdit;
import pipe.gui.undo.TimedPlaceInvariantEdit;
import pipe.gui.undo.UndoableEdit;

public class ColoredTimedPlace extends TimedPlace {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8389233604098400485L;
	private ColorSet colorInvariant;
	private List<ColoredToken> tokens;
	
	public ColoredTimedPlace(double positionXInput, double positionYInput) {
		super(positionXInput, positionYInput);
		
		init();
	}
	
	public ColoredTimedPlace(double positionXInput,  double positionYInput, 
            String idInput, 
            String nameInput, 
            Double nameOffsetXInput, Double nameOffsetYInput, 
            int initialMarkingInput, 
            double markingOffsetXInput,  double markingOffsetYInput,
            int capacityInput, String invariant){
	
		super(positionXInput, positionYInput, idInput, nameInput, 
				nameOffsetXInput, nameOffsetYInput,
				initialMarkingInput, markingOffsetXInput, markingOffsetYInput,
				capacityInput, invariant);
		
		init();
	}

	public ColoredTimedPlace(Place place, String invariant){		
		super(place, invariant);
		
		init();
	}
	
	public ColoredTimedPlace(String idInput, 
            String nameInput, 
            int initialMarkingInput, 
            int capacityInput, String invariant){
		super(idInput, nameInput, initialMarkingInput, capacityInput, invariant);
		
		init();
	}

	private void init() {
		colorInvariant = new ColorSet();
		tokens = new ArrayList<ColoredToken>();
	}
	
	public String getColorInvariantString(){
		return colorInvariant.toString();
	}
	
	
	public boolean satisfiesInvariant(ColoredToken token) {
		return colorInvariant.contains(token.getColor()) && satisfiesInvariant(token.getAge());
	}
	
	public String getStringOfTokens() {
		StringBuilder builder = new StringBuilder("{");
		
		boolean first = true;
		for(ColoredToken token : tokens){
			if(!first){
				builder.append(", ");
			}
			builder.append(token.toString());
			first = false;
		}
		
		builder.append("}");
		return builder.toString();
	}
	
	
	@Override
	protected String getInvariantString() {
		String inv = super.getInvariantString();
		
		if(!colorInvariant.isEmpty()){
			inv += "\n Value: " + colorInvariant.toString();
		}
		
		return inv;
	}

	public String getColorInvariantStringWithoutSetNotation() {
		return colorInvariant.toStringNoSetNotation();
	}

	public UndoableEdit setColorInvariant(ColorSet newColorInvariant) {
		ColorSet old = this.colorInvariant;
		this.colorInvariant = newColorInvariant;

		update();

		return new PlaceColorInvariantEdit(this, old, newColorInvariant);		
	}
	
}
