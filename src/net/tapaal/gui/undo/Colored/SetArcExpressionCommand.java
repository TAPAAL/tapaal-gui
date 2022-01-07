package net.tapaal.gui.undo.Colored;

import net.tapaal.gui.undo.Command;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import pipe.gui.graphicElements.Arc;

public class SetArcExpressionCommand extends Command {
    private final Arc arc;
    private final ArcExpression oldExpression;
    private final ArcExpression newExpression;

    public SetArcExpressionCommand(Arc arc, ArcExpression oldExpression, ArcExpression newExpression){
        this.arc = arc;
        this.oldExpression = oldExpression;
        this.newExpression = newExpression;
    }

    @Override
    public void undo() {
        arc.setExpression(oldExpression);
        arc.updateLabel(true);
    }

    @Override
    public void redo() {
        arc.setExpression(newExpression);
        arc.updateLabel(true);
    }
}
