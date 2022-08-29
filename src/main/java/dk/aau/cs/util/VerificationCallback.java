package dk.aau.cs.util;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.VerificationResult;

@FunctionalInterface
public interface VerificationCallback {
	void run(VerificationResult<TAPNNetworkTrace> result);
}
