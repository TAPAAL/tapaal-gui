/*
 * SetInverseArcEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.NormalArc;


/**
 *
 * @author Pere Bonet
 */
public class SetInverseArcEdit 
        extends UndoableEdit {
   
   private NormalArc arc;
   private NormalArc inverse;
   private boolean junts;
   
   
   /** Creates a new instance of placeRateEdit */
   public SetInverseArcEdit(NormalArc _arc, NormalArc _inverse, boolean _junts){
      arc = _arc;
      inverse = _inverse;
      junts = _junts;
   }

   
   /** */
   @Override
public void undo() {
      arc.clearInverse();
   }

   
   /** */
   @Override
public void redo() {
      inverse.setInverse(arc, junts);
   }
   
}
