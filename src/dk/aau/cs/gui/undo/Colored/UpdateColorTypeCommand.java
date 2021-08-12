package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.*;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ConstantsPane;

import java.math.BigDecimal;
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
                    List<TimedToken> oldTokens = place.tokens();
                    place.setColorType(oldColorType);
                    for(TimedToken token : oldTokens){
                        if(oldColorType.contains(token.getColor())){
                            place.addToken(new TimedToken(place, token.age(), oldColorType.getColorByName(token.getColor().getName())));
                        }
                    }
                }
            }
        }

        for(Variable var : network.variables()){
            if (var.getColorType().equals(newColorType)){
                var.setColorType(oldColorType);
            }
        }
        CreateGui.getModel().repaintPlaces();
        colorTypesListModel.updateName();
    }

    @Override
    public void redo() {
        network.colorTypes().set(index, newColorType);
        for(TimedArcPetriNet tapn : network.allTemplates()){
            for(TimedPlace place : tapn.places()){
                if(place.getColorType().equals(oldColorType)){
                    List<TimedToken> oldTokens = place.tokens();
                    place.setColorType(newColorType);
                    for(TimedToken token : oldTokens){
                        if(newColorType.contains(token.getColor())){
                            place.addToken(new TimedToken(place, token.age(), newColorType.getColorByName(token.getColor().getName())));
                        }
                    }
                }
            }
        }
        for(Variable var : network.variables()){
            if (var.getColorType().equals(oldColorType)){
                var.setColorType(newColorType);
            }
        }
        CreateGui.getModel().repaintPlaces();
        colorTypesListModel.updateName();
    }
}