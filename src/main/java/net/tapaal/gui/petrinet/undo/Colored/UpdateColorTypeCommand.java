package net.tapaal.gui.petrinet.undo.Colored;

import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.*;
import net.tapaal.gui.petrinet.editor.ConstantsPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UpdateColorTypeCommand implements Command {
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
        performUpdate(oldColorType, newColorType);
        network.colorTypes().set(index, oldColorType);
        colorTypesListModel.updateName();
    }

    @Override
    public void redo() {
        network.colorTypes().set(index, newColorType);
        performUpdate(newColorType, oldColorType);
        colorTypesListModel.updateName();
    }

    private void performUpdate(ColorType targetType, ColorType sourceType) {
        List<ProductType> modifiedProductTypes = new ArrayList<>();
        for (ColorType ct : network.colorTypes()) {
            if (ct.isProductColorType()) {
                ProductType pt = (ProductType)ct;
                if (pt.contains(sourceType)) {
                    pt.replaceColorType(targetType, sourceType);
                    modifiedProductTypes.add(pt);
                }
            }
        }

        for (TimedArcPetriNet tapn : network.allTemplates()) {
            for (TimedPlace place : tapn.places()) {
                boolean isModifiedProductType = place.getColorType().isProductColorType() && modifiedProductTypes.contains((ProductType)place.getColorType());

                if (place.getColorType().equals(sourceType)) {
                    if(place.getTokensAsExpression() != null) {
                        place.setTokenExpression(place.getTokensAsExpression().getExprConverted(sourceType, targetType));
                    }
                    List<TimedToken> oldTokens = new ArrayList<>(place.tokens());
                    place.setColorType(targetType);
                    for (TimedToken token : oldTokens) {
                        if (targetType.contains(token.getColor())) {
                            place.addToken(new TimedToken(place, token.age(), targetType.getColorByName(token.getColor().getName())));
                        }
                    }
                } else if (isModifiedProductType) {
                    if (place.getTokensAsExpression() != null) {
                        place.setTokenExpression(place.getTokensAsExpression().getExprConverted(sourceType, targetType));
                    }
                    List<TimedToken> oldTokens = new ArrayList<>(place.tokens());
                    List<TimedToken> newTokens = new ArrayList<>();
                    for (TimedToken token : oldTokens) {
                        Color oldColor = token.getColor();
                        Color newColor = updateColorRecursive(oldColor, sourceType, targetType);
                        newTokens.add(new TimedToken(place, token.age(), newColor));
                    }
                    place.updateTokens(newTokens, place.getTokensAsExpression());
                }
            }
            for (TimedInputArc arc : tapn.inputArcs()) {
                if (arc.getArcExpression() != null) {
                    arc.setExpression(arc.getArcExpression().getExprConverted(sourceType, targetType));
                }
            }
            for (TimedOutputArc arc : tapn.outputArcs()) {
                if (arc.getExpression() != null) {
                    arc.setExpression(arc.getExpression().getExprConverted(sourceType, targetType));
                }
            }
            for (TimedTransition transition : tapn.transitions()) {
                if (transition.getGuard() != null) {
                    Expression newGuardExpr = updateExpressionRecursively(transition.getGuard(), sourceType, targetType);
                    if (newGuardExpr instanceof GuardExpression) {
                        transition.setGuard((GuardExpression)newGuardExpr);
                    }
                   
                    transition.getGuard().setColorType(targetType);
                }
            }
        }

        eval(targetType);

        for (Variable var : network.variables()) {
            if (var.getColorType().equals(sourceType)) {
                var.setColorType(targetType);
            }
        }
    }

    private Color updateColorRecursive(Color color, ColorType sourceType, ColorType targetType) {
        if (color == null) return null;
        if (color.getColorType().equals(sourceType)) {
            if (targetType.contains(color)) {
                return targetType.getColorByName(color.getName());
            }
        }

        if (color.getTuple() != null) {
            Vector<Color> newTuple = new Vector<>();
            boolean changed = false;
            for (Color c : color.getTuple()) {
                Color newC = updateColorRecursive(c, sourceType, targetType);
                newTuple.add(newC);
                if (newC != c) changed = true;
            }
            if (changed) {
                return new Color(color.getColorType(), color.getId(), newTuple);
            }
        }
        
        return color;
    }
    
    private Expression updateExpressionRecursively(Expression expr, ColorType oldCt, ColorType newCt) {
        if (expr == null) return null;

        if (expr instanceof UserOperatorExpression) {
            UserOperatorExpression uoe = (UserOperatorExpression) expr;
            if (uoe.getUserOperator().getColorType().getName().equals(oldCt.getName())) {
                return uoe.getExprWithNewColorType(newCt);
            }
            return expr;
        }
        
        Expression result = expr;
        for (ExprStringPosition pos : expr.getChildren()) {
            Expression child = pos.getObject();
            Expression updatedChild = updateExpressionRecursively(child, oldCt, newCt);
            
            if (updatedChild != child) {
                result = result.replace(child, updatedChild);
            }
        }
        return result;
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
