/*
 * TransitionTimingEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class TransitionTimingEdit
        extends UndoableEdit {
   
   Transition transition;
   
   
   /** Creates a new instance of placeRateEdit */
   public TransitionTimingEdit(Transition _transition) {
      transition = _transition;
   }

   
   /** */
   public void undo() {
      transition.setTimed(!transition.isTimed());
   }

   
   /** */
   public void redo() {
      transition.setTimed(!transition.isTimed());
   }
   
}
