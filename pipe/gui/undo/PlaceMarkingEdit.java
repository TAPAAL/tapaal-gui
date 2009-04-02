/*
 * PlaceMarkingEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.Place;

/**
 *
 * @author corveau
 */
public class PlaceMarkingEdit 
        extends UndoableEdit {
   
   Place place;
   Integer newMarking;
   Integer oldMarking;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public PlaceMarkingEdit(Place _place,
                           Integer _oldMarking, Integer _newMarking) {
      place = _place;
      oldMarking = _oldMarking;      
      newMarking = _newMarking;
   }

   
   /** */
   public void undo() {
      place.setCurrentMarking(oldMarking);
   }

   
   /** */
   public void redo() {
      place.setCurrentMarking(newMarking);
   }
   
}
