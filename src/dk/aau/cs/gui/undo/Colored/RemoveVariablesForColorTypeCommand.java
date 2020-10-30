package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import pipe.gui.CreateGui;

import java.util.ArrayList;
import java.util.List;

public class RemoveVariablesForColorTypeCommand extends Command {

    private final ColorType oldColorType;
    private final ColorType newColorType;
    private final List<Variable> variables;
    private final ArrayList<Variable> removedVars;

    public RemoveVariablesForColorTypeCommand(ColorType oldColorType, ColorType newColorType, List<Variable> variables) {
        this.oldColorType = oldColorType;
        this.newColorType = newColorType;
        this.variables = variables;
        this.removedVars = new ArrayList<>();

        for(Variable var : variables){
            if(var.getColorType().equals(oldColorType)){
                removedVars.add(var);
            }
        }
    }

    @Override
    public void undo() {
        variables.addAll(removedVars);
    }

    @Override
    public void redo() {
        variables.removeAll(removedVars);
    }
}
