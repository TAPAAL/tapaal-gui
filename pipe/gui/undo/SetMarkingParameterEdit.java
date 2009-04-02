/*
 * SetMarkingParameterEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.Place;

/**
 *
 * @author corveau
 */
public class SetMarkingParameterEdit 
        extends UndoableEdit {
   
   Place place;
   Integer oldMarking;
   MarkingParameter newMarkingParameter;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public SetMarkingParameterEdit(Place _place, 
                                  Integer _oldMarking, 
                                  MarkingParameter _newMarkingParameter) {
      place = _place;
      oldMarking = _oldMarking;
      newMarkingParameter = _newMarkingParameter;
   }

   
   /** */
   public void undo() {
      place.clearMarkingParameter();
      place.setCurrentMarking(oldMarking);
   }

   
   /** */
   public void redo() {
      place.setMarkingParameter(newMarkingParameter);
   }
   
}
