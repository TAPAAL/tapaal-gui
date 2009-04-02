/*
 * UndoableEdit.java
 */
package pipe.gui.undo;

/**
 * @author Pere Bonet
 */
public abstract class UndoableEdit {
   
   
   public abstract void undo();
   
   
   public abstract void redo();

   
   // used for debug purposes
   public String toString(){
      return this.getClass().toString();
   }
   
}
