package dk.aau.cs.gui.undo.Colored;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UpdateColorTypeForPlaceCommand extends Command {
    private TimedPlace place;
    private ColorType oldColorType;
    private ColorType newColorType;
    private  ColorType oldPlaceColorType;
    private List<TimedToken> tokensToRemove;
    private List<ColoredTimeInvariant> invariantsToRemove;
    private ArcExpression newTokenExpression;
    private ArcExpression oldTokenExpression;
    private final ArrayList<Color> removedColors = new ArrayList<>();


    public UpdateColorTypeForPlaceCommand(TimedPlace place, ColorType oldColorType, ColorType newColorType) {
        this.place = place;
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        tokensToRemove = new ArrayList<>();
        invariantsToRemove = new ArrayList<>();
        oldPlaceColorType = place.getColorType();
        oldTokenExpression = place.getTokensAsExpression();

        for (TimedToken token : place.tokens()) {
            if (!place.getColorType().contains(token.color())) {
                tokensToRemove.add(token);
            }
        }

        for (ColoredTimeInvariant invariant : place.getCtiList()) {
            if (!place.getColorType().contains(invariant.getColor())) {
                invariantsToRemove.add(invariant);
            }
        }

        for(Color color : oldColorType.getColors()) {
            if (!newColorType.getColors().contains(color)) {
                removedColors.add(color);
            }
        }

        AllExpression oldColorExpr = new AllExpression(oldColorType);
        AllExpression newColorExpr = new AllExpression(newColorType);
        if(place.getTokensAsExpression() != null){
            newTokenExpression = place.getTokensAsExpression().deepCopy();
            newTokenExpression.replace(oldColorExpr, newColorExpr);
            for(Color color : removedColors){
                newTokenExpression = newTokenExpression.removeColorFromExpression(color, newColorType);
                if(newTokenExpression == null){
                    break;
                }
            }
        }
        if(newTokenExpression != null){
            updateColorTypesOnColorsInExpression();
        }
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


        if(oldPlaceColorType.equals(oldColorType)) {
            place.setColorType(newColorType);
        }

        if(newColorType instanceof ProductType){
            place.tokens().clear();
            place.getCtiList().clear();
            newTokenExpression = buildSingleDotExpression();
            place.setTokenExpression(newTokenExpression);
            CreateGui.getModel().repaintPlaces();
            return;
        }

        place.setTokenExpression(newTokenExpression);

        place.removeTokens(tokensToRemove);

        place.getCtiList().removeAll(invariantsToRemove);

        CreateGui.getModel().repaintPlaces(true);
    }

    private AddExpression buildSingleDotExpression(){
        Vector<ColorExpression> numberOfVector = new Vector<>();
        numberOfVector.add(new DotConstantExpression());
        Vector<ArcExpression> addVector = new Vector<>();
        addVector.add(new NumberOfExpression(1, numberOfVector));
        return new AddExpression(addVector);
    }

    //This function only works on non-tuples
    private void updateColorTypesOnColorsInExpression(){
        for(ArcExpression expr : ((AddExpression)newTokenExpression).getAddExpression()){
            for(ColorExpression cexpr : ((NumberOfExpression)expr).getColor()){
                cexpr = cexpr.getButtomColorExpression();
                if(cexpr instanceof UserOperatorExpression){
                    try {
                        ((UserOperatorExpression)cexpr).getUserOperator().setColorType(newColorType);
                    } catch (RequireException e){
                        System.out.println(e.getMessage());
                    }
                }else if(cexpr instanceof VariableExpression){
                    try {
                        ((VariableExpression)cexpr).getVariable().setColorType(newColorType);
                    } catch (RequireException e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }
}
