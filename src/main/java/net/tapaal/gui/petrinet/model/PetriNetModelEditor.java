package net.tapaal.gui.petrinet.model;

import java.util.Vector;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.AllExpression;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.NumberOfExpression;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.LocalTimedPlace;

/**
 * Domain-only edit operations used by the Swing editor and undo commands.
 * This class deliberately has no Swing, DataLayer, or graphical-component
 * dependency.
 */
public final class PetriNetModelEditor {

    public LocalTimedPlace createPlace(String name) {
        return new LocalTimedPlace(name, ColorType.COLORTYPE_DOT);
    }

    public TimedTransition createTransition(String name, boolean urgent, boolean uncontrollable) {
        TimedTransition transition = new TimedTransition(name);
        transition.setUrgent(urgent);
        transition.setUncontrollable(uncontrollable);
        return transition;
    }

    public void addPlace(TimedArcPetriNet model, TimedPlace place) {
        model.add(place);
    }

    public void removePlace(TimedArcPetriNet model, TimedPlace place) {
        model.remove(place);
    }

    public void addTransition(TimedArcPetriNet model, TimedTransition transition) {
        model.add(transition);
    }

    public void removeTransition(TimedArcPetriNet model, TimedTransition transition) {
        transition.delete();
    }

    public TimedInputArc createInputArc(TimedPlace place, TimedTransition transition) {
        Vector<ColorExpression> colors = new Vector<>();
        colors.add(place.getColorType().createColorExpressionForFirstColor());
        return new TimedInputArc(
            place,
            transition,
            TimeInterval.ZERO_INF,
            new NumberOfExpression(1, colors)
        );
    }

    public TimedOutputArc createOutputArc(TimedTransition transition, TimedPlace place) {
        Vector<ColorExpression> colors = new Vector<>();
        colors.add(place.getColorType().createColorExpressionForFirstColor());
        return new TimedOutputArc(
            transition,
            place,
            new NumberOfExpression(1, colors)
        );
    }

    public TimedInhibitorArc createInhibitorArc(TimedPlace place, TimedTransition transition) {
        TimedInhibitorArc arc = new TimedInhibitorArc(place, transition);
        Vector<ColorExpression> colors = new Vector<>();
        colors.add(new AllExpression(place.getColorType()));
        arc.setExpression(new NumberOfExpression(1, colors));
        return arc;
    }

    public TransportArc createTransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination) {
        return new TransportArc(source, transition, destination);
    }

    public void addInputArc(TimedArcPetriNet model, TimedInputArc arc) {
        model.add(arc);
    }

    public void removeInputArc(TimedInputArc arc) {
        arc.delete();
    }

    public void addOutputArc(TimedArcPetriNet model, TimedOutputArc arc) {
        model.add(arc);
    }

    public void removeOutputArc(TimedOutputArc arc) {
        arc.delete();
    }

    public void addInhibitorArc(TimedArcPetriNet model, TimedInhibitorArc arc) {
        model.add(arc);
    }

    public void removeInhibitorArc(TimedInhibitorArc arc) {
        arc.delete();
    }

    public void addTransportArc(TimedArcPetriNet model, TransportArc arc) {
        model.add(arc);
    }

    public void removeTransportArc(TransportArc arc) {
        arc.delete();
    }
}
