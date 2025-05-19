package dk.aau.cs.model.tapn.simulation;

public interface TAPNNetworkTrace extends Iterable<TAPNNetworkTraceStep> {
	boolean isConcreteTrace();
    
    default boolean isColoredTrace(){
        return false;
    }

	int length();
}
