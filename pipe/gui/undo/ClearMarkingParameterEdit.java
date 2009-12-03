/*
 * ClearMarkingParameterEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.Place;

/**
 *
 * @author corveau
 */
public class ClearMarkingParameterEdit
        extends UndoableEdit {
   
   Place place;
   MarkingParameter oldMarkingParameter;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public ClearMarkingParameterEdit(Place _place, 
                                    MarkingParameter _oldMarkingParameter) {
      place = _place;
      oldMarkingParameter = _oldMarkingParameter;
   }

   
   /** */
   @Override
public void undo() {
      place.setMarkingParameter(oldMarkingParameter);      
   }

   
   /** */
   @Override
public void redo() {
      place.clearMarkingParameter();
   }
   
}
