/*
 * ChangeMarkingParameterEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.Place;

/**
 *
 * @author corveau
 */
public class ChangeMarkingParameterEdit 
        extends UndoableEdit {
   
   Place place;
   MarkingParameter oldMarkingParameter;
   MarkingParameter newMarkingParameter;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public ChangeMarkingParameterEdit(Place _place, 
                                  MarkingParameter _oldMarkingParameter,
                                  MarkingParameter _newMarkingParameter) {
      place = _place;
      oldMarkingParameter = _oldMarkingParameter;
      newMarkingParameter = _newMarkingParameter;
   }

   
   /** */
   public void undo() {
      place.changeMarkingParameter(oldMarkingParameter);
   }

   
   /** */
   public void redo() {
      place.changeMarkingParameter(newMarkingParameter);
   }
   
}
