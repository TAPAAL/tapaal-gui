/*
 * DeleteArcPathPointEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.Arc;
import pipe.dataLayer.ArcPath;
import pipe.dataLayer.ArcPathPoint;

/**
 *
 * @author Pere Bonet
 */
public class DeleteArcPathPointEdit
        extends UndoableEdit {
   
   ArcPath arcPath;
   ArcPathPoint point;
   Integer index;

   /** Creates a new instance of placeWeightEdit */
   public DeleteArcPathPointEdit(Arc _arc, ArcPathPoint  _point, Integer _index) {
      arcPath = _arc.getArcPath();
      point = _point;
      index = _index;
   }

   
   /** */
   public void undo() {
      arcPath.insertPoint(index, point);
      arcPath.updateArc();      
   }

   
   /** */
   public void redo() {
      point.delete();
   }
   
}
