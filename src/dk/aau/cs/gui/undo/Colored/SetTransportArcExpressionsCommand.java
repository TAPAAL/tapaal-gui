package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

public class SetTransportArcExpressionsCommand extends Command {
    TimedTransportArcComponent arc;
    ArcExpression oldInputExpression;
    ArcExpression newInputExpression;
    ArcExpression oldOutputExpression;
    ArcExpression newOutputExpression;

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
