/*
 * TagArcEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.NormalArc;


/**
 *
 * @author corveau
 */
public class TagArcEdit 
        extends UndoableEdit {
   
   NormalArc arc;
   
   
   /** Creates a new instance of TagArcEdit */
   public TagArcEdit(NormalArc _arc) {
      arc = _arc;
   }

   
   /** */
   public void undo() {
      arc.setTagged(!arc.isTagged());
   }

   
   /** */
   public void redo() {
      arc.setTagged(!arc.isTagged());
   }
   
}
