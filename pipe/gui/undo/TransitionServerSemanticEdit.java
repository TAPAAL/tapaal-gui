/*
 * TransitionServerSemanticEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class TransitionServerSemanticEdit
        extends UndoableEdit {
   
   Transition transition;
   
   
   /** Creates a new instance of TransitionServerSemanticEdit */
   public TransitionServerSemanticEdit(Transition _transition) {
      transition = _transition;
   }

   
   /** */
   public void undo() {
      transition.setInfiniteServer(!transition.isInfiniteServer());
   }

   
   /** */
   public void redo() {
      transition.setInfiniteServer(!transition.isInfiniteServer());
   }
   
}
