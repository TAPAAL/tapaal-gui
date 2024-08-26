package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;

import java.util.List;

public class SetColoredArcIntervalsCommand implements Command {
    private final TimedInputArcComponent arc;
    private final List<ColoredTimeInterval> oldIntervalList;
    private final List<ColoredTimeInterval> newIntervalList;

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
