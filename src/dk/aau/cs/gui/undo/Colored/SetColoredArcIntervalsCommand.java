package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;

import java.util.List;

public class SetColoredArcIntervalsCommand extends Command {
    TimedInputArcComponent arc;
    List<ColoredTimeInterval> oldIntervalList;
    List<ColoredTimeInterval> newIntervalList;
    public SetColoredArcIntervalsCommand(TimedInputArcComponent arc, List<ColoredTimeInterval> oldIntervalList, List<ColoredTimeInterval> newIntervalList){
        this.arc = arc;
        this.oldIntervalList = oldIntervalList;
        this.newIntervalList = newIntervalList;
    }
    @Override
    public void undo() {
        arc.setCtiList(oldIntervalList);
        arc.updateLabel(true);
    }

    @Override
    public void redo() {
        arc.setCtiList(newIntervalList);
        arc.updateLabel(true);
    }
}
