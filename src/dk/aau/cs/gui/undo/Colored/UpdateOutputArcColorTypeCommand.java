package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.Weight;
import pipe.gui.CreateGui;

import java.util.ArrayList;
import java.util.List;

public class UpdateOutputArcColorTypeCommand extends Command {

    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final TimedOutputArc arc;
    private final ArrayList<Color> removedColors;
    private final List<Variable> varsToRemove;

    private ColorExpression oldColorExpr;
    private ColorExpression newColorExpr;

    private ArcExpression oldArcExpression;
    private Weight oldWeight;

    public UpdateOutputArcColorTypeCommand(ColorType oldColorType, ColorType newColorType, TimedOutputArc arc, List<Variable> variables) {
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.arc = arc;
        this.varsToRemove = new ArrayList<>();
        this.removedColors = new ArrayList<>();
        oldColorExpr = new AllExpression(oldColorType);
        newColorExpr = new AllExpression(newColorType);
        oldArcExpression = arc.getExpression();
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

        for(Variable var : variables){
            if(!var.getColorType().equals(arc.destination().getColorType())){
                varsToRemove.add(var);
            }
        }

    }

    @Override
    public void undo() {

        arc.setExpression(oldArcExpression);
        arc.setWeight(oldWeight);

        CreateGui.getModel().repaintAll(true);
    }

    @Override
    public void redo() {

        if(newColorType instanceof ProductType){
            arc.createNewArcExpression();
            CreateGui.getModel().repaintAll(true);
            return;
        }

        ArcExpression arcExpr = arc.getExpression().deepCopy();
        arcExpr.replace(oldColorExpr, newColorExpr);
        for(Color color : removedColors){
            arcExpr = arcExpr.removeColorFromExpression(color, newColorType);
            if(arcExpr == null){
                break;
            }
        }
        if (arcExpr != null) {
            arcExpr = arcExpr.removeExpressionVariables(varsToRemove);
        }

        if(arcExpr != null){
            arc.setExpression(arcExpr);
        } else{
            arc.createNewArcExpression();
        }

        CreateGui.getModel().repaintAll(true);
    }


}
