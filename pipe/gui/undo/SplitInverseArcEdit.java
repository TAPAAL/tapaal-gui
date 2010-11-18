/*
 * SplitInverseArcEdit.java
 */
package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.NormalArc;


/**
 *
 * @author corveau
 */
public class SplitInverseArcEdit 
        extends Command {
   
   NormalArc arc;
   
   
   /** Creates a new instance of placeRateEdit */
   public SplitInverseArcEdit(NormalArc _arc) {
      arc = _arc;
   }

   
   /** */
   @Override
public void undo() {
      arc.join();
   }

   
   /** */
   @Override
public void redo() {
      arc.split();
   }
   
}
