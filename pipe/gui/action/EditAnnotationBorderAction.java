/*
 * Created on 07-Mar-2004
 * Author is Michael Camacho
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.dataLayer.AnnotationNote;
import pipe.gui.CreateGui;


public class EditAnnotationBorderAction
        extends AbstractAction {

   private AnnotationNote selected;
   

   public EditAnnotationBorderAction(AnnotationNote component) {
      selected = component;
   }

      
   /** Action for editing the text in an AnnotationNote */
   public void actionPerformed(ActionEvent e) {
      CreateGui.getView().getUndoManager().addNewEdit(
               selected.showBorder(!selected.isShowingBorder()));
   }

}
