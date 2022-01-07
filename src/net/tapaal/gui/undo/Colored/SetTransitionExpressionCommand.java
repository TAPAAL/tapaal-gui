package net.tapaal.gui.undo.Colored;

import net.tapaal.gui.undo.Command;
import dk.aau.cs.model.CPN.Expressions.GuardExpression;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;

public class SetTransitionExpressionCommand extends Command {
    private final TimedTransitionComponent transition;
    private final GuardExpression oldExpression;
    private final GuardExpression newExpression;

    public SetTransitionExpressionCommand(TimedTransitionComponent transition, GuardExpression oldExpression, GuardExpression newExpression){
        this.transition = transition;
        this.oldExpression = oldExpression;
        this.newExpression = newExpression;
    }

    @Override
    public void undo() {
        transition.setGuardExpression(oldExpression);
        transition.update(true);
    }

    @Override
    public void redo() {
        transition.setGuardExpression(newExpression);
        transition.update(true);
    }
}
