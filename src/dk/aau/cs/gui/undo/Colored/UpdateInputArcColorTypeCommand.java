package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.Weight;
import pipe.gui.CreateGui;

import java.util.ArrayList;
import java.util.List;

public class UpdateInputArcColorTypeCommand extends Command {

    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final TimedInputArc arc;
    private final ArrayList<Color> removedColors;
    private final List<Variable> varsToRemove;
    List<ColoredTimeInterval> intervalsToRemove;

    private ColorExpression oldColorExpr;
    private ColorExpression newColorExpr;

    private ArcExpression oldArcExpression;
    private Weight oldWeight;

    public UpdateInputArcColorTypeCommand(ColorType oldColorType, ColorType newColorType, TimedInputArc arc, List<Variable> variables) {
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.arc = arc;
        this.varsToRemove = new ArrayList<>();
        this.removedColors = new ArrayList<>();
        oldColorExpr = new AllExpression(oldColorType);
        newColorExpr = new AllExpression(newColorType);
        intervalsToRemove = new ArrayList<>();
        oldArcExpression = arc.getArcExpression().copy();
        oldWeight = arc.getWeight();

        for(Color color : oldColorType.getColors()){
            if(!newColorType.getColors().contains(color)){
                removedColors.add(color);
            }
        }
        if(oldColorType instanceof ProductType) {
            for(ColorType ct : ((ProductType) oldColorType).getConstituents()){
                for(Color color : ct.getColors()){
                    if(!newColorType.getColors().contains(color)){
                        removedColors.add(color);
                    }
                }
            }
        }

        for(ColoredTimeInterval interval : arc.getColorTimeIntervals()){
            if(!newColorType.getColors().contains(interval.getColor())){
                intervalsToRemove.add(interval);
            }
        }

        for(Variable var : variables){
            if(!var.getColorType().equals(arc.source().getColorType())){
                varsToRemove.add(var);
            }
        }
    }

    @Override
    public void undo() {

        arc.setExpression(oldArcExpression);
        arc.setWeight(oldWeight);
        arc.getColorTimeIntervals().addAll(intervalsToRemove);

        CreateGui.getModel().repaintAll(true);
    }

    @Override
    public void redo() {

        if(newColorType instanceof ProductType){
            arc.createNewArcExpression(newColorType);
            CreateGui.getModel().repaintAll(true);
            return;
        }

        ArcExpression arcExpr = arc.getArcExpression().deepCopy();
        arcExpr.replace(oldColorExpr, newColorExpr);
        for(Color color : removedColors){
            arcExpr = arcExpr.removeColorFromExpression(color, newColorType);
            if(arcExpr == null){
                break;
            }
        }

        if(arcExpr != null){
            arcExpr = arcExpr.removeExpressionVariables(varsToRemove);
        }

        if(arcExpr != null){
            arc.setExpression(arcExpr);
        } else{
            arc.createNewArcExpression(newColorType);
        }

        arc.getColorTimeIntervals().removeAll(intervalsToRemove);

        CreateGui.getModel().repaintAll(true);
    }


}
