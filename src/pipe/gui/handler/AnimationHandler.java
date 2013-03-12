package pipe.gui.handler;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import pipe.gui.CreateGui;
import pipe.gui.GuiFrame;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.model.tapn.TimedTransition;

/**
 * This class handles mouse clicks by the user.
 * 
 * @author unknown
 * @author David Patterson
 * 
 *         Change by David Patterson was to fire the selected transition in the
 *         DataLayer, and then record the firing in the animator.
 * 
 * @author Pere Bonet reverted the above change.
 */

public class AnimationHandler extends javax.swing.event.MouseInputAdapter {

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getComponent() instanceof TimedTransitionComponent && CreateGui.getApp().getGUIMode().equals(GuiFrame.GUIMode.animation)) {
			TimedTransition transition = ((TimedTransitionComponent) e.getComponent()).underlyingTransition();

			if (SwingUtilities.isLeftMouseButton(e)) {
				if(transition.isDEnabled()){
					CreateGui.getAnimationHistory().clearStepsForward();
					CreateGui.getAnimator().dFireTransition(transition);
					CreateGui.getApp().setRandomAnimationMode(false);
				}
			}
		}
	}

}
