/*
 * PlaceMarkingEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.Place;
import dk.aau.cs.gui.undo.Command;

/**
 *
 * @author corveau
 */
public class PlaceMarkingEdit 
        extends Command {
   
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
   @Override
public void undo() {
      place.setCurrentMarking(oldMarking);
   }

   
   /** */
   @Override
public void redo() {
      place.setCurrentMarking(newMarking);
   }
   
}
