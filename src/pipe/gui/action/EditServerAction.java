package pipe.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.dataLayer.Transition;

/**
 * Action object that can be used to alternate a transition between
 * timed and immediate.
 * @author unknown
 */
public class EditServerAction 
        extends AbstractAction {
   
   private static final long serialVersionUID = 2001;
   
   private Transition selected;
   
   
   public EditServerAction(Transition component) {
      selected = component;
   }
   
   
   /** Action for toggling timing on/off */
   public void actionPerformed(ActionEvent e) {
      boolean currentServer = selected.isInfiniteServer();
      // if currentTimed it true, set it false, if false, set it true
      selected.setInfiniteServer( ! currentServer );
   }
   
}		// end of class EditServerAction
