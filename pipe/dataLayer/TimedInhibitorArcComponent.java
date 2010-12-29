package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.math.BigDecimal;

import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.undo.ArcTimeIntervalEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedInhibitorArc;

public class TimedInhibitorArcComponent extends TimedInputArcComponent {
	private static final long serialVersionUID = 5492180277264669192L;
	private TimedInhibitorArc inhibitorArc;

	public TimedInhibitorArcComponent(TimedOutputArcComponent arc)
	{
		super(arc);
	}
	
	public TimedInhibitorArcComponent(TimedOutputArcComponent arc, String guard)
	{
		super(arc,guard);
	}
	
	public TimedInhibitorArcComponent(PlaceTransitionObject source) {
		super(source);
	}
	
	@Override
	public boolean satisfiesGuard(BigDecimal token)
	{
		return inhibitorArc.isEnabledBy(token);
	}
	
	public void setUnderlyingArc(TimedInhibitorArc arc){
		this.inhibitorArc = arc;
	}
	
	public TimedInhibitorArc underlyingTimedInhibitorArc(){
		return inhibitorArc;
	}
	
	@Override
	public void delete() {
		if(inhibitorArc != null) inhibitorArc.delete();
		super.delete();
	}
	
	@Override
	public void updateWeightLabel() {
		if(inhibitorArc != null){
			weightLabel.setText(inhibitorArc.interval().toString());
		}else{
			weightLabel.setText("");
		}
		this.setWeightLabelPosition();	
	}
	
	@Override
	public String getGuardAsString() {
		return inhibitorArc.interval().toString();
	}
	
	@Override
	public Command setGuard(TimeInterval guard) {
		
		TimeInterval oldTimeInterval = inhibitorArc.interval();
		inhibitorArc.setTimeInterval(guard);

		//hacks - I use the weight to display the TimeInterval
		updateWeightLabel();
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval, inhibitorArc.interval());
	}
	
	@Override
	public TimeInterval getGuard() {
		return inhibitorArc.interval();
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
	      //super.paintComponent(g);
	      Graphics2D g2 = (Graphics2D)g;   
	      
	      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
	                          RenderingHints.VALUE_ANTIALIAS_ON);
	      
	      g2.translate(COMPONENT_DRAW_OFFSET + zoomGrow - myPath.getBounds().getX(),
	               COMPONENT_DRAW_OFFSET + zoomGrow - myPath.getBounds().getY());
	      
	      if (selected && !ignoreSelection){
	         g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
	      } else{
	         g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
	      }
	     
	      g2.setStroke(new BasicStroke(0.01f * zoom));
	      g2.draw(myPath);
	      
	      g2.translate(myPath.getPoint(myPath.getEndIndex()).getX(),
	               myPath.getPoint(myPath.getEndIndex()).getY());
	        
	      g2.rotate(myPath.getEndAngle()+Math.PI);
	      g2.setColor(java.awt.Color.WHITE);
	            
	      AffineTransform reset = g2.getTransform();
	      g2.transform(Zoomer.getTransform(zoom));   
	  
	      g2.setStroke(new BasicStroke(0.8f));      
	      g2.fillOval(-4,-8, 8, 8);
	  
	      if (selected && !ignoreSelection){
	         g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
	      } else{
	         g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
	      }
	      g2.drawOval(-4,-8, 8, 8);
	      
	      g2.setTransform(reset);
	   }   
}
