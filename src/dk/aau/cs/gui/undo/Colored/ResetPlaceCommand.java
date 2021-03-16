package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.RequireException;
import pipe.gui.CreateGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.RequireException;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
public class ResetPlaceCommand extends Command {
    private TimedPlace place;
    private ColorType oldColorType;
    private ColorType newColorType;
    private  ColorType oldPlaceColorType;
    private List<TimedToken> tokensToRemove;
    private List<ColoredTimeInvariant> invariantsToRemove;
    private ArcExpression newTokenExpression;
    private ArcExpression oldTokenExpression;
    private final ArrayList<Color> removedColors = new ArrayList<>();


    public ResetPlaceCommand(TimedPlace place, ColorType oldColorType) {
        this.place = place;
        this.oldColorType = oldColorType;
        tokensToRemove = place.tokens();
        invariantsToRemove = place.getCtiList();
        oldPlaceColorType = place.getColorType();
        oldTokenExpression = place.getTokensAsExpression();
    }


    @Override
    public void undo() {

        place.setColorType(oldPlaceColorType);
        place.addTokens(tokensToRemove);
        place.getCtiList().addAll(invariantsToRemove);
        place.setTokenExpression(oldTokenExpression);

        CreateGui.getModel().repaintPlaces(true);
    }

    @Override
    public void redo() {


        place.setColorType(ColorType.COLORTYPE_DOT);

        place.setTokenExpression(null);

        place.tokens().clear();

        place.getCtiList().clear();

        CreateGui.getModel().repaintPlaces(true);
    }
}
