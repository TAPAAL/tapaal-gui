package pipe.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.dataLayer.AnnotationNote;


/**
 * Action to toggle the background of a note between white and transparent
 * @author Tim Kimber
 */
public class EditAnnotationBackgroundAction 
        extends AbstractAction {

   private AnnotationNote note;
   
   
   public EditAnnotationBackgroundAction(AnnotationNote an) {
      note = an;
   }
   
   
   public void actionPerformed(ActionEvent e) {
      note.changeBackground();
      note.repaint();
   }

}
