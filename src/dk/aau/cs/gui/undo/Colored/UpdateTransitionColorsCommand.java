package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.GuardExpression;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.CreateGui;

import java.security.Guard;
import java.util.ArrayList;

public class UpdateTransitionColorsCommand extends Command {
    private final ArrayList<Color> removedColors;
    private final TimedTransition transition;
    private final GuardExpression oldExpression;

    private final ColorType oldColorType;
    private final ColorType newColorType;

    public UpdateTransitionColorsCommand(TimedTransition transition, ColorType oldColorType, ColorType newColorType) {
        this.transition = transition;
        oldExpression = transition.getGuard();
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        removedColors = new ArrayList<>();
    }


    @Override
    public void undo() {
        transition.setGuard(oldExpression);
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

        GuardExpression expr = oldExpression;
        for(Color color : removedColors){
            if(expr != null){
                expr = expr.removeColorFromExpression(color);
            }
        }
        transition.setGuard(expr);

        CreateGui.getModel().repaintAll(true);

    }
}
