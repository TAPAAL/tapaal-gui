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
   @Override
public void undo() {
      inverse.setInverse(arc, junts);
   }

   
   /** */
   @Override
public void redo() {
      arc.clearInverse();
   }
   
}
