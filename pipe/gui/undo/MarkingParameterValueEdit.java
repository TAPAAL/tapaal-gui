/*
 * MarkingParameterValueEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.MarkingParameter;

/**
 *
 * @author Pere Bonet
 */
public class MarkingParameterValueEdit 
        extends UndoableEdit {
   
   MarkingParameter markingParameter;
   Integer newValue;
   Integer oldValue;
   
   
   /** Creates a new instance of MarkingParameterValueEdit */
   public MarkingParameterValueEdit(MarkingParameter _markingParameter,
                           Integer _oldValue, Integer _newValue) {
      markingParameter = _markingParameter;
      oldValue = _oldValue;      
      newValue = _newValue;
   }

   
   /** */
   public void undo() {
      markingParameter.setValue(oldValue);
      markingParameter.update();
      markingParameter.updateBounds();
   }

   
   /** */
   public void redo() {
      markingParameter.setValue(newValue);
      markingParameter.update();
      markingParameter.updateBounds();
   }
   
}
