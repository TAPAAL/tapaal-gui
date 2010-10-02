package pipe.gui.handler;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;


/**
 * This class handles mouse clicks by the user. 
 * 
 * @author unknown 
 * @author David Patterson
 * 
 * Change by David Patterson was to fire the selected 
 * transition in the DataLayer, and then record the firing
 * in the animator.
 * 
 * @author Pere Bonet reverted the above change.
 */


public class AnimationHandler 
        extends javax.swing.event.MouseInputAdapter {
   
   
   @Override
public void mouseClicked(MouseEvent e){      
      if (e.getComponent() instanceof Transition) {
         Transition transition = (Transition)e.getComponent();
//Edited by Joakim Byg - It seems enough to check if it is enabled 
//and not if it enabled and do side-effects at the same time (the true argument).          
         if (SwingUtilities.isLeftMouseButton(e)
                 && (transition.isEnabled(/*true*/))) {
        	 
        	 // If animation mode is Select we need to show the animation menu
        	 if (CreateGui.getAnimator().firingmode.getName().equals("Select")){
        		 CreateGui.getAnimator().showSelectSimulatorDialogue(transition);
        	 }
        	 
            CreateGui.getAnimationHistory().clearStepsForward();
            CreateGui.getAnimator().fireTransition(transition);            
            CreateGui.getApp().setRandomAnimationMode(false);
         }
      }
   }
   
}
