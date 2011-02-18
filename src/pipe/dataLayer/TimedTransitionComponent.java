package pipe.dataLayer;

import java.awt.Container;
import java.awt.Graphics;

import javax.swing.BoxLayout;

import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
import pipe.gui.handler.AnimationHandler;
import pipe.gui.handler.LabelHandler;
import pipe.gui.handler.TAPNTransitionHandler;
import pipe.gui.handler.TransitionHandler;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.TAPNTransitionEditor;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;

public class TimedTransitionComponent extends Transition {
	private static final long serialVersionUID = -2280012053262288174L;
	private dk.aau.cs.model.tapn.TimedTransition transition;

	public TimedTransitionComponent(double positionXInput, double positionYInput,
			dk.aau.cs.model.tapn.TimedTransition transition) {
		super(positionXInput, positionYInput);
		this.transition = transition;
	}

	public TimedTransitionComponent(double positionXInput,
			double positionYInput, String idInput, String nameInput,
			double nameOffsetXInput, double nameOffsetYInput,
			boolean timedTransition, boolean infServer, int angleInput,
			int priority) {
		super(positionXInput, positionYInput, idInput, nameInput,
				nameOffsetXInput, nameOffsetYInput, timedTransition, infServer,
				angleInput, priority);
		transition = new dk.aau.cs.model.tapn.TimedTransition(nameInput);
	}

	@Override
	public void delete() {
		if (transition != null)
			transition.delete();
		super.delete();
	}

	@Override
	public void showEditor() {
		// Build interface
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(),
				Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new TAPNTransitionEditor(guiDialog.getRootPane(), this,
				CreateGui.getModel(), CreateGui.getView(), CreateGui.getCurrentTab().network()), this);

		guiDialog.setResizable(true);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

//	@Override
//	public boolean isEnabled(boolean animationStatus) {
//		if (animationStatus) {
//			if (isEnabled()) {
//				highlighted = true;
//				return true;
//			} else {
//				highlighted = false;
//			}
//		}
//		return false;
//	}

	@Override
	public boolean isEnabled() {
		return transition.isEnabled();
	}

	public dk.aau.cs.model.tapn.TimedTransition underlyingTransition() {
		return transition;
	}

	public void setUnderlyingTransition(TimedTransition transition) {
		this.transition = transition;
		this.setName(transition.name());
		repaint();
	}

	@Override
	public void setName(String nameInput) {
		transition.setName(nameInput);
		super.setName(nameInput);
	}
	
	public String getName() {
		return transition != null ? transition.name() : "";
	}
	
	@Override
	public void update(boolean displayConstantNames) {
		if(transition != null) {
			pnName.setName(transition.name());
			pnName.zoomUpdate(zoom);
		}
		else {
			pnName.setText("");
		}
		//updateBounds();
		//updateLabelLocation();
		//updateConnected();
		
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public TimedTransitionComponent copy(TimedArcPetriNet tapn, DataLayer guiModel) {
		TimedTransitionComponent transitionComponent = new TimedTransitionComponent(positionX, positionY, id, transition.name(), nameOffsetX, nameOffsetY, true, isInfiniteServer(), getAngle(), getPriority());
		transitionComponent.setUnderlyingTransition(tapn.getTransitionByName(transition.name()));
		
		LabelHandler labelHandler = new LabelHandler(transitionComponent.getNameLabel(), transitionComponent);
		transitionComponent.getNameLabel().addMouseListener(labelHandler);
		transitionComponent.getNameLabel().addMouseMotionListener(labelHandler);
		transitionComponent.getNameLabel().addMouseWheelListener(labelHandler);
		
		TransitionHandler transitionHandler = new TAPNTransitionHandler((DrawingSurfaceImpl)getParent(), transitionComponent, guiModel, tapn);
		transitionComponent.addMouseListener(transitionHandler);
		transitionComponent.addMouseMotionListener(transitionHandler);
		transitionComponent.addMouseWheelListener(transitionHandler);

		transitionComponent.addMouseListener(new AnimationHandler());
	
		transitionComponent.setGuiModel(guiModel);
		
		return transitionComponent;
	}

}
