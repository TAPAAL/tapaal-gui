/*
 * Created on 
 * Author is 
 *
 */
package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;

import pipe.dataLayer.Note;


public class NoteHandler 
        extends PetriNetObjectHandler {
   
   
   public NoteHandler(Container contentpane, Note note) {
      super(contentpane, note);
      enablePopup = true;
   }

   
   @Override
public void mousePressed(MouseEvent e) {      
      if ((e.getComponent() == myObject) || !e.getComponent().isEnabled()){
         super.mousePressed(e);
      }
   }

   
   @Override
public void mouseDragged(MouseEvent e) {
      if ((e.getComponent() == myObject) || !e.getComponent().isEnabled()){
         super.mouseDragged(e);
      }
   }

   
   @Override
public void mouseReleased(MouseEvent e) {
      if ((e.getComponent() == myObject) || !e.getComponent().isEnabled()){
         super.mouseReleased(e);
      }
   }
   
}
