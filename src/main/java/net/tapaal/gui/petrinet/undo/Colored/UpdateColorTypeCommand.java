package net.tapaal.gui.petrinet.undo.Colored;

import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.*;
import net.tapaal.gui.petrinet.editor.ConstantsPane;

import java.util.ArrayList;
import java.util.List;

public class UpdateColorTypeCommand extends Command {
    private final TimedArcPetriNetNetwork network;
    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final Integer index;
    private final ConstantsPane.ColorTypesListModel colorTypesListModel;

    public UpdateColorTypeCommand(TimedArcPetriNetNetwork network, ColorType oldColorType, ColorType newColorType, Integer index, ConstantsPane.ColorTypesListModel colorTypesListModel) {
        this.network = network;
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.index = index;
        this.colorTypesListModel = colorTypesListModel;
    }

    @Override
    public void undo() {
        network.colorTypes().set(index, oldColorType);
        for (TimedArcPetriNet tapn : network.allTemplates()) {
            for (TimedPlace place : tapn.places()) {
                if (place.getColorType().equals(newColorType)) {
                    List<TimedToken> oldTokens = new ArrayList<>(place.tokens());
                    place.setColorType(oldColorType);
                    for (TimedToken token : oldTokens) {
                        if (oldColorType.contains(token.getColor())) {
                            place.addToken(new TimedToken(place, token.age(), oldColorType.getColorByName(token.getColor().getName())));
                        }
                    }
                }
            }
            for (TimedInputArc arc : tapn.inputArcs()) {
                if (arc.getArcExpression() != null) {
                    arc.setExpression(arc.getArcExpression().getExprWithNewColorType(oldColorType));
                }
            }
            for (TimedOutputArc arc : tapn.outputArcs()) {
                if (arc.getExpression() != null) {
                    arc.setExpression(arc.getExpression().getExprWithNewColorType(oldColorType));
                }
            }
            for (TimedTransition transition : tapn.transitions()) {
                if (transition.getGuard() != null) {
                    transition.getGuard().setColorType(oldColorType);
                }
            }
        }

        eval(oldColorType);

        for (Variable var : network.variables()) {
            if (var.getColorType().equals(newColorType)) {
                var.setColorType(oldColorType);
            }
        }
        colorTypesListModel.updateName();
    }

    @Override
    public void redo() {
        network.colorTypes().set(index, newColorType);
        for (TimedArcPetriNet tapn : network.allTemplates()) {
            for (TimedPlace place : tapn.places()) {
                if (place.getColorType().equals(oldColorType)) {
                    List<TimedToken> oldTokens = new ArrayList<>(place.tokens());
                    place.setColorType(newColorType);
                    for (TimedToken token : oldTokens) {
                        if (newColorType.contains(token.getColor())) {
                            place.addToken(new TimedToken(place, token.age(), newColorType.getColorByName(token.getColor().getName())));
                        }
                    }
                }
            }
            for (TimedInputArc arc : tapn.inputArcs()) {
                if (arc.getArcExpression() != null) {
                    arc.setExpression(arc.getArcExpression().getExprWithNewColorType(newColorType));
                }
            }
            for (TimedOutputArc arc : tapn.outputArcs()) {
                if (arc.getExpression() != null) {
                    arc.setExpression(arc.getExpression().getExprWithNewColorType(newColorType));
                }
            }
            for (TimedTransition transition : tapn.transitions()) {
                if (transition.getGuard() != null) {
                    transition.getGuard().setColorType(newColorType);
                }
            }
        }

        eval(newColorType);

        for (Variable var : network.variables()) {
            if (var.getColorType().equals(oldColorType)) {
                System.out.println();
                var.setColorType(newColorType);
            }
        }
        colorTypesListModel.updateName();
    }

    private void eval(ColorType colorType) {
        for (TimedArcPetriNet tapn : network.allTemplates()) {
            for (TimedPlace place : tapn.places()) {
                ArcExpression expression = place.getExprWithNewColorType(colorType);

                if (expression != place.getTokensAsExpression()) {
                    ColorMultiset cm = expression.eval(network.getContext());
                    if (cm != null) {
                        ArrayList<TimedToken> tokensToAdd = new ArrayList<>(place.tokens());
                        for (TimedToken token : cm.getTokens(place)) {
                            tapn.marking().remove(token);
                        }
                        place.updateTokens(tokensToAdd, expression);
                    }
                }

                ArrayList<ColoredTimeInvariant> invariantsToAdd = new ArrayList<>();
                for (ColoredTimeInvariant invariant : place.getCtiList()) {
                    if (colorType.contains(invariant.getColor())) {
                        invariantsToAdd.add(new ColoredTimeInvariant(invariant.isUpperNonstrict(), invariant.upperBound(), colorType.getColorByName(invariant.getColor().getColorName())));
                    } else {
                        invariantsToAdd.add(invariant);
                    }
                }
                place.setCtiList(invariantsToAdd);

                if (place.getColorType().getName().equals(colorType.getName())) {
                    place.setColorType(colorType);
                }
            }
        }
    }
}
