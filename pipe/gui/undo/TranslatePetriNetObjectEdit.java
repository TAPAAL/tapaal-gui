/*
 * TranslatePetriNetObjectEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.PetriNetObject;


/**
 *
 * @author Pere Bonet
 */
public class TranslatePetriNetObjectEdit 
        extends UndoableEdit {
   
   PetriNetObject pnObject;
   Integer transX;
   Integer transY;
   
   
   /** Creates a new instance of  */
   public TranslatePetriNetObjectEdit(PetriNetObject _pnObject,
                                      Integer _transX, Integer _transY) {
      pnObject = _pnObject;
      transX = _transX;
      transY = _transY;
   }

   
   /** */
   public void undo() {
      pnObject.translate(-transX, -transY);
   }

   
   /** */
   public void redo() {
      pnObject.translate(transX, transY);
   }

   
   public String toString(){
      return super.toString()  + " " + pnObject.getName() + 
              " (" + transX + "," + transY + ")";
   }
   
}
