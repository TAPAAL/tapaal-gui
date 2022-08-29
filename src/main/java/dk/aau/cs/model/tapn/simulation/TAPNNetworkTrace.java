package dk.aau.cs.model.tapn.simulation;

public interface TAPNNetworkTrace extends Iterable<TAPNNetworkTraceStep> {
	boolean isConcreteTrace();
	int length();
}
