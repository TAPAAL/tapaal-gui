package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import net.tapaal.gui.petrinet.editor.ConstantsPane;

public class UpdateVariableCommand extends Command {

    private final Variable variable;
    private final String newName;
    private final String oldName;
    private final ColorType newColorType;
    private final ColorType oldColorType;
    private final ConstantsPane.VariablesListModel listModel;

    public UpdateVariableCommand(Variable var, String newName, ColorType colorType, ConstantsPane.VariablesListModel listModel){
        this.variable = var;
        this.newName = newName;
        this.oldName = var.getName();
        this.newColorType = colorType;
        this.oldColorType = var.getColorType();
        this.listModel = listModel;
    }

    @Override
    public void undo() {
        variable.setColorType(oldColorType);
        variable.setName(oldName);
        listModel.updateName();
    }

    @Override
    public void redo() {
        variable.setColorType(newColorType);
        variable.setName(newName);
        listModel.updateName();

    }
}
