/*
 * AnnotationTextEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.AnnotationNote;

/**
 *
 * @author corveau
 */
public final class AnnotationTextEdit 
        extends UndoableEdit {
   
   AnnotationNote annotationNote;
   String oldText;
   String newText;
   
   
   /** Creates a new instance of placeRateEdit */
   public AnnotationTextEdit(AnnotationNote _annotationNote,
                             String _oldText, String _newText) {
      annotationNote = _annotationNote;
      oldText = _oldText;
      newText = _newText;
   }

   
   /** */
   public void undo() {
      annotationNote.setText(oldText);
   }

   
   /** */
   public void redo() {
      annotationNote.setText(newText);
   }

   
   public String toString(){
      return super.toString() + " " + annotationNote.getClass().getSimpleName() +
              "oldText: " + oldText + "newText: " + newText;
   }
      
}
