package pipe.gui.graphicElements.tapn;

import java.awt.Container;
import java.util.Hashtable;

import javax.swing.BoxLayout;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetType;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
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
		updateWeightLabel(true);
	}

	public TimedInputArcComponent(TimedOutputArcComponent arc) {
		super(arc);
		init();
	}

	public TimedInputArcComponent(TimedOutputArcComponent arc, String guard) {
		super(arc);
		timeInterval = guard;
		updateWeightLabel(true);
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
		updateWeightLabel(true);
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval, inputArc.interval());
	}

	// hacks - I use the weight to display the TimeInterval
	@Override
	public void updateWeightLabel(boolean showConstantNames) {
		if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
			if (inputArc == null)
				weightLabel.setText("");
			else
				weightLabel.setText(inputArc.interval().toString(showConstantNames));

			this.setWeightLabelPosition();
		}
	}

	@Override
	public TimedInputArcComponent copy() {
		return new TimedInputArcComponent(new TimedOutputArcComponent(this), this.timeInterval);
	}

	@Override
	public TimedInputArcComponent paste(double despX, double despY,	boolean toAnotherView) {
		TimedOutputArcComponent copy = new TimedOutputArcComponent(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedInputArcComponent timedCopy = new TimedInputArcComponent(copy.paste(despX, despY, toAnotherView), this.timeInterval);
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
	public void setWeightLabelPosition() {
		weightLabel.setPosition((int) (myPath.midPoint.x)
				+ weightLabel.getWidth() / 2 - 4, (int) (myPath.midPoint.y)
				- ((zoom / 55) * (zoom / 55)));
	}

	public dk.aau.cs.model.tapn.TimedInputArc underlyingTimedInputArc() {
		return inputArc;
	}

	public void setUnderlyingArc(dk.aau.cs.model.tapn.TimedInputArc ia) {
		this.inputArc = ia;
		updateWeightLabel(true);
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