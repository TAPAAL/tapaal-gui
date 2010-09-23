package pipe.gui;

import java.io.File;

import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public class RunKBoundAnalysis extends RunVerificationBase {
	public RunKBoundAnalysis(ModelChecker<NTA, UPPAALQuery> modelChecker,
			VerificationOptions options, File modelFile, File queryFile) {
		super(modelChecker, options, modelFile, queryFile);
	}

	@Override
	protected void showResult(VerificationResult result, long verificationTime) {
		
	}
}
