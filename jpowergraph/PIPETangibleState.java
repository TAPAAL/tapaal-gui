/*
 * MyNode.java
 *
 * Created on September 28, 2007, 10:37 AM
 */

package jpowergraph;

import net.sourceforge.jpowergraph.painters.node.ShapeNodePainter;
import net.sourceforge.jpowergraph.swtswinginteraction.color.JPowerGraphColor;


/**
 * This class defines a node used in the reachability graph to represent a 
 * Tangible state
 * @author Pere Bonet
 */
public class PIPETangibleState 
        extends PIPENode{
   
   // light_red
   static JPowerGraphColor bgColor = new JPowerGraphColor(255, 102, 102);
    
   private static ShapeNodePainter shapeNodePainter = new ShapeNodePainter(
           ShapeNodePainter.ELLIPSE, bgColor, JPowerGraphColor.LIGHT_GRAY, 
           fgColor);   
   
   
   /**
    * Creates a new instance of PIPETangibleState
    * @param _label    the node id.
    * @param _marking  the marking
    */
   public PIPETangibleState(String label, String marking){
      super(label, marking);
   }

   
   public static ShapeNodePainter getShapeNodePainter(){
      return shapeNodePainter;
   }

   
   public String getNodeType(){
      return "Tangible State";
   }   
   
}
