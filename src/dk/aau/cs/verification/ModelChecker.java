package dk.aau.cs.verification;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;

// TODO: MJ -- This interface is getting somewhat bloated -- Try to fix it
public interface ModelChecker {
	boolean setup();
	void setPath(String path) throws IllegalArgumentException;
	boolean useDiscreteSemantics();
	
	String getVersion();

	boolean isCorrectVersion();

	String getPath(); // TODO: MJ -- Delete me when refactoring is done

	VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query) throws Exception;

	void kill();

	boolean supportsStats();
	boolean supportsModel(TimedArcPetriNet model, VerificationOptions options);
	boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query, VerificationOptions options);
	String getStatsExplanation();
}
