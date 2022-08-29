package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;

public class SetTransportArcExpressionsCommand extends Command {
    private final TimedTransportArcComponent arc;
    private final ArcExpression oldInputExpression;
    private final ArcExpression newInputExpression;
    private final ArcExpression oldOutputExpression;
    private final ArcExpression newOutputExpression;

    public SetTransportArcExpressionsCommand(TimedTransportArcComponent arc, ArcExpression oldInputExpression, ArcExpression newInputExpression, ArcExpression oldOutputExpression, ArcExpression newOutputExpression){
        this.arc = arc;
        this.oldInputExpression = oldInputExpression;
        this.oldOutputExpression = oldOutputExpression;
        this.newInputExpression = newInputExpression;
        this.newOutputExpression = newOutputExpression;
    }

    @Override
    public void undo() {
        arc.setInputExpression(oldInputExpression);
        arc.setOutputExpression(oldOutputExpression);
        arc.updateLabel(true);
    }

    @Override
    public void redo() {
        arc.setInputExpression(newInputExpression);
        arc.setOutputExpression(newOutputExpression);
        arc.updateLabel(true);
    }
}
