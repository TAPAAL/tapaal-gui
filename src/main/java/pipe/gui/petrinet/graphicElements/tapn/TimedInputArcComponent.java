package pipe.gui.petrinet.graphicElements.tapn;

import java.util.Hashtable;
import java.util.List;

import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import pipe.gui.TAPAALGUI;
import pipe.gui.Constants;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.undo.ArcTimeIntervalEditCommand;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.ConstantWeight;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.Weight;

public class TimedInputArcComponent extends TimedOutputArcComponent {

	private TimedInputArc inputArc;

	public TimedInputArcComponent(PlaceTransitionObject source) {
		super(source);
		updateLabel(true);
	}

	public TimedInputArcComponent(PlaceTransitionObject source, PlaceTransitionObject target, TimedInputArc modelArc, TAPNLens lens){
	    super(source);
	    setTarget(target);
	    setUnderlyingArc(modelArc);
	    updateLabel(true);
	    this.lens = lens;
	    sealArc();
    }

	public TimedInputArcComponent(TimedOutputArcComponent arc) {
		super(arc);
		updateLabel(true);
	}

    public TimedInputArcComponent(TimedOutputArcComponent arc, TAPNLens lens) {
        super(arc);
        updateLabel(true);
        this.lens = lens;
    }

	public String getGuardAsString() {
		return getGuard().toString();
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

		return new ArcTimeIntervalEditCommand(this, oldTimeInterval, inputArc.interval(), oldWeight, weight);
	}

	// hacks - I use the weight to display the TimeInterval
	@Override
	public void updateLabel(boolean showConstantNames) {
        if (inputArc == null)
            getNameLabel().setText("");
        else {
            if (lens != null && lens.isColored()) {
                StringBuilder arcPrint = new StringBuilder();
                if (inputArc.getArcExpression() != null) {
                    arcPrint = new StringBuilder(inputArc.getArcExpression().toString());
                }
                if (lens.isTimed()) {
                    arcPrint.append("\n");
                    arcPrint.append(inputArc.interval().toString(showConstantNames));
                    arcPrint.append("\n");
                    List<ColoredTimeInterval> ctiList;
                    ctiList = this.underlyingTimedInputArc().getColorTimeIntervals();
                    for (ColoredTimeInterval coloredTimeInterval : ctiList) {
                        if (coloredTimeInterval != null){
                            arcPrint.append(coloredTimeInterval);
                            arcPrint.append("\n");
                        }
                    }
                }
                getNameLabel().setText(arcPrint.toString());
            } else if (!TAPAALGUI.getAppGui().showZeroToInfinityIntervals() || (lens != null && !lens.isTimed())) {
                if (inputArc.interval().toString(showConstantNames).equals("[0,inf)")){
                    getNameLabel().setText("");
                }
                else {
                    getNameLabel().setText(inputArc.interval().toString(showConstantNames));
                }
            } else {
                getNameLabel().setText(inputArc.interval().toString(showConstantNames));
            }

            if(getNameLabel().getText().contains("inf")) {
                String intervalStringWithInfSymbol = getNameLabel().getText().replace("inf", Character.toString('\u221e'));
                getNameLabel().setText(intervalStringWithInfSymbol);
            }

            getNameLabel().setText(getWeight().toString(showConstantNames)+" "+getNameLabel().getText());

            // Handle constant highlighting
            boolean focusedConstant = false;
            boolean isvisible = true;
            if(inputArc.interval().lowerBound() instanceof ConstantBound){
                if(((ConstantBound) inputArc.interval().lowerBound()).constant().hasFocus()){
                    focusedConstant = true;
                }

                if(!((ConstantBound) inputArc.interval().lowerBound()).constant().getVisible()) {
                    isvisible = false;
                }
            }
            if(inputArc.interval().upperBound() instanceof ConstantBound){
                if(((ConstantBound) inputArc.interval().upperBound()).constant().hasFocus()){
                    focusedConstant = true;
                }
                if(!((ConstantBound) inputArc.interval().upperBound()).constant().getVisible()){
                    isvisible = false;
                }
            }
            if(getWeight() instanceof ConstantWeight){
                if(((ConstantWeight) getWeight()).constant().hasFocus()){
                    focusedConstant = true;
                }
                /*if(((ConstantWeight) getWeight()).constant().getVisible()){
                    focusedConstant = false;
                }*/
            }
            if(focusedConstant){
                getNameLabel().setForeground(Constants.SELECTION_TEXT_COLOUR);
            }else{
                getNameLabel().setForeground(Constants.ELEMENT_TEXT_COLOUR);
            }
            pnName.setVisible(isvisible);

        }
        this.setLabelPosition();
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

	@Override
    public void setExpression(ArcExpression expr){
	    inputArc.setExpression(expr);
    }

    @Override
    public ArcExpression getExpression(){
        return inputArc.getArcExpression();
    }

    public void setCtiList(List<ColoredTimeInterval> ctiList){
	    inputArc.setColorTimeIntervals(ctiList);
    }

    public List<ColoredTimeInterval> getCtiList(){
	    return inputArc.getColorTimeIntervals();
    }
}