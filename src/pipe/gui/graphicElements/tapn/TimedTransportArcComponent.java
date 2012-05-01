package pipe.gui.graphicElements.tapn;

import java.awt.Color;
import java.awt.Polygon;
import java.util.Hashtable;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.handler.TransportArcHandler;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.undo.TransportArcGroupEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TransportArc;

public class TimedTransportArcComponent extends TimedInputArcComponent {
	private static final long serialVersionUID = 3728885532894319528L;
	private int group;
	private boolean isInPreSet;
	private TimedTransportArcComponent connectedTo = null;
	private TransportArc underlyingTransportArc;

	public TimedTransportArcComponent(PlaceTransitionObject newSource, int groupNr,
			boolean isInPreSet) {
		super(new TimedOutputArcComponent(newSource));
		this.isInPreSet = isInPreSet;
		setHead();
		setGroup(groupNr);
		// hack to reprint the label of the arc
		updateLabel(true);
	}

	public TimedTransportArcComponent(TimedInputArcComponent timedArc, int group,
			boolean isInPreSet) {
		super(timedArc, "");
		this.isInPreSet = isInPreSet;
		setHead();
		this.setGroup(group);
		// hack to reprint the label of the arc
		updateLabel(true);
	}

	public void setUnderlyingArc(TransportArc arc) {
		underlyingTransportArc = arc; // must explicitly set underlying arc
											// on connected to
		updateLabel(true);
	}

	public TransportArc underlyingTransportArc() {
		return underlyingTransportArc;
	}

	private void setHead() {
		head = new Polygon(new int[] { 0, 5, 0, -5 }, new int[] { 0, -11, -18,
				-11 }, 4);
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
			if (CreateGui.showZeroToInfinityIntervals()){
				label.setText(underlyingTransportArc.interval().toString(
						displayConstantNames)
						+ " : " + getGroup());
			}
			else {
				if (underlyingTransportArc.interval().toString(
						displayConstantNames).equals("[0,inf)")) {
					label.setText(" : " + String.valueOf(getGroup()));
				}
				else {
					label.setText(underlyingTransportArc.interval().toString(
							displayConstantNames)
							+ " : " + getGroup());
				}				
			}
		} else if (!isInPreSet) {
			label.setText(" : " + String.valueOf(getGroup()));
		} else {
			label.setText("");
		}
		
		if(underlyingTransportArc != null && getWeight() > 1){
					label.setText(getWeight()+"x "+label.getText());
		}
		
		this.setLabelPosition();
	}

	@Override
	public TimedTransportArcComponent copy() {

		return null;
	}

	@Override
	public void delete() {
		if (underlyingTransportArc != null) {
			underlyingTransportArc.delete();
			underlyingTransportArc = null;
			connectedTo.underlyingTransportArc = null;
		}

		// kyrke - do ekstra suff when deleting a transport arc

		super.delete();

		//Avoid delete loop (but we need to save connected to for undo redo)
		TimedTransportArcComponent a = connectedTo;
		connectedTo = null;
		if (a != null && a.connectedTo != null) {
			a.delete();
		}
		connectedTo = a;

	}

	@Override
	public void undelete(DrawingSurfaceImpl view) {
		super.undelete(view);

		//Avoid loop
		TimedTransportArcComponent a = connectedTo;
		connectedTo = null;
		if (a.connectedTo != null) {
			a.undelete(view);
			a.connectedTo = this;
		}
		connectedTo = a;
	}

	@Override
	public TimedTransportArcComponent paste(double despX, double despY,
			boolean toAnotherView) {

		return null;
	}

	/*
	 * public TransportArc getBuddy() { TransportArc toReturn = null; if
	 * (isInPreSet){ HashMap<TransportArc, TransportArc> transportArcMap =
	 * (HashMap<TransportArc, TransportArc>) ((HashMap)
	 * CreateGui.getModel().getTransportArcMap() ). get( this.getTarget() );
	 * 
	 * toReturn = transportArcMap.get(this); }else { HashMap<TransportArc,
	 * TransportArc> transportArcMap = (HashMap<TransportArc, TransportArc>)
	 * ((HashMap) CreateGui.getModel().getTransportArcMap() ). get(
	 * this.getSource() ); for ( TransportArc ta : transportArcMap.keySet() ){
	 * if (transportArcMap.get(ta) == this){ toReturn = ta; } } } return
	 * toReturn; }
	 */
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
	public String getGuardAsString() {
		return underlyingTransportArc.interval().toString();
	}

	@Override
	public TimeInterval getGuard() {
		return underlyingTransportArc.interval();
	}

	@Override
	public Command setGuardAndWeight(TimeInterval guard, int weight) {

		TimeInterval oldTimeInterval = underlyingTransportArc.interval();
		underlyingTransportArc.setTimeInterval(guard);
		int oldWeight = getWeight();
		setWeight(weight);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval,
				underlyingTransportArc.interval(), oldWeight, weight);
	}
	
	public TimedTransportArcComponent copy(TimedArcPetriNet tapn, DataLayer guiModel, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedTransportArcComponent arc = new TimedTransportArcComponent(this, group, isInPreSet);
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		
		arc.setUnderlyingArc(tapn.getTransportArcFromPlaceTransitionAndPlace(tapn.getPlaceByName(underlyingTransportArc.source().name()), 
																			 tapn.getTransitionByName(underlyingTransportArc.transition().name()), 
																			 tapn.getPlaceByName(underlyingTransportArc.destination().name())));
		
		arc.getSource().addConnectFrom(arc);
		arc.getTarget().addConnectTo(arc);
		
		TransportArcHandler transportArcHandler = new TransportArcHandler((DrawingSurfaceImpl)getParent(), arc);
		arc.addMouseListener(transportArcHandler);
		//arc.addMouseWheelListener(transportArcHandler);
		arc.addMouseMotionListener(transportArcHandler);
		
		arc.setGuiModel(guiModel);
		
		return arc;
	}
	
	@Override
	public void setWeight(int weight){
		underlyingTransportArc.setWeight(weight);
	}
	
	@Override
	public int getWeight(){
		return underlyingTransportArc.getWeight();
	}

}
