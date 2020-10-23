package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.Weight;
import pipe.gui.CreateGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UpdateInputArcColorTypeCommand extends Command {

    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final TimedInputArc arc;
    private final ArrayList<Color> removedColors;
    List<ColoredTimeInterval> intervalsToRemove;

    private ColorExpression oldColorExpr;
    private ColorExpression newColorExpr;

    private ArcExpression oldArcExpression;
    private Weight oldWeight;

    public UpdateInputArcColorTypeCommand(ColorType oldColorType, ColorType newColorType, TimedInputArc arc) {
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.arc = arc;
        this.removedColors = new ArrayList<>();
        oldColorExpr = new AllExpression(oldColorType);
        newColorExpr = new AllExpression(newColorType);
        intervalsToRemove = new ArrayList<>();
        oldArcExpression = arc.getArcExpression().copy();
        oldWeight = arc.getWeight();
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

        if (removedColors.isEmpty()) {
            for(Color color : oldColorType.getColors()){
                if(!newColorType.getColors().contains(color)){
                    removedColors.add(color);
                }
            }
        }
        if(newColorType instanceof ProductType){
            arc.createNewArcExpression();
            CreateGui.getModel().repaintAll(true);
            return;
        }


        ArcExpression arcExpr = arc.getArcExpression().deepCopy();
        arcExpr.replace(oldColorExpr, newColorExpr);
        for(Color color : removedColors){
            arcExpr = arcExpr.removeColorFromExpression(color);
            if(arcExpr == null){
                break;
            }
        }
        if(arcExpr != null){
            arc.setExpression(arcExpr);
        } else{
            arc.createNewArcExpression();
        }

        if(intervalsToRemove.isEmpty()) {
            for(ColoredTimeInterval interval : arc.getColorTimeIntervals()){
                if(!newColorType.getColors().contains(interval.getColor())){
                    intervalsToRemove.add(interval);
                }
            }
        }
        arc.getColorTimeIntervals().removeAll(intervalsToRemove);

        CreateGui.getModel().repaintAll(true);
    }


}
