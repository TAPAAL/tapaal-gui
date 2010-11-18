/*
 * JoinInverseArcEdit.java
 */
package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.NormalArc;

/**
 *
 * @author corveau
 */
public class JoinInverseArcEdit 
        extends Command {
   
   NormalArc arc;
   
   
   /** Creates a new instance of placeRateEdit */
   public JoinInverseArcEdit(NormalArc _arc) {
      arc = _arc;
   }

   
   /** */
   @Override
public void undo() {
      arc.split();
   }

   
   /** */
   @Override
public void redo() {
      arc.join();
   }
   
}
