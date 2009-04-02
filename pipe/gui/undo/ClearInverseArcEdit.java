/*
 * ClearInverseArcEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.NormalArc;


/**
 *
 * @author corveau
 */
public class ClearInverseArcEdit
        extends UndoableEdit {
   
   private NormalArc arc;
   private NormalArc inverse;
   private boolean junts;
   
   
   /** Creates a new instance of placeRateEdit */
   public ClearInverseArcEdit(NormalArc _arc, NormalArc _inverse, boolean _junts){
      arc = _arc;
      inverse = _inverse;
      junts = _junts;
   }

   
   /** */
   public void undo() {
      inverse.setInverse(arc, junts);
   }

   
   /** */
   public void redo() {
      arc.clearInverse();
   }
   
}
