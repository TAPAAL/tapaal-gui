/*
 * UndoableEdit.java
 */
package pipe.gui.undo;

/**
 * @author Pere Bonet
 */
public abstract class UndoableEdit
implements dk.aau.cs.gui.undo.Command {
   
   
   public abstract void undo();
   
   
   public abstract void redo();

   
   // used for debug purposes
   @Override
public String toString(){
      return this.getClass().toString();
   }
   
}
