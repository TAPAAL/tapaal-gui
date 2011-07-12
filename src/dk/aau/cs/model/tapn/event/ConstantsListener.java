package dk.aau.cs.model.tapn.event;

public interface ConstantsListener {
	void ConstantChanged(ConstantChangedEvent e);
	void ConstantAdded(ConstantEvent e);
	void ConstantRemoved(ConstantEvent e);
}
