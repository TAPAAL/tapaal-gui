package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

public class UpdateColorTypeOnVariableCommand extends Command {

    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final Variable variable;

    public UpdateColorTypeOnVariableCommand(ColorType oldColorType, ColorType newColorType, Variable variable) {
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.variable = variable;
    }

    @Override
    public void undo() {
        variable.setColorType(oldColorType);
    }

    @Override
    public void redo() {
        variable.setColorType(newColorType);
    }
}
