package pipe.gui.graphicElements.tapn;

import java.awt.Color;
import java.awt.Polygon;
import java.util.Hashtable;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.handler.TransportArcHandler;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.undo.TransportArcGroupEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.ConstantWeight;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.Weight;

public class TimedTransportArcComponent extends TimedInputArcComponent {
	private static final long serialVersionUID = 3728885532894319528L;
	private int group;
	private boolean isInPreSet;
	private TimedTransportArcComponent connectedTo = null;
	private TransportArc underlyingTransportArc;

	public TimedTransportArcComponent(PlaceTransitionObject newSource, int groupNr, boolean isInPreSet) {
		super(new TimedOutputArcComponent(newSource));
		this.isInPreSet = isInPreSet;

		setGroup(groupNr);
		// hack to reprint the label of the arc
		updateLabel(true);
		isPrototype = true;

		//XXX: se note in funcation
		addMouseHandler();
	}

	public TimedTransportArcComponent(TimedInputArcComponent timedArc, int group, boolean isInPreSet) {
		super(timedArc, "");
		this.isInPreSet = isInPreSet;

		this.setGroup(group);
		// hack to reprint the label of the arc
		updateLabel(true);

		//XXX: se note in funcation
		addMouseHandler();
	}

	private void addMouseHandler() {
		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		mouseHandler = new TransportArcHandler(this);
	}

	public void setUnderlyingArc(TransportArc arc) {
		underlyingTransportArc = arc; // must explicitly set underlying arc
											// on connected to
		updateLabel(true);
	}

	public TransportArc underlyingTransportArc() {
		return underlyingTransportArc;
	}

	@Override
	protected void setHead() {
		setHead(new Polygon(new int[] { 0, 5, 0, -5 }, new int[] { 0, -11, -18, -11 }, 4), true);;
	}

	public void setColor() {
		getGraphics().setColor(Color.RED);
		repaint();
	}

	public Command setGroupNr(int groupNr) {
		int oldGroup = this.getGroup();
		setGroup(groupNr);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

		return new TransportArcGroupEdit(this, oldGroup, this.getGroup());
	}

	public int getGroupNr() {
		return getGroup();
	}

	@Override
	public void updateLabel(boolean displayConstantNames) {
		if (isInPreSet && underlyingTransportArc != null) {
			if (CreateGui.getApp().showZeroToInfinityIntervals()){
				getNameLabel().setText(underlyingTransportArc.interval().toString(
						displayConstantNames)
						+ " : " + getGroup());
			}
			else {
				if (underlyingTransportArc.interval().toString(
						displayConstantNames).equals("[0,inf)")) {

					getNameLabel().setText(" : " + getGroup());

				}
				else {
					getNameLabel().setText(underlyingTransportArc.interval().toString(
							displayConstantNames)
							+ " : " + getGroup());
				}				
			}
			
			// Handle constant highlighting
			boolean focusedConstant = false;
			if(underlyingTransportArc.interval().lowerBound() instanceof ConstantBound){
				if(((ConstantBound) underlyingTransportArc.interval().lowerBound()).constant().hasFocus()){
					focusedConstant = true;
				}
			}
			if(underlyingTransportArc.interval().upperBound() instanceof ConstantBound){
				if(((ConstantBound) underlyingTransportArc.interval().upperBound()).constant().hasFocus()){
					focusedConstant = true;
				}
			}
			if(getWeight() instanceof ConstantWeight){
				if(((ConstantWeight) getWeight()).constant().hasFocus()){
					focusedConstant = true;
				}
			}
			if(focusedConstant){
				getNameLabel().setForeground(Pipe.SELECTION_TEXT_COLOUR);
			}else{
				getNameLabel().setForeground(Pipe.ELEMENT_TEXT_COLOUR);
			}
			
		} else if (!isInPreSet) {
			getNameLabel().setText(" : " + getGroup());
		} else {
			getNameLabel().setText("");
		}
		
		if(underlyingTransportArc != null){
			getNameLabel().setText(getWeight().toString(displayConstantNames)+" "+getNameLabel().getText());
		}
		
		this.setLabelPosition();
	}

	public boolean isInPreSet() {
		return isInPreSet;
	}

	public TimedTransportArcComponent getConnectedTo() {
		return connectedTo;
	}

	public void setConnectedTo(TimedTransportArcComponent connectedTo) {
		this.connectedTo = connectedTo;
	}


	public void setGroup(int group) {
		this.group = group;
	}

	public int getGroup() {
		return group;
	}

	@Override
	public TimeInterval getGuard() {
		return underlyingTransportArc.interval();
	}
	
	@Override
	public boolean isUrgentTransition(){
		return underlyingTransportArc.transition().isUrgent();
	}

	@Override
	public Command setGuardAndWeight(TimeInterval guard, Weight weight) {

		TimeInterval oldTimeInterval = underlyingTransportArc.interval();
		underlyingTransportArc.setTimeInterval(guard);
		Weight oldWeight = getWeight();
		setWeight(weight);
		connectedTo.setWeight(weight);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		connectedTo.updateLabel(true);
		
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval,
				underlyingTransportArc.interval(), oldWeight, weight);
	}
	
	public TimedTransportArcComponent copy(TimedArcPetriNet tapn, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedTransportArcComponent arc = new TimedTransportArcComponent(this, group, isInPreSet);
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		
		arc.setUnderlyingArc(tapn.getTransportArcFromPlaceTransitionAndPlace(tapn.getPlaceByName(underlyingTransportArc.source().name()), 
																			 tapn.getTransitionByName(underlyingTransportArc.transition().name()), 
																			 tapn.getPlaceByName(underlyingTransportArc.destination().name())));

		
		return arc;
	}
	
	@Override
	public void setWeight(Weight weight){
		underlyingTransportArc.setWeight(weight);
		connectedTo.underlyingTransportArc.setWeight(weight);
	}
	
	@Override
	public Weight getWeight(){
		return underlyingTransportArc.getWeight();
	}

}
