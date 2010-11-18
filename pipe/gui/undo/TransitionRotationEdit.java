/*
 * transitionPriorityEdit.java
 */
package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class TransitionRotationEdit 
        extends Command {
   
   Transition transition;
   Integer angle;
   
   
   /** Creates a new instance of placePriorityEdit */
   public TransitionRotationEdit(Transition _transition, Integer _angle) {
      transition = _transition;
      angle = _angle;
   }

   
   /** */
   @Override
public void undo() {
      transition.rotate(-angle);
   }

   
   /** */
   @Override
public void redo() {
      transition.rotate(angle);
   }
   
}
