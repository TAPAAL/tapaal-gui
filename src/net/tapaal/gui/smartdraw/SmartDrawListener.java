package net.tapaal.gui.smartdraw;

public interface SmartDrawListener {
	void fireStatusChanged(int objectsPlaced);
	void fireStartDraw();
	void fireDone(boolean cancelled);
}
