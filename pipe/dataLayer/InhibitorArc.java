package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import pipe.gui.Pipe;
import pipe.gui.Zoomer;


/**
 * @version 1.0
 * @author Pere Bonet
 */
public class InhibitorArc 
        extends Arc {
/*CB Joakim Byg - propably not needed   
   public final static String type = "inhibitor";
EOC*/   
   
   /**
    * Create Petri-Net Arc object
    *
    * @param startPositionXInput Start X-axis Position
    * @param startPositionYInput Start Y-axis Position
    * @param endPositionXInput End X-axis Position
    * @param endPositionYInput End Y-axis Position
    * @param sourceInput Arc source
    * @param targetInput Arc target
    * @param idInput Arc id
    */
   public InhibitorArc(double startPositionXInput, double startPositionYInput,                        double endPositionXInput, double endPositionYInput, 
                       PlaceTransitionObject sourceInput, 
                       PlaceTransitionObject targetInput,
                       int weightInput, 
                       String idInput) {
      super(startPositionXInput, startPositionYInput,
            endPositionXInput, endPositionYInput,
            sourceInput,
            targetInput,
            weightInput,
            idInput);
   }
   
   
   /**
    * Create Petri-Net Arc object
    */
   public InhibitorArc(PlaceTransitionObject newSource) {
      super(newSource);
   }
   
   
   
   public InhibitorArc(InhibitorArc arc) {
      weightLabel = new NameLabel(zoom);
      
      for (int i = 0; i <= arc.myPath.getEndIndex(); i++){
         this.myPath.addPoint(arc.myPath.getPoint(i).getX(),
                              arc.myPath.getPoint(i).getY(),
                              arc.myPath.getPointType(i));         
      }      
      this.myPath.createPath();
      this.updateBounds();  
      this.id = arc.id;
      this.setSource(arc.getSource());
      this.setTarget(arc.getTarget());
      this.setWeight(arc.getWeight());
   }
   
   
   public InhibitorArc paste(double despX, double despY, boolean toAnotherView){
      PlaceTransitionObject source = this.getSource().getLastCopy();
      PlaceTransitionObject target = this.getTarget().getLastCopy();
      
      if (source == null && target == null) {
         // don't paste an arc with neither source nor target
         return null;
      }
      
      if (source == null){
         if (toAnotherView) {
            // if the source belongs to another Petri Net, the arc can't be 
            // pasted
            return null;
         } else {
            source = this.getSource();
         }
      }
      
      if (target == null){
         if (toAnotherView) {
            // if the target belongs to another Petri Net, the arc can't be 
            // pasted
            return null;
         } else {
            target = this.getTarget();
         }
      }

      InhibitorArc copy =
              new InhibitorArc(0, 0, //startPoint
                               0, 0, //endPoint
                               source,
                               target,
                               this.getWeight(),
                               source.getId() + " to " + target.getId());      

      copy.myPath.delete();
      for (int i = 0; i <= this.myPath.getEndIndex(); i++){
         copy.myPath.addPoint(this.myPath.getPoint(i).getX() + despX,
                              this.myPath.getPoint(i).getY() + despY,
                              this.myPath.getPointType(i));         
         //copy.myPath.selectPoint(i);
      }

      source.addConnectFrom(copy);
      target.addConnectTo(copy);
      return copy;
   }
   
   
   public InhibitorArc copy(){
      return new InhibitorArc(this);
   }
    

/*CB Joakim Byg - This is sortof ugly, since it is only used one place 
   and we would like to make an other type of arc that inherits from NormalArc 
    public String getType(){
       return this.type;
    }
EOC*/
   
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
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
