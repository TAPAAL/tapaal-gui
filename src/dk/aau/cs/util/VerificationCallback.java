package dk.aau.cs.util;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.VerificationResult;

public interface VerificationCallback extends Runnable {
	public void run(VerificationResult<TAPNNetworkTrace> result);
}
