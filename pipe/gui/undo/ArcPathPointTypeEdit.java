/*
 * ArcPathPointTypeEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.ArcPathPoint;

/**
 *
 * @author corveau
 */
public class ArcPathPointTypeEdit 
        extends UndoableEdit {
   
   ArcPathPoint arcPathPoint;
  
   
   /** Creates a new instance of placeWeightEdit */
   public ArcPathPointTypeEdit(ArcPathPoint _arcPathPoint) {
      arcPathPoint = _arcPathPoint;
   }
   
   
   /** */
   public void undo() {
      arcPathPoint.togglePointType();
   }

   
   /** */
   public void redo() {
      arcPathPoint.togglePointType();
   }

   
   
   public String toString(){
      return super.toString() + " " + arcPathPoint.getName();
   }
      
}
