/*
 * SetRateParameterEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.RateParameter;
import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class SetRateParameterEdit 
        extends UndoableEdit {
   
   Transition transition;
   Double oldRate;
   RateParameter newRateParameter;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public SetRateParameterEdit(Transition _transition, 
                               Double _oldRate, 
                               RateParameter _newRateParameter) {
      transition = _transition;
      oldRate = _oldRate;
      newRateParameter = _newRateParameter;
   }

   
   /** */
   public void undo() {
      transition.clearRateParameter();
      transition.setRate(oldRate);
   }

   
   /** */
   public void redo() {
      transition.setRateParameter(newRateParameter);
   }
   
}
