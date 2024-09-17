package net.tapaal.gui.petrinet.undo;

public interface Command {
	void undo();
  void redo();
}
