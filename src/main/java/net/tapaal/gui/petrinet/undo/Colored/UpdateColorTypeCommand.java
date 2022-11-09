package net.tapaal.gui.petrinet.undo.Colored;

import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
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
        for(TimedArcPetriNet tapn : network.allTemplates()){
            for(TimedPlace place : tapn.places()){
                if(place.getColorType().equals(newColorType)){
                    List<TimedToken> oldTokens = new ArrayList<>(place.tokens());
                    place.setColorType(oldColorType);
                    for(TimedToken token : oldTokens){
                        if(oldColorType.contains(token.getColor())){
                            place.addToken(new TimedToken(place, token.age(), oldColorType.getColorByName(token.getColor().getName())));
                        }
                    }
                }
            }
        }

        eval(oldColorType);

        for(Variable var : network.variables()){
            if (var.getColorType().equals(newColorType)){
                var.setColorType(oldColorType);
            }
        }

        colorTypesListModel.updateName();
    }

    @Override
    public void redo() {
        network.colorTypes().set(index, newColorType);
        for(TimedArcPetriNet tapn : network.allTemplates()){
            for(TimedPlace place : tapn.places()){
                if(place.getColorType().equals(oldColorType)){
                    List<TimedToken> oldTokens = new ArrayList<>(place.tokens());
                    place.setColorType(newColorType);
                    for(TimedToken token : oldTokens){
                        if(newColorType.contains(token.getColor())){
                            place.addToken(new TimedToken(place, token.age(), newColorType.getColorByName(token.getColor().getName())));
                        }
                    }
                }
            }
        }

        eval(newColorType);

        for(Variable var : network.variables()){
            if (var.getColorType().equals(oldColorType)){
                var.setColorType(newColorType);
            }
        }

        colorTypesListModel.updateName();
    }

    private void eval(ColorType colorType) {
        for (TimedArcPetriNet tapn : network.allTemplates()) {
            for (TimedPlace place : tapn.places()) {
                ArrayList<TimedToken> tokensToAdd = new ArrayList<>();
                ArcExpression expression = place.getExprWithNewColorType(colorType);

                if (expression != place.getTokensAsExpression()) {
                    ColorMultiset cm = expression.eval(network.getContext());
                    if (cm != null) {
                        tokensToAdd.addAll(cm.getTokens(place));

                        for (TimedToken token : tokensToAdd) {
                            tapn.marking().remove(token);
                        }

                        if (place.getColorType().getName().equals(colorType.getName())) {
                            place.setColorType(colorType);
                        }
                        place.updateTokens(tokensToAdd, expression);
                    }
                }
            }
        }
    }
}
