package pipe.gui.graphicElements.tapn;

import java.awt.Container;
import java.util.Hashtable;

import javax.swing.BoxLayout;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.handler.TimedArcHandler;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GuardDialogue;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;

public class TimedInputArcComponent extends TimedOutputArcComponent {
	
	private static final long serialVersionUID = 8263782840119274756L;
	private TimedInputArc inputArc;
	protected String timeInterval;

	public TimedInputArcComponent(PlaceTransitionObject source) {
		super(source);
		init();
	}

	private void init() {
		timeInterval = "[0,inf)";
		updateLabel(true);
	}

	public TimedInputArcComponent(TimedOutputArcComponent arc) {
		super(arc);
		init();
	}

	public TimedInputArcComponent(TimedOutputArcComponent arc, String guard) {
		super(arc);
		timeInterval = guard;
		updateLabel(true);
	}

	@Override
	public void delete() {
		if (inputArc != null)
			inputArc.delete();
		super.delete();
	}

	public String getGuardAsString() {
		return inputArc.interval().toString();
	}

	public TimeInterval getGuard() {
		return inputArc.interval();
	}

	public Command setGuard(TimeInterval guard) {

		TimeInterval oldTimeInterval = inputArc.interval();
		inputArc.setTimeInterval(guard);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval, inputArc.interval());
	}

	// hacks - I use the weight to display the TimeInterval
	@Override
	public void updateLabel(boolean showConstantNames) {
		if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
			if (inputArc == null)
				label.setText("");
			else {
				if (!ShowNilToInfinityIntervals.showNilToInfinityIntervals()) {
					if (inputArc.interval().toString(showConstantNames).equals("[0,inf)")){
						label.setText("");
					}
					else {
						label.setText(inputArc.interval().toString(showConstantNames));
					}					
				}
				else {
					label.setText(inputArc.interval().toString(showConstantNames));
				}
			}
			this.setLabelPosition();
		}
	}

	@Override
	public TimedInputArcComponent copy() {
		return new TimedInputArcComponent(new TimedOutputArcComponent(this), timeInterval);
	}

	@Override
	public TimedInputArcComponent paste(double despX, double despY,	boolean toAnotherView) {
		TimedOutputArcComponent copy = new TimedOutputArcComponent(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedInputArcComponent timedCopy = new TimedInputArcComponent(copy.paste(despX, despY, toAnotherView), timeInterval);
		return timedCopy;
	}

	public void showTimeIntervalEditor() {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Timed Arc", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new GuardDialogue(guiDialog.getRootPane(), this));

		guiDialog.setResizable(false);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

	}

	@Override
	public void setLabelPosition() {
		label.setPosition((int) (myPath.midPoint.x)
				+ label.getWidth() / 2 - 4, (int) (myPath.midPoint.y)
				- ((zoom / 55) * (zoom / 55)));
	}

	public dk.aau.cs.model.tapn.TimedInputArc underlyingTimedInputArc() {
		return inputArc;
	}

	public void setUnderlyingArc(dk.aau.cs.model.tapn.TimedInputArc ia) {
		inputArc = ia;
		updateLabel(true);
	}

	public TimedInputArcComponent copy(TimedArcPetriNet tapn, DataLayer guiModel, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedInputArcComponent arc =  new TimedInputArcComponent(this);
		
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		arc.setUnderlyingArc(tapn.getInputArcFromPlaceToTransition(tapn.getPlaceByName(inputArc.source().name()), tapn.getTransitionByName(inputArc.destination().name())));
		
		arc.getSource().addConnectFrom(arc);
		arc.getTarget().addConnectTo(arc);
		
		TimedArcHandler timedArcHandler = new TimedArcHandler((DrawingSurfaceImpl)getParent(), arc);
		arc.addMouseListener(timedArcHandler);
		//arc.addMouseWheelListener(timedArcHandler);
		arc.addMouseMotionListener(timedArcHandler);
		
		arc.setGuiModel(guiModel);
		
		return arc;
	}
}