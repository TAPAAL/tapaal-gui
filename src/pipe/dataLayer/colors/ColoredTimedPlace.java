package pipe.dataLayer.colors;

import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import pipe.dataLayer.Place;
import pipe.dataLayer.TimedPlace;
import pipe.gui.CreateGui;
import pipe.gui.undo.ColoredPlaceAddTokenEdit;
import pipe.gui.undo.ColoredPlaceRemoveTokenEdit;
import pipe.gui.undo.ColoredPlaceTokensChangedEdit;
import pipe.gui.undo.ColoredTimedPlaceTimeInvariantEdit;
import pipe.gui.undo.PlaceColorInvariantEdit;
import pipe.gui.undo.UndoableEdit;

public class ColoredTimedPlace extends TimedPlace {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8389233604098400485L;
	private ColorSet colorInvariant;
	private List<ColoredToken> tokens;
	private ColoredTimeInvariant timeInvariant;

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
		timeInvariant = new ColoredTimeInvariant();
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

		return colorInvariant.contains(value) && timeInvariant.contains(token);
	}

	@Override
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
	public String getInvariantString() {
		
		String inv = timeInvariant.goesToInfinity() ? "" : "\nage " + timeInvariant.toString();
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
		
		update();

		return new ColoredPlaceAddTokenEdit(this, token);
	}

	public UndoableEdit removeColoredToken(ColoredToken token){
		tokens.remove(token);
		
		update();

		return new ColoredPlaceRemoveTokenEdit(this,token);
	}

	public UndoableEdit setColoredTokens(List<ColoredToken> newTokens) {
		List<ColoredToken> old = this.tokens;
		this.tokens = newTokens;
		
		update();

		return new ColoredPlaceTokensChangedEdit(this,old,newTokens);
	}


	@Override
	public int getCurrentMarking() {
		return tokens.size();
	}


	@Override
	protected void paintTokens(Graphics g) {
		int numberOfTokens = getCurrentMarking();

		if(numberOfTokens > 0){
			String toDraw = String.format("#%1$d", numberOfTokens);

			Insets insets = getInsets();
			int x = insets.left + 2;
			int y = insets.top + 20;

			g.drawString(toDraw, x, y);
		}
	}

	public ColorSet getColorInvariant() {
		return colorInvariant;
	}

	public ColoredTimeInvariant getTimeInvariant() {
		return timeInvariant;
	}

	public UndoableEdit setTimeInvariant(ColoredTimeInvariant newTimeInvariant) {
		ColoredTimeInvariant old = this.timeInvariant;
		this.timeInvariant = newTimeInvariant;
		
		update();
		
		return new ColoredTimedPlaceTimeInvariantEdit(this, old, newTimeInvariant);
	}

	public void displayValues(boolean showValues) {
		timeInvariant.displayValues(showValues);
		colorInvariant.displayValues(showValues);
		
		for(ColoredToken token : tokens){
			token.displayValues(showValues);
		}
		update();		
	}

	public void updateConstantName(String oldName, String newName) {
		timeInvariant.updateConstantName(oldName, newName);
		colorInvariant.updateConstantName(oldName, newName);
		
		for(ColoredToken token : tokens){
			token.updateConstantName(oldName, newName);
		}
		
		update();		
	}

}
