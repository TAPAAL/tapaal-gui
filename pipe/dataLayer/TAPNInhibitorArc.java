package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.math.BigDecimal;

import pipe.gui.Pipe;
import pipe.gui.Zoomer;

public class TAPNInhibitorArc extends TimedInputArcComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5492180277264669192L;


	public TAPNInhibitorArc(NormalArc arc)
	{
		super(arc);
	}
	public TAPNInhibitorArc(NormalArc arc, String guard)
	{
		super(arc,guard);
	}
	
	
	public TAPNInhibitorArc(PlaceTransitionObject source) {
		super(source);
	}
	@Override
	public boolean satisfiesGuard(BigDecimal token)
	{
		return !super.satisfiesGuard(token);
	}
	
	
	/**
	 * @version 1.0
	 * @author Pere Bonet
	 */
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
