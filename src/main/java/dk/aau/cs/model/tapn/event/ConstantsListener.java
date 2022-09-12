package dk.aau.cs.model.tapn.event;

public interface ConstantsListener {
	void constantChanged(ConstantChangedEvent e);
	void constantAdded(ConstantEvent e);
	void constantRemoved(ConstantEvent e);
}
