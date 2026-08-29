package dk.aau.cs.model.tapn;

import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.NumberOfExpression;
import dk.aau.cs.model.CPN.Expressions.UserOperatorExpression;
import dk.aau.cs.util.Require;

import java.util.Vector;

public class TimedOutputArc extends TAPNElement {
	private Weight weight;
	private final TimedTransition source;
	private TimedPlace destination;
    private ArcExpression expression;
	
	public TimedOutputArc(TimedTransition source, TimedPlace destination){
		this(source, destination, new IntWeight(1), null);
	}
    public TimedOutputArc(TimedTransition source, TimedPlace destination, ArcExpression expr){
        this(source, destination, new IntWeight(expr.weight()), expr);
    }

	public TimedOutputArc(TimedTransition source, TimedPlace destination, Weight weight, ArcExpression expression) {
		Require.that(source != null, "An arc must have a non-null source transition");
		Require.that(destination != null, "An arc must have a non-null destination place");
		Require.that(!source.isShared() || !destination.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		this.source = source;
		this.destination = destination;
		this.weight = weight;
        if(expression == null){
            createNewArcExpression();
        } else{
            this.expression = expression;
        }
	}
	
	public Weight getWeight(){
		return weight;
	}
        
        public Weight getWeightValue(){
                return new IntWeight(weight.value());
	}
	
	public void setWeight(Weight weight){
		this.weight = weight;
	}

	public TimedTransition source() {
		return source;
	}

	public TimedPlace destination() {
		return destination;
	}

    public void createNewArcExpression() {
        UserOperatorExpression userOperatorExpression = new UserOperatorExpression(destination().getColorType().getFirstColor());
        Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
        vecColorExpr.add(userOperatorExpression);
        NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
        setExpression(numbExpr);
    }

	@Override
	public void delete() {
		model().remove(this);
	}

	public TimedOutputArc copy(TimedArcPetriNet tapn) {
		return new TimedOutputArc(tapn.getTransitionByName(source.name()), tapn.getPlaceByName(destination.name()), weight, expression.copy());
	}

	public void setDestination(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		destination = place;		
	}
	
	@Override
	public String toString() {
		return "from " + source.name() + " to " + destination.name();
	}

    public void setExpression(ArcExpression expression) {this.expression = expression;}

    public ArcExpression getExpression(){return this.expression;}
}
