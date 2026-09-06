package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

public class UpdateVariableCommand implements Command {
    private final Variable variable;
    private final String newName;
    private final String oldName;
    private final ColorType newColorType;
    private final ColorType oldColorType;
    private final Runnable onChange;

    public UpdateVariableCommand(Variable var, String newName, ColorType colorType, Runnable onChange){
        this.variable = var;
        this.newName = newName;
        this.oldName = var.getName();
        this.newColorType = colorType;
        this.oldColorType = var.getColorType();
        this.onChange = onChange == null ? () -> {} : onChange;
    }

    @Override
    public void undo() {
        variable.setColorType(oldColorType);
        variable.setName(oldName);
        variable.setId(oldName);
        onChange.run();
    }

    @Override
    public void redo() {
        variable.setColorType(newColorType);
        variable.setName(newName);
        variable.setId(newName);
        onChange.run();
    }
}
