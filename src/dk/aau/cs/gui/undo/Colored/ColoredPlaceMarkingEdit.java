package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.TimedToken;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;

import java.util.ArrayList;
import java.util.List;

public class ColoredPlaceMarkingEdit extends Command {

    private final ArrayList<TimedToken> tokenList;
    private final ArrayList<TimedToken> newTokenList;
    private final Context context;
    private final TimedPlaceComponent place;
    private final List<ColoredTimeInvariant> ctiList;
    private final ColorType colorType;
    private ColorType oldColorType;
    List<ColoredTimeInvariant> oldCtiList;
    private ArcExpression oldExpression;
    private ArcExpression newExpression;

    public ColoredPlaceMarkingEdit(ArrayList<TimedToken> tokenList, ArrayList<TimedToken> newTokenList, ArcExpression oldExpression, ArcExpression newExpression,
                                   Context context, TimedPlaceComponent place, List<ColoredTimeInvariant> ctiList, ColorType colorType1){
        this.tokenList = tokenList;
        this.newTokenList = newTokenList;
        this.context = context;
        this.place = place;
        this.ctiList = ctiList;
        this.colorType = colorType1;
        this.oldExpression = oldExpression;
        this.newExpression = newExpression;
    }

    @Override
    public void undo() {

        for (TimedToken token : newTokenList) {
            context.activeModel().marking().remove(token);
        }

        for (TimedToken token : tokenList) {
            context.activeModel().marking().add(token);
        }

        place.underlyingPlace().setColorType(oldColorType);
        place.underlyingPlace().setCtiList(oldCtiList);
        place.underlyingPlace().setTokenExpression(oldExpression);

        place.update(true);
        place.repaint();
    }

    @Override
    public void redo() {
        oldColorType = place.underlyingPlace().getColorType();
        oldCtiList = place.underlyingPlace().getCtiList();

        for (TimedToken token : tokenList) {
            context.activeModel().marking().remove(token);
        }

        for (TimedToken token : newTokenList) {
            context.activeModel().marking().add(token);
        }

        place.underlyingPlace().setCtiList(ctiList);
        place.underlyingPlace().setColorType(colorType);
        place.underlyingPlace().setTokenExpression(newExpression);


        place.update(true);
        place.repaint();

    }
}
