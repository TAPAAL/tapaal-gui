package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import pipe.gui.editor.ConstantsPane;

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
