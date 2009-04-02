/*
 * ClearRateParameterEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.RateParameter;
import pipe.dataLayer.Transition;

/**
 *
 * @author corveau
 */
public class ClearRateParameterEdit 
        extends UndoableEdit {
   
   Transition transition;
   RateParameter oldRateParameter;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public ClearRateParameterEdit(Transition _transition, 
                                 RateParameter _oldRateParameter) {
      transition = _transition;
      oldRateParameter = _oldRateParameter;
   }

   
   /** */
   public void undo() {
      transition.setRateParameter(oldRateParameter);      
   }

   
   /** */
   public void redo() {
      transition.clearRateParameter();
   }
   
}
