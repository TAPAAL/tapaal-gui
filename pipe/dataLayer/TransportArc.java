package pipe.dataLayer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import pipe.gui.GuiView;
import pipe.gui.undo.TransportArcGroupEdit;
import pipe.gui.undo.UndoableEdit;

public class TransportArc extends TimedArc {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3728885532894319528L;
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
		setGroup(groupNr);
		//hack to reprint the label of the arc
		updateWeightLabel();
	}
	public TransportArc(TimedArc timedArc, int group, boolean isInPreSet) {
		super(timedArc, timedArc.getGuard());
		this.isInPreSet = isInPreSet;
		setHead();
		this.setGroup(group);
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
		int oldGroup = this.getGroup();
		setGroup(groupNr);

		//hacks - I use the weight to display the TimeInterval
		updateWeightLabel();
		repaint();
		
		return new TransportArcGroupEdit(this, oldGroup, this.getGroup());
	}
	public int getGroupNr(){
		return getGroup();
	}
	
	@Override
	public void updateWeightLabel(){   
		if (isInPreSet){
		weightLabel.setText(timeInterval+" : "+getGroup());
		} else {
			weightLabel.setText(""+getGroup());
		}
		this.setWeightLabelPosition();
	}
	
	@Override
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

	@Override
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
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getGroup() {
		return group;
	}
	
}
