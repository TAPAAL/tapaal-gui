package pipe.gui.petrinet.graphicElements.tapn;

import java.awt.geom.Ellipse2D;
import java.util.Hashtable;

import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import pipe.gui.Constants;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.undo.ArcTimeIntervalEditCommand;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.ConstantWeight;
import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.Weight;

public class TimedInhibitorArcComponent extends TimedInputArcComponent {

	private TimedInhibitorArc inhibitorArc;

	public TimedInhibitorArcComponent(TimedOutputArcComponent arc) {
		super(arc);
	}

	@Deprecated
	public TimedInhibitorArcComponent(TimedOutputArcComponent arc, String guard) {
		this(arc);
	}

	public TimedInhibitorArcComponent(PlaceTransitionObject source) {
		super(source);
	}

    public TimedInhibitorArcComponent(TimedPlaceComponent source, TimedTransitionComponent target, TimedInhibitorArc modelArc) {
        super(source);
        setTarget(target);
        setUnderlyingArc(modelArc);
        updateLabel(true);
        sealArc();
    }

	public void setUnderlyingArc(TimedInhibitorArc arc) {
		inhibitorArc = arc;
		updateLabel(true);
	}

	public TimedInhibitorArc underlyingTimedInhibitorArc() {
		return inhibitorArc;
	}

	@Override
	protected void setHead() {
		setHead(new Ellipse2D.Double(-4, -8, 8, 8), false);
	}

	@Override
	public void updateLabel(boolean displayConstantNames) {
		getNameLabel().setText("");
        if (getWeight().value() > 1 || displayConstantNames) {
            getNameLabel().setText(getWeight().toString(displayConstantNames));
        }

        boolean focusedConstant = false;
        if (getWeight() instanceof ConstantWeight) {
            if (((ConstantWeight) getWeight()).constant().hasFocus()) {
                focusedConstant = true;
            }
            pnName.setVisible(((ConstantWeight) getWeight()).constant().getVisible());
        }
        if(focusedConstant){
            getNameLabel().setForeground(Constants.SELECTION_TEXT_COLOUR);
        }else{
            getNameLabel().setForeground(Constants.ELEMENT_TEXT_COLOUR);
        }


		this.setLabelPosition();
	}

	@Override
	public Command setGuardAndWeight(TimeInterval guard, Weight weight) {

		TimeInterval oldTimeInterval = inhibitorArc.interval();
		inhibitorArc.setTimeInterval(guard);
		Weight oldWeight = getWeight();
		setWeight(weight);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

		return new ArcTimeIntervalEditCommand(this, oldTimeInterval, inhibitorArc.interval(), oldWeight, weight);
	}

	@Override
	public TimeInterval getGuard() {
		return inhibitorArc.interval();
	}

	public TimedInhibitorArcComponent copy(TimedArcPetriNet tapn, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedInhibitorArcComponent arc = new TimedInhibitorArcComponent(this);
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		
		arc.setUnderlyingArc(tapn.getInhibitorArcFromPlaceAndTransition(tapn.getPlaceByName(inhibitorArc.source().name()), tapn.getTransitionByName(inhibitorArc.destination().name())));
		
		return arc;
	}

	public ArcExpression getExpression(){
        if(inhibitorArc == null) return null;	// Hack to support inherited constructor (updateLabel called before inhibitorArc set when opening a saved file)
        return inhibitorArc.getArcExpression();
    }

	@Override
	public void setWeight(Weight weight){
		inhibitorArc.setWeight(weight);
	}
	
	@Override
	public Weight getWeight(){
		if(inhibitorArc == null) return new IntWeight(1);		// Hack to support inherited constructor (updateLabel called before inhibitorArc set when opening a saved file)
		return inhibitorArc.getWeight();
	}

    @Override
    public void setExpression(ArcExpression expr){
        inhibitorArc.setExpression(expr);
    }
}
