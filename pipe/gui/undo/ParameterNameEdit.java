/*
 * ParameterNameEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.Parameter;

/**
 *
 * @author corveau
 */
public class ParameterNameEdit 
        extends UndoableEdit {
   
   Parameter parameter;
   String newName;
   String oldName;
   
   
   /** Creates a new instance of placeCapacityEdit */
   public ParameterNameEdit(Parameter _parameter, 
                            String _oldName, String _newName) {
      parameter = _parameter;
      oldName = _oldName;      
      newName = _newName;
   }

   
   /** */
   @Override
public void undo() {
      parameter.setParameterName(oldName);
      parameter.update();
   }

   
   /** */
   @Override
public void redo() {
      parameter.setParameterName(newName);
      parameter.update();
   }
   
}
