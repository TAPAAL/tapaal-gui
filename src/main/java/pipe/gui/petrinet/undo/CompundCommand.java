package pipe.gui.petrinet.undo;

import net.tapaal.gui.petrinet.undo.Command;

import java.util.ArrayList;
import java.util.List;

public class CompundCommand extends Command {
    private final List<Command> commands;

    public CompundCommand(List<Command> commands) {
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public void undo() {
        // Undo in reverse order (order matters)
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public void redo() {
        for (Command command : commands) {
            command.redo();
        }
    }
}
