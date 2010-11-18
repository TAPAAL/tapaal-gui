/*
 * PlaceCapacityEdit.java
 */

package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.Place;

/**
 *
 * @author corveau
 */
public class PlaceCapacityEdit 
        extends Command {
   
   Place place;
   Integer newCapacity;
   Integer oldCapacity;
   
   
   /**
    * Creates a new instance of PlaceCapacityEdit
    */
   public PlaceCapacityEdit(Place _place,
                            Integer _oldCapacity, Integer _newCapacity) {
      place = _place;
      oldCapacity = _oldCapacity;      
      newCapacity = _newCapacity;
   }

   
   /** */
   @Override
public void undo() {
      place.setCapacity(oldCapacity);
   }
   

   /** */
   @Override
public void redo() {
      place.setCapacity(newCapacity);
   }
   
}
