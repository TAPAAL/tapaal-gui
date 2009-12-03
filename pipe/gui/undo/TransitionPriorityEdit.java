/*
 * TransitionPriorityEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class TransitionPriorityEdit 
        extends UndoableEdit {
   
   Transition transition;
   Integer newPriority;
   Integer oldPriority;
   
   
   /** Creates a new instance of placePriorityEdit */
   public TransitionPriorityEdit(
           Transition _transition, Integer _oldPriority, Integer _newPriority) {
      transition = _transition;
      oldPriority = _oldPriority;      
      newPriority = _newPriority;
   }

   
   /** */
   @Override
public void undo() {
      transition.setPriority(oldPriority);
   }

   
   /** */
   @Override
public void redo() {
      transition.setPriority(newPriority);
   }
   
}
