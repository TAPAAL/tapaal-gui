package dk.aau.cs.gui.smartDraw;

public interface SmartDrawListener {
	void fireStatusChanged(int objectsPlaced);
	void fireStartDraw();
	void fireDone(boolean cancelled);
}
