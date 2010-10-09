package dk.aau.cs.verification;

import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;

// TODO: MJ -- This interface is getting somewhat bloated -- Try to fix it
public interface ModelChecker {
	boolean setup();
		
	String getVersion();
	boolean isCorrectVersion();
	
	String getPath(); // TODO: MJ -- Delete me when refactoring is done

	VerificationResult verify(VerificationOptions options, TimedArcPetriNet model, TAPNQuery query);
	void kill();
}
