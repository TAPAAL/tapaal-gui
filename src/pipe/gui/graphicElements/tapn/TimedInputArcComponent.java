package pipe.gui.graphicElements.tapn;

import java.util.Hashtable;

import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.handler.TimedArcHandler;
import pipe.gui.undo.ArcTimeIntervalEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.ConstantWeight;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.Weight;

public class TimedInputArcComponent extends TimedOutputArcComponent {
	
	private static final long serialVersionUID = 8263782840119274756L;
	private TimedInputArc inputArc;

	public TimedInputArcComponent(PlaceTransitionObject source) {
		super(source);
		updateLabel(true);

		//XXX: se note in funcation
		addMouseHandler();

	}

	public TimedInputArcComponent(TimedOutputArcComponent arc) {
		super(arc);
		updateLabel(true);

		//XXX: se note in funcation
		addMouseHandler();

	}

	/** @deprecated */
	@Deprecated
	public TimedInputArcComponent(TimedOutputArcComponent arc, String guard) {
		this(arc);
	}

	private void addMouseHandler() {
		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		mouseHandler = new TimedArcHandler(this);
	}

	public String getGuardAsString() {
		return inputArc.interval().toString();
	}

        
	public TimeInterval getGuard() {
		return inputArc.interval();
	}
	
	public boolean isUrgentTransition(){
		return inputArc.destination().isUrgent();
	}

	@Override
	public Command setGuardAndWeight(TimeInterval guard, Weight weight) {

		TimeInterval oldTimeInterval = inputArc.interval();
		inputArc.setTimeInterval(guard);
		Weight oldWeight = getWeight();
		setWeight(weight);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval, inputArc.interval(), oldWeight, weight);
	}

	// hacks - I use the weight to display the TimeInterval
	@Override
	public void updateLabel(boolean showConstantNames) {
		//If there is no model we can't set the labels
		if(CreateGui.getModel() == null) {
			return;
		}
		if (!CreateGui.getModel().netType().equals(NetType.UNTIMED)) {
			if (inputArc == null)
				getNameLabel().setText("");
			else {
				if (!CreateGui.getApp().showZeroToInfinityIntervals()) {
					if (inputArc.interval().toString(showConstantNames).equals("[0,inf)")){
						getNameLabel().setText("");
					}
					else {
						getNameLabel().setText(inputArc.interval().toString(showConstantNames));
					}					
				}
				else {
					getNameLabel().setText(inputArc.interval().toString(showConstantNames));
				}

				getNameLabel().setText(getWeight().toString(showConstantNames)+" "+getNameLabel().getText());
				
				// Handle constant highlighting
				boolean focusedConstant = false;
				if(inputArc.interval().lowerBound() instanceof ConstantBound){
					if(((ConstantBound) inputArc.interval().lowerBound()).constant().hasFocus()){
						focusedConstant = true;
					}
				}
				if(inputArc.interval().upperBound() instanceof ConstantBound){
					if(((ConstantBound) inputArc.interval().upperBound()).constant().hasFocus()){
						focusedConstant = true;
					}
				}
				if(getWeight() instanceof ConstantWeight){
					if(((ConstantWeight) getWeight()).constant().hasFocus()){
						focusedConstant = true;
					}
				}
				if(focusedConstant){
					getNameLabel().setForeground(Pipe.SELECTION_TEXT_COLOUR);
				}else{
					getNameLabel().setForeground(Pipe.ELEMENT_TEXT_COLOUR);
				}
				
			}
			this.setLabelPosition();
		}
	}

	public dk.aau.cs.model.tapn.TimedInputArc underlyingTimedInputArc() {
		return inputArc;
	}

	public void setUnderlyingArc(dk.aau.cs.model.tapn.TimedInputArc ia) {
		inputArc = ia;
		updateLabel(true);
	}

	public TimedInputArcComponent copy(TimedArcPetriNet tapn, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedInputArcComponent arc =  new TimedInputArcComponent(this);
		
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		arc.setUnderlyingArc(tapn.getInputArcFromPlaceToTransition(tapn.getPlaceByName(inputArc.source().name()), tapn.getTransitionByName(inputArc.destination().name())));
		
		return arc;
	}
	
	@Override
	public void setWeight(Weight weight){
		inputArc.setWeight(weight);
	}
	
	@Override
	public Weight getWeight(){
		return inputArc.getWeight();
	}
}