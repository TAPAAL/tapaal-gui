/*
 * TransitionServerSemanticEdit.java
 */
package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class TransitionServerSemanticEdit
        extends Command {
   
   Transition transition;
   
   
   /** Creates a new instance of TransitionServerSemanticEdit */
   public TransitionServerSemanticEdit(Transition _transition) {
      transition = _transition;
   }

   
   /** */
   @Override
public void undo() {
      transition.setInfiniteServer(!transition.isInfiniteServer());
   }

   
   /** */
   @Override
public void redo() {
      transition.setInfiniteServer(!transition.isInfiniteServer());
   }
   
}
