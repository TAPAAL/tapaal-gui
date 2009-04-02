/*
 * ChangeRateParameterEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.RateParameter;
import pipe.dataLayer.Transition;

/**
 *
 * @author corveau
 */
public class ChangeRateParameterEdit 
        extends UndoableEdit {
   
   Transition transition;
   RateParameter oldRateParameter;
   RateParameter newRateParameter;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public ChangeRateParameterEdit(Transition _transition, 
                                  RateParameter _oldRateParameter,
                                  RateParameter _newRateParameter) {
      transition = _transition;
      oldRateParameter = _oldRateParameter;
      newRateParameter = _newRateParameter;
   }

   
   /** */
   public void undo() {
      transition.changeRateParameter(oldRateParameter);
   }

   
   /** */
   public void redo() {
      transition.changeRateParameter(newRateParameter);
   }
   
}
