/*
 * placeRateEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class TransitionRateEdit 
        extends UndoableEdit {
   
   Transition transition;
   Double newRate;
   Double oldRate;
   
   
   /** Creates a new instance of placeRateEdit */
   public TransitionRateEdit(
           Transition _transition, Double _oldRate, Double _newRate) {
      transition = _transition;
      oldRate = _oldRate;      
      newRate = _newRate;
   }

   
   /** */
   @Override
public void undo() {
      transition.setRate(oldRate);
   }

   
   /** */
   @Override
public void redo() {
      transition.setRate(newRate);
   }
   
}
