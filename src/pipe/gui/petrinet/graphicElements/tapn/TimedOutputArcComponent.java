package pipe.gui.petrinet.graphicElements.tapn;

import java.awt.Container;
import java.util.Hashtable;

import javax.swing.BoxLayout;

import net.tapaal.gui.petrinet.Context;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.*;
import pipe.gui.TAPAALGUI;

import pipe.gui.canvas.Grid;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.ArcPath;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.undo.ArcTimeIntervalEditCommand;
import pipe.gui.swingcomponents.EscapableDialog;
import pipe.gui.petrinet.editor.GuardDialogue;
import net.tapaal.gui.petrinet.undo.Command;

public class TimedOutputArcComponent extends Arc {

	private dk.aau.cs.model.tapn.TimedOutputArc outputArc;

	public TimedOutputArcComponent(PlaceTransitionObject sourceInput, PlaceTransitionObject targetInput, int weightInput, String idInput) {
		super(sourceInput, targetInput, idInput);
	}

	/**
	 * Create Petri-Net Arc object
	 */
	public TimedOutputArcComponent(PlaceTransitionObject newSource) {
		super(newSource);
	}

	public TimedOutputArcComponent(TimedOutputArcComponent arc) {

		super(arc.getSource(), arc.getTarget(), null);

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


    public TimedOutputArcComponent(PlaceTransitionObject source, PlaceTransitionObject target, TimedOutputArc modelArc, PetriNetTab.TAPNLens lens){
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

		return new ArcTimeIntervalEditCommand(this, guard, guard, oldWeight, weight);
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
		EscapableDialog guiDialog = new EscapableDialog(TAPAALGUI.getApp(), "Edit Arc", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new GuardDialogue(guiDialog.getRootPane(), this, new Context(TAPAALGUI.getCurrentTab())));

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
