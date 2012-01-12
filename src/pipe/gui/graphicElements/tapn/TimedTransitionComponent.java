package pipe.gui.graphicElements.tapn;

import java.awt.BasicStroke;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.BoxLayout;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
import pipe.gui.graphicElements.Transition;
import pipe.gui.handler.AnimationHandler;
import pipe.gui.handler.LabelHandler;
import pipe.gui.handler.TAPNTransitionHandler;
import pipe.gui.handler.TransitionHandler;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.TAPNTransitionEditor;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.event.TimedTransitionEvent;
import dk.aau.cs.model.tapn.event.TimedTransitionListener;

public class TimedTransitionComponent extends Transition {
	private static final long serialVersionUID = -2280012053262288174L;
	private dk.aau.cs.model.tapn.TimedTransition transition;
	private dk.aau.cs.model.tapn.event.TimedTransitionListener listener;
	private GeneralPath dashedOutline;

	public TimedTransitionComponent(double positionXInput, double positionYInput,
			dk.aau.cs.model.tapn.TimedTransition transition) {
		super(positionXInput, positionYInput);
		this.transition = transition;
		listener = timedTransitionListener();
		transition.addTimedTransitionListener(listener);
	}

	public TimedTransitionComponent(double positionXInput,
			double positionYInput, String idInput, String nameInput,
			double nameOffsetXInput, double nameOffsetYInput,
			boolean timedTransition, boolean infServer, int angleInput,
			int priority) {
		super(positionXInput, positionYInput, idInput, nameInput,
				nameOffsetXInput, nameOffsetYInput, infServer,
				angleInput, priority);
		listener = timedTransitionListener();
	}
	
	private TimedTransitionListener timedTransitionListener(){
		return new TimedTransitionListener() {
			public void nameChanged(TimedTransitionEvent e) {
				TimedTransition source = e.source();
				TimedTransitionComponent.super.setName(source.name());
			}

			public void sharedStateChanged(TimedTransitionEvent e) { repaint(); }
		};
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
				"Edit Transition", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new TAPNTransitionEditor(guiDialog.getRootPane(), this, new Context(CreateGui.getCurrentTab())));

		guiDialog.setResizable(true);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	@Override
	public boolean isEnabled() {
		return transition.isEnabled();
	}
	
	@Override
	public boolean isBlueTransition() {
		return transition.isDEnabled();
	}

	public dk.aau.cs.model.tapn.TimedTransition underlyingTransition() {
		return transition;
	}

	public void setUnderlyingTransition(TimedTransition transition) {
		if(this.transition != null && listener != null){
			transition.removeListener(listener);
		}
		transition.addTimedTransitionListener(listener);
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
		super.update(displayConstantNames);
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(transition.isShared()){
			Graphics2D graphics = (Graphics2D)g;
			Stroke oldStroke = graphics.getStroke();
			
			BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] {5.0f}, 0.0f);
			graphics.setStroke(dashed);
			
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.draw(dashedOutline);
						
			graphics.setStroke(oldStroke);
		}
	}
	
	@Override
	protected void constructTransition() {
		super.constructTransition();
		double x = ((componentWidth - TRANSITION_WIDTH - Pipe.DASHED_PADDING) / 2);
		double y = -Pipe.DASHED_PADDING/2;
		double width = TRANSITION_WIDTH + Pipe.DASHED_PADDING;
		double height = TRANSITION_HEIGHT + Pipe.DASHED_PADDING;
		dashedOutline = new GeneralPath(new Rectangle2D.Double(x, y, width, height));
	}
	
	@Override
	public Command rotate(int angleInc) {
		dashedOutline.transform(AffineTransform.getRotateInstance(Math.toRadians(angleInc), (componentWidth) / 2, (componentHeight)  / 2));
		return super.rotate(angleInc);
	}

	public TimedTransitionComponent copy(TimedArcPetriNet tapn, DataLayer guiModel) {
		TimedTransitionComponent transitionComponent = new TimedTransitionComponent(getPositionXObject(), getPositionYObject(), id, transition.name(), nameOffsetX, nameOffsetY, true, false, getAngle(), 0);
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
