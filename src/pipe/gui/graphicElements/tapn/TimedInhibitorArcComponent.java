package pipe.gui.graphicElements.tapn;

import java.awt.geom.Ellipse2D;
import java.util.Hashtable;

import pipe.gui.Pipe;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.handler.TimedArcHandler;
import pipe.gui.undo.ArcTimeIntervalEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.ConstantWeight;
import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.Weight;

public class TimedInhibitorArcComponent extends TimedInputArcComponent {
	private static final long serialVersionUID = 5492180277264669192L;
	private TimedInhibitorArc inhibitorArc;

	public TimedInhibitorArcComponent(TimedOutputArcComponent arc) {
		super(arc);
		//XXX: se note in funcation
		addMouseHandler();
		setHead();
	}

	public TimedInhibitorArcComponent(TimedOutputArcComponent arc, String guard) {
		super(arc, guard);
		//XXX: se note in funcation
		addMouseHandler();
		setHead();

	}

	public TimedInhibitorArcComponent(PlaceTransitionObject source) {
		super(source);

		//XXX: se note in funcation
		addMouseHandler();
		setHead();
	}

	private void addMouseHandler() {
		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		mouseHandler = new TimedArcHandler(this);
	}

	public void setUnderlyingArc(TimedInhibitorArc arc) {
		inhibitorArc = arc;
		updateLabel(true);
	}

	public TimedInhibitorArc underlyingTimedInhibitorArc() {
		return inhibitorArc;
	}

	protected void setHead() {
		head = new Ellipse2D.Double(-4, -8, 8, 8);
		fillHead = false;
	}

	@Override
	public void delete() {
		if (inhibitorArc != null)
			inhibitorArc.delete();
		super.delete();
	}

	@Override
	public void updateLabel(boolean displayConstantNames) {
		label.setText("");
		if(getWeight().value() > 1 || displayConstantNames){
			label.setText(getWeight().toString(displayConstantNames));
		}
		
		boolean focusedConstant = false;
		if(getWeight() instanceof ConstantWeight){
			if(((ConstantWeight) getWeight()).constant().hasFocus()){
				focusedConstant = true;
			}
		}
		if(focusedConstant){
			label.setForeground(Pipe.SELECTION_TEXT_COLOUR);
		}else{
			label.setForeground(Pipe.ELEMENT_TEXT_COLOUR);
		}
		
		this.setLabelPosition();
	}

	@Override
        public String getGuardAsString() {
		return getGuardAsString(true);
	}

        public String getGuardAsString(boolean showZeroToInfinityIntervals) {
                if (!showZeroToInfinityIntervals) {
                        return "";  // inhibitor arcs do not carry any intervals - [0,inf) by default
                } 
                return inhibitorArc.interval().toString();
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

		return new ArcTimeIntervalEdit(this, oldTimeInterval, inhibitorArc.interval(), oldWeight, weight);
	}

	@Override
	public TimeInterval getGuard() {
		return inhibitorArc.interval();
	}

	public TimedInhibitorArcComponent copy(TimedArcPetriNet tapn, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedInhibitorArcComponent arc = new TimedInhibitorArcComponent(this);
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		
		arc.getSource().addConnectFrom(arc);
		arc.getTarget().addConnectTo(arc);
		
		arc.setUnderlyingArc(tapn.getInhibitorArcFromPlaceAndTransition(tapn.getPlaceByName(inhibitorArc.source().name()), tapn.getTransitionByName(inhibitorArc.destination().name())));
		
		return arc;
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
}
