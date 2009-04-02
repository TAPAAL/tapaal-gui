/*
 * RateParameterValueEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.RateParameter;

/**
 *
 * @author corveau
 */
public class RateParameterValueEdit 
        extends UndoableEdit {
   
   RateParameter rateParameter;
   Double newValue;
   Double oldValue;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public RateParameterValueEdit(RateParameter _rateParameter,
                           Double _oldValue, Double _newValue) {
      rateParameter = _rateParameter;
      oldValue = _oldValue;      
      newValue = _newValue;
   }

   
   /** */
   public void undo() {
      rateParameter.setValue(oldValue);
      rateParameter.update();
      rateParameter.updateBounds();
   }

   
   /** */
   public void redo() {
      rateParameter.setValue(newValue);
      rateParameter.update();
      rateParameter.updateBounds();
   }
   
}
