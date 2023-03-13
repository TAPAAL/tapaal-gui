package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.Context;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.tapn.TimedToken;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

import java.util.ArrayList;
import java.util.List;

public class ColoredPlaceMarkingEditCommand extends Command {

    private final ArrayList<TimedToken> tokenList;
    private final ArrayList<TimedToken> newTokenList;
    private final Context context;
    private final TimedPlaceComponent place;
    private final List<ColoredTimeInvariant> ctiList;
    private final ColorType colorType;
    private final ColorType oldColorType;
    private final List<ColoredTimeInvariant> oldCtiList;
    private final ArcExpression oldExpression;
    private final ArcExpression newExpression;
    private final int oldTokenCount;
    private final int newTokenCount;

    public ColoredPlaceMarkingEditCommand(
        ArrayList<TimedToken> tokenList,
        ArrayList<TimedToken> newTokenList,
        ArcExpression oldExpression,
        ArcExpression newExpression,
        Context context,
        TimedPlaceComponent place,
        List<ColoredTimeInvariant> ctiList,
        ColorType colorType1,
        int oldTokenCount,
        int newTokenCount
    ){
        this.tokenList = tokenList;
        this.newTokenList = newTokenList;
        this.context = context;
        this.place = place;
        this.ctiList = ctiList;
        this.colorType = colorType1;
        this.oldExpression = oldExpression;
        this.newExpression = newExpression;
        this.oldColorType = place.underlyingPlace().getColorType();
        this.oldCtiList = place.underlyingPlace().getCtiList();
        this.oldTokenCount = oldTokenCount;
        this.newTokenCount = newTokenCount;
    }

    @Override
    public void undo() {
        for (TimedToken token : newTokenList) {
            context.activeModel().marking().remove(token);
        }

        place.underlyingPlace().setColorType(oldColorType);
        place.underlyingPlace().setCtiList(oldCtiList);
        place.underlyingPlace().updateTokens(tokenList, oldExpression);
        place.underlyingPlace().setNumberOfTokens(oldTokenCount);

        place.update(true);
        place.repaint();
    }

    @Override
    public void redo() {
        for (TimedToken token : tokenList) {
            context.activeModel().marking().remove(token);
        }

        place.underlyingPlace().setCtiList(ctiList);
        place.underlyingPlace().setColorType(colorType);
        place.underlyingPlace().updateTokens(newTokenList, newExpression);
        place.underlyingPlace().setNumberOfTokens(newTokenCount);

        place.update(true);
        place.repaint();
    }
}
