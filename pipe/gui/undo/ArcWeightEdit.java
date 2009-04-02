/*
 * ArcWeightEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.Arc;

/**
 *
 * @author Pere Bonet
 */
public class ArcWeightEdit 
        extends UndoableEdit {
   
   Arc arc;
   Integer newWeight;
   Integer oldWeight;
   
   
   /** Creates a new instance of placeWeightEdit */
   public ArcWeightEdit(Arc _arc, Integer _oldWeight, Integer _newWeight) {
      arc = _arc;
      oldWeight = _oldWeight;      
      newWeight = _newWeight;
   }

   
   /** */
   public void undo() {
      arc.setWeight(oldWeight);
   }

   
   /** */
   public void redo() {
      arc.setWeight(newWeight);
   }
   
   
   public String toString(){
      return super.toString() + " " + arc.getName() + 
              "oldWeight: " + oldWeight + "newWeight: " + newWeight;
   }   
   
}
