/*
 * SplitInverseArcEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.NormalArc;


/**
 *
 * @author corveau
 */
public class SplitInverseArcEdit 
        extends UndoableEdit {
   
   NormalArc arc;
   
   
   /** Creates a new instance of placeRateEdit */
   public SplitInverseArcEdit(NormalArc _arc) {
      arc = _arc;
   }

   
   /** */
   public void undo() {
      arc.join();
   }

   
   /** */
   public void redo() {
      arc.split();
   }
   
}
