package dk.aau.cs.model.tapn.simulation;


public interface TapaalTrace extends Iterable<TapaalTraceStep> {
	boolean isConcreteTrace();
	int length();
}
