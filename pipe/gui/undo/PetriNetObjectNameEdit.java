/*
 * PetriNetObjectNameEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.PetriNetObject;


/**
 *
 * @author corveau
 */
public class PetriNetObjectNameEdit 
        extends UndoableEdit {
   
   PetriNetObject pno;
   String oldName;
   String newName;
   
   
   /** Creates a new instance of placeNameEdit */
   public PetriNetObjectNameEdit(PetriNetObject _pno,
                            String _oldName, String _newName) {
      pno = _pno;
      oldName = _oldName;      
      newName = _newName;
   }

   
   /** */
   public void undo() {
      pno.setName(oldName);
   }

   
   /** */
   public void redo() {
      pno.setName(newName);
   }
   
}
