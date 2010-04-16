package pipe.dataLayer.colors;

import java.util.ArrayList;
import java.util.List;

import pipe.dataLayer.Place;
import pipe.dataLayer.TimedPlace;
import pipe.gui.CreateGui;
import pipe.gui.undo.ColoredPlaceAddTokenEdit;
import pipe.gui.undo.ColoredPlaceRemoveTokenEdit;
import pipe.gui.undo.ColoredPlaceTokensChangedEdit;
import pipe.gui.undo.PlaceColorInvariantEdit;
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
		IntOrConstant val = token.getColor();
		int value = 0;
		if(val.isUsingConstant()){
			value = CreateGui.getModel().getConstantValue(val.getConstantName());
		}else{
			value = val.getIntegerValue();
		}
		
		return colorInvariant.contains(value) && satisfiesInvariant(token.getAge());
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
	
	
	protected String getInvariantString() {
		String inv = super.getInvariantString();
		
		if(!colorInvariant.isEmpty()){
			inv += "\n val \u2208 " + colorInvariant.toString();
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
	
	public List<ColoredToken> getColoredTokens(){
		return tokens;
	}
	
	public UndoableEdit addColoredToken(ColoredToken token){
		tokens.add(token);
		
		return new ColoredPlaceAddTokenEdit(this, token);
	}
	
	public UndoableEdit removeColoredToken(ColoredToken token){
		tokens.remove(token);
		
		return new ColoredPlaceRemoveTokenEdit(this,token);
	}
	
	public UndoableEdit setColoredTokens(List<ColoredToken> newTokens) {
		List<ColoredToken> old = this.tokens;
		this.tokens = newTokens;
		
		return new ColoredPlaceTokensChangedEdit(this,old,newTokens);
	}
	
}
