package pipe.dataLayer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Collection;
import java.util.HashMap;

import dk.aau.cs.petrinet.Arc;

import pipe.gui.CreateGui;
import pipe.gui.GuiFrame;
import pipe.gui.GuiView;
import pipe.gui.Pipe;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.undo.TransportArcGroupEdit;
import pipe.gui.undo.UndoableEdit;

public class TransportArc extends TimedArc {
	private int group;
	private boolean isInPreSet; 
	TransportArc connectedTo=null;
//	private TransportArc pairArc = null;
//	private boolean hasPairArcYet = false; 

	public TransportArc getconnectedTo() {
		return connectedTo;
	}
	
	public TransportArc(PlaceTransitionObject newSource, int groupNr, boolean isInPreSet) {
		super(new NormalArc(newSource));
		this.isInPreSet = isInPreSet;
		setHead();
		group = groupNr;
		//hack to reprint the label of the arc
		updateWeightLabel();
	}
	public TransportArc(TimedArc timedArc, int group, boolean isInPreSet) {
		super((NormalArc) timedArc, timedArc.getGuard());
		this.isInPreSet = isInPreSet;
		setHead();
		this.group = group;
		//hack to reprint the label of the arc
		updateWeightLabel();
	}
	private void setHead(){
		head = new Polygon(new int[]{0, 5, 0, -5}, new int[]{0, -11, -18, -11}, 4);
	}
	public void setColor(){
		getGraphics().setColor(Color.RED);
		repaint();
	}
	public UndoableEdit setGroupNr(int groupNr){
		int oldGroup = this.group;
		group = groupNr;

		//hacks - I use the weight to display the TimeInterval
		updateWeightLabel();
		repaint();
		
		return new TransportArcGroupEdit(this, oldGroup, this.group);
	}
	public int getGroupNr(){
		return group;
	}
	
	public void updateWeightLabel(){   
		if (isInPreSet){
		weightLabel.setText(timeInterval+" : "+group);
		} else {
			weightLabel.setText(""+group);
		}
		this.setWeightLabelPosition();
	}
	
	public TransportArc copy() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void delete() {
		
		
		// kyrke - do ekstra suff when deleting a transport arc
		
		super.delete();
		
		// xxx - hack to awoid delete loop
		
		TransportArc a = connectedTo;
		connectedTo = null;
		if (a != null && a.connectedTo != null){
			a.delete();
		}
		connectedTo = a;
		
	
	}
	
	@Override
	public void undelete(DataLayer model, GuiView view) {
		super.undelete(model, view);
		
		TransportArc a = connectedTo;
		connectedTo = null;
		if (a.connectedTo != null){
			a.undelete(model,view);
			a.connectedTo = this;
		}
		connectedTo = a;
	}

	public TransportArc paste(double despX, double despY, boolean toAnotherView) {
		// TODO Auto-generated method stub
		return null;
	}
	/*
	public TransportArc getBuddy() {
		TransportArc toReturn = null;
		if (isInPreSet){
			HashMap<TransportArc, TransportArc> transportArcMap = 
				(HashMap<TransportArc, TransportArc>) ((HashMap) CreateGui.getModel().getTransportArcMap() ).
																	get( this.getTarget() );

			toReturn = transportArcMap.get(this);
		}else {
			HashMap<TransportArc, TransportArc> transportArcMap = 
				(HashMap<TransportArc, TransportArc>) ((HashMap) CreateGui.getModel().getTransportArcMap() ).
																	get( this.getSource() );
			for ( TransportArc ta : transportArcMap.keySet() ){
				if (transportArcMap.get(ta) == this){
					toReturn = ta;
				}
			}
		}
		return toReturn;
	}*/
	public boolean isInPreSet() {
		return isInPreSet;
	}
	public TransportArc getConnectedTo() {
		return connectedTo;
	}
	public void setConnectedTo(TransportArc connectedTo) {
		this.connectedTo = connectedTo;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
	
}
