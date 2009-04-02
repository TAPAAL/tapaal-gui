/*
 * PlaceCapacityEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.Place;

/**
 *
 * @author corveau
 */
public class PlaceCapacityEdit 
        extends UndoableEdit {
   
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
   public void undo() {
      place.setCapacity(oldCapacity);
   }
   

   /** */
   public void redo() {
      place.setCapacity(newCapacity);
   }
   
}
