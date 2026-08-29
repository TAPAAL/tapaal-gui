package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import pipe.gui.petrinet.graphicElements.Arc;

public class SetArcExpressionCommand implements Command {
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
