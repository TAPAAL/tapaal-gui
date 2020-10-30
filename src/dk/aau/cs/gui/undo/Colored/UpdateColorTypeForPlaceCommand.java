package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import pipe.gui.CreateGui;

import java.util.ArrayList;
import java.util.List;

public class UpdateColorTypeForPlaceCommand extends Command {
    private TimedPlace place;
    private ColorType oldColorType;
    private ColorType newColorType;
    private  ColorType oldPlaceColorType;
    private List<TimedToken> tokensToRemove;
    private List<ColoredTimeInvariant> invariantsToRemove;

    public UpdateColorTypeForPlaceCommand(TimedPlace place, ColorType oldColorType, ColorType newColorType) {
        this.place = place;
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        tokensToRemove = new ArrayList<>();
        invariantsToRemove = new ArrayList<>();
        oldPlaceColorType = place.getColorType();
    }


    @Override
    public void undo() {

        place.setColorType(oldPlaceColorType);
        place.addTokens(tokensToRemove);
        place.getCtiList().addAll(invariantsToRemove);

        CreateGui.getModel().repaintPlaces(true);
    }

    @Override
    public void redo() {


        if(oldPlaceColorType.equals(oldColorType)) {
            place.setColorType(newColorType);
        }

        if(newColorType instanceof ProductType){
            place.tokens().clear();
            place.getCtiList().clear();
            CreateGui.getModel().repaintPlaces();
            return;
        }

        if (tokensToRemove.isEmpty()) {
            for (TimedToken token : place.tokens()) {
                if (!place.getColorType().contains(token.color())) {
                    tokensToRemove.add(token);
                }
            }
        }

        place.removeTokens(tokensToRemove);

        if (invariantsToRemove.isEmpty()) {
            for (ColoredTimeInvariant invariant : place.getCtiList()) {
                if (!place.getColorType().contains(invariant.getColor())) {
                    invariantsToRemove.add(invariant);
                }
            }
        }

        place.getCtiList().removeAll(invariantsToRemove);

        CreateGui.getModel().repaintPlaces(true);
    }
}
