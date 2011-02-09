package pipe.gui.handler;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import pipe.dataLayer.Arc;
import pipe.dataLayer.TransportArcComponent;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;

/**
 * @authors Michael Camacho and Tom Barnwell
 * 
 */
public class ArcKeyboardEventHandler extends KeyAdapter {

	private Arc arcBeingDrawn;

	public ArcKeyboardEventHandler(Arc anArc) {
		arcBeingDrawn = anArc;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_META:
		case KeyEvent.VK_WINDOWS:
			// I don't know if it's a java's bug or if I have a configuration
			// problem with my linux box, but there is an issue with the
			// Windows key under linux, so the space key is used as a
			// provisional
			// solution
		case KeyEvent.VK_SPACE: // provisional
			((DrawingSurfaceImpl) arcBeingDrawn.getParent()).setMetaDown(true);
			break;

		case KeyEvent.VK_ESCAPE:
		case KeyEvent.VK_DELETE:
			DrawingSurfaceImpl aView = ((DrawingSurfaceImpl) arcBeingDrawn
					.getParent());
			aView.createArc = null;
			arcBeingDrawn.delete();

			if (arcBeingDrawn instanceof TransportArcComponent) {
				if (aView.transportArcPart1 != null) {
					aView.transportArcPart1.delete();
					aView.transportArcPart1 = null;
				}
			}

			if ((CreateGui.getApp().getMode() == Pipe.FAST_PLACE)
					|| (CreateGui.getApp().getMode() == Pipe.FAST_TRANSITION)) {
				CreateGui.getApp().resetMode();
			}
			aView.repaint();
			break;

		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_META:
		case KeyEvent.VK_WINDOWS:
		case KeyEvent.VK_SPACE: // provisional
			((DrawingSurfaceImpl) arcBeingDrawn.getParent()).setMetaDown(false);
			break;

		default:
			break;
		}
		e.consume();
	}

}
