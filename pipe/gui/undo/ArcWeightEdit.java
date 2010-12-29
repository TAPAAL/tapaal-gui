/*
 * ArcWeightEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.Arc;
import dk.aau.cs.gui.undo.Command;

/**
 *
 * @author Pere Bonet
 */
public class ArcWeightEdit 
        extends Command {
   
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
   @Override
public void undo() {
      arc.setWeight(oldWeight);
   }

   
   /** */
   @Override
public void redo() {
      arc.setWeight(newWeight);
   }
   
   
   @Override
public String toString(){
      return super.toString() + " " + arc.getName() + 
              "oldWeight: " + oldWeight + "newWeight: " + newWeight;
   }   
   
}
