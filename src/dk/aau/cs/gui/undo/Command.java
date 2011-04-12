package dk.aau.cs.gui.undo;

// TODO: change to interface
public abstract class Command {
	public abstract void undo();

	public abstract void redo();
}
