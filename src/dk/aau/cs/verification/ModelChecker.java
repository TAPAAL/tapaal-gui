package dk.aau.cs.verification;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.petrinet.TAPNQuery;

import dk.aau.cs.petrinet.trace.TAPNTrace;
import dk.aau.cs.util.Tuple;

// TODO: MJ -- This interface is getting somewhat bloated -- Try to fix it
public interface ModelChecker {
	boolean setup();

	String getVersion();

	boolean isCorrectVersion();

	String getPath(); // TODO: MJ -- Delete me when refactoring is done

	VerificationResult<TAPNTrace> verify(VerificationOptions options,
			Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query);

	void kill();
}
