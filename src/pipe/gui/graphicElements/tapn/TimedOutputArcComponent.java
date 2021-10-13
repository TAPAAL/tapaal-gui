package pipe.gui.graphicElements.tapn;

import java.awt.Container;
import java.util.Hashtable;

import javax.swing.BoxLayout;

import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.*;
import pipe.gui.CreateGui;

import pipe.gui.Grid;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPath;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GuardDialogue;
import dk.aau.cs.gui.undo.Command;

public class TimedOutputArcComponent extends Arc {

	private dk.aau.cs.model.tapn.TimedOutputArc outputArc;

	public TimedOutputArcComponent(PlaceTransitionObject sourceInput, PlaceTransitionObject targetInput, int weightInput, String idInput) {
		super(sourceInput, targetInput, weightInput, idInput);
	}

	/**
	 * Create Petri-Net Arc object
	 */
	public TimedOutputArcComponent(PlaceTransitionObject newSource) {
		super(newSource);
	}

	public TimedOutputArcComponent(TimedOutputArcComponent arc) {

		super(arc.getSource(), arc.getTarget(), 0, null);

		myPath = new ArcPath(this, arc.myPath);

		this.updateBounds();
		id = arc.id;
		this.setSource(arc.getSource());
		this.setTarget(arc.getTarget());
		this.setNameOffsetX(arc.getNameOffsetX());
		this.setNameOffsetY(arc.getNameOffsetY());
		this.getNameLabel().setPosition(
				Grid.getModifiedX((int) (arc.getNameLabel().getXPosition() + Zoomer.getZoomedValue(getNameOffsetX(), getZoom()))),
				Grid.getModifiedY((int) (arc.getNameLabel().getYPosition() + Zoomer.getZoomedValue(getNameOffsetY(), getZoom())))
        );
		this.lens = arc.lens;
	}


    public TimedOutputArcComponent(PlaceTransitionObject source, PlaceTransitionObject target, TimedOutputArc modelArc, TabContent.TAPNLens lens){
        super(source);
        setTarget(target);
        setUnderlyingArc(modelArc);
        this.lens = lens;
        updateLabel(true);
        sealArc();
    }

	public Command setGuardAndWeight(TimeInterval guard, Weight weight) {

		Weight oldWeight = getWeight();
		setWeight(weight);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

		return new ArcTimeIntervalEdit(this, guard, guard, oldWeight, weight);
	}

	public void updateLabel(boolean displayConstantNames) {
	    if(isColored()){
            if (underlyingArc() != null) {
                if (underlyingArc().getExpression() != null) {
                    getNameLabel().setText(getWeight().toString(displayConstantNames) +
                        "\n" + underlyingArc().getExpression().toString());
                    setLabelPosition();
                }

            } else {
                getNameLabel().setText("");
                getNameLabel().setText(getWeight().toString(displayConstantNames)+" " + getNameLabel().getText());
                setLabelPosition();
            }
        } else{
            getNameLabel().setText("");
            getNameLabel().setText(getWeight().toString(displayConstantNames)+" " + getNameLabel().getText());
            setLabelPosition();
        }
	}

    @Override
    protected void showPropertiesEditor() {
        showTimeIntervalEditor();
    }

    public void showTimeIntervalEditor() {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Arc", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new GuardDialogue(guiDialog.getRootPane(), this, new Context(CreateGui.getCurrentTab())));

		guiDialog.setResizable(true);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

	}

	public dk.aau.cs.model.tapn.TimedOutputArc underlyingArc() {
		return outputArc;
	}

	public void setUnderlyingArc(dk.aau.cs.model.tapn.TimedOutputArc outputArc) {
		this.outputArc = outputArc;
	}

	public TimedOutputArcComponent copy(TimedArcPetriNet tapn, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedOutputArcComponent newCopyArc = new TimedOutputArcComponent(this);
		newCopyArc.setSource(oldToNewMapping.get(this.getSource()));
		newCopyArc.setTarget(oldToNewMapping.get(this.getTarget()));
		newCopyArc.setUnderlyingArc(tapn.getOutputArcFromTransitionAndPlace(tapn.getTransitionByName(outputArc.source().name()), tapn.getPlaceByName(outputArc.destination().name())));
		
		return newCopyArc;
	}

	@Override
	public void setWeight(Weight weight) {
		outputArc.setWeight(weight);
	}

	@Override
	public Weight getWeight() {
		return outputArc.getWeight();
	}
    @Override
    public void setExpression(ArcExpression expr){
        outputArc.setExpression(expr);
    }
    @Override
    public ArcExpression getExpression(){
	    return outputArc.getExpression();
    }

}
